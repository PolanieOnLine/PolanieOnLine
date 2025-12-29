/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ItemMap } from "./ItemMap";
import { MenuItem } from "../action/MenuItem";
import { Entity } from "./Entity";
import { TextSprite } from "../sprite/TextSprite";
import { Canvas, RenderingContext2D } from "util/Types";

declare var marauroa: any;
declare var stendhal: any;

type FrameBuffer = ImageBitmap | Canvas;

interface CachedFrames {
	frames: FrameBuffer[];
	columns: number;
	rows: number;
	width: number;
	height: number;
}

export class Item extends Entity {
	private static readonly FRAME_SIZE = 32;
	private static readonly frameCache: Map<string, CachedFrames> = new Map();
	private static readonly offscreenSupported = (typeof OffscreenCanvas !== "undefined");

	override minimapShow = false;
	override minimapStyle = "rgb(0,255,0)";
	override zIndex = 7000;
	private quantityTextSprite: TextSprite;

	// animation
	private frameTimeStamp = 0;
	private animated: boolean | null = null;
	private xFrames: number | null = null;
	private yFrames: number | null = null;

	constructor() {
		super();
		this.sprite = {
			height: 32,
			width: 32
		};
		this.quantityTextSprite = new TextSprite("", "white", "10px sans-serif");
	}

	override isVisibleToAction(_filter: boolean) {
		return true;
	}

	override buildActions(list: MenuItem[]) {
		super.buildActions(list);

		for (const mi of ItemMap.getActions(this)) {
			if (typeof (mi.index) === "number") {
				list.splice(mi.index, 0, mi);
			} else {
				list.push(mi);
			}
		}
	}

	// default action for items on the ground is to pick them up
	// do not require players to use drag & drop (as the classic client did)
	// because drag & drop is difficult on mobile (especially if the page is zoomed)
	override getDefaultAction() {
		return {
			type: "equip",
			"source_path": this.getIdPath(),
			"target_path": "[" + marauroa.me["id"] + "\tbag]",
			"clicked": "", // useful for changing default target in equip action
			"zone": marauroa.currentZoneName
		} as any;
	}

	override set(key: string, value: any) {
		super.set(key, value);
		if (key === "class" || key === "subclass") {
			this.sprite.filename = stendhal.paths.sprites + "/items/"
				+ this["class"] + "/" + this["subclass"] + ".png";
		}
		if (key === "quantity") {
			this.quantityTextSprite = new TextSprite(this.formatQuantity(), "white", "10px sans-serif");
		}
	}

	override draw(ctx: RenderingContext2D) {
		this.sprite.offsetY = (this["state"] || 0) * 32
		if (this.isAnimated()) {
			this.stepAnimation();
		}

		this.drawAt(ctx, this["x"] * 32, this["y"] * 32);
	}

	drawAt(ctx: RenderingContext2D, x: number, y: number) {
		if (this.sprite) {
			const cachedFrames = this.ensureFrameBuffers();
			const frameBuffer = this.getCachedFrame(cachedFrames);
			if (frameBuffer && cachedFrames) {
				this.drawCachedFrame(ctx, frameBuffer, cachedFrames, x, y);
			} else {
				this.drawSpriteAt(ctx, x, y);
			}
			let textMetrics = this.quantityTextSprite.getTextMetrics(ctx);
			if (!textMetrics) {
				throw new Error("textMetrics is undefined");
			}
			this.quantityTextSprite.draw(ctx, x + (32 - textMetrics.width), y + 6);
		}
	}

	public stepAnimation() {
		if (!this.isAnimated()) {
			return;
		}

		const currentTimeStamp = +new Date();
		if (this.frameTimeStamp == 0) {
			this.frameTimeStamp = currentTimeStamp;
			this.sprite.offsetX = 0;
			this.sprite.offsetY = 0;
		} else if (currentTimeStamp - this.frameTimeStamp >= 100) {
			// FIXME: need proper FPS limit
			this.setXFrameIndex(this.getXFrameIndex() + 1);
			this.frameTimeStamp = currentTimeStamp;
		}
	}

	formatQuantity() {
		if (!this["quantity"] || this["quantity"] === "1") {
			return "";
		}
		if (this["quantity"] > 10000000) {
			return Math.floor(this["quantity"] / 1000000) + "m";
		}
		if (this["quantity"] > 10000) {
			return Math.floor(this["quantity"] / 1000) + "k";
		}
		return this["quantity"];
	}

	override getCursor(_x: number, _y: number) {
		let cursor;
		if (!this._parent) {
			cursor = "itempickupfromslot";
		} else {
			cursor = ItemMap.getCursor(this["class"], this["name"]);
		}
		return "url(" + stendhal.paths.sprites + "/cursor/" + cursor + ".png) 1 3, auto";
	}

	public getToolTip(): string {
		if (this["class"] === "scroll" && this["dest"]) {
			const dest = this["dest"].split(",");
			if (dest.length > 2) {
				return dest[0] + " " + dest[1] + "," + dest[2];
			}
		}
		return "";
	}

