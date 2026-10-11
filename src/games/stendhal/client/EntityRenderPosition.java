/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

/** Immutable native-pixel position captured for one displayed entity frame. */
public final class EntityRenderPosition {
	private final double pixelX;
	private final double pixelY;
	private final double width;

	public EntityRenderPosition(final double pixelX, final double pixelY, final double width) {
		this.pixelX = pixelX;
		this.pixelY = pixelY;
		this.width = width;
	}

	public double getPixelX() { return pixelX; }
	public double getPixelY() { return pixelY; }
	public double getWidth() { return width; }
}
