/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.settings;

import static games.stendhal.client.gui.settings.SettingsProperties.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JCheckBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;

public class ScreenScalingPreferenceTest {
	private WtWindowManager manager;
	private Field propertiesField;
	private Object originalProperties;
	private Map<String, List<SettingChangeListener>> listeners;
	private Map<String, List<SettingChangeListener>> originalListeners;

	@Before
	public void isolatePreferencesInMemory() throws Exception {
		manager = WtWindowManager.getInstance();
		propertiesField = WtWindowManager.class.getDeclaredField("properties");
		propertiesField.setAccessible(true);
		originalProperties = propertiesField.get(manager);
		propertiesField.set(manager, new Properties());
		final Field listenersField = WtWindowManager.class.getDeclaredField("listeners");
		listenersField.setAccessible(true);
		@SuppressWarnings("unchecked")
		final Map<String, List<SettingChangeListener>> managerListeners =
				(Map<String, List<SettingChangeListener>>) listenersField.get(manager);
		listeners = managerListeners;
		originalListeners = new HashMap<>(listeners);
		listeners.clear();
	}

	@After
	public void restorePreferencesWithoutSaving() throws Exception {
		propertiesField.set(manager, originalProperties);
		listeners.clear();
		listeners.putAll(originalListeners);
	}

	@Test
	public void missingPreferenceIsOffInBothRenderingAndSettings() {
		assertFalse(SCALE_SCREEN_DEFAULT);
		assertFalse(manager.getPropertyBoolean(SCALE_SCREEN_PROPERTY, SCALE_SCREEN_DEFAULT));
		assertFalse(toggle().isSelected());
	}

	@Test
	public void savedChoicesAreRespectedAndCheckboxNotifiesRenderer() {
		manager.setProperty(SCALE_SCREEN_PROPERTY, "true");
		final JCheckBox toggle = toggle();
		assertTrue(toggle.isSelected());
		final AtomicReference<String> observed = new AtomicReference<>();
		final SettingChangeListener listener = observed::set;
		manager.registerSettingChangeListener(SCALE_SCREEN_PROPERTY, listener);
		try {
			toggle.setSelected(false);
			assertEquals("false", observed.get());
			assertFalse(manager.getPropertyBoolean(SCALE_SCREEN_PROPERTY, SCALE_SCREEN_DEFAULT));
			toggle.setSelected(true);
			assertEquals("true", observed.get());
			assertTrue(manager.getPropertyBoolean(SCALE_SCREEN_PROPERTY, SCALE_SCREEN_DEFAULT));
		} finally {
			manager.deregisterSettingChangeListener(SCALE_SCREEN_PROPERTY, listener);
		}
	}

	private JCheckBox toggle() {
		return SettingsComponentFactory.createSettingsToggle(SCALE_SCREEN_PROPERTY,
				SCALE_SCREEN_DEFAULT, "Skalowanie", "");
	}
}
