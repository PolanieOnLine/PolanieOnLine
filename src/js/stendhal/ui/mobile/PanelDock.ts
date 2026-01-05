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

import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";


/**
 * Handles collapsing and expanding panel docks for narrow viewports.
 */
export class PanelDock {

	private readonly store = UiStateStore.get();

	private readonly root = document.getElementById("client")!;
	private readonly leftToggle = document.getElementById("left-panel-toggle") as HTMLButtonElement|null;
	private readonly rightToggle = document.getElementById("right-panel-toggle") as HTMLButtonElement|null;
	private unsubscribe?: () => void;


	constructor() {
		this.unsubscribe = this.store.subscribe((state) => this.applyState(state));

		this.leftToggle?.addEventListener("click", () => this.onToggle(UiHandedness.LEFT));
		this.rightToggle?.addEventListener("click", () => this.onToggle(UiHandedness.RIGHT));
	}

	destroy() {
		this.unsubscribe?.();
		this.unsubscribe = undefined;
	}

	private onToggle(targetHandedness: UiHandedness) {
		const state = this.store.getState();
		if (state.mode === UiMode.PANELS && state.handedness === targetHandedness) {
			this.store.setMode(UiMode.GAME);
			return;
		}
		this.store.setHandedness(targetHandedness);
		this.store.setMode(UiMode.PANELS);
	}

	private applyState(state: UiState) {
		const showPanels = state.mode === UiMode.PANELS;
		const showLeft = showPanels && state.handedness === UiHandedness.LEFT;
		const showRight = showPanels && state.handedness === UiHandedness.RIGHT;

		this.root.classList.toggle("left-panel-collapsed", !showLeft);
		this.root.classList.toggle("right-panel-collapsed", !showRight);

		if (this.leftToggle) {
			this.leftToggle.setAttribute("aria-expanded", showLeft ? "true" : "false");
		}
		if (this.rightToggle) {
			this.rightToggle.setAttribute("aria-expanded", showRight ? "true" : "false");
		}
	}
}