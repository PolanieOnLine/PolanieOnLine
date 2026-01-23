/***************************************************************************
 *                   (C) Copyright 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { PopupInventory } from "./PopupInventory";
import { MenuItem } from "../action/MenuItem";
import { GoldenCauldronComponent } from "../ui/component/GoldenCauldronComponent";
import { FloatingWindow } from "../ui/toolkit/FloatingWindow";
import { Paths } from "../data/Paths";
import { singletons } from "../SingletonRepo";

declare var marauroa: any;
declare var stendhal: any;

const WINDOW_ID = "golden-cauldron";
const SLOT_CONTENT = "content";
const MAX_DIST_TO_VIEW = 4;
const BREW_TIME_MS = 5 * 60 * 1000;

export class GoldenCauldron extends PopupInventory {
	override zIndex = 5000;
	override maxDistToView = MAX_DIST_TO_VIEW;

	private open = false;
	private state = 0;
	private status = "";
	private brewer?: string;
	private readyAt = 0;
	private readyInSeconds = 0;
	private readyStartedAt = 0;
	private window?: FloatingWindow;
	private component?: GoldenCauldronComponent;
	private requestOpen = false;

	constructor() {
		super();
		this.sprite = {
			filename: Paths.maps + "/tileset/item/pot/cauldron.png",
			height: 64,
			width: 64,
			offsetX: 0,
			offsetY: 0
		};
	}

	override set(key: string, value: any) {
		super.set(key, value);
		if (key === "open") {
			this.open = true;
			this.syncWindowState();
		} else if (key === "state") {
			this.state = parseInt(value, 10) || 0;
			this.updateSprite();
			this.updateMixAvailability();
		} else if (key === "status") {
			this.status = value || "";
			this.updateStatus();
		} else if (key === "brewer") {
			this.brewer = value;
			this.updateMixAvailability();
		} else if (key === "ready_at") {
			this.readyAt = typeof value === "number" ? value : parseInt(value, 10) || 0;
			this.normalizeReadyAt();
			this.updateStatus();
			this.updateMixAvailability();
		} else if (key === "ready_in") {
			this.readyInSeconds = parseInt(value, 10) || 0;
			this.normalizeReadyAt();
			this.updateStatus();
			this.updateMixAvailability();
		} else if (key === "ready_started_at") {
			this.readyStartedAt = typeof value === "number" ? value : parseInt(value, 10) || 0;
			this.normalizeReadyAt();
			this.updateStatus();
			this.updateMixAvailability();
		}
	}

	override unset(key: string) {
		super.unset(key);
		if (key === "open") {
			this.open = false;
			this.requestOpen = false;
			this.closeInventoryWindow();
		} else if (key === "state") {
			this.state = 0;
			this.updateSprite();
			this.updateMixAvailability();
		} else if (key === "status") {
			this.status = "";
			this.updateStatus();
		} else if (key === "brewer") {
			this.brewer = undefined;
			this.updateMixAvailability();
		} else if (key === "ready_at") {
			this.readyAt = 0;
			this.readyInSeconds = 0;
			this.readyStartedAt = 0;
			this.updateStatus();
			this.updateMixAvailability();
		} else if (key === "ready_in") {
			this.readyInSeconds = 0;
			this.normalizeReadyAt();
			this.updateStatus();
			this.updateMixAvailability();
		} else if (key === "ready_started_at") {
			this.readyStartedAt = 0;
			this.normalizeReadyAt();
			this.updateStatus();
			this.updateMixAvailability();
		}
	}

	override buildActions(list: MenuItem[]) {
		super.buildActions(list);
		if (this.open) {
			list.push({
				title: "Przeszukaj",
				action: () => {
					this.requestOpen = true;
					this.openInventoryWindow();
				}
			});
			list.push({
				title: "Zamknij",
				action: () => {
					this.requestOpen = false;
					this.onUsed();
				}
			});
		} else {
			list.push({
				title: "Otwórz",
				action: () => {
					this.requestOpen = true;
					this.onUsed();
				}
			});
		}
	}

	override onclick(_x: number, _y: number) {
		if (this.open) {
			this.requestOpen = true;
			this.openInventoryWindow();
		} else {
			this.requestOpen = true;
			this.onUsed();
		}
	}

	private onUsed() {
		const action: { [key: string]: any } = {
			type: "use",
			zone: marauroa.currentZoneName,
			target_path: this.getIdPath()
		};
		if (!this.isContained()) {
			action.target = "#" + this["id"];
		}
		marauroa.clientFramework.sendAction(action);
	}

	private sendMixAction() {
		const action = {
			type: "goldencauldron",
			command: "mix",
			target_path: this.getIdPath(),
			zone: marauroa.currentZoneName
		};
		marauroa.clientFramework.sendAction(action);
	}

	private openInventoryWindow() {
		if (!this.open) {
			return;
		}
		if (!this.requestOpen && !this.isControlledByUser()) {
			return;
		}
		if (!this.window || !this.window.isOpen()) {
			const component = new GoldenCauldronComponent(
				this,
				SLOT_CONTENT,
				() => this.sendMixAction(),
				() => this.onWindowClosed()
			);
			component.updateStatus(this.status, this.readyAt);
			component.setMixEnabled(this.isControlledByUser() && !this.isActive());

			const state = stendhal.config.getWindowState(WINDOW_ID);
			const windowInstance = new FloatingWindow("Kocioł Draconii", component, state.x, state.y);
			windowInstance.setId(WINDOW_ID);
			windowInstance.enableMinimizeButton(true);
			windowInstance.setFixedWidth(232);
			this.window = windowInstance;
			this.component = component;
		}
		this.window.setMinimized(false);
		this.requestOpen = false;
		this.updateStatus();
		this.updateMixAvailability();
	}

	override closeInventoryWindow() {
		if (this.window && this.window.isOpen()) {
			this.window.close();
		}
		this.window = undefined;
		this.component = undefined;
	}

	private onWindowClosed() {
		this.window = undefined;
		this.component = undefined;
		this.requestOpen = false;
		if (this.open && this.isControlledByUser()) {
			this.onUsed();
		}
	}

	private syncWindowState() {
		if (this.open) {
			if (this.canViewContents()) {
				this.openInventoryWindow();
			}
		} else {
			this.closeInventoryWindow();
		}
	}

	private updateStatus() {
		if (this.component) {
			this.component.updateStatus(this.status, this.readyAt);
		}
	}

	private updateMixAvailability() {
		if (this.component) {
			const canMix = this.isControlledByUser() && !this.isActive();
			this.component.setMixEnabled(canMix);
		}
	}

	private isControlledByUser(): boolean {
		const name = singletons.getSessionManager().getCharName();
		if (!name || !this.brewer) {
			return false;
		}
		return name.toLowerCase() === this.brewer.toLowerCase();
	}

	private isActive(): boolean {
		return this.state > 0;
	}

	private updateSprite() {
		const offsetY = this.state > 0 ? 64 : 0;
		if (this.sprite) {
			this.sprite.offsetY = offsetY;
		}
	}

	private normalizeReadyAt() {
		let computed = this.readyAt;
		if (this.readyInSeconds > 0) {
			const candidate = Date.now() + this.readyInSeconds * 1000;
			if (computed > 0) {
				computed = Math.min(computed, candidate);
			} else {
				computed = candidate;
			}
		} else if (this.readyStartedAt > 0) {
			const expected = this.readyStartedAt + BREW_TIME_MS;
			if (computed <= 0 || computed > expected) {
				computed = expected;
			}
		}
		this.readyAt = computed;
	}
}
