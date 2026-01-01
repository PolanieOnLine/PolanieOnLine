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

import { TargetingController } from "../../game/TargetingController";
import { Entity } from "../../entity/Entity";
import { Item } from "../../entity/Item";
import { NPC } from "../../entity/NPC";
import { Player } from "../../entity/Player";
import { PopupInventory } from "../../entity/PopupInventory";
import { UseableEntity } from "../../entity/UseableEntity";
import { ConfigManager } from "../../util/ConfigManager";
import { SessionManager } from "../../util/SessionManager";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";
import { QuickslotEntry, QuickslotStore } from "./QuickslotStore";


const DOCK_MARGIN = 18;
const DOCK_RANGE = 5;


/**
 * Floating action dock used by mobile layout.
 */
export class ActionDock {

	private static instance: ActionDock;

	private readonly store = UiStateStore.get();
	private readonly quickslots = QuickslotStore.get();
	private readonly session = SessionManager.get();
	private readonly config = ConfigManager.get();

	private readonly root = document.createElement("div");
	private readonly cluster = document.createElement("div");

	private readonly attackButton = new AttackDockButton();
	private readonly interactButton = new InteractDockButton(() => this.greetNearestNpc());
	private readonly pickupButton = new PickupDockButton(() => this.pickupNearest());
	private readonly quickslotButtons: QuickslotButton[] = [];

	private mounted = false;
	private availabilityInterval?: number;
	private debounceId?: number;
	private unsubscribeState?: () => void;
	private unsubscribeQuickslots?: () => void;
	private lastState?: UiState;
	private readonly debouncedPosition: () => void;
	private readonly debouncedLayout: () => void;


	static get(): ActionDock {
		if (!ActionDock.instance) {
			ActionDock.instance = new ActionDock();
		}
		return ActionDock.instance;
	}

	static isMounted(): boolean {
		return !!ActionDock.instance?.mounted;
	}

	private constructor() {
		this.root.className = "action-dock action-dock--hidden";
		this.root.setAttribute("aria-label", "Akcje mobilne");

		this.cluster.className = "action-dock__cluster";

		this.root.appendChild(this.cluster);

		this.cluster.appendChild(this.attackButton.element);
		this.cluster.appendChild(this.interactButton.element);
		this.cluster.appendChild(this.pickupButton.element);

		this.debouncedPosition = this.debounce(() => this.updatePosition(), 120);
		this.debouncedLayout = this.debounce(() => this.layoutButtons(), 60);

		for (let slot = 1; slot <= 3; slot++) {
			const btn = new QuickslotButton(slot, this.quickslots, () => this.layoutButtons());
			this.quickslotButtons.push(btn);
			this.cluster.appendChild(btn.element);
		}

		this.attackButton.setOnSizeChange(() => this.debouncedLayout());
		this.interactButton.setOnSizeChange(() => this.debouncedLayout());
		this.pickupButton.setOnSizeChange(() => this.debouncedLayout());
	}

	refresh() {
		if (this.shouldShow()) {
			this.mount();
		} else {
			this.unmount();
		}
	}

	destroy() {
		this.unmount();
		this.unsubscribeState?.();
		this.unsubscribeQuickslots?.();
	}

	private shouldShow(): boolean {
		if (this.config.isSet("attack.button")) {
			return this.config.getBoolean("attack.button");
		}
		return this.session.touchOnly();
	}

	private mount() {
		if (this.mounted) {
			this.updatePosition();
			return;
		}
		document.body.appendChild(this.root);
		this.mounted = true;
		this.subscribe();
		this.onQuickslotUpdate(this.quickslots.getEntries());
		this.refreshAvailability();
		this.layoutButtons();
		this.updatePosition();
		this.bindListeners();
	}

	private unmount() {
		if (!this.mounted) {
			return;
		}
		this.root.remove();
		this.mounted = false;
		this.unbindListeners();
		this.stopAvailabilityLoop();
	}

	private subscribe() {
		if (!this.unsubscribeState) {
			this.unsubscribeState = this.store.subscribe((state) => this.applyState(state));
		}
		if (!this.unsubscribeQuickslots) {
			this.unsubscribeQuickslots = this.quickslots.subscribe((entries) => this.onQuickslotUpdate(entries));
		}
	}

