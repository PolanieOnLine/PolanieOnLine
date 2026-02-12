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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Overlay used to display active map event progress.
 */
public class EventProgressBarOverlay extends JPanel {
	private static final int BAR_WIDTH = 260;
	private static final int BAR_HEIGHT = 12;
	private static final int BAR_RADIUS = 8;

	private final JLabel eventTitle;
	private final JProgressBar progressBar;
	private String eventId;

	public EventProgressBarOverlay() {
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(2, 8, 3, 8));
		setAlignmentX(JComponent.CENTER_ALIGNMENT);
		setAlignmentY(JComponent.TOP_ALIGNMENT);
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		eventTitle = new JLabel();
		eventTitle.setForeground(Color.WHITE);
		eventTitle.setFont(eventTitle.getFont().deriveFont(Font.BOLD));
		eventTitle.setHorizontalAlignment(JLabel.CENTER);
		eventTitle.setAlignmentX(CENTER_ALIGNMENT);
		add(eventTitle, SLayout.EXPAND_X);

		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setMinimumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setMaximumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setFont(progressBar.getFont().deriveFont(Font.BOLD, 10f));
		progressBar.setOpaque(false);
		progressBar.setBorder(BorderFactory.createEmptyBorder());
		progressBar.setUI(new RoundedProgressBarUI());
		add(progressBar, SLayout.EXPAND_X);

		setVisible(false);
	}

	public void showOverlay(final String newEventId, final String title, final int progressPercent,
			final String value) {
		eventId = newEventId;
		applyValues(title, progressPercent, value);
		setVisible(true);
	}

	public void updateOverlay(final String newEventId, final String title, final int progressPercent,
			final String value) {
		eventId = newEventId;
		applyValues(title, progressPercent, value);
		if (!isVisible()) {
			setVisible(true);
		}
	}

	public void hideOverlay() {
		eventId = null;
		setVisible(false);
	}

	public boolean isShowingEvent(final String checkedEventId) {
		return (eventId != null) && eventId.equals(checkedEventId);
	}

	private void applyValues(final String title, final int progressPercent, final String value) {
		final int clampedPercent = Math.max(0, Math.min(100, progressPercent));
		eventTitle.setText((title == null || title.trim().isEmpty()) ? "Wydarzenie" : title);
		progressBar.setValue(clampedPercent);

		final String suffix = (value == null || value.trim().isEmpty()) ? "" : " • " + value;
		progressBar.putClientProperty("displayText", clampedPercent + "%" + suffix);
	}

	private static final class RoundedProgressBarUI extends BasicProgressBarUI {
		@Override
		protected void paintDeterminate(final Graphics g, final JComponent c) {
			final Graphics2D g2d = (Graphics2D) g.create();
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				final Insets insets = progressBar.getInsets();
				final int width = progressBar.getWidth() - (insets.right + insets.left);
				final int height = progressBar.getHeight() - (insets.top + insets.bottom);
				if ((width <= 0) || (height <= 0)) {
					return;
				}

				final int x = insets.left;
				final int y = insets.top;
				final int arc = Math.min(BAR_RADIUS * 2, height);
				final int clampedValue = Math.max(progressBar.getMinimum(),
						Math.min(progressBar.getMaximum(), progressBar.getValue()));
				final int range = Math.max(1, progressBar.getMaximum() - progressBar.getMinimum());
				final double fraction = (double) (clampedValue - progressBar.getMinimum()) / range;
				final int fillWidth = (int) Math.round(width * fraction);

				g2d.setColor(new Color(0, 0, 0, 70));
				g2d.fillRoundRect(x, y + 1, width, height, arc, arc);

				g2d.setColor(new Color(10, 12, 16, 170));
				g2d.fillRoundRect(x, y, width, height, arc, arc);

				if (fillWidth > 0) {
					final int warningThreshold = progressBar.getMinimum() + (int) Math.round(range * 0.25d);
					final boolean warning = clampedValue <= warningThreshold;
					final Color startColor = warning ? new Color(255, 186, 94) : new Color(125, 225, 255);
					final Color endColor = warning ? new Color(235, 88, 52) : new Color(28, 145, 255);

					g2d.setPaint(new GradientPaint(x, y, startColor, x, y + height, endColor));
					if (fillWidth >= arc) {
						g2d.fillRoundRect(x, y, fillWidth, height, arc, arc);
					} else {
						g2d.fillRoundRect(x, y, arc, height, arc, arc);
						g2d.setColor(new Color(10, 12, 16, 170));
						g2d.fillRect(x + fillWidth, y, Math.max(0, arc - fillWidth), height);
					}
				}

				g2d.setColor(new Color(236, 244, 255, 155));
				g2d.drawRoundRect(x, y, width - 1, height - 1, arc, arc);

				final Object textValue = progressBar.getClientProperty("displayText");
				final String text = (textValue instanceof String) ? (String) textValue : "";
				if (!text.isEmpty()) {
					g2d.setFont(progressBar.getFont());
					final int textX = x + (width - g2d.getFontMetrics().stringWidth(text)) / 2;
					final int textY = y + ((height - g2d.getFontMetrics().getHeight()) / 2)
							+ g2d.getFontMetrics().getAscent();

					g2d.setColor(new Color(0, 0, 0, 160));
					g2d.drawString(text, textX + 1, textY + 1);
					g2d.setColor(new Color(250, 252, 255));
					g2d.drawString(text, textX, textY);
				}
			} finally {
				g2d.dispose();
			}
		}

		@Override
		protected Color getSelectionForeground() {
			return new Color(245, 250, 255);
		}

		@Override
		protected Color getSelectionBackground() {
			return new Color(18, 22, 30);
		}
	}
}
