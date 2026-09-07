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
export type ItemTooltipLineKind = "divider"|"primary"|"tree"|"detail"|"bonus"|"affix"|"footer";

export interface ItemTooltipDelta {
	text: string;
	direction: Direction;
}

export interface ItemTooltipLine {
	text: string;
	kind?: ItemTooltipLineKind;
	branchContinues?: boolean;
	deltas?: ItemTooltipDelta[];
}

export interface StructuredItemTooltip {
	comparisonName?: string;
	titleSuffix?: string;
	upgradeText?: string;
	lines: ItemTooltipLine[];
}

interface PercentageBonusDefinition {
	key: string;
	label: string;
	fraction: boolean;
}

const SPECIAL_BONUSES: PercentageBonusDefinition[] = [
	{key: "atk_additional_bonus", label: "bonusu ataku", fraction: false},
	{key: "accuracy_bonus", label: "bonusu precyzji", fraction: false},
	{key: "critical_chance", label: "szansy na trafienie krytyczne", fraction: false},
	{key: "critical_damage_bonus", label: "obrażeń trafienia krytycznego", fraction: true},
	{key: "parry_chance", label: "szansy na parowanie", fraction: true},
	{key: "armor_penetration", label: "penetracji pancerza", fraction: true},
	{key: "bleed_on_hit", label: "szansy na krwawienie", fraction: true},
	{key: "execute_damage", label: "obrażeń poniżej 25% PW celu", fraction: true},
	{key: "poison_on_hit", label: "szansy na zatrucie", fraction: true},
	{key: "distance_damage", label: "obrażeń z dystansu", fraction: true},
	{key: "resist_poisoned", label: "odporności na zatrucie", fraction: true},
	{key: "resist_bleeding", label: "odporności na krwawienie", fraction: true},
	{key: "resist_shocked", label: "odporności na szok", fraction: true},
	{key: "resist_confused", label: "odporności na dezorientację", fraction: true},
	{key: "resist_heavy", label: "odporności na spowolnienie", fraction: true},
	{key: "critical_additional_bonus", label: "obrażeń krytycznych", fraction: false},
	{key: "lifesteal", label: "kradzieży życia", fraction: true},
	{key: "lifesteal_increase", label: "zwiększonej kradzieży życia", fraction: false},
	{key: "def_additional_bonus", label: "bonusu pancerza", fraction: false}
];

const RESISTANCES: Array<[string, string]> = [
	["light", "światło"],
	["dark", "mrok"],
	["fire", "ogień"],
	["ice", "lód"],
	["earth", "naturę"],
	["water", "wodę"],
	["cut", "obrażenia fizyczne"]
];

