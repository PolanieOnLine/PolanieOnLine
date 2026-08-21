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

import { Paths } from "../data/Paths";
import { Entity } from "./Entity";

import { stendhal } from "../stendhal";

export class VisibleEntity extends Entity {

	override zIndex = 1;

	constructor() {
		super();
		this.sprite = {
			height: 32,
			width: 32
		};
	}

	override set(key: string, value: any) {
		super.set(key, value);
		if (key === "class" && value === "questuseable") {
			this["action"] = "use";
		}
		if (["class", "subclass", "name", "_name", "tileset", "tile_index", "tileset_columns"].indexOf(key) > -1) {
			this.updateSpriteSource();
		} else if (key === "state" && !this["tileset"]) {
			this.sprite.offsetY = Number(value) * 32;
		}
	}

	private updateSpriteSource() {
		if (this["tileset"]
				&& typeof this["tile_index"] !== "undefined"
				&& typeof this["tileset_columns"] !== "undefined") {
			const tileIndex = Number(this["tile_index"]);
			const columns = Number(this["tileset_columns"]);
			if (tileIndex >= 0 && columns > 0) {
				this.sprite.filename = Paths.tileset + "/" + this["tileset"] + ".png";
				this.sprite.offsetX = tileIndex % columns * 32;
				this.sprite.offsetY = Math.floor(tileIndex / columns) * 32;
				this.sprite.width = 32;
				this.sprite.height = 32;
				return;
			}
		}

		this.sprite.filename = Paths.sprites + "/"
			+ (this["class"] || "") + "/"
			+ (this["subclass"] || "") + "/"
			+ (this["_name"] || "") + ".png";
		this.sprite.offsetX = 0;
		this.sprite.offsetY = Number(this["state"] || 0) * 32;
	}

	override isVisibleToAction(_filter: boolean) {
		return true;
	}

	override getCursor(x: number, y: number) {
		if (this["class"] === "questuseable") {
			return super.getCursor(x, y);
		}
		return "url(" + Paths.sprites + "/cursor/look.png) 1 3, auto";
	}

}
