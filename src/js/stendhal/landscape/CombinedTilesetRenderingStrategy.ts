/***************************************************************************
 *                (C) Copyright 2022-2026 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { LandscapeRenderingStrategy } from "./LandscapeRenderingStrategy";
import { Canvas } from "util/Types";
import { TileMap } from "../data/TileMap";
import { CombinedTilesetFactory } from "./CombinedTilesetFactory";
import { CombinedTileset } from "./CombinedTileset";
import { BASE_TILE_EDGE_TRIM, getTileOverlapMetrics, resolveTileScale } from "./TileOverlap";

export class CombinedTilesetRenderingStrategy extends LandscapeRenderingStrategy {
	private combinedTileset?: CombinedTileset;
	private map!: TileMap;

	public onMapLoaded(map: TileMap): void {
		this.map = map;
		let combinedTilesetFactory = new CombinedTilesetFactory(map);
		this.combinedTileset = combinedTilesetFactory.combine();
	}

	public onTilesetLoaded(): void {
		let body = document.getElementById("body")!;
		body.style.cursor = "auto";
	}

	public render(
		canvas: Canvas, gamewindow: any,
		tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {

		this.drawLayer(
			canvas,
			this.combinedTileset,
			0,
			tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight);

		gamewindow.drawEntities();

		this.drawLayer(
			canvas,
			this.combinedTileset,
			1,
			tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight);
	}

	drawLayer(
			canvas: Canvas,
			combinedTileset: CombinedTileset|undefined, layerNo: number,
			tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {
		if (!combinedTileset) {
			return;
		}
		let ctx = canvas.getContext("2d")!;
		ctx.imageSmoothingEnabled = false;

		const layer = combinedTileset.combinedLayers[layerNo];
		const yStart = Math.max(0, tileOffsetY);
		const xStart = Math.max(0, tileOffsetX);
		const yMax = Math.min(tileOffsetY + canvas.height / targetTileHeight + 1, this.map.zoneSizeY);
		const xMax = Math.min(tileOffsetX + canvas.width / targetTileWidth + 1, this.map.zoneSizeX);
		const tileScale = resolveTileScale(targetTileWidth / this.map.tileWidth);
		const pixelRatio = typeof window.devicePixelRatio === "number" ? window.devicePixelRatio || 1 : 1;
		const metrics = getTileOverlapMetrics(
			tileScale,
			BASE_TILE_EDGE_TRIM,
			pixelRatio,
			this.map.tileWidth
		);
		const sourceWidth = this.map.tileWidth - metrics.edgeTrim * 2;
		const sourceHeight = this.map.tileHeight - metrics.edgeTrim * 2;
		const drawTileWidth = targetTileWidth + metrics.tileOverlap;
		const drawTileHeight = targetTileHeight + metrics.tileOverlap;

		for (let y = yStart; y < yMax; y++) {
			for (let x = xStart; x < xMax; x++) {
				let index = layer[y * this.map.zoneSizeX + x];
				if (index > -1) {

					try {
						const pixelX = x * targetTileWidth - metrics.overlapOffset;
						const pixelY = y * targetTileHeight - metrics.overlapOffset;

						ctx.drawImage(combinedTileset.canvas,

							(index % combinedTileset.tilesPerRow) * this.map.tileWidth + metrics.edgeTrim,
							Math.floor(index / combinedTileset.tilesPerRow) * this.map.tileHeight + metrics.edgeTrim,

							sourceWidth, sourceHeight,
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
