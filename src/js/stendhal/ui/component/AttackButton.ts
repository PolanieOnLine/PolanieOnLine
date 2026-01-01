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

declare var marauroa: any;
declare var stendhal: any;

import { Component } from "../toolkit/Component";

import { TargetingController } from "../../game/TargetingController";
import { UiHandedness } from "../mobile/UiStateStore";

import { Entity } from "../../entity/Entity";

import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Overlay attack button for quickly targeting the nearest enemy.
 */
export class AttackButton extends Component {

	private readonly cooldownDuration = 800;
	private readonly longPressDelay = 450;
	private readonly repeatInterval = 900;
	private cooldownId?: number;
	private repeatId?: number;
	private pressTimeoutId?: number;
	private longPressTriggered = false;
	private readonly boundUpdate: () => void;
	private handedness: UiHandedness = UiHandedness.RIGHT;

	constructor() {
		const element = document.createElement("button");
		element.id = "attack-button";
		element.classList.add("attack-button", "unclickable", "hidden");
		element.setAttribute("aria-label", "Atakuj najbliższy cel");
		element.title = "Atakuj najbliższy cel";

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = (evt: Event) => {
			this.onActivate(evt);
		};

		this.componentElement.addEventListener("pointerdown", (evt) => this.onPressStart(evt));
		this.componentElement.addEventListener("pointerup", () => this.onPressEnd());
		this.componentElement.addEventListener("pointerleave", () => this.onPressEnd());
		this.componentElement.addEventListener("pointercancel", () => this.onPressEnd());

		this.boundUpdate = this.update.bind(this);
	}

	/**
	 * Adds the attack button to the DOM.
	 */
	public mount() {
		// Append to the body to break out of the right-column layout constraints,
		// allowing for positioning relative to the viewport.
		const container = document.getElementById("attack-button-container") || document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		this.componentElement.classList.remove("hidden");

		this.update();
		window.addEventListener("resize", this.boundUpdate);
	}

	/**
	 * Removes the attack button from DOM.
	 */
	public unmount() {
		if (this.componentElement.parentElement) {
			this.componentElement.remove();
		}
		this.componentElement.classList.add("hidden");
		this.setBusy(false);
		window.removeEventListener("resize", this.boundUpdate);
		this.clearRepeat();
	}

	/**
	 * Sets visual disabled/cooldown state.
	 *
	 * @param busy {boolean}
	 *   `true` to show cooldown/disabled styling.
	 */
	public setBusy(busy: boolean) {
		this.componentElement.classList.toggle("attack-button--disabled", busy);
	}

	/**
	 * Called when the button is activated via click or touch.
	 */
	private onActivate(_evt: Event) {
		if (this.longPressTriggered) {
			return;
		}
		this.tryAttack();
	}

	private performAttack(attackFn: () => Entity | undefined): boolean {
		if (this.cooldownId) {
			return true;
		}

		const target = attackFn();
		if (!target) {
			this.flashDisabled();
			return false;
		}

		this.startCooldown();
		return true;
	}

	private tryAttack(): boolean {
		return this.performAttack(() => TargetingController.get().attackCurrentOrNearest());
	}

	private tryCycleAttack(): boolean {
		return this.performAttack(() => TargetingController.get().cycleAndAttack());
	}

	private onPressStart(_evt: PointerEvent|TouchEvent|MouseEvent) {
		this.clearRepeat();
		this.pressTimeoutId = window.setTimeout(() => {
			this.longPressTriggered = true;
			if (!this.tryCycleAttack()) {
				return;
			}
			this.repeatId = window.setInterval(() => this.tryCycleAttack(), this.repeatInterval);
		}, this.longPressDelay);
	}

	private onPressEnd() {
		this.clearRepeat();
	}

	private clearRepeat() {
		if (this.pressTimeoutId) {
			window.clearTimeout(this.pressTimeoutId);
			this.pressTimeoutId = undefined;
		}
		if (this.repeatId) {
			window.clearInterval(this.repeatId);
			this.repeatId = undefined;
		}
		this.longPressTriggered = false;
	}

	/**
	 * Temporarily highlight disabled state when no target found.
	 */
	public flashDisabled() {
		this.setBusy(true);
		window.setTimeout(() => this.setBusy(false), 400);
	}

	/**
	 * Starts a short cooldown animation to avoid spamming.
	 */
	private startCooldown() {
		this.setBusy(true);
		this.cooldownId = window.setTimeout(() => {
			this.setBusy(false);
			this.cooldownId = undefined;
		}, this.cooldownDuration);
	}

	public setHandedness(handedness: UiHandedness) {
		if (this.handedness === handedness) {
			return;
		}
		this.handedness = handedness;
		this.update();
	}

	/**
	 * Updates button positioning based on its center coordinates.
	 * This is similar to how the joystick is positioned, ensuring that
	 * the button stays in a fixed position relative to the canvas,
	 * even when the window is resized or scrolled.
	 */
	public update(): void {
		const viewport = document.getElementById("viewport");
		const margin = 20;
		const width = this.componentElement.offsetWidth || 64;
		const height = this.componentElement.offsetHeight || 64;

		let left = margin;
		let top = margin;

		if (viewport) {
			const rect = viewport.getBoundingClientRect();
			left = rect.right - width - margin;
			top = rect.bottom - height - margin;
		}

		// Use fixed positioning to place the button relative to the viewport,
		// making it independent of its original container's layout.
		this.componentElement.style.position = "fixed";
		this.componentElement.style.left = left + "px";
		this.componentElement.style.top = top + "px";
	}
}
