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
declare var marauroa: any;

import { LandscapeRenderingStrategy } from "./LandscapeRenderingStrategy";
import { ImagePreloader } from "../data/ImagePreloader";
import { Chat } from "../util/Chat";
import { drawLayerByName } from "./TileLayerPainter";

const TILE_EDGE_TRIM = 0.02;

export class IndividualTilesetRenderingStrategy extends LandscapeRenderingStrategy {

	private targetTileWidth = 32;
	private targetTileHeight = 32;
	private readonly lastEntityPositions: WeakMap<any, { x: number; y: number; width: number; height: number }> = new WeakMap();
	private previousFrame?: HTMLCanvasElement;
	private pendingRects: { x: number; y: number; width: number; height: number }[] = [];
	private initialRender = true;
	private readonly mobileTileSize?: number;

	constructor() {
		super();
		if (this.isMobileMode()) {
			this.targetTileWidth = 24;
			this.targetTileHeight = 24;
			this.mobileTileSize = 24;
		}
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

		this.targetTileWidth = this.mobileTileSize ?? targetTileWidth;
		this.targetTileHeight = this.mobileTileSize ?? targetTileHeight;

		this.ensureBackBuffer(canvas);
		const ctx = canvas.getContext("2d")!;

		if (this.previousFrame) {
			ctx.drawImage(this.previousFrame, 0, 0);
		} else {
			ctx.clearRect(0, 0, canvas.width, canvas.height);
		}

		this.collectDirtyRectangles(canvas, tileOffsetX, tileOffsetY);
		if (this.pendingRects.length === 0) {
			this.copyToBackBuffer(canvas);
			return;
		}

		for (const rect of this.pendingRects) {
			ctx.clearRect(rect.x, rect.y, rect.width, rect.height);
		}

		ctx.save();
		ctx.beginPath();
		for (const rect of this.pendingRects) {
			ctx.rect(rect.x, rect.y, rect.width, rect.height);
		}
		ctx.clip();

		const groundLayers = ["0_floor", "1_terrain", "2_object"];
		for (const name of groundLayers) {
			this.drawLayerClipped(ctx, name, tileOffsetX, tileOffsetY);
			if (name === "2_object") {
				gamewindow.drawEntities(alpha);
			}
		}

		const composite = typeof (gamewindow.getBlendCompositeOperation) === "function"
			? gamewindow.getBlendCompositeOperation()
			: undefined;
		const blendOptions = composite ? { composite } : undefined;
		drawLayerByName(ctx, "blend_ground", tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight, blendOptions);

		const roofLayers = ["3_roof", "4_roof_add"];
		for (const name of roofLayers) {
			this.drawLayerClipped(ctx, name, tileOffsetX, tileOffsetY);
		}
		drawLayerByName(ctx, "blend_roof", tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight, blendOptions);

		ctx.restore();
		this.copyToBackBuffer(canvas);
		this.pendingRects = [];
	}

	private collectDirtyRectangles(canvas: HTMLCanvasElement, tileOffsetX: number, tileOffsetY: number) {
		const rects: { x: number; y: number; width: number; height: number }[] = [];
		if (this.initialRender) {
			rects.push({ x: 0, y: 0, width: canvas.width, height: canvas.height });
			this.initialRender = false;
		}

		const playerRect = this.getPlayerRect(tileOffsetX, tileOffsetY);
		if (playerRect) {
			rects.push(playerRect);
		}

		const entities = (stendhal.zone && Array.isArray(stendhal.zone.entities)) ? stendhal.zone.entities : [];
		for (const entity of entities) {
			const bounds = this.resolveEntityBounds(entity);
			if (!bounds) {
				continue;
			}
			const currentRect = this.toScreenRect(bounds, tileOffsetX, tileOffsetY);
			const previous = this.lastEntityPositions.get(entity);
			this.lastEntityPositions.set(entity, bounds);
			if (!previous) {
				rects.push(currentRect);
				continue;
			}
			const previousRect = this.toScreenRect(previous, tileOffsetX, tileOffsetY);
			if (this.rectsDiffer(previousRect, currentRect)) {
				rects.push(this.combineRects(previousRect, currentRect));
			}
		}

		this.pendingRects = this.mergeOverlappingRects(rects, canvas.width, canvas.height);
	}

