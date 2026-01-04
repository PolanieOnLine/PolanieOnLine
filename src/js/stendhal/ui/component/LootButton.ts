/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var marauroa: any;
declare var stendhal: any;

import { Component } from "../toolkit/Component";

import { TargetingController } from "../../game/TargetingController";
import { UiHandedness } from "../mobile/UiStateStore";
import { ItemContainerImplementation } from "./ItemContainerImplementation";
import { Item } from "../../entity/Item";

import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Overlay loot button for quick pickup of nearby or open container items.
 */
export class LootButton extends Component {

	private readonly cooldownDuration = 600;
	private cooldownId?: number;
	private handedness: UiHandedness = UiHandedness.RIGHT;
	private readonly boundUpdate: () => void;

	constructor() {
		const element = document.createElement("button");
		element.id = "loot-button";
		element.classList.add("loot-button", "unclickable", "hidden");
		element.setAttribute("aria-label", "Podnieś łup");
		element.title = "Podnieś łup";

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = (evt: Event) => this.onActivate(evt);

		this.boundUpdate = this.update.bind(this);
	}

	public mount() {
		const container = document.getElementById("attack-button-container") || document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		this.componentElement.classList.remove("hidden");

		this.update();
		window.addEventListener("resize", this.boundUpdate);
	}

	public unmount() {
		if (this.componentElement.parentElement) {
			this.componentElement.remove();
		}
		this.componentElement.classList.add("hidden");
		this.setBusy(false);
		window.removeEventListener("resize", this.boundUpdate);
		this.clearCooldown();
	}

	public setBusy(busy: boolean) {
		this.componentElement.classList.toggle("loot-button--disabled", busy);
	}

	private onActivate(_evt: Event) {
		if (this.cooldownId) {
			return;
		}
		if (this.tryLootOpenContainer()) {
			this.startCooldown();
			return;
		}
		const target = TargetingController.get().pickupNearest();
		if (target) {
			this.startCooldown();
		} else {
			this.flashDisabled();
		}
	}

	private tryLootOpenContainer(): boolean {
		const containers = stendhal.ui.equip.getInventory() as ItemContainerImplementation[];
		for (const container of containers) {
			if (container.getSlot() !== "content") {
				continue;
			}
			const object = container.getObject();
			if (!object || !this.canViewContents(object)) {
				continue;
			}
			if (object.inventory && typeof object.inventory.isOpen === "function" && !object.inventory.isOpen()) {
				continue;
			}

			const items = this.getContainerItems(object, container.getSlot());
			if (!items.length) {
				continue;
			}

			for (const item of items) {
				marauroa.clientFramework.sendAction({
					type: "equip",
					"source_path": item.getIdPath(),
					"target_path": "[" + marauroa.me["id"] + "\tbag]",
					"clicked": "",
					"zone": marauroa.currentZoneName
				});
			}
			return true;
		}
		return false;
	}

	private canViewContents(object: any): boolean {
		if (typeof object?.getDistanceTo !== "function" || !marauroa.me) {
			return false;
		}
		if (object && typeof object.canViewContents === "function") {
			return object.canViewContents();
		}
		const maxDist = typeof object?.maxDistToView === "number" ? object.maxDistToView : 4;
		return object.getDistanceTo(marauroa.me) <= maxDist;
	}

	private getContainerItems(object: any, slot: string): Item[] {
		const container = object?.[slot];
		if (!container || typeof container.count !== "function" || typeof container.getByIndex !== "function") {
			return [];
		}

		const items: Item[] = [];
		for (let i = 0; i < container.count(); i++) {
			const item = container.getByIndex(i) as Item;
			if (item && typeof item.getIdPath === "function") {
				items.push(item);
			}
		}
		return items;
	}

	public setHandedness(handedness: UiHandedness) {
		if (this.handedness === handedness) {
			return;
		}
		this.handedness = handedness;
		this.update();
	}

	public flashDisabled() {
		this.setBusy(true);
		window.setTimeout(() => this.setBusy(false), 300);
	}

	private startCooldown() {
		this.setBusy(true);
		this.cooldownId = window.setTimeout(() => {
			this.setBusy(false);
			this.cooldownId = undefined;
		}, this.cooldownDuration);
	}

	private clearCooldown() {
		if (this.cooldownId) {
			window.clearTimeout(this.cooldownId);
			this.cooldownId = undefined;
		}
	}

	/**
	 * Positions loot button adjacent to attack button placement.
	 */
	public update(): void {
		const viewport = document.getElementById("viewport");
		const attackButton = document.getElementById("attack-button");
		const margin = 20;
		const width = this.componentElement.offsetWidth || 32;
		const height = this.componentElement.offsetHeight || 32;

		let left = margin;
		let top = margin;

		if (viewport) {
			const rect = viewport.getBoundingClientRect();
			if (attackButton) {
				const attackRect = attackButton.getBoundingClientRect();
				left = attackRect.left - width - margin;
				top = attackRect.top;
			} else {
				left = rect.right - width * 2 - margin * 2;
				top = rect.bottom - height - margin;
			}
		}

		this.componentElement.style.position = "fixed";
		this.componentElement.style.left = left + "px";
		this.componentElement.style.top = top + "px";
	}
}
