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

	public static Builder builder(final String eventName) {
		return new Builder(eventName);
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

		public MapEventConfig build() {
			if (duration.isNegative() || duration.isZero()) {
				throw new IllegalArgumentException("duration must be positive");
			}
			if (announcementIntervalSeconds < 0) {
				throw new IllegalArgumentException("announcementIntervalSeconds must be >= 0");
			}
			if (startAnnouncement != null && startAnnouncement.isBlank()) {
				throw new IllegalArgumentException("startAnnouncement must not be blank");
			}
			if (stopAnnouncement != null && stopAnnouncement.isBlank()) {
				throw new IllegalArgumentException("stopAnnouncement must not be blank");
			}
			if (triggerThreshold < 0) {
				throw new IllegalArgumentException("triggerThreshold must be >= 0");
			}
			for (final Map.Entry<String, Double> multiplierEntry : zoneSpawnMultipliers.entrySet()) {
				if (multiplierEntry.getKey() == null || multiplierEntry.getKey().isBlank()) {
					throw new IllegalArgumentException("zoneSpawnMultipliers contains blank zone name");
				}
				final Double multiplier = multiplierEntry.getValue();
				if (multiplier == null || multiplier < 0d) {
					throw new IllegalArgumentException("zoneSpawnMultipliers must be >= 0");
				}
			}
			for (final Map.Entry<String, Integer> capEntry : zoneSpawnCaps.entrySet()) {
				if (capEntry.getKey() == null || capEntry.getKey().isBlank()) {
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
			return new MapEventConfig(this);
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
