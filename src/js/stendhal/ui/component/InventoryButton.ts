/***************************************************************************
 *                     Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Component } from "../toolkit/Component";
import { toggleRightPanel } from "../mobile/PanelDock";
import { UiHandedness } from "../mobile/UiStateStore";
import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Overlay inventory button for toggling the right column on mobile.
 */
export class InventoryButton extends Component {

	private handedness: UiHandedness = UiHandedness.RIGHT;
	private readonly boundUpdate: () => void;

	constructor() {
		const element = document.createElement("button");
		element.id = "inventory-button";
		element.classList.add("inventory-button", "unclickable", "hidden");
		element.setAttribute("aria-label", "Pokaż prawy panel");
		element.title = "Pokaż prawy panel";
		element.setAttribute("aria-pressed", "false");

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
		window.removeEventListener("resize", this.boundUpdate);
	}

	private onActivate(_evt: Event) {
		toggleRightPanel();
	}

	public setHandedness(handedness: UiHandedness) {
		if (this.handedness === handedness) {
			return;
		}
		this.handedness = handedness;
		this.update();
	}

	public setPanelVisibility(visible: boolean) {
		const label = visible ? "Schowaj prawy panel" : "Pokaż prawy panel";
		this.componentElement.classList.toggle("inventory-button--active", visible);
		this.componentElement.setAttribute("aria-pressed", visible.toString());
		this.componentElement.setAttribute("aria-label", label);
		this.componentElement.title = label;
	}

	/**
	 * Positions button near the attack/loot buttons based on handedness.
	 */
	public update(): void {
		const viewport = document.getElementById("viewport");
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const width = this.componentElement.offsetWidth || 48;
		const height = this.componentElement.offsetHeight || 48;

		let left = margin;
		let top = margin;

		if (viewport) {
			const rect = viewport.getBoundingClientRect();
			const anchor = attackButton?.getBoundingClientRect() || rect;

			if (this.handedness === UiHandedness.RIGHT) {
				left = anchor.left;
				top = anchor.top - height - margin;
				if (top < rect.top + margin) {
					top = rect.top + margin;
				}
			} else {
				left = rect.left + margin;
				top = rect.bottom - height - margin;
			}
		}

		this.componentElement.style.position = "fixed";
		this.componentElement.style.left = left + "px";
		this.componentElement.style.top = top + "px";
	}
}
