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

import { InteractionButtonBase } from "./InteractionButtonBase";

import { TargetingController } from "../../game/TargetingController";
import { UiHandedness } from "../mobile/UiStateStore";

import { Entity } from "../../entity/Entity";

import { ElementClickListener } from "../../util/ElementClickListener";
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";


/**
 * Overlay attack button for quickly targeting the nearest enemy.
 */
export class AttackButton extends InteractionButtonBase {

	private readonly cooldownDuration = 800;
	private readonly longPressDelay = 450;
	private readonly repeatInterval = 900;
	private readonly doubleCycleLimit = 5;
	private cooldownId?: number;
	private repeatId?: number;
	private pressTimeoutId?: number;
	private longPressTriggered = false;
	private handedness: UiHandedness = UiHandedness.RIGHT;

	constructor() {
		const element = InteractionButtonBase.createButton({
			id: "attack-button",
			classes: ["attack-button"],
			ariaLabel: "Atakuj najbliższy cel",
			title: "Atakuj najbliższy cel"
		});

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = (evt: Event) => {
			this.onActivate(evt);
		};
		listener.onDoubleClick = (evt: Event) => {
			this.onDoubleActivate(evt);
		};

		this.componentElement.addEventListener("pointerdown", (evt) => this.onPressStart(evt));
		this.componentElement.addEventListener("pointerup", () => this.onPressEnd());
		this.componentElement.addEventListener("pointerleave", () => this.onPressEnd());
		this.componentElement.addEventListener("pointercancel", () => this.onPressEnd());

	}

	public override unmount() {
		super.unmount();
		this.setBusy(false);
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

	private onDoubleActivate(_evt: Event) {
		this.clearRepeat();
		this.tryCycleAttack();
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

	private tryCycleAttack(maxCandidates?: number): boolean {
		return this.performAttack(() => TargetingController.get().cycleAndAttack(maxCandidates));
	}

	private onPressStart(_evt: PointerEvent|TouchEvent|MouseEvent) {
		this.clearRepeat();
		this.pressTimeoutId = window.setTimeout(() => {
			this.longPressTriggered = true;
			if (!this.tryCycleAttack(this.doubleCycleLimit)) {
				return;
			}
			this.repeatId = window.setInterval(() => this.tryCycleAttack(this.doubleCycleLimit), this.repeatInterval);
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
		const margin = 20;
		const width = this.componentElement.offsetWidth || 64;
		const height = this.componentElement.offsetHeight || 64;
		const bounds = getViewportOverlayPosition({
			margin,
			elementWidth: width,
			elementHeight: height,
			offsetRight: getMobileRightPanelCollapsedInset()
		});
		if (!bounds) {
			return;
		}

		const left = bounds.baseRight;
		const top = bounds.baseBottom;
		const clampedLeft = Math.min(
			Math.max(left, bounds.safeLeft),
			bounds.safeRight < bounds.safeLeft ? bounds.safeLeft : bounds.safeRight
		);
		const clampedTop = Math.min(
			Math.max(top, bounds.safeTop),
			bounds.safeBottom < bounds.safeTop ? bounds.safeTop : bounds.safeBottom
		);

		this.componentElement.style.position = "absolute";
		this.componentElement.style.left = clampedLeft + "px";
		this.componentElement.style.top = clampedTop + "px";
	}
}
