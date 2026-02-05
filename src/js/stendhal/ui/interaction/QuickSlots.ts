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

var marauroa = (window as any).marauroa = (window as any).marauroa || {};
import { stendhal } from "../../stendhal";

import { Component } from "../toolkit/Component";
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";
import { ActionContextMenu } from "../dialog/ActionContextMenu";
import { ui } from "../UI";
import { Item } from "../../entity/Item";
import { ItemContainerImplementation } from "../component/ItemContainerImplementation";
import { singletons } from "../../SingletonRepo";
import { Paths } from "../../data/Paths";
import { Chat } from "../../util/Chat";
import { ConfigManager } from "../../util/ConfigManager";

type QuickSlotData = {
	targetPath: string;
	zone: string;
	item?: Item;
	itemId?: string | number;
};

type QuickSlotAssignment = {
	targetPath: string;
	zone: string;
	itemId?: string | number;
};

const QUICK_SLOT_CONFIG_KEY = "quickslots.assignments";


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
	private assignments: Array<QuickSlotAssignment | null> = [];
	private readonly containerId: string;
	private readonly allowedClasses = new Set(["potion", "drink", "food", "scroll"]);
	private lastActivation = 0;

	constructor(containerId = "interaction-button-container") {
		const element = document.createElement("div");
		element.id = "quick-slots";
		element.classList.add("quick-slots", "hidden");
		super(element);

		this.containerId = containerId;
		this.boundUpdate = this.update.bind(this);
		this.buildSlots();
		this.loadAssignments();
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

		this.refreshSlots();

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

		const horizontalOffset = -12;
		anchorX += horizontalOffset;

		const verticalOffset = height + 2;
		anchorY -= verticalOffset;

		const baseRadius = Math.max(width, height) + 44;
		const startAngle = -135;
		const angleStep = 30;

		this.slots.forEach((slot, index) => {
			const angle = (startAngle + angleStep * index) * (Math.PI / 180);
			const left = anchorX + Math.cos(angle) * baseRadius - width / 2;
			const top = anchorY + Math.sin(angle) * baseRadius - height / 2;

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
		const item = this.findItemByTargetPath(heldObject.path);
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
		if (!data.item) {
			const item = this.findItemByAssignment(data);
			if (!item) {
				this.clearSlot(slot);
				return;
			}
			const latestPath = typeof item.getIdPath === "function" ? item.getIdPath() : data.targetPath;
			const itemId = item["id"];
			this.slotData.set(slot, { ...data, item, targetPath: latestPath, itemId });
			if (latestPath !== data.targetPath || itemId !== data.itemId) {
				this.setAssignmentForSlot(slot, { targetPath: latestPath, zone: data.zone, itemId });
				this.persistAssignments();
			}
		}
		const now = Date.now();
		if (now - this.lastActivation < 250) {
			return;
		}
		this.lastActivation = now;

		marauroa.clientFramework.sendAction({
			type: "use",
			"target_path": this.slotData.get(slot)?.targetPath ?? data.targetPath,
			"zone": marauroa.currentZoneName || data.zone
		});
	}

	private setSlotItem(slot: HTMLButtonElement, item: Item, targetPath: string, zone: string) {
		const itemId = item["id"];
		this.slotData.set(slot, { targetPath, zone, item, itemId });
		this.setAssignmentForSlot(slot, { targetPath, zone, itemId });
		this.persistAssignments();
		this.updateSlotVisual(slot, item);
	}

	private clearSlot(slot: HTMLButtonElement) {
		this.slotData.delete(slot);
		this.setAssignmentForSlot(slot, null);
		this.persistAssignments();
		this.setEmptySlotVisual(slot);
	}

	private updateSlotVisual(slot: HTMLButtonElement, item: Item) {
		const count = this.slotCounts.get(slot);
		if (count) {
			count.textContent = typeof item.formatQuantity === "function" ? item.formatQuantity() : "";
		}
		const tileSize = 32;
		const isAnimated = typeof item.isAnimated === "function" ? item.isAnimated() : false;
		if (!isAnimated) {
			const atlasKey = `${item["class"]}/${item["subclass"]}/${item["state"] || 0}`;
			const atlas = singletons.getSpriteStore().getItemIconAtlas([{
				id: atlasKey,
				filename: item.sprite.filename,
				sourceX: 0,
				sourceY: (item["state"] || 0) * tileSize,
				sourceWidth: tileSize,
				sourceHeight: tileSize
			}], tileSize);
			if (atlas) {
				const atlasPosition = atlas.positions.get(atlasKey);
				if (atlasPosition) {
					slot.style.backgroundImage = `url(${atlas.dataUrl}), url(${Paths.gui}/panel/empty_btn.png)`;
					slot.style.backgroundPosition = `${-(atlasPosition.x) + 1}px ${-(atlasPosition.y) + 1}px, center center`;
					slot.style.backgroundRepeat = "no-repeat, no-repeat";
					slot.style.backgroundSize = "auto, contain";
					ItemContainerImplementation.updateCursorFor(slot, item);
					ItemContainerImplementation.updateToolTipFor(slot, item);
					return;
				}
			}
		}

		let xOffset = 0;
		if (isAnimated) {
			item.stepAnimation();
			xOffset = -(item.getXFrameIndex() * tileSize);
		}
		const yOffset = (item["state"] || 0) * -tileSize;
		const spritePath = singletons.getSpriteStore().checkPath(Paths.sprites
			+ "/items/" + item["class"] + "/" + item["subclass"] + ".png");
		slot.style.backgroundImage = `url(${spritePath}), url(${Paths.gui}/panel/empty_btn.png)`;
		slot.style.backgroundPosition = `${xOffset + 1}px ${yOffset + 1}px, center center`;
		slot.style.backgroundRepeat = "no-repeat, no-repeat";
		slot.style.backgroundSize = `${tileSize}px ${tileSize}px, contain`;
		ItemContainerImplementation.updateCursorFor(slot, item);
		ItemContainerImplementation.updateToolTipFor(slot, item);
	}

	private setEmptySlotVisual(slot: HTMLButtonElement) {
		slot.style.backgroundImage = `url(${Paths.gui}/panel/empty_btn.png)`;
		slot.style.backgroundPosition = "center center";
		slot.style.backgroundRepeat = "no-repeat";
		slot.style.backgroundSize = "contain";
		const count = this.slotCounts.get(slot);
		if (count) {
			count.textContent = "";
		}
		ItemContainerImplementation.updateCursorFor(slot);
		ItemContainerImplementation.updateToolTipFor(slot);
	}

	private refreshSlots() {
		const containers = stendhal.ui.equip.getInventory() as ItemContainerImplementation[];
		const itemById = new Map<string | number, Item>();
		const itemByTargetPath = new Map<string, Item>();

		for (const container of containers) {
			const containerObject = container.getObject() || marauroa.me;
			const slot = container.getSlot();
			const items = containerObject?.[slot];
			if (!items || typeof items.count !== "function" || typeof items.getByIndex !== "function") {
				continue;
			}
			for (let i = 0; i < items.count(); i++) {
				const item = items.getByIndex(i) as Item;
				if (!item) {
					continue;
				}
				const itemId = item["id"];
				if (itemId !== undefined) {
					itemById.set(itemId, item);
				}
				if (typeof item.getIdPath === "function") {
					const targetPath = item.getIdPath();
					if (targetPath) {
						itemByTargetPath.set(targetPath, item);
					}
				}
			}
		}

		const resolveItem = (data: QuickSlotAssignment): Item | undefined => {
			if (data.itemId !== undefined) {
				const item = itemById.get(data.itemId);
				if (item) {
					return item;
				}
			}
			return itemByTargetPath.get(data.targetPath);
		};

		let assignmentsUpdated = false;
		for (const slot of this.slots) {
			const data = this.slotData.get(slot);
			if (!data) {
				this.setEmptySlotVisual(slot);
				continue;
			}
			const item = resolveItem(data);
			if (!item) {
				this.slotData.set(slot, { ...data, item: undefined });
				this.setEmptySlotVisual(slot);
				continue;
			}
			const latestPath = typeof item.getIdPath === "function" ? item.getIdPath() : data.targetPath;
			const itemId = item["id"];
			this.slotData.set(slot, { ...data, item, targetPath: latestPath, itemId });
			if (latestPath !== data.targetPath || itemId !== data.itemId) {
				this.setAssignmentForSlot(slot, { targetPath: latestPath, zone: data.zone, itemId });
				assignmentsUpdated = true;
			}
			this.updateSlotVisual(slot, item);
		}
		if (assignmentsUpdated) {
			this.persistAssignments();
		}
	}

	private findItemByTargetPath(path: string): Item|undefined {
		const containers = stendhal.ui.equip.getInventory() as ItemContainerImplementation[];
		for (const container of containers) {
			const item = container.findItemByTargetPath(path);
			if (item) {
				return item;
			}
		}
		return undefined;
	}

	private findItemByAssignment(data: QuickSlotAssignment): Item|undefined {
		if (data.itemId !== undefined) {
			const itemById = this.findItemById(data.itemId);
			if (itemById) {
				return itemById;
			}
		}
		return this.findItemByTargetPath(data.targetPath);
	}

	private findItemById(itemId: string | number): Item|undefined {
		const containers = stendhal.ui.equip.getInventory() as ItemContainerImplementation[];
		for (const container of containers) {
			const item = container.findItemById(itemId);
			if (item) {
				return item;
			}
		}
		return undefined;
	}

	private loadAssignments() {
		const config = ConfigManager.get();
		const raw = config.get(QUICK_SLOT_CONFIG_KEY);
		const parsed = this.parseAssignments(raw);
		if (!parsed) {
			this.assignments = [null, null, null];
			config.set(QUICK_SLOT_CONFIG_KEY, JSON.stringify(this.assignments));
		} else {
			this.assignments = parsed.assignments;
			if (parsed.sanitized) {
				config.set(QUICK_SLOT_CONFIG_KEY, JSON.stringify(this.assignments));
			}
		}
		this.assignments.forEach((assignment, index) => {
			const slot = this.slots[index];
			if (!slot || !assignment) {
				return;
			}
			this.slotData.set(slot, { ...assignment });
		});
		this.refreshSlots();
	}

	private parseAssignments(raw: unknown): { assignments: Array<QuickSlotAssignment | null>; sanitized: boolean } | null {
		if (typeof raw !== "string") {
			return null;
		}
		try {
			const parsed = JSON.parse(raw);
			if (!Array.isArray(parsed) || parsed.length !== this.slots.length) {
				return null;
			}
			let sanitized = false;
			const assignments = parsed.map((entry) => {
				if (entry === null) {
					return null;
				}
				if (typeof entry !== "object" || entry === null) {
					sanitized = true;
					return null;
				}
				const targetPath = (entry as QuickSlotAssignment).targetPath;
				const zone = (entry as QuickSlotAssignment).zone;
				const itemId = (entry as QuickSlotAssignment).itemId;
				if (typeof targetPath !== "string" || typeof zone !== "string") {
					sanitized = true;
					return null;
				}
				if (itemId !== undefined && typeof itemId !== "string" && typeof itemId !== "number") {
					sanitized = true;
					return { targetPath, zone };
				}
				return itemId === undefined ? { targetPath, zone } : { targetPath, zone, itemId };
			});
			return { assignments, sanitized };
		} catch (error) {
			return null;
		}
	}

	private setAssignmentForSlot(slot: HTMLButtonElement, assignment: QuickSlotAssignment | null) {
		const index = this.slots.indexOf(slot);
		if (index === -1) {
			return;
		}
		if (!this.assignments.length) {
			this.assignments = Array.from({ length: this.slots.length }, () => null);
		}
		this.assignments[index] = assignment;
	}

	private persistAssignments() {
		if (!this.assignments.length) {
			this.assignments = Array.from({ length: this.slots.length }, () => null);
		}
		ConfigManager.get().set(QUICK_SLOT_CONFIG_KEY, JSON.stringify(this.assignments));
	}
}
