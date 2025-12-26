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
		this.sprite.offsetY = (this["state"] || 0) * 32
		this.stepAnimation();

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
			// store animation state
			this.animated = (stendhal.data.sprites.get(this.sprite.filename).width / 32) > 1;
		}

		return this.animated;
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
