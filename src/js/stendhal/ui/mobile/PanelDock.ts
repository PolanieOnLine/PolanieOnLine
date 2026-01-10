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

import { SoftwareJoystickController } from "../SoftwareJoystickController";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";


/**
 * Handles collapsing and expanding panel docks for narrow viewports.
 */
export class PanelDock {

	private readonly store = UiStateStore.get();

	private readonly root = document.getElementById("client")!;
	private unsubscribe?: () => void;


	constructor() {
		this.unsubscribe = this.store.subscribe((state) => this.applyState(state));
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
		const mobileFloating = this.root.classList.contains("mobile-floating-ui");
		if (!mobileFloating) {
			this.root.classList.remove("left-panel-collapsed");
			this.root.classList.remove("right-panel-collapsed");
			this.updateMinimapDock(true);
			SoftwareJoystickController.get().update();
			return;
		}

		const showPanels = state.mode === UiMode.PANELS;
		const showLeft = showPanels && state.handedness === UiHandedness.LEFT && state.leftPanelExpanded;
		const showRight = showPanels && state.handedness === UiHandedness.RIGHT && state.rightPanelExpanded;

		this.root.classList.toggle("left-panel-collapsed", !showLeft);
		this.root.classList.toggle("right-panel-collapsed", !showRight);
		this.updateMinimapDock(showLeft);
		SoftwareJoystickController.get().update();
	}

	private updateMinimapDock(showLeft: boolean) {
		const minimapFrame = document.getElementById("minimap-frame");
		const minimapOverlay = document.getElementById("minimap-overlay");
		const leftColumn = document.getElementById("leftColumn");
		if (!minimapFrame || !minimapOverlay || !leftColumn) {
			return;
		}

		if (showLeft) {
			if (minimapFrame.parentElement !== leftColumn) {
				leftColumn.insertBefore(minimapFrame, leftColumn.firstChild);
			}
			minimapOverlay.hidden = true;
			return;
		}

		if (minimapFrame.parentElement !== minimapOverlay) {
			minimapOverlay.append(minimapFrame);
		}
		minimapOverlay.hidden = false;
	}
}
