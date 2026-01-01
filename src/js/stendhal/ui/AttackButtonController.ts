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

import { AttackButton } from "./component/AttackButton";

import { ConfigManager } from "../util/ConfigManager";
import { SessionManager } from "../util/SessionManager";
import { UiStateStore } from "./mobile/UiStateStore";

/**
 * Manages lifecycle of the attack button overlay.
 */
export class AttackButtonController {

	private static instance: AttackButtonController;
	private component?: AttackButton;
	private unsubscribeHandedness?: () => void;

	/**
	 * Retrieves singleton instance.
	 */
	public static get(): AttackButtonController {
		if (!AttackButtonController.instance) {
			AttackButtonController.instance = new AttackButtonController();
		}
		return AttackButtonController.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Refreshes attack button state based on configuration and device.
	 */
	public update() {
		if (!SessionManager.get().attackButtonEnabled()) {
			this.remove();
			return;
		}
		if (!this.component) {
			this.component = new AttackButton();
			this.subscribeHandedness();
		}
		this.component.mount();
	}

	/**
	 * Removes button and clears instance.
	 */
	public remove() {
		if (this.component) {
			this.component.unmount();
		}
		this.component = undefined;
		this.unsubscribeHandedness?.();
		this.unsubscribeHandedness = undefined;
	}

	/**
	 * Toggles configuration value and refreshes component.
	 */
	public toggleSetting(nextState: boolean) {
		ConfigManager.get().set("attack.button", nextState);
		this.update();
	}

	private subscribeHandedness() {
		if (!this.component || this.unsubscribeHandedness) {
			return;
		}

		const store = UiStateStore.get();
		this.unsubscribeHandedness = store.subscribe((state) => {
			this.component?.setHandedness(state.handedness);
		});
		this.component.setHandedness(store.getState().handedness);
	}
}
