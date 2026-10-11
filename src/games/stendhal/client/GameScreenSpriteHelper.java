/***************************************************************************
 *                   (C) Copyright 2003-2023 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import games.stendhal.client.entity.IEntity;

import games.stendhal.client.gui.j2d.RemovableSprite;
import games.stendhal.client.sprite.Sprite;

/**
 * Some GameScreen methods that can be accessed by individual sprites.
 */
public class GameScreenSpriteHelper {
	/** Singleton instance. */
	private static GameScreenSpriteHelper instance;

	private static int ww;
	private static int wh;

	private static int svx;
	private static int frameViewX;
	private static int frameViewY;
	private static boolean frameActive;
	private static int svy;
	private static volatile PixelArtViewport renderedViewport;
	private static Map<IEntity, EntityRenderPosition> renderedPositions = Collections.emptyMap();

	private static double scale = 1.0;

	/**
	 * The text bubbles.
	 */
	private static List<RemovableSprite> texts;

	private static List<RemovableSprite> emojis;

	/**
	 * Retrieves the singleton instance.
	 */
	public static GameScreenSpriteHelper get() {
		if (instance == null) {
			instance = new GameScreenSpriteHelper();
		}

		return instance;
	}

	/**
	 * Private singleton constructor.
	 */
	private GameScreenSpriteHelper() {}

	static void beginFrame(final PixelArtViewport viewport) {
		beginFrame(viewport, Collections.<IEntity, EntityRenderPosition>emptyMap());
	}

	static void beginFrame(final PixelArtViewport viewport,
			final Map<IEntity, EntityRenderPosition> positions) {
		renderedPositions = positions;
		renderedViewport = viewport;
		frameViewX = (int) Math.round(viewport.getCameraX());
		frameViewY = (int) Math.round(viewport.getCameraY());
		frameActive = true;
	}

	public static Graphics2D createEntityGraphics(final Graphics2D source, final IEntity entity,
			final int anchorX, final int anchorY, final double offsetX, final double offsetY) {
		final PixelArtViewport viewport = renderedViewport;
		if (frameActive && viewport != null && entity.isUser()) {
			return viewport.createTrackedSpriteGraphics(source, anchorX, anchorY, offsetX, offsetY);
		}
		return PixelArtViewport.createSpriteGraphics(source, anchorX, anchorY, offsetX, offsetY);
	}

	/** Attach bubbles to the body from the displayed frame, never the live model. */
	public static int getEntityScreenX(final IEntity entity, final boolean rightEdge) {
		final EntityRenderPosition position = getEntityPosition(entity);
		final double extra = rightEdge ? position.getWidth() * IGameScreen.SIZE_UNIT_PIXELS * scale : 0.0;
		return (int) Math.round(getEntityScreenAnchor(entity, position).getX() + extra) + getActiveViewX();
	}

	public static int getEntityScreenY(final IEntity entity) {
		return (int) Math.round(getEntityScreenAnchor(entity, getEntityPosition(entity)).getY()) + getActiveViewY();
	}

	private static EntityRenderPosition getEntityPosition(final IEntity entity) {
		final EntityRenderPosition cached = renderedViewport == null ? null : renderedPositions.get(entity);
		return cached != null ? cached : new EntityRenderPosition(
				entity.getX() * IGameScreen.SIZE_UNIT_PIXELS,
				entity.getY() * IGameScreen.SIZE_UNIT_PIXELS, entity.getWidth());
	}

	private static Point2D getEntityScreenAnchor(final IEntity entity, final EntityRenderPosition position) {
		final PixelArtViewport viewport = renderedViewport;
		return viewport != null ? viewport.entityToScreen(position, entity.isUser())
				: new Point2D.Double((position.getPixelX() - getActiveViewX()) * scale,
						(position.getPixelY() - getActiveViewY()) * scale);
	}

	static void endFrame() {
		frameActive = false;
	}

	private static int getActiveViewX() {
		return getScreenViewX();
	}

