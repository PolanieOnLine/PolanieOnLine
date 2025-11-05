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

import { ParamList } from "./ParamList";
import { SlashAction } from "./SlashAction";

import { ui } from "../ui/UI";
import { UIComponentEnum } from "../ui/UIComponentEnum";

import { TravelLogDialog } from "../ui/dialog/TravelLogDialog";

import { FloatingWindow } from "../ui/toolkit/FloatingWindow";

import { ConfigManager } from "../util/ConfigManager";
import { Pair } from "../util/Pair";


/**
 * Requests progress status info from server.
 */
export class ProgressStatusAction extends SlashAction {

	readonly minParams = 0;
	readonly maxParams = 0;


	override execute(_type: string, _params: string[], _remainder: string): boolean {
		let travelLogDialog = ui.get(UIComponentEnum.TravelLogDialog) as TravelLogDialog;
		if (!travelLogDialog) {
			// display travel log dialog before sending request so player knows action executed correctly
			// & not confused by potential delay in response
			const dstate = ConfigManager.get().getWindowState("travel-log");
			travelLogDialog = new TravelLogDialog();
			new FloatingWindow("Dziennik Zadań", travelLogDialog, dstate.x, dstate.y).setId("travel-log");
		}

		const action: any = {"type": _type};
		if (_remainder.length > 0) {
			if (_remainder.indexOf("Otwarte zadania") > -1) {
				action["progress_type"] = "Otwarte zadania";
				_remainder = _remainder.substring(12);
			} else if (_remainder.indexOf("Ukończone zadania") > -1) {
				action["progress_type"] = "Ukończone zadania";
				_remainder = _remainder.substring(17);
			} else if (_remainder.indexOf("Produkcja") > -1) {
				action["progress_type"] = "Produkcja";
				_remainder = _remainder.substring(11);
			} else {

			}
			if (_remainder) {
				action["item"] = _remainder;
			}
		}
		this.send(action);
		return true;
	}

	override getHelp(params?: ParamList|Pair<string, string>[]): string[] {
		// FIXME: including parameter in chat command input should set visible tab
		return ["", "Otwórz okno dialogowe dziennika podróży."];
	}
}
