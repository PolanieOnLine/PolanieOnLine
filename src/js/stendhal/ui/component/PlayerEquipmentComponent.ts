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
import { ItemContainerImplementation } from "./ItemContainerImplementation";
import { InventoryWindowController } from "./InventoryWindowController";

declare var marauroa: any;

/**
 * manage the equipment which is used by the player
 */
export class PlayerEquipmentComponent extends Component {

	private slotNames = ["neck", "head", "cloak", "lhand", "armor", "rhand", "finger", "pas", "legs", "glove", "fingerb", "feet", "pouch"];
	private slotSizes = [  1,      1,       1,      1,       1,	       1,       1,       1,      1,      1,       1,	    1,       1];
	private slotImages = ["slot-neck.png", "slot-helmet.png", "slot-cloak.png", "slot-shield.png", "slot-armor.png", "slot-weapon.png",
		"slot-ring.png", "slot-belt.png", "slot-legs.png", "slot-gloves.png", "slot-ringb.png", "slot-boots.png", "slot-pouch.png"];
	private readonly secondarySlotNames = this.slotNames.map((name) => name + "_set");
	private inventory: ItemContainerImplementation[] = [];

	private pouchVisible = false;
	private swapButton?: HTMLButtonElement;
	private reserveToggle?: HTMLButtonElement;
	private reserveWindow?: HTMLElement;
	private reserveVisible = false;
	private currentTitle = "";
	private readonly handleLayoutChange = () => this.refreshReserveWindowPosition();

	constructor() {
		super("equipment");
		this.inventory = [];
		for (let i = 0; i < this.slotNames.length; i++) {
			this.inventory.push(
				new ItemContainerImplementation(
					document, this.slotNames[i], this.slotSizes[i], null, "", false, this.slotImages[i]));
		}

		for (let i = 0; i < this.secondarySlotNames.length; i++) {
			this.inventory.push(
				new ItemContainerImplementation(
					document, this.secondarySlotNames[i], this.slotSizes[i], null, "", false, this.slotImages[i]));
		}

		const swapElement = document.getElementById("equipment-swap");
		if (swapElement instanceof HTMLButtonElement) {
			this.swapButton = swapElement;
			this.swapButton.addEventListener("click", () => this.swapSets());
		}

		const reserveToggleElement = document.getElementById("reserve-toggle");
		if (reserveToggleElement instanceof HTMLButtonElement) {
			this.reserveToggle = reserveToggleElement;
			this.reserveToggle.addEventListener("click", () => this.toggleReserveWindow());
		}
		const reserveWindowElement = document.getElementById("reserve-window");
		if (reserveWindowElement instanceof HTMLElement) {
			this.reserveWindow = reserveWindowElement;
		}
		this.updateReserveWindow(false);
		this.refreshReserveWindowPosition();

		window.addEventListener("resize", this.handleLayoutChange);
		const passiveOptions = { passive: true } as AddEventListenerOptions;
		window.addEventListener("scroll", this.handleLayoutChange, passiveOptions);
		const rightColumn = document.getElementById("rightColumn");
		if (rightColumn) {
			rightColumn.addEventListener("scroll", this.handleLayoutChange, passiveOptions);
		}

		// hide pouch by default
		this.showPouch(false);
	}

	public update() {
		for (var i in this.inventory) {
			this.inventory[i].update();
		}

		this.updateWindowTitle();

		if (!this.pouchVisible) {
			var features = null
			if (marauroa.me != null) {
				features = marauroa.me["features"];
			}

			if (features != null) {
				if (features["pouch"] != null) {
					this.showPouch(true);
				}
			}
		}
	}

	public markDirty() {
		for (const inv of this.inventory) {
			inv.markDirty();
		}
	}

	private showSlot(id: string, show: boolean) {
		var slot = document.getElementById(id)!;
		var prevState = slot.style.display;

		if (show === true) {
			slot.style.display = "block";
		} else {
			slot.style.display = "none";
		}

		return prevState != slot.style.display;
	}

	private showPouch(show: boolean) {
		const primaryChanged = this.showSlot("pouch0", show);
		const secondaryChanged = this.showSlot("pouch_set0", show);
		if (primaryChanged || secondaryChanged) {
			// resize the inventory window
			var equip = document.getElementById("equipment")!;
			if (show) {
				equip.style.height = "210px";
			} else {
				equip.style.height = "210px";
			}
			this.pouchVisible = show;
		}
	}

	private swapSets() {
		const action: any = { type: "setchange" };
		if (marauroa && marauroa.currentZoneName) {
			action.zone = marauroa.currentZoneName;
		}
		marauroa.clientFramework.sendAction(action);
	}

	private toggleReserveWindow() {
		this.updateReserveWindow(!this.reserveVisible);
	}

	private updateReserveWindow(show: boolean) {
		this.reserveVisible = show;

		this.refreshReserveWindowPosition();

		if (this.reserveWindow) {
			this.reserveWindow.classList.toggle("visible", show);
			this.reserveWindow.setAttribute("aria-hidden", (!show).toString());
		}

		if (this.reserveToggle) {
			this.reserveToggle.textContent = show ? ">" : "<";
			this.reserveToggle.setAttribute("aria-expanded", show.toString());
			this.reserveToggle.setAttribute("aria-label", show ? "Ukryj schowek" : "Pokaż schowek");
		}
	}
	
	private refreshReserveWindowPosition() {
		if (!this.reserveWindow) {
			return;
		}

		const anchor = document.getElementById("equipmentborder");
		if (!anchor) {
			return;
		}

		const anchorRect = anchor.getBoundingClientRect();
		const width = this.reserveWindow.offsetWidth || this.reserveWindow.scrollWidth;
		if (!width) {
			return;
		}

		const gap = 0;
		const left = Math.round(anchorRect.left - width - gap);
		const top = Math.round(anchorRect.top);

		this.reserveWindow.style.setProperty("--reserve-window-left", `${left}px`);
		this.reserveWindow.style.setProperty("--reserve-window-top", `${top}px`);
		this.reserveWindow.style.setProperty("--reserve-window-slide", `${width + gap}px`);
		this.reserveWindow.style.removeProperty("--reserve-window-height");
	}

	private updateWindowTitle() {
		const player = marauroa?.me;
		const title = player ? (player["_name"] || player["name"]) : "";
		if (title && title !== this.currentTitle) {
			InventoryWindowController.setTitle("equipmentborder", title);
			this.currentTitle = title;
		}
	}
}
