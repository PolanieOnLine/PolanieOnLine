/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;

public class Item2DViewGroundRarityTest {
	@Test
	public void rareGroundGlowFollowsSpriteSilhouetteWithoutCoveringIt() {
		final BufferedImage source = new BufferedImage(9, 9,
				BufferedImage.TYPE_INT_ARGB);
		for (int y = 3; y <= 5; y++) {
			for (int x = 3; x <= 5; x++) {
				source.setRGB(x, y, 0xffffffff);
			}
		}

		final BufferedImage glow = GroundItemRarityGlow.createGlow(
				source, ItemRarity.RARE, 3);
		final Color insideSprite = new Color(glow.getRGB(7, 7), true);
		final Color besideSprite = new Color(glow.getRGB(5, 7), true);
		final Color farAway = new Color(glow.getRGB(0, 0), true);

		assertEquals(0, insideSprite.getAlpha());
		assertTrue(besideSprite.getAlpha() >= 100);
		assertTrue(besideSprite.getBlue() > besideSprite.getRed());
		assertTrue(besideSprite.getBlue() > besideSprite.getGreen());
		assertEquals(0, farAway.getAlpha());
	}

	@Test
	public void higherRarityProducesStrongerOutlineGlow() {
		final BufferedImage source = new BufferedImage(5, 5,
				BufferedImage.TYPE_INT_ARGB);
		source.setRGB(2, 2, 0xffffffff);

		final BufferedImage common = GroundItemRarityGlow.createGlow(
				source, ItemRarity.COMMON, 3);
		final BufferedImage legendary = GroundItemRarityGlow.createGlow(
				source, ItemRarity.LEGENDARY, 3);

		final int commonAlpha = new Color(common.getRGB(4, 5), true).getAlpha();
		final int legendaryAlpha = new Color(legendary.getRGB(4, 5), true).getAlpha();
		assertTrue(legendaryAlpha > commonAlpha);
	}

	@Test
	public void missingRarityProducesTransparentGlow() {
		final BufferedImage source = new BufferedImage(5, 5,
				BufferedImage.TYPE_INT_ARGB);
		source.setRGB(2, 2, 0xffffffff);

		final BufferedImage glow = GroundItemRarityGlow.createGlow(source, null, 3);
		assertEquals(0, new Color(glow.getRGB(4, 5), true).getAlpha());
	}
}
