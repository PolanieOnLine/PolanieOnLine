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
	private resizeObserver?: ResizeObserver;
	private mutationObserver?: MutationObserver;

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
		window.addEventListener("scroll", this.boundUpdate);
		this.observeLayoutChanges();
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
		window.removeEventListener("scroll", this.boundUpdate);
		this.resizeObserver?.disconnect();
		this.resizeObserver = undefined;
		this.mutationObserver?.disconnect();
		this.mutationObserver = undefined;
	}

	public setExpanded(expanded: boolean) {
		this.componentElement.classList.toggle("right-panel-toggle--active", expanded);
		this.componentElement.setAttribute("aria-pressed", expanded.toString());
	}

	private observeLayoutChanges() {
		if (this.resizeObserver || typeof ResizeObserver === "undefined") {
			return;
		}
		const viewport = document.getElementById("viewport");
		const clientRoot = document.getElementById("client");
		if (!viewport && !clientRoot) {
			return;
		}
		if (viewport) {
			this.resizeObserver = new ResizeObserver(() => this.update());
			this.resizeObserver.observe(viewport);
		}
		if (clientRoot) {
			this.mutationObserver = new MutationObserver(() => this.update());
			this.mutationObserver.observe(clientRoot, { attributes: true, attributeFilter: ["class"] });
		}
	}

	/**
	 * Positions the toggle near the attack button when possible.
	 */
	public update(): void {
		const viewport = document.getElementById("viewport");
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const separation = 12;
		const width = this.componentElement.offsetWidth || 48;
		const height = this.componentElement.offsetHeight || 48;
		const scrollLeft = window.scrollX || document.documentElement.scrollLeft;
		const scrollTop = window.scrollY || document.documentElement.scrollTop;

		let left = margin + scrollLeft;
		let top = margin + scrollTop;
		let safeLeft = left;
		let safeTop = top;
		let safeRight = scrollLeft + document.documentElement.clientWidth - width - margin;
		let safeBottom = scrollTop + document.documentElement.clientHeight - height - margin;

		if (viewport) {
			const rect = viewport.getBoundingClientRect();
			const fallbackLeft = rect.right + scrollLeft - width - margin;
			const fallbackTop = rect.bottom + scrollTop - height - separation;
			const safeBoundsLeft = rect.left + scrollLeft + margin;
			const safeBoundsTop = rect.top + scrollTop + margin;
			const safeBoundsRight = rect.right + scrollLeft - width - margin;
			const safeBoundsBottom = rect.bottom + scrollTop - height - margin;

			if (attackButton) {
				const attackRect = attackButton.getBoundingClientRect();
				left = attackRect.left + scrollLeft;
				top = attackRect.top + scrollTop - height - separation;

				const minTop = rect.top + scrollTop + margin;
				if (top < minTop) {
					top = fallbackTop;
				}
			} else {
				left = fallbackLeft;
				top = fallbackTop;
			}

			safeLeft = safeBoundsLeft;
			safeTop = safeBoundsTop;
			safeRight = safeBoundsRight;
			safeBottom = safeBoundsBottom;
		}

		const clampedLeft = Math.min(Math.max(left, safeLeft), safeRight < safeLeft ? safeLeft : safeRight);
		const clampedTop = Math.min(Math.max(top, safeTop), safeBottom < safeTop ? safeTop : safeBottom);

		this.componentElement.style.position = "absolute";
		this.componentElement.style.left = clampedLeft + "px";
		this.componentElement.style.top = clampedTop + "px";
	}
}
