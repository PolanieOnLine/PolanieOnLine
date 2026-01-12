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
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";


/**
 * Quick slot buttons positioned near interaction controls.
 */
export class QuickSlots extends Component {

	private readonly boundUpdate: () => void;
	private resizeObserver?: ResizeObserver;
	private mutationObserver?: MutationObserver;
	private viewportObserver?: MutationObserver;
	private readonly slots: HTMLButtonElement[] = [];
	private readonly containerId: string;

	constructor(containerId = "interaction-button-container") {
		const element = document.createElement("div");
		element.id = "quick-slots";
		element.classList.add("quick-slots", "hidden");
		super(element);

		this.containerId = containerId;
		this.boundUpdate = this.update.bind(this);
		this.buildSlots();
	}

	public mount() {
		const container = document.getElementById(this.containerId) || document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		this.componentElement.classList.remove("hidden");

		this.update();
		window.addEventListener("resize", this.boundUpdate);
		window.addEventListener("scroll", this.boundUpdate);
		this.observeLayoutChanges();
	}

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
		this.viewportObserver?.disconnect();
		this.viewportObserver = undefined;
	}

	private buildSlots() {
		for (let i = 0; i < 3; i++) {
			const slot = document.createElement("button");
			slot.type = "button";
			slot.classList.add("quick-slot");
			slot.setAttribute("aria-label", `Szybki slot ${i + 1}`);
			slot.title = `Szybki slot ${i + 1}`;

			const count = document.createElement("span");
			count.classList.add("quick-slot__count");
			slot.appendChild(count);

			this.componentElement.appendChild(slot);
			this.slots.push(slot);
		}
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
		if (viewport && !this.viewportObserver) {
			this.viewportObserver = new MutationObserver(() => this.update());
			this.viewportObserver.observe(viewport, {
				attributes: true,
				attributeFilter: ["left", "right", "top", "bottom", "width", "height"]
			});
		}
	}

	public update(): void {
		if (!this.slots.length) {
			return;
		}

		const reference = document.getElementById("attack-button")
			|| document.getElementById("right-panel-toggle")
			|| document.getElementById("loot-button");
		const firstSlot = this.slots[0];
		const width = firstSlot.offsetWidth || 32;
		const height = firstSlot.offsetHeight || 32;
		const bounds = getViewportOverlayPosition({
			margin: 16,
			elementWidth: width,
			elementHeight: height,
			offsetBottom: 0,
			offsetRight: getMobileRightPanelCollapsedInset()
		});
		if (!bounds) {
			return;
		}

		let anchorX = bounds.baseRight + width / 2;
		let anchorY = bounds.baseBottom + height / 2;

		if (reference) {
			const rect = reference.getBoundingClientRect();
			anchorX = rect.left + bounds.scrollLeft + rect.width / 2;
			anchorY = rect.top + bounds.scrollTop + rect.height / 2;
		}

		const verticalOffset = height + 12;
		anchorY -= verticalOffset;

		const baseRadius = Math.max(width, height) + 28;
		const radiusStep = 8;
		const startAngle = -150;
		const angleStep = 30;

		this.slots.forEach((slot, index) => {
			const radius = baseRadius + radiusStep * index;
			const angle = (startAngle + angleStep * index) * (Math.PI / 180);
			const left = anchorX + Math.cos(angle) * radius - width / 2;
			const top = anchorY + Math.sin(angle) * radius - height / 2;

			const clampedLeft = Math.min(
				Math.max(left, bounds.safeLeft),
				bounds.safeRight < bounds.safeLeft ? bounds.safeLeft : bounds.safeRight
			);
			const clampedTop = Math.min(
				Math.max(top, bounds.safeTop),
				bounds.safeBottom < bounds.safeTop ? bounds.safeTop : bounds.safeBottom
			);

			slot.style.left = clampedLeft + "px";
			slot.style.top = clampedTop + "px";
		});
	}
}
