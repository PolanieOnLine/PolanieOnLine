/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
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


import { TargetFilter, TargetingController } from "../../game/TargetingController";
import { Item } from "../../entity/Item";
import { NPC } from "../../entity/NPC";
import { SessionManager } from "../../util/SessionManager";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";


type QuickSlotBinding = {
	item: Item;
	element?: HTMLElement|null;
};


/**
 * Floating radial dock for common mobile actions (attack, interact, pickup & quickslots).
 */
export class ActionDock {

	private static instance?: ActionDock;

	private readonly store = UiStateStore.get();

	private readonly greetings = ["Cześć", "Witaj", "Dzień dobry"];
	private readonly farewells = ["Do widzenia", "Bywaj", "Żegnaj"];

	private readonly container: HTMLDivElement;
	private readonly attackButton: HTMLButtonElement;
	private readonly interactButton: HTMLButtonElement;
	private readonly pickupButton: HTMLButtonElement;
	private readonly quickSlotButtons: HTMLButtonElement[] = [];

	private readonly interactionHistory: Map<number|string, boolean> = new Map();
	private readonly scheduleRepositionBound = () => this.scheduleReposition();

	private unsubscribe?: () => void;
	private mounted = false;
	private repositionId?: number;
	private updateIntervalId?: number;
	private handedness: UiHandedness = UiHandedness.RIGHT;

	private constructor() {
		this.container = document.createElement("div");
		this.container.className = "action-dock hidden";
		this.container.setAttribute("aria-hidden", "true");

		this.attackButton = this.buildButton("action-dock__button--attack", "Atakuj", () => this.onAttackPressStart(), () => this.onAttackPressEnd(), true);
		this.interactButton = this.buildButton("action-dock__button--interact", "Interakcja", () => this.onInteract());
		this.pickupButton = this.buildButton("action-dock__button--pickup", "Podnieś", () => this.onPickup());

		for (let i = 0; i < 3; i++) {
			const quick = this.buildButton("action-dock__button--quick", "Szybki przedmiot " + (i + 1), () => this.onQuickSlot(i));
			quick.dataset.slot = (i + 1).toString();
			quick.classList.add("action-dock__button--quick-" + (i + 1));
			this.quickSlotButtons.push(quick);
		}

		this.container.appendChild(this.attackButton);
		this.container.appendChild(this.interactButton);
		this.container.appendChild(this.pickupButton);
		for (const quick of this.quickSlotButtons) {
			this.container.appendChild(quick);
		}
	}

	static get(): ActionDock {
		if (!ActionDock.instance) {
			ActionDock.instance = new ActionDock();
		}
		return ActionDock.instance;
	}

	/**
	 * Adds the dock to the DOM for touch-only devices.
	 */
	public mount() {
		if (this.mounted || !SessionManager.get().touchOnly()) {
			return;
		}
		this.mounted = true;

		const host = document.getElementById("action-dock-container") || document.body;
		host.appendChild(this.container);

		this.unsubscribe = this.store.subscribe((state) => this.applyUiState(state));
		this.applyUiState(this.store.getState());

		window.addEventListener("resize", this.scheduleRepositionBound);
		window.addEventListener("touchmove", this.scheduleRepositionBound, { passive: true });

		this.scheduleReposition();
		this.refreshTargets();
		this.updateIntervalId = window.setInterval(() => this.refreshTargets(), 400);
		this.container.classList.remove("hidden");
		this.container.setAttribute("aria-hidden", "false");
	}

	public destroy() {
		if (!this.mounted) {
			return;
		}

		this.mounted = false;
		this.unsubscribe?.();
		this.unsubscribe = undefined;
		window.removeEventListener("resize", this.scheduleRepositionBound);
		window.removeEventListener("touchmove", this.scheduleRepositionBound);
		if (this.repositionId) {
			window.clearTimeout(this.repositionId);
		}
		if (this.updateIntervalId) {
			window.clearInterval(this.updateIntervalId);
		}
		if (this.container.parentElement) {
			this.container.remove();
		}
	}

