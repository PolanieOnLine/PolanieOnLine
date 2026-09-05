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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.border.Border;

import org.junit.Test;

import games.stendhal.client.sound.facade.AudibleArea;
import games.stendhal.client.sound.facade.SoundFileType;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.client.sound.facade.SoundHandle;
import games.stendhal.client.sound.facade.Time;
import games.stendhal.client.sound.nosound.NoSoundFacade;
import games.stendhal.client.sound.nosound.NoSoundGroup;
import games.stendhal.common.constants.SoundLayer;

public class StartupMusicControllerTest {
	@Test
	public void startsLoopingMusicAndStopsTheSameHandle() {
		RecordingFacade facade = new RecordingFacade();
		StartupMusicController controller = new StartupMusicController(facade);

		controller.start();

		assertEquals(SoundLayer.BACKGROUND_MUSIC.groupName, facade.requestedGroup);
		assertTrue(facade.group.streaming);
		assertEquals("pol-krolewskie-miasto1", facade.group.loadedName);
		assertEquals("pol-krolewskie-miasto1.ogg", facade.group.loadedFile);
		assertTrue(facade.group.looping);
		assertFalse(facade.group.clone);

		controller.stop();
		assertSame(facade.group.handle, facade.stopped);
	}

	@Test
	public void doesNotPlayWhenMusicCannotBeLoaded() {
		RecordingFacade facade = new RecordingFacade();
		facade.group.loadSuccessful = false;

		new StartupMusicController(facade).start();

		assertFalse(facade.group.played);
	}

	@Test
	public void appliesAnExplicitSoundState() {
		RecordingFacade facade = new RecordingFacade();
		RecordingSettingsStore settings = new RecordingSettingsStore();
		StartupMusicController controller = new StartupMusicController(facade, settings);

		assertFalse(controller.setSoundEnabled(false));
		assertTrue(facade.muted);
		assertFalse(settings.enabled);

		assertTrue(controller.setSoundEnabled(true));
		assertFalse(facade.muted);
		assertTrue(settings.enabled);
	}

	@Test
	public void appliesAndPersistsMasterVolume() {
		RecordingFacade facade = new RecordingFacade();
		RecordingSettingsStore settings = new RecordingSettingsStore();
		StartupMusicController controller = new StartupMusicController(facade, settings);
		facade.volume = 0.42f;

		assertEquals(42, controller.getMasterVolumePercent());

		controller.previewMasterVolumePercent(35);
		assertEquals(0.35f, facade.volume, 0.0001f);
		assertEquals(-1, settings.masterVolume);

		controller.setMasterVolumePercent(73);
		assertEquals(0.73f, facade.volume, 0.0001f);
		assertEquals(73, settings.masterVolume);

		controller.setMasterVolumePercent(150);
		assertEquals(1.0f, facade.volume, 0.0001f);
		assertEquals(100, settings.masterVolume);
	}

	@Test
	public void configuresACompactSoundButtonWithoutReplacingItsBorder() {
		JButton button = new JButton();
		Border border = button.getBorder();

		StendhalFirstScreen.configureSoundButton(button);

		assertEquals(new Dimension(24, 24), button.getPreferredSize());
		assertEquals(new Dimension(24, 24), button.getMinimumSize());
		assertEquals(new Dimension(24, 24), button.getMaximumSize());
		assertSame(border, button.getBorder());
		assertTrue(button.isBorderPainted());
		assertTrue(button.isContentAreaFilled());
		assertFalse(button.isFocusPainted());
	}

	private static class RecordingFacade extends NoSoundFacade {
		private final RecordingGroup group = new RecordingGroup();
		private String requestedGroup;
		private SoundHandle stopped;
		private boolean muted;
		private float volume;

		@Override
		public SoundGroup getGroup(String groupName) {
			requestedGroup = groupName;
			return group;
		}

		@Override
		public void stop(SoundHandle sound, Time fadingDuration) {
			stopped = sound;
		}

		@Override
		public void mute(boolean state, boolean insideSoundGroup, Time fadingDuration) {
			muted = state;
		}

		@Override
		public float getVolume() {
			return volume;
		}

		@Override
		public void changeVolume(float volume) {
			this.volume = volume;
		}
	}

	private static class RecordingSettingsStore implements StartupMusicController.SoundSettingsStore {
		private boolean enabled = true;
		private int masterVolume = -1;

		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void setMasterVolume(int volume) {
			masterVolume = volume;
		}
	}

	private static class RecordingGroup extends NoSoundGroup {
		private final SoundHandle handle = new SoundHandle() { };
		private boolean streaming;
		private boolean loadSuccessful = true;
		private boolean played;
		private boolean looping;
		private boolean clone;
		private String loadedName;
		private String loadedFile;

		@Override
		public void enableStreaming() {
			streaming = true;
		}

		@Override
		public boolean loadSound(String name, String fileURI, SoundFileType fileType,
				boolean enableStreaming) {
			loadedName = name;
			loadedFile = fileURI;
			return loadSuccessful;
		}

		@Override
		public SoundHandle play(String soundName, int layerLevel, AudibleArea area,
				Time fadeInDuration, boolean autoRepeat, boolean shouldClone) {
			played = true;
			looping = autoRepeat;
			clone = shouldClone;
			return handle;
		}
	}
}