	private static int getActiveViewY() {
		return getScreenViewY();
	}

	static PixelArtViewport getRenderedViewport() {
		return renderedViewport;
	}

	/**
	 * Sets screen scale.
	 *
	 * @param newScale
	 *     Scaling to be applied.
	 */
	static void setScale(final double newScale) {
		if (Math.abs(scale - newScale) > 0.000001) {
			renderedViewport = null;
			renderedPositions = Collections.emptyMap();
		}
		scale = newScale;
	}

	/**
	 * Retrieves screen scale.
	 */
	static double getScale() {
		return scale;
	}

	/**
	 * Sets width of world in world units.
	 *
	 * @param width
	 *     New width.
	 */
	static void setWorldWidth(final int width) {
		ww = width;
		renderedViewport = null;
		renderedPositions = Collections.emptyMap();
	}

	/**
	 * Get the width of the world in world units.
	 */
	static int getWorldWidth() {
		return ww;
	}

	/**
	 * Sets height of world in world units.
	 *
	 * @param height
	 *     New height.
	 */
	static void setWorldHeight(final int height) {
		wh = height;
		renderedViewport = null;
		renderedPositions = Collections.emptyMap();
	}

	/**
	 * Gets the height of the world in world units.
	 */
	static int getWorldHeight() {
		return wh;
	}

	/**
	 * Sets the view X screen coordinate.
	 *
	 * @param x
	 *     New X coordinate.
	 */
	static void setScreenViewX(final int x) {
		svx = x;
	}

	/**
	 * Gets the view X screen coordinate.
	 *
	 * @return
	 *     The X coordinate of the left side.
	 */
	public static int getScreenViewX() {
		final PixelArtViewport viewport = renderedViewport;
		return frameActive ? frameViewX
				: viewport == null ? svx : (int) Math.round(viewport.getCameraX());
	}

	/**
	 * Sets the view Y screen coordinate.
	 *
	 * @param y
	 *     New Y coordinate.
	 */
	static void setScreenViewY(final int y) {
		svy = y;
	}

	/**
	 * Gets the view Y screen coordinate.
	 *
	 * @return
	 *     The Y coordinate of the left side.
	 */
	public static int getScreenViewY() {
		final PixelArtViewport viewport = renderedViewport;
		return frameActive ? frameViewY
				: viewport == null ? svy : (int) Math.round(viewport.getCameraY());
	}

	/**
	 * Sets the text bubbles to display on screen.
	 *
	 * @param t
	 *     All bubbles.
	 */
	static void setTexts(final List<RemovableSprite> t) {
		texts = t;
	}

	/**
	 * Retrieves list of text bubbles displayed on screen.
	 */
	static List<RemovableSprite> getTexts() {
		return texts;
	}

	/**
	 * Adds a text bubble to display on screen.
	 *
	 * @param sprite
	 *     Text sprite to add.
	 */
	static void addText(final RemovableSprite sprite) {
		texts.add(sprite);
	}

	/**
	 * Removes a text bubble from the display.
	 *
	 * @param sprite
	 *     Text sprite to remove.
	 */
	static void removeText(final RemovableSprite sprite) {
		texts.remove(sprite);
	}

	/**
	 * Removes all text bubbles from the display.
	 */
	static void clearTexts() {
		texts.clear();
	}

	static void setEmojis(final List<RemovableSprite> e) {
		emojis = e;
	}

	static List<RemovableSprite> getEmojis() {
		return emojis;
	}

	static void addEmoji(final RemovableSprite emoji) {
		emojis.add(emoji);
	}

	static void removeEmoji(final RemovableSprite emoji) {
		emojis.remove(emoji);
	}

	static void clearEmojis() {
		emojis.clear();
	}

	/**
	 * Convert a world unit value to pixel units.
	 *
	 * @param w
	 *     World value.
	 * @return
	 *     A screen value (in pixels).
	 */
	public static int convertWorldToPixelUnits(final double w) {
		return (int) (w * IGameScreen.SIZE_UNIT_PIXELS);
	}

