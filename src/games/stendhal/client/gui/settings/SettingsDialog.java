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
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.gui.layout.SBoxLayout;

/**
 * Dialog for game settings.
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	private final SettingChangeListener languageListener = value -> SwingUtilities.invokeLater(() -> {
		setTitle(ClientLanguage.getText("Ustawienia"));
		SettingsTranslation.apply(getContentPane());
		getContentPane().revalidate();
		Dimension required = getPreferredSize();
		Dimension minimum = getMinimumSize();
		int width = Math.max(minimum.width, required.width);
		int height = Math.max(minimum.height, required.height);
		setMinimumSize(new Dimension(width, height));
		if (getWidth() < width || getHeight() < height) setSize(Math.max(getWidth(), width), Math.max(getHeight(), height));
		getContentPane().repaint();
	});

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
		buttons.setBorder(BorderFactory.createEmptyBorder());
		buttons.add(closeButton);
		footer.add(buttons, BorderLayout.EAST);
		add(footer, BorderLayout.SOUTH);
		WindowUtils.closeOnEscape(this);
		if (getFont() == null) {
			setFont(UIManager.getFont("Label.font"));
		}
		WindowUtils.watchFontSize(this);
		WindowUtils.trackLocation(this, "settings", false);
		setTitle(ClientLanguage.getText("Ustawienia"));
		SettingsTranslation.apply(getContentPane());
		WtWindowManager.getInstance().registerSettingChangeListener(ClientLanguage.PROPERTY, languageListener);
		pack();
		setMinimumSize(getSize());
	}

	@Override
	public void dispose() {
		WtWindowManager.getInstance().deregisterSettingChangeListener(ClientLanguage.PROPERTY, languageListener);
		super.dispose();
	}
}
