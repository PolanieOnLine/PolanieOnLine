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
import { animatedItemSampler } from "../sprite/AnimatedItemSampler";
import { FrameAnimator } from "../sprite/FrameAnimator";

declare var marauroa: any;
declare var stendhal: any;


export class Item extends Entity {

	override minimapShow = false;
	override minimapStyle = "rgb(0,255,0)";
	override zIndex = 7000;
	private quantityTextSprite: TextSprite;

	// animation
	private frameTimeStamp = 0;
	private animated: boolean | null = null;
	private xFrames: number | null = null;
	private yFrames: number | null = null;
	private animator?: FrameAnimator;
	private readonly isMobileLikely: boolean = (() => {
		if (typeof navigator === "undefined") {
			return false;
		}
		const ua = navigator.userAgent || "";
		return /Android|iPhone|iPad|iPod|Mobile|Windows Phone/i.test(ua);
	})();

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

	override draw(ctx: CanvasRenderingContext2D, _tileXOverride?: number, _tileYOverride?: number) {
		this.sprite.offsetY = (this["state"] || 0) * 32;

		const tileX = this.getRenderTileX();
		const tileY = this.getRenderTileY();
		this.drawAt(ctx, tileX * 32, tileY * 32);
	}

	drawAt(ctx: CanvasRenderingContext2D, x: number, y: number) {
		if (this.sprite) {
			this.drawSpriteAt(ctx, x, y);
			let textMetrics = this.quantityTextSprite.getTextMetrics(ctx);
			this.quantityTextSprite.draw(ctx, x + (32 - textMetrics.width), y + 6);
		}
	}

	override drawSpriteAt(ctx: CanvasRenderingContext2D, x: number, y: number) {
		if (this.drawSampledFrame(ctx, x, y)) {
			return;
		}
		super.drawSpriteAt(ctx, x, y);
	}

	private drawSampledFrame(ctx: CanvasRenderingContext2D, x: number, y: number): boolean {
		if (!this.isAnimated()) {
			return false;
		}
		const image = stendhal.data.sprites.get(this.sprite.filename) as CanvasImageSource;
		if (!image || !(image as any).height) {
			return false;
		}
		const width = this.sprite.width || (image as any).width || 32;
		const height = this.sprite.height || (image as any).height || 32;
		const offsetX = this.sprite.offsetX || 0;
		const offsetY = this.sprite.offsetY || 0;

		const preSampled = animatedItemSampler.getFrame(image, offsetX, offsetY, width, height);
		if (!preSampled) {
			return false;
		}

		const destX = x + Math.floor((this.getWidth() * 32 - width) / 2);
		const destY = y + Math.floor((this.getHeight() * 32 - height) / 2);
		ctx.drawImage(preSampled, destX, destY, width, height);
		return true;
	}

	public stepAnimation() {
		const currentTimeStamp = +new Date();
		if (this.frameTimeStamp == 0) {
			this.frameTimeStamp = currentTimeStamp;
			return;
		}
		const delta = currentTimeStamp - this.frameTimeStamp;
		this.frameTimeStamp = currentTimeStamp;
		this.advanceItemAnimation(delta);
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
		const img = stendhal.data.sprites.get(this.sprite.filename);
		if (!img || !img.height) {
			return false;
		}
		if (this.animated == null) {
			// store animation state
			const frameWidth = this.sprite.width || 32;
			const frameCount = img.width ? Math.max(1, Math.floor(img.width / frameWidth)) : 1;
			this.animated = this.canAnimateFrames() && frameCount > 1;
		}

		return this.animated;
	}

	override advanceAnimation(deltaMs: number) {
		this.advanceItemAnimation(deltaMs);
	}

	private advanceItemAnimation(deltaMs: number) {
		if (!this.canAnimateFrames() || !Number.isFinite(deltaMs) || deltaMs <= 0) {
			this.sprite.offsetX = 0;
			this.sprite.offsetY = (this["state"] || 0) * 32;
			this.animator = undefined;
			this.animated = false;
			return;
		}

		const img = stendhal.data.sprites.get(this.sprite.filename);
		if (!img || !img.width || !img.height) {
			return;
		}
		const frameWidth = this.sprite.width || 32;
		const frameCount = Math.max(1, Math.floor(img.width / frameWidth));

		if (!this.animator || this.animator.getFrameCount() !== frameCount) {
			this.animator = new FrameAnimator(frameCount, 100, 0, true, false, frameCount > 1);
		}
		this.animator.setAnimating(frameCount > 1);
		this.animator.advance(deltaMs);

		const frame = this.animator.getFrame();
		this.sprite.offsetX = frame * frameWidth;
		this.animated = frameCount > 1;
	}

	private canAnimateFrames(): boolean {
		const cfg = stendhal.config;
		const lowEffects = cfg && typeof cfg.getBoolean === "function" ? cfg.getBoolean("effect.low") : false;
		const reducedMotion = cfg && typeof cfg.getBoolean === "function" ? cfg.getBoolean("effect.reduced-motion") : false;
		if (lowEffects || reducedMotion || this.isMobileLikely) {
			return false;
		}
		const img = stendhal.data.sprites.get(this.sprite.filename);
		const frameWidth = this.sprite.width || 32;
		const frames = (img && img.width) ? Math.floor(img.width / frameWidth) : 1;
		return frames > 1;
	}

	private setXFrameIndex(idx: number) {
		if (this.xFrames == null) {
			const img = stendhal.data.sprites.get(this.sprite.filename);
			this.xFrames = img.width / 32;
		}

		if (idx >= this.xFrames) {
			// restart
			idx = 0;
		}

		this.sprite.offsetX = idx * 32;
	}

	private setYFrameIndex(idx: number) {
		if (this.yFrames == null) {
			const img = stendhal.data.sprites.get(this.sprite.filename);
			this.yFrames = img.height / 32;
		}

		if (idx >= this.yFrames) {
			// restart
			idx = 0;
		}

		this.sprite.offsetY = idx * 32;
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
}
