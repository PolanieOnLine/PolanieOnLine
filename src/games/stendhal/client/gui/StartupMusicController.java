/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sound.facade.SoundFileType;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.client.sound.facade.SoundHandle;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.client.sound.facade.Time;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.math.Numeric;

/**
 * Owns the music played on the first screen and the global sound switch shown
 * there. The sound facade is later reused by the game client.
 */
final class StartupMusicController {
	static final String SOUND_PROPERTY = "sound.play";
	private static final String MASTER_VOLUME_PROPERTY = "sound.volume.master";

	private static final String MUSIC_NAME = "pol-krolewskie-miasto1";
	private static final String MUSIC_FILE = MUSIC_NAME + ".ogg";
	private static final Time SHORT_FADE = new Time(500, Time.Unit.MILLI);

	private final SoundSystemFacade soundSystem;
	private final SoundSettingsStore settingsStore;
	private SoundHandle music;

	StartupMusicController(SoundSystemFacade soundSystem) {
		this(soundSystem, new PersistedSoundSettingsStore());
	}

	StartupMusicController(SoundSystemFacade soundSystem, SoundSettingsStore settingsStore) {
		this.soundSystem = soundSystem;
		this.settingsStore = settingsStore;
	}

	void start() {
		if (music != null) {
			return;
		}
		SoundGroup group = soundSystem.getGroup(SoundLayer.BACKGROUND_MUSIC.groupName);
		group.enableStreaming();
		if (group.loadSound(MUSIC_NAME, MUSIC_FILE, SoundFileType.OGG, true)) {
			music = group.play(MUSIC_NAME, 0, null, SHORT_FADE, true, false);
		}
	}

	void stop() {
		if (music != null) {
			soundSystem.stop(music, SHORT_FADE);
			music = null;
		}
	}

	boolean isSoundEnabled() {
		return settingsStore.isEnabled();
	}

	boolean toggleSound() {
		return setSoundEnabled(!isSoundEnabled());
	}

	/**
	 * Persist and apply the global sound state.
	 *
	 * @param enabled whether all client sounds should be enabled
	 * @return the applied state
	 */
	boolean setSoundEnabled(boolean enabled) {
		settingsStore.setEnabled(enabled);
		soundSystem.mute(!enabled, true, SHORT_FADE);
		return enabled;
	}

	int getMasterVolumePercent() {
		return clampVolume(Numeric.floatToInt(soundSystem.getVolume(), 100f));
	}

	void previewMasterVolumePercent(int volume) {
		applyMasterVolume(clampVolume(volume));
	}

	void setMasterVolumePercent(int volume) {
		int clampedVolume = clampVolume(volume);
		applyMasterVolume(clampedVolume);
		settingsStore.setMasterVolume(clampedVolume);
	}

	private void applyMasterVolume(int volume) {
		soundSystem.changeVolume(Numeric.intToFloat(volume, 100f));
	}

	private static int clampVolume(int volume) {
		return Math.max(0, Math.min(100, volume));
	}

	interface SoundSettingsStore {
		boolean isEnabled();

		void setEnabled(boolean enabled);

		void setMasterVolume(int volume);
	}

	private static final class PersistedSoundSettingsStore implements SoundSettingsStore {
		@Override
		public boolean isEnabled() {
			return WtWindowManager.getInstance().getPropertyBoolean(SOUND_PROPERTY, true);
		}

		@Override
		public void setEnabled(boolean enabled) {
			WtWindowManager manager = WtWindowManager.getInstance();
			manager.setProperty(SOUND_PROPERTY, Boolean.toString(enabled));
			manager.save();
		}

		@Override
		public void setMasterVolume(int volume) {
			WtWindowManager manager = WtWindowManager.getInstance();
			manager.setProperty(MASTER_VOLUME_PROPERTY, Integer.toString(volume));
			manager.save();
		}
	}
}
