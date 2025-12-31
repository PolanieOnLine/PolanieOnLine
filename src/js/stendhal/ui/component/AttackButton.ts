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

import { ElementClickListener } from "../../util/ElementClickListener";


/**
 * Overlay attack button for quickly targeting the nearest enemy.
 */
export class AttackButton extends Component {

	private readonly cooldownDuration = 800;
	private cooldownId?: number;

	constructor() {
		const element = document.createElement("button");
		element.id = "attack-button";
		element.classList.add("attack-button", "joystick-button", "hidden");
		element.setAttribute("aria-label", "Atakuj najbliższy cel");
		element.title = "Atakuj najbliższy cel";

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = (evt: Event) => {
			this.onActivate(evt);
		};
	}

	/**
	 * Adds the attack button to the DOM.
	 */
	public mount() {
		const container = document.getElementById("attack-button-container");
		if (!container) {
			return;
		}
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}
		this.componentElement.classList.remove("hidden");
	}

	/**
	 * Removes the attack button from DOM.
	 */
	public unmount() {
		const container = document.getElementById("attack-button-container");
		if (container && container.contains(this.componentElement)) {
			container.removeChild(this.componentElement);
		}
		this.componentElement.classList.add("hidden");
		this.setBusy(false);
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
	 * Finds the nearest attackable entity around the player.
	 *
	 * @return {RPEntity|undefined}
	 *   Target entity or `undefined` if none found.
	 */
	private findNearestTarget(): RPEntity | undefined {
		if (!marauroa.me || !stendhal.zone) {
			return;
		}

		const entities: Entity[] = (stendhal.zone as any)["entities"] || [];
		let nearest: RPEntity | undefined;
		let nearestDist = Number.MAX_SAFE_INTEGER;

		for (const ent of entities) {
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
	private isAttackable(ent: Entity): ent is RPEntity {
		if (!(ent instanceof RPEntity)) {
			return false;
		}
		if (ent === marauroa.me) {
			return false;
		}
		// Entities with custom menu entries are typically not attackable (see RPEntity.buildActions)
		if ((ent as any)["menu"]) {
			return false;
		}
		return ent.isVisibleToAction(false);
	}
}
