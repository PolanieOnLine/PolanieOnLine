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

/**
	* Improvement result notification.
	*/
export class ImproveResultEvent extends RPEvent {
execute(_entity: any): void {
const npc = this["npc"];
const success = !!this["success"];
const message = this["message"];
ItemImprovementController.handleResult(npc, success, message);
}
}
