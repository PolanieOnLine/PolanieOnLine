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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

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

		JComponent settings = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		settings.setBorder(BorderFactory.createEmptyBorder(pad, pad, 0, pad));
		settings.add(createWindowModeSelector(), SLayout.EXPAND_X);

		final JCheckBox soundToggle = new JCheckBox("Włącz wszystkie dźwięki");
		soundToggle.setSelected(startupMusic.isSoundEnabled());
		soundToggle.setToolTipText("Włącza lub wycisza muzykę i pozostałe dźwięki klienta");
		soundToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startupMusic.setSoundEnabled(soundToggle.isSelected());
			}
		});
		settings.add(soundToggle);
		add(settings, SLayout.EXPAND_X);

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
}