const LEGENDARY_AFFIXES: Array<[string, string, string]> = [
	["legendary_deep_wounds", "Głębokie Rany", "15% szansy na krwawienie; rana zadaje 35% obrażeń trafienia."],
	["legendary_armor_breaker", "Łamacz Pancerzy", "Redukuje 40% niekorzystnej kary wynikającej z pancerza celu."],
	["legendary_longshot", "Dalekosiężność", "Ataki wykonane z dystansu zadają +25% obrażeń."],
	["legendary_executioner", "Egzekutor", "Przeciw celom poniżej 20% PW zadajesz +35% obrażeń."],
	["legendary_duel_master", "Mistrz Pojedynku", "Zyskujesz +5 pkt proc. szansy na parowanie; po udanym parowaniu następne trafienie zadaje +30% obrażeń."],
	["legendary_crushing_blow", "Miażdżący Cios", "Przeciw celom w średnim lub ciężkim pancerzu zadajesz +25% obrażeń."],
	["legendary_stunning_force", "Ogłuszająca Siła", "15% szansy na ogłuszenie celu przez 3 s (4 s przeciw graczom) po trafieniu zadającym obrażenia."],
	["legendary_binding_strike", "Pętający Cios", "15% szansy na nałożenie ociężałości przez 10 s po trafieniu."],
	["legendary_merciless_reach", "Bezlitosny Zasięg", "Zyskujesz +1 pole dodatkowego zasięgu ataku."],
	["legendary_falcon_eye", "Sokole Oko", "Przy ataku z co najmniej 4 pól zyskujesz +10 pkt proc. szansy na trafienie krytyczne."],
	["legendary_first_salvo", "Pierwsza Salwa", "Przeciw celom mającym co najmniej 80% PW ataki dystansowe zadają +30% obrażeń."],
	["legendary_power_overload", "Przeciążenie Mocy", "Atak dystansowy ma 15% szansy zadać +50% obrażeń."],
	["legendary_arcane_focus", "Skupienie Arkanów", "Ataki dystansowe przeciw celom z aktywnym negatywnym statusem zadają +25% obrażeń."],
	["legendary_wall_of_gord", "Wał grodu", "Co 8 s, gdy pojedyncze bezpośrednie trafienie stworzenia zadałoby co najmniej 10% maksymalnych PW, zmniejsza obrażenia tego trafienia o 35%."],
	["legendary_iron_will", "Żelazna Wola", "Zyskujesz +20 pkt proc. odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie."],
	["legendary_unyielding_protection", "Nieugięta Ochrona", "Poniżej 30% PW zyskujesz +10 pkt proc. szansy na pełne sparowanie ataku wręcz; końcowa szansa nie przekracza 15%."],
	["legendary_hero_eye", "Oko Bohatera", "Zyskujesz +8 pkt proc. szansy na trafienie krytyczne."],
	["legendary_guardian_seal", "Pieczęć Strażnika", "Zyskujesz +20 pkt proc. odporności na zatrucie, krwawienie, porażenie, dezorientację, ociężałość i ogłuszenie."]
];

export function hasStructuredItemTooltip(item: Item): boolean {
	return !!stats(item);
}

export function buildStructuredItemTooltip(item: Item,
		compareWithEquipment=true): StructuredItemTooltip {
	const current = stats(item);
	if (!current) {
		return {lines: []};
	}
	const equippedItem = compareWithEquipment ? resolveEquippedItem(item) : undefined;
	const equipped = stats(equippedItem);
	const lines: ItemTooltipLine[] = [];
	const upgrade = intValue(current, "improve");
	const maxUpgrade = intValue(current, "max_improves");
	const category = current["category"];
	const weapon = category === "weapon";
	const armour = category === "armour" && numberValue(current, "def") > 0;

	if (weapon) {
		appendWeaponPerformance(lines, current, equipped);
	} else if (armour) {
		appendArmourPerformance(lines, current, equipped);
	}
	appendCoreStats(lines, current, equipped, weapon);
	appendBonuses(lines, current, equipped, weapon, armour);
	appendAffixes(lines, current);
	appendFooter(lines, current);

	return {
		comparisonName: equippedItem?.getDisplayName(),
		titleSuffix: upgrade > 0 ? " +" + upgrade : undefined,
		upgradeText: maxUpgrade > 0 || upgrade > 0
				? "Ulepszenie: +" + upgrade + (maxUpgrade > 0 ? " / +" + maxUpgrade : "")
				: undefined,
		lines
	};
}

