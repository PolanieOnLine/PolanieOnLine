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
import { UiStateStore } from "../mobile/UiStateStore";
import { ElementClickListener } from "../../util/ElementClickListener";
import { getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";


/**
 * Toggle button for collapsing or expanding the left panel on touch devices.
 */
export class LeftPanelToggleButton extends Component {

	private readonly boundUpdate: () => void;
	private resizeObserver?: ResizeObserver;
	private mutationObserver?: MutationObserver;

	constructor() {
		const element = document.createElement("button");
		element.id = "left-panel-toggle";
		element.classList.add("unclickable", "hidden");
		element.setAttribute("aria-label", "Przełącz lewy panel");
		element.title = "Przełącz lewy panel";
		element.setAttribute("aria-pressed", "false");

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = () => UiStateStore.get().toggleLeftPanel();

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
		this.componentElement.classList.toggle("left-panel-toggle--active", expanded);
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
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const separation = 12;
		const width = this.componentElement.offsetWidth || 48;
		const height = this.componentElement.offsetHeight || 48;
		const bounds = getViewportOverlayPosition({
			margin,
			elementWidth: width,
			elementHeight: height,
			offsetBottom: separation - margin
		});
		if (!bounds) {
			return;
		}

		let anchorLeft = bounds.baseRight;
		let anchorTop = bounds.baseBottom;

		if (attackButton) {
			const attackRect = attackButton.getBoundingClientRect();
			anchorLeft = Math.max(attackRect.left + bounds.scrollLeft, bounds.baseRight);
			anchorTop = attackRect.top + bounds.scrollTop - height - separation;

			const minTop = bounds.rect.top + bounds.scrollTop + margin;
			if (anchorTop < minTop) {
				anchorTop = bounds.baseBottom;
			}
		}

		const left = anchorLeft - width - separation;
		const top = anchorTop;

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