	private resolveEntityBounds(entity: any): { x: number; y: number; width: number; height: number } | undefined {
		if (!entity) {
			return undefined;
		}
		const x = this.resolveCoordinate(entity["_x"], entity["x"]);
		const y = this.resolveCoordinate(entity["_y"], entity["y"]);
		const width = this.resolveCoordinate(entity["width"], 1);
		const height = this.resolveCoordinate(entity["height"], 1);
		return { x, y, width, height };
	}

	private resolveCoordinate(...candidates: Array<number | undefined>): number {
		for (const candidate of candidates) {
			if (typeof candidate === "number" && Number.isFinite(candidate)) {
				return candidate;
			}
		}
		return 0;
	}

	private getPlayerRect(tileOffsetX: number, tileOffsetY: number) {
		try {
			if (typeof marauroa !== "undefined" && marauroa.me) {
				const x = this.resolveCoordinate(marauroa.me["_x"], marauroa.me["x"]);
				const y = this.resolveCoordinate(marauroa.me["_y"], marauroa.me["y"]);
				return this.toScreenRect({ x, y, width: 1, height: 1 }, tileOffsetX, tileOffsetY);
			}
		} catch (err) {
			// ignore
		}
		return undefined;
	}

	private toScreenRect(bounds: { x: number; y: number; width: number; height: number },
		tileOffsetX: number, tileOffsetY: number) {
		const screenX = Math.floor((bounds.x - tileOffsetX) * this.targetTileWidth);
		const screenY = Math.floor((bounds.y - tileOffsetY) * this.targetTileHeight);
		return {
			x: screenX,
			y: screenY,
			width: Math.ceil(bounds.width * this.targetTileWidth),
			height: Math.ceil(bounds.height * this.targetTileHeight)
		};
	}

	private combineRects(a: { x: number; y: number; width: number; height: number },
		b: { x: number; y: number; width: number; height: number }) {
		const x1 = Math.min(a.x, b.x);
		const y1 = Math.min(a.y, b.y);
		const x2 = Math.max(a.x + a.width, b.x + b.width);
		const y2 = Math.max(a.y + a.height, b.y + b.height);
		return { x: x1, y: y1, width: x2 - x1, height: y2 - y1 };
	}

	private rectsDiffer(a: { x: number; y: number; width: number; height: number },
		b: { x: number; y: number; width: number; height: number }): boolean {
		return a.x !== b.x || a.y !== b.y || a.width !== b.width || a.height !== b.height;
	}

	private mergeOverlappingRects(rects: { x: number; y: number; width: number; height: number }[],
		maxWidth: number, maxHeight: number) {
		const merged: { x: number; y: number; width: number; height: number }[] = [];
		for (const rect of rects) {
			const clamped = this.clampRect(rect, maxWidth, maxHeight);
			if (clamped.width <= 0 || clamped.height <= 0) {
				continue;
			}
			let mergedRect = clamped;
			for (let i = 0; i < merged.length;) {
				if (this.intersects(merged[i], mergedRect)) {
					mergedRect = this.combineRects(merged[i], mergedRect);
					merged.splice(i, 1);
					i = 0;
					continue;
				}
				i++;
			}
			merged.push(mergedRect);
		}
		return merged;
	}

	private clampRect(rect: { x: number; y: number; width: number; height: number },
		maxWidth: number, maxHeight: number) {
		const x = Math.max(0, rect.x);
		const y = Math.max(0, rect.y);
		const width = Math.min(rect.x + rect.width, maxWidth) - x;
		const height = Math.min(rect.y + rect.height, maxHeight) - y;
		return { x, y, width, height };
	}

	private intersects(a: { x: number; y: number; width: number; height: number },
		b: { x: number; y: number; width: number; height: number }) {
		return a.x < b.x + b.width && b.x < a.x + a.width && a.y < b.y + b.height && b.y < a.y + a.height;
	}

	private ensureBackBuffer(canvas: HTMLCanvasElement) {
		if (!this.previousFrame) {
			this.previousFrame = document.createElement("canvas");
		}
		if (this.previousFrame.width !== canvas.width || this.previousFrame.height !== canvas.height) {
			this.previousFrame.width = canvas.width;
			this.previousFrame.height = canvas.height;
			this.initialRender = true;
		}
	}

	private copyToBackBuffer(canvas: HTMLCanvasElement) {
		if (!this.previousFrame) {
			return;
		}
		const ctx = this.previousFrame.getContext("2d");
		if (ctx) {
			ctx.clearRect(0, 0, this.previousFrame.width, this.previousFrame.height);
			ctx.drawImage(canvas, 0, 0);
		}
	}

