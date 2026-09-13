/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.sprite;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

public class TextSpriteTest {
	@Test
	public void smoothDrawingPreservesFractionalMotion() {
		final TextSprite sprite = TextSprite.createTextSprite("PolanieOnLine",
				new Color(200, 200, 0));

		final double wholePixelCenter = alphaCenter(sprite, 20.0);
		final double halfPixelCenter = alphaCenter(sprite, 20.5);
		final double movement = halfPixelCenter - wholePixelCenter;

		assertTrue("smooth text was snapped instead of moving fractionally",
				movement > 0.2);
		assertTrue("smooth text jumped by a full pixel", movement < 0.8);
	}

	private double alphaCenter(final TextSprite sprite, final double x) {
		final BufferedImage image = new BufferedImage(180, 48,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			sprite.drawSmooth(graphics, x, 12.0);
		} finally {
			graphics.dispose();
		}

		double weightedX = 0.0;
		long totalAlpha = 0L;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int pixelX = 0; pixelX < image.getWidth(); pixelX++) {
				final int alpha = (image.getRGB(pixelX, y) >>> 24) & 0xff;
				weightedX += alpha * pixelX;
				totalAlpha += alpha;
			}
		}

		assertTrue("smooth text did not render any pixels", totalAlpha > 0L);
		return weightedX / totalAlpha;
	}
}
