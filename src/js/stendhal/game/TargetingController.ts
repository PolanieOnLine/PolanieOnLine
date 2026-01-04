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

import { Creature } from "../entity/Creature";
import { Entity } from "../entity/Entity";
import { NPC } from "../entity/NPC";
import { Player } from "../entity/Player";
import { RPEntity } from "../entity/RPEntity";
import { Item } from "../entity/Item";
import { Inventory } from "../ui/Inventory";
import { ConfigManager } from "../util/ConfigManager";


type TargetType = "creature" | "player" | "npc";

export interface TargetFilter {
	types?: TargetType[];
	predicate?: (entity: Entity) => boolean;
	requireHealth?: boolean;
	respectPreferences?: boolean;
	maxDistance?: number;
}


/**
 * Centralized controller for selecting and executing actions on nearby targets.
 */
export class TargetingController {

	private static instance: TargetingController;
	private static readonly INTERACT_RANGE = 2;
	private static readonly PICKUP_RANGE = 2;
	private current?: Entity;
	private readonly config = ConfigManager.get();


	/**
	 * Retrieves singleton instance.
	 */
	public static get(): TargetingController {
		if (!TargetingController.instance) {
			TargetingController.instance = new TargetingController();
		}
		return TargetingController.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Returns the currently selected target.
	 */
	public getCurrent(): Entity|undefined {
		return this.current;
	}

	/**
	 * Sets the current target.
	 */
	public setCurrent(target?: Entity) {
		this.current = target;
	}

	/**
	 * Finds the nearest target that matches the provided filter.
	 *
	 * @param filter {TargetFilter}
	 *   Optional filter options for narrowing down valid targets.
	 * @return {Entity|undefined}
	 *   Nearest matching entity or `undefined` if none found.
	 */
	public getNearest(filter?: TargetFilter): Entity|undefined {
		const resolvedFilter = this.resolveFilter(filter);
		const targets = this.getTargets(resolvedFilter);

		if (targets.length) {
			this.current = targets[0];
			return this.current;
		}
		return;
	}

	/**
	 * Cycles through valid targets, moving focus to the next in range.
	 *
	 * @param filterSet {TargetFilter[]}
	 *   Ordered list of filters to apply when gathering targets.
	 * @return {Entity|undefined}
	 *   Newly selected entity or `undefined` if none available.
	 */
	public cycle(filterSet?: TargetFilter[]): Entity|undefined {
		const filters = filterSet && filterSet.length ? filterSet : [{}];
		const candidates: Entity[] = [];
		for (const filter of filters) {
			for (const target of this.getTargets(this.resolveFilter(filter))) {
				if (candidates.indexOf(target) === -1) {
					candidates.push(target);
				}
			}
		}

		if (!candidates.length) {
			this.current = undefined;
			return;
		}

		if (!this.current || candidates.indexOf(this.current) === -1) {
			this.current = candidates[0];
			return this.current;
		}

		const currentIndex = candidates.indexOf(this.current);
		const nextIndex = currentIndex + 1 >= candidates.length ? 0 : currentIndex + 1;

		this.current = candidates[nextIndex];
		return this.current;
	}

	/**
	 * Attacks the current target if still valid, otherwise the nearest one.
	 */
	public attackCurrentOrNearest(): RPEntity|undefined {
		const filter = this.buildAttackFilter();

		const target = this.getNearest(filter) as RPEntity;

		if (!this.isAttackable(target, filter)) {
			this.current = undefined;
			return;
		}

		this.current = target;
		this.sendAction("attack", target);
		return target;
	}

    /**
	 * Cycles through attackable targets using the active filters.
	 */
	public cycleAttackTargets(): Entity|undefined {
		return this.cycle([this.buildAttackFilter()]);
	}

    /**
	 * Attacks the current target if valid; otherwise falls back to nearest.
	 */
	public attackCurrentWithFallback(): RPEntity|undefined {
		const filter = this.buildAttackFilter();
		if (this.isAttackable(this.current, filter)) {
			const current = this.current as RPEntity;
			this.sendAction("attack", current);
			return current;
		}
		return this.attackCurrentOrNearest();
	}

	/**
	 * Cycles through attackable targets using the active filters and attacks the result.
	 */
	public cycleAndAttack(): Entity|undefined {
		const filter = this.buildAttackFilter();
		const target = this.cycle([filter]);
		if (!target || !this.isAttackable(target, filter)) {
			this.current = undefined;
			return;
		}
		this.current = target;
		this.sendAction("attack", target);
		return target;
	}

	/**
	 * Interacts with the nearest visible entity, preferring NPCs for talking.
	 */
	public interactNearest(): Entity|undefined {
		const target = this.getNearestNpcInteractable();

		if (!target) {
			return;
		}

		this.current = target;

		return target;
	}

	/**
	 * Retrieves nearest interactable entity without sending an action.
	 */
	public getNearestInteractable(): Entity|undefined {
		return this.getNearestNpcInteractable();
	}

	/**
	 * Checks if an interactable entity is within range.
	 */
	public hasInteractTarget(): boolean {
		return !!this.getNearestNpcInteractable();
	}

	/**
	 * Sends a pickup action to the nearest visible item.
	 */
	public pickupNearest(): Entity|undefined {
		const containerLoot = this.getNearestContainerLoot();
		if (containerLoot) {
			this.sendEquipToBag(containerLoot);
			return containerLoot;
		}

		const groundItem = this.getNearestGroundItem();
		if (groundItem) {
			this.sendEquipToBag(groundItem);
			return groundItem;
		}

		return;
	}

	/**
	 * Checks if a pickup target is available nearby.
	 */
	public hasPickupTarget(): boolean {
		return !!this.getNearestContainerLoot() || !!this.getNearestGroundItem();
	}

	/**
	 * Sends an action command to the server with the provided target.
	 */
	private sendAction(type: string, target: Entity) {
		if (!target || typeof target["id"] === "undefined") {
			return;
		}

		marauroa.clientFramework.sendAction({
			type: type,
			target: "#" + target["id"],
			zone: marauroa.currentZoneName
		});
	}

	private sendEquipToBag(item: Item) {
		if (!item || typeof item.getIdPath !== "function" || !marauroa?.me) {
			return;
		}
		marauroa.clientFramework.sendAction({
			type: "equip",
			"source_path": item.getIdPath(),
			"target_path": "[" + marauroa.me["id"] + "\tbag]",
			"clicked": "",
			"zone": marauroa.currentZoneName
		});
	}

	/**
	 * Collects all valid targets for the given filter, sorted by distance.
	 */
	private getTargets(filter: TargetFilter): Entity[] {
		if (!marauroa || !marauroa.currentZone || !marauroa.me) {
			return [];
		}

		const targets: Entity[] = [];
		for (const key in marauroa.currentZone) {
			if (!marauroa.currentZone.hasOwnProperty(key) || typeof marauroa.currentZone[key] === "function") {
				continue;
			}
			const entity = marauroa.currentZone[key];
			if (this.isCandidate(entity, filter)) {
				targets.push(entity as Entity);
			}
		}

		targets.sort((a, b) => {
			const distA = this.getDistanceTo(a);
			const distB = this.getDistanceTo(b);

			if (distA === distB) {
				return (a["id"] || 0) - (b["id"] || 0);
			}
			return distA - distB;
		});

		return targets;
	}

	private getNearestContainerLoot(): Item|undefined {
		const containers = Inventory.get().getInventory();
		for (const container of containers) {
			if (container.getSlotName() !== "content") {
				continue;
			}
			const boundObject = container.getBoundObject();
			if (boundObject && !this.isWithinRange(boundObject, TargetingController.PICKUP_RANGE)) {
				continue;
			}
			const items = container.getItems();
			const loot = items.find((item) => typeof (item as any)["id"] !== "undefined");
			if (loot) {
				return loot;
			}
		}
	}

	private getNearestGroundItem(): Item|undefined {
		const candidates = this.getTargets({
			requireHealth: false,
			respectPreferences: false,
			predicate: (entity: Entity) => (entity instanceof Item) && typeof (entity as any)["id"] !== "undefined",
			maxDistance: TargetingController.PICKUP_RANGE
		}) as Item[];
		return candidates.length ? candidates[0] : undefined;
	}

	/**
	 * Checks whether an entity matches targeting criteria.
	 */
	private isCandidate(entity: any, filter: TargetFilter): entity is Entity {
		if (!(entity instanceof Entity)) {
			return false;
		}
		if (entity === marauroa.me) {
			return false;
		}
		if (typeof entity.isVisibleToAction !== "function" || !entity.isVisibleToAction(false)) {
			return false;
		}
		if (!this.isDistanceValid(entity)) {
			return false;
		}
		if (filter.maxDistance !== undefined && !this.isWithinRange(entity, filter.maxDistance)) {
			return false;
		}

		if (filter.requireHealth !== false) {
			if (typeof entity["hp"] !== "number" || entity["hp"] <= 0) {
				return false;
			}
		} else if (typeof entity["hp"] === "number" && entity["hp"] <= 0) {
			return false;
		}

		if (!this.matchesPreferences(entity, filter.respectPreferences !== false)) {
			return false;
		}

		if (filter.types && filter.types.length && !this.matchesTypes(entity, filter.types)) {
			return false;
		}

		if (filter.predicate && !filter.predicate(entity)) {
			return false;
		}

		return true;
	}

	/**
	 * Confirms if an entity can be targeted for attacks.
	 */
	private isAttackable(entity: any, filter: TargetFilter): entity is RPEntity {
		if (!(entity instanceof RPEntity)) {
			return false;
		}
		if (entity["menu"]) {
			return false;
		}

		return this.isCandidate(entity, filter);
	}

	/**
	 * Validates distance information for an entity.
	 */
	private isDistanceValid(entity: Entity): boolean {
		if (!marauroa || !marauroa.me || typeof marauroa.me.getDistanceTo !== "function") {
			return false;
		}

		const distance = marauroa.me.getDistanceTo(entity);
		return distance >= 0 && Number.isFinite(distance);
	}

	private isWithinRange(entity: Entity, maxDistance: number): boolean {
		const distance = this.getDistanceTo(entity);
		return distance <= maxDistance;
	}

	private getDistanceTo(entity: Entity): number {
		if (!marauroa?.me || typeof marauroa.me.getDistanceTo !== "function") {
			return Infinity;
		}
		const distance = marauroa.me.getDistanceTo(entity);
		return Number.isFinite(distance) ? distance : Infinity;
	}

	/**
	 * Matches entities by type against the provided list.
	 */
	private matchesTypes(entity: Entity, types: TargetType[]): boolean {
		const type = this.getTargetType(entity);

		return !!type && types.indexOf(type) > -1;
	}

	/**
	 * Identifies the target type for filtering and preference checks.
	 */
	private getTargetType(entity: Entity): TargetType|undefined {
		if (entity instanceof Player) {
			return "player";
		}
		if (entity instanceof NPC) {
			return "npc";
		}
		if (entity instanceof Creature) {
			return "creature";
		}
	}

	/**
	 * Ensures preference settings from configuration are respected.
	 */
	private matchesPreferences(entity: Entity, respectPreferences: boolean): boolean {
		if (!respectPreferences) {
			return true;
		}

		const type = this.getTargetType(entity);

		if (type === "player") {
			return this.config.getBoolean("attack.target.players");
		}

		return true;
	}

    /**
	 * Builds filter definition for attack-focused targeting.
	 */
	private buildAttackFilter(): TargetFilter {
		return {
			requireHealth: true,
			respectPreferences: true,
			types: this.getAttackableTypes()
		};
	}

	private getAttackableTypes(): TargetType[] {
		const types: TargetType[] = ["creature"];

		if (this.config.getBoolean("attack.target.players")) {
			types.push("player");
		}

		return types;
	}

	/**
	 * Merges user supplied options with defaults.
	 */
	private resolveFilter(filter?: TargetFilter): TargetFilter {
		const defaults: TargetFilter = {
			requireHealth: true,
			respectPreferences: true
		};

		return Object.assign({}, defaults, filter);
	}

	private getNearestNpcInteractable(): Entity|undefined {
		const target = this.getNearest({
			requireHealth: true,
			respectPreferences: false,
			types: ["npc"],
			maxDistance: TargetingController.INTERACT_RANGE
		});

		if (!target || !this.isCandidate(target, {requireHealth: false, respectPreferences: false, types: ["npc"], maxDistance: TargetingController.INTERACT_RANGE})) {
			return;
		}

		return target;
	}
}
