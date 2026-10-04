/***************************************************************************
 *                (C) Copyright 2003-2024 - Faiumoni e. V.                 *
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

import { ui } from "../UI";

import { ActionContextMenu } from "../dialog/ActionContextMenu";
import { DropQuantitySelectorDialog } from "../dialog/DropQuantitySelectorDialog";
import { ItemUpgradeDialog } from "../dialog/ItemUpgradeDialog";
import { Item } from "../../entity/Item";

import { singletons } from "../../SingletonRepo";

import { Point } from "../../util/Point";
import { Paths } from "../../data/Paths";
import { ItemRarity } from "../../data/ItemRarity";
import {
	buildStructuredItemTooltip,
	hasStructuredItemTooltip,
	ItemTooltipDelta,
	ItemTooltipLine
} from "./ItemTooltipPresentation";

interface InventorySlotView {
	element: HTMLElement;
	item?: Item;
	spritePath?: string;
	backgroundImage?: string;
	backgroundPosition?: string;
	quantity?: string;
	toolTip?: string;
	displayName?: string;
	rarity?: ItemRarity;
	tooltipStats?: string;
	animated: boolean;
	animationPending: boolean;
	visible: boolean;
}


/**
 * a container for items like a bag or corpse
 */
export class ItemContainerImplementation {
	private static rarityToolTip?: HTMLElement;
	private static rarityToolTipTarget?: HTMLElement;

	private rightClickDuration = 300;
	private timestampMouseDown = 0;
	private timestampMouseDownPrev = 0;
	private lastClickedId = "";

	// marked for updating certain attributes
	private dirty = false;
	private slots: InventorySlotView[] = [];
	private animationSlots: InventorySlotView[] = [];
	private nextAnimationTime = 0;
	private renderedObject: any;
	private slotVisibilityObserver?: IntersectionObserver;


	// TODO: replace usage of global document.getElementById()

	/**
	 * slot name, slot size, object (a corpse or chest) or null for marauroa.me,
	 * which changes on zone change.
	 */
	constructor(private parentElement: Document|HTMLElement, private slot: string, private size: number, public object: any, private suffix: string, private quickPickup: boolean, private defaultImage?: string) {
		this.init(size);
	}

	public getSlot(): string {
		return this.slot;
	}

	public getObject(): any {
		return this.object;
	}

	public init(size: number) {
		this.slotVisibilityObserver?.disconnect();
		this.slots = [];
		this.animationSlots = [];
		this.nextAnimationTime = 0;
		this.dirty = true;
		if (typeof IntersectionObserver !== "undefined") {
			this.slotVisibilityObserver = new IntersectionObserver(entries => {
				for (const entry of entries) {
					const slot = this.slots.find(view => view?.element === entry.target);
					if (slot) {
						slot.visible = entry.isIntersecting;
					}
				}
			});
		}
		this.size = size;
		for (let i = 0; i < size; i++) {
			let e = this.parentElement.querySelector("#" + this.slot + this.suffix + i) as HTMLElement;
			if (!e) {
				continue;
			}
			this.slots[i] = {element: e, animated: false, animationPending: false, visible: true};
			this.slotVisibilityObserver?.observe(e);
			e.setAttribute("draggable", "true");
			e.style.touchAction = "none";
			e.addEventListener("dragstart", (event: DragEvent) => {
				this.onDragStart(event)
			});
			e.addEventListener("dragover", (event: DragEvent) => {
				this.onDragOver(event)
			});
			e.addEventListener("touchmove", (event: TouchEvent) => {
				this.onTouchMove(event);
			}, {passive: false});
			e.addEventListener("drop", (event: DragEvent) => {
				this.onDrop(event)
			});
			e.addEventListener("mousedown", (event: MouseEvent) => {
				this.onMouseDown(event)
			});
			e.addEventListener("mouseup", (event: MouseEvent) => {
				this.onMouseUp(event)
			});
			e.addEventListener("touchstart", (event: TouchEvent) => {
				this.onTouchStart(event)
			}, {passive: true});
			e.addEventListener("touchend", (event: TouchEvent) => {
				this.onTouchEnd(event)
			});
			e.addEventListener("contextmenu", (event: MouseEvent) => {
				this.onContextMenu(event);
			});
			e.addEventListener("mouseenter", (event: MouseEvent) => {
				this.onMouseEnter(event);
			});
			e.addEventListener("mouseleave", (event: MouseEvent) => {
				this.onMouseLeave(event);
			});
		}
		this.update();
	}

