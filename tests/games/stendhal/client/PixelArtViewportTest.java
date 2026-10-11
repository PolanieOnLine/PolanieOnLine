/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.TextSprite;

public class PixelArtViewportTest {
	@After
	public void resetFrameHelper() {
		GameScreenSpriteHelper.endFrame();
		GameScreenSpriteHelper.setWorldWidth(0);
		GameScreenSpriteHelper.setWorldHeight(0);
		GameScreenSpriteHelper.setScale(1.0);
		GameScreenSpriteHelper.setScreenViewX(0);
		GameScreenSpriteHelper.setScreenViewY(0);
	}

	@Test
	public void worldLayersHaveWholeDeviceTranslationAtZoomAndHiDpi() {
		for (final double dpi : new double[] {1.0, 1.25, 1.5, 2.0}) {
			for (final double zoom : new double[] {1.0, 1.3, 1.5, 2.0}) {
				final Graphics2D destination = canvas().createGraphics();
				destination.scale(dpi, dpi);
				destination.translate(0.3, 0.7);
				final AffineTransform original = destination.getTransform();
				final PixelArtViewport viewport = new PixelArtViewport(destination, zoom, 31.42, -10.27);
				final Graphics2D world = viewport.createWorldGraphics(destination);
				try {
					final AffineTransform transform = world.getTransform();
					assertEquals(Math.round(transform.getTranslateX()), transform.getTranslateX(), 0.000001);
					assertEquals(Math.round(transform.getTranslateY()), transform.getTranslateY(), 0.000001);
					assertEquals(dpi * zoom, transform.getScaleX(), 0.000001);
					assertSame(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
							world.getRenderingHint(RenderingHints.KEY_INTERPOLATION));
					assertEquals(original, destination.getTransform());
				} finally {
					world.dispose();
					destination.dispose();
				}
			}
		}
	}

	@Test
	public void stationaryMapPixelsNeverChangeColourWhileCameraScrolls() {
		final TileRenderer tiles = tiles();
		for (final double dpi : new double[] {1.0, 1.25, 1.5}) {
			for (final double zoom : new double[] {1.0, 1.3, 1.5, 2.0}) {
				BufferedImage previous = null;
				int previousX = 0;
				int previousY = 0;
				for (int frame = 0; frame < 25; frame++) {
					final BufferedImage image = canvas();
					final Graphics2D destination = image.createGraphics();
					destination.scale(dpi, dpi);
					final PixelArtViewport viewport = new PixelArtViewport(destination, zoom,
							20 + frame * 0.23, 22 + frame * 0.17);
					final Graphics2D world = viewport.createWorldGraphics(destination);
					final int cameraX = (int) Math.round(world.getTransform().getTranslateX());
					final int cameraY = (int) Math.round(world.getTransform().getTranslateY());
					try {
						tiles.draw(world, 0, 0, 24, 24);
					} finally {
						world.dispose();
						destination.dispose();
					}
					if (previous != null) {
						try {
							assertTranslatedPixels(previous, image, cameraX - previousX, cameraY - previousY);
						} catch (final AssertionError failure) {
							throw new AssertionError("dpi=" + dpi + ", zoom=" + zoom + ", frame=" + frame, failure);
						}
					}
					previous = image;
					previousX = cameraX;
					previousY = cameraY;
				}
			}
		}
	}

	@Test
	public void cachedNamesKeepIdenticalShapeWhileFollowingCamera() {
		final TextSprite title = TextSprite.createTextSprite("karajuss / rycerz szafirowy", Color.YELLOW);
		for (final double dpi : new double[] {1.0, 1.25, 1.5}) {
			for (final double zoom : new double[] {1.0, 1.3, 1.5, 2.0}) {
				BufferedImage previous = null;
				Rectangle previousBounds = null;
				for (int frame = 0; frame < 15; frame++) {
					final BufferedImage image = canvas();
					final Graphics2D destination = image.createGraphics();
					destination.scale(dpi, dpi);
					final Graphics2D world = new PixelArtViewport(destination, zoom,
							10 + frame * 0.27, 8 + frame * 0.19).createWorldGraphics(destination);
					try {
						title.drawPixelAligned(world, 32.5, 35);
					} finally {
						world.dispose();
						destination.dispose();
					}
					final Rectangle bounds = opaqueBounds(image);
					assertTrue("title must render", bounds.width > 0);
					if (previous != null) {
						assertEquals(previousBounds.getSize(), bounds.getSize());
						assertArrayEquals("letters must not change coverage between camera phases",
								previous.getRGB(previousBounds.x, previousBounds.y, bounds.width, bounds.height,
										null, 0, bounds.width),
								image.getRGB(bounds.x, bounds.y, bounds.width, bounds.height, null, 0, bounds.width));
					}
					previous = image;
					previousBounds = bounds;
				}
			}
		}
	}