	private applyState(state: UiState) {
		this.lastState = state;
		const faded = state.mode === UiMode.PANELS;
		this.root.classList.toggle("action-dock--faded", faded);
		this.root.classList.toggle("action-dock--hidden", faded);
		this.updatePosition();
	}

	private onQuickslotUpdate(entries: QuickslotEntry[]) {
		for (const btn of this.quickslotButtons) {
			const entry = entries.find((e) => e.slot === btn.slot);
			btn.update(entry);
		}
	}

	private bindListeners() {
		window.addEventListener("resize", this.debouncedPosition);
		window.addEventListener("touchmove", this.debouncedPosition, { passive: true });
		window.addEventListener("resize", this.debouncedLayout);
		this.startAvailabilityLoop();
	}

	private unbindListeners() {
		window.removeEventListener("resize", this.debouncedPosition);
		window.removeEventListener("touchmove", this.debouncedPosition);
		window.removeEventListener("resize", this.debouncedLayout);
	}

	private startAvailabilityLoop() {
		this.stopAvailabilityLoop();
		this.availabilityInterval = window.setInterval(() => this.refreshAvailability(), 400);
	}

	private stopAvailabilityLoop() {
		if (this.availabilityInterval) {
			window.clearInterval(this.availabilityInterval);
			this.availabilityInterval = undefined;
		}
	}

	private updatePosition() {
		if (!this.mounted) {
			return;
		}
		const viewport = document.getElementById("viewport");
		const rect = viewport?.getBoundingClientRect();
		const width = this.root.offsetWidth || 240;
		const height = this.root.offsetHeight || 240;

		let left = window.innerWidth - width - DOCK_MARGIN;
		let top = window.innerHeight - height - DOCK_MARGIN;

		if (rect) {
			left = rect.right - width - DOCK_MARGIN;
			top = rect.bottom - height - DOCK_MARGIN;
		}

		this.root.style.transform = `translate(${Math.max(DOCK_MARGIN, left)}px, ${Math.max(DOCK_MARGIN, top)}px)`;
	}

	private refreshAvailability() {
		if (!marauroa || !marauroa.currentZone) {
			this.interactButton.setVisible(false);
			this.pickupButton.setAvailable(false);
			return;
		}

		const interactTarget = this.findInteractTarget();
		this.interactButton.setVisible(!!interactTarget);

		const pickupTarget = this.findPickupTarget();
		this.pickupButton.setAvailable(!!pickupTarget);
	}

	private findInteractTarget(): Entity|undefined {
		return TargetingController.get().getNearest({
			requireHealth: false,
			respectPreferences: false,
			predicate: (entity: Entity) => this.isInteractionCandidate(entity) && this.isWithinRange(entity)
		});
	}

	private findPickupTarget(): Entity|undefined {
		return TargetingController.get().getNearest({
			requireHealth: false,
			respectPreferences: false,
			predicate: (entity: Entity) => entity instanceof Item && this.isWithinRange(entity)
		});
	}

	private isInteractionCandidate(entity: Entity): boolean {
		return entity instanceof NPC;
	}

	private isWithinRange(entity: Entity): boolean {
		if (!marauroa || !marauroa.me || typeof marauroa.me.getDistanceTo !== "function") {
			return false;
		}
		const dist = marauroa.me.getDistanceTo(entity);
		return dist >= 0 && dist <= DOCK_RANGE;
	}

	private debounce(fn: () => void, wait: number): () => void {
		return () => {
			if (this.debounceId) {
				window.clearTimeout(this.debounceId);
			}
			this.debounceId = window.setTimeout(fn, wait);
		};
	}

	private greetNearestNpc() {
		const target = this.findInteractTarget();
		if (!target) {
			return;
		}
		TargetingController.get().setCurrent(target);
		if (typeof stendhal !== "undefined" && stendhal.actions) {
			stendhal.actions.execute("Cześć");
		}
	}

	private pickupNearest() {
		const target = this.findPickupTarget();
		if (!target) {
			return;
		}
		const action = typeof (target as any).getDefaultAction === "function"
			? (target as any).getDefaultAction()
			: {
				type: "equip",
				"source_path": target.getIdPath(),
				"target_path": "[" + marauroa.me["id"] + "\tbag]",
				"zone": marauroa.currentZoneName
			};
		marauroa.clientFramework.sendAction(action);
	}

