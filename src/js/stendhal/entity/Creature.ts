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

import { SkillEffect } from "../sprite/action/SkillEffect";
import { Paths } from "../data/Paths";
import { RenderingContext2D } from "util/Types";
import { drawCreatureArmor, getCreatureArmorLabel } from "../ui/render/CreatureArmorRenderer";

import { marauroa } from "marauroa"
import { stendhal } from "../stendhal";

export class Creature extends RPEntity {
	override minimapShow = true;
	override minimapStyle = Color.CREATURE;
	override spritePath = "monsters";
	override titleStyle = "#ffc8c8";

	getArmorLabel(): string | undefined {
		return getCreatureArmorLabel(this["armor_type"]);
	}

	override drawHealthBar(ctx: RenderingContext2D, x: number, y: number) {
		if (this.hasOwnProperty("no_hpbar")) {
			return;
		}
		super.drawHealthBar(ctx, x, y);
		if (marauroa.me?.getAttackTarget() === this) {
			const barX = x + ((this["width"] * 32) - this["drawWidth"]) / 2;
			const barY = y + this["height"] * 32 - this["drawHeight"] - 6 + this.titleDrawYOffset;
			drawCreatureArmor(ctx, this["armor_type"], barX, barY, this["drawWidth"], 4);
		}
	}


	override set(key: string, value: any) {
		super.set(key, value);

		if (key === "name") {
			// overlay animation
			this.overlay = EntityOverlayRegistry.get("creature", this);
		}
	}

	override onclick(_x: number, _y: number) {
		var action = {
				"type": "attack",
				"target": "#" + this["id"]
			};
		marauroa.clientFramework.sendAction(action);
	}

	// Overrides the one in creature
	override say(text: string) {
		if (stendhal.config.getBoolean("speech.creature")) {
			this.addSpeechBubble(text);
		}
	}

	override getCursor(_x: number, _y: number) {
		return "url(" + Paths.sprites + "/cursor/attack.png) 1 3, auto";
	}

	/**
	 * Shows a temporary animation overlay for certain entities.
	 *
	 * FIXME: does not restore previous overlay
	 */
	protected override onTransformed() {
		const allowed = ["panna wampir", "lord wampir", "wampirzyca"];
		if (!allowed.includes(this["name"])) {
			return;
		}

		const delay = 100;
		const frames = 5;
		this.overlay = new SkillEffect("transform", delay, delay * frames);
	}
}