	@Test
	public void clicksAndAttachedOverlaysUseTheDisplayedFrameNotNextCameraUpdate() {
		final Graphics2D destination = canvas().createGraphics();
		destination.scale(1.25, 1.25);
		final PixelArtViewport viewport = new PixelArtViewport(destination, 1.5, 20.7, 31.4);
		destination.dispose();
		GameScreenSpriteHelper.setScale(1.5);
		GameScreenSpriteHelper.beginFrame(viewport);
		for (final double worldX : new double[] {0.0, 1.234, 7.5}) {
			final double screenX = viewport.worldToScreenX(worldX * 32);
			final double screenY = viewport.worldToScreenY(4.25 * 32);
			final Point2D hit = viewport.screenToWorld(screenX, screenY);
			assertEquals(worldX * 32, hit.getX(), 0.000001);
			assertEquals(4.25 * 32, hit.getY(), 0.000001);
			assertEquals(Math.round(screenX), GameScreenSpriteHelper.convertWorldXToScaledScreen(worldX)
					- GameScreenSpriteHelper.getScreenViewX());
			assertEquals(Math.round(screenY), GameScreenSpriteHelper.convertWorldYToScaledScreen(4.25)
					- GameScreenSpriteHelper.getScreenViewY());
		}
		GameScreenSpriteHelper.endFrame();
		GameScreenSpriteHelper.setScreenViewX(500);
		GameScreenSpriteHelper.setScreenViewY(600);
		assertSame(viewport, GameScreenSpriteHelper.getRenderedViewport());
		assertEquals((int) Math.round(viewport.getCameraX()), GameScreenSpriteHelper.getScreenViewX());
		GameScreenSpriteHelper.setScale(1.0);
		assertNull("a resize must invalidate the old scale", GameScreenSpriteHelper.getRenderedViewport());
	}

	@Test
	public void sourceTransformHintsAndClipAreNotChangedByDrawing() {
		final Graphics2D source = canvas().createGraphics();
		source.setClip(11, 12, 150, 130);
		source.translate(5.25, 3.75);
		source.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		final AffineTransform transform = source.getTransform();
		final Rectangle clip = source.getClipBounds();
		final Graphics2D world = new PixelArtViewport(source, 1.3, 5.1, 7.6).createWorldGraphics(source);
		final Graphics2D sprite = PixelArtViewport.createSpriteGraphics(world, 30, 40, 0.27, 0.69);
		sprite.dispose();
		world.dispose();
		assertEquals(transform, source.getTransform());
		assertEquals(clip, source.getClipBounds());
		assertSame(RenderingHints.VALUE_INTERPOLATION_BILINEAR,
				source.getRenderingHint(RenderingHints.KEY_INTERPOLATION));
		source.dispose();
	}

	private static BufferedImage canvas() {
		return new BufferedImage(800, 400, BufferedImage.TYPE_INT_ARGB);
	}

	private static TileRenderer tiles() {
		final BufferedImage texture = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++) {
				texture.setRGB(x, y, new Color((x * 37 + y * 13) % 256,
						(x * 17 + y * 53) % 256, (x * 67 + y * 7) % 256).getRGB());
			}
		}
		final TileRenderer renderer = new TileRenderer();
		renderer.width = renderer.height = 24;
		renderer.spriteMap = new Sprite[24 * 24];
		Arrays.fill(renderer.spriteMap, new ImageSprite(texture));
		return renderer;
	}

	private static void assertTranslatedPixels(final BufferedImage previous,
			final BufferedImage current, final int dx, final int dy) {
		for (int y = 80; y < 230; y++) {
			for (int x = 80; x < 350; x++) {
				if (previous.getRGB(x, y) != current.getRGB(x + dx, y + dy)) {
					fail("map pixel changed colour after camera translation at " + x + "," + y);
				}
			}
		}
	}

	private static Rectangle opaqueBounds(final BufferedImage image) {
		int minX = image.getWidth(), minY = image.getHeight(), maxX = -1, maxY = -1;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}
		return maxX < 0 ? new Rectangle() : new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}
}
