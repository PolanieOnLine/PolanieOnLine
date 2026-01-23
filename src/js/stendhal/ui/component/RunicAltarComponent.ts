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

import { Component } from "../toolkit/Component";
import { ItemContainerImplementation } from "./ItemContainerImplementation";

export class RunicAltarComponent extends Component {
	private readonly inventory: ItemContainerImplementation[] = [];

	constructor() {
		const grid = document.createElement("div");
		grid.classList.add("runic-altar-grid");
		super(grid);

		const slots = [
			{ name: "control_rune", image: "rune-control.png" },
			{ name: "utility_rune", image: "rune-utility.png" },
			{ name: "offensive_rune", image: "rune-offensive.png" },
			{ name: "special_rune", image: "rune-special.png" },
			{ name: "defensive_rune", image: "rune-defensive.png" },
			{ name: "healing_rune", image: "rune-healing.png" },
			{ name: "resistance_rune", image: "rune-resistance.png" },
		];

		for (const slot of slots) {
			const slotElement = document.createElement("div");
			slotElement.id = `${slot.name}0`;
			slotElement.classList.add("itemSlot");
			grid.appendChild(slotElement);

			this.inventory.push(
				new ItemContainerImplementation(
					grid,
					slot.name,
					1,
					null,
					"",
					false,
					slot.image,
				),
			);
		}
	}

	public update() {
		for (const slot of this.inventory) {
			slot.update();
		}
	}

	public markDirty() {
		for (const slot of this.inventory) {
			slot.markDirty();
		}
	}
}