	private layoutButtons() {
		const clusterSize = this.getClusterSize();
		this.root.style.width = clusterSize.width + "px";
		this.root.style.height = clusterSize.height + "px";
		this.cluster.style.width = clusterSize.width + "px";
		this.cluster.style.height = clusterSize.height + "px";

		const mainSize = this.attackButton.getSize();
		const base = Math.max(mainSize.width || 64, mainSize.height || 64);
		const radius = base * 1.35;

		const centerX = clusterSize.width - base * 0.6;
		const centerY = clusterSize.height - base * 0.6;

		this.attackButton.setPosition(centerX, centerY);

		this.positionAround(this.interactButton, centerX, centerY, radius * 1.05, -55);
		this.positionAround(this.pickupButton, centerX, centerY, radius * 1.15, -120);

		const quickAngles = [-25, -75, -150];
		for (let i = 0; i < this.quickslotButtons.length; i++) {
			this.positionAround(this.quickslotButtons[i], centerX, centerY, radius * 1.3, quickAngles[i]);
		}
	}

	private getClusterSize(): {width: number; height: number} {
		const mainSize = this.attackButton.getSize();
		const base = Math.max(mainSize.width || 64, mainSize.height || 64);
		const width = base * 3.2;
		const height = base * 3.2;
		return { width, height };
	}

	private positionAround(button: DockButton, centerX: number, centerY: number, radius: number, angleDeg: number) {
		const angle = angleDeg * Math.PI / 180;
		const x = centerX + radius * Math.cos(angle);
		const y = centerY + radius * Math.sin(angle);
		button.setPosition(x, y);
	}
}


abstract class DockButton {

	public readonly element: HTMLButtonElement;
	private readonly icon: HTMLImageElement;
	private onSizeChange?: () => void;


	constructor(type: string, label: string, title: string, onActivate?: () => void) {
		this.element = document.createElement("button");
		this.element.type = "button";
		this.element.className = `action-dock__button action-dock__button--${type}`;
		this.element.title = title;
		this.element.setAttribute("aria-label", title);
		this.icon = document.createElement("img");
		this.icon.className = "action-dock__icon";
		this.icon.alt = label || title;
		this.icon.draggable = false;
		this.element.appendChild(this.icon);

		if (onActivate) {
			this.element.addEventListener("click", (evt) => {
				evt.preventDefault();
				onActivate();
			});
		}

		this.icon.addEventListener("load", () => this.onSizeChange?.());
	}

	setEnabled(enabled: boolean) {
		this.element.disabled = !enabled;
		this.element.classList.toggle("action-dock__button--disabled", !enabled);
	}

	setVisible(visible: boolean) {
		this.element.classList.toggle("action-dock__button--hidden", !visible);
	}

	setPosition(x: number, y: number) {
		const size = this.getSize();
		const left = x - size.width / 2;
		const top = y - size.height / 2;
		this.element.style.left = `${left}px`;
		this.element.style.top = `${top}px`;
	}

	setIcon(src: string) {
		if (src) {
			this.icon.src = src;
		}
	}

	getSize(): {width: number; height: number} {
		const rect = this.icon.getBoundingClientRect();
		if (rect.width && rect.height) {
			return { width: rect.width, height: rect.height };
		}
		return { width: 64, height: 64 };
	}

	setOnSizeChange(handler: () => void) {
		this.onSizeChange = handler;
	}
}


class AttackDockButton extends DockButton {

	private readonly cooldownDuration = 800;
	private readonly longPressDelay = 450;
	private readonly repeatInterval = 900;

	private cooldownId?: number;
	private repeatId?: number;
	private pressTimeoutId?: number;
	private longPressTriggered = false;


	constructor() {
		super("attack", "", "Atakuj najbliższy cel");
		this.setIcon("/data/gui/panel/attack_btn.png");
		this.element.addEventListener("click", (evt) => {
			this.onActivate(evt);
		});
		this.element.addEventListener("pointerdown", (evt) => this.onPressStart(evt));
		this.element.addEventListener("pointerup", () => this.onPressEnd());
		this.element.addEventListener("pointerleave", () => this.onPressEnd());
		this.element.addEventListener("pointercancel", () => this.onPressEnd());
	}

	private onActivate(evt: Event) {
		evt.preventDefault();
		if (this.longPressTriggered) {
			return;
		}
		this.tryAttack();
	}

