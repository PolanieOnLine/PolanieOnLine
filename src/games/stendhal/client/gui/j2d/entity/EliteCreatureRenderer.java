/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.client.entity.Creature;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.TextSprite;

/** Draws runtime-only elite visuals without touching creature sprite sheets. */
final class EliteCreatureRenderer {
	private static final String ELITE_TITLE_TYPE = "elite";
	private static final Color GOLD = new Color(255, 210, 64);
	private static final int MIN_GLOW_ALPHA = 96;
	private static final int TITLE_CACHE_SIZE = 64;

	private static final ThreadLocal<BufferedImage> GLOW_BUFFER =
			new ThreadLocal<BufferedImage>();
	private static final Map<String, Sprite> GOLD_TITLES =
			new LinkedHashMap<String, Sprite>(TITLE_CACHE_SIZE, 0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(final Map.Entry<String, Sprite> eldest) {
					return size() > TITLE_CACHE_SIZE;
				}
			};

	private EliteCreatureRenderer() {
	}

	static boolean isElite(final Creature entity) {
		return entity != null && ELITE_TITLE_TYPE.equals(entity.getTitleType());
	}

	/**
	 * Draws a thin gold silhouette behind the current animation frame. Weakly
	 * transparent pixels are discarded so the normal creature shadow does not
	 * turn into another ring-like marker around its feet.
	 */
	static void drawSpriteGlow(final Creature entity, final Sprite sprite,
			final Graphics2D g2d, final int x, final int y,
			final int width, final int height) {
		if (!isElite(entity) || sprite == null || width <= 0 || height <= 0) {
			return;
		}

		final BufferedImage buffer = getGlowBuffer(width, height);
		final Graphics2D bufferGraphics = buffer.createGraphics();
		try {
			bufferGraphics.setComposite(AlphaComposite.Clear);
			bufferGraphics.fillRect(0, 0, width, height);
			bufferGraphics.setComposite(AlphaComposite.SrcOver);
			sprite.draw(bufferGraphics, 0, 0);
		} finally {
			bufferGraphics.dispose();
		}

		final int[] pixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
		final int stride = buffer.getWidth();
		final int goldRgb = GOLD.getRGB() & 0x00ffffff;
		for (int row = 0; row < height; row++) {
			final int offset = row * stride;
			for (int col = 0; col < width; col++) {
				final int index = offset + col;
				final int alpha = pixels[index] >>> 24;
				pixels[index] = alpha >= MIN_GLOW_ALPHA
						? (alpha << 24) | goldRgb : 0;
			}
		}

		final Graphics2D copy = (Graphics2D) g2d.create();
		try {
			copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -1; dx <= 1; dx++) {
					if (dx == 0 && dy == 0) {
						continue;
					}
					drawBuffer(copy, buffer, x + dx, y + dy, width, height);
				}
			}
		} finally {
			copy.dispose();
		}
	}

	/** Draws the elite title in gold and places a small crown directly above it. */
	static void drawTitleAndCrown(final Creature entity, final Graphics2D g2d,
			final int x, final int statusY, final int width,
			final int titleDrawYOffset) {
		if (!isElite(entity) || entity.getTitle() == null) {
			return;
		}

		final Sprite title = getGoldTitle(entity.getTitle());
		final int titleX = x + (width - title.getWidth()) / 2;
		final int titleY = statusY - 3 - title.getHeight() + titleDrawYOffset;
		title.draw(g2d, titleX, titleY);
		drawCrown(g2d, x + width / 2, titleY - 2);
	}

	private static BufferedImage getGlowBuffer(final int width, final int height) {
		BufferedImage buffer = GLOW_BUFFER.get();
		if (buffer == null || buffer.getWidth() < width || buffer.getHeight() < height) {
			final int newWidth = buffer == null ? width : Math.max(width, buffer.getWidth());
			final int newHeight = buffer == null ? height : Math.max(height, buffer.getHeight());
			buffer = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			GLOW_BUFFER.set(buffer);
		}
		return buffer;
	}

	private static void drawBuffer(final Graphics2D g2d, final BufferedImage buffer,
			final int x, final int y, final int width, final int height) {
		g2d.drawImage(buffer, x, y, x + width, y + height,
				0, 0, width, height, null);
	}

	private static Sprite getGoldTitle(final String text) {
		synchronized (GOLD_TITLES) {
			Sprite title = GOLD_TITLES.get(text);
			if (title == null) {
				title = TextSprite.createTextSprite(text, GOLD);
				GOLD_TITLES.put(text, title);
			}
			return title;
		}
	}

	/**
	 * Crown bottom is anchored above the title rather than above the creature
	 * sprite, keeping the marker stable for both tiny and very large monsters.
	 */
	private static void drawCrown(final Graphics2D g2d, final int centerX,
			final int bottomY) {
		final Graphics2D copy = (Graphics2D) g2d.create();
		try {
			copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			final int topY = bottomY - 8;
			final int[] crownX = {centerX - 7, centerX - 4, centerX,
					centerX + 4, centerX + 7, centerX + 5, centerX - 5};
			final int[] crownY = {topY + 1, topY + 5, topY,
					topY + 5, topY + 1, bottomY, bottomY};

			copy.setColor(new Color(72, 49, 8, 220));
			copy.drawPolygon(crownX, crownY, crownX.length);
			copy.setColor(GOLD);
			copy.fillPolygon(crownX, crownY, crownX.length);
		} finally {
			copy.dispose();
		}
	}
}
