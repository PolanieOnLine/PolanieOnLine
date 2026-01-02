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

import { Item } from "../../entity/Item";
import { TargetingController } from "../../game/TargetingController";
import { Chat } from "../../util/Chat";
import { ConfigManager } from "../../util/ConfigManager";
import { SessionManager } from "../../util/SessionManager";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";

type QuickslotConfig = {
	path: string;
	icon?: string;
};

type QuickslotMap = {[id: string]: QuickslotConfig};


/**
 * Radial mobile action dock anchored to the viewport.
 */
export class ActionDock {

	private static readonly SIZE = 220;
	private static readonly MARGIN = 18;
	private static readonly PRIMARY_SIZE = 96;
	private static readonly SECONDARY_SIZE = 68;
	private static readonly REPEAT_DELAY = 450;
	private static readonly REPEAT_INTERVAL = 900;

	private readonly store = UiStateStore.get();
	private readonly targeting = TargetingController.get();
	private readonly config = ConfigManager.get();

	private readonly host: HTMLElement;
	private readonly root: HTMLDivElement;
	private readonly attackButton: HTMLButtonElement;
	private readonly interactButton: HTMLButtonElement;
	private readonly pickupButton: HTMLButtonElement;
	private readonly quickslotButtons: HTMLButtonElement[] = [];
	private readonly debouncedUpdate: () => void;

	private unsubscribe?: () => void;
	private availabilityInterval?: number;
	private repeatTimeoutId?: number;
	private repeatIntervalId?: number;

	private handedness: UiHandedness = UiHandedness.RIGHT;
	private mode: UiMode = UiMode.PANELS;
	private quickslots: QuickslotMap = {};


	constructor() {
		this.host = this.resolveHost();
		this.root = document.createElement("div");
		this.root.className = "action-dock action-dock--panels";
		this.root.style.width = ActionDock.SIZE + "px";
		this.root.style.height = ActionDock.SIZE + "px";

		this.attackButton = this.createButton("action-dock__button action-dock__button--attack", "/data/gui/panel/attack_btn.png", "Atakuj");
		this.interactButton = this.createButton("action-dock__button action-dock__button--interact", "/data/gui/panel/talk_btn.png", "Interakcja");
		this.pickupButton = this.createButton("action-dock__button action-dock__button--pickup", "/data/gui/panel/pickup_btn.png", "Podnieś");

		for (let idx = 1; idx <= 3; idx++) {
			const btn = this.createButton("action-dock__button action-dock__button--quickslot", "/data/gui/panel/empty_btn.png", "Szybki slot " + idx);
			btn.dataset.index = "" + idx;
			this.quickslotButtons.push(btn);
		}

		this.root.appendChild(this.attackButton);
		this.root.appendChild(this.interactButton);
		this.root.appendChild(this.pickupButton);
		for (const btn of this.quickslotButtons) {
			this.root.appendChild(btn);
		}

		this.debouncedUpdate = this.buildDebounce(() => this.updatePosition(), 150);
		this.registerEvents();
		this.attach();
		this.loadQuickslots();
		this.applyHandedness(this.store.getState().handedness);
		this.applyMode(this.store.getState().mode);
		this.refreshAvailability();
	}

	private resolveHost(): HTMLElement {
		const viewport = document.getElementById("viewport");
		if (viewport && viewport.parentElement) {
			return viewport.parentElement;
		}
		return document.body;
	}

	private registerEvents() {
		this.attackButton.addEventListener("click", (e) => this.onAttack(e));
		this.attackButton.addEventListener("pointerdown", (e) => this.onAttackPressStart(e));
		for (const event of ["pointerup", "pointerleave", "pointercancel"]) {
			this.attackButton.addEventListener(event, () => this.clearAttackRepeat());
		}

		this.interactButton.addEventListener("click", (e) => this.onInteract(e));
		this.pickupButton.addEventListener("click", (e) => this.onPickup(e));

		for (const btn of this.quickslotButtons) {
			btn.addEventListener("click", (e) => this.onQuickslotActivate(e));
			btn.addEventListener("contextmenu", (e) => this.onQuickslotAssign(e));
			btn.addEventListener("dragover", (e) => this.onQuickslotDragOver(e));
			btn.addEventListener("drop", (e) => this.onQuickslotDrop(e));
		}

		this.unsubscribe = this.store.subscribe((state) => this.onStoreUpdate(state));

		window.addEventListener("resize", this.debouncedUpdate);
		window.addEventListener("orientationchange", this.debouncedUpdate);
		window.addEventListener("touchmove", this.debouncedUpdate);

		this.availabilityInterval = window.setInterval(() => this.refreshAvailability(), 600);
	}

