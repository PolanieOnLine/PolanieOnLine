/***************************************************************************
 *                (C) Copyright 2003-2023 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Component } from "../toolkit/Component";

declare let marauroa: any;

/**
 * zone info
 */
export class ZoneInfoComponent extends Component {
	private readonly DANGER_LEVEL_DESCRIPTIONS = [
		"Ten obszar wygląda na bezpieczny.",
		"Ten obszar jest stosunkowo bezpieczny.",
		"Ten obszar jest trochę niebezpieczny.",
		"Ten obszar jest niebezpieczny.",
		"Ten obszar jest bardzo niebezpieczny!",
		"Ten obszar jest wyjątkowo niebezpieczny. Uciekaj!"];


	constructor() {
		super("zoneinfo");
	}


	public zoneChange(zoneinfo: any) {
		document.getElementById("zonename")!.textContent = zoneinfo["readable_name"];
		if (marauroa.me) {
			let dangerLevel = Number.parseFloat(zoneinfo["danger_level"]);
			let skulls = Math.min(5, Math.round(2 * dangerLevel / (Number.parseInt(marauroa.me.level, 10) + 3)));
			let div = document.getElementById("skulls")!;
			if (skulls === 0) {
				div.style.height = "0";
			} else {
				div.style.height = "16px";
			}
			div.style.width = (skulls * 20) + "px";
			this.componentElement.title = this.DANGER_LEVEL_DESCRIPTIONS[skulls];
		}
	};

}
