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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Settings that are safe to change before a character has logged in.
 */
@SuppressWarnings("serial")
final class StartupSettingsDialog extends JDialog {
	StartupSettingsDialog(Frame parent, final StartupMusicController startupMusic) {
		super(parent, "Ustawienia klienta", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		int pad = SBoxLayout.COMMON_PADDING;
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL, pad));
		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Obraz i dźwięk", createDisplayAndSoundSettings(startupMusic));
		tabs.add("Połączenie", createConnectionSettings());
		add(tabs, SLayout.EXPAND_X);

		JButton closeButton = new JButton("Zamknij");
		closeButton.setAlignmentX(RIGHT_ALIGNMENT);
		closeButton.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(pad, pad, pad, pad), closeButton.getBorder()));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(closeButton);

		WindowUtils.closeOnEscape(this);
		WindowUtils.watchFontSize(this);
		setResizable(false);
		pack();
		setLocationRelativeTo(parent);
	}

	private JComponent createDisplayAndSoundSettings(final StartupMusicController startupMusic) {
		int pad = SBoxLayout.COMMON_PADDING;
		JComponent settings = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		settings.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		settings.add(createWindowModeSelector(), SLayout.EXPAND_X);

		final JSlider masterVolume = createMasterVolumeSlider(startupMusic);
		final JCheckBox soundToggle = new JCheckBox("Włącz wszystkie dźwięki");
		soundToggle.setSelected(startupMusic.isSoundEnabled());
		soundToggle.setToolTipText("Włącza lub wycisza muzykę i pozostałe dźwięki klienta");
		masterVolume.setEnabled(soundToggle.isSelected());
		soundToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean enabled = soundToggle.isSelected();
				startupMusic.setSoundEnabled(enabled);
				masterVolume.setEnabled(enabled);
			}
		});
		settings.add(soundToggle);
		settings.add(createMasterVolumeRow(masterVolume), SLayout.EXPAND_X);
		return settings;
	}

	private JComponent createConnectionSettings() {
		int pad = SBoxLayout.COMMON_PADDING;
		JComponent settings = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		settings.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

		final JRadioButton official = new JRadioButton("Oficjalny serwer ("
				+ StartupConnectionSettings.getOfficialHost() + ":"
				+ StartupConnectionSettings.getOfficialPort() + ")");
		final JRadioButton custom = new JRadioButton("Inny serwer");
		ButtonGroup serverChoice = new ButtonGroup();
		serverChoice.add(official);
		serverChoice.add(custom);
		custom.setSelected(StartupConnectionSettings.isCustom());
		official.setSelected(!StartupConnectionSettings.isCustom());
		settings.add(official);
		settings.add(custom);

		final JTextField hostField = new JTextField(StartupConnectionSettings.getCustomHost(), 24);
		final JTextField portField = new JTextField(String.valueOf(StartupConnectionSettings.getCustomPort()), 7);
		JComponent hostRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		hostRow.add(new JLabel("Adres serwera"));
		hostRow.add(hostField);
		settings.add(hostRow, SLayout.EXPAND_X);
		JComponent portRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		portRow.add(new JLabel("Port"));
		portRow.add(portField);
		settings.add(portRow, SLayout.EXPAND_X);

		ActionListener choiceListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostField.setEnabled(custom.isSelected());
				portField.setEnabled(custom.isSelected());
			}
		};
		official.addActionListener(choiceListener);
		custom.addActionListener(choiceListener);
		choiceListener.actionPerformed(null);

		JLabel hint = new JLabel("Logowanie przez Steam działa tylko na oficjalnym serwerze.");
		settings.add(hint);
		JButton applyButton = new JButton("Zapisz połączenie");
		applyButton.setAlignmentX(RIGHT_ALIGNMENT);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (custom.isSelected()) {
						StartupConnectionSettings.selectCustom(hostField.getText(),
								StartupConnectionSettings.parsePort(portField.getText()));
					} else {
						StartupConnectionSettings.selectOfficial();
					}
					JOptionPane.showMessageDialog(StartupSettingsDialog.this,
							"Połączenie zapisane. Będzie użyte przy następnym logowaniu bez wybranego profilu.");
				} catch (IllegalArgumentException exception) {
					JOptionPane.showMessageDialog(StartupSettingsDialog.this,
							exception.getMessage(), "Nieprawidłowe połączenie", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		settings.add(applyButton);
		return settings;
	}

	private JComponent createWindowModeSelector() {
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL,
				SBoxLayout.COMMON_PADDING);
		JLabel label = new JLabel("Tryb wyświetlania gry");
		final JComboBox<WindowMode> selector = new JComboBox<WindowMode>(WindowMode.values());
		selector.setSelectedItem(WindowModeController.getConfiguredMode());
		selector.setToolTipText("Wybrany tryb zostanie użyty przez okno gry po zalogowaniu");
		label.setToolTipText(selector.getToolTipText());
		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowMode selected = (WindowMode) selector.getSelectedItem();
				if (selected != null) {
					WindowModeController.select(selected);
				}
			}
		});
		row.add(label);
		row.add(Box.createHorizontalStrut(SBoxLayout.COMMON_PADDING));
		row.add(selector);
		return row;
	}

	private JComponent createMasterVolumeRow(JSlider masterVolume) {
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL,
				SBoxLayout.COMMON_PADDING);
		JLabel label = new JLabel("Głośność główna");
		String tooltip = "Głośność muzyki startowej i wszystkich dźwięków gry";
		label.setToolTipText(tooltip);
		masterVolume.setToolTipText(tooltip);
		row.add(label);
		SBoxLayout.addSpring(row);
		row.add(masterVolume);
		return row;
	}

	private JSlider createMasterVolumeSlider(final StartupMusicController startupMusic) {
		final JSlider slider = new JSlider(0, 100, startupMusic.getMasterVolumePercent());
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int volume = slider.getValue();
				startupMusic.previewMasterVolumePercent(volume);
				if (!slider.getValueIsAdjusting()) {
					startupMusic.setMasterVolumePercent(volume);
				}
			}
		});
		return slider;
	}
}
