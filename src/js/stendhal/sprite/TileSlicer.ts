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

/**
 * Splits a sprite into a two-dimensional array of equally sized tiles.
 *
 * @param sprite
 *   Source sprite to slice.
 * @param tileWidth
 *   Width of each tile.
 * @param tileHeight
 *   Height of each tile.
 * @return
 *   2D array of sprites where the first index represents the row (Y axis)
 *   and the second index represents the column (X axis).
 */
export function sliceIntoTiles(sprite: Sprite, tileWidth: number, tileHeight: number): Sprite[][] {
	const tiles: Sprite[][] = [];
	for (let y = 0; y < sprite.getHeight(); y += tileHeight) {
		const row: Sprite[] = [];
		const sliceHeight = Math.min(tileHeight, sprite.getHeight() - y);
		for (let x = 0; x < sprite.getWidth(); x += tileWidth) {
			const sliceWidth = Math.min(tileWidth, sprite.getWidth() - x);
			row.push(sprite.createRegion(x, y, sliceWidth, sliceHeight, { x, y }));
		}
		tiles.push(row);
	}
	return tiles;
}

export default sliceIntoTiles;
