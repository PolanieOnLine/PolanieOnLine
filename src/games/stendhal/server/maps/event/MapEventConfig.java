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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
	private final int triggerThreshold;
	private final int guaranteedIntervalDays;

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
		triggerThreshold = builder.triggerThreshold;
		guaranteedIntervalDays = builder.guaranteedIntervalDays;
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

	public int getTriggerThreshold() {
		return triggerThreshold;
	}

	public int getGuaranteedIntervalDays() {
		return guaranteedIntervalDays;
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
		private int triggerThreshold;
		private int guaranteedIntervalDays = 1;

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

		public Builder triggerThreshold(final int triggerThreshold) {
			this.triggerThreshold = triggerThreshold;
			return this;
		}

		public Builder guaranteedIntervalDays(final int guaranteedIntervalDays) {
			this.guaranteedIntervalDays = guaranteedIntervalDays;
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
			if (guaranteedIntervalDays <= 0) {
				throw new IllegalArgumentException("guaranteedIntervalDays must be > 0");
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