function appendWeaponPerformance(lines: ItemTooltipLine[], current: Record<string, string>,
		equipped?: Record<string, string>) {
	const weaponLines: ItemTooltipLine[] = [];
	const minimum = numberValue(current, "damage_min") || weaponAttack(current);
	const maximum = Math.max(minimum, numberValue(current, "damage_max") || minimum);
	const equippedMinimum = numberValue(equipped, "damage_min") || weaponAttack(equipped);
	const equippedMaximum = Math.max(equippedMinimum,
			numberValue(equipped, "damage_max") || equippedMinimum);
	const attacksPerSecond = attacksPerSecondValue(current);
	const equippedAttacksPerSecond = attacksPerSecondValue(equipped);
	const dps = (minimum + maximum) / 2 * attacksPerSecond;
	const equippedDps = (equippedMinimum + equippedMaximum) / 2 * equippedAttacksPerSecond;

	if (dps > 0 || (equipped && equippedDps > 0)) {
		weaponLines.push({
			kind: "primary",
			text: formatNumber(dps, 1) + " pkt. obrażeń na sekundę",
			deltas: equipped ? [createDelta(dps - equippedDps, 1)] : undefined
		});
	}
	if (minimum > 0 || maximum > 0 || (equipped && (equippedMinimum > 0 || equippedMaximum > 0))) {
		const rangeLine: ItemTooltipLine = {
			kind: "tree",
			branchContinues: true,
			text: "[" + formatNumber(minimum, 0) + "\u2013" + formatNumber(maximum, 0)
					+ "] pkt. obrażeń za trafienie"
		};
		if (equipped && (minimum !== equippedMinimum || maximum !== equippedMaximum)) {
			rangeLine.deltas = [
				createDelta(minimum - equippedMinimum, 0),
				createDelta(maximum - equippedMaximum, 0)
			];
		}
		weaponLines.push(rangeLine);
	}
	if (attacksPerSecond > 0 || (equipped && equippedAttacksPerSecond > 0)) {
		weaponLines.push({
			kind: "tree",
			branchContinues: false,
			text: formatNumber(attacksPerSecond, 2) + " ataku na sekundę ("
					+ getWeaponSpeedLabel(attacksPerSecond) + ")",
			deltas: equipped
					? [createDelta(attacksPerSecond - equippedAttacksPerSecond, 2)]
					: undefined
		});
	}

	const range = intValue(current, "range");
	const equippedRange = intValue(equipped, "range");
	if (range > 0 || (equipped && equippedRange > 0)) {
		weaponLines.push({
			kind: "detail",
			text: "Zasięg: " + range,
			deltas: equipped ? [createDelta(range - equippedRange, 0)] : undefined
		});
	}
	const damageType = current["damage_type"];
	if (damageType) {
		weaponLines.push({kind: "detail", text: "Typ obrażeń: " + localizeDamageType(damageType)});
	}
	const statuses = current["statusattack"];
	if (statuses) {
		weaponLines.push({kind: "detail", text: "Efekty trafienia: " + statuses.replace(/;/g, ",")});
	}

	if (weaponLines.length > 0) {
		lines.push({kind: "divider", text: ""}, ...weaponLines);
	}
}

function appendArmourPerformance(lines: ItemTooltipLine[], current: Record<string, string>,
		equipped?: Record<string, string>) {
	const value = intValue(current, "def");
	const previous = intValue(equipped, "def");
	if (value <= 0 && (!equipped || previous <= 0)) {
		return;
	}
	lines.push({kind: "divider", text: ""});
	lines.push({
		kind: "primary",
		text: value + " pkt. pancerza",
		deltas: equipped ? [createDelta(value - previous, 0)] : undefined
	});
}

function appendCoreStats(lines: ItemTooltipLine[], current: Record<string, string>,
		equipped: Record<string, string>|undefined, weapon: boolean) {
	const section: ItemTooltipLine[] = [];
	if (weapon) {
		appendPlainStat(section, "Pancerz", intValue(current, "def"), intValue(equipped, "def"), !!equipped);
	}
	appendPlainStat(section, "Siła ataku", intValue(current, "skill_atk"),
			intValue(equipped, "skill_atk"), !!equipped);
	appendSection(lines, section);
}

