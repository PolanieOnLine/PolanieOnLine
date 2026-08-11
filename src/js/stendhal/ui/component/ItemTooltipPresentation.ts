/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { marauroa } from "marauroa";

import { Item } from "../../entity/Item";


const TOOLTIP_STATS = "tooltip_stats";
const EQUIPMENT_SLOTS = "equipment_slots";

type Direction = "better"|"worse"|"equal";

export interface ItemTooltipDelta {
	text: string;
	direction: Direction;
}

export interface ItemTooltipLine {
	text: string;
	deltas?: ItemTooltipDelta[];
}

export interface StructuredItemTooltip {
	comparisonName?: string;
	lines: ItemTooltipLine[];
}

interface BonusDefinition {
	key: string;
	label: string;
	percentage?: boolean;
	fraction?: boolean;
}

const BONUS_DEFINITIONS: BonusDefinition[] = [
	{key: "health", label: "Zdrowie"},
	{key: "skill_atk", label: "Siła ataku"},
	{key: "affix_flat_attack_bonus", label: "Atak z affixu"},
	{key: "affix_flat_defense_bonus", label: "Pancerz z affixu"},
	{key: "atk_additional_bonus", label: "Bonus ataku", percentage: true},
	{key: "accuracy_bonus", label: "Precyzja", percentage: true},
	{key: "critical_chance", label: "Szansa krytyczna", percentage: true},
	{key: "critical_damage_bonus", label: "Obrażenia krytyczne", percentage: true, fraction: true},
	{key: "parry_chance", label: "Parowanie", percentage: true, fraction: true},
	{key: "armor_penetration", label: "Penetracja pancerza", percentage: true, fraction: true},
	{key: "lifesteal", label: "Kradzież życia", percentage: true, fraction: true},
	{key: "def_additional_bonus", label: "Bonus pancerza", percentage: true}
];

const RESISTANCES: Array<[string, string]> = [
	["light", "Odporność na światło"],
	["dark", "Odporność na mrok"],
	["fire", "Odporność na ogień"],
	["ice", "Odporność na lód"],
	["earth", "Odporność na naturę"],
	["water", "Odporność na wodę"],
	["cut", "Odporność fizyczna"]
];

export function hasStructuredItemTooltip(item: Item): boolean {
	return !!stats(item);
}

export function buildStructuredItemTooltip(item: Item): StructuredItemTooltip {
	const currentStats = stats(item);
	if (!currentStats) {
		return {lines: []};
	}
	const equipped = resolveEquippedItem(item);
	const equippedStats = stats(equipped);
	const lines: ItemTooltipLine[] = [];
	const category = currentStats["category"];

	const upgrade = numberValue(currentStats, "improve");
	const maxUpgrade = numberValue(currentStats, "max_improves");
	if (upgrade || maxUpgrade) {
		lines.push({text: "Ulepszenie: +" + upgrade
				+ (maxUpgrade ? "/" + maxUpgrade : "")});
	}

	if (category === "weapon") {
		appendWeaponLines(lines, currentStats, equippedStats);
	} else if (category === "armour") {
		appendNumberLine(lines, "Pancerz", currentStats, equippedStats, "def", 0);
	} else {
		const attack = Math.max(numberValue(currentStats, "atk"),
				numberValue(currentStats, "ratk"));
		const equippedAttack = Math.max(numberValue(equippedStats, "atk"),
				numberValue(equippedStats, "ratk"));
		appendValueLine(lines, "Atak", attack, equippedAttack, 0,
				equippedStats !== undefined);
		appendNumberLine(lines, "Pancerz", currentStats, equippedStats, "def", 0);
	}

	for (const bonus of BONUS_DEFINITIONS) {
		let current = numberValue(currentStats, bonus.key);
		let previous = numberValue(equippedStats, bonus.key);
		if (bonus.fraction) {
			current = toPercentage(current);
			previous = toPercentage(previous);
		}
		appendValueLine(lines, bonus.label, current, previous,
				bonus.percentage ? 1 : 0, equippedStats !== undefined,
				bonus.percentage ? "%" : "");
	}

	for (const [nature, label] of RESISTANCES) {
		const current = resistanceValue(currentStats, nature);
		const previous = resistanceValue(equippedStats, nature);
		appendValueLine(lines, label, current, previous, 1,
				equippedStats !== undefined, "%");
	}

	return {
		comparisonName: equipped?.getDisplayName(),
		lines
	};
}

