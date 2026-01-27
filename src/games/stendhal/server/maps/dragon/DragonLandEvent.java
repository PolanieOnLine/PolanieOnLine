/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.dragon;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;

public class DragonLandEvent extends BaseMapEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);
	private static final DragonLandEvent INSTANCE = new DragonLandEvent();

	private static final Duration EVENT_DURATION = Duration.ofMinutes(45);
	private static final int GUARANTEED_EVENT_INTERVAL_DAYS = 2;
	private final AtomicBoolean wawelAnnounced = new AtomicBoolean(false);
	private final AtomicInteger dragonKillCount = new AtomicInteger(0);
	private static final int DRAGON_KILL_THRESHOLD = 500;
	private static final int AMBIENT_ANNOUNCEMENT_INTERVAL_SECONDS = 600;
	private static final List<String> DRAGON_TYPES = Arrays.asList(
			"dwugłowy czarny smok",
			"dwugłowy lodowy smok",
			"dwugłowy czerwony smok",
			"dwugłowy złoty smok",
			"dwugłowy zielony smok",
			"lodowy smok",
			"pustynny smok",
			"zgniły szkielet smoka",
			"smok arktyczny",
			"zielone smoczysko",
			"niebieskie smoczysko",
			"czerwone smoczysko",
			"czarne smoczysko",
			"latający czarny smok",
			"latający złoty smok",
			"Smok Wawelski"
	);
	private static final Set<String> DRAGON_ZONES = new HashSet<>(Arrays.asList(
			"0_dragon_land_n",
			"0_dragon_land_s",
			"int_dragon_house_1",
			"int_dragon_house_2",
			"int_dragon_house_3",
			"int_dragon_house_4",
			"int_dragon_house_5",
			"int_dragon_house_6",
			"int_dragon_workshop",
			"int_dragon_castle",
			"int_dragon_castle_room_1",
			"int_dragon_castle_room_2",
			"int_dragon_castle_room_3",
			"int_dragon_castle_room_4",
			"int_dragon_castle_room_5",
			"int_dragon_castle_room_6",
			"int_dragon_castle_dragon_npc",
			"int_dragon_castle_dragon",
			"-1_dragon_cave"
	));
	private static final List<String> DRAGON_LAND_ZONES = Arrays.asList(
			"0_dragon_land_n",
			"0_dragon_land_s"
	);
	private static final DragonDeathObserver DRAGON_DEATH_OBSERVER = new DragonDeathObserver();
	private static final List<String> AMBIENT_ANNOUNCEMENTS = Arrays.asList(
			"Niebo przeszywa skrzek smoków - smocze stado krąży nad krainą.",
			"Z oddali dobiega trzepot skrzydeł i syk ognia - smoki nie odpuszczają.",
			"Smocza kraina drży pod ciężarem bestii, które krążą nad ziemią."
	);
	private static final int SPAWN_ATTEMPTS_PER_CREATURE = 20;
	private static final List<EventWave> DRAGON_WAVES = Arrays.asList(
			new EventWave(30, Arrays.asList(
					new EventSpawn("zgniły szkielet smoka", 6),
					new EventSpawn("pustynny smok", 4),
					new EventSpawn("zielony smok", 4),
					new EventSpawn("czerwony smok", 2),
					new EventSpawn("błękitny smok", 4)
			)),
			new EventWave(45, Arrays.asList(
					new EventSpawn("lodowy smok", 4),
					new EventSpawn("smok arktyczny", 3),
					new EventSpawn("dwugłowy zielony smok", 6)
			)),
			new EventWave(60, Arrays.asList(
					new EventSpawn("dwugłowy złoty smok", 4),
					new EventSpawn("dwugłowy zielony smok", 8),
					new EventSpawn("dwugłowy czerwony smok", 6)
			)),
			new EventWave(90, Arrays.asList(
					new EventSpawn("dwugłowy złoty smok", 2),
					new EventSpawn("zielone smoczysko", 3),
					new EventSpawn("niebieskie smoczysko", 3)
			)),
			new EventWave(120, Arrays.asList(
					new EventSpawn("dwugłowy czarny smok", 4),
					new EventSpawn("dwugłowy lodowy smok", 8)
			)),
			new EventWave(150, Arrays.asList(
					new EventSpawn("czerwone smoczysko", 3),
					new EventSpawn("czarne smoczysko", 2)
			)),
			new EventWave(180, Arrays.asList(
					new EventSpawn("latający czarny smok", 1),
					new EventSpawn("latający złoty smok", 1)
			)),
			new EventWave(240, Arrays.asList(
					new EventSpawn("Smok Wawelski", 1)
			))
	);

	private DragonLandEvent() {
		super(LOGGER);
	}

	public static void registerZoneObserver(final StendhalRPZone zone) {
		INSTANCE.registerZoneObserverInternal(zone);
	}

	public static void scheduleEveryTwoDaysAt(LocalTime time) {
		INSTANCE.scheduleEveryDaysAt(time, GUARANTEED_EVENT_INTERVAL_DAYS);
	}

	public static boolean forceStart() {
		return INSTANCE.forceStartInternal();
	}

	private void registerZoneObserverInternal(final StendhalRPZone zone) {
		Objects.requireNonNull(zone, "zone");
		if (!DRAGON_ZONES.contains(zone.getName())) {
			return;
		}
		for (CreatureRespawnPoint respawnPoint : zone.getRespawnPointList()) {
			if (respawnPoint != null
					&& DRAGON_TYPES.contains(respawnPoint.getPrototypeCreature().getName())) {
				respawnPoint.addObserver(DRAGON_DEATH_OBSERVER);
			}
		}
	}

	private boolean forceStartInternal() {
		if (!startEvent()) {
			LOGGER.warn("Dragon Land event already active; refusing forced start.");
			return false;
		}
		return true;
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			LOGGER.warn("Dragon Land event already active; skipping duplicate start.");
		}
	}

	private void resetKillCounter(final String reason) {
		int previous = dragonKillCount.getAndSet(0);
		LOGGER.debug("Dragon Land kill counter reset (" + reason + ") from " + previous + ".");
	}

	private void recordDragonDeath(final CircumstancesOfDeath circs) {
		if (isEventActive()) {
			return;
		}
		if (!DRAGON_ZONES.contains(circs.getZone().getName())) {
			return;
		}
		final String victimName = circs.getVictim().getName();
		if (!DRAGON_TYPES.contains(victimName)) {
			return;
		}
		int current = dragonKillCount.incrementAndGet();
		if (current >= DRAGON_KILL_THRESHOLD) {
			LOGGER.info("Dragon Land kill threshold reached: " + current + ".");
			startEventFromKills();
		}
	}

	private void summonCreatures(final String creatureName, final int count) {
		final int zoneCount = getZones().size();
		if (zoneCount == 0) {
			LOGGER.warn("Dragon Land zones list is empty; cannot spawn " + creatureName + ".");
			return;
		}
		for (int zoneIndex = 0; zoneIndex < zoneCount; zoneIndex++) {
			summonCreaturesInZone(getZones().get(zoneIndex), creatureName, count);
		}
	}

	private void summonCreaturesInZone(final String zoneName, final String creatureName, final int count) {
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				LOGGER.warn("Dragon Land event missing creature template: " + creatureName + ".");
				continue;
			}
			final Creature creature = new Creature(template.getNewInstance());
			if (placeCreatureInRandomSafeSpot(creature, zoneName)) {
				registerEventCreature(creature);
				LOGGER.debug("Dragon Land spawned " + creature.getName() + " in zone " + zoneName + ".");
			}
		}
	}

	private boolean placeCreatureInRandomSafeSpot(final Creature creature, final String zoneName) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
		if (zone == null) {
			LOGGER.warn("Dragon Land zone not found for spawn: " + zoneName + ".");
			return false;
		}
		final int[] centerAnchor = zone.getName().startsWith("0")
				? findNearestPassableCenter(zone, creature)
				: null;
		if (zone.getName().startsWith("0") && centerAnchor == null) {
			LOGGER.warn("Dragon Land spawn path anchor not found; skipping path validation for " + zoneName + ".");
		}
		for (int attempt = 0; attempt < SPAWN_ATTEMPTS_PER_CREATURE; attempt++) {
			final int x = Rand.rand(zone.getWidth());
			final int y = Rand.rand(zone.getHeight());
			if (zone.collides(creature, x, y)) {
				continue;
			}
			if (centerAnchor != null) {
				final List<Node> path = Path.searchPath(zone, x, y, centerAnchor[0], centerAnchor[1], (64 + 64) * 2);
				if (path == null || path.isEmpty()) {
					continue;
				}
			}
			if (StendhalRPAction.placeat(zone, creature, x, y)) {
				return true;
			}
		}
		LOGGER.debug("Dragon Land spawn failed after attempts for " + creature.getName() + ".");
		return false;
	}

	private void announceWawelSpawn() {
		if (!isEventActive()) {
			return;
		}
		if (!wawelAnnounced.compareAndSet(false, true)) {
			return;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				games.stendhal.common.NotificationType.PRIVMSG,
				"W oddali słychać ryk… Smok Wawelski pojawił się w Smoczej Krainie!"
		);
	}

	private static int[] findNearestPassableCenter(final StendhalRPZone zone, final Creature creature) {
		final int centerX = zone.getWidth() / 2;
		final int centerY = zone.getHeight() / 2;
		if (!zone.collides(creature, centerX, centerY)) {
			return new int[] { centerX, centerY };
		}
		final int maxRadius = Math.max(zone.getWidth(), zone.getHeight());
		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				int x = centerX + dx;
				int y = centerY - radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
				y = centerY + radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
			}
			for (int dy = -radius + 1; dy <= radius - 1; dy++) {
				int y = centerY + dy;
				int x = centerX - radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
				x = centerX + radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private static boolean isPassableAt(final StendhalRPZone zone, final Creature creature, final int x, final int y) {
		if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getHeight()) {
			return false;
		}
		return !zone.collides(creature, x, y);
	}

	private static class DragonDeathObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (arg instanceof CircumstancesOfDeath) {
				INSTANCE.recordDragonDeath((CircumstancesOfDeath) arg);
			}
		}
	}

	@Override
	protected String getEventName() {
		return "Dragon Land";
	}

	@Override
	protected Duration getEventDuration() {
		return EVENT_DURATION;
	}

	@Override
	protected List<String> getZones() {
		return DRAGON_LAND_ZONES;
	}

	@Override
	protected List<EventWave> getWaves() {
		return DRAGON_WAVES;
	}

	@Override
	protected List<String> getAnnouncements() {
		return AMBIENT_ANNOUNCEMENTS;
	}

	@Override
	protected int getAnnouncementIntervalSeconds() {
		return AMBIENT_ANNOUNCEMENT_INTERVAL_SECONDS;
	}

	@Override
	protected void onStart() {
		wawelAnnounced.set(false);
		resetKillCounter("event started");
		LOGGER.info("Dragon Land event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				games.stendhal.common.NotificationType.PRIVMSG,
				"Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie."
		);
		lockWeather("fog", false);
	}

	@Override
	protected void onStop() {
		resetKillCounter("event ended");
		LOGGER.info("Dragon Land event ended.");
		removeEventCreatures();
		stopAnnouncements();
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				games.stendhal.common.NotificationType.PRIVMSG,
				"Smocza kraina uspokaja się. Wydarzenie dobiegło końca."
		);
		restoreWeather();
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		summonCreatures(creatureName, count);
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		if ("Smok Wawelski".equals(spawn.getCreatureName())) {
			announceWawelSpawn();
		}
	}
}
