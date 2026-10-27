/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { marauroa } from "marauroa"
import { stendhal } from "../stendhal";

import { RPEvent } from "marauroa"
import { Chat } from "../util/Chat";
import { GameLanguage } from "../util/GameLanguage";


export class PlayerLoggedOutEvent extends RPEvent {

	public name!: string;


	override execute(entity: any) {
		const playerName = this.name;
		const index = stendhal.players.indexOf(playerName);
		if (index >= 0) {
			stendhal.players.splice(index, 1);
		}

		const localName = marauroa?.me?.["_name"] ?? marauroa?.me?.["name"];
		const isLocalPlayer = typeof localName === "string" && localName === playerName;
		const buddies = marauroa?.me?.["buddies"];
		const isFriend = buddies != null && Object.prototype.hasOwnProperty.call(buddies, playerName);
		// Never announce an unknown/hidden player's departure or a duplicate event.
		if (stendhal.playerInGame && !isLocalPlayer && isFriend && index >= 0) {
			Chat.log("information", GameLanguage.getText(`${playerName} opuścił PolanieOnLine.`));
		}
	}
}
