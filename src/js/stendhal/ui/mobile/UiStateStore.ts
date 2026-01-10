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

declare var stendhal: any;

import { SessionManager } from "../../util/SessionManager";

export enum UiMode {
	GAME = "game",
	PANELS = "panels"
}

export enum UiHandedness {
	LEFT = "left",
	RIGHT = "right"
}

export interface UiState {
	mode: UiMode;
	handedness: UiHandedness;
	chatExpanded: boolean;
	leftPanelExpanded: boolean;
	rightPanelExpanded: boolean;
}

type UiStateCallback = (state: UiState) => void;

/**
 * Simple pub/sub store for UI preferences used by the mobile layout.
 */
export class UiStateStore {

	private static instance: UiStateStore;

	private readonly callbacks: Set<UiStateCallback> = new Set();
	private initialized = false;
	private state: UiState = this.createDefaultState();


	static get(): UiStateStore {
		if (!UiStateStore.instance) {
			UiStateStore.instance = new UiStateStore();
		}
		return UiStateStore.instance;
	}

	private constructor() {
		// do nothing
	}

	/**
	 * Initializes the store using values stored in config/session.
	 */
	initFromConfig() {
		if (this.initialized) {
			return;
		}
		this.initialized = true;

		const config = stendhal.config;
		const mode = this.parseMode(config.get("ui.mode"));
		const handedness = this.parseHandedness(config.get("ui.handedness"));
		const chatExpanded = this.resolveChatExpanded();
		const leftPanelExpanded = this.resolveLeftPanelExpanded();
		const rightPanelExpanded = this.resolveRightPanelExpanded();

		this.state = { mode, handedness, chatExpanded, leftPanelExpanded, rightPanelExpanded };
	}

	getState(): UiState {
		return { ...this.state };
	}

	subscribe(cb: UiStateCallback): () => void {
		this.callbacks.add(cb);
		cb(this.getState());
		return () => this.callbacks.delete(cb);
	}

	setMode(mode: UiMode) {
		if (this.state.mode === mode) {
			return;
		}
		this.state = { ...this.state, mode };
		stendhal.config.set("ui.mode", mode);
		this.notify();
	}

	setHandedness(handedness: UiHandedness) {
		if (this.state.handedness === handedness) {
			return;
		}
		this.state = { ...this.state, handedness };
		stendhal.config.set("ui.handedness", handedness);
		this.notify();
	}

	setChatExpanded(expanded: boolean) {
		if (this.state.chatExpanded === expanded) {
			return;
		}
		this.state = { ...this.state, chatExpanded: expanded };
		stendhal.config.set("ui.chat.expanded", expanded);
		this.notify();
	}

	setLeftPanelExpanded(expanded: boolean) {
		if (this.state.leftPanelExpanded === expanded) {
			return;
		}
		this.state = { ...this.state, leftPanelExpanded: expanded };
		stendhal.config.set("ui.leftpanel.visible", expanded);
		this.notify();
	}

	resetLeftPanelExpandedForDesktop() {
		if (this.state.leftPanelExpanded) {
			return;
		}
		this.state = { ...this.state, leftPanelExpanded: true };
		stendhal.config.set("ui.leftpanel.visible", true);
		this.notify();
	}

	refreshLayout() {
		this.notify();
	}

	setRightPanelExpanded(expanded: boolean) {
		if (this.state.rightPanelExpanded === expanded) {
			return;
		}
		this.state = { ...this.state, rightPanelExpanded: expanded };
		stendhal.config.set("ui.rightpanel.visible", expanded);
		this.notify();
	}

	toggleRightPanel() {
		this.setRightPanelExpanded(!this.state.rightPanelExpanded);
	}

	toggleLeftPanel() {
		this.setLeftPanelExpanded(!this.state.leftPanelExpanded);
	}

	private notify() {
		for (const cb of this.callbacks) {
			cb(this.getState());
		}
	}

	private parseMode(mode: string|null|undefined): UiMode {
		if (mode && mode.toLowerCase() === UiMode.GAME) {
			return UiMode.GAME;
		}
		return UiMode.PANELS;
	}

	private parseHandedness(handedness: string|null|undefined): UiHandedness {
		if (handedness && handedness.toLowerCase() === UiHandedness.LEFT) {
			return UiHandedness.LEFT;
		}
		return UiHandedness.RIGHT;
	}

	private resolveChatExpanded(): boolean {
		if (stendhal.config.isSet("ui.chat.expanded")) {
			return stendhal.config.getBoolean("ui.chat.expanded");
		}
		if (!stendhal.config.getBoolean("chat.float")) {
			return true;
		}
		return stendhal.config.getBoolean("chat.visible");
	}

	private resolveRightPanelExpanded(): boolean {
		if (stendhal.config.isSet("ui.rightpanel.visible")) {
			return stendhal.config.getBoolean("ui.rightpanel.visible");
		}
		return this.resolveDefaultRightPanelExpanded();
	}

	private resolveLeftPanelExpanded(): boolean {
		if (stendhal.config.isSet("ui.leftpanel.visible")) {
			return stendhal.config.getBoolean("ui.leftpanel.visible");
		}
		return this.resolveDefaultLeftPanelExpanded();
	}

	private resolveDefaultRightPanelExpanded(): boolean {
		const touchOnly = SessionManager.get().touchOnly();
		const menuStyle = stendhal && stendhal.ui && typeof stendhal.ui.getMenuStyle === "function"
			? stendhal.ui.getMenuStyle()
			: undefined;
		if (touchOnly && menuStyle === "floating") {
			return false;
		}
		return true;
	}

	private resolveDefaultLeftPanelExpanded(): boolean {
		const touchOnly = SessionManager.get().touchOnly();
		const menuStyle = stendhal && stendhal.ui && typeof stendhal.ui.getMenuStyle === "function"
			? stendhal.ui.getMenuStyle()
			: undefined;
		if (touchOnly && menuStyle === "floating") {
			return false;
		}
		return true;
	}

	private createDefaultState(): UiState {
		return {
			mode: UiMode.PANELS,
			handedness: UiHandedness.RIGHT,
			chatExpanded: this.resolveChatExpanded(),
			leftPanelExpanded: this.resolveDefaultLeftPanelExpanded(),
			rightPanelExpanded: this.resolveDefaultRightPanelExpanded()
		};
	}
}