	/**
	 * Retrieves the inventory component element associated with this container.
	 *
	 * @return {HTMLElement}
	 *   Parent element.
	 */
	public getParentElement(): HTMLElement {
		return this.parentElement as HTMLElement;
	}

	/**
	 * Marks items to update cursors & tooltips.
	 */
	public markDirty() {
		this.dirty = true;
	}

	public update() {
		this.render();
	}

	/** Advances icon frames without scanning the live inventory on every world frame. */
	public animate(now = Date.now()) {
		if (now < this.nextAnimationTime) {
			return;
		}
		this.nextAnimationTime = now + 100;
		if (!this.isAnimationVisible()) {
			return;
		}
		if (this.dirty || this.renderedObject !== (this.object || marauroa.me)) {
			this.render();
		}
		for (let i = this.animationSlots.length - 1; i >= 0; i--) {
			const view = this.animationSlots[i];
			const item = view.item!;
			if (!view.visible) {
				continue;
			}
			if (view.animationPending) {
				this.updateImage(view);
				view.animated = item.isAnimated();
				view.animationPending = !singletons.getSpriteStore().get(item.sprite.filename).height;
				if (!view.animated) {
					if (!view.animationPending) {
						this.animationSlots.splice(i, 1);
					}
					continue;
				}
			}
			item.stepAnimation(now);
			this.updatePosition(view, item);
		}
	}

	public dispose() {
		this.slotVisibilityObserver?.disconnect();
		for (const view of this.slots) {
			if (view) {
				ItemContainerImplementation.hideRarityToolTip(view.element);
			}
		}
	}

	private isAnimationVisible(): boolean {
		const element = this.parentElement.nodeType === 9
			? this.slots.find(view => !!view)?.element : this.parentElement as HTMLElement;
		if (!element || document.visibilityState === "hidden"
				|| element.closest("[hidden], [aria-hidden='true']")) {
			return false;
		}
		if (typeof element.checkVisibility === "function") {
			return element.checkVisibility({checkOpacity: true, checkVisibilityCSS: true});
		}
		return element.getClientRects().length > 0 && getComputedStyle(element).visibility !== "hidden";
	}

	private updatePosition(view: InventorySlotView, item: Item) {
		const position = (1 - item.getXFrameIndex() * 32) + "px "
			+ (1 - (item["state"] || 0) * 32) + "px";
		if (view.backgroundPosition !== position) {
			view.element.style.backgroundPosition = position;
			view.backgroundPosition = position;
		}
	}

	private updateImage(view: InventorySlotView) {
		const image = "url(" + singletons.getSpriteStore().checkPath(view.spritePath!) + "), none";
		if (view.backgroundImage !== image) {
			view.element.style.backgroundImage = image;
			view.backgroundImage = image;
		}
	}

	private findItem(predicate: (item: Item) => boolean): Item | undefined {
		const myobject = this.object || marauroa.me;
		const items = myobject?.[this.slot];
		if (!items || typeof items.count !== "function" || typeof items.getByIndex !== "function") {
			return undefined;
		}
		for (let i = 0; i < items.count(); i++) {
			const item = items.getByIndex(i) as Item;
			if (item && predicate(item)) {
				return item;
			}
		}
		return undefined;
	}

	public findItemByTargetPath(targetPath: string): Item|undefined {
		return this.findItem(item => typeof item.getIdPath === "function" && item.getIdPath() === targetPath);
	}

	public findItemById(itemId: string | number): Item|undefined {
		return this.findItem(item => item["id"] === itemId);
	}

