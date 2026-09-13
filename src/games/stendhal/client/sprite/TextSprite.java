/* $Id$ */
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
package games.stendhal.client.sprite;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import games.stendhal.client.gui.TransparencyMode;

/**
 * Outlined text representation of a string.
 */
public class TextSprite extends ImageSprite {
	// needed only because there's no other reliable way to calculate
	// string widths other than having a Graphics object
	private static final Graphics graphics = (new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)).getGraphics();
	/* Keep the legacy hinted glyph advances. Fractional positioning is applied
	 * only while the already-laid-out glyph run is painted. */
	private static final FontRenderContext SMOOTH_FONT_RENDER_CONTEXT =
			new FontRenderContext(new AffineTransform(), false, false);

	private final GlyphVector smoothGlyphs;
	private final Color textColor;
	private final Color outlineColor;
	private final int baseline;

	private TextSprite(final Image image, final GlyphVector smoothGlyphs,
			final Color textColor, final Color outlineColor, final int baseline) {
		super(image);
		this.smoothGlyphs = smoothGlyphs;
		this.textColor = textColor;
		this.outlineColor = outlineColor;
		this.baseline = baseline;
	}

	/**
	 * Create a new <code>TextSprite</code>
	 *
	 * @param text The text to be rendered
	 * @param textColor Color of the text
	 * @return TextSprite with the wanted text
	 */
	public static TextSprite createTextSprite(String text, final Color textColor) {
		final GraphicsConfiguration gc = getGC();
		FontMetrics metrics = graphics.getFontMetrics();
		LineMetrics lm = metrics.getLineMetrics(text, graphics);
		final int baseline = Math.round(lm.getAscent());
		final Image image = gc.createCompatibleImage(metrics.stringWidth(text)
				+ 2, Math.round(lm.getHeight()) + 2, TransparencyMode.TRANSPARENCY);
		final Color outlineColor = determineOutlineColor(textColor);

		drawOutlineString(image, textColor, outlineColor, text, 1, baseline);
		final GlyphVector smoothGlyphs = graphics.getFont().createGlyphVector(
				SMOOTH_FONT_RENDER_CONTEXT, text);

		return new TextSprite(image, smoothGlyphs, textColor, outlineColor, baseline);
	}

	/**
	 * Draw text at a fractional position while keeping the old compact glyph
	 * metrics and the legacy one-pixel, eight-direction outline. Using filled
	 * glyph outlines lets the whole label move between pixels without changing
	 * its layout or resampling a cached bitmap.
	 *
	 * @param source destination graphics
	 * @param x left edge of the text sprite in user-space pixels
	 * @param y top edge of the text sprite in user-space pixels
	 */
	public void drawSmooth(final Graphics2D source, final double x, final double y) {
		final Graphics2D g = (Graphics2D) source.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			final float gx = (float) (x + 1.0);
			final float gy = (float) (y + baseline);

			g.setColor(outlineColor);
			fillGlyphs(g, gx - 1.0f, gy - 1.0f);
			fillGlyphs(g, gx + 1.0f, gy + 1.0f);
			fillGlyphs(g, gx - 1.0f, gy + 1.0f);
			fillGlyphs(g, gx, gy - 1.0f);
			fillGlyphs(g, gx + 1.0f, gy);
			fillGlyphs(g, gx - 1.0f, gy);
			fillGlyphs(g, gx, gy + 1.0f);
			fillGlyphs(g, gx + 1.0f, gy - 1.0f);

			g.setColor(textColor);
			fillGlyphs(g, gx, gy);
		} finally {
			g.dispose();
		}
	}

	private void fillGlyphs(final Graphics2D graphics2d, final float x,
			final float y) {
		final Shape shape = smoothGlyphs.getOutline(x, y);
		graphics2d.fill(shape);
	}

	/**
	 * Draw a text string (like <em>Graphics</em><code>.drawString()</code>)
	 * only with an outline border. The area drawn extends 1 pixel out on all
	 * side from what would normal be drawn by drawString().
	 *
	 * @param image image to draw to
	 * @param textColor Color of the text
	 * @param text The text to draw
	 * @param x X position
	 * @param y Y position
	 */
	private static void drawOutlineString(final Image image, final Color textColor,
			final String text, final int x, final int y) {
		drawOutlineString(image, textColor, determineOutlineColor(textColor), text, x, y);
	}

	private static Color determineOutlineColor(final Color textColor) {
		/*
		 * Use light gray as outline for colors < 25% bright. Luminance = 0.299R +
		 * 0.587G + 0.114B
		 */
		final int lum = ((textColor.getRed() * 299) + (textColor.getGreen() * 587)
				+ (textColor.getBlue() * 114)) / 1000;
		return lum >= 64 ? Color.black : Color.lightGray;
	}

	/**
	 * Draw a text string (like <em>Graphics</em><code>.drawString()</code>)
	 * only with an outline border. The area drawn extends 1 pixel out on all
	 * side from what would normal be drawn by drawString().
	 *
	 * @param image image to draw to
	 * @param textColor Color of the text
	 * @param outlineColor Color of the outline
	 * @param text The text to draw
	 * @param x X position
	 * @param y Y position
	 */
	private static void drawOutlineString(final Image image, final Color textColor,
			final Color outlineColor, final String text, final int x,
			final int y) {
		Graphics g = image.getGraphics();
		g.setColor(outlineColor);

		// The same text will be drawn eight times to create a border
		// note that this is not a good solution, but re-using the image
		// to draw it again doesn't work on Mac OSX
		g.drawString(text, x - 1, y - 1);
		g.drawString(text, x + 1, y + 1);
		g.drawString(text, x - 1, y + 1);
		g.drawString(text, x, y - 1);
		g.drawString(text, x + 1, y);
		g.drawString(text, x - 1, y);
		g.drawString(text, x, y + 1);
		g.drawString(text, x + 1, y - 1);

		g.setColor(textColor);
		g.drawString(text, x, y);
	}
}
