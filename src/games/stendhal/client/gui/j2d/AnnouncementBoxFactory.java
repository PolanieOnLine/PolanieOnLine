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
package games.stendhal.client.gui.j2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import games.stendhal.client.gui.TransparencyMode;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;

/**
 * Factory for decorative banner-like screen announcements.
 */
public class AnnouncementBoxFactory {
	private static final int MAX_TEXT_WIDTH = 520;
	private static final int HORIZONTAL_PADDING = 30;
	private static final int TOP_PADDING = 18;
	private static final int BOTTOM_PADDING = 18;
	private static final int TITLE_GAP = 8;
	private static final int ORNAMENT_SIZE = 12;

	public Sprite createAnnouncementBox(final String title, final String text, final String category) {
		final BannerPalette palette = BannerPalette.resolve(category);
		final String safeTitle = title == null ? "" : title.trim();
		final String safeText = text == null ? "" : text.trim();

		final Font titleFont = new Font("Serif", Font.BOLD, 18);
		final Font textFont = new Font("Serif", Font.BOLD, 21);
		final GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		final BufferedImage probe = configuration.createCompatibleImage(1, 1, TransparencyMode.TRANSPARENCY);
		final Graphics2D probeGraphics = probe.createGraphics();
		probeGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		probeGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final FontMetrics titleMetrics = probeGraphics.getFontMetrics(titleFont);
		final FontRenderContext context = probeGraphics.getFontRenderContext();
		final List<TextLayout> textLayouts = createLayouts(safeText, textFont, context);
		int textWidth = 0;
		int textHeight = 0;
		for (TextLayout layout : textLayouts) {
			textWidth = Math.max(textWidth, (int) Math.ceil(layout.getAdvance()));
			textHeight += (int) Math.ceil(layout.getAscent() + layout.getDescent() + layout.getLeading());
		}
		if (textLayouts.isEmpty()) {
			textHeight = probeGraphics.getFontMetrics(textFont).getHeight();
		}
		final int titleWidth = safeTitle.isEmpty() ? 0 : titleMetrics.stringWidth(safeTitle);
		final int width = Math.max(260, Math.max(textWidth, titleWidth) + (HORIZONTAL_PADDING * 2));
		final int height = TOP_PADDING + (safeTitle.isEmpty() ? 0 : titleMetrics.getHeight() + TITLE_GAP)
				+ textHeight + BOTTOM_PADDING;
		probeGraphics.dispose();

		final BufferedImage image = configuration.createCompatibleImage(width, height, TransparencyMode.TRANSPARENCY);
		final Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final RoundRectangle2D outer = new RoundRectangle2D.Double(0.5d, 0.5d, width - 1.0d, height - 1.0d, 28, 28);
		final RoundRectangle2D inner = new RoundRectangle2D.Double(5.0d, 5.0d, width - 10.0d, height - 10.0d, 22, 22);

		g2d.setPaint(new GradientPaint(0, 0, palette.topFill, 0, height, palette.bottomFill));
		g2d.fill(outer);
		g2d.setColor(palette.innerGlow);
		g2d.fill(inner);
		g2d.setStroke(new BasicStroke(2.2f));
		g2d.setColor(palette.border);
		g2d.draw(outer);
		g2d.setStroke(new BasicStroke(1.2f));
		g2d.setColor(palette.borderHighlight);
		g2d.draw(inner);
		drawOrnaments(g2d, width, height, palette);

		int y = TOP_PADDING;
		if (!safeTitle.isEmpty()) {
			g2d.setFont(titleFont);
			g2d.setColor(palette.titleColor);
			final FontMetrics metrics = g2d.getFontMetrics();
			g2d.drawString(safeTitle, (width - metrics.stringWidth(safeTitle)) / 2, y + metrics.getAscent());
			y += metrics.getHeight() + TITLE_GAP;
		}

		g2d.setFont(textFont);
		g2d.setColor(palette.textColor);
		if (textLayouts.isEmpty()) {
			final FontMetrics metrics = g2d.getFontMetrics();
			g2d.drawString(safeText, (width - metrics.stringWidth(safeText)) / 2, y + metrics.getAscent());
		} else {
			for (TextLayout layout : textLayouts) {
				y += Math.ceil(layout.getAscent());
				layout.draw(g2d, (float) ((width - layout.getAdvance()) / 2.0d), y);
				y += Math.ceil(layout.getDescent() + layout.getLeading());
			}
		}

		g2d.dispose();
		return new ImageSprite(image);
	}