	public render() {
		const myobject = this.object || marauroa.me;
		const items = myobject?.[this.slot];
		const count = Math.min(items?.count() || 0, this.size);
		this.animationSlots = [];
		for (let i = 0; i < this.size; i++) {
			const view = this.slots[i];
			if (!view) {
				continue;
			}
			const item = i < count ? items.getByIndex(i) as Item : undefined;
			const changed = this.dirty || view.item !== item || view.backgroundImage === undefined;
			const element = view.element;
			if (item) {
				const path = Paths.sprites + "/items/" + item["class"] + "/" + item["subclass"] + ".png";
				view.spritePath = path;
				// A failed image can switch to the store's fallback without an item change.
				this.updateImage(view);
				this.updatePosition(view, item);
				const quantity = String(item.formatQuantity());
				if (view.quantity !== quantity) {
					element.textContent = quantity;
					view.quantity = quantity;
				}
				const toolTip = item.getToolTip();
				const displayName = item.getDisplayName();
				const rarity = item.getRarity();
				// Maps can be mutated in place by a perception. Never fingerprint them per frame.
				const tooltipStats = JSON.stringify(item["tooltip_stats"]);
				const cursor = this.slot === "content" && stendhal.config.getBoolean("inventory.quick-pickup")
					? "url(" + Paths.sprites + "/cursor/itempickupfromslot.png) 1 3, auto" : item.getCursor(0, 0);
				if (changed || element.style.cursor !== cursor) {
					this.updateCursor(element, item);
				}
				if (changed || view.toolTip !== toolTip || view.rarity !== rarity
						|| view.tooltipStats !== tooltipStats || view.displayName !== displayName) {
					this.updateToolTip(element, item);
					view.toolTip = toolTip;
					view.rarity = rarity;
					view.tooltipStats = tooltipStats;
					view.displayName = displayName;
				}
				view.animated = item.isAnimated();
				view.animationPending = !view.animated && !singletons.getSpriteStore().get(item.sprite.filename).height;
				if (view.animated || view.animationPending) {
					this.animationSlots.push(view);
				}
			} else {
				const image = this.defaultImage ? "url(" + Paths.gui + "/" + this.defaultImage + "), none" : "none, none";
				if (view.backgroundImage !== image) {
					element.style.backgroundImage = image;
					view.backgroundImage = image;
				}
				if (view.quantity !== "") {
					element.textContent = "";
					view.quantity = "";
				}
				if (view.backgroundPosition !== "1px 1px") {
					element.style.backgroundPosition = "1px 1px";
					view.backgroundPosition = "1px 1px";
				}
				if (changed) {
					this.updateCursor(element);
					this.updateToolTip(element);
				}
				view.spritePath = undefined;
				view.toolTip = undefined;
				view.displayName = undefined;
				view.rarity = undefined;
				view.tooltipStats = undefined;
				view.animated = false;
				view.animationPending = false;
			}
			view.item = item;
			(element as any).dataItem = item;
		}
		this.renderedObject = myobject;
		this.dirty = false;
	}

	private onDragStart(event: DragEvent|TouchEvent) {
		let myobject = this.object || marauroa.me;
		// some mobile browsers such as Chrome call "dragstart" via long touch
		if (!myobject[this.slot] || (event.type === "dragstart" && stendhal.ui.touch.isTouchEngaged())) {
			event.preventDefault();
			return;
		}

		let target
		if (event instanceof DragEvent) {
			target = (event.target as HTMLElement);
		} else {
			// touch event
			const touch = event.touches[0] || event.targetTouches[0] || event.changedTouches[0];
			target = (touch.target as HTMLElement);
		}

		const slotNumber = target.id.slice(this.slot.length + this.suffix.length);
		let item = myobject[this.slot].getByIndex(slotNumber);
		if (item) {
			const heldObject = {
				path: item.getIdPath(),
				zone: marauroa.currentZoneName,
				slot: this.slot,
				quantity: item.hasOwnProperty("quantity") ? item["quantity"] : 1
			};

			const img = singletons.getSpriteStore().getAreaOf(singletons.getSpriteStore().get(item.sprite.filename), 32, 32);
			if (event instanceof DragEvent && event.dataTransfer) {
				stendhal.ui.heldObject = heldObject;
				event.dataTransfer.setDragImage(img, 0, 0);
			} else if (stendhal.ui.touch.isTouchEvent(event)) {
				stendhal.ui.touch.setHolding(true);
				// TODO: move when supported by mouse events
				const pos = stendhal.ui.html.extractPosition(event);
				singletons.getHeldObjectManager().set(heldObject, img, new Point(pos.pageX, pos.pageY));
			}
		} else {
			event.preventDefault();
		}
	}

