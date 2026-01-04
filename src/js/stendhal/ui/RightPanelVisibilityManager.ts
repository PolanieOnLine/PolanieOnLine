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

declare const stendhal: any;

import { InventoryWindowController } from "./component/InventoryWindowController";
import { UiMode, UiState, UiStateStore } from "./mobile/UiStateStore";

type VisibilityListener = (visible: boolean) => void;

/**
 * Controls visibility of the right inventory column, keeping state in sync with
 * mobile layout hints and floating menu mode.
 */
export class RightPanelVisibilityManager {

	private static instance?: RightPanelVisibilityManager;

	private readonly store = UiStateStore.get();
	private readonly root = document.getElementById("client");
	private readonly rightColumn = document.getElementById("rightColumn");

	private readonly listeners: Set<VisibilityListener> = new Set();
	private readonly windowIds = ["equipmentborder", "bag-window", "keyring-window", "magicbag-window"];
	private readonly savedWindowVisibility: Map<string, boolean> = new Map();

	private initialized = false;
	private floatingMenuEnabled = false;
	private uiMode: UiMode;
	private preferredVisible: boolean;
	private effectiveVisible = true;
	private unsubscribeStore?: () => void;

	private constructor() {
		const state = this.store.getState();
		this.uiMode = state.mode;
		this.preferredVisible = this.resolveInitialPreference();
	}

	static get(): RightPanelVisibilityManager {
		if (!this.instance) {
			this.instance = new RightPanelVisibilityManager();
		}
		return this.instance;
	}

	init() {
		if (this.initialized) {
			return;
		}
		this.initialized = true;
		this.unsubscribeStore = this.store.subscribe((state) => this.onUiStateChange(state));
		this.applyVisibility();
	}

	setFloatingMenuEnabled(enabled: boolean) {
		if (this.floatingMenuEnabled === enabled) {
			return;
		}
		this.floatingMenuEnabled = enabled;
		this.applyVisibility();
	}

	toggleVisibility(): boolean {
		return this.setVisible(!this.isVisible());
	}

	setVisible(visible: boolean): boolean {
		if (this.preferredVisible !== visible) {
			this.preferredVisible = visible;
			stendhal.config.set("ui.rightpanel.visible", visible);
		}
		if (visible && this.uiMode === UiMode.GAME) {
			this.store.setMode(UiMode.PANELS);
			this.store.setHandedness(this.store.getState().handedness);
		}
		this.applyVisibility();
		return this.effectiveVisible;
	}

	isVisible(): boolean {
		return this.effectiveVisible;
	}

	managesFloatingLayout(): boolean {
		return this.shouldManageVisibility();
	}

	subscribe(listener: VisibilityListener): () => void {
		this.listeners.add(listener);
		listener(this.effectiveVisible);
		return () => this.listeners.delete(listener);
	}

	private resolveInitialPreference(): boolean {
		if (stendhal.session.touchOnly()) {
			if (stendhal.config.isSet("ui.rightpanel.visible")) {
				return stendhal.config.getBoolean("ui.rightpanel.visible");
			}
			return false;
		}
		if (!stendhal.config.getBoolean("ui.rightpanel.visible")) {
			stendhal.config.set("ui.rightpanel.visible", true);
		}
		return true;
	}

	private shouldManageVisibility(): boolean {
		return this.floatingMenuEnabled || this.uiMode === UiMode.GAME;
	}

	private onUiStateChange(state: UiState) {
		this.uiMode = state.mode;
		this.applyVisibility();
	}

	private computeEffectiveVisibility(): boolean {
		if (!this.shouldManageVisibility()) {
			return true;
		}
		if (this.uiMode === UiMode.GAME) {
			return false;
		}
		return this.preferredVisible;
	}

	private applyVisibility() {
		const shouldHide = this.shouldManageVisibility() && !this.computeEffectiveVisibility();
		this.effectiveVisible = !shouldHide;

		if (shouldHide) {
			this.hideRightPanel();
		} else {
			this.showRightPanel();
		}

		this.notify();
	}

	private hideRightPanel() {
		if (this.root) {
			this.root.classList.add("floating-right-panel-hidden");
		}
		if (this.rightColumn) {
			this.rightColumn.setAttribute("aria-hidden", "true");
		}
		this.captureWindowVisibility();
		for (const id of this.windowIds) {
			InventoryWindowController.setWindowVisibility(id, false);
		}
	}

	private showRightPanel() {
		if (this.root) {
			this.root.classList.remove("floating-right-panel-hidden");
		}
		if (this.rightColumn) {
			this.rightColumn.removeAttribute("aria-hidden");
		}
		this.restoreWindowVisibility();
	}

	private captureWindowVisibility() {
		if (this.savedWindowVisibility.size > 0) {
			return;
		}
		for (const id of this.windowIds) {
			const windowElement = document.getElementById(id);
			if (!windowElement) {
				continue;
			}
			const hidden = windowElement.classList.contains("inventory-window--hidden") || windowElement.hasAttribute("hidden");
			this.savedWindowVisibility.set(id, !hidden);
		}
	}

	private restoreWindowVisibility() {
		if (this.savedWindowVisibility.size === 0) {
			return;
		}
		for (const id of this.windowIds) {
			const visible = this.savedWindowVisibility.get(id);
			if (typeof visible === "undefined") {
				continue;
			}
			InventoryWindowController.setWindowVisibility(id, visible);
		}
		this.savedWindowVisibility.clear();
	}

	private notify() {
		for (const listener of this.listeners) {
			listener(this.effectiveVisible);
		}
	}
}
