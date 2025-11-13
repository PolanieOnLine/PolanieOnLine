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

import { TileStore } from "../data/TileStore";
import { MapOfSets } from "../util/MapOfSets";
import { CombinedTileset } from "./CombinedTileset";
import { CachedTilesetImage, TilesetImageCache } from "./TilesetImageCache";

declare var stendhal: any;

export class CombinedTilesetImageLoader {

	private tileUsedAtIndex!: MapOfSets<number, number>
	private tilesetImages: CanvasImageSource[] = [];
	private tilesetDimensions: Array<{ width: number; height: number }> = [];
	private animations: any = {};
	private landscapeAnimationMap: any;

	constructor(
		private map: any,
		private indexToCombinedTiles: Map<number, number[]>,
		private combinedTileset: CombinedTileset) {
		this.landscapeAnimationMap = TileStore.get().getLandscapeMap();
	}


	private calculateTileUsedAtIndex(): void {
		this.tileUsedAtIndex = new MapOfSets<number, number>();
		for (let [index, combinedTile] of this.indexToCombinedTiles.entries()) {
			for (let tile of combinedTile) {
				let gid = tile & 0x1FFFFFFF;
				this.tileUsedAtIndex.add(gid, index);
			}
		}
	}


	private calculateUsedTilesets(gids: IterableIterator<number>): Set<number> {
		let usedTilesets = new Set<number>();
		for (let gid of gids) {
			usedTilesets.add(this.map.getTilesetForGid(gid));
		}
		return usedTilesets;
	}


	private loadTileset(tileset: number, cache: TilesetImageCache = TilesetImageCache.get()) {
		const tsname = this.map.tilesetFilenames[tileset];
		const url = tsname + "?v=" + stendhal.data.build.version;

		if (this.landscapeAnimationMap) {
			const animation = this.landscapeAnimationMap[tsname];
			if (animation) {
				this.animations[tileset] = animation;
			}
		}

		cache.load(url)
			.then((image) => this.onTilesetLoaded(tileset, image))
			.catch((error) => {
				console.warn(`Failed to load tileset ${url}`, error);
			});
	}


	private onTilesetLoaded(tileset: number, image: CachedTilesetImage): void {
		this.tilesetImages[tileset] = image.source;
		this.tilesetDimensions[tileset] = { width: image.width, height: image.height };
		this.drawTileset(tileset);
	}


	private drawTileset(tileset: number): void {
		let firstGid = this.map.firstgids[tileset];
		let image = this.tilesetImages[tileset];
		let dimensions = this.tilesetDimensions[tileset];
		if (!image || !dimensions) {
			return;
		}
		let tilesPerRow = Math.floor(dimensions.width / this.map.tileWidth);
		let numberOfTiles = tilesPerRow * Math.floor(dimensions.height / this.map.tileHeight);

		let lastGid = firstGid + numberOfTiles;
		for (let gid = firstGid; gid <= lastGid; gid++) {
			let indexes = this.tileUsedAtIndex.get(gid);
			if (indexes === undefined) {
				continue;
			}
			for (let index of indexes) {
				this.drawCombinedTileAtIndex(index);
			}
		}
	}


	private drawCombinedTileAtIndex(index: number) {
		let tiles = this.indexToCombinedTiles.get(index)!;

		let x = index % this.combinedTileset.tilesPerRow;
		let y = Math.floor(index / this.combinedTileset.tilesPerRow);
		const pixelX = x * this.map.tileWidth;
		const pixelY = y * this.map.tileHeight;

		this.combinedTileset.ctx.clearRect(pixelX, pixelY, this.map.tileWidth, this.map.tileHeight);
		for (let tile of tiles) {
			let flip = tile & 0xE0000000;
			let gid = tile & 0x1FFFFFFF;
			let tileset = this.map.getTilesetForGid(gid);
			let image = this.tilesetImages[tileset];
			let dimensions = this.tilesetDimensions[tileset];
			if (!image || !dimensions || !dimensions.height) {
				continue;
			}

			let base = this.map.firstgids[tileset];
			let tileIndexInTileset = gid - base;
			this.drawTile(pixelX, pixelY, image, dimensions.width, tileIndexInTileset, flip);

		}
	}

	private drawTile(pixelX: number, pixelY: number, tilesetImage: CanvasImageSource, tilesetWidth: number, tileIndexInTileset: number, flip: number) {
		const tilesPerRow = Math.floor(tilesetWidth / this.map.tileWidth);

		const ctx = this.combinedTileset.ctx;

		if (flip === 0) {
			ctx.drawImage(tilesetImage,
				(tileIndexInTileset % tilesPerRow) * this.map.tileWidth,
				Math.floor(tileIndexInTileset / tilesPerRow) * this.map.tileHeight,
				this.map.tileWidth, this.map.tileHeight,
				pixelX, pixelY,
				this.map.tileWidth, this.map.tileHeight);

		} else {
			ctx.translate(pixelX, pixelY);
			// an ugly hack to restore the previous transformation matrix
			const restore = [[1, 0, 0, 1, -pixelX, -pixelY]];

			if ((flip & 0x80000000) !== 0) {
				// flip horizontally
				ctx.transform(-1, 0, 0, 1, 0, 0);
				ctx.translate(-this.map.tileWidth, 0);

				restore.push([-1, 0, 0, 1, 0, 0]);
				restore.push([1, 0, 0, 1, this.map.tileWidth, 0]);
			}
			if ((flip & 0x40000000) !== 0) {
				// flip vertically
				ctx.transform(1, 0, 0, -1, 0, 0);
				ctx.translate(0, -this.map.tileWidth);

				restore.push([1, 0, 0, -1, 0, 0]);
				restore.push([1, 0, 0, 1, 0, this.map.tileHeight]);
			}
			if ((flip & 0x20000000) !== 0) {
				// Coordinate swap
				ctx.transform(0, 1, 1, 0, 0, 0);
				restore.push([0, 1, 1, 0, 0, 0]);
			}

			this.drawTile(0, 0, tilesetImage, tilesetWidth, tileIndexInTileset, 0);

			restore.reverse();
			for (const args of restore) {
				ctx.transform.apply(ctx, args as any);
			}
		}
	}


	load() {
		console.log("CombinedTilesetImageLoader.load()");
		this.calculateTileUsedAtIndex();
		let usedTilesets = this.calculateUsedTilesets(this.tileUsedAtIndex.keys());
		const cache = TilesetImageCache.get();
		const urls: string[] = [];
		for (let tileset of usedTilesets) {
			const tsname = this.map.tilesetFilenames[tileset];
			urls.push(tsname + "?v=" + stendhal.data.build.version);
		}
		cache.prefetch(urls);
		for (let tileset of usedTilesets) {
			this.loadTileset(tileset, cache);
		}

	}
}