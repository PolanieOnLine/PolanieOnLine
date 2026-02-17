/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.map;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Small overlay with live event activity ranking.
 */
public class EventActivityLeaderboardOverlay extends JPanel {
	private static final int MAX_ROWS = 10;
	private final JLabel titleLabel = new JLabel("Aktywność");
	private final JLabel[] rows = new JLabel[MAX_ROWS];

	public EventActivityLeaderboardOverlay() {
		setOpaque(true);
		setBackground(new Color(20, 12, 8, 132));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(168, 130, 92, 140), 1),
				BorderFactory.createEmptyBorder(6, 8, 6, 8)));
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		titleLabel.setForeground(new Color(246, 230, 200));
		titleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		add(titleLabel, SLayout.EXPAND_X);
		for (int i = 0; i < MAX_ROWS; i++) {
			rows[i] = new JLabel();
			rows[i].setForeground(Color.WHITE);
			rows[i].setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
			add(rows[i], SLayout.EXPAND_X);
		}
		setVisible(false);
	}

	public void updateRows(final List<String> entries) {
		final List<String> safeEntries = (entries == null) ? Collections.<String>emptyList() : entries;
		for (int i = 0; i < MAX_ROWS; i++) {
			if (i < safeEntries.size()) {
				rows[i].setText((i + 1) + ". " + safeEntries.get(i));
				rows[i].setVisible(true);
			} else {
				rows[i].setText("");
				rows[i].setVisible(false);
			}
		}
		setVisible(!safeEntries.isEmpty());
		revalidate();
		repaint();
	}
}
