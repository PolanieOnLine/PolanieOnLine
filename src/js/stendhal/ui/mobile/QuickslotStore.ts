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

declare var marauroa: any;

import { ConfigManager } from "../../util/ConfigManager";


export interface QuickslotEntry {
	slot: number;
	targetPath: string;
	label: string;
	icon?: string;
	itemClass?: string;
	itemSubclass?: string;
	state?: number;
}

type QuickslotSubscriber = (entries: QuickslotEntry[]) => void;


/**
 * Stores quickslot assignments and notifies listeners when they change.
 */
export class QuickslotStore {

	private static instance: QuickslotStore;

	private readonly config = ConfigManager.get();
	private readonly entries: Map<number, QuickslotEntry> = new Map();
	private readonly subscribers: Set<QuickslotSubscriber> = new Set();
	private readonly allowedClasses = new Set(["drink", "food", "scroll", "potion"]);


	static get(): QuickslotStore {
		if (!QuickslotStore.instance) {
			QuickslotStore.instance = new QuickslotStore();
		}
		return QuickslotStore.instance;
	}

	private constructor() {
		this.load();
	}

	subscribe(cb: QuickslotSubscriber): () => void {
		this.subscribers.add(cb);
		cb(this.getEntries());
		return () => this.subscribers.delete(cb);
	}

	assign(slot: number, entity: any) {
		if (!entity || typeof entity.getIdPath !== "function") {
			return;
		}
		if (entity["class"] && !this.allowedClasses.has(entity["class"])) {
			return;
		}

		const targetPath = entity.getIdPath();
		const label = entity["name"] || entity["_name"] || `Slot ${slot}`;
		const icon = entity.sprite?.filename;
		const itemClass = entity["class"];
		const itemSubclass = entity["subclass"];
		const state = entity["state"];

		const entry: QuickslotEntry = { slot, targetPath, label, icon, itemClass, itemSubclass, state };
		this.entries.set(slot, entry);
		this.saveEntry(entry);
		this.notify();
	}

	clear(slot: number) {
		this.entries.delete(slot);
		this.removeEntry(slot);
		this.notify();
	}

	trigger(slot: number): boolean {
		const entry = this.entries.get(slot);
		if (!entry || !marauroa || !marauroa.clientFramework) {
			return false;
		}

		marauroa.clientFramework.sendAction({
			type: "use",
			target_path: entry.targetPath,
			zone: marauroa.currentZoneName
		});
		return true;
	}

	getEntry(slot: number): QuickslotEntry|undefined {
		return this.entries.get(slot);
	}

	getEntries(): QuickslotEntry[] {
		return Array.from(this.entries.values()).sort((a, b) => a.slot - b.slot);
	}

	private notify() {
		const entries = this.getEntries();
		for (const cb of this.subscribers) {
			cb(entries);
		}
	}

	private getKey(slot: number): string {
		return `ui.quickslot.${slot}`;
	}

	private load() {
		for (let slot = 1; slot <= 3; slot++) {
			const raw = this.config.get(this.getKey(slot));
			if (!raw) {
				continue;
			}
			try {
				const parsed = JSON.parse(raw);
					if (parsed && parsed.targetPath) {
						this.entries.set(slot, {
							slot,
							targetPath: parsed.targetPath,
							label: parsed.label || `Slot ${slot}`,
							icon: parsed.icon,
							itemClass: parsed.itemClass,
							itemSubclass: parsed.itemSubclass,
							state: parsed.state
						});
					}
			} catch (e) {
				// ignore malformed entries
				console.warn("Unable to parse quickslot entry", e);
			}
		}
	}

	private saveEntry(entry: QuickslotEntry) {
		this.config.set(this.getKey(entry.slot), JSON.stringify({
			targetPath: entry.targetPath,
			label: entry.label,
			icon: entry.icon,
			itemClass: entry.itemClass,
			itemSubclass: entry.itemSubclass,
			state: entry.state
		}));
	}

	private removeEntry(slot: number) {
		this.config.remove(this.getKey(slot));
	}
}