	/**
	 * Convert a world x coordinate to <em>raw</em> (native resolution)
	 * screen x coordinate.
	 *
	 * @param x
	 *     World X coordinate.
	 * @return
	 *     Pixel X coordinate on the screen.
	 */
	public static int convertWorldXToScaledScreen(final double x) {
		final int baseViewX = getActiveViewX();
		final PixelArtViewport viewport = renderedViewport;
		final double nativeX = x * IGameScreen.SIZE_UNIT_PIXELS;
		final double screenX = viewport == null ? (nativeX - baseViewX) * scale
				: viewport.worldToScreenX(nativeX);
		return (int) Math.round(screenX) + baseViewX;
	}

	/**
	 * Convert a world y coordinate to <em>raw</em> (native resolution)
	 * screen y coordinate.
	 *
	 * @param y
	 *     World Y coordinate.
	 * @return
	 *     Pixel Y coordinate on the screen.
	 */
	public static int convertWorldYToScaledScreen(final double y) {
		final int baseViewY = getActiveViewY();
		final PixelArtViewport viewport = renderedViewport;
		final double nativeY = y * IGameScreen.SIZE_UNIT_PIXELS;
		final double screenY = viewport == null ? (nativeY - baseViewY) * scale
				: viewport.worldToScreenY(nativeY);
		return (int) Math.round(screenY) + baseViewY;
	}

	/**
	 * Try to keep a sprite on the map. Adjust the X coordinate.
	 *
	 * @param sprite
	 *     Sprite to keep on the map.
	 * @param sx
	 *     Suggested X coordinate on screen.
	 * @return
	 *     New X coordinate.
	 */
	public static int keepSpriteOnMapX(final Sprite sprite, int sx) {
		sx = Math.max(sx, 0);

		/*
		 * Allow placing beyond the map, but only if the area is on the screen.
		 * Do not try to adjust the coordinates if the world size is not known
		 * yet (as in immediately after a zone change)
		 */
		if (ww != 0) {
			sx = Math.min(sx, Math.max(GameScreen.get().getWidth() + getActiveViewX(),
				convertWorldXToScaledScreen(ww)) - sprite.getWidth());
		}

		return sx;
	}

	/**
	 * Try to keep a sprite on the map. Adjust the Y coordinate.
	 *
	 * @param sprite
	 *     Sprite to keep on the map.
	 * @param sy
	 *     Suggested Y coordinate on screen.
	 * @return
	 *     New Y coordinate.
	 */
	public static int keepSpriteOnMapY(final Sprite sprite, int sy) {
		sy = Math.max(sy, 0);

		/*
		 * Allow placing beyond the map, but only if the area is on the screen.
		 * Do not try to adjust the coordinates if the world size is not known
		 * yet (as in immediately after a zone change)
		 */
		if (wh != 0) {
			sy = Math.min(sy, Math.max(GameScreen.get().getHeight() + getActiveViewY(),
				convertWorldYToScaledScreen(wh)) - sprite.getHeight());
		}

		return sy;
	}

	/**
	 * Adjust the position of boxes placed at the same point to make it
	 * clear for the player there are more than one.
	 *
	 * @param sprite
	 * @param x
	 * @param sy
	 */
	public static int findFreeTextBoxPosition(final Sprite sprite, final int x, int sy) {
		boolean found = true;
		int tries = 0;

		while (found) {
			found = false;

			synchronized (texts) {
				for (final RemovableSprite item : texts) {
					if ((item.getX() == x) && (item.getY() == sy)) {
						found = true;
						sy += IGameScreen.SIZE_UNIT_PIXELS / 2;
						sy = keepSpriteOnMapY(sprite, sy);
						break;
					}
				}
			}

			tries++;
			// give up, if no location found in a reasonable amount of tries
			if (tries > 20) {
				break;
			}
		}
		return sy;
	}
}
