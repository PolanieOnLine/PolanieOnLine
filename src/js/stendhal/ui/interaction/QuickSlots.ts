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
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";
import { ActionContextMenu } from "../dialog/ActionContextMenu";
import { ui } from "../UI";
import { Item } from "../../entity/Item";
import { ItemContainerImplementation } from "../component/ItemContainerImplementation";
import { singletons } from "../../SingletonRepo";
import { Paths } from "../../data/Paths";
import { Chat } from "../../util/Chat";

type QuickSlotData = {
	targetPath: string;
	zone: string;
	item: Item;
};


/**
 * Quick slot buttons positioned near interaction controls.
 */
export class QuickSlots extends Component {

	private readonly boundUpdate: () => void;
	private resizeObserver?: ResizeObserver;
	private mutationObserver?: MutationObserver;
	private viewportObserver?: MutationObserver;
	private readonly slots: HTMLButtonElement[] = [];
	private readonly slotData = new Map<HTMLButtonElement, QuickSlotData>();
	private readonly slotCounts = new Map<HTMLButtonElement, HTMLElement>();
	private readonly containerId: string;
	private readonly allowedClasses = new Set(["potion", "drink", "food", "scroll"]);

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
			slot.addEventListener("dragover", (event: DragEvent) => {
				this.onDragOver(event);
			});
			slot.addEventListener("drop", (event: DragEvent) => {
				this.onDrop(event, slot);
			});
			slot.addEventListener("contextmenu", (event: MouseEvent) => {
				this.onContextMenu(event, slot);
			});
			slot.addEventListener("mouseup", (event: MouseEvent) => {
				this.onMouseUp(event, slot);
			});
			slot.addEventListener("touchstart", (event: TouchEvent) => {
				this.onTouchStart(event);
			}, { passive: true });
			slot.addEventListener("touchend", (event: TouchEvent) => {
				this.onTouchEnd(event, slot);
			});

			const count = document.createElement("span");
			count.classList.add("quick-slot__count");
			slot.appendChild(count);
			this.slotCounts.set(slot, count);

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

	private onDragOver(event: DragEvent) {
		if (!stendhal.ui.heldObject) {
			return;
		}
		event.preventDefault();
		if (event.dataTransfer) {
			event.dataTransfer.dropEffect = "copy";
		}
	}

	private onDrop(event: DragEvent|TouchEvent, slot: HTMLButtonElement) {
		if (!stendhal.ui.heldObject) {
			return;
		}
		event.preventDefault();
		event.stopPropagation();

		const heldObject = stendhal.ui.heldObject;
		const item = this.findInventoryItem(heldObject.path);
		stendhal.ui.heldObject = undefined;
		singletons.getHeldObjectManager().onRelease();

		if (!item) {
			Chat.log("warning", "Nie udało się znaleźć przedmiotu w ekwipunku.");
			return;
		}

		const itemClass = String(item["class"] || "").toLowerCase();
		if (!this.allowedClasses.has(itemClass)) {
			Chat.log("warning", "Ten przedmiot nie może trafić do szybkiego slotu.");
			return;
		}

		this.setSlotItem(slot, item, heldObject.path, heldObject.zone);
	}

	private onMouseUp(event: MouseEvent, slot: HTMLButtonElement) {
		if (stendhal.ui.touch.isTouchEngaged()) {
			return;
		}
		if (event.button !== 0) {
			return;
		}
		event.preventDefault();
		this.activateSlot(slot);
	}

	private onTouchStart(event: TouchEvent) {
		const pos = stendhal.ui.html.extractPosition(event);
		stendhal.ui.touch.onTouchStart(pos.pageX, pos.pageY);
	}

	private onTouchEnd(event: TouchEvent, slot: HTMLButtonElement) {
		stendhal.ui.touch.onTouchEnd(event);
		if (stendhal.ui.touch.isLongTouch(event) && !stendhal.ui.touch.holding()) {
			this.openContextMenu(slot, event);
		} else if (stendhal.ui.touch.holding()) {
			event.preventDefault();
			this.onDrop(event, slot);
			stendhal.ui.touch.setHolding(false);
		} else {
			this.activateSlot(slot);
		}
		stendhal.ui.touch.unsetOrigin();
	}

	private onContextMenu(event: MouseEvent, slot: HTMLButtonElement) {
		event.preventDefault();
		this.openContextMenu(slot, event);
	}

	private openContextMenu(slot: HTMLButtonElement, event: Event) {
		const data = this.slotData.get(slot);
		if (!data) {
			return;
		}
		const pos = stendhal.ui.html.extractPosition(event);
		const append = [{
			title: "Wyczyść slot",
			action: () => {
				this.clearSlot(slot);
			}
		}];
		stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Czynności",
			new ActionContextMenu(data.item, append),
			pos.pageX - 50, pos.pageY - 5));
	}

	private activateSlot(slot: HTMLButtonElement) {
		const data = this.slotData.get(slot);
		if (!data) {
			return;
		}

		marauroa.clientFramework.sendAction({
			type: "use",
			"target_path": data.targetPath,
			"zone": marauroa.currentZoneName || data.zone
		});
	}

	private setSlotItem(slot: HTMLButtonElement, item: Item, targetPath: string, zone: string) {
		this.slotData.set(slot, { targetPath, zone, item });
		this.updateSlotVisual(slot, item);
	}

	private clearSlot(slot: HTMLButtonElement) {
		this.slotData.delete(slot);
		slot.style.removeProperty("background-image");
		slot.style.removeProperty("background-position");
		slot.style.removeProperty("background-repeat");
		slot.style.removeProperty("background-size");
		const count = this.slotCounts.get(slot);
		if (count) {
			count.textContent = "";
		}
	}

	private updateSlotVisual(slot: HTMLButtonElement, item: Item) {
		const count = this.slotCounts.get(slot);
		if (count) {
			count.textContent = typeof item.formatQuantity === "function" ? item.formatQuantity() : "";
		}

		const animationFrame = typeof item.getAnimationFrameIndex === "function"
			? item.getAnimationFrameIndex()
			: 0;
		const xOffset = -(animationFrame * 32);
		const yOffset = (item["state"] || 0) * -32;
		const spritePath = singletons.getSpriteStore().checkPath(Paths.sprites
			+ "/items/" + item["class"] + "/" + item["subclass"] + ".png");
		slot.style.backgroundImage = `url(${spritePath}), url(${Paths.gui}/panel/empty_btn.png)`;
		slot.style.backgroundPosition = `${xOffset + 1}px ${yOffset + 1}px, center center`;
		slot.style.backgroundRepeat = "no-repeat, no-repeat";
		slot.style.backgroundSize = "32px 32px, contain";
	}

	private findInventoryItem(path: string): Item|undefined {
		const containers = stendhal.ui.equip.getInventory() as ItemContainerImplementation[];
		for (const container of containers) {
			const object = container.getObject() || marauroa.me;
			const slot = container.getSlot();
			const items = object?.[slot];
			if (!items || typeof items.count !== "function" || typeof items.getByIndex !== "function") {
				continue;
			}
			for (let i = 0; i < items.count(); i++) {
				const item = items.getByIndex(i) as Item;
				if (item && typeof item.getIdPath === "function" && item.getIdPath() === path) {
					return item;
				}
			}
		}
		return undefined;
	}
}
