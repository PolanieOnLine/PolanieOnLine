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

declare var marauroa: any;
declare var stendhal: any;

import { RPEvent } from "./RPEvent";
import { Chat } from "../util/Chat";


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
		if (stendhal.playerInGame && !isLocalPlayer) {
			Chat.log("information", `${playerName} opuścił PolanieOnLine.`);
		}
	}
}
