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

declare const stendhal: any;

import { RightPanelVisibilityManager } from "../RightPanelVisibilityManager";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";

function togglePanelForHandedness(handedness: UiHandedness): boolean {
	const store = UiStateStore.get();
	const state = store.getState();
	const rightPanelVisibility = RightPanelVisibilityManager.get();

	if (handedness === UiHandedness.RIGHT && rightPanelVisibility.managesFloatingLayout()) {
		const nextVisible = rightPanelVisibility.isVisible()
			? rightPanelVisibility.toggleVisibility()
			: rightPanelVisibility.setVisible(true);
		store.setHandedness(UiHandedness.RIGHT);
		if (nextVisible) {
			store.setMode(UiMode.PANELS);
		}
		return nextVisible;
	}

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

	private currentState: UiState;
	private readonly root = document.getElementById("client")!;
	private readonly rightPanelVisibility = RightPanelVisibilityManager.get();
	private rightColumn?: HTMLElement|null;
	private unsubscribe?: () => void;
	private unsubscribeRightPanel?: () => void;


	constructor() {
		this.rightPanelVisibility.init();
		this.rightColumn = document.getElementById("rightColumn");
		this.currentState = this.store.getState();
		this.unsubscribe = this.store.subscribe((state) => this.applyState(state));
		this.unsubscribeRightPanel = this.rightPanelVisibility.subscribe(() => this.applyState(this.currentState));
	}

	destroy() {
		this.unsubscribe?.();
		this.unsubscribe = undefined;
		this.unsubscribeRightPanel?.();
		this.unsubscribeRightPanel = undefined;
	}

	private applyState(state: UiState) {
		this.currentState = state;
		const showPanels = state.mode === UiMode.PANELS;
		const showLeft = showPanels && state.handedness === UiHandedness.LEFT;
		const touchOnly = stendhal.session.touchOnly();
		const managesFloatingLayout = this.rightPanelVisibility.managesFloatingLayout();
		const rightVisible = this.rightPanelVisibility.isVisible();
		const showRight = showPanels && state.handedness === UiHandedness.RIGHT && rightVisible;

		this.root.classList.toggle("left-panel-collapsed", !showLeft);

		if (!touchOnly && !managesFloatingLayout) {
			this.root.classList.toggle("right-panel-collapsed", !rightVisible);
			this.updateRightColumnDisplay(rightVisible);
			return;
		}

		this.root.classList.toggle("right-panel-collapsed", !showRight);

		if (touchOnly && !managesFloatingLayout) {
			this.updateRightColumnDisplay(showRight);
		}
	}

	private updateRightColumnDisplay(visible: boolean) {
		if (!this.rightColumn || !document.body.contains(this.rightColumn)) {
			this.rightColumn = document.getElementById("rightColumn");
		}
		if (!this.rightColumn) {
			return;
		}
		if (visible) {
			this.rightColumn.style.removeProperty("display");
			this.rightColumn.removeAttribute("aria-hidden");
		} else {
			this.rightColumn.style.display = "none";
			this.rightColumn.setAttribute("aria-hidden", "true");
		}
	}
}
