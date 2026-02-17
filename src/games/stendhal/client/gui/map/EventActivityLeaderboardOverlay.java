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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicPanelUI;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Small overlay with live event activity ranking.
 */
public class EventActivityLeaderboardOverlay extends JPanel {
	private static final int MAX_ROWS = 10;
	private static final int PANEL_RADIUS = 9;
	private static final Color PANEL_BACKGROUND = new Color(22, 14, 10, 102);
	private static final Color PANEL_BORDER = new Color(168, 130, 92, 130);

	private final JLabel titleLabel = new JLabel("Aktywność");
	private final JLabel[] rows = new JLabel[MAX_ROWS];
	private float overlayAlpha = 1.0f;

	@Override
	public void updateUI() {
		setUI(new BasicPanelUI());
		if (titleLabel != null) {
			titleLabel.setUI(new BasicLabelUI());
		}
		if (rows == null) {
			return;
		}
		for (int i = 0; i < rows.length; i++) {
			if (rows[i] != null) {
				rows[i].setUI(new BasicLabelUI());
			}
		}
	}

	public EventActivityLeaderboardOverlay() {
		setUI(new BasicPanelUI());
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		titleLabel.setUI(new BasicLabelUI());
		titleLabel.setForeground(new Color(246, 230, 200));
		titleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		add(titleLabel, SLayout.EXPAND_X);
		for (int i = 0; i < MAX_ROWS; i++) {
			rows[i] = new JLabel();
			rows[i].setUI(new BasicLabelUI());
			rows[i].setForeground(Color.WHITE);
			rows[i].setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
			add(rows[i], SLayout.EXPAND_X);
		}
		setVisible(false);
	}

	public void setOverlayAlpha(final float alpha) {
		overlayAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
		repaint();
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

	@Override
	protected void paintComponent(final Graphics g) {
		if (overlayAlpha <= 0.0f) {
			return;
		}
		final Graphics2D g2d = (Graphics2D) g.create();
		try {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final int width = getWidth();
			final int height = getHeight();
			if ((width > 1) && (height > 1)) {
				final int arc = PANEL_RADIUS * 2;
				g2d.setColor(PANEL_BACKGROUND);
				g2d.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);
				final Stroke originalStroke = g2d.getStroke();
				g2d.setStroke(new BasicStroke(0.8f));
				g2d.setColor(PANEL_BORDER);
				g2d.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
				g2d.setStroke(originalStroke);
			}
			super.paintComponent(g2d);
		} finally {
			g2d.dispose();
		}
	}
}
