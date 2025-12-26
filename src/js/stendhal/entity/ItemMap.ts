/***************************************************************************
 *                    Copyright © 2003-2023 - Stendhal                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Entity } from "./Entity";
import { MenuItem } from "../action/MenuItem";

import { singletons } from "../SingletonRepo";

declare var marauroa: any;

const defaultUse = {
	title: "Użyj",
	type: "use",
	index: 0
}


export const ItemMap: {[index: string]: any} = {
	["class"]: {
		["box"]: {
			cursor: "bag",
			actions: [
				{
					title: "Otwórz",
					type: "use",
					index: 0
				}
			]
		},
		["drink"]: {
			cursor: "itemuse"
		},
		["food"]: {
			cursor: "itemuse"
		},
		["scroll"]: {
			cursor: "itemuse",
			actions: [defaultUse]
		}
	},

	["name"]: {
		["woda święcona z popiołem"]: {
			actions: [defaultUse]
		},
		["bestiariusz"]: {
			cursor: "itemuse"
		},
		["bulwa"]: {
			cursor: "itemuse"
		},
		["niezapisany zwój"]: {
			actions: function(e: Entity) {
				const count = parseInt(e["quantity"], 10);
				if (count > 1 && e._parent) {
					return [{
						title: "Zapisz wszystkie",
						index: 1,
						action: function(entity: Entity) {
							// FIXME: doesn't work if scrolls are on ground
							//        tries to pull scrolls from inventory
							marauroa.clientFramework.sendAction(
								{
									"type": "markscroll",
									"quantity": ""+count
								});
						}
					}];
				}
			}
		},
		["moździerz z tłuczkiem"]: {
			cursor: "itemuse"
		},
		["wykrywacz metali"]: {
			cursor: "itemuse"
		},
		["obraz w drewnianej ramce"]: {
			cursor: "itemuse"
		},
		["obrotowy nożyk"]: {
			cursor: "itemuse"
		},
		["zwój czyszczący"]: {
			cursor: "itemuse"
		},
		["nasiona"]: {
			cursor: "itemuse"
		},
		["zima zaklęta w kuli"]: {
			cursor: "itemuse",
			actions: [
				{
					title: "Potrząśnij",
					type: "use",
					index: 0
				}
			]
		},
		["młynek do cukru"]: {
			cursor: "itemuse"
		},
		["pluszowy miś"]: {
			cursor: "itemuse",
			actions: [
				{
					title: "Przytul",
					type: "use",
					index: 0
				}
			]
		},
		["obrączka ślubna"]: {
			cursor: "itemuse",
			actions: [defaultUse]
		}
	},

	/**
	 * Retrieves cursor registered for item.
	 *
	 * @param clazz
	 *     Item class.
	 * @param name
	 *     Item name.
	 * @return
	 *     Cursor name.
	 */
	getCursor: function(clazz: string, name: string) {
		let cursor = "normal";
		for (const imap of [ItemMap["class"][clazz], ItemMap["name"][name]]) {
			if (imap && imap.cursor) {
				cursor = imap.cursor;
			}
		}
		if (cursor === "itemuse") {
			const config = singletons.getConfigManager();
			if (!config.getBoolean("inventory.double-click")) {
				cursor = "activity";
			}
		}
		return cursor;
	},

	/**
	 * Retrieves list of menu actions defined for item.
	 *
	 * @param item
	 *     Object containing item defintion.
	 * @return
	 *     Actions.
	 */
	getActions: function(item: any): MenuItem[] {
		let actions: MenuItem[] = [];
		if (!item) {
			return actions;
		}

		for (const imap of [ItemMap["class"][item["class"]], ItemMap["name"][item["name"]]]) {
			if (imap && imap.actions) {
				let a: MenuItem[];
				if (typeof(imap.actions) === "function") {
					a = imap.actions(item) || [];
				} else {
					a = imap.actions;
				}
				actions = actions.concat(a);
			}
		}
		return actions;
	}
};
