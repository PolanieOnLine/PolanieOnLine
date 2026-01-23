/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { SlashAction } from "./SlashAction";

import { ui } from "../ui/UI";
import { UIComponentEnum } from "../ui/UIComponentEnum";


export class RunicAltarAction extends SlashAction {
	readonly minParams = 0;
	readonly maxParams = 0;

	override readonly desc = "Przełącza widoczność okna Ołtarza Runicznego.";


	execute(_type: string, _params: string[], _remainder: string): boolean {
		const runicAltar = ui.get(UIComponentEnum.RunicAltar);
		if (!runicAltar) {
			return false;
		}
		runicAltar.toggleVisibility();
		return true;
	}
}
