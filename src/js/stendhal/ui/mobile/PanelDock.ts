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
import { singletons } from "../../SingletonRepo";
import { UiHandedness, UiMode, UiState, UiStateStore } from "./UiStateStore";


/**
 * Handles collapsing and expanding panel docks for narrow viewports.
 */
export class PanelDock {

	private readonly store = UiStateStore.get();

	private readonly root = document.getElementById("client")!;
	private minimapOverlay: HTMLElement | null = null;
	private minimapToggle: HTMLButtonElement | null = null;
	private minimapToggleBound = false;
	private unsubscribe?: () => void;


	constructor() {
		this.unsubscribe = this.store.subscribe((state) => this.applyState(state));
		this.bindMinimapToggle();
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
		this.bindMinimapToggle();
		this.ensureMinimapOverlay();
		const minimapFrame = document.getElementById("minimap-frame");
		const mobileFloating = this.root.classList.contains("mobile-floating-ui");
		if (!mobileFloating) {
			this.root.classList.remove("left-panel-collapsed");
			this.root.classList.remove("right-panel-collapsed");
			if (state.minimapMinimized) {
				this.store.setMinimapMinimized(false);
			}
			this.minimapOverlay?.classList.remove("minimap-minimized");
			minimapFrame?.classList.remove("minimap-minimized-frame");
			this.updateMinimapDock(true);
			SoftwareJoystickController.get().update();
			return;
		}

		const showPanels = state.mode === UiMode.PANELS;
		const showLeft = showPanels && state.handedness === UiHandedness.LEFT && state.leftPanelExpanded;
		const showRight = showPanels && state.handedness === UiHandedness.RIGHT && state.rightPanelExpanded;

		this.root.classList.toggle("left-panel-collapsed", !showLeft);
		this.root.classList.toggle("right-panel-collapsed", !showRight);
		if (showLeft && state.minimapMinimized) {
			this.store.setMinimapMinimized(false);
		}
		const allowMinimapMinimized = !showLeft;
		this.minimapOverlay?.classList.toggle(
			"minimap-minimized",
			allowMinimapMinimized && state.minimapMinimized
		);
		minimapFrame?.classList.toggle(
			"minimap-minimized-frame",
			allowMinimapMinimized && state.minimapMinimized
		);
		requestAnimationFrame(() => singletons.getQuickSlotsController().update());
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

	private ensureMinimapOverlay() {
		if (!this.minimapOverlay) {
			this.minimapOverlay = document.getElementById("minimap-overlay");
		}
	}

	private bindMinimapToggle() {
		if (this.minimapToggleBound) {
			return;
		}
		if (!this.minimapToggle) {
			this.minimapToggle = document.getElementById("minimap-toggle") as HTMLButtonElement | null;
		}
		if (!this.minimapToggle) {
			return;
		}
		this.minimapToggle.addEventListener("click", (event) => {
			event.preventDefault();
			event.stopPropagation();
			this.store.toggleMinimapMinimized();
		});
		this.minimapToggle.addEventListener("pointerdown", (event) => {
			event.stopPropagation();
		});
		this.minimapToggle.addEventListener("pointerup", (event) => {
			event.stopPropagation();
		});
		this.minimapToggleBound = true;
	}
}
