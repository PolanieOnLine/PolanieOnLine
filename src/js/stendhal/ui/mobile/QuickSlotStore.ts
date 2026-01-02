/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Item } from "../../entity/Item";

declare var stendhal: any;

export type QuickSlotAssignment = {
	itemId: number|string;
	slot: string;
	suffix: string;
};

export type ResolvedQuickSlot = {
	item: Item;
	element?: HTMLElement|null;
	container: QuickSlotContainer;
};

export interface QuickSlotContainer {
	getSlotName(): string;
	getSuffix(): string;
	findItemIndexById(itemId: number|string): number;
	getItemAt(index: number): Item|undefined;
	getElementForIndex(index: number): HTMLElement|null;
	useItem(item: Item): void;
	isOwnedByPlayer(): boolean;
}

/**
 * Stores quickslot bindings and resolves them against the current player inventory.
 */
export class QuickSlotStore {

	static readonly SLOT_COUNT = 3;
	private static readonly STORAGE_KEY = "ui.quickslots";
	private static instance: QuickSlotStore;

	private bindings: Array<QuickSlotAssignment|null>;

	static get(): QuickSlotStore {
		if (!QuickSlotStore.instance) {
			QuickSlotStore.instance = new QuickSlotStore();
		}
		return QuickSlotStore.instance;
	}

	private constructor() {
		this.bindings = this.loadBindings();
	}

	getAssignments(): Array<QuickSlotAssignment|null> {
		return [...this.bindings];
	}

	assign(slotIdx: number, item: Item, container: QuickSlotContainer) {
		if (!QuickSlotStore.isAllowedItem(item) || !container.isOwnedByPlayer()) {
			return;
		}
		if (slotIdx < 0 || slotIdx >= QuickSlotStore.SLOT_COUNT) {
			return;
		}

		const assignment: QuickSlotAssignment = {
			itemId: item["id"],
			slot: container.getSlotName(),
			suffix: container.getSuffix()
		};

		this.bindings[slotIdx] = assignment;
		this.persist();
	}

	clear(slotIdx: number) {
		if (slotIdx < 0 || slotIdx >= QuickSlotStore.SLOT_COUNT) {
			return;
		}
		if (this.bindings[slotIdx]) {
			this.bindings[slotIdx] = null;
			this.persist();
		}
	}

	resolveBindings(): Array<ResolvedQuickSlot|undefined> {
		const resolved: Array<ResolvedQuickSlot|undefined> = [];
		const containers = this.getContainers();
		let mutated = false;

		for (let i = 0; i < QuickSlotStore.SLOT_COUNT; i++) {
			const assignment = this.bindings[i];
			if (!assignment) {
				resolved.push(undefined);
				continue;
			}

			const container = containers.find((c) =>
				c.getSlotName() === assignment.slot
				&& c.getSuffix() === assignment.suffix
				&& c.isOwnedByPlayer());

			if (!container) {
				resolved.push(undefined);
				continue;
			}

			const itemIndex = container.findItemIndexById(assignment.itemId);
			if (itemIndex < 0) {
				this.bindings[i] = null;
				mutated = true;
				resolved.push(undefined);
				continue;
			}

			const item = container.getItemAt(itemIndex);
			if (!item || !QuickSlotStore.isAllowedItem(item)) {
				resolved.push(undefined);
				continue;
			}

			resolved.push({
				item,
				element: container.getElementForIndex(itemIndex),
				container
			});
		}

		if (mutated) {
			this.persist();
		}

		return resolved;
	}

	static isAllowedItem(item: Item): boolean {
		const cls = (item["class"] || "").toLowerCase();
		return cls === "potion" || cls === "food" || cls === "drink" || cls === "scroll";
	}

	private loadBindings(): Array<QuickSlotAssignment|null> {
		const raw = stendhal.config.get(QuickSlotStore.STORAGE_KEY);
		try {
			const parsed = JSON.parse(raw || "[]");
			const filled: Array<QuickSlotAssignment|null> = new Array(QuickSlotStore.SLOT_COUNT).fill(null);
			for (let i = 0; i < Math.min(parsed.length, QuickSlotStore.SLOT_COUNT); i++) {
				const value = parsed[i];
				if (value && typeof value.itemId !== "undefined" && value.slot && value.suffix) {
					filled[i] = value as QuickSlotAssignment;
				}
			}
			return filled;
		} catch (err) {
			console.warn("failed to parse quickslot bindings", err);
			return new Array(QuickSlotStore.SLOT_COUNT).fill(null);
		}
	}

	private getContainers(): QuickSlotContainer[] {
		if (!stendhal.ui?.equip?.getInventory) {
			return [];
		}
		return stendhal.ui.equip.getInventory()
			.filter((c: any): c is QuickSlotContainer => typeof c.getSlotName === "function" && typeof c.findItemIndexById === "function");
	}

	private persist() {
		stendhal.config.set(QuickSlotStore.STORAGE_KEY, JSON.stringify(this.bindings));
	}
}
