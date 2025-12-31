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

import { Entity } from "../../entity/Entity";
import { RPEntity } from "../../entity/RPEntity";

import { Point } from "../../util/Point";
import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Overlay attack button for quickly targeting the nearest enemy.
 */
export class AttackButton extends Component {

	private readonly cooldownDuration = 800;
	private cooldownId?: number;
	private readonly boundUpdate: () => void;
	private radius = 0;
	private center: Point;

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

		this.boundUpdate = this.update.bind(this);
		this.center = new Point(0, 0);
	}

	/**
	 * Adds the attack button to the DOM.
	 */
	public mount() {
		// Append to the body to break out of the right-column layout constraints,
		// allowing for positioning relative to the viewport.
		const container = document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		// Set radius based on element size, assuming it's a square.
		this.radius = Math.floor(this.componentElement.offsetWidth / 2);

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
		if (this.cooldownId) {
			return;
		}

		const target = this.findNearestTarget();
		if (!target) {
			this.flashDisabled();
			return;
		}

		marauroa.clientFramework.sendAction({
			type: "attack",
			target: "#" + target["id"],
			zone: marauroa.currentZoneName
		});

		this.startCooldown();
	}

	/**
	 * Temporarily highlight disabled state when no target found.
	 */
	private flashDisabled() {
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

	/**
	 * Updates button positioning based on its center coordinates.
	 * This is similar to how the joystick is positioned, ensuring that
	 * the button stays in a fixed position relative to the canvas,
	 * even when the window is resized or scrolled.
	 */
	public update(): void {
		const center = this.updateCenter();
		const centerX = center.x;
		const centerY = center.y;

		// Use fixed positioning to place the button relative to the viewport,
		// making it independent of its original container's layout.
		this.componentElement.style.position = "fixed";
		this.componentElement.style.left = ((centerX - this.radius) - 50) + "px";
		this.componentElement.style.top = ((centerY - this.radius) - 100) + "px";
	}

	/**
	 * Updates cached center position.
	 */
	private updateCenter(): Point {
		const resolved = this.resolveCenter();
		this.center = resolved;
		return resolved;
	}

	/**
	 * Determines the center position, placing it on the bottom-right of the canvas.
	 */
	private resolveCenter(): Point {
		const viewport = document.getElementById("viewport"); // canvas is the viewport
		if (viewport) {
			const rect = viewport.getBoundingClientRect();
			const margin = 20;

			const x = rect.right - this.radius - margin;
			const y = rect.bottom - this.radius - margin;
			return new Point(x, y);
		}

		// Fallback if canvas is not found
		return new Point(0, 0);
	}

	/**
	 * Finds the nearest attackable entity around the player.
	 *
	 * @return {RPEntity|undefined}
	 *   Target entity or `undefined` if none found.
	 */
	private findNearestTarget(): RPEntity | undefined {
		if (!marauroa.me) {
			return;
		}

		const entities = marauroa.currentZone;
		let nearest: RPEntity | undefined;
		let nearestDist = Number.MAX_SAFE_INTEGER;

		for (const key in entities) {
			const ent = entities[key];
			if (!this.isAttackable(ent)) {
				continue;
			}
			const dist = marauroa.me.getDistanceTo(ent);
			if (dist < 0) {
				continue;
			}
			if (dist < nearestDist) {
				nearest = ent as RPEntity;
				nearestDist = dist;
			}
		}

		return nearest;
	}

	/**
	 * Determines if entity can be targeted by an attack.
	 *
	 * @param ent {Entity}
	 *   Entity to check.
	 * @return {boolean}
	 *   `true` if target considered attackable.
	 */
	private isAttackable(ent: any): ent is RPEntity {
		if (!ent) {
			return false;
		}
		if (ent === marauroa.me) {
			return false;
		}
		// Check for HP to ensure it's a living entity (Creature or Player)
		if (!ent["hp"] || ent["hp"] <= 0) {
			return false;
		}
		// Entities with custom menu entries are typically not attackable (see RPEntity.buildActions)
		if (ent["menu"]) {
			return false;
		}
		return typeof ent.isVisibleToAction === 'function' ? ent.isVisibleToAction(false) : true;
	}
}
