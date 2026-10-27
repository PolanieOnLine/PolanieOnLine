package games.stendhal.server.maps.quests.revivalweeks;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.sound.BackgroundMusicSource;
import games.stendhal.server.entity.mapstuff.sound.PeriodicAmbientSoundSource;
import marauroa.common.game.IRPZone;

/** Occasional ghost calls accompanying existing Halloween music sources. */
public class HalloweenGhostAmbience implements LoadableContent {

	private static final String MUSIC = "pol-halloween-szepty-zaswiatow";
	private static final int MIN_INTERVAL = 60;
	private static final int MAX_INTERVAL = 150;
	private final List<GhostSoundSource> sources = new ArrayList<GhostSoundSource>();
	private boolean active;

	@Override
	public void addToWorld() {
		if (active || !SeasonalEventService.get().isMineTownEnabled()) {
			return;
		}
		active = true;
		try {
			for (final IRPZone raw : SingletonRepository.getRPWorld()) {
				final StendhalRPZone zone = (StendhalRPZone) raw;
				if (zone.getAttributes() != null && MUSIC.equals(zone.getAttributes().get("music"))) {
					addSource(zone, zone.getWidth() / 2, zone.getHeight() / 2, 10000);
				} else {
					for (final Entity source : zone.getEntitiesOfClass(BackgroundMusicSource.class)) {
						if (MUSIC.equals(source.get("sound"))) {
							addSource(zone, source.getX(), source.getY(), source.getInt("radius"));
						}
					}
				}
			}
		} catch (final RuntimeException exception) {
			removeFromWorld();
			throw exception;
		}
	}

	private void addSource(final StendhalRPZone zone, final int x, final int y, final int radius) {
		final GhostSoundSource source = new GhostSoundSource(radius);
		sources.add(source);
		source.setPosition(x, y);
		zone.add(source);
		source.startAfterPlacement();
	}

	@Override
	public boolean removeFromWorld() {
		active = false;
		for (final GhostSoundSource source : sources) {
			TurnNotifier.get().dontNotify(source);
			if (source.getZone() != null) {
				source.getZone().remove(source);
			}
		}
		sources.clear();
		return true;
	}

	private final class GhostSoundSource extends PeriodicAmbientSoundSource {
		GhostSoundSource(final int radius) {
			super("ghost-01, ghost-02, ghost-03, ghost-04", radius, 20, MIN_INTERVAL, MAX_INTERVAL);
			// The inherited constructor schedules before the entity has a stable ID.
			TurnNotifier.get().dontNotify(this);
		}

		void startAfterPlacement() {
			final int seconds = MIN_INTERVAL + (int) (Math.random() * (MAX_INTERVAL - MIN_INTERVAL));
			TurnNotifier.get().notifyInSeconds(seconds, this);
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			if (active && getZone() != null && SeasonalEventService.get().isMineTownEnabled()) {
				super.onTurnReached(currentTurn);
			}
		}
	}
}