	private buildButton(cssClass: string, label: string, onTap: () => void, onRelease?: () => void, triggerOnPointerDown = false): HTMLButtonElement {
		const btn = document.createElement("button");
		btn.type = "button";
		btn.className = "action-dock__button " + cssClass;
		btn.title = label;
		btn.setAttribute("aria-label", label);

		btn.addEventListener("pointerdown", (evt) => {
			evt.preventDefault();
			if (triggerOnPointerDown) {
				btn.dataset.press = "true";
				onTap();
			}
		});
		btn.addEventListener("click", (evt) => {
			evt.preventDefault();
			if (triggerOnPointerDown && btn.dataset.press) {
				delete btn.dataset.press;
				return;
			}
			onTap();
		});
		if (onRelease) {
			btn.addEventListener("pointerup", () => onRelease());
			btn.addEventListener("pointercancel", () => onRelease());
			btn.addEventListener("pointerleave", () => onRelease());
		}

		return btn;
	}

	private applyUiState(state: UiState) {
		this.container.classList.toggle("action-dock--panels", state.mode === UiMode.PANELS);
		this.container.classList.toggle("action-dock--game", state.mode === UiMode.GAME);
		this.container.classList.toggle("action-dock--left", state.handedness === UiHandedness.LEFT);
		this.container.classList.toggle("action-dock--right", state.handedness === UiHandedness.RIGHT);
		this.handedness = state.handedness;
		this.scheduleReposition();
	}

	private refreshTargets() {
		this.updateInteractionButton();
		this.updatePickupButton();
		this.updateQuickSlots();
	}

	private updateInteractionButton() {
		const target = this.peekNearestInteraction();
		const available = !!target;
		this.toggleAvailability(this.interactButton, available);
		this.interactButton.classList.toggle("action-dock__button--ready", available);
		this.interactButton.setAttribute("aria-hidden", available ? "false" : "true");
	}

	private updatePickupButton() {
		const hasTarget = this.peekPickupTarget();
		const corpseWindow = document.getElementById("corpse");
		const available = hasTarget || !!corpseWindow;

		this.toggleAvailability(this.pickupButton, available);
		this.pickupButton.classList.toggle("action-dock__button--ready", available);
	}

	private updateQuickSlots() {
		const bindings = this.buildQuickSlotBindings();

		for (let i = 0; i < this.quickSlotButtons.length; i++) {
			const btn = this.quickSlotButtons[i];
			const binding = bindings[i];
			if (binding) {
				btn.disabled = false;
				btn.classList.add("action-dock__button--ready");
				btn.style.backgroundImage = "url(" + binding.item.sprite.filename + ")";
				const label = binding.item["name"] ? `Użyj ${binding.item["name"]}` : "Użyj przedmiotu";
				btn.setAttribute("aria-label", label);
			} else {
				btn.disabled = true;
				btn.classList.remove("action-dock__button--ready");
				btn.style.backgroundImage = "url(/data/gui/panel/empty_btn.png)";
				btn.setAttribute("aria-label", "Pusty szybki slot");
			}
		}
	}

	private toggleAvailability(btn: HTMLButtonElement, available: boolean) {
		btn.disabled = !available;
		btn.classList.toggle("action-dock__button--hidden", !available);
	}

	private onAttackPressStart() {
		this.attackButton.classList.add("action-dock__button--engaged");
		this.tryAttack();
		// hold to repeat attack/cycle nearest targets
		window.setTimeout(() => {
			if (!this.attackButton.classList.contains("action-dock__button--engaged")) {
				return;
			}
			this.cycleAttack();
			this.attackButton.dataset.repeat = window.setInterval(() => this.cycleAttack(), 900).toString();
		}, 450);
	}

	private onAttackPressEnd() {
		this.attackButton.classList.remove("action-dock__button--engaged");
		const repeatId = this.attackButton.dataset.repeat;
		if (repeatId) {
			window.clearInterval(parseInt(repeatId, 10));
			delete this.attackButton.dataset.repeat;
		}
	}

	private tryAttack() {
		const controller = TargetingController.get();
		const target = controller.attackCurrentOrNearest();
		if (!target) {
			this.flashDisabled(this.attackButton);
		}
	}

	private cycleAttack() {
		const controller = TargetingController.get();
		const target = controller.cycleAndAttack();
		if (!target) {
			this.flashDisabled(this.attackButton);
		}
	}

