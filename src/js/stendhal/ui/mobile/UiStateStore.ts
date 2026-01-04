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
}

type UiStateCallback = (state: UiState) => void;

/**
 * Simple pub/sub store for UI preferences used by the mobile layout.
 */
export class UiStateStore {

	private static instance: UiStateStore;

	private readonly callbacks: Set<UiStateCallback> = new Set();
	private initialized = false;
	private state: UiState = {
		mode: UiMode.PANELS,
		handedness: UiHandedness.RIGHT,
		chatExpanded: false
	};


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

		this.state = { mode, handedness, chatExpanded };
		if (!stendhal.session.touchOnly()) {
			this.state.mode = UiMode.PANELS;
		}
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
		return stendhal.config.getBoolean("chat.visible");
	}
}
