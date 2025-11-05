/***************************************************************************
 *                (C) Copyright 2022-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;

import { LandscapeRenderingStrategy } from "./LandscapeRenderingStrategy";
import { ImagePreloader } from "../data/ImagePreloader";
import { Chat } from "../util/Chat";


// Trim a fraction of a texel from each side so bilinear filtering never pulls
// colour from neighbouring tiles when the canvas is translated sub-pixel.
const TILE_EDGE_TRIM = 0.02;

export class IndividualTilesetRenderingStrategy extends LandscapeRenderingStrategy {

	private targetTileWidth = 32;
	private targetTileHeight = 32;

	constructor() {
		super();
		window.setTimeout(() => {
			Chat.log("client", "Using IndividualTilesetRenderingStrategy");
		}, 1000);
	}

	public onMapLoaded(_map: any): void {
		// do nothing
		console.log("Using IndividualTilesetRenderingStrategy.")
	}

	public onTilesetLoaded(): void {
		new ImagePreloader(stendhal.data.map.tilesetFilenames, function() {
			let body = document.getElementById("body")!;
			body.style.cursor = "auto";
		});
	}

	public render(
		canvas: HTMLCanvasElement, gamewindow: any,
		tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number,
		alpha: number): void {

		this.targetTileWidth = targetTileWidth;
		this.targetTileHeight = targetTileHeight;

		for (var drawingLayer=0; drawingLayer < stendhal.data.map.layers.length; drawingLayer++) {
			var name = stendhal.data.map.layerNames[drawingLayer];
			if (name !== "protection" && name !== "collision" && name !== "objects"
				&& name !== "blend_ground" && name !== "blend_roof") {
				this.paintLayer(canvas, drawingLayer, tileOffsetX, tileOffsetY);
			}
			if (name === "2_object") {
				gamewindow.drawEntities(alpha);
			}
		}
	}

	private paintLayer(canvas: HTMLCanvasElement, drawingLayer: number,
		tileOffsetX: number, tileOffsetY: number) {
		const layer = stendhal.data.map.layers[drawingLayer];
		const yMax = Math.min(tileOffsetY + canvas.height / this.targetTileHeight + 1, stendhal.data.map.zoneSizeY);
		const xMax = Math.min(tileOffsetX + canvas.width / this.targetTileWidth + 1, stendhal.data.map.zoneSizeX);
		let ctx = canvas.getContext("2d")!;

		for (let y = tileOffsetY; y < yMax; y++) {
			for (let x = tileOffsetX; x < xMax; x++) {
				let gid = layer[y * stendhal.data.map.zoneSizeX + x];
				const flip = gid & 0xE0000000;
				gid &= 0x1FFFFFFF;

				if (gid > 0) {
					const tileset = stendhal.data.map.getTilesetForGid(gid);
					const base = stendhal.data.map.firstgids[tileset];
					const idx = gid - base;

					try {
						if (stendhal.data.map.aImages[tileset].height > 0) {
							const screenX = x * this.targetTileWidth;
							const screenY = y * this.targetTileHeight;
							this.drawTile(ctx, stendhal.data.map.aImages[tileset], idx, screenX, screenY, flip);
						}
					} catch (e) {
						console.error(e);
					}
				}
			}
		}
	}

	private drawTile(ctx: CanvasRenderingContext2D, tileset: HTMLImageElement, idx: number,
		screenX: number, screenY: number, flip = 0) {
		const tilesetWidth = tileset.width;
		const tilesPerRow = Math.floor(tilesetWidth / stendhal.data.map.tileWidth);
		const destX = screenX;
		const destY = screenY;
		const destWidth = this.targetTileWidth;
		const destHeight = this.targetTileHeight;
		const sourceX = (idx % tilesPerRow) * stendhal.data.map.tileWidth + TILE_EDGE_TRIM;
		const sourceY = Math.floor(idx / tilesPerRow) * stendhal.data.map.tileHeight + TILE_EDGE_TRIM;
		const sourceWidth = stendhal.data.map.tileWidth - TILE_EDGE_TRIM * 2;
		const sourceHeight = stendhal.data.map.tileHeight - TILE_EDGE_TRIM * 2;

		if (flip === 0) {
			ctx.drawImage(tileset,
					sourceX,
					sourceY,
					sourceWidth, sourceHeight,
					destX, destY,
					destWidth, destHeight);
		} else {
			ctx.save();
			ctx.translate(screenX, screenY);

			if ((flip & 0x80000000) !== 0) {
				ctx.scale(-1, 1);
				ctx.translate(-this.targetTileWidth, 0);
			}
			if ((flip & 0x40000000) !== 0) {
				ctx.scale(1, -1);
				ctx.translate(0, -this.targetTileHeight);
			}
			if ((flip & 0x20000000) !== 0) {
				ctx.transform(0, 1, 1, 0, 0, 0);
			}

			ctx.drawImage(tileset,
					sourceX,
					sourceY,
					sourceWidth, sourceHeight,
					0, 0,
					destWidth, destHeight);

			ctx.restore();
		}
	}
}