function appendWeaponLines(lines: ItemTooltipLine[], current: Record<string, string>,
		equipped?: Record<string, string>) {
	const minimum = numberValue(current, "damage_min") || weaponAttack(current);
	const maximum = Math.max(minimum, numberValue(current, "damage_max") || minimum);
	const equippedMinimum = numberValue(equipped, "damage_min")
			|| weaponAttack(equipped);
	const equippedMaximum = Math.max(equippedMinimum,
			numberValue(equipped, "damage_max") || equippedMinimum);
	const attacksPerSecond = numberValue(current, "attacks_per_second");
	const equippedAttacksPerSecond = numberValue(equipped, "attacks_per_second");
	const dps = (minimum + maximum) / 2 * attacksPerSecond;
	const equippedDps = (equippedMinimum + equippedMaximum) / 2
			* equippedAttacksPerSecond;

	appendValueLine(lines, "Obrażenia na sekundę", dps, equippedDps, 1,
			equipped !== undefined);
	if (minimum || maximum || (equipped && (equippedMinimum || equippedMaximum))) {
		const line: ItemTooltipLine = {text: "Obrażenia: " + minimum + "–" + maximum};
		if (equipped && (minimum !== equippedMinimum || maximum !== equippedMaximum)) {
			line.deltas = [createDelta(minimum - equippedMinimum, 0),
					createDelta(maximum - equippedMaximum, 0)];
		}
		lines.push(line);
	}
	appendValueLine(lines, "Ataki na sekundę", attacksPerSecond,
			equippedAttacksPerSecond, 2, equipped !== undefined);
	appendNumberLine(lines, "Zasięg", current, equipped, "range", 0);
	appendNumberLine(lines, "Pancerz", current, equipped, "def", 0);
}

function appendNumberLine(lines: ItemTooltipLine[], label: string,
		current: Record<string, string>|undefined,
		equipped: Record<string, string>|undefined, key: string, precision: number) {
	appendValueLine(lines, label, numberValue(current, key),
			numberValue(equipped, key), precision, equipped !== undefined);
}

function appendValueLine(lines: ItemTooltipLine[], label: string, current: number,
		previous: number, precision: number, comparing: boolean, suffix = "") {
	if (!current && (!comparing || !previous)) {
		return;
	}
	const line: ItemTooltipLine = {
		text: label + ": " + formatNumber(current, precision) + suffix
	};
	if (comparing && !roundedEqual(current, previous, precision)) {
		line.deltas = [createDelta(current - previous, precision, suffix)];
	}
	lines.push(line);
}

function createDelta(value: number, precision: number, suffix = ""): ItemTooltipDelta {
	return {
		text: (value > 0 ? "+" : "") + formatNumber(value, precision) + suffix,
		direction: value > 0 ? "better" : value < 0 ? "worse" : "equal"
	};
}

function resolveEquippedItem(item: Item): Item|undefined {
	const current = stats(item);
	const player = marauroa.me as any;
	const publishedSlots = current?.[EQUIPMENT_SLOTS];
	if (!player || !publishedSlots) {
		return undefined;
	}
	const slots = publishedSlots.split(";").filter(Boolean);
	if (item._parent?._parent === player && slots.includes(item._parent._name)) {
		return undefined;
	}
	const category = current?.["category"];
	const ordered = orderSlots(slots, category, item["class"]);
	for (const slotName of ordered) {
		const slot = player[slotName];
		if (!slot || typeof slot.count !== "function"
				|| typeof slot.getByIndex !== "function") {
			continue;
		}
		for (let index = 0; index < slot.count(); index++) {
			const candidate = slot.getByIndex(index) as Item;
			if (stats(candidate)?.["category"] === category) {
				return candidate;
			}
		}
	}
	return undefined;
}

function orderSlots(published: string[], category?: string,
		itemClass?: string): string[] {
	const slots = [...new Set(published)];
	const preferred = category === "weapon" ? ["rhand", "lhand"]
			: itemClass === "shield" ? ["lhand", "rhand"] : [];
	return [...preferred.filter((slot) => slots.includes(slot)),
			...slots.filter((slot) => !preferred.includes(slot))];
}

function stats(item?: Item): Record<string, string>|undefined {
	const value = item?.[TOOLTIP_STATS];
	return value && typeof value === "object"
			? value as Record<string, string> : undefined;
}

function weaponAttack(value: Record<string, string>|undefined): number {
	return Math.max(numberValue(value, "atk"),
			numberValue(value, "ratk"));
}

function numberValue(value: Record<string, string>|undefined, key: string): number {
	const parsed = Number(value?.[key] || 0);
	return Number.isFinite(parsed) ? parsed : 0;
}

function resistanceValue(value: Record<string, string>|undefined,
		nature: string): number {
	const raw = value?.["resistance_" + nature];
	return raw === undefined ? 0 : numberValue(value, "resistance_" + nature) - 100;
}

function toPercentage(value: number): number {
	return Math.abs(value) <= 1 ? value * 100 : value;
}

function roundedEqual(first: number, second: number, precision: number): boolean {
	const scale = 10 ** precision;
	return Math.round(first * scale) === Math.round(second * scale);
}

function formatNumber(value: number, precision: number): string {
	return value.toLocaleString("pl-PL", {
		minimumFractionDigits: precision,
		maximumFractionDigits: precision
	});
}
