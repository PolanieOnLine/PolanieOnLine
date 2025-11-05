/***************************************************************************
 *                (C) Copyright 2022-2023 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { CombinedTileset } from "./CombinedTileset";

declare var stendhal: any;

// Trim a fraction of a texel from each side so bilinear filtering never pulls
// colour from neighbouring tiles when the canvas is translated sub-pixel.
const TILE_EDGE_TRIM = 0.02;

export class LandscapeRenderer {

	drawLayer(
			canvas: HTMLCanvasElement,
			combinedTileset: CombinedTileset, layerNo: number,
			tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {
		if (!combinedTileset) {
			return;
		}
		let ctx = canvas.getContext("2d")!;

		const layer = combinedTileset.combinedLayers[layerNo];
		const yMax = Math.min(tileOffsetY + canvas.height / targetTileHeight + 1, stendhal.data.map.zoneSizeY);
		const xMax = Math.min(tileOffsetX + canvas.width / targetTileWidth + 1, stendhal.data.map.zoneSizeX);

		for (let y = tileOffsetY; y < yMax; y++) {
			for (let x = tileOffsetX; x < xMax; x++) {
				let index = layer[y * stendhal.data.map.zoneSizeX + x];
				if (index > -1) {

					try {
						const pixelX = x * targetTileWidth;
						const pixelY = y * targetTileHeight;
						const tilesPerRow = combinedTileset.tilesPerRow;
						const sourceX = (index % tilesPerRow) * stendhal.data.map.tileWidth + TILE_EDGE_TRIM;
						const sourceY = Math.floor(index / tilesPerRow) * stendhal.data.map.tileHeight + TILE_EDGE_TRIM;
						const sourceWidth = stendhal.data.map.tileWidth - TILE_EDGE_TRIM * 2;
						const sourceHeight = stendhal.data.map.tileHeight - TILE_EDGE_TRIM * 2;

						ctx.drawImage(combinedTileset.canvas,

							sourceX,
							sourceY,
							sourceWidth, sourceHeight,
							pixelX, pixelY,
							targetTileWidth, targetTileHeight);
					} catch (e) {
						console.error(e);
					}
				}
			}
		}
	}
}
