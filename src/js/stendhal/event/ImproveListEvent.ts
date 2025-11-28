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

import { RPEvent } from "./RPEvent";
import { ItemImprovementController } from "../ui/dialog/ItemImprovementController";
import { ItemImprovementEntry } from "../ui/dialog/ItemImprovementTypes";

/**
	* Receives improvement list payload and forwards it to the UI controller.
	*/
export class ImproveListEvent extends RPEvent {
execute(_entity: any) {
const npc = this["npc"];
const raw = this["items"];
let entries: ItemImprovementEntry[] = [];
if (raw) {
try {
entries = JSON.parse(raw) as ItemImprovementEntry[];
} catch (e) {
console.error("Failed to parse improvement list", e);
}
}

ItemImprovementController.showList(npc, entries);
}
}