	private attach() {
		this.host.appendChild(this.root);
		this.updatePosition();
	}

	public destroy() {
		this.clearAttackRepeat();
		if (this.root.parentElement) {
			this.root.remove();
		}
		if (this.unsubscribe) {
			this.unsubscribe();
			this.unsubscribe = undefined;
		}
		if (this.availabilityInterval) {
			window.clearInterval(this.availabilityInterval);
			this.availabilityInterval = undefined;
		}
		window.removeEventListener("resize", this.debouncedUpdate);
		window.removeEventListener("orientationchange", this.debouncedUpdate);
		window.removeEventListener("touchmove", this.debouncedUpdate);
	}

	public updatePosition() {
		const viewport = document.getElementById("viewport");
		const rect = viewport ? viewport.getBoundingClientRect() : document.body.getBoundingClientRect();
		const half = ActionDock.SIZE / 2;
		const offsetX = this.handedness === UiHandedness.RIGHT
			? rect.right - ActionDock.MARGIN - half
			: rect.left + ActionDock.MARGIN + half;
		const offsetY = rect.bottom - ActionDock.MARGIN - half;
		this.root.style.transform = "translate3d(" + (offsetX - half) + "px, " + (offsetY - half) + "px, 0)";
		this.layoutButtons();
	}

	private createButton(className: string, icon: string, label: string): HTMLButtonElement {
		const btn = document.createElement("button");
		btn.className = className;
		btn.style.backgroundImage = "url(" + icon + ")";
		btn.setAttribute("aria-label", label);
		btn.title = label;
		return btn;
	}

	private layoutButtons() {
		const center = ActionDock.SIZE / 2;
		this.setButtonLayout(this.attackButton, center, center, ActionDock.PRIMARY_SIZE);

		const layout = this.getRadialLayout();
		this.setButtonLayout(this.interactButton, layout.interact.x, layout.interact.y, ActionDock.SECONDARY_SIZE);
		this.setButtonLayout(this.pickupButton, layout.pickup.x, layout.pickup.y, ActionDock.SECONDARY_SIZE);
		for (let idx = 0; idx < this.quickslotButtons.length; idx++) {
			const pos = layout.quickslots[idx];
			this.setButtonLayout(this.quickslotButtons[idx], pos.x, pos.y, ActionDock.SECONDARY_SIZE);
		}
	}

	private setButtonLayout(btn: HTMLButtonElement, x: number, y: number, size: number) {
		btn.style.width = size + "px";
		btn.style.height = size + "px";
		btn.style.left = (x - size / 2) + "px";
		btn.style.top = (y - size / 2) + "px";
	}

	private getRadialLayout() {
		const center = ActionDock.SIZE / 2;
		const radius = 94;
		const baseAngles = {
			interact: -60,
			pickup: -120,
			quickslots: [-200, -260, -320]
		};
		const multiplier = this.handedness === UiHandedness.LEFT ? -1 : 1;
		const toPos = (deg: number) => {
			const rad = (deg * multiplier) * (Math.PI / 180);
			return {
				x: center + Math.cos(rad) * radius,
				y: center + Math.sin(rad) * radius
			};
		};
		return {
			interact: toPos(baseAngles.interact),
			pickup: toPos(baseAngles.pickup),
			quickslots: baseAngles.quickslots.map((ang) => toPos(ang))
		};
	}

	private onAttack(evt: Event) {
		evt.preventDefault();
		if (this.repeatIntervalId) {
			return;
		}
		this.targeting.attackCurrentOrNearest();
	}

	private onAttackPressStart(_evt: Event) {
		this.clearAttackRepeat();
		this.repeatTimeoutId = window.setTimeout(() => {
			this.repeatIntervalId = window.setInterval(() => {
				this.targeting.attackCurrentOrNearest();
			}, ActionDock.REPEAT_INTERVAL);
		}, ActionDock.REPEAT_DELAY);
	}

	private clearAttackRepeat() {
		if (this.repeatTimeoutId) {
			window.clearTimeout(this.repeatTimeoutId);
			this.repeatTimeoutId = undefined;
		}
		if (this.repeatIntervalId) {
			window.clearInterval(this.repeatIntervalId);
			this.repeatIntervalId = undefined;
		}
	}