function appendBonuses(lines: ItemTooltipLine[], current: Record<string, string>,
		equipped: Record<string, string>|undefined, weapon: boolean, armour: boolean) {
	const core: ItemTooltipLine[] = [];
	const resistances: ItemTooltipLine[] = [];
	const special: ItemTooltipLine[] = [];

	if (!weapon) {
		appendIntegerBonus(core, Math.max(intValue(current, "atk"), intValue(current, "ratk")),
				Math.max(intValue(equipped, "atk"), intValue(equipped, "ratk")), "ataku", !!equipped);
	}
	if (!weapon && !armour) {
		appendIntegerBonus(core, intValue(current, "def"), intValue(equipped, "def"),
				"pancerza", !!equipped);
	}
	appendIntegerBonus(core, intValue(current, "health"), intValue(equipped, "health"),
			"zdrowia", !!equipped);
	appendIntegerBonus(core,
			intValueWithFallback(current, "affix_flat_attack_bonus", "flat_attack_bonus"),
			intValueWithFallback(equipped, "affix_flat_attack_bonus", "flat_attack_bonus"),
			"dodatkowego ataku", !!equipped);
	appendIntegerBonus(core,
			intValueWithFallback(current, "affix_flat_defense_bonus", "flat_defense_bonus"),
			intValueWithFallback(equipped, "affix_flat_defense_bonus", "flat_defense_bonus"),
			"dodatkowego pancerza", !!equipped);

	for (const [nature, label] of RESISTANCES) {
		const value = resistanceValue(current, nature);
		const previous = resistanceValue(equipped, nature);
		if (value === 0 && (!equipped || previous === 0)) {
			continue;
		}
		resistances.push({
			kind: "bonus",
			text: signed(formatCompact(value)) + "% odporności na " + label,
			deltas: equipped ? [createDelta(value - previous, 1, "%")] : undefined
		});
	}

	for (const bonus of SPECIAL_BONUSES) {
		const value = percentageValue(current, bonus.key, bonus.fraction);
		const previous = percentageValue(equipped, bonus.key, bonus.fraction);
		if (value === 0 && (!equipped || previous === 0)) {
			continue;
		}
		special.push({
			kind: "bonus",
			text: signed(formatCompact(value)) + "% " + bonus.label,
			deltas: equipped ? [createDelta(value - previous, 1, "%")] : undefined
		});
	}

	appendSection(lines, core);
	appendSection(lines, resistances);
	appendSection(lines, special);
}

function appendAffixes(lines: ItemTooltipLine[], current: Record<string, string>) {
	const affixes: ItemTooltipLine[] = [];
	const spiked = numberValue(current, "spiked_plating");
	if (spiked > 0) {
		affixes.push({kind: "affix", text: "Kolczaste okucie: odbija "
				+ formatNumber(spiked * 100, 1) + "% obrażeń z otrzymanych ciosów wręcz; łącznie maksymalnie 10%."});
	}
	if (hasValue(current, "hunter_mark")) {
		affixes.push({kind: "affix", text: "Znak łowcy: trafienie przez przeciwnika oznacza go na 6 s; przeciw oznaczonemu celowi redukujesz dodatkowe 5% niekorzystnej kary pancerza."});
	}
	if (hasValue(current, "giant_slayer")) {
		affixes.push({kind: "affix", text: "Łowca olbrzymów: za każde pełne 50 poziomów przewagi celu redukujesz 1% niekorzystnej kary pancerza, maksymalnie 10%."});
	}
	for (const [key, title, description] of LEGENDARY_AFFIXES) {
		if (hasValue(current, key)) {
			affixes.push({kind: "affix", text: title + ": " + description});
		}
	}
	appendRolledAffix(affixes, current, "legendary_bastion_bonus", "Niezłomny Bastion",
			"+", " pkt. dodatkowego pancerza.");
	appendRolledAffix(affixes, current, "legendary_relic_power", "Relikt Mocy",
			"+", " pkt. dodatkowego ataku.");
	appendSection(lines, affixes);
}

function appendRolledAffix(lines: ItemTooltipLine[], current: Record<string, string>,
		key: string, title: string, prefix: string, suffix: string) {
	if (!hasValue(current, key)) {
		return;
	}
	const value = intValue(current, key);
	lines.push({kind: "affix", text: title + ": " + prefix + value + suffix});
}

function appendFooter(lines: ItemTooltipLine[], current: Record<string, string>) {
	const footer: ItemTooltipLine[] = [];
	const minLevel = intValue(current, "min_level");
	if (minLevel > 0) {
		footer.push({kind: "footer", text: "Wymagany poziom: " + minLevel});
	}
	const durability = intValue(current, "durability");
	if (durability > 0) {
		const uses = intValue(current, "uses");
		footer.push({kind: "footer", text: "Wytrzymałość: "
				+ Math.max(0, durability - uses) + "/" + durability});
	}
	appendSection(lines, footer);
}

function appendSection(lines: ItemTooltipLine[], section: ItemTooltipLine[]) {
	if (section.length === 0) {
		return;
	}
	lines.push({kind: "divider", text: ""}, ...section);
}

