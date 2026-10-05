/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.util.QuestUtils;
import marauroa.common.game.IRPZone;

/** Bounded, quest-independent lantern population, owned by the live event. */
public final class MineTownLanternSpawns {
	private static final Logger LOGGER = Logger.getLogger(MineTownLanternSpawns.class);
	public static final int RESPAWN_SECONDS = 30 * 60;
	private final List<LanternPoint> points = new ArrayList<LanternPoint>();

	public static String itemData() {
		return QuestUtils.evaluateQuestSlotName("minetown_lantern_[year]");
	}

	public void start() {
		stop();
		if (!SeasonalEventService.get().isMineTownEnabled()) {
			return;
		}
		final List<StendhalRPZone> eligible = new ArrayList<StendhalRPZone>();
		for (final IRPZone raw : SingletonRepository.getRPWorld()) {
			final StendhalRPZone zone = (StendhalRPZone) raw;
			if (isEligible(zone)) {
				eligible.add(zone);
			}
		}
		// Neighbouring zone names normally share a region. Shuffle within each trio,
		// not the whole world, so the population remains geographically distributed.
		Collections.sort(eligible, Comparator.comparing(StendhalRPZone::getName));
		for (int offset = 0; offset < eligible.size(); offset += 3) {
			final List<StendhalRPZone> group = new ArrayList<StendhalRPZone>(
					eligible.subList(offset, Math.min(offset + 3, eligible.size())));
			Collections.shuffle(group);
			final int wanted = Math.min(group.size(), 1 + Rand.rand(2));
			int added = 0;
			for (final StendhalRPZone zone : group) {
				if (added >= wanted) {
					break;
				}
				final LanternPoint point = new LanternPoint();
				points.add(point);
				zone.add(point);
				try {
					point.setToFullGrowth();
				} catch (final RuntimeException e) {
					stop();
					throw e;
				}
				if (point.hasLantern()) {
					added++;
				} else {
					points.remove(point);
					zone.remove(point);
				}
			}
		}
		LOGGER.info("Mine Town: " + points.size() + " latarenek na " + eligible.size() + " mapach.");
	}

	static boolean isEligible(final StendhalRPZone zone) {
		final String name = zone.getName();
		return zone.isPublicAccessible() && zone.getWidth() >= 16 && zone.getHeight() >= 16
				&& !name.contains("admin") && !name.contains("jail") && !name.contains("prison")
				&& !name.contains("arena") && !name.contains("deathmatch")
				&& !name.contains("tutorial");
	}

	public void stop() {
		for (final LanternPoint point : points) {
			if (point.getZone() != null) {
				point.getZone().remove(point);
			}
		}
		points.clear();
	}

	/** Reject enclosed islands and secret areas, not just blocked spawn tiles. */
	static boolean isReachable(final StendhalRPZone zone, final Point candidate) {
		return candidate.x >= 0 && candidate.y >= 0 && candidate.x < zone.getWidth()
				&& candidate.y < zone.getHeight()
				&& reachableTiles(zone).get(candidate.y * zone.getWidth() + candidate.x);
	}

	private static BitSet reachableTiles(final StendhalRPZone zone) {
		final int width = zone.getWidth();
		final int height = zone.getHeight();
		final BitSet visited = new BitSet(width * height);
		final BitSet reachable = new BitSet(width * height);
		final ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
		if (!zone.isInterior()) {
			for (int x = 0; x < width; x++) {
				queue.add(x);
				queue.add((height - 1) * width + x);
			}
			for (int y = 0; y < height; y++) {
				queue.add(y * width);
				queue.add(y * width + width - 1);
			}
		}
		for (final Portal portal : zone.getPortals()) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					final int x = portal.getX() + dx;
					final int y = portal.getY() + dy;
					if (x >= 0 && y >= 0 && x < width && y < height) {
						queue.add(y * width + x);
					}
				}
			}
		}
		while (!queue.isEmpty()) {
			final int tile = queue.removeFirst();
			if (visited.get(tile)) {
				continue;
			}
			visited.set(tile);
			final int x = tile % width;
			final int y = tile / width;
			if (zone.collides(x, y) || (zone.secretMap.getWidth() > 0
					&& zone.secretMap.collides(new Rectangle(x, y, 1, 1)))) {
				continue;
			}
			reachable.set(tile);
			if (x > 0) { queue.add(tile - 1); }
			if (x + 1 < width) { queue.add(tile + 1); }
			if (y > 0) { queue.add(tile - width); }
			if (y + 1 < height) { queue.add(tile + width); }
		}
		return reachable;
	}

	static final class LanternPoint extends PassiveEntityRespawnPoint {
		private Item lantern;
		private boolean active = true;

		LanternPoint() {
			super("latarenka", SingletonRepository.getRPWorld().getTurnsInSeconds(RESPAWN_SECONDS));
		}

		boolean hasLantern() {
			return lantern != null;
		}

		@Override
		protected int getRandomTurnsForRegrow() {
			// Exactly 30 minutes from pickup, not a Gaussian approximation.
			return meanTurnsForRegrow;
		}

		@Override
		protected Item growNewFruit() {
			if (!active || hasPickableFruit || !SeasonalEventService.get().isMineTownEnabled()) {
				return null;
			}
			final StendhalRPZone zone = getZone();
			if (zone == null) {
				return null;
			}
			Point position = null;
			// One flood fill per respawn, never a full-map search for every retry.
			final BitSet reachable = reachableTiles(zone);
			for (int attempt = 0; attempt < 50; attempt++) {
				final Point candidate = zone.getRandomSpawnPosition(this);
				if (candidate != null && candidate.x > 1 && candidate.y > 1
						&& candidate.x < zone.getWidth() - 2 && candidate.y < zone.getHeight() - 2
						&& zone.getEntitiesAt(candidate.x, candidate.y).isEmpty()
						&& zone.getPortal(candidate.x, candidate.y) == null
						&& reachable.get(candidate.y * zone.getWidth() + candidate.x)) {
					position = candidate;
					break;
				}
			}
			if (position == null) {
				TurnNotifier.get().notifyInSeconds(60, this);
				return null;
			}
			final Item item = SingletonRepository.getEntityManager().getItem("latarenka");
			if (item == null) {
				throw new IllegalStateException("Brakuje przedmiotu latarenka");
			}
			setPosition(position.x, position.y);
			notifyWorldAboutChanges();
			item.setPosition(position.x, position.y);
			item.setItemData(itemData());
			item.setDescription("Oto latarenka zagubiona podczas Mine Town. Wolrad na południu Zakopanego zbiera je na festyn.");
			item.setPlantGrower(this);
			item.setFromCorpse(true);
			zone.add(item, false);
			lantern = item;
			hasPickableFruit = true;
			return item;
		}

		@Override
		public void onFruitPicked(final Item picked) {
			lantern = null;
			if (active) {
				super.onFruitPicked(picked);
			}
		}

		@Override
		public void onRemoved(final StendhalRPZone zone) {
			active = false;
			TurnNotifier.get().dontNotify(this);
			// Collected lanterns are detached by onFruitPicked and must not be deleted.
			if (lantern != null && lantern.getZone() == zone && lantern.getContainer() == null) {
				lantern.setPlantGrower(null);
				zone.remove(lantern);
			}
			lantern = null;
			hasPickableFruit = false;
			super.onRemoved(zone);
		}
	}
}
