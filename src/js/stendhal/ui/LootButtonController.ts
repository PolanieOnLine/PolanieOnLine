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

import { LootButton } from "./component/LootButton";

import { ConfigManager } from "../util/ConfigManager";
import { SessionManager } from "../util/SessionManager";
import { UiStateStore } from "./mobile/UiStateStore";

export class LootButtonController {

	private static instance: LootButtonController;
	private component?: LootButton;
	private unsubscribeHandedness?: () => void;

	public static get(): LootButtonController {
		if (!LootButtonController.instance) {
			LootButtonController.instance = new LootButtonController();
		}
		return LootButtonController.instance;
	}

	private constructor() {
		// hidden constructor
	}

	public update() {
		if (!SessionManager.get().lootButtonEnabled()) {
			this.remove();
			return;
		}
		if (!this.component) {
			this.component = new LootButton();
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
		ConfigManager.get().set("loot.button", nextState);
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