	/**
	 * Handles displaying an icon for objects dragged with touch.
	 */
	private onTouchMove(event: TouchEvent) {
		if (event.cancelable) {
			event.preventDefault();
		}
		if (stendhal.ui.heldObject) {
			return;
		}
		this.onDragStart(event);
	}

	private onDragOver(event: DragEvent|TouchEvent) {
		event.preventDefault();
		if (event instanceof DragEvent && event.dataTransfer) {
			event.dataTransfer.dropEffect = "move";
		}
		return false;
	}

	/**
	 * Extracts slot index from element ID.
	 */
	private parseIndex(id: string): string|undefined {
		// NOTE: element ID is formatted as "<name>-<id>-<index>"
		//       - name:  inventory name (e.g. "bag")
		//       - id:    inventory ID number
		//       - index: inventory index of this element
		// See `ui.component.ItemInventoryComponent.ItemInventoryComponent`
		if (id.includes("-")) {
			const tmp = id.split("-");
			const idx = tmp[tmp.length - 1];
			if (!isNaN(parseInt(idx, 10))) {
				return idx;
			}
		}
	}

	private onDrop(event: DragEvent|TouchEvent) {
		const myobject = this.object || marauroa.me;
		if (stendhal.ui.heldObject) {
			const pos = stendhal.ui.html.extractPosition(event);
			const id = (pos.target as HTMLElement).id;
			const targetSlot = stendhal.ui.html.parseSlotName(id);
			if (event.type === "touchend" && id === "viewport") {
				stendhal.ui.gamewindow.onDrop(event);
				event.stopPropagation();
				event.preventDefault();
				return;
			}

			let objectId = myobject["id"];
			if (event.type === "touchend") {
				// find the actual target ID for touch events
				if (targetSlot === "content") {
					const container = stendhal.ui.equip.getByElement(stendhal.ui.html.extractTarget(event).parentElement!);
					if (container && container.object) {
						objectId = container.object.id;
					}
				} else {
					// moving from content container (corpse, chest) to player container (bag, keyring, etc.)
					objectId = marauroa.me["id"];
				}
			}

			const action = {
				"source_path": stendhal.ui.heldObject.path
			} as any;
			const sameSlot = stendhal.ui.heldObject.slot === targetSlot;
			if (sameSlot) {
				action["type"] = "reorder";
				action["new_position"] = this.parseIndex(id) || "" + (this.size - 1);
			} else {
				action["type"] = "equip";
				action["target_path"] = "[" + objectId + "\t" + targetSlot + "]";
				action["zone"] = stendhal.ui.heldObject.zone;
			}

			const quantity = stendhal.ui.heldObject.quantity;
			stendhal.ui.heldObject = undefined;

			// if ctrl is pressed or holding stackable item from touch event, we ask for the quantity
			const touch_held = stendhal.ui.touch.holding() && quantity > 1;
			const split_action = !sameSlot && ((event instanceof DragEvent && event.ctrlKey) || touch_held);
			if (split_action) {
				const pos = stendhal.ui.html.extractPosition(event);
				ui.createSingletonFloatingWindow("Ilość",
					new DropQuantitySelectorDialog(action, touch_held),
					pos.pageX - 50, pos.pageY - 25);
			} else {
				singletons.getHeldObjectManager().onRelease();
				marauroa.clientFramework.sendAction(action);
			}
		}

		event.stopPropagation();
		event.preventDefault();
	}

	private onContextMenu(event: MouseEvent) {
		event.preventDefault();
	}

	isRightClick(event: MouseEvent) {
		if (event.which) {
			return (event.which === 3);
		} else {
			return (event.button === 2);
		}
	}

