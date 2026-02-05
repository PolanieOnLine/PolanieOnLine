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

import { stendhal } from "../stendhal";

import { LeftPanelToggleButton } from "./interaction/LeftPanelToggleButton";

import { SessionManager } from "../util/SessionManager";
import { UiStateStore } from "./mobile/UiStateStore";


/**
 * Controls visibility and lifecycle of the left panel toggle button.
 */
export class LeftPanelToggleController {

	private static instance: LeftPanelToggleController;
	private component?: LeftPanelToggleButton;
	private unsubscribeState?: () => void;

	public static get(): LeftPanelToggleController {
		if (!LeftPanelToggleController.instance) {
			LeftPanelToggleController.instance = new LeftPanelToggleController();
		}
		return LeftPanelToggleController.instance;
	}

	private constructor() {
		// hidden singleton
	}

	public update() {
		if (!this.shouldShowToggle()) {
			this.remove();
			return;
		}

		if (!this.component) {
			this.component = new LeftPanelToggleButton();
			this.subscribeState();
		}

		this.component.mount();
		this.component.setExpanded(UiStateStore.get().getState().leftPanelExpanded);
	}

	public remove() {
		if (this.component) {
			this.component.unmount();
		}
		this.component = undefined;
		this.unsubscribeState?.();
		this.unsubscribeState = undefined;
	}

	private shouldShowToggle(): boolean {
		return SessionManager.get().touchOnly() && stendhal.ui.getMenuStyle() === "floating";
	}

	private subscribeState() {
		if (!this.component || this.unsubscribeState) {
			return;
		}

		const store = UiStateStore.get();
		this.unsubscribeState = store.subscribe(({ leftPanelExpanded }) => {
			this.component?.setExpanded(leftPanelExpanded);
			this.component?.update();
		});
	}
}
