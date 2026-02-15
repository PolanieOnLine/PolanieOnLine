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
	private float overlayAlpha = 1.0f;

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
		progressBar.putClientProperty("displayText", clampedPercent + "%" + suffix);
	}

	private static final class RoundedProgressBarUI extends BasicProgressBarUI {
		private static final Color BACKGROUND_SHADOW = new Color(30, 18, 10, 90);
		private static final Color BACKGROUND_TOP = new Color(92, 66, 42, 205);
		private static final Color BACKGROUND_BOTTOM = new Color(51, 33, 20, 220);
		private static final Color BACKGROUND_GRAIN_TOP = new Color(126, 95, 64, 50);
		private static final Color BACKGROUND_GRAIN_BOTTOM = new Color(73, 48, 29, 30);

		private static final Color STANDARD_FILL_TOP = new Color(216, 157, 79);
		private static final Color STANDARD_FILL_BOTTOM = new Color(142, 85, 49);
		private static final Color FINAL_PHASE_FILL_TOP = new Color(188, 98, 56);
		private static final Color FINAL_PHASE_FILL_BOTTOM = new Color(120, 56, 35);

		private static final Color BAR_BORDER = new Color(129, 93, 60, 190);
		private static final Color TEXT_SHADOW = new Color(18, 11, 6, 190);
		private static final Color TEXT_COLOR = new Color(250, 241, 222);

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

				g2d.setColor(BACKGROUND_SHADOW);
				g2d.fillRoundRect(x, y + 1, width, height, arc, arc);

				g2d.setPaint(new GradientPaint(x, y, BACKGROUND_TOP, x, y + height, BACKGROUND_BOTTOM));
				g2d.fillRoundRect(x, y, width, height, arc, arc);
				g2d.setPaint(new GradientPaint(x, y, BACKGROUND_GRAIN_TOP, x + width, y + height, BACKGROUND_GRAIN_BOTTOM));
				g2d.fillRoundRect(x, y, width, height, arc, arc);

				if (fillWidth > 0) {
					final int warningThreshold = progressBar.getMinimum() + (int) Math.round(range * 0.25d);
					final boolean warning = clampedValue <= warningThreshold;
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

				final Object textValue = progressBar.getClientProperty("displayText");
				final String text = (textValue instanceof String) ? (String) textValue : "";
				if (!text.isEmpty()) {
					g2d.setFont(progressBar.getFont());
					final int textX = x + (width - g2d.getFontMetrics().stringWidth(text)) / 2;
					final int textY = y + ((height - g2d.getFontMetrics().getHeight()) / 2)
							+ g2d.getFontMetrics().getAscent();

					g2d.setColor(TEXT_SHADOW);
					g2d.drawString(text, textX + 1, textY + 1);
					g2d.drawString(text, textX - 1, textY + 1);
					g2d.setColor(TEXT_COLOR);
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
