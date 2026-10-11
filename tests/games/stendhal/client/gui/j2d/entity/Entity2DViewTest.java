/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.client.entity.Entity;

public class Entity2DViewTest {
	@Test
	public void topLayerRoundsOnlyFinalDevicePositionWithoutBilinearFiltering() {
		final TestEntity entity = new TestEntity(0.2, 0.3);
		final TestView view = new TestView();
		view.initialize(entity);
		view.applyChanges();

		final BufferedImage image = new BufferedImage(64, 64,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			view.drawTop(graphics);
		} finally {
			graphics.dispose();
		}

		assertEquals(0.0, view.translateX, 0.0001);
		assertEquals(1.0, view.translateY, 0.0001);
		assertSame(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
				view.interpolationHint);
	}

	@Test
	public void bodyAndNameplateSharePositionAtFractionalZoomAndHiDpi() {
		for (final double scale : new double[] {1.0, 1.25, 1.5, 2.0}) {
			final TestView view = new TestView();
			view.initialize(new TestEntity(0.2, 0.3));
			final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = image.createGraphics();
			try {
				graphics.scale(scale, scale);
				graphics.translate(-0.37, 0.24);
				view.draw(graphics);
				final double bodyX = view.translateX;
				final double bodyY = view.translateY;
				assertSame(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, view.interpolationHint);
				view.drawTop(graphics);
				assertEquals(bodyX, view.translateX, 0.000001);
				assertEquals(bodyY, view.translateY, 0.000001);
				assertEquals(Math.round(bodyX + 6 * scale), bodyX + 6 * scale, 0.000001);
				assertEquals(Math.round(bodyY + 9 * scale), bodyY + 9 * scale, 0.000001);
			} finally {
				graphics.dispose();
			}
		}
	}

	private static final class TestEntity extends Entity {
		private TestEntity(final double x, final double y) {
			this.x = x;
			this.y = y;
		}
	}

	private static final class TestView extends Entity2DView<TestEntity> {
		private double translateX;
		private double translateY;
		private Object interpolationHint;

		@Override
		protected void buildRepresentation(final TestEntity entity) {
			// The test only needs positioning; no sprite is required.
		}

		@Override
		protected void draw(final Graphics2D graphics, final int x,
				final int y, final int width, final int height) {
			drawTop(graphics, x, y, width, height);
		}

		@Override
		protected void drawTop(final Graphics2D graphics, final int x,
				final int y, final int width, final int height) {
			translateX = graphics.getTransform().getTranslateX();
			translateY = graphics.getTransform().getTranslateY();
			interpolationHint = graphics.getRenderingHint(
					RenderingHints.KEY_INTERPOLATION);
		}
	}
}
