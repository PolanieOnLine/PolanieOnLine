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

import { marauroa } from "marauroa"
import { stendhal } from "../../stendhal";

import { InteractionButtonBase } from "./InteractionButtonBase";

import { TargetingController } from "../../game/TargetingController";
import { UiHandedness } from "../mobile/UiStateStore";
import { ItemContainerImplementation } from "../component/ItemContainerImplementation";
import { Item } from "../../entity/Item";

import { ElementClickListener } from "../../util/ElementClickListener";
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";


/**
 * Overlay loot button for quick pickup of nearby or open container items.
 */
export class LootButton extends InteractionButtonBase {

	private readonly cooldownDuration = 600;
	private cooldownId?: number;
	private handedness: UiHandedness = UiHandedness.RIGHT;

	constructor() {
		const element = InteractionButtonBase.createButton({
			id: "loot-button",
			classes: ["loot-button"],
			ariaLabel: "Podnieś łup",
			title: "Podnieś łup"
		});

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = (evt: Event) => this.onActivate(evt);

	}

	public override unmount() {
		super.unmount();
		this.setBusy(false);
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
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const separation = 12;
		const width = this.componentElement.offsetWidth || 32;
		const height = this.componentElement.offsetHeight || 32;
		const bounds = getViewportOverlayPosition({
			margin,
			elementWidth: width,
			elementHeight: height,
			offsetBottom: separation - margin,
			offsetRight: getMobileRightPanelCollapsedInset()
		});
		if (!bounds) {
			return;
		}

		let left = bounds.baseRight - width - margin;
		let top = bounds.baseBottom;

		if (attackButton) {
			const attackRect = attackButton.getBoundingClientRect();
			left = attackRect.left + bounds.scrollLeft - width - margin;
			top = attackRect.top + bounds.scrollTop;
		}

		const clampedLeft = Math.min(
			Math.max(left, bounds.safeLeft),
			bounds.safeRight < bounds.safeLeft ? bounds.safeLeft : bounds.safeRight
		);
		const clampedTop = Math.min(
			Math.max(top, bounds.safeTop),
			bounds.safeBottom < bounds.safeTop ? bounds.safeTop : bounds.safeBottom
		);

		this.componentElement.style.position = "absolute";
		this.componentElement.style.left = clampedLeft + "px";
		this.componentElement.style.top = clampedTop + "px";
	}
}
