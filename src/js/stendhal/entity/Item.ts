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
import { RenderingContext2D } from "util/Types";

type FrameCounts = {
	columns: number;
	rows: number;
};

export class ItemAnimationClock {
	private static readonly FRAME_DELAY = 100;
	private static frameCounts = new Map<string, number>();
	private static frameIndexCache = new Map<string, number>();
	private static elapsed = 0;
	private static lastTick = performance.now();

	static update(delta?: number) {
		const now = performance.now();
		const step = (typeof delta === "number" && Number.isFinite(delta))
			? Math.max(0, delta)
			: Math.max(0, now - this.lastTick);
		this.lastTick = (typeof delta === "number" && Number.isFinite(delta)) ? this.lastTick + step : now;
		if (step === 0) {
			return;
		}

		this.elapsed += step;

		for (const [key, columns] of this.frameCounts) {
			const frameIndex = columns > 0 ? Math.floor((this.elapsed / ItemAnimationClock.FRAME_DELAY) % columns) : 0;
			this.frameIndexCache.set(key, frameIndex);
		}
	}

	static setFrameCount(key: string, columns: number) {
		const safeColumns = Math.max(1, columns);
		this.frameCounts.set(key, safeColumns);
		const currentIndex = Math.floor((this.elapsed / ItemAnimationClock.FRAME_DELAY) % safeColumns);
		this.frameIndexCache.set(key, currentIndex);
	}

	static getFrameIndex(key: string, columns: number): number {
		this.setFrameCount(key, columns);
		return this.frameIndexCache.get(key) || 0;
	}
}

declare var marauroa: any;
declare var stendhal: any;

export class Item extends Entity {
	private static readonly FRAME_SIZE = 32;
	private static readonly frameCountsCache = new Map<string, FrameCounts>();

	override minimapShow = false;
	override minimapStyle = "rgb(0,255,0)";
	override zIndex = 7000;
	private quantityTextSprite: TextSprite;

	// animation
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
		if (!this.inView()) {
			return;
		}

		this.sprite.offsetY = (this["state"] || 0) * 32
		if (this.isAnimated()) {
			this.stepAnimation();
		}

		this.drawAt(ctx, this["x"] * 32, this["y"] * 32);
	}

	drawAt(ctx: RenderingContext2D, x: number, y: number) {
		if (!this.sprite || !this.inView()) {
			return;
		}

		const image = stendhal.data.sprites.get(this.sprite.filename);
		if (!image || !image.height) {
			return;
		}

		const tileX = x;
		const tileY = y;
		const frameWidth = this.sprite.width || Item.FRAME_SIZE;
		const frameHeight = this.sprite.height || Item.FRAME_SIZE;
		const width = frameWidth;
		const height = frameHeight;
		const offsetX = this.sprite.offsetX || 0;
		const offsetY = this.sprite.offsetY || 0;

		x += Math.floor((this.getWidth() * Item.FRAME_SIZE - width) / 2);
		y += Math.floor((this.getHeight() * Item.FRAME_SIZE - height) / 2);

		ctx.drawImage(image, offsetX, offsetY, frameWidth, frameHeight, x, y, width, height);
		let textMetrics = this.quantityTextSprite.getTextMetrics(ctx);
		if (!textMetrics) {
			throw new Error("textMetrics is undefined");
		}
		this.quantityTextSprite.draw(ctx, tileX + (32 - textMetrics.width), tileY + 6);
	}

	public stepAnimation() {
		if (!this.isAnimated()) {
			return;
		}

		const frameIndex = this.getAnimationFrameIndex();
		this.setXFrameIndex(frameIndex);
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
			const frames = this.ensureFrameCounts();
			// store animation state
			this.animated = frames.columns > 1;
		}

		return this.animated;
	}

	private setXFrameIndex(idx: number) {
		const frameWidth = this.getFrameWidth();
		const frameCount = this.xFrames ?? this.ensureFrameCounts().columns;

		if (idx >= frameCount) {
			// restart
			idx = 0;
		}

		this.sprite.offsetX = idx * frameWidth;
	}

	private setYFrameIndex(idx: number) {
		const frameHeight = this.getFrameHeight();
		const frameCount = this.yFrames ?? this.ensureFrameCounts().rows;

		if (idx >= frameCount) {
			// restart
			idx = 0;
		}

		this.sprite.offsetY = idx * frameHeight;
	}

	public getXFrameIndex(): number {
		const frameWidth = this.sprite.width || Item.FRAME_SIZE;
		return (this.sprite.offsetX || 0) / frameWidth;
	}

	public getYFrameIndex(): number {
		const frameHeight = this.sprite.height || Item.FRAME_SIZE;
		return (this.sprite.offsetY || 0) / frameHeight;
	}

	public override isDraggable(): boolean {
		return true;
	}

	public getAnimationFrameIndex(): number {
		const frameCounts = this.ensureFrameCounts();

		if (this.animated == null) {
			this.animated = frameCounts.columns > 1;
		}

		if (!this.animated) {
			return 0;
		}

		return ItemAnimationClock.getFrameIndex(this.getAnimationKey(), frameCounts.columns);
	}

	private getFrameCounts() {
		const img = stendhal.data.sprites.get(this.sprite.filename);
		const frameWidth = this.getFrameWidth();
		const frameHeight = this.getFrameHeight();
		const width = img && img.width ? img.width : frameWidth;
		const height = img && img.height ? img.height : frameHeight;
		const columns = Math.max(1, Math.floor(width / frameWidth));
		const rows = Math.max(1, Math.floor(height / frameHeight));
		this.xFrames = columns;
		this.yFrames = rows;
		const key = this.getAnimationKey();
		ItemAnimationClock.setFrameCount(key, columns);
		Item.frameCountsCache.set(key, { columns, rows });
		return { columns, rows };
	}

	private ensureFrameCounts(): FrameCounts {
		if (this.xFrames != null && this.yFrames != null) {
			return { columns: this.xFrames, rows: this.yFrames };
		}

		const key = this.getAnimationKey();
		const cached = Item.frameCountsCache.get(key);
		if (cached) {
			this.xFrames = cached.columns;
			this.yFrames = cached.rows;
			return cached;
		}

		return this.getFrameCounts();
	}

	private getAnimationKey(): string {
		return `${this.sprite.filename}:${this.getFrameWidth()}x${this.getFrameHeight()}`;
	}

	private getFrameWidth() {
		return this.sprite.width || Item.FRAME_SIZE;
	}

	private getFrameHeight() {
		return this.sprite.height || Item.FRAME_SIZE;
	}
}