	private onInteract() {
		const controller = TargetingController.get();
		const target = controller.interactNearest();

		if (target && target instanceof NPC) {
			const key = target["id"] || target["name"] || "";
			const greeted = this.interactionHistory.get(key) === true;
			if (!greeted) {
				this.emitChat(this.pickRandom(this.greetings));
				this.interactionHistory.set(key, true);
			} else {
				this.emitChat(this.pickRandom(this.farewells));
			}
		}

		if (!target) {
			this.flashDisabled(this.interactButton);
		}
	}

	private onPickup() {
		const controller = TargetingController.get();
		const target = controller.pickupNearest();
		if (!target) {
			this.flashDisabled(this.pickupButton);
		}
	}

	private onQuickSlot(idx: number) {
		const bindings = this.buildQuickSlotBindings();
		const binding = bindings[idx];
		if (!binding) {
			this.flashDisabled(this.quickSlotButtons[idx]);
			return;
		}

		if (binding.element) {
			const evt = new MouseEvent("mouseup", { bubbles: true, cancelable: true });
			binding.element.dispatchEvent(evt);
			return;
		}

		marauroa.clientFramework.sendAction({
			type: "use",
			"target_path": binding.item.getIdPath(),
			"zone": marauroa.currentZoneName
		});
	}

	private flashDisabled(btn: HTMLButtonElement) {
		btn.classList.add("action-dock__button--disabled");
		window.setTimeout(() => btn.classList.remove("action-dock__button--disabled"), 350);
	}

	private emitChat(message: string) {
		if (typeof stendhal.actions?.execute === "function") {
			stendhal.actions.execute(message);
		}
	}

	private pickRandom(values: string[]): string {
		return values[Math.floor(Math.random() * values.length)];
	}

	private peekNearestInteraction() {
		return this.peekNearest({
			requireHealth: false,
			respectPreferences: false,
			types: ["npc", "player", "creature"]
		});
	}

	private peekPickupTarget() {
		return this.peekNearest({
			requireHealth: false,
			respectPreferences: false,
			predicate: (entity) => typeof entity["hp"] !== "number" && !!entity["id"]
		});
	}

	private peekNearest(filter: TargetFilter) {
		const controller = TargetingController.get();
		const prev = controller.getCurrent();
		const nearest = controller.getNearest(filter);
		controller.setCurrent(prev);
		return nearest;
	}

	private buildQuickSlotBindings(): QuickSlotBinding[] {
		const bindings: QuickSlotBinding[] = [];
		const bagContainer = this.findBagContainer();
		const slotName = (bagContainer as any)?.slot || "bag";
		const suffix = (bagContainer as any)?.suffix || "";
		const bagHolder = (bagContainer as any)?.object || marauroa.me;
		const bag = bagHolder ? bagHolder[slotName] : undefined;

		if (!bag || typeof bag.count !== "function") {
			return bindings;
		}

		for (let i = 0; i < bag.count(); i++) {
			const item = bag.getByIndex(i) as Item;
			if (!item || !this.isQuickItemAllowed(item)) {
				continue;
			}
			const elementId = slotName + suffix + i;
			const element = document.getElementById(elementId);
			bindings.push({ item, element });
			if (bindings.length >= this.quickSlotButtons.length) {
				break;
			}
		}

		return bindings;
	}

	private isQuickItemAllowed(item: Item): boolean {
		const cls = (item["class"] || "").toLowerCase();
		return cls === "potion" || cls === "food" || cls === "drink" || cls === "scroll";
	}

	private findBagContainer() {
		if (!stendhal.ui?.equip?.getInventory) {
			return;
		}
		for (const container of stendhal.ui.equip.getInventory()) {
			if ((container as any).slot === "bag") {
				return container;
			}
		}
	}

	private scheduleReposition() {
		if (this.repositionId) {
			window.clearTimeout(this.repositionId);
		}
		this.repositionId = window.setTimeout(() => this.reposition(), 60);
	}

	private reposition() {
		const viewport = document.getElementById("viewport");
		const rect = viewport ? viewport.getBoundingClientRect() : undefined;
		const margin = 20;
		const dockSize = this.container.getBoundingClientRect().width || 220;

		let x = window.innerWidth - dockSize - margin;
		let y = window.innerHeight - dockSize - margin;

		if (rect) {
			x = this.handedness === UiHandedness.LEFT
				? rect.left + margin
				: rect.right - dockSize - margin;
			y = rect.bottom - dockSize - margin;
		}

		this.container.style.transform = "translate(" + x + "px, " + y + "px)";
	}
}