	private drawLayerClipped(ctx: CanvasRenderingContext2D, name: string, tileOffsetX: number, tileOffsetY: number) {
		const index = stendhal.data.map.layerNames.indexOf(name);
		if (index < 0) {
			return;
		}
		const layer = stendhal.data.map.layers[index];
		if (!layer) {
			return;
		}
		const map = stendhal.data.map;
		const union = this.computeUnionRect();
		if (!union) {
			return;
		}

		const startY = Math.max(tileOffsetY + Math.floor(union.y / this.targetTileHeight), tileOffsetY);
		const startX = Math.max(tileOffsetX + Math.floor(union.x / this.targetTileWidth), tileOffsetX);
		const endY = Math.min(tileOffsetY + Math.ceil((union.y + union.height) / this.targetTileHeight), map.zoneSizeY);
		const endX = Math.min(tileOffsetX + Math.ceil((union.x + union.width) / this.targetTileWidth), map.zoneSizeX);

		for (let y = startY; y < endY; y++) {
			for (let x = startX; x < endX; x++) {
				let gid = layer[y * map.zoneSizeX + x];
				const flip = gid & 0xE0000000;
				gid &= 0x1FFFFFFF;
				if (gid <= 0) {
					continue;
				}
				const tilesetIndex = map.getTilesetForGid(gid);
				const tileset = map.aImages[tilesetIndex];
				if (!tileset || tileset.height <= 0) {
					continue;
				}
				const base = map.firstgids[tilesetIndex];
				const idx = gid - base;
				const screenX = x * this.targetTileWidth;
				const screenY = y * this.targetTileHeight;
				if (!this.intersects(union, { x: screenX, y: screenY, width: this.targetTileWidth, height: this.targetTileHeight })) {
					continue;
				}
				this.drawTile(ctx, tileset, idx, screenX, screenY,
					this.targetTileWidth, this.targetTileHeight, flip);
			}
		}
	}

	private drawTile(ctx: CanvasRenderingContext2D, tileset: HTMLImageElement, idx: number, screenX: number, screenY: number,
		destWidth: number, destHeight: number, flip: number): void {
		const tilesetWidth = tileset.width;
		const tilesPerRow = Math.floor(tilesetWidth / stendhal.data.map.tileWidth);
		const sourceX = (idx % tilesPerRow) * stendhal.data.map.tileWidth + TILE_EDGE_TRIM;
		const sourceY = Math.floor(idx / tilesPerRow) * stendhal.data.map.tileHeight + TILE_EDGE_TRIM;
		const sourceWidth = stendhal.data.map.tileWidth - TILE_EDGE_TRIM * 2;
		const sourceHeight = stendhal.data.map.tileHeight - TILE_EDGE_TRIM * 2;

		if (flip === 0) {
			ctx.drawImage(tileset, sourceX, sourceY, sourceWidth, sourceHeight, screenX, screenY, destWidth, destHeight);
			return;
		}

		ctx.save();
		ctx.translate(screenX, screenY);

		if ((flip & 0x80000000) !== 0) {
			ctx.scale(-1, 1);
			ctx.translate(-destWidth, 0);
		}
		if ((flip & 0x40000000) !== 0) {
			ctx.scale(1, -1);
			ctx.translate(0, -destHeight);
		}
		if ((flip & 0x20000000) !== 0) {
			ctx.transform(0, 1, 1, 0, 0, 0);
		}

		ctx.drawImage(tileset, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, destWidth, destHeight);

		ctx.restore();
	}

	private computeUnionRect() {
		if (this.pendingRects.length === 0) {
			return undefined;
		}
		let rect = this.pendingRects[0];
		for (let i = 1; i < this.pendingRects.length; i++) {
			rect = this.combineRects(rect, this.pendingRects[i]);
		}
		return rect;
	}

	private isMobileMode(): boolean {
		try {
			if (typeof stendhal !== "undefined" && stendhal.session && typeof stendhal.session.touchOnly === "function") {
				return !!stendhal.session.touchOnly();
			}
		} catch (err) {
			// ignore
		}
		if (window.matchMedia) {
			const coarse = window.matchMedia("(pointer: coarse)");
			const fine = window.matchMedia("(pointer: fine)");
			return coarse.matches && !fine.matches;
		}
		return false;
	}
}
