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

declare var stendhal: any;

import { RightPanelToggleButton } from "./interaction/RightPanelToggleButton";

import { SessionManager } from "../util/SessionManager";
import { UiStateStore } from "./mobile/UiStateStore";


/**
 * Controls visibility and lifecycle of the right panel toggle button.
 */
export class RightPanelToggleController {

	private static instance: RightPanelToggleController;
	private component?: RightPanelToggleButton;
	private unsubscribeState?: () => void;

	public static get(): RightPanelToggleController {
		if (!RightPanelToggleController.instance) {
			RightPanelToggleController.instance = new RightPanelToggleController();
		}
		return RightPanelToggleController.instance;
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
			this.component = new RightPanelToggleButton();
			this.subscribeState();
		}

		this.component.mount();
		this.component.setExpanded(UiStateStore.get().getState().rightPanelExpanded);
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
		this.unsubscribeState = store.subscribe(({ rightPanelExpanded }) => {
			this.component?.setExpanded(rightPanelExpanded);
			this.component?.update();
		});
	}
}
