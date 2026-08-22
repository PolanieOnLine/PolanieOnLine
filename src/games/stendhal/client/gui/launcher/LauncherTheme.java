/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/** Shared visual language for the desktop launcher. */
public final class LauncherTheme {

	public static final Color WINDOW_TOP = new Color(18, 23, 27);
	public static final Color WINDOW_BOTTOM = new Color(7, 10, 12);
	public static final Color PANEL_TOP = new Color(31, 31, 30, 238);
	public static final Color PANEL_BOTTOM = new Color(15, 16, 17, 246);
	public static final Color PANEL_INNER = new Color(12, 14, 16, 210);
	public static final Color GOLD = new Color(191, 143, 70);
	public static final Color GOLD_BRIGHT = new Color(226, 185, 111);
	public static final Color GOLD_DARK = new Color(92, 61, 30);
	public static final Color TEXT = new Color(232, 226, 211);
	public static final Color TEXT_MUTED = new Color(164, 160, 151);
	public static final Color SUCCESS = new Color(88, 176, 94);
	public static final Color DIVIDER = new Color(116, 87, 47, 135);

	private static final String DISPLAY_FONT = "BlackChancery";
	private static final String BODY_FONT = Font.SANS_SERIF;

	private LauncherTheme() {
	}

	public static Font displayFont(final int size) {
		return new Font(DISPLAY_FONT, Font.PLAIN, size);
	}

	public static Font bodyFont(final int style, final int size) {
		return new Font(BODY_FONT, style, size);
	}

	public static void configureGraphics(final Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

		graphics.setStroke(new BasicStroke(1.0f));
		graphics.setColor(GOLD_DARK);
		graphics.draw(outer);

		if (width > 8 && height > 8) {
			final RoundRectangle2D inner = new RoundRectangle2D.Float(x + 4, y + 4,
					width - 9, height - 9, Math.max(2, arc - 4), Math.max(2, arc - 4));
			graphics.setColor(new Color(211, 163, 88, 55));
			graphics.draw(inner);
		}
	}
}