	isDoubleClick(evt: MouseEvent) {
		if (this.timestampMouseDown - this.timestampMouseDownPrev <= this.rightClickDuration) {
			// reset so subsequent single clicks/taps aren't counted
			this.timestampMouseDown = 0;
			return (stendhal.ui.html.extractTarget(evt) as HTMLElement).id === this.lastClickedId;
		}
		this.lastClickedId = (stendhal.ui.html.extractTarget(evt) as HTMLElement).id;
		return false;
	}

	onMouseDown(evt: MouseEvent|TouchEvent) {
		this.timestampMouseDownPrev = this.timestampMouseDown;
		this.timestampMouseDown = +new Date();
	}

	onMouseUp(evt: MouseEvent|TouchEvent) {
		if (evt instanceof MouseEvent) {
			evt.preventDefault();
		}

		// workaround to prevent accidentally using items when disengaging joystick
		// FIXME: a global solution would be better
		if (singletons.getJoystickController().isEngaged()) {
			return;
		}

		let event = stendhal.ui.html.extractPosition(evt);
		if ((event.target as any).dataItem) {
			const long_touch = stendhal.ui.touch.isLongTouch(evt);
			const context_action = (evt instanceof MouseEvent && this.isRightClick(evt)) || long_touch;
			if (this.quickPickup && !context_action) {
				marauroa.clientFramework.sendAction({
					type: "equip",
					"source_path": (event.target as any).dataItem.getIdPath(),
					"target_path": "[" + marauroa.me["id"] + "\tbag]",
					"clicked": "", // useful for changing default target in equip action
					"zone": marauroa.currentZoneName
				});
				return;
			}

			if (this.isRightClick(event) || long_touch) {
				const append = [];
				const selectedItem = (event.target as any).dataItem as Item;
				if (ItemUpgradeDialog.canSelectItem(selectedItem)) {
					append.push({
						title: "Ulepsz przedmiot",
						action: (entity: Item) => {
							ItemUpgradeDialog.selectItemForUpgrade(entity);
						}
					});
				}
				if (long_touch) {
					// XXX: better way to pass instance to action function?
					const tmp = this;
					// action to "hold" item for moving or dropping using touch
					// XXX: temporary workaround, should use drag-and-drop instead
					append.push({
						title: "Przytrzymaj",
						action: function(entity: any) {
							tmp.onDragStart(evt as TouchEvent);
						}
					});
				}
				stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Czynności",
					new ActionContextMenu((event.target as any).dataItem, append),
					event.pageX - 50, event.pageY - 5));
			} else if (!stendhal.ui.heldObject) {
				if (!stendhal.config.getBoolean("inventory.double-click") || this.isDoubleClick(event)) {
					marauroa.clientFramework.sendAction({
						type: "use",
						"target_path": (event.target as any).dataItem.getIdPath(),
						"zone": marauroa.currentZoneName
					});
				}
			}
		}

		// clean up item held via touch
		stendhal.ui.touch.setHolding(false);

		document.getElementById("viewport")!.focus();
	}

	private onMouseEnter(evt: MouseEvent) {
		const target = evt.currentTarget as HTMLElement;
		const item = (target as any).dataItem as Item|undefined;
		if (item && (item.getRarity() || hasStructuredItemTooltip(item))) {
			ItemContainerImplementation.showRarityToolTip(target, item, evt.clientX, evt.clientY);
		}
	}

	private onMouseLeave(evt: MouseEvent) {
		const target = evt.currentTarget as HTMLElement;
		ItemContainerImplementation.hideRarityToolTip(target);
	}

	private onTouchStart(evt: TouchEvent) {
		const pos = stendhal.ui.html.extractPosition(evt);
		stendhal.ui.touch.onTouchStart(pos.pageX, pos.pageY);
	}

	private onTouchEnd(evt: TouchEvent) {
		stendhal.ui.touch.onTouchEnd();
		if (stendhal.ui.touch.holding()) {
			if (evt.cancelable) {
				evt.preventDefault();
			}
			this.onDrop(evt);
			stendhal.ui.touch.setHolding(false);
		} else {
			// Treat a tap like a normal click and a long press like a context click.
			this.onMouseUp(evt);
		}
		// clean up touch handler
		stendhal.ui.touch.unsetOrigin();
	}

	/**
	 * Updates cursor to display for targeted item.
	 *
	 * @param target
	 *     HTMLElement representing item.
	 * @param item
	 *     Object containing item information.
	 */
	private updateCursor(target: HTMLElement, item?: Item) {
		ItemContainerImplementation.updateCursorFor(target, item, this.slot);
	}

	/**
	 * Sets tooltip to be shown for item.
	 *
	 * @param target
	 *     HTMLElement representing item.
	 * @param item
	 *     Object containing item information.
	 */
	private updateToolTip(target: HTMLElement, item?: Item) {
		ItemContainerImplementation.updateToolTipFor(target, item);
	}

	public static updateCursorFor(target: HTMLElement, item?: Item, slot?: string) {
		if (item) {
			if (slot === "content" && stendhal.config.getBoolean("inventory.quick-pickup")) {
				target.style.cursor = "url(" + Paths.sprites
						+ "/cursor/itempickupfromslot.png) 1 3, auto";
				return;
			}
			target.style.cursor = item.getCursor(0, 0);
			return;
		}
		target.style.cursor = "url(" + Paths.sprites
				+ "/cursor/normal.png) 1 3, auto";
	}

	public static updateToolTipFor(target: HTMLElement, item?: Item) {
		for (const rarity of ItemRarity.VALUES) {
			target.classList.remove(rarity.cssClass);
		}
		target.classList.remove("item-rarity");

		const toolTip = item?.getToolTip() || "";
		if (toolTip) {
			target.title = toolTip;
			target.setAttribute("aria-label", toolTip);
		} else {
			target.removeAttribute("title");
			target.removeAttribute("aria-label");
		}

		const rarity = item?.getRarity();
		if (rarity) {
			target.classList.add("item-rarity", rarity.cssClass);
		}

		if (ItemContainerImplementation.rarityToolTipTarget === target) {
			// The slot may have been re-rendered while the pointer was over it.
			// Keep the attributes just calculated for the new item.
			ItemContainerImplementation.hideRarityToolTip(target, false);
		}
	}

	private static showRarityToolTip(target: HTMLElement, item: Item, x: number, y: number) {
		const rarity = item.getRarity();
		const hasStructured = hasStructuredItemTooltip(item);
		const structured = buildStructuredItemTooltip(item,
				singletons.getConfigManager().getBoolean("item-tooltip.comparison"));
		if (!rarity && structured.lines.length === 0 && !structured.upgradeText) {
			return;
		}

		ItemContainerImplementation.hideRarityToolTip();
		target.removeAttribute("title");

		const toolTip = document.createElement("div");
		toolTip.id = "item-rarity-tooltip";
		toolTip.classList.add("item-rarity-tooltip");
		if (rarity) {
			toolTip.classList.add(rarity.cssClass);
		} else {
			toolTip.style.setProperty("--item-rarity-color", "#a37861");
		}
		toolTip.setAttribute("role", "tooltip");

		const name = document.createElement("div");
		name.className = "item-rarity-tooltip__name";
		name.textContent = (item.getDisplayName() + (structured.titleSuffix || "")).toUpperCase();
		toolTip.appendChild(name);

		if (rarity) {
			const rarityLine = document.createElement("div");
			rarityLine.className = "item-rarity-tooltip__rarity";
			rarityLine.textContent = rarity.polishDisplayName;
			toolTip.appendChild(rarityLine);
		}

		if (structured.upgradeText) {
			const upgrade = document.createElement("div");
			upgrade.className = "item-rarity-tooltip__upgrade";
			upgrade.textContent = "◆ " + structured.upgradeText;
			toolTip.appendChild(upgrade);
		}

		if (structured.comparisonName) {
			const comparison = document.createElement("div");
			comparison.className = "item-rarity-tooltip__comparison";
			comparison.textContent = "Porównanie z: " + structured.comparisonName;
			toolTip.appendChild(comparison);
		}

		for (const line of structured.lines) {
			ItemContainerImplementation.appendStructuredTooltipLine(toolTip, line);
		}

		// Legacy servers do not publish tooltip_stats, so preserve their plain text.
		if (!hasStructured) {
			const rarityText = rarity ? "Rzadkość: " + rarity.polishDisplayName : "";
			for (const line of item.getToolTip().split("\n")) {
				if (!line || line === item.getDisplayName() || line === rarityText) {
					continue;
				}
				const detail = document.createElement("div");
				detail.className = "item-rarity-tooltip__detail";
				detail.textContent = line;
				toolTip.appendChild(detail);
			}
		}

		document.body.appendChild(toolTip);
		const margin = 8;
		const left = Math.min(x + 12, window.innerWidth - toolTip.offsetWidth - margin);
		const top = Math.min(y + 12, window.innerHeight - toolTip.offsetHeight - margin);
		toolTip.style.left = Math.max(margin, left) + "px";
		toolTip.style.top = Math.max(margin, top) + "px";

		target.setAttribute("aria-describedby", toolTip.id);
		ItemContainerImplementation.rarityToolTip = toolTip;
		ItemContainerImplementation.rarityToolTipTarget = target;
	}

	private static appendStructuredTooltipLine(toolTip: HTMLElement, line: ItemTooltipLine) {
		if (line.kind === "divider") {
			const divider = document.createElement("div");
			divider.className = "item-rarity-tooltip__divider";
			divider.textContent = "────◇◇────";
			toolTip.appendChild(divider);
			return;
		}

		const detail = document.createElement("div");
		if (line.kind === "tree") {
			detail.className = "item-rarity-tooltip__tree";
			const prefix = document.createElement("span");
			prefix.className = "item-rarity-tooltip__tree-prefix";
			prefix.textContent = line.branchContinues ? "├─◆ " : "└─◆ ";
			detail.appendChild(prefix);
			const value = document.createElement("span");
			value.className = "item-rarity-tooltip__tree-value";
			value.appendChild(document.createTextNode(line.text));
			ItemContainerImplementation.appendTooltipDeltas(value, line.deltas);
			detail.appendChild(value);
			toolTip.appendChild(detail);
			return;
		}

		switch (line.kind) {
		case "primary":
			detail.className = "item-rarity-tooltip__primary";
			break;
		case "bonus":
			detail.className = "item-rarity-tooltip__bonus";
			detail.appendChild(document.createTextNode("◆ "));
			break;
		case "affix":
			detail.className = "item-rarity-tooltip__affix";
			break;
		case "footer":
			detail.className = "item-rarity-tooltip__footer";
			break;
		default:
			detail.className = "item-rarity-tooltip__detail";
			break;
		}
		detail.appendChild(document.createTextNode(line.text));
		ItemContainerImplementation.appendTooltipDeltas(detail, line.deltas);
		toolTip.appendChild(detail);
	}

	private static appendTooltipDeltas(target: HTMLElement, deltas?: ItemTooltipDelta[]) {
		if (!deltas?.length) {
			return;
		}
		target.appendChild(document.createTextNode(" ("));
		deltas.forEach((delta, index) => {
			if (index > 0) {
				target.appendChild(document.createTextNode("–"));
			}
			const value = document.createElement("span");
			value.className = "item-tooltip-delta item-tooltip-delta--" + delta.direction;
			value.textContent = delta.text;
			target.appendChild(value);
		});
		target.appendChild(document.createTextNode(")"));
	}

	private static hideRarityToolTip(target?: HTMLElement, restoreTitle = true) {
		if (target && ItemContainerImplementation.rarityToolTipTarget !== target) {
			return;
		}

		ItemContainerImplementation.rarityToolTip?.remove();
		if (ItemContainerImplementation.rarityToolTipTarget) {
			const oldTarget = ItemContainerImplementation.rarityToolTipTarget;
			oldTarget.removeAttribute("aria-describedby");
			if (restoreTitle) {
				const item = (oldTarget as any).dataItem as Item|undefined;
				const toolTip = item?.getToolTip() || "";
				if (toolTip) {
					oldTarget.title = toolTip;
				} else {
					oldTarget.removeAttribute("title");
				}
			}
		}
		ItemContainerImplementation.rarityToolTip = undefined;
		ItemContainerImplementation.rarityToolTipTarget = undefined;
	}
}
