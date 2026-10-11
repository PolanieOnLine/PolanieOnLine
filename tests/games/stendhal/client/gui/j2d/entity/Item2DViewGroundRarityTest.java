/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.ItemRarityOutline;
import games.stendhal.common.constants.ItemRarity;

public class Item2DViewGroundRarityTest {
	@Test
	public void groundOutlineHasOnlyFourImmediateNeighborsAndDoesNotCoverTheSprite() {
		final BufferedImage source = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		source.setRGB(2, 2, 0xffffffff);
		final BufferedImage outline = ItemRarityOutline.create(source, ItemRarity.RARE);
		assertEquals(7, outline.getWidth());
		assertEquals(7, outline.getHeight());
		int count = 0;
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				final int alpha = outline.getRGB(x, y) >>> 24;
				final boolean neighbor = Math.abs(x - 3) + Math.abs(y - 3) == 1;
				assertEquals(neighbor ? 255 : 0, alpha);
				if (alpha > 0) { count++; }
			}
		}
		assertEquals(4, count);
		assertEquals(0xffffffff, source.getRGB(2, 2));
	}

	@Test
	public void groundOutlinePreservesInventoryHueOpacityAndDrawingCoordinates() {
		final BufferedImage source = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		source.setRGB(2, 2, 0xffffffff);
		final int[] alphas = {255, 128, 179, 102};
		final ItemRarity[] rarities = {ItemRarity.COMMON, ItemRarity.RARE, ItemRarity.EPIC, ItemRarity.LEGENDARY};
		for (int i = 0; i < rarities.length; i++) {
			final BufferedImage target = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = target.createGraphics();
			try {
				ItemRarityOutline.paint(graphics, new ImageSprite(source), rarities[i], 4, 4);
			} finally { graphics.dispose(); }
			final Color pixel = new Color(target.getRGB(5, 6), true);
			final Color color = Color.decode(rarities[i].getColorHex());
			assertTrue(Math.abs(alphas[i] - pixel.getAlpha()) <= 1);
			// Alpha compositing may round an unpremultiplied color by one unit.
			assertTrue(Math.abs(color.getRed() - pixel.getRed()) <= 1);
			assertTrue(Math.abs(color.getGreen() - pixel.getGreen()) <= 1);
			assertTrue(Math.abs(color.getBlue() - pixel.getBlue()) <= 1);
			assertEquals(0, target.getRGB(6, 6));
			assertEquals(0, target.getRGB(4, 6));
		}
	}

	@Test
	public void transparentPixelsAndMissingRarityDoNotGenerateAnOutline() {
		final BufferedImage source = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		source.setRGB(2, 2, 0x10ffffff);
		assertEquals(0, ItemRarityOutline.create(source, ItemRarity.RARE).getRGB(2, 3));
		source.setRGB(2, 2, 0x11ffffff);
		assertNotEquals(0, ItemRarityOutline.create(source, ItemRarity.RARE).getRGB(2, 3));
		assertEquals(0, ItemRarityOutline.create(source, null).getRGB(2, 3));
	}
}
