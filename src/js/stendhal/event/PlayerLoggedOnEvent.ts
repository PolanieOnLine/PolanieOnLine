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


/**
 * FIXME: doesn't appear to always receive logged on event(s) at time of client creation as
 *        `stendhal.players` is sometimes empty event after `entity.User.User` instance is
 *        created.
 * NOTE:  So far have only noticed players on same map as user at login don't always register
 *        a login event.
 */
export class PlayerLoggedOnEvent extends RPEvent {

	public name!: string;


	override execute(entity: any) {
		const playerName = this.name;
		if (!stendhal.ui.buddyList.hasBuddy(playerName)) {
			return;
		}
		if (stendhal.players.indexOf(playerName) < 0) {
			// remember logged on players
			stendhal.players.push(playerName);
			stendhal.players = stendhal.players.sort();
		}

		const localName = marauroa?.me?.["_name"] ?? marauroa?.me?.["name"];
		const isLocalPlayer = typeof localName === "string" && localName === playerName;
		if (stendhal.playerInGame && !isLocalPlayer) {
			Chat.log("information", `${playerName} zawitał do PolanieOnLine.`);
		}
	}
}