	public isAnimated(): boolean {
		if (!stendhal.data.sprites.get(this.sprite.filename).height) {
			return false;
		}
		if (this.animated == null) {
			const frames = this.getFrameCounts();
			// store animation state
			this.animated = frames.columns > 1;
		}

		return this.animated;
	}

	private setXFrameIndex(idx: number) {
		if (this.xFrames == null) {
			this.xFrames = this.getFrameCounts().columns;
		}

		if (idx >= this.xFrames) {
			// restart
			idx = 0;
		}

		this.sprite.offsetX = idx * Item.FRAME_SIZE;
	}

	private setYFrameIndex(idx: number) {
		if (this.yFrames == null) {
			this.yFrames = this.getFrameCounts().rows;
		}

		if (idx >= this.yFrames) {
			// restart
			idx = 0;
		}

		this.sprite.offsetY = idx * Item.FRAME_SIZE;
	}

	public getXFrameIndex(): number {
		return (this.sprite.offsetX || 0) / 32;
	}

	public getYFrameIndex(): number {
		return (this.sprite.offsetY || 0) / 32;
	}

	public override isDraggable(): boolean {
		return true;
	}

	private static createFrameCanvas(width: number, height: number): Canvas {
		if (Item.offscreenSupported) {
			return new OffscreenCanvas(width, height);
		}

		const canvas = document.createElement("canvas");
		canvas.width = width;
		canvas.height = height;
		return canvas;
	}

	private static getFrameContext(canvas: Canvas): RenderingContext2D | null {
		return canvas.getContext("2d") as RenderingContext2D | null;
	}

	private static finalizeFrameBuffer(canvas: Canvas): FrameBuffer {
		if (Item.offscreenSupported && canvas instanceof OffscreenCanvas && typeof canvas.transferToImageBitmap === "function") {
			try {
				return canvas.transferToImageBitmap();
			} catch (e) {
				// fall through to canvas usage
			}
		}

		return canvas;
	}

	private ensureFrameBuffers(): CachedFrames | null {
		const filename = this.sprite.filename;
		if (!filename) {
			return null;
		}

		const cached = Item.frameCache.get(filename);
		if (cached) {
			this.xFrames = cached.columns;
			this.yFrames = cached.rows;
			return cached;
		}

		const image = stendhal.data.sprites.get(filename);
		if (!image || !image.height || image.width <= Item.FRAME_SIZE) {
			return null;
		}

		const frameWidth = Item.FRAME_SIZE;
		const frameHeight = this.sprite.height || Item.FRAME_SIZE;
		const columns = Math.max(1, Math.floor(image.width / frameWidth));
		const rows = Math.max(1, Math.floor(image.height / frameHeight));
		if (columns <= 1) {
			this.xFrames = columns;
			this.yFrames = rows;
			return null;
		}

		const frames: FrameBuffer[] = [];
		for (let y = 0; y < rows; y++) {
			for (let x = 0; x < columns; x++) {
				const canvas = Item.createFrameCanvas(frameWidth, frameHeight);
				const frameCtx = Item.getFrameContext(canvas);
				if (!frameCtx) {
					continue;
				}
				frameCtx.clearRect(0, 0, frameWidth, frameHeight);
				frameCtx.drawImage(image, x * frameWidth, y * frameHeight, frameWidth, frameHeight,
					0, 0, frameWidth, frameHeight);
				frames.push(Item.finalizeFrameBuffer(canvas));
			}
		}

		const generated = { frames, columns, rows, width: frameWidth, height: frameHeight };
		Item.frameCache.set(filename, generated);
		this.xFrames = columns;
		this.yFrames = rows;
		return generated;
	}

	private getCachedFrame(cachedFrames: CachedFrames | null): FrameBuffer | undefined {
		if (!cachedFrames || !cachedFrames.frames.length) {
			return undefined;
		}

		const xIndex = this.getXFrameIndex();
		const yIndex = this.getYFrameIndex();
		if (xIndex >= cachedFrames.columns || yIndex >= cachedFrames.rows) {
			return undefined;
		}

		return cachedFrames.frames[yIndex * cachedFrames.columns + xIndex];
	}

	private drawCachedFrame(ctx: RenderingContext2D, frame: FrameBuffer, cachedFrames: CachedFrames, x: number, y: number) {
		const width = this.sprite.width || cachedFrames.width;
		const height = this.sprite.height || cachedFrames.height;
		const offsetX = Math.floor((this.getWidth() * Item.FRAME_SIZE - width) / 2);
		const offsetY = Math.floor((this.getHeight() * Item.FRAME_SIZE - height) / 2);
		ctx.drawImage(frame, 0, 0, cachedFrames.width, cachedFrames.height,
			x + offsetX, y + offsetY, width, height);
	}

	private getFrameCounts() {
		const cached = this.ensureFrameBuffers();
		if (cached) {
			return { columns: cached.columns, rows: cached.rows };
		}

		const img = stendhal.data.sprites.get(this.sprite.filename);
		const width = img && img.width ? img.width : Item.FRAME_SIZE;
		const height = img && img.height ? img.height : Item.FRAME_SIZE;
		return {
			columns: Math.max(1, Math.floor(width / Item.FRAME_SIZE)),
			rows: Math.max(1, Math.floor(height / Item.FRAME_SIZE)),
		};
	}
}
