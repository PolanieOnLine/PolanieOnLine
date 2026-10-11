/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.client.sprite;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import games.stendhal.common.constants.ItemRarity;

/** The same thin alpha-silhouette outline in inventory slots and on the ground. */
public final class ItemRarityOutline {
	private static final int ALPHA_THRESHOLD = 16;
	private static final int[][] NEIGHBORS = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};

	private ItemRarityOutline() {
	}

	public static void paint(final Graphics graphics, final Sprite sprite,
			final ItemRarity rarity, final int x, final int y) {
		if (graphics == null || sprite == null || rarity == null
				|| sprite.getWidth() < 1 || sprite.getHeight() < 1) {
			return;
		}
		final BufferedImage source = new BufferedImage(sprite.getWidth(),
				sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D sourceGraphics = source.createGraphics();
		try {
			sprite.draw(sourceGraphics, 0, 0);
		} finally {
			sourceGraphics.dispose();
		}
		final BufferedImage outline = create(source, rarity);
		final Graphics2D output = (Graphics2D) graphics.create();
		try {
			output.setComposite(AlphaComposite.SrcOver.derive(getOpacity(rarity)));
			output.drawImage(outline, x - 1, y - 1, null);
		} finally {
			output.dispose();
		}
	}

	public static float getOpacity(final ItemRarity rarity) {
		switch (rarity) {
		case LEGENDARY: return 0.4f;
		case EPIC: return 0.7f;
		case RARE: return 0.5f;
		case COMMON:
		default: return 1.0f;
		}
	}

	/** Only four immediate neighbors; no fading halo and no diagonal thickening. */
	public static BufferedImage create(final BufferedImage source, final ItemRarity rarity) {
		if (source == null) {
			throw new IllegalArgumentException("source must not be null");
		}
		final BufferedImage outline = new BufferedImage(source.getWidth() + 2,
				source.getHeight() + 2, BufferedImage.TYPE_INT_ARGB);
		if (rarity == null) {
			return outline;
		}
		final int color = Color.decode(rarity.getColorHex()).getRGB();
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				if (!isOpaque(source, x, y)) {
					continue;
				}
				for (final int[] neighbor : NEIGHBORS) {
					final int nx = x + neighbor[0];
					final int ny = y + neighbor[1];
					if (!isOpaque(source, nx, ny)) {
						outline.setRGB(nx + 1, ny + 1, color);
					}
				}
			}
		}
		return outline;
	}

	private static boolean isOpaque(final BufferedImage source, final int x, final int y) {
		return x >= 0 && y >= 0 && x < source.getWidth() && y < source.getHeight()
				&& ((source.getRGB(x, y) >>> 24) & 0xff) > ALPHA_THRESHOLD;
	}
}
