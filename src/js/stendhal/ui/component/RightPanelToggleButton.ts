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

import { Component } from "../toolkit/Component";
import { UiStateStore } from "../mobile/UiStateStore";
import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Toggle button for collapsing or expanding the right panel on touch devices.
 */
export class RightPanelToggleButton extends Component {

	private readonly boundUpdate: () => void;

	constructor() {
		const element = document.createElement("button");
		element.id = "right-panel-toggle";
		element.classList.add("unclickable", "hidden");
		element.setAttribute("aria-label", "Przełącz prawy panel");
		element.title = "Przełącz prawy panel";
		element.setAttribute("aria-pressed", "false");

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = () => UiStateStore.get().toggleRightPanel();

		this.boundUpdate = this.update.bind(this);
	}

	/**
	 * Adds the toggle button to the DOM.
	 */
	public mount() {
		const container = document.getElementById("attack-button-container") || document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		this.componentElement.classList.remove("hidden");

		this.update();
		window.addEventListener("resize", this.boundUpdate);
	}

	/**
	 * Removes the toggle button from DOM.
	 */
	public unmount() {
		if (this.componentElement.parentElement) {
			this.componentElement.remove();
		}
		this.componentElement.classList.add("hidden");
		window.removeEventListener("resize", this.boundUpdate);
	}

	public setExpanded(expanded: boolean) {
		this.componentElement.classList.toggle("right-panel-toggle--active", expanded);
		this.componentElement.setAttribute("aria-pressed", expanded.toString());
	}

	/**
	 * Positions the toggle near the attack button when possible.
	 */
	public update(): void {
		const viewport = document.getElementById("viewport");
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const separation = 8;
		const width = this.componentElement.offsetWidth || 48;
		const height = this.componentElement.offsetHeight || 48;

		let left = margin;
		let top = margin;

		if (attackButton) {
			const rect = attackButton.getBoundingClientRect();
			left = rect.left;
			top = rect.top - height - separation;
			if (top < margin && viewport) {
				const vpRect = viewport.getBoundingClientRect();
				left = vpRect.right - width - margin;
				top = vpRect.top + margin;
			}
		} else if (viewport) {
			const rect = viewport.getBoundingClientRect();
			left = rect.right - width - margin;
			top = rect.bottom - height * 2 - separation;
		}

		this.componentElement.style.position = "fixed";
		this.componentElement.style.left = left + "px";
		this.componentElement.style.top = top + "px";
	}
}
