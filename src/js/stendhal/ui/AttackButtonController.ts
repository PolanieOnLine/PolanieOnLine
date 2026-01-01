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

import { AttackButton } from "./component/AttackButton";

import { ConfigManager } from "../util/ConfigManager";
import { SessionManager } from "../util/SessionManager";
import { UiHandedness, UiStateStore } from "./mobile/UiStateStore";
import { TargetingController } from "../game/TargetingController";


/**
 * Manages lifecycle of the attack button overlay.
 */
export class AttackButtonController {

	private static instance: AttackButtonController;
	private component?: AttackButton;
	private cycleButton?: HTMLButtonElement;
	private unsubscribeHandedness?: () => void;


	/**
	 * Retrieves singleton instance.
	 */
	public static get(): AttackButtonController {
		if (!AttackButtonController.instance) {
			AttackButtonController.instance = new AttackButtonController();
		}
		return AttackButtonController.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Refreshes attack button state based on configuration and device.
	 */
	public update() {
		if (!SessionManager.get().attackButtonEnabled()) {
			this.remove();
			return;
		}
		if (!this.component) {
			this.component = new AttackButton();
			this.subscribeHandedness();
			this.bindCycleButton();
		}
		this.component.mount();
	}

	/**
	 * Removes button and clears instance.
	 */
	public remove() {
		if (this.component) {
			this.component.unmount();
		}
		if (this.cycleButton && this.cycleButton.parentElement) {
			this.cycleButton.remove();
		}
		this.component = undefined;
		this.cycleButton = undefined;
		this.unsubscribeHandedness?.();
		this.unsubscribeHandedness = undefined;
	}

	/**
	 * Toggles configuration value and refreshes component.
	 */
	public toggleSetting(nextState: boolean) {
		ConfigManager.get().set("attack.button", nextState);
		this.update();
	}

	private subscribeHandedness() {
		if (!this.component || this.unsubscribeHandedness) {
			return;
		}

		const store = UiStateStore.get();
		this.unsubscribeHandedness = store.subscribe((state) => {
			this.component?.setHandedness(state.handedness);
			this.positionCycleButton();
		});
		this.component.setHandedness(store.getState().handedness);
		this.positionCycleButton();
	}

	private bindCycleButton() {
		if (!this.component || this.cycleButton) {
			return;
		}
		const container = document.getElementById("attack-button-container") || document.body;
		container.classList.add("action-dock");

		const button = document.createElement("button");
		button.type = "button";
		button.classList.add("action-dock__button", "action-dock__button--cycle");
		button.textContent = "Zmień cel";
		button.title = "Zmień aktualny cel";
		button.setAttribute("aria-label", "Zmień aktualny cel");
		button.addEventListener("click", () => {
			const target = TargetingController.get().cycleAttackTargets();
			if (!target) {
				this.component?.flashDisabled();
			}
		});

		this.component.onPositionUpdate = () => this.positionCycleButton();
		this.cycleButton = button;
		container.appendChild(button);
		this.positionCycleButton();
	}

	private positionCycleButton() {
		if (!this.cycleButton || !this.component) {
			return;
		}
		const attackRect = this.component.getBoundingRect();
		if (!attackRect) {
			return;
		}

		const margin = 10;
		const store = UiStateStore.get();
		const handedness = store.getState().handedness;
		const top = attackRect.top + attackRect.height - 4;
		const left = handedness === UiHandedness.LEFT
			? attackRect.right + margin
			: attackRect.left - this.cycleButton.offsetWidth - margin;

		this.cycleButton.style.position = "fixed";
		this.cycleButton.style.top = `${top}px`;
		this.cycleButton.style.left = `${left}px`;
	}
}
