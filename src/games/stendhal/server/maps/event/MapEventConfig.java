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
package games.stendhal.server.maps.event;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MapEventConfig {
	public static final int KILLS_PER_ACTIVITY_POINT = 4;

	private final String eventName;
	private final Duration duration;
	private final List<String> zones;
	private final List<String> observerZones;
	private final Set<String> creatureFilter;
	private final List<BaseMapEvent.EventWave> waves;
	private final List<String> announcements;
	private final String startAnnouncement;
	private final String stopAnnouncement;
	private final int announcementIntervalSeconds;
	private final WeatherLockConfig weatherLock;
	private final Map<String, Double> zoneSpawnMultipliers;
	private final Map<String, Integer> zoneSpawnCaps;
	private final int triggerThreshold;
	private final LocalTime defaultStartTime;
	private final int defaultIntervalDays;
	private final boolean giantOnlyAggro;
	private final EscortSettings escortSettings;
	private final ScalingConfig scaling;
	private final List<CapturePointConfig> capturePoints;
	private final List<CaptureProgressWaveConfig> captureProgressWaves;

	private MapEventConfig(final Builder builder) {
		eventName = builder.eventName;
		duration = builder.duration;
		zones = Collections.unmodifiableList(new ArrayList<>(builder.zones));
		observerZones = Collections.unmodifiableList(new ArrayList<>(builder.observerZones));
		creatureFilter = Collections.unmodifiableSet(new LinkedHashSet<>(builder.creatureFilter));
		waves = Collections.unmodifiableList(new ArrayList<>(builder.waves));
		announcements = Collections.unmodifiableList(new ArrayList<>(builder.announcements));
		startAnnouncement = builder.startAnnouncement;
		stopAnnouncement = builder.stopAnnouncement;
		announcementIntervalSeconds = builder.announcementIntervalSeconds;
		weatherLock = builder.weatherLock;
		zoneSpawnMultipliers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.zoneSpawnMultipliers));
		zoneSpawnCaps = Collections.unmodifiableMap(new LinkedHashMap<>(builder.zoneSpawnCaps));
		triggerThreshold = builder.triggerThreshold;
		defaultStartTime = builder.defaultStartTime;
		defaultIntervalDays = builder.defaultIntervalDays;
		giantOnlyAggro = builder.giantOnlyAggro;
		escortSettings = builder.escortSettings;
		scaling = builder.scaling;
		capturePoints = Collections.unmodifiableList(new ArrayList<>(builder.capturePoints));
		captureProgressWaves = Collections.unmodifiableList(new ArrayList<>(builder.captureProgressWaves));
	}

	public String getEventName() {
		return eventName;
	}

	public Duration getDuration() {
		return duration;
	}

	public List<String> getZones() {
		return zones;
	}

	public List<String> getObserverZones() {
		return observerZones;
	}

	public Set<String> getCreatureFilter() {
		return creatureFilter;
	}

	public List<BaseMapEvent.EventWave> getWaves() {
		return waves;
	}

	public List<String> getAnnouncements() {
		return announcements;
	}

	public String getStartAnnouncement() {
		return startAnnouncement;
	}

	public String getStopAnnouncement() {
		return stopAnnouncement;
	}

	public int getAnnouncementIntervalSeconds() {
		return announcementIntervalSeconds;
	}

	public WeatherLockConfig getWeatherLock() {
		return weatherLock;
	}

	public Map<String, Double> getZoneSpawnMultipliers() {
		return zoneSpawnMultipliers;
	}

	public double getZoneSpawnMultiplier(final String zoneName) {
		return zoneSpawnMultipliers.getOrDefault(zoneName, 1.0d);
	}

	public Map<String, Integer> getZoneSpawnCaps() {
		return zoneSpawnCaps;
	}

	public Integer getZoneSpawnCap(final String zoneName) {
		return zoneSpawnCaps.get(zoneName);
	}

	public int getTriggerThreshold() {
		return triggerThreshold;
	}

	public LocalTime getDefaultStartTime() {
		return defaultStartTime;
	}

	public int getDefaultIntervalDays() {
		return defaultIntervalDays;
	}

	public boolean isGiantOnlyAggro() {
		return giantOnlyAggro;
	}

	public EscortSettings getEscortSettings() {
		return escortSettings;
	}

	public ScalingConfig getScaling() {
		return scaling;
	}

	public List<CapturePointConfig> getCapturePoints() {
		return capturePoints;
	}

	public List<CaptureProgressWaveConfig> getCaptureProgressWaves() {
		return captureProgressWaves;
	}

	public static Builder builder(final String eventName) {
		return new Builder(eventName);
	}

	public static int resolveKillActivityPoints(final int killCount) {
		return Math.max(0, killCount) / KILLS_PER_ACTIVITY_POINT;
	}

	public static final class Builder {
		private final String eventName;
		private Duration duration = Duration.ofMinutes(30);
		private List<String> zones = Collections.emptyList();
		private List<String> observerZones = Collections.emptyList();
		private Set<String> creatureFilter = Collections.emptySet();
		private List<BaseMapEvent.EventWave> waves = Collections.emptyList();
		private List<String> announcements = Collections.emptyList();
		private String startAnnouncement;
		private String stopAnnouncement;
		private int announcementIntervalSeconds = 600;
		private WeatherLockConfig weatherLock;
		private Map<String, Double> zoneSpawnMultipliers = Collections.emptyMap();
		private Map<String, Integer> zoneSpawnCaps = Collections.emptyMap();
		private int triggerThreshold;
		private LocalTime defaultStartTime;
		private int defaultIntervalDays = 1;
		private boolean giantOnlyAggro;
		private EscortSettings escortSettings;
		private ScalingConfig scaling;
		private List<CapturePointConfig> capturePoints = Collections.emptyList();
		private List<CaptureProgressWaveConfig> captureProgressWaves = Collections.emptyList();

		private Builder(final String eventName) {
			this.eventName = Objects.requireNonNull(eventName, "eventName");
		}

		public Builder duration(final Duration duration) {
			this.duration = Objects.requireNonNull(duration, "duration");
			return this;
		}

		public Builder zones(final List<String> zones) {
			this.zones = new ArrayList<>(Objects.requireNonNull(zones, "zones"));
			return this;
		}

		public Builder observerZones(final List<String> observerZones) {
			this.observerZones = new ArrayList<>(Objects.requireNonNull(observerZones, "observerZones"));
			return this;
		}

		public Builder creatureFilter(final Set<String> creatureFilter) {
			this.creatureFilter = new LinkedHashSet<>(Objects.requireNonNull(creatureFilter, "creatureFilter"));
			return this;
		}

		public Builder waves(final List<BaseMapEvent.EventWave> waves) {
			this.waves = new ArrayList<>(Objects.requireNonNull(waves, "waves"));
			return this;
		}

		public Builder announcements(final List<String> announcements) {
			this.announcements = new ArrayList<>(Objects.requireNonNull(announcements, "announcements"));
			return this;
		}

		public Builder startAnnouncement(final String startAnnouncement) {
			this.startAnnouncement = startAnnouncement;
			return this;
		}

		public Builder stopAnnouncement(final String stopAnnouncement) {
			this.stopAnnouncement = stopAnnouncement;
			return this;
		}

		public Builder announcementIntervalSeconds(final int announcementIntervalSeconds) {
			this.announcementIntervalSeconds = announcementIntervalSeconds;
			return this;
		}

		public Builder weatherLock(final WeatherLockConfig weatherLock) {
			this.weatherLock = weatherLock;
			return this;
		}

		public Builder zoneSpawnMultipliers(final Map<String, Double> zoneSpawnMultipliers) {
			this.zoneSpawnMultipliers = new LinkedHashMap<>(Objects.requireNonNull(zoneSpawnMultipliers,
					"zoneSpawnMultipliers"));
			return this;
		}

		public Builder zoneSpawnCaps(final Map<String, Integer> zoneSpawnCaps) {
			this.zoneSpawnCaps = new LinkedHashMap<>(Objects.requireNonNull(zoneSpawnCaps, "zoneSpawnCaps"));
			return this;
		}

		public Builder triggerThreshold(final int triggerThreshold) {
			this.triggerThreshold = triggerThreshold;
			return this;
		}

		public Builder defaultStartTime(final LocalTime defaultStartTime) {
			this.defaultStartTime = Objects.requireNonNull(defaultStartTime, "defaultStartTime");
			return this;
		}

		public Builder defaultStartTime(final String defaultStartTime) {
			Objects.requireNonNull(defaultStartTime, "defaultStartTime");
			try {
				this.defaultStartTime = LocalTime.parse(defaultStartTime.trim());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException(
						"defaultStartTime must be a valid ISO local time (e.g. 20:00), got '"
								+ defaultStartTime + "'.",
						e);
			}
			return this;
		}

		public Builder defaultIntervalDays(final int defaultIntervalDays) {
			this.defaultIntervalDays = defaultIntervalDays;
			return this;
		}

		public Builder giantOnlyAggro(final boolean giantOnlyAggro) {
			this.giantOnlyAggro = giantOnlyAggro;
			return this;
		}

		public Builder escortSettings(final EscortSettings escortSettings) {
			this.escortSettings = escortSettings;
			return this;
		}

		public Builder scaling(final ScalingConfig scaling) {
			this.scaling = scaling;
			return this;
		}

		public Builder capturePoints(final List<CapturePointConfig> capturePoints) {
			this.capturePoints = new ArrayList<>(Objects.requireNonNull(capturePoints, "capturePoints"));
			return this;
		}

		public Builder captureProgressWaves(final List<CaptureProgressWaveConfig> captureProgressWaves) {
			this.captureProgressWaves = new ArrayList<>(
					Objects.requireNonNull(captureProgressWaves, "captureProgressWaves"));
			return this;
		}

		public MapEventConfig build() {
			if (duration.isNegative() || duration.isZero()) {
				throw new IllegalArgumentException("duration must be positive");
			}
			if (announcementIntervalSeconds < 0) {
				throw new IllegalArgumentException("announcementIntervalSeconds must be >= 0");
			}
			if (startAnnouncement != null && startAnnouncement.trim().isEmpty()) {
				throw new IllegalArgumentException("startAnnouncement must not be blank");
			}
			if (stopAnnouncement != null && stopAnnouncement.trim().isEmpty()) {
				throw new IllegalArgumentException("stopAnnouncement must not be blank");
			}
			if (triggerThreshold < 0) {
				throw new IllegalArgumentException("triggerThreshold must be >= 0");
			}
			for (final Map.Entry<String, Double> multiplierEntry : zoneSpawnMultipliers.entrySet()) {
				if (multiplierEntry.getKey() == null || multiplierEntry.getKey().trim().isEmpty()) {
					throw new IllegalArgumentException("zoneSpawnMultipliers contains blank zone name");
				}
				final Double multiplier = multiplierEntry.getValue();
				if (multiplier == null || multiplier < 0d) {
					throw new IllegalArgumentException("zoneSpawnMultipliers must be >= 0");
				}
			}
			for (final Map.Entry<String, Integer> capEntry : zoneSpawnCaps.entrySet()) {
				if (capEntry.getKey() == null || capEntry.getKey().trim().isEmpty()) {
					throw new IllegalArgumentException("zoneSpawnCaps contains blank zone name");
				}
				final Integer cap = capEntry.getValue();
				if (cap == null || cap < 0) {
					throw new IllegalArgumentException("zoneSpawnCaps must be >= 0");
				}
			}
			if (defaultIntervalDays <= 0) {
				throw new IllegalArgumentException("defaultIntervalDays must be > 0");
			}
			if (escortSettings != null) {
				escortSettings.validate();
			}
			if (scaling != null) {
				scaling.validate();
			}
			for (final CapturePointConfig capturePoint : capturePoints) {
				if (capturePoint == null) {
					throw new IllegalArgumentException("capturePoints must not contain null entries");
				}
				capturePoint.validate();
			}
			for (final CaptureProgressWaveConfig captureProgressWave : captureProgressWaves) {
				if (captureProgressWave == null) {
					throw new IllegalArgumentException("captureProgressWaves must not contain null entries");
				}
				captureProgressWave.validate();
			}
			return new MapEventConfig(this);
		}
	}

	public static final class CapturePointConfig {
		private final String pointId;
		private final String zone;
		private final int x;
		private final int y;
		private final int radiusTiles;

		public CapturePointConfig(final String pointId, final String zone, final int x, final int y,
				final int radiusTiles) {
			this.pointId = pointId;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.radiusTiles = radiusTiles;
		}

		public String getPointId() {
			return pointId;
		}

		public String getZone() {
			return zone;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getRadiusTiles() {
			return radiusTiles;
		}

		private void validate() {
			if (pointId == null || pointId.trim().isEmpty()) {
				throw new IllegalArgumentException("capture point id must not be blank");
			}
			if (zone == null || zone.trim().isEmpty()) {
				throw new IllegalArgumentException("capture point zone must not be blank");
			}
			if (radiusTiles <= 0) {
				throw new IllegalArgumentException("capture point radiusTiles must be > 0");
			}
		}
	}

	public static final class CaptureProgressWaveConfig {
		private final int thresholdPercent;
		private final List<BaseMapEvent.EventSpawn> spawns;

		public CaptureProgressWaveConfig(final int thresholdPercent, final List<BaseMapEvent.EventSpawn> spawns) {
			this.thresholdPercent = thresholdPercent;
			this.spawns = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(spawns, "spawns")));
		}

		public int getThresholdPercent() {
			return thresholdPercent;
		}

		public List<BaseMapEvent.EventSpawn> getSpawns() {
			return spawns;
		}

		private void validate() {
			if (thresholdPercent < 0 || thresholdPercent > 100) {
				throw new IllegalArgumentException("capture progress thresholdPercent must be between 0 and 100");
			}
			if (spawns.isEmpty()) {
				throw new IllegalArgumentException("capture progress wave must contain at least one spawn");
			}
			for (final BaseMapEvent.EventSpawn spawn : spawns) {
				if (spawn == null) {
					throw new IllegalArgumentException("capture progress wave must not contain null spawn");
				}
			}
		}
	}

	public static final class ScalingConfig {
		private final int minPlayers;
		private final int maxPlayers;
		private final boolean scaleByOnlineInZones;
		private final int onlineZoneMinPlayerLevel;
		private final int onlineZoneMaxPlayerLevel;
		private final double killRateMultiplier;
		private final int minSpawnPerWave;
		private final int maxSpawnPerWave;

		private ScalingConfig(final Builder builder) {
			minPlayers = builder.minPlayers;
			maxPlayers = builder.maxPlayers;
			scaleByOnlineInZones = builder.scaleByOnlineInZones;
			onlineZoneMinPlayerLevel = builder.onlineZoneMinPlayerLevel;
			onlineZoneMaxPlayerLevel = builder.onlineZoneMaxPlayerLevel;
			killRateMultiplier = builder.killRateMultiplier;
			minSpawnPerWave = builder.minSpawnPerWave;
			maxSpawnPerWave = builder.maxSpawnPerWave;
		}

		public int getMinPlayers() {
			return minPlayers;
		}

		public int getMaxPlayers() {
			return maxPlayers;
		}

		public boolean isScaleByOnlineInZones() {
			return scaleByOnlineInZones;
		}

		public int getOnlineZoneMinPlayerLevel() {
			return onlineZoneMinPlayerLevel;
		}

		public int getOnlineZoneMaxPlayerLevel() {
			return onlineZoneMaxPlayerLevel;
		}

		public double getKillRateMultiplier() {
			return killRateMultiplier;
		}

		public int getMinSpawnPerWave() {
			return minSpawnPerWave;
		}

		public int getMaxSpawnPerWave() {
			return maxSpawnPerWave;
		}

		private void validate() {
			if (minPlayers < 0) {
				throw new IllegalArgumentException("scaling.minPlayers must be >= 0");
			}
			if (maxPlayers < minPlayers) {
				throw new IllegalArgumentException("scaling.maxPlayers must be >= minPlayers");
			}
			if (killRateMultiplier < 0d) {
				throw new IllegalArgumentException("scaling.killRateMultiplier must be >= 0");
			}
			if (onlineZoneMinPlayerLevel < 0) {
				throw new IllegalArgumentException("scaling.onlineZoneMinPlayerLevel must be >= 0");
			}
			if (onlineZoneMaxPlayerLevel < onlineZoneMinPlayerLevel) {
				throw new IllegalArgumentException("scaling.onlineZoneMaxPlayerLevel must be >= onlineZoneMinPlayerLevel");
			}
			if (minSpawnPerWave < 0) {
				throw new IllegalArgumentException("scaling.minSpawnPerWave must be >= 0");
			}
			if (maxSpawnPerWave < minSpawnPerWave) {
				throw new IllegalArgumentException("scaling.maxSpawnPerWave must be >= minSpawnPerWave");
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private int minPlayers;
			private int maxPlayers;
			private boolean scaleByOnlineInZones;
			private int onlineZoneMinPlayerLevel;
			private int onlineZoneMaxPlayerLevel = Integer.MAX_VALUE;
			private double killRateMultiplier = 1.0d;
			private int minSpawnPerWave;
			private int maxSpawnPerWave = Integer.MAX_VALUE;

			private Builder() {
			}

			public Builder minPlayers(final int minPlayers) {
				this.minPlayers = minPlayers;
				return this;
			}

			public Builder maxPlayers(final int maxPlayers) {
				this.maxPlayers = maxPlayers;
				return this;
			}

			public Builder scaleByOnlineInZones(final boolean scaleByOnlineInZones) {
				this.scaleByOnlineInZones = scaleByOnlineInZones;
				return this;
			}

			public Builder killRateMultiplier(final double killRateMultiplier) {
				this.killRateMultiplier = killRateMultiplier;
				return this;
			}

			public Builder onlineZoneMinPlayerLevel(final int onlineZoneMinPlayerLevel) {
				this.onlineZoneMinPlayerLevel = onlineZoneMinPlayerLevel;
				return this;
			}

			public Builder onlineZoneMaxPlayerLevel(final int onlineZoneMaxPlayerLevel) {
				this.onlineZoneMaxPlayerLevel = onlineZoneMaxPlayerLevel;
				return this;
			}

			public Builder minSpawnPerWave(final int minSpawnPerWave) {
				this.minSpawnPerWave = minSpawnPerWave;
				return this;
			}

			public Builder maxSpawnPerWave(final int maxSpawnPerWave) {
				this.maxSpawnPerWave = maxSpawnPerWave;
				return this;
			}

			public ScalingConfig build() {
				final ScalingConfig config = new ScalingConfig(this);
				config.validate();
				return config;
			}
		}
	}

	public static final class EscortSettings {
		private static final int MAX_ENTITY_HP_SHORT = 32767;

		private final int giantHp;
		private final int giantDefBonus;
		private final int resistance;
		private final int hardCap;
		private final int waveBudgetBase;
		private final int waveBudgetPerStage;
		private final int cooldownMinutes;

		private EscortSettings(final Builder builder) {
			giantHp = builder.giantHp;
			giantDefBonus = builder.giantDefBonus;
			resistance = builder.resistance;
			hardCap = builder.hardCap;
			waveBudgetBase = builder.waveBudgetBase;
			waveBudgetPerStage = builder.waveBudgetPerStage;
			cooldownMinutes = builder.cooldownMinutes;
		}

		public int getGiantHp() {
			return giantHp;
		}

		public int getGiantDefBonus() {
			return giantDefBonus;
		}

		public int getResistance() {
			return resistance;
		}

		public int getHardCap() {
			return hardCap;
		}

		public int getWaveBudgetBase() {
			return waveBudgetBase;
		}

		public int getWaveBudgetPerStage() {
			return waveBudgetPerStage;
		}

		public int getCooldownMinutes() {
			return cooldownMinutes;
		}

		private void validate() {
			if (giantHp <= 0 || giantHp > MAX_ENTITY_HP_SHORT) {
				throw new IllegalArgumentException("escortSettings.giantHp must be in range 1-" + MAX_ENTITY_HP_SHORT);
			}
			if (hardCap <= 0) {
				throw new IllegalArgumentException("escortSettings.hardCap must be > 0");
			}
			if (waveBudgetBase < 0) {
				throw new IllegalArgumentException("escortSettings.waveBudgetBase must be >= 0");
			}
			if (waveBudgetPerStage < 0) {
				throw new IllegalArgumentException("escortSettings.waveBudgetPerStage must be >= 0");
			}
			if (cooldownMinutes < 0) {
				throw new IllegalArgumentException("escortSettings.cooldownMinutes must be >= 0");
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private int giantHp;
			private int giantDefBonus;
			private int resistance;
			private int hardCap;
			private int waveBudgetBase;
			private int waveBudgetPerStage;
			private int cooldownMinutes;

			private Builder() {
			}

			public Builder giantHp(final int giantHp) {
				this.giantHp = giantHp;
				return this;
			}

			public Builder giantDefBonus(final int giantDefBonus) {
				this.giantDefBonus = giantDefBonus;
				return this;
			}

			public Builder resistance(final int resistance) {
				this.resistance = resistance;
				return this;
			}

			public Builder hardCap(final int hardCap) {
				this.hardCap = hardCap;
				return this;
			}

			public Builder waveBudgetBase(final int waveBudgetBase) {
				this.waveBudgetBase = waveBudgetBase;
				return this;
			}

			public Builder waveBudgetPerStage(final int waveBudgetPerStage) {
				this.waveBudgetPerStage = waveBudgetPerStage;
				return this;
			}

			public Builder cooldownMinutes(final int cooldownMinutes) {
				this.cooldownMinutes = cooldownMinutes;
				return this;
			}

			public EscortSettings build() {
				final EscortSettings settings = new EscortSettings(this);
				settings.validate();
				return settings;
			}
		}
	}

	public static final class WeatherLockConfig {
		private final String weather;
		private final boolean thundering;

		public WeatherLockConfig(final String weather, final boolean thundering) {
			this.weather = weather;
			this.thundering = thundering;
		}

		public String getWeather() {
			return weather;
		}

		public boolean isThundering() {
			return thundering;
		}
	}
}
