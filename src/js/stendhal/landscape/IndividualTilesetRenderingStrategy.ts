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
import { drawLayerByName } from "./TileLayerPainter";

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

		const ctx = canvas.getContext("2d")!;
		const groundLayers = ["0_floor", "1_terrain", "2_object"];
		for (const name of groundLayers) {
			drawLayerByName(ctx, name, tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight);
			if (name === "2_object") {
				gamewindow.drawEntities(alpha);
			}
		}

		const composite = typeof(gamewindow.getBlendCompositeOperation) === "function"
				? gamewindow.getBlendCompositeOperation()
				: undefined;
		const blendOptions = composite ? {composite} : undefined;
		drawLayerByName(ctx, "blend_ground", tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight, blendOptions);

		const roofLayers = ["3_roof", "4_roof_add"];
		for (const name of roofLayers) {
			drawLayerByName(ctx, name, tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight);
		}
		drawLayerByName(ctx, "blend_roof", tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight, blendOptions);
	}

}
