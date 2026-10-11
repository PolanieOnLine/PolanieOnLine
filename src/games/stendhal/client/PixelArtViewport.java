/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * One camera transform for an entire frame, including scaled and HiDPI views.
 * Scale the world first and round its translation on the final device grid.
 * Rounding in a small intermediate image changes the sampling phase of map
 * pixels during scrolling, even when nearest-neighbour interpolation is used.
 */
public final class PixelArtViewport {

	private final AffineTransform worldTransform;
	private final AffineTransform screenTransform;
	private final AffineTransform inverseScreenTransform;
	private final double scale;

	public PixelArtViewport(final Graphics2D destination, final double scale,
			final double cameraX, final double cameraY) {
		if (!Double.isFinite(scale) || scale <= 0.0
				|| !Double.isFinite(cameraX) || !Double.isFinite(cameraY)) {
			throw new IllegalArgumentException("Viewport scale and camera must be finite; scale must be positive");
		}
		this.scale = scale;
		final AffineTransform destinationTransform = destination.getTransform();
		worldTransform = new AffineTransform(destinationTransform);
		worldTransform.scale(scale, scale);
		worldTransform.translate(-cameraX, -cameraY);
		alignOrigin(worldTransform, 0.0, 0.0);
		try {
			screenTransform = destinationTransform.createInverse();
			screenTransform.concatenate(worldTransform);
			inverseScreenTransform = screenTransform.createInverse();
		} catch (final NoninvertibleTransformException e) {
			throw new IllegalArgumentException("Viewport destination must have an invertible transform", e);
		}
	}

	/** Caller owns the returned graphics; the destination is never modified. */
	public Graphics2D createWorldGraphics(final Graphics2D destination) {
		final Graphics2D graphics = (Graphics2D) destination.create();
		graphics.setTransform(worldTransform);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		return graphics;
	}

	/**
	 * Keep a sprite's sampling phase constant while its position changes. Both
	 * the entity and its name/health bar use the same origin and rounding rule.
	 * Contained inventory sprites are intentionally handled by their own caller.
	 */
	public static Graphics2D createSpriteGraphics(final Graphics2D source,
			final int anchorX, final int anchorY, final double offsetX, final double offsetY) {
		final Graphics2D graphics = (Graphics2D) source.create();
		graphics.translate(offsetX, offsetY);
		final AffineTransform transform = graphics.getTransform();
		alignOrigin(transform, anchorX, anchorY);
		graphics.setTransform(transform);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		return graphics;
	}

	private static void alignOrigin(final AffineTransform transform,
			final double x, final double y) {
		final Point2D origin = transform.transform(new Point2D.Double(x, y), null);
		transform.setTransform(transform.getScaleX(), transform.getShearY(),
				transform.getShearX(), transform.getScaleY(),
				Math.round(origin.getX()) - (transform.getScaleX() * x + transform.getShearX() * y),
				Math.round(origin.getY()) - (transform.getShearY() * x + transform.getScaleY() * y));
	}

	public double getCameraX() {
		return -screenTransform.getTranslateX() / scale;
	}

	public double getCameraY() {
		return -screenTransform.getTranslateY() / scale;
	}

	public double worldToScreenX(final double nativeX) {
		return nativeX * scale + screenTransform.getTranslateX();
	}

	public double worldToScreenY(final double nativeY) {
		return nativeY * scale + screenTransform.getTranslateY();
	}

	/** Inverse of the displayed frame, rather than the next logical camera update. */
	public Point2D screenToWorld(final double x, final double y) {
		return inverseScreenTransform.transform(new Point2D.Double(x, y), null);
	}
}
