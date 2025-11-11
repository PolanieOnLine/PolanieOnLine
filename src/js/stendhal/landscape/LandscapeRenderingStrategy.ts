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

import { CombinedTilesetFactory } from "./CombinedTilesetFactory";
import { LandscapeRenderer } from "./LandscapeRenderer";
import { drawLayerByName } from "./TileLayerPainter";

declare var stendhal: any;

export abstract class LandscapeRenderingStrategy {

	public abstract onMapLoaded(map: any): void;

	public abstract onTilesetLoaded(): void;

	public abstract render(
		canvas: HTMLCanvasElement, gamewindow: any,
		tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number,
		alpha: number): void;

}

export class CombinedTilesetRenderingStrategy extends LandscapeRenderingStrategy {

	public onMapLoaded(map: any): void {
		let combinedTilesetFactory = new CombinedTilesetFactory(map);
		stendhal.data.map.combinedTileset = combinedTilesetFactory.combine();
	}

	public onTilesetLoaded(): void {
		let body = document.getElementById("body")!;
		body.style.cursor = "auto";
	}

	public render(
			canvas: HTMLCanvasElement, gamewindow: any,
			tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number,
			alpha: number): void {

		let landscapeRenderder = new LandscapeRenderer();
		const ctx = canvas.getContext("2d")!;
		const composite = typeof(gamewindow.getBlendCompositeOperation) === "function"
				? gamewindow.getBlendCompositeOperation()
				: undefined;
		const blendOptions = composite ? {composite} : undefined;

		landscapeRenderder.drawLayer(
			canvas,
			stendhal.data.map.combinedTileset,
			0,
			tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight);

		drawLayerByName(ctx, "blend_ground", tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight, blendOptions);

		gamewindow.drawEntities(alpha);

		landscapeRenderder.drawLayer(
			canvas,
			stendhal.data.map.combinedTileset,
			1,
			tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight);

		drawLayerByName(ctx, "blend_roof", tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight, blendOptions);
	}

}
