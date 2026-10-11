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

import { stendhal } from "../stendhal";

import { RPEvent } from "marauroa"


/**
 * View center changing event.
 */
export class ViewChangeEvent extends RPEvent {

	public x!: number;
	public y!: number;


	execute(_entity: any) {
		queueMicrotask(() => {
			stendhal.ui.viewport.requestViewCenter(this.x, this.y);
		});
	}
}

