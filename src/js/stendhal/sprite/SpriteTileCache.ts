/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Sprite } from "./Sprite";

/** Default tile size used for slicing large sprites. */
export const TILE_SIZE = 32;

/** Maximum number of cached tile entries. */
const TILE_CACHE_SIZE = 100;

/**
 * Creates and caches 32x32 tiles from larger sprites using LRU eviction.
 */
class SpriteTileCache {

	private static readonly spriteIds = new WeakMap<Sprite, number>();
	private static nextId = 0;

	private readonly cache = new Map<string, Sprite>();

	constructor(private readonly maxEntries = TILE_CACHE_SIZE) {
		// nothing to do
	}

	public get(sprite: Sprite, x: number, y: number, width: number, height: number): Sprite {
		const key = this.getKey(sprite, x, y, width, height);
		const cached = this.cache.get(key);
		if (cached) {
			// update recency
			this.cache.delete(key);
			this.cache.set(key, cached);
			return cached;
		}

		const reference = { key };
		const tile = sprite.createRegion(x, y, width, height, reference);
		this.cache.set(key, tile);
		this.trim();
		return tile;
	}

	private trim(): void {
		while (this.cache.size > this.maxEntries) {
			const oldest = this.cache.keys().next().value;
			if (!oldest) {
				break;
			}
			this.cache.delete(oldest);
		}
	}

	private getKey(sprite: Sprite, x: number, y: number, width: number, height: number): string {
		const spriteId = this.getSpriteId(sprite);
		return `${spriteId}:${x},${y},${width}x${height}`;
	}

	private getSpriteId(sprite: Sprite): number {
		let id = SpriteTileCache.spriteIds.get(sprite);
		if (!id) {
			id = ++SpriteTileCache.nextId;
			SpriteTileCache.spriteIds.set(sprite, id);
		}
		return id;
	}
}

const sharedCache = new SpriteTileCache();

/**
 * Draws a sub-region of a sprite using cached 32x32 tiles.
 *
 * @param ctx
 *   Canvas context to draw to.
 * @param sprite
 *   Sprite containing the source image data.
 * @param srcX
 *   X coordinate of the source region.
 * @param srcY
 *   Y coordinate of the source region.
 * @param width
 *   Width of the source region.
 * @param height
 *   Height of the source region.
 * @param destX
 *   X coordinate where the image should be drawn.
 * @param destY
 *   Y coordinate where the image should be drawn.
 */
export function drawTiledRegion(ctx: CanvasRenderingContext2D, sprite: Sprite, srcX: number,
	srcY: number, width: number, height: number, destX: number, destY: number): void {
	for (let y = 0; y < height; y += TILE_SIZE) {
		const tileHeight = Math.min(TILE_SIZE, height - y);
		for (let x = 0; x < width; x += TILE_SIZE) {
			const tileWidth = Math.min(TILE_SIZE, width - x);
			const tile = sharedCache.get(sprite, srcX + x, srcY + y, tileWidth, tileHeight);
			tile.draw(ctx, destX + x, destY + y);
		}
	}
}
