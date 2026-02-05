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

import { Canvas, RenderingContext2D } from "util/Types";
import { CombinedTileset } from "./CombinedTileset";
import { BASE_TILE_EDGE_TRIM, getTileOverlapMetrics, resolveTileScale } from "./TileOverlap";

import { stendhal } from "../stendhal";

function resolvePixelRatio(): number {
	if (typeof window !== "undefined" && typeof window.devicePixelRatio === "number") {
		return window.devicePixelRatio || 1;
	}
	return 1;
}

function snapToPixel(value: number, effectiveScale: number): number {
	if (!effectiveScale) {
		return value;
	}
	return Math.round(value * effectiveScale) / effectiveScale;
}

export class LandscapeRenderer {

	drawLayer(
		canvas: Canvas,
		combinedTileset: CombinedTileset, layerNo: number,
		tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {
		if (!combinedTileset) {
			return;
		}
		let ctx = canvas.getContext("2d")! as RenderingContext2D;
		ctx.imageSmoothingEnabled = false;
		ctx.imageSmoothingQuality = "low";
		const renderScale = typeof (stendhal.ui?.gamewindow?.getTileScale) === "function"
			? stendhal.ui.gamewindow.getTileScale()
			: 1;
		const clampedScale = resolveTileScale(renderScale);
		const viewportSize = typeof (stendhal.ui?.gamewindow?.getWorldViewportSize) === "function"
			? stendhal.ui.gamewindow.getWorldViewportSize()
			: {
				width: canvas.width / clampedScale,
				height: canvas.height / clampedScale,
			};

		const layer = combinedTileset.combinedLayers[layerNo];
		const yMax = Math.min(tileOffsetY + viewportSize.height / targetTileHeight + 1, stendhal.data.map.zoneSizeY);
		const xMax = Math.min(tileOffsetX + viewportSize.width / targetTileWidth + 1, stendhal.data.map.zoneSizeX);
		const pixelRatio = resolvePixelRatio();
		const { tileOverlap, overlapOffset } = getTileOverlapMetrics(
			clampedScale,
			BASE_TILE_EDGE_TRIM,
			pixelRatio,
			stendhal.data.map.tileWidth
		);
		const effectiveScale = clampedScale * pixelRatio;
		const drawTileWidth = snapToPixel(targetTileWidth + tileOverlap, effectiveScale);
		const drawTileHeight = snapToPixel(targetTileHeight + tileOverlap, effectiveScale);

		for (let y = tileOffsetY; y < yMax; y++) {
			for (let x = tileOffsetX; x < xMax; x++) {
				let index = layer[y * stendhal.data.map.zoneSizeX + x];
				if (index > -1) {

					try {
						const pixelX = snapToPixel(x * targetTileWidth - overlapOffset, effectiveScale);
						const pixelY = snapToPixel(y * targetTileHeight - overlapOffset, effectiveScale);

						ctx.drawImage(combinedTileset.canvas,

							(index % combinedTileset.tilesPerRow) * stendhal.data.map.tileWidth,
							Math.floor(index / combinedTileset.tilesPerRow) * stendhal.data.map.tileHeight,

							stendhal.data.map.tileWidth, stendhal.data.map.tileHeight,
							pixelX, pixelY,
							drawTileWidth, drawTileHeight);
					} catch (e) {
						console.error(e);
					}
				}
			}
		}
	}

}
