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

function togglePanelForHandedness(handedness: UiHandedness): boolean {
	const store = UiStateStore.get();
	const state = store.getState();

	if (state.mode === UiMode.PANELS && state.handedness === handedness) {
		store.setMode(UiMode.GAME);
		return false;
	}

	store.setHandedness(handedness);
	store.setMode(UiMode.PANELS);
	return true;
}

export function toggleRightPanel(): boolean {
	return togglePanelForHandedness(UiHandedness.RIGHT);
}

/**
 * Handles collapsing and expanding panel docks for narrow viewports.
 */
export class PanelDock {

	private readonly store = UiStateStore.get();

	private root: HTMLElement | null;
	private readonly leftToggle: HTMLButtonElement | null;
	private readonly rightToggle: HTMLButtonElement | null;
	private unsubscribe?: () => void;


	constructor() {
		this.root = document.getElementById("client");
		this.leftToggle = document.getElementById("left-panel-toggle") as HTMLButtonElement | null;
		this.rightToggle = document.getElementById("right-panel-toggle") as HTMLButtonElement | null;
		this.unsubscribe = this.store.subscribe((state) => this.applyState(state));

		this.leftToggle?.addEventListener("click", () => this.onToggle(UiHandedness.LEFT));
		this.rightToggle?.addEventListener("click", () => this.onToggle(UiHandedness.RIGHT));
	}

	destroy() {
		this.unsubscribe?.();
		this.unsubscribe = undefined;
	}

	private onToggle(targetHandedness: UiHandedness) {
		togglePanelForHandedness(targetHandedness);
	}

	private applyState(state: UiState) {
		if (!this.root) {
			this.root = document.getElementById("client");
		}
		if (!this.root) {
			return;
		}

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
