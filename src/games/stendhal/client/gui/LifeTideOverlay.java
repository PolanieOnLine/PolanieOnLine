/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Graphics;

/** Shared surplus-HP overlay for overhead and HUD health bars. */
public final class LifeTideOverlay {
	public static final Color COLOR = new Color(182, 124, 240);
	private static final long PERIOD_MILLISECONDS = 2400L;

	private LifeTideOverlay() { }

	public static double opacity(final long now) {
		return 0.65 - 0.18 * Math.cos(2.0 * Math.PI * (now % PERIOD_MILLISECONDS) / PERIOD_MILLISECONDS);
	}

	public static void draw(final Graphics graphics, final int x, final int y,
			final int width, final int height, final double surplusRatio, final long now) {
		if (!Double.isFinite(surplusRatio) || surplusRatio <= 0 || width <= 0 || height <= 0) {
			return;
		}
		final Graphics overlay = graphics.create();
		try {
			overlay.setColor(new Color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(),
					(int) Math.round(255 * opacity(now))));
			final int coveredWidth = (int) Math.round(width * Math.min(1.0, surplusRatio));
			// Leave diagonal gaps completely untouched so normal HP remains visible.
			for (int row = 0; row < height; row++) {
				for (int stripe = -row; stripe < coveredWidth; stripe += 8) {
					final int left = Math.max(0, stripe);
					final int right = Math.min(coveredWidth, stripe + 5);
					if (right > left) {
						overlay.fillRect(x + left, y + row, right - left, 1);
					}
				}
			}
		} finally {
			overlay.dispose();
		}
	}
}