	private onInteract(evt: Event) {
		evt.preventDefault();
		const target = this.targeting.getNearestInteractable();
		if (!target) {
			this.interactButton.classList.add("action-dock__button--disabled");
			window.setTimeout(() => this.interactButton.classList.remove("action-dock__button--disabled"), 320);
			return;
		}
		const attending = Chat.attending;
		if (attending) {
			marauroa.clientFramework.sendAction({ type: "chat", text: "bye" });
		} else {
			marauroa.clientFramework.sendAction({ type: "chat", text: "hi" });
		}
		this.targeting.interactNearest();
	}

	private onPickup(evt: Event) {
		evt.preventDefault();
		if (this.pickupButton.disabled) {
			return;
		}
		this.targeting.pickupNearest();
	}

	private onQuickslotActivate(evt: Event) {
		evt.preventDefault();
		const btn = evt.currentTarget as HTMLButtonElement;
		const idx = btn.dataset.index!;
		const data = this.quickslots[idx];
		if (!data) {
			btn.classList.add("action-dock__button--disabled");
			window.setTimeout(() => btn.classList.remove("action-dock__button--disabled"), 320);
			return;
		}
		marauroa.clientFramework.sendAction({
			type: "use",
			target_path: data.path,
			zone: marauroa.currentZoneName
		});
	}

	private onQuickslotAssign(evt: Event) {
		evt.preventDefault();
		const idx = (evt.currentTarget as HTMLButtonElement).dataset.index!;
		this.tryAssignQuickslot(idx);
	}

	private onQuickslotDragOver(evt: DragEvent) {
		evt.preventDefault();
	}

	private onQuickslotDrop(evt: DragEvent) {
		evt.preventDefault();
		const idx = (evt.currentTarget as HTMLButtonElement).dataset.index!;
		this.tryAssignQuickslot(idx);
	}

	private tryAssignQuickslot(idx: string) {
		const held = stendhal.ui.heldObject;
		if (!held || !held.path) {
			return;
		}
		const entity = this.resolveEntity(held.path);
		if (!(entity instanceof Item)) {
			return;
		}
		const icon = this.getIconForEntity(entity);
		this.quickslots[idx] = { path: held.path, icon };
		this.persistQuickslots();
		this.applyQuickslotVisual(idx);
	}

	private getIconForEntity(entity?: any): string|undefined {
		if (!entity || !(entity instanceof Item) || !entity.sprite || !entity.sprite.filename) {
			return;
		}
		return entity.sprite.filename;
	}

	private resolveEntity(path: string): any {
		if (marauroa.util && typeof marauroa.util.rpobjectFromPath === "function") {
			return marauroa.util.rpobjectFromPath(path);
		}
		return undefined;
	}

	private loadQuickslots() {
		const stored = this.config.getObject("ui.quickslots");
		if (stored) {
			this.quickslots = stored;
		}
		for (const idx of Object.keys(this.quickslots)) {
			this.applyQuickslotVisual(idx);
		}
	}

	private persistQuickslots() {
		this.config.set("ui.quickslots", this.quickslots);
		SessionManager.get().set("ui.quickslots", this.quickslots);
	}

	private applyQuickslotVisual(idx: string) {
		const config = this.quickslots[idx];
		const button = this.quickslotButtons[parseInt(idx, 10) - 1];
		if (!config || !button) {
			return;
		}
		if (config.icon) {
			button.style.backgroundImage = "url(" + config.icon + ")";
		}
		button.classList.remove("action-dock__button--disabled");
	}

	private refreshAvailability() {
		const canInteract = this.targeting.hasInteractTarget();
		const canPickup = this.targeting.hasPickupTarget();

		this.interactButton.classList.toggle("action-dock__button--hidden", !canInteract);
		this.pickupButton.disabled = !canPickup;
	}

	private onStoreUpdate(state: UiState) {
		if (state.handedness !== this.handedness) {
			this.applyHandedness(state.handedness);
		}
		if (state.mode !== this.mode) {
			this.applyMode(state.mode);
		}
	}

	private applyHandedness(next: UiHandedness) {
		this.handedness = next;
		this.root.classList.toggle("action-dock--left", next === UiHandedness.LEFT);
		this.updatePosition();
	}

	private applyMode(mode: UiMode) {
		this.mode = mode;
		this.root.classList.toggle("action-dock--panels", mode === UiMode.PANELS);
		this.root.classList.toggle("action-dock--game", mode === UiMode.GAME);
	}

	private buildDebounce(fn: () => void, delay: number): () => void {
		let handle: number|undefined;
		return () => {
			if (handle) {
				window.clearTimeout(handle);
			}
			handle = window.setTimeout(() => {
				handle = undefined;
				fn();
			}, delay);
		};
	}
}
