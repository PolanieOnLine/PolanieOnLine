/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { RPEntity } from "./RPEntity";

import { EntityOverlayRegistry } from "../data/EntityOverlayRegistry";

import { Color } from "../data/color/Color";
import { MenuItem } from "../ui/toolkit/Menu";
import { ItemImprovementController } from "../ui/dialog/ItemImprovementController";

declare var stendhal: any;

export class NPC extends RPEntity {
	override minimapShow = true;
	override minimapStyle = Color.NPC;
	override spritePath = "npc";
	override titleStyle = "#c8c8ff";

	constructor() {
		super();
		this["hp"] = 100;
		this["base_hp"] = 100;
	}

	override set(key: string, value: string) {
		super.set(key, value);

		if (key === "name") {
			// overlay animation
			this.overlay = EntityOverlayRegistry.get("NPC", this);

			if (value.startsWith("Zekiel")) {
				// Zekiel uses transparentnpc sprite but he is taller
				this.titleDrawYOffset = -32;
			}
		}
	}

	override drawTop(ctx: CanvasRenderingContext2D, _tileX?: number, _tileY?: number) {
		const tileX = this.getRenderTileX();
		const tileY = this.getRenderTileY();
		var localX = tileX * 32;
		var localY = tileY * 32;
		if (typeof (this["no_hpbar"]) == "undefined") {
			this.drawHealthBar(ctx, localX, localY + this.statusBarYOffset);
		}
		if (typeof (this["unnamed"]) == "undefined") {
			this.drawTitle(ctx, localX, localY + this.statusBarYOffset);
		}
	}

		override buildActions(list: MenuItem[]) {
			super.buildActions(list);
			const rawName = (this["name"] || this["title"] || this["type"] || "").toString();
			const nameLower = rawName.toLowerCase();
			if (nameLower.indexOf("tworzymir") >= 0) {
				const npcName = rawName || "Kowal Tworzymir";
				list.push({
					title: "Ulepszanie",
					action: () => {
						ItemImprovementController.requestList(npcName);
					}
				});
			}
		}

	override getCursor(_x: number, _y: number) {
		return "url(" + stendhal.paths.sprites + "/cursor/look.png) 1 3, auto";
	}

}
