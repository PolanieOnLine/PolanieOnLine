/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.settings;

import static games.stendhal.client.gui.settings.SettingsProperties.DOUBLE_TAP_AUTOWALK_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.MOVE_CONTINUOUS_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.MSG_BLINK;
import static games.stendhal.client.gui.settings.SettingsProperties.MSG_SOUND;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.actions.MoveContinuousAction;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;

/**
 * Page for general settings.
 */
class GeneralSettings {
	private static final String GAMESCREEN_AUTORAISECORPSE = "gamescreen.autoinspectcorpses";

	/** Property used for the double click setting. */
	private static final String DOUBLE_CLICK_PROPERTY = "ui.doubleclick";

	private static final String HEALING_MESSAGE_PROPERTY = "ui.healingmessage";

	private static final String POISON_MESSAGE_PROPERTY = "ui.poisonmessage";

	private static final String DIMENSIONS_PROPERTY = "ui.dimensions";

	/**
	 * Create new GeneralSettings.
	 */
	GeneralSettings(SettingsPages pages) {
		JComponent movement = pages.addGroup(SettingsPages.Category.CONTROLS, "Poruszanie się");
		JComponent clicking = pages.addGroup(SettingsPages.Category.CONTROLS, "Klikanie");
		JComponent experimental = pages.addGroup(SettingsPages.Category.CONTROLS, "Funkcje eksperymentalne");
		JComponent messages = pages.addGroup(SettingsPages.Category.COMBAT, "Informacje o walce");
		JComponent items = pages.addGroup(SettingsPages.Category.COMBAT, "Przedmioty");
		JComponent privateMessages = pages.addGroup(SettingsPages.Category.TEXT, "Wiadomości prywatne");

		final WtWindowManager windowManager = WtWindowManager.getInstance();
		final JComboBox<MovementKeyOption> movementSchemeSelector = new JComboBox<MovementKeyOption>(
				MovementKeyOption.values());
		movementSchemeSelector.setToolTipText(
				"Wybierz, czy poruszasz się strzałkami, czy klawiszami WASD (eksperymentalne). WASD wyłącza automatyczne ustawianie fokusu na czacie.");
		final MovementKeyOption selectedScheme = MovementKeyOption.fromProperty(windowManager
				.getProperty(SettingsProperties.MOVE_KEY_SCHEME_PROPERTY, SettingsProperties.MOVE_KEY_SCHEME_ARROWS));
		movementSchemeSelector.setSelectedItem(selectedScheme);
		movementSchemeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Object choice = movementSchemeSelector.getSelectedItem();
				if (choice instanceof MovementKeyOption) {
					MovementKeyOption option = (MovementKeyOption) choice;
					windowManager.setProperty(SettingsProperties.MOVE_KEY_SCHEME_PROPERTY, option.getPropertyValue());
					String msg = "Sterowanie ruchem ustawione na " + option.getLabel() + ".";
					ClientSingletonRepository.getUserInterface()
							.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
				}
			}
		});
		movement.add(SettingsComponentFactory.createSettingsRow("Sterowanie ruchem:", movementSchemeSelector), SLayout.EXPAND_X);

		// click mode
		JCheckBox clickModeToggle = SettingsComponentFactory.createSettingsToggle(DOUBLE_CLICK_PROPERTY, false,
				"Tryb podwójnego klikania",
				"Poruszanie i atak podwójnym kliknięciem zamiast pojedynczym.");
		clicking.add(clickModeToggle);
		clickModeToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getState(e, "Podwójne klikanie zostało ");
			}
		});

		// raising corpses
		JCheckBox autoRaiseToggle = SettingsComponentFactory.createSettingsToggle(GAMESCREEN_AUTORAISECORPSE, true,
				"Automatycznie sprawdzaj zwłoki", "Automatycznie otwiera okno zwłok potwora, z którego możesz wziąć swój łup");
		items.add(autoRaiseToggle);
		autoRaiseToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getState(e, "Automatyczne sprawdzanie zwłok zostało ");
			}
		});

		// show healing messages
		JCheckBox showHealingToggle = SettingsComponentFactory.createSettingsToggle(HEALING_MESSAGE_PROPERTY, false,
				"Pokaż wiadomości o leczeniu", "Pokazuje wiadomości o leczeniu w oknie rozmowy");
		messages.add(showHealingToggle);
		showHealingToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getState(e, "Wiadomości o leczeniu zostały ");
			}
		});

		// show poison messages
		JCheckBox showPoisonToggle = SettingsComponentFactory.createSettingsToggle(POISON_MESSAGE_PROPERTY, false,
				"Pokaż wiadomości o zatruciu", "Pokazuje wiadomości o zatruciu w oknie rozmowy");
		messages.add(showPoisonToggle);
		showPoisonToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getState(e, "Wiadomości o zatruciu zostały ");
			}
		});

		// Double-tap direction for auto-walk
		JCheckBox doubleTapAutowalkToggle = SettingsComponentFactory.createSettingsToggle(DOUBLE_TAP_AUTOWALK_PROPERTY,
				false, "Automatyczne chodzenie",
				"Włącza automatyczne chodzenie, gdy klawisz kierunkowy został podwójnie kliknięty");
		experimental.add(doubleTapAutowalkToggle);
		experimental.add(new JLabel("Ta opcja może działać nieprawidłowo."));
		doubleTapAutowalkToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getState(e, "Automatyczne chodzenie zostało ");
			}
		});

		// Continuous movement
		final JCheckBox moveContinuousToggle = SettingsComponentFactory.createSettingsToggle(MOVE_CONTINUOUS_PROPERTY,
				false, "Ciągły ruch", "Zmieniaj mapy i przechodź przez portale bez zatrzymywania się");
		moveContinuousToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new MoveContinuousAction().sendAction(moveContinuousToggle.isSelected());
			}
		});
		WtWindowManager.getInstance().registerSettingChangeListener(MOVE_CONTINUOUS_PROPERTY,
				new SettingChangeListener() {
					@Override
					public void changed(String newValue) {
						moveContinuousToggle.setSelected(Boolean.parseBoolean(newValue));
					}
				});
		movement.add(moveContinuousToggle);

		final JCheckBox msgBlinkToggle = SettingsComponentFactory.createSettingsToggle(MSG_BLINK, true,
				"Migaj przy nowej wiadomości",
				"Karta kanału czatu miga w wiadomości, gdy nie jest skoncentrowana");
		privateMessages.add(msgBlinkToggle);

		final JCheckBox msgSoundToggle = SettingsComponentFactory.createSettingsToggle(MSG_SOUND, true,
				"Powiadomienie dźwiękowe",
				"Odtwarzaj dźwięk dla kanału wiadomości osobistych, gdy nie jest skoncentrowany");
		privateMessages.add(msgSoundToggle);

		// Client dimensions
		JComponent clientSizeBox = pages.addGroup(SettingsPages.Category.DISPLAY, "Rozmiar okna");

		// Save client dimensions
		JCheckBox saveDimensionsToggle = SettingsComponentFactory.createSettingsToggle(DIMENSIONS_PROPERTY, true,
				"Zapamiętaj rozmiar", "Przywraca rozmiar okna i jego zmaksymalizowanie przy kolejnym uruchomieniu gry.");
		clientSizeBox.add(saveDimensionsToggle);

		// Reset client window to default dimensions
		JButton resetDimensions = new JButton("Przywróć rozmiar");
		resetDimensions.setToolTipText("Zresetuj szerokość i wysokość klienta do domyślnych wymiarów");
		resetDimensions.setActionCommand("reset_dimensions");
		resetDimensions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetClientDimensions();
			}
		});
		resetDimensions.setAlignmentX(Component.RIGHT_ALIGNMENT);
		clientSizeBox.add(resetDimensions);
	}

	private void getState(ItemEvent e, String text) {
		boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
		String tmp = enabled ? "włączone" : "wyłączone";
		String msg = text + tmp + ".";
		ClientSingletonRepository.getUserInterface().addEventLine(new EventLine("", msg, NotificationType.CLIENT));
	}

	/**
	 * Resets the clients width and height to their default values.
	 */
	private void resetClientDimensions() {
		j2DClient clientFrame = j2DClient.get();
		clientFrame.resetClientDimensions();
	}

	private enum MovementKeyOption {
		ARROWS(SettingsProperties.MOVE_KEY_SCHEME_ARROWS, "strzałki"),
		WASD(SettingsProperties.MOVE_KEY_SCHEME_WASD, "WASD");

		private final String propertyValue;
		private final String label;

		MovementKeyOption(final String propertyValue, final String label) {
			this.propertyValue = propertyValue;
			this.label = label;
		}

		String getPropertyValue() {
			return propertyValue;
		}

		String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}

		static MovementKeyOption fromProperty(final String propertyValue) {
			for (MovementKeyOption option : values()) {
				if (option.propertyValue.equalsIgnoreCase(propertyValue)) {
					return option;
				}
			}
			return ARROWS;
		}
	}
}
