/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;

public class Item2DViewGroundRarityTest {
	@Test
	public void rareGroundGlowIsStrongestInCenterAndUsesRarityColor() {
		final BufferedImage image = new BufferedImage(32, 32,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			Item2DView.paintGroundRarityGlow(graphics, ItemRarity.RARE,
					0, 0, 32, 32);
		} finally {
			graphics.dispose();
		}

		final Color center = new Color(image.getRGB(16, 16), true);
		final Color edge = new Color(image.getRGB(1, 1), true);
		assertTrue(center.getAlpha() > edge.getAlpha());
		assertTrue(center.getAlpha() > 0);
		assertTrue(center.getBlue() > center.getRed());
		assertTrue(center.getBlue() > center.getGreen());
	}

	@Test
	public void missingRarityDoesNotPaintGroundGlow() {
		final BufferedImage image = new BufferedImage(32, 32,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			Item2DView.paintGroundRarityGlow(graphics, null, 0, 0, 32, 32);
		} finally {
			graphics.dispose();
		}

		assertEquals(0, new Color(image.getRGB(16, 16), true).getAlpha());
	}
}