	private List<TextLayout> createLayouts(final String text, final Font font, final FontRenderContext context) {
		final List<TextLayout> layouts = new ArrayList<TextLayout>();
		if (text == null || text.isEmpty()) {
			return layouts;
		}
		final AttributedString attributed = new AttributedString(text);
		attributed.addAttribute(TextAttribute.FONT, font);
		final AttributedCharacterIterator iterator = attributed.getIterator();
		final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, context);
		while (measurer.getPosition() < iterator.getEndIndex()) {
			layouts.add(measurer.nextLayout(MAX_TEXT_WIDTH));
		}
		return layouts;
	}

	private void drawOrnaments(final Graphics2D g2d, final int width, final int height, final BannerPalette palette) {
		g2d.setColor(palette.ornament);
		g2d.setStroke(new BasicStroke(1.5f));
		final int centerY = height / 2;
		g2d.drawLine(22, centerY, 60, centerY);
		g2d.drawLine(width - 60, centerY, width - 22, centerY);
		drawDiamond(g2d, 72, centerY, ORNAMENT_SIZE);
		drawDiamond(g2d, width - 72, centerY, ORNAMENT_SIZE);
		drawTopCrest(g2d, width / 2, 9, 24, palette);
	}

	private void drawDiamond(final Graphics2D g2d, final int centerX, final int centerY, final int size) {
		final Path2D diamond = new Path2D.Double();
		diamond.moveTo(centerX, centerY - size);
		diamond.lineTo(centerX + size, centerY);
		diamond.lineTo(centerX, centerY + size);
		diamond.lineTo(centerX - size, centerY);
		diamond.closePath();
		g2d.draw(diamond);
	}

	private void drawTopCrest(final Graphics2D g2d, final int centerX, final int topY, final int width,
			final BannerPalette palette) {
		final Path2D crest = new Path2D.Double();
		crest.moveTo(centerX - width, topY + 6);
		crest.curveTo(centerX - 12, topY - 2, centerX + 12, topY - 2, centerX + width, topY + 6);
		crest.lineTo(centerX + 11, topY + 6);
		crest.lineTo(centerX, topY + 18);
		crest.lineTo(centerX - 11, topY + 6);
		crest.closePath();
		g2d.setColor(palette.ornamentFill);
		g2d.fill(crest);
		g2d.setColor(palette.ornament);
		g2d.draw(crest);
	}

	private static final class BannerPalette {
		private final Color topFill;
		private final Color bottomFill;
		private final Color innerGlow;
		private final Color border;
		private final Color borderHighlight;
		private final Color textColor;
		private final Color titleColor;
		private final Color ornament;
		private final Color ornamentFill;

		private BannerPalette(final Color topFill, final Color bottomFill, final Color innerGlow, final Color border,
				final Color borderHighlight, final Color textColor, final Color titleColor, final Color ornament,
				final Color ornamentFill) {
			this.topFill = topFill;
			this.bottomFill = bottomFill;
			this.innerGlow = innerGlow;
			this.border = border;
			this.borderHighlight = borderHighlight;
			this.textColor = textColor;
			this.titleColor = titleColor;
			this.ornament = ornament;
			this.ornamentFill = ornamentFill;
		}

		private static BannerPalette resolve(final String category) {
			if ("admin".equalsIgnoreCase(category)) {
				return new BannerPalette(new Color(47, 20, 20, 228), new Color(22, 10, 10, 218),
						new Color(118, 40, 40, 54), new Color(215, 168, 90, 235),
						new Color(255, 226, 162, 180), new Color(255, 244, 214),
						new Color(255, 205, 110), new Color(245, 191, 96, 210), new Color(140, 50, 26, 150));
			}
			if ("event".equalsIgnoreCase(category)) {
				return new BannerPalette(new Color(18, 42, 56, 228), new Color(10, 22, 32, 218),
						new Color(34, 116, 138, 52), new Color(183, 220, 132, 235),
						new Color(236, 248, 200, 170), new Color(239, 250, 228),
						new Color(198, 232, 139), new Color(196, 232, 139, 210), new Color(62, 114, 102, 150));
			}
			return new BannerPalette(new Color(36, 36, 50, 224), new Color(18, 18, 28, 214),
					new Color(104, 118, 176, 44), new Color(197, 204, 230, 230),
					new Color(255, 255, 255, 144), new Color(246, 248, 255),
					new Color(225, 231, 255), new Color(206, 214, 240, 204), new Color(94, 107, 154, 150));
		}
	}
}