	private performAttack(attackFn: () => Entity|undefined): boolean {
		if (this.cooldownId) {
			return true;
		}

		const target = attackFn();
		if (!target) {
			this.flashDisabled();
			return false;
		}

		this.startCooldown();
		return true;
	}

	private tryAttack(): boolean {
		return this.performAttack(() => TargetingController.get().attackCurrentOrNearest());
	}

	private tryCycleAttack(): boolean {
		return this.performAttack(() => TargetingController.get().cycleAndAttack());
	}

	private onPressStart(_evt: PointerEvent|TouchEvent|MouseEvent) {
		this.clearRepeat();
		this.pressTimeoutId = window.setTimeout(() => {
			this.longPressTriggered = true;
			if (!this.tryCycleAttack()) {
				return;
			}
			this.repeatId = window.setInterval(() => this.tryCycleAttack(), this.repeatInterval);
		}, this.longPressDelay);
	}

	private onPressEnd() {
		this.clearRepeat();
	}

	private clearRepeat() {
		if (this.pressTimeoutId) {
			window.clearTimeout(this.pressTimeoutId);
			this.pressTimeoutId = undefined;
		}
		if (this.repeatId) {
			window.clearInterval(this.repeatId);
			this.repeatId = undefined;
		}
		this.longPressTriggered = false;
	}

	private flashDisabled() {
		this.setEnabled(false);
		window.setTimeout(() => this.setEnabled(true), 400);
	}

	private startCooldown() {
		this.setEnabled(false);
		this.cooldownId = window.setTimeout(() => {
			this.setEnabled(true);
			this.cooldownId = undefined;
		}, this.cooldownDuration);
	}
}


class InteractDockButton extends DockButton {

	constructor(onActivate: () => void) {
		super("interact", "", "Interakcja z najbliższym NPC", onActivate);
		this.setIcon("/data/gui/panel/talk_btn.png");
	}
}


class PickupDockButton extends DockButton {

	constructor(onActivate: () => void) {
		super("pickup", "", "Podnieś najbliższy przedmiot", onActivate);
		this.setIcon("/data/gui/panel/pickup_btn.png");
	}

	setAvailable(available: boolean) {
		this.setEnabled(available);
		this.element.classList.toggle("action-dock__button--disabled", !available);
	}
}


class QuickslotButton extends DockButton {

	private pressTimeoutId?: number;
	private currentEntry?: QuickslotEntry;

	constructor(public readonly slot: number, private readonly store: QuickslotStore, private readonly requestLayout: () => void) {
		super("quickslot", `Q${slot}`, `Quickslot ${slot}`);
		this.element.dataset.slot = String(slot);
		this.element.addEventListener("click", (evt) => {
			evt.preventDefault();
			this.onActivate();
		});
		this.element.addEventListener("pointerdown", () => this.onPressStart());
		this.element.addEventListener("pointerup", () => this.onPressEnd(false));
		this.element.addEventListener("pointerleave", () => this.onPressEnd(true));
		this.element.addEventListener("pointercancel", () => this.onPressEnd(true));
		this.setOnSizeChange(() => this.requestLayout());
	}

	update(entry?: QuickslotEntry) {
		this.currentEntry = entry;
		const label = entry?.label || `Quick ${this.slot}`;
		const shortLabel = label.length > 10 ? label.slice(0, 9) + "…" : label;
		this.element.setAttribute("aria-label", label);
		this.element.title = label;
		if (entry?.icon) {
			this.setIcon(entry.icon);
		} else {
			this.setIcon("/data/gui/panel/empty_btn.png");
		}
		const hasEntry = !!entry;
		this.element.classList.toggle("action-dock__button--empty", !hasEntry);
		this.setEnabled(hasEntry);
		this.requestLayout();
	}

	private onActivate() {
		if (!this.currentEntry) {
			return;
		}
		this.store.trigger(this.slot);
	}

	private onPressStart() {
		this.clearPressTimeout();
		this.pressTimeoutId = window.setTimeout(() => {
			this.store.clear(this.slot);
			this.clearPressTimeout();
		}, 750);
	}

	private onPressEnd(cancelOnly: boolean) {
		if (!cancelOnly && this.pressTimeoutId) {
			// short press, do nothing special
		}
		this.clearPressTimeout();
	}

	private clearPressTimeout() {
		if (this.pressTimeoutId) {
			window.clearTimeout(this.pressTimeoutId);
			this.pressTimeoutId = undefined;
		}
	}
}
