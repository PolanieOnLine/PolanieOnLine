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

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.layout.SBoxLayout;

/**
 * Dialog for game settings.
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {

	/**
	 * Create a new SettingsDialog.
	 *
	 * @param parent parent window, or <code>null</code>
	 */
	public SettingsDialog(Frame parent) {
		super(parent, "Ustawienia");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		int pad = SBoxLayout.COMMON_PADDING;
		setLayout(new BorderLayout(pad, pad));
		SettingsPages pages = new SettingsPages();
		new GeneralSettings(pages);
		new VisualSettings(pages);
		new SoundSettings(pages);
		add(pages, BorderLayout.CENTER);
		setResizable(true);
		JPanel footer = new JPanel(new BorderLayout(pad, 0));
		footer.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		footer.add(new JLabel("Ustawienia zapisują się automatycznie"), BorderLayout.WEST);
		JButton closeButton = new JButton("Zamknij");
		closeButton.addActionListener(event -> dispose());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttons.add(closeButton);
		footer.add(buttons, BorderLayout.EAST);
		add(footer, BorderLayout.SOUTH);
		WindowUtils.closeOnEscape(this);
		if (getFont() == null) {
			setFont(UIManager.getFont("Label.font"));
		}
		WindowUtils.watchFontSize(this);
		WindowUtils.trackLocation(this, "settings", false);
		pack();
		setMinimumSize(getSize());
	}
}
