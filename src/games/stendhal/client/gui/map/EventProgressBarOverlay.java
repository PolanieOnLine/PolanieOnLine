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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Overlay used to display active map event progress.
 */
public class EventProgressBarOverlay extends JPanel {
	private static final int BAR_WIDTH = 260;
	private static final int BAR_HEIGHT = 10;
	private static final int BAR_RADIUS = 8;
	private static final int PANEL_RADIUS = 9;
	private static final Color PANEL_BACKGROUND = new Color(22, 14, 10, 102);
	private static final Color PANEL_BORDER = new Color(168, 130, 92, 130);

	private final JLabel eventTitle;
	private final EventProgressBar progressBar;
	private String eventId;
	private float overlayAlpha = 1.0f;

	public EventProgressBarOverlay() {
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
		setAlignmentX(JComponent.CENTER_ALIGNMENT);
		setAlignmentY(JComponent.TOP_ALIGNMENT);
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		eventTitle = new JLabel();
		eventTitle.setOpaque(false);
		eventTitle.setBorder(BorderFactory.createEmptyBorder());
		eventTitle.setForeground(Color.WHITE);
		eventTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		eventTitle.setHorizontalAlignment(JLabel.CENTER);
		eventTitle.setAlignmentX(CENTER_ALIGNMENT);
		add(eventTitle, SLayout.EXPAND_X);
		add(Box.createVerticalStrut(5), SLayout.EXPAND_X);

		progressBar = new EventProgressBar();
		progressBar.setPreferredSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setMinimumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setMaximumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
		progressBar.setValue(0);
		progressBar.setFont(new Font(Font.DIALOG, Font.BOLD, 10));
		progressBar.setOpaque(false);
		progressBar.setBorder(BorderFactory.createEmptyBorder());
		add(progressBar, SLayout.EXPAND_X);

		setVisible(false);
	}

	public void showOverlay(final String newEventId, final String title, final int progressPercent,
			final String value) {
		eventId = newEventId;
		overlayAlpha = 1.0f;
		applyValues(title, progressPercent, value);
		setVisible(true);
		repaint();
	}

	public void updateOverlay(final String newEventId, final String title, final int progressPercent,
			final String value) {
		eventId = newEventId;
		applyValues(title, progressPercent, value);
		if (!isVisible()) {
			setVisible(true);
		}
		repaint();
	}

	public void showTerminalState(final String title, final String value) {
		eventId = null;
		overlayAlpha = 1.0f;
		applyValues(title, 100, value);
		setVisible(true);
		repaint();
	}

	public void setOverlayAlpha(final float alpha) {
		overlayAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
		repaint();
	}

	public void hideOverlay() {
		eventId = null;
		setVisible(false);
	}

	public boolean isShowingEvent(final String checkedEventId) {
		return (eventId != null) && eventId.equals(checkedEventId);
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

	private void applyValues(final String title, final int progressPercent, final String value) {
		final int clampedPercent = Math.max(0, Math.min(100, progressPercent));
		eventTitle.setText((title == null || title.trim().isEmpty()) ? "Wydarzenie" : title);
		progressBar.setValue(clampedPercent);

		final String suffix = (value == null || value.trim().isEmpty()) ? "" : " • " + value;
		progressBar.setDisplayText(clampedPercent + "%" + suffix);
	}

	private static final class EventProgressBar extends JComponent {
		private static final Color BACKGROUND_SHADOW = new Color(30, 18, 10, 58);
		private static final Color BACKGROUND_TOP = new Color(92, 66, 42, 98);
		private static final Color BACKGROUND_BOTTOM = new Color(51, 33, 20, 108);
		private static final Color BACKGROUND_GRAIN_TOP = new Color(126, 95, 64, 30);
		private static final Color BACKGROUND_GRAIN_BOTTOM = new Color(73, 48, 29, 18);

		private static final Color STANDARD_FILL_TOP = new Color(216, 157, 79);
		private static final Color STANDARD_FILL_BOTTOM = new Color(142, 85, 49);
		private static final Color FINAL_PHASE_FILL_TOP = new Color(188, 98, 56);
		private static final Color FINAL_PHASE_FILL_BOTTOM = new Color(120, 56, 35);

		private static final Color BAR_BORDER = new Color(129, 93, 60, 150);
		private static final Color TEXT_SHADOW = new Color(18, 11, 6, 190);
		private static final Color TEXT_COLOR = new Color(250, 241, 222);
		private int value;
		private String displayText = "";

		private void setValue(final int newValue) {
			value = Math.max(0, Math.min(100, newValue));
			repaint();
		}

		private void setDisplayText(final String newDisplayText) {
			displayText = (newDisplayText == null) ? "" : newDisplayText;
			repaint();
		}

		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2d = (Graphics2D) g.create();
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				final int width = getWidth();
				final int height = getHeight();
				if ((width <= 0) || (height <= 0)) {
					return;
				}

				final int x = 0;
				final int y = 0;
				final int arc = Math.min(BAR_RADIUS * 2, height);
				final int fillWidth = (int) Math.round(width * (value / 100.0d));

				g2d.setColor(BACKGROUND_SHADOW);
				g2d.fillRoundRect(x, y + 1, width, height, arc, arc);

				g2d.setPaint(new GradientPaint(x, y, BACKGROUND_TOP, x, y + height, BACKGROUND_BOTTOM));
				g2d.fillRoundRect(x, y, width, height, arc, arc);
				g2d.setPaint(new GradientPaint(x, y, BACKGROUND_GRAIN_TOP, x + width, y + height, BACKGROUND_GRAIN_BOTTOM));
				g2d.fillRoundRect(x, y, width, height, arc, arc);

				if (fillWidth > 0) {
					final boolean warning = value <= 25;
					final Color startColor = warning ? FINAL_PHASE_FILL_TOP : STANDARD_FILL_TOP;
					final Color endColor = warning ? FINAL_PHASE_FILL_BOTTOM : STANDARD_FILL_BOTTOM;

					g2d.setPaint(new GradientPaint(x, y, startColor, x, y + height, endColor));
					if (fillWidth >= arc) {
						g2d.fillRoundRect(x, y, fillWidth, height, arc, arc);
					} else {
						g2d.fillRoundRect(x, y, arc, height, arc, arc);
						g2d.setPaint(new GradientPaint(x, y, BACKGROUND_TOP, x, y + height, BACKGROUND_BOTTOM));
						g2d.fillRect(x + fillWidth, y, Math.max(0, arc - fillWidth), height);
					}
				}

				g2d.setColor(BAR_BORDER);
				g2d.drawRoundRect(x, y, width - 1, height - 1, arc, arc);

				if (!displayText.isEmpty()) {
					g2d.setFont(getFont());
					final FontMetrics metrics = g2d.getFontMetrics();
					final int textX = x + (width - metrics.stringWidth(displayText)) / 2;
					final int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();

					g2d.setColor(TEXT_SHADOW);
					g2d.drawString(displayText, textX + 1, textY + 1);
					g2d.drawString(displayText, textX - 1, textY + 1);
					g2d.setColor(TEXT_COLOR);
					g2d.drawString(displayText, textX, textY);
				}
			} finally {
				g2d.dispose();
			}
		}
	}
}