function appendPlainStat(lines: ItemTooltipLine[], label: string, current: number,
		previous: number, comparing: boolean) {
	if (current === 0 && (!comparing || previous === 0)) {
		return;
	}
	lines.push({
		kind: "detail",
		text: label + ": " + current,
		deltas: comparing ? [createDelta(current - previous, 0)] : undefined
	});
}

function appendIntegerBonus(lines: ItemTooltipLine[], current: number,
		previous: number, label: string, comparing: boolean) {
	if (current === 0 && (!comparing || previous === 0)) {
		return;
	}
	lines.push({
		kind: "bonus",
		text: signed(String(current)) + " " + label,
		deltas: comparing ? [createDelta(current - previous, 0)] : undefined
	});
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
		if (!slot || typeof slot.count !== "function" || typeof slot.getByIndex !== "function") {
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

function orderSlots(published: string[], category?: string, itemClass?: string): string[] {
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

function hasValue(value: Record<string, string>|undefined, key: string): boolean {
	return !!value && Object.prototype.hasOwnProperty.call(value, key);
}

function weaponAttack(value: Record<string, string>|undefined): number {
	return Math.max(numberValue(value, "atk"), numberValue(value, "ratk"));
}

function attacksPerSecondValue(value: Record<string, string>|undefined): number {
	const published = numberValue(value, "attacks_per_second");
	if (published > 0) {
		return published;
	}
	const interval = numberValue(value, "attack_interval_seconds");
	return interval > 0 ? 1 / interval : 0;
}

function intValue(value: Record<string, string>|undefined, key: string): number {
	return Math.trunc(numberValue(value, key));
}

function intValueWithFallback(value: Record<string, string>|undefined,
		key: string, fallback: string): number {
	return hasValue(value, key) ? intValue(value, key) : intValue(value, fallback);
}

function numberValue(value: Record<string, string>|undefined, key: string): number {
	const parsed = Number(value?.[key] || 0);
	return Number.isFinite(parsed) ? parsed : 0;
}

function resistanceValue(value: Record<string, string>|undefined, nature: string): number {
	if (!hasValue(value, "resistance_" + nature)) {
		return 0;
	}
	return numberValue(value, "resistance_" + nature) - 100;
}

function percentageValue(value: Record<string, string>|undefined,
		key: string, fraction: boolean): number {
	let parsed = numberValue(value, key);
	if (fraction && Math.abs(parsed) <= 1) {
		parsed *= 100;
	}
	return parsed;
}

function localizeDamageType(value: string): string {
	switch (value.toLowerCase()) {
	case "light": return "Światło";
	case "dark": return "Mrok";
	case "fire": return "Ogień";
	case "ice": return "Lód";
	case "water": return "Woda";
	case "earth": return "Natura";
	case "cut": return "Fizyczne";
	default: return value;
	}
}

function getWeaponSpeedLabel(attacksPerSecond: number): string {
	if (attacksPerSecond >= 2) {
		return "Bardzo szybka broń";
	}
	if (attacksPerSecond >= 1.25) {
		return "Szybka broń";
	}
	if (attacksPerSecond >= 1) {
		return "Umiarkowana broń";
	}
	if (attacksPerSecond >= 0.6) {
		return "Powolna broń";
	}
	return "Bardzo powolna broń";
}

function createDelta(value: number, precision: number, suffix = ""): ItemTooltipDelta {
	return {
		text: signed(formatNumber(value, precision)) + suffix,
		direction: value > 0 ? "better" : value < 0 ? "worse" : "equal"
	};
}

function signed(value: string): string {
	return value.startsWith("-") || value === "0" || value.startsWith("0,")
			? value : "+" + value;
}

function formatCompact(value: number): string {
	return value.toLocaleString("pl-PL", {
		minimumFractionDigits: 0,
		maximumFractionDigits: 1
	});
}

function formatNumber(value: number, precision: number): string {
	return value.toLocaleString("pl-PL", {
		minimumFractionDigits: precision,
		maximumFractionDigits: precision
	});
}
