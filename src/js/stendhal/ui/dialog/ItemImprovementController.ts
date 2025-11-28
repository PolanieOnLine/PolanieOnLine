/***************************************************************************
 *                      (C) Copyright 2024 - PolanieOnLine                 *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { UIComponentEnum } from "../UIComponentEnum";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { ui } from "../UI";
import { Chat } from "../../util/Chat";
import { ItemImprovementPanel } from "./ItemImprovementPanel";

import { ItemImprovementEntry } from "./ItemImprovementTypes";

declare var marauroa: any;
declare var stendhal: any;

/**
 * Controller wiring NPC menu actions, server messages, and the improvement UI.
 */
export class ItemImprovementController {
	private static windowId = "item-improvement";

	/**
	 * Requests the improvement list for the given NPC.
	 */
	static requestList(npc: string) {
		const target = npc || "Kowal Tworzymir";
		marauroa.clientFramework.sendAction({
			type: "improve_list",
			npc: target,
			target: target,
			zone: marauroa.currentZoneName
		});
	}

	/**
	 * Sends the improve request for the selected entry.
	 */
	static requestUpgrade(npc: string, entry: ItemImprovementEntry) {
		const target = npc || "Kowal Tworzymir";
		marauroa.clientFramework.sendAction({
			type: "improve_do",
			npc: target,
			target: target,
			itemid: entry.id,
			zone: marauroa.currentZoneName
		});
	}

	/**
	 * Opens or updates the improvement panel with data from the server.
	 */
	static showList(npc: string, entries: ItemImprovementEntry[]) {
		let dialog = ui.get(UIComponentEnum.ItemImprovementDialog) as ItemImprovementPanel;
		if (!dialog) {
			const dstate = stendhal.config.getWindowState(ItemImprovementController.windowId);
			dialog = new ItemImprovementPanel(npc);
			const frame = new FloatingWindow("Ulepszanie", dialog, dstate.x, dstate.y);
			frame.setId(ItemImprovementController.windowId);
			dialog.setFrame(frame);
		}

		dialog.setNpcName(npc);
		dialog.setEntries(entries);
	}

	/**
	 * Handles result notifications and refreshes the list if the panel is open.
	 */
	static handleResult(npc: string, success: boolean, message: string) {
		const type = success ? "server" : "error";
		Chat.log(type, message, npc);

		const dialog = ui.get(UIComponentEnum.ItemImprovementDialog) as ItemImprovementPanel;
		if (dialog && dialog.matchesNpc(npc)) {
			dialog.reload();
		}
	}
}
