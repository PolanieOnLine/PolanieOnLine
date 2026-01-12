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

import { QuickSlots } from "./interaction/QuickSlots";
import { SessionManager } from "../util/SessionManager";
import { UiStateStore } from "./mobile/UiStateStore";

/**
 * Controls visibility and lifecycle of quick slots overlay.
 */
export class QuickSlotsController {

	private static instance: QuickSlotsController;
	private component?: QuickSlots;
	private unsubscribeState?: () => void;

	public static get(): QuickSlotsController {
		if (!QuickSlotsController.instance) {
			QuickSlotsController.instance = new QuickSlotsController();
		}
		return QuickSlotsController.instance;
	}

	private constructor() {
		// hidden singleton
	}

	public update() {
		if (!this.shouldShow()) {
			this.remove();
			return;
		}

		if (!this.component) {
			this.component = new QuickSlots();
			this.subscribeState();
			this.component.mount();
			return;
		}

		this.component.update();
	}

	public remove() {
		if (this.component) {
			this.component.unmount();
		}
		this.component = undefined;
		this.unsubscribeState?.();
		this.unsubscribeState = undefined;
	}

	private shouldShow(): boolean {
		return SessionManager.get().touchOnly();
	}

	private subscribeState() {
		if (!this.component || this.unsubscribeState) {
			return;
		}

		this.unsubscribeState = UiStateStore.get().subscribe(() => {
			this.component?.update();
		});
	}
}
