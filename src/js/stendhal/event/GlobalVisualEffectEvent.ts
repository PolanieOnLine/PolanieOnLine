/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { singletons } from "../SingletonRepo";
import { RPEvent } from "./RPEvent";

/**
 * An event that triggers a full-screen visual effect.
 */
export class GlobalVisualEffectEvent extends RPEvent {
	public effect_name!: string;
	public duration!: number;
	public strength?: number;

	execute(_entity: any): void {
		const name = this["effect_name"];
		const duration = Number(this["duration"]);
		const strength = this.hasOwnProperty("strength") ? Number(this["strength"]) : undefined;
		singletons.getGlobalVisualEffectRenderer().addEffect(name, duration, strength);
	}
}
