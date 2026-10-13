/***************************************************************************
 *                   (C) Copyright 2005-2023 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Entity } from "entity/Entity";
import { RPEvent } from "marauroa"
import { GameLanguage } from "util/GameLanguage";

/**
 * text events
 */
export class TextEvent extends RPEvent {

	public range!: string;
	public text!: string;

	public execute(entity: Entity): void {
		const text = entity["type"] === "npc" ? GameLanguage.getText(this["text"]) : this["text"];
		if (this["range"]) {
			entity.say(text, parseInt(this["range"], 10));
		} else {
			entity.say(text);
		}
	}

};
