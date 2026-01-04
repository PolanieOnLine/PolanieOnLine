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

import { InventoryButton } from "./component/InventoryButton";

import { ConfigManager } from "../util/ConfigManager";
import { SessionManager } from "../util/SessionManager";
import { UiHandedness, UiMode, UiStateStore } from "./mobile/UiStateStore";

/**
 * Manages lifecycle of the inventory overlay button.
 */
export class InventoryButtonController {

	private static instance: InventoryButtonController;
	private component?: InventoryButton;
	private unsubscribeHandedness?: () => void;

	public static get(): InventoryButtonController {
		if (!InventoryButtonController.instance) {
			InventoryButtonController.instance = new InventoryButtonController();
		}
		return InventoryButtonController.instance;
	}

	private constructor() {
		// hidden constructor
	}

	public update() {
		if (!SessionManager.get().inventoryButtonEnabled()) {
			this.remove();
			return;
		}
		if (!this.component) {
			this.component = new InventoryButton();
			this.subscribeHandedness();
		}
		this.component.mount();
	}

	public remove() {
		if (this.component) {
			this.component.unmount();
		}
		this.component = undefined;
		this.unsubscribeHandedness?.();
		this.unsubscribeHandedness = undefined;
	}

	public toggleSetting(nextState: boolean) {
		ConfigManager.get().set("inventory.button", nextState);
		this.update();
	}

	private subscribeHandedness() {
		if (!this.component || this.unsubscribeHandedness) {
			return;
		}

		const store = UiStateStore.get();
		this.unsubscribeHandedness = store.subscribe((state) => {
			this.component?.setHandedness(state.handedness);
			this.component?.setPanelVisibility(this.isRightPanelVisible(state.mode, state.handedness));
		});

		const state = store.getState();
		this.component.setHandedness(state.handedness);
		this.component.setPanelVisibility(this.isRightPanelVisible(state.mode, state.handedness));
	}

	private isRightPanelVisible(mode: UiMode, handedness: UiHandedness): boolean {
		return mode === UiMode.PANELS && handedness === UiHandedness.RIGHT;
	}
}
