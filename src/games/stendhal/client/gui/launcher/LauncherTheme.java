/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;

import games.stendhal.client.sprite.DataLoader;

/** Shared visual language for the desktop launcher. */
public final class LauncherTheme {

	public static final Color WINDOW_TOP = new Color(19, 24, 29);
	public static final Color WINDOW_BOTTOM = new Color(6, 9, 12);
	public static final Color PANEL_TOP = new Color(29, 30, 30, 246);
	public static final Color PANEL_BOTTOM = new Color(12, 14, 16, 250);
	public static final Color PANEL_INNER = new Color(9, 12, 15, 218);
	public static final Color GOLD = new Color(181, 132, 63);
	public static final Color GOLD_BRIGHT = new Color(224, 180, 101);
	public static final Color GOLD_DARK = new Color(78, 52, 27);
	public static final Color TEXT = new Color(235, 229, 215);
	public static final Color TEXT_MUTED = new Color(157, 158, 155);
	public static final Color SUCCESS = new Color(75, 187, 104);
	public static final Color DIVIDER = new Color(121, 89, 47, 125);
	public static final Color SHADOW = new Color(0, 0, 0, 150);

	private static final String DISPLAY_FONT_RESOURCE = "data/font/AntykwaTorunska.ttf";
	private static final Font DISPLAY_FONT = loadDisplayFont();
	private static final String BODY_FONT = "Carlito";

	private LauncherTheme() {
	}

	public static Font displayFont(final int size) {
		return DISPLAY_FONT.deriveFont(Font.BOLD, size);
	}

	public static Font bodyFont(final int style, final int size) {
		return new Font(BODY_FONT, style, size);
	}

	public static void configureGraphics(final Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	private static Font loadDisplayFont() {
		try (InputStream stream = DataLoader.getResourceAsStream(DISPLAY_FONT_RESOURCE)) {
			if (stream != null) {
				return Font.createFont(Font.TRUETYPE_FONT, stream);
			}
		} catch (final IOException | FontFormatException exception) {
			// The launcher must remain usable when an optional display font is unavailable.
		}
		return new Font(Font.SERIF, Font.BOLD, 12);
	}

	/** Paints the layered metal-like frame used by launcher panels. */
	public static void paintFrame(final Graphics2D graphics, final int x, final int y,
			final int width, final int height, final int arc) {
		if (width <= 2 || height <= 2) {
			return;
		}

		configureGraphics(graphics);
		final RoundRectangle2D outer = new RoundRectangle2D.Float(x, y, width - 1, height - 1, arc, arc);
		graphics.setPaint(new GradientPaint(x, y, PANEL_TOP, x, y + height, PANEL_BOTTOM));
		graphics.fill(outer);
		paintBorder(graphics, x, y, width, height, arc);
	}

	/** Paints only the layered border, leaving existing artwork untouched. */
	public static void paintBorder(final Graphics2D graphics, final int x, final int y,
			final int width, final int height, final int arc) {
		if (width <= 2 || height <= 2) {
			return;
		}

		configureGraphics(graphics);
		final RoundRectangle2D outer = new RoundRectangle2D.Float(x, y,
				width - 1, height - 1, arc, arc);
		graphics.setStroke(new BasicStroke(1.0f));
		graphics.setColor(GOLD_DARK);
		graphics.draw(outer);

		if (width > 8 && height > 8) {
			final RoundRectangle2D inner = new RoundRectangle2D.Float(x + 4, y + 4,
					width - 9, height - 9, Math.max(2, arc - 4), Math.max(2, arc - 4));
			graphics.setColor(new Color(211, 163, 88, 55));
			graphics.draw(inner);
		}

		final int corner = Math.min(13, Math.max(6, Math.min(width, height) / 5));
		graphics.setColor(new Color(224, 180, 101, 105));
		graphics.drawLine(x + 5, y + 5, x + corner, y + 5);
		graphics.drawLine(x + 5, y + 5, x + 5, y + corner);
		graphics.drawLine(x + width - 6, y + 5, x + width - corner - 1, y + 5);
		graphics.drawLine(x + width - 6, y + 5, x + width - 6, y + corner);
		graphics.drawLine(x + 5, y + height - 6, x + corner, y + height - 6);
		graphics.drawLine(x + 5, y + height - 6, x + 5, y + height - corner - 1);
		graphics.drawLine(x + width - 6, y + height - 6,
				x + width - corner - 1, y + height - 6);
		graphics.drawLine(x + width - 6, y + height - 6,
				x + width - 6, y + height - corner - 1);
	}
}
