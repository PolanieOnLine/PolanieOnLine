/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { marauroa } from "marauroa";
import type { Chest } from "../entity/Chest";

import { ItemContainerImplementation } from "./component/ItemContainerImplementation";


export class Inventory {

	private inventory: ItemContainerImplementation[] = [];
	private activeChests: {chest: Chest; player: any; zone: string}[] = [];

	/** Called only by genuine chest windows when opened or focused. */
	activateChest(chest: Chest) {
		this.activeChests = this.activeChests.filter(entry => entry.chest !== chest);
		this.activeChests.push({chest, player: marauroa.me, zone: marauroa.currentZoneName});
	}

	/** Revalidate at the moment of input; a world sprite alone is never a target. */
	getQuickTransferTarget(): string|undefined {
		this.activeChests = this.activeChests.filter(({chest, player, zone}) => {
			if (!player || player !== marauroa.me || zone !== marauroa.currentZoneName
					|| marauroa.currentZone?.[chest["id"]] !== chest || !chest.open
					|| !chest.inventory?.isOpen() || chest.getDistanceTo(player) > 4) {
				return false;
			}
			return this.inventory.some(container => container.getObject() === chest && container.getSlot() === "content");
		});
		for (let i = this.activeChests.length - 1; i >= 0; i--) {
			const chest = this.activeChests[i].chest;
			const container = this.inventory.find(view => view.getObject() === chest && view.getSlot() === "content")!;
			if (!chest.inventory?.isMinimized() && container.isVisible()) {
				return chest.getIdPath().slice(0, -1) + "\tcontent]";
			}
		}
		return undefined;
	}

	/** Singleton instance. */
	private static instance: Inventory;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): Inventory {
		if (!Inventory.instance) {
			Inventory.instance = new Inventory();
		}
		return Inventory.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	update() {
		for (var i in this.inventory) {
			this.inventory[i].update();
		}
	}

	animate() {
		const now = Date.now();
		for (const inventory of this.inventory) {
			inventory.animate(now);
		}
	}

	getInventory(): ItemContainerImplementation[] {
		return this.inventory;
	}

	/**
	 * Retrieves an inventory item container.
	 *
	 * @param element {string}
	 *   HTML element associated with container.
	 * @return {ItemContainerImplementation}
	 *   Container with matching element or `undefined`.
	 */
	getByElement(element: HTMLElement): ItemContainerImplementation|undefined {
		for (const container of this.inventory) {
			if (element === container.getParentElement()) {
				return container;
			}
		}
	}

	add(comp: ItemContainerImplementation) {
		this.inventory.push(comp);
	}

	remove(comp: ItemContainerImplementation) {
		const idx = this.inventory.indexOf(comp);
		if (idx > -1) {
			comp.dispose();
			this.inventory.splice(idx, 1);
		}
	}

	removeIndex(idx: number) {
		if (idx > -1 && idx < this.inventory.length) {
			this.inventory[idx].dispose();
			this.inventory.splice(idx, 1);
		}
	}

	indexOf(comp: ItemContainerImplementation): number {
		return this.inventory.indexOf(comp);
	}
}

