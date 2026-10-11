/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.sprite;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Field;

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

	@Test
	public void pixelAlignedNamesKeepSmoothEdgesAndReuseTheirDeviceSizeImage() throws Exception {
		final TextSprite sprite = TextSprite.createTextSprite("karajuss", Color.YELLOW);
		final BufferedImage image = new BufferedImage(240, 80, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.scale(1.5, 1.25);
		graphics.translate(-0.27, 0.33);
		final AffineTransform original = graphics.getTransform();
		final Field cache = TextSprite.class.getDeclaredField("alignedImage");
		cache.setAccessible(true);
		try {
			sprite.drawPixelAligned(graphics, 20.3, 12.7);
			final Object first = cache.get(sprite);
			sprite.drawPixelAligned(graphics, 31.8, 18.4);
			assertSame("camera/name movement must not rebuild glyph images", first, cache.get(sprite));
			assertEquals(original, graphics.getTransform());
			boolean smoothEdges = false;
			for (final int pixel : image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())) {
				final int alpha = pixel >>> 24;
				smoothEdges |= alpha > 0 && alpha < 255;
			}
			assertTrue("names must remain antialiased rather than blocky enlarged bitmaps", smoothEdges);
			graphics.scale(1.1, 1.1);
			sprite.drawPixelAligned(graphics, 20.3, 12.7);
			assertNotSame("changing zoom/DPI must refresh the device-size glyph cache", first, cache.get(sprite));
		} finally {
			graphics.dispose();
		}
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
