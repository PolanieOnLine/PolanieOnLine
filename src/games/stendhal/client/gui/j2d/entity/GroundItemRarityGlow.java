/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import games.stendhal.client.sprite.Sprite;
import games.stendhal.common.constants.ItemRarity;

/**
 * Builds a rarity glow that follows the alpha silhouette of a ground item.
 */
final class GroundItemRarityGlow {
	private static final int GLOW_RADIUS = 3;
	private static final int ALPHA_THRESHOLD = 16;

	private GroundItemRarityGlow() {
	}

	static void paint(final Graphics2D graphics, final Sprite sprite,
			final ItemRarity rarity, final int x, final int y,
			final int width, final int height) {
		if (graphics == null || sprite == null || rarity == null
				|| width < 1 || height < 1) {
			return;
		}

		final BufferedImage source = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D sourceGraphics = source.createGraphics();
		try {
			sprite.draw(sourceGraphics, 0, 0);
		} finally {
			sourceGraphics.dispose();
		}

		final BufferedImage glow = createGlow(source, rarity, GLOW_RADIUS);
		graphics.drawImage(glow, x - GLOW_RADIUS, y - GLOW_RADIUS, null);
	}

	/**
	 * Creates a pixel-art friendly glow around, but never over, opaque sprite
	 * pixels. The nearest ring is strongest and the following rings fade out.
	 */
	static BufferedImage createGlow(final BufferedImage source,
			final ItemRarity rarity, final int radius) {
		if (source == null) {
			throw new IllegalArgumentException("source must not be null");
		}

		final int safeRadius = Math.max(1, radius);
		final int width = source.getWidth();
		final int height = source.getHeight();
		final BufferedImage glow = new BufferedImage(width + safeRadius * 2,
				height + safeRadius * 2, BufferedImage.TYPE_INT_ARGB);
		if (rarity == null) {
			return glow;
		}

		final Color rarityColor;
		try {
			rarityColor = Color.decode(rarity.getColorHex());
		} catch (NumberFormatException e) {
			return glow;
		}
		final float strength = getStrength(rarity);

		for (int sy = 0; sy < height; sy++) {
			for (int sx = 0; sx < width; sx++) {
				final int sourceAlpha = (source.getRGB(sx, sy) >>> 24) & 0xff;
				if (sourceAlpha <= ALPHA_THRESHOLD) {
					continue;
				}

				for (int dy = -safeRadius; dy <= safeRadius; dy++) {
					for (int dx = -safeRadius; dx <= safeRadius; dx++) {
						if (dx == 0 && dy == 0) {
							continue;
						}
						final double distance = Math.sqrt(dx * dx + dy * dy);
						if (distance > safeRadius) {
							continue;
						}

						final int targetSourceX = sx + dx;
						final int targetSourceY = sy + dy;
						if (isOpaque(source, targetSourceX, targetSourceY)) {
							continue;
						}

						final float falloff = (float) ((safeRadius + 1.0 - distance)
								/ safeRadius);
						final int alpha = clampAlpha(Math.round(255.0f * strength
								* falloff * sourceAlpha / 255.0f));
						final int gx = targetSourceX + safeRadius;
						final int gy = targetSourceY + safeRadius;
						if (gx < 0 || gy < 0 || gx >= glow.getWidth()
								|| gy >= glow.getHeight()) {
							continue;
						}

						final int oldAlpha = (glow.getRGB(gx, gy) >>> 24) & 0xff;
						if (alpha > oldAlpha) {
							glow.setRGB(gx, gy, (alpha << 24)
									| (rarityColor.getRed() << 16)
									| (rarityColor.getGreen() << 8)
									| rarityColor.getBlue());
						}
					}
				}
			}
		}

		return glow;
	}

	private static boolean isOpaque(final BufferedImage source,
			final int x, final int y) {
		if (x < 0 || y < 0 || x >= source.getWidth() || y >= source.getHeight()) {
			return false;
		}
		return ((source.getRGB(x, y) >>> 24) & 0xff) > ALPHA_THRESHOLD;
	}

	private static int clampAlpha(final int alpha) {
		return Math.max(0, Math.min(255, alpha));
	}

	private static float getStrength(final ItemRarity rarity) {
		switch (rarity) {
		case LEGENDARY:
			return 0.6f;
		case EPIC:
			return 0.4f;
		case RARE:
			return 0.5f;
		case COMMON:
		default:
			return 0.6f;
		}
	}
}
