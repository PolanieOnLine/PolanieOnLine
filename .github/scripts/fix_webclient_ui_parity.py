from pathlib import Path


def replace_once(path: str, old: str, new: str) -> None:
    file = Path(path)
    text = file.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{path}: expected one match, found {count}")
    file.write_text(text.replace(old, new, 1))


# Match the desktop Java tooltip structure and content more closely.
tooltip = r'''/***************************************************************************
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
'''
Path('src/js/stendhal/ui/component/ItemTooltipPresentation.ts').write_text(tooltip)

# Clear slot decoration even when the last item disappears and no remaining item changed identity.
replace_once(
    'src/js/stendhal/ui/component/ItemContainerImplementation.ts',
    '''\t\t\te.textContent = "";\n\t\t\tif (this.dirty) {\n\t\t\t\tthis.updateCursor(e);\n\t\t\t\tthis.updateToolTip(e);\n\t\t\t}\n\t\t\t(e as any).dataItem = undefined;''',
    '''\t\t\te.textContent = "";\n\t\t\t// Empty slots must always drop cursor, tooltip and rarity decoration.\n\t\t\tthis.updateCursor(e);\n\t\t\tthis.updateToolTip(e);\n\t\t\t(e as any).dataItem = undefined;'''
)

container_path = Path('src/js/stendhal/ui/component/ItemContainerImplementation.ts')
container = container_path.read_text()
container = container.replace(
    '''\tbuildStructuredItemTooltip,\n\thasStructuredItemTooltip,\n\tItemTooltipDelta\n} from "./ItemTooltipPresentation";''',
    '''\tbuildStructuredItemTooltip,\n\thasStructuredItemTooltip,\n\tItemTooltipDelta,\n\tItemTooltipLine\n} from "./ItemTooltipPresentation";''', 1)
start = container.index('\tprivate static showRarityToolTip(')
end = container.index('\n\tprivate static hideRarityToolTip(', start)
replacement = r'''	private static showRarityToolTip(target: HTMLElement, item: Item, x: number, y: number) {
		const rarity = item.getRarity();
		const hasStructured = hasStructuredItemTooltip(item);
		const structured = buildStructuredItemTooltip(item,
				singletons.getConfigManager().getBoolean("item-tooltip.comparison"));
		if (!rarity && structured.lines.length === 0 && !structured.upgradeText) {
			return;
		}

		ItemContainerImplementation.hideRarityToolTip();
		target.removeAttribute("title");

		const toolTip = document.createElement("div");
		toolTip.id = "item-rarity-tooltip";
		toolTip.classList.add("item-rarity-tooltip");
		if (rarity) {
			toolTip.classList.add(rarity.cssClass);
		} else {
			toolTip.style.setProperty("--item-rarity-color", "#a37861");
		}
		toolTip.setAttribute("role", "tooltip");

		const name = document.createElement("div");
		name.className = "item-rarity-tooltip__name";
		name.textContent = (item.getDisplayName() + (structured.titleSuffix || "")).toUpperCase();
		toolTip.appendChild(name);

		if (rarity) {
			const rarityLine = document.createElement("div");
			rarityLine.className = "item-rarity-tooltip__rarity";
			rarityLine.textContent = rarity.polishDisplayName;
			toolTip.appendChild(rarityLine);
		}

		if (structured.upgradeText) {
			const upgrade = document.createElement("div");
			upgrade.className = "item-rarity-tooltip__upgrade";
			upgrade.textContent = "◆ " + structured.upgradeText;
			toolTip.appendChild(upgrade);
		}

		if (structured.comparisonName) {
			const comparison = document.createElement("div");
			comparison.className = "item-rarity-tooltip__comparison";
			comparison.textContent = "Porównanie z: " + structured.comparisonName;
			toolTip.appendChild(comparison);
		}

		for (const line of structured.lines) {
			ItemContainerImplementation.appendStructuredTooltipLine(toolTip, line);
		}

		// Legacy servers do not publish tooltip_stats, so preserve their plain text.
		if (!hasStructured) {
			const rarityText = rarity ? "Rzadkość: " + rarity.polishDisplayName : "";
			for (const line of item.getToolTip().split("\n")) {
				if (!line || line === item.getDisplayName() || line === rarityText) {
					continue;
				}
				const detail = document.createElement("div");
				detail.className = "item-rarity-tooltip__detail";
				detail.textContent = line;
				toolTip.appendChild(detail);
			}
		}

		document.body.appendChild(toolTip);
		const margin = 8;
		const left = Math.min(x + 12, window.innerWidth - toolTip.offsetWidth - margin);
		const top = Math.min(y + 12, window.innerHeight - toolTip.offsetHeight - margin);
		toolTip.style.left = Math.max(margin, left) + "px";
		toolTip.style.top = Math.max(margin, top) + "px";

		target.setAttribute("aria-describedby", toolTip.id);
		ItemContainerImplementation.rarityToolTip = toolTip;
		ItemContainerImplementation.rarityToolTipTarget = target;
	}

	private static appendStructuredTooltipLine(toolTip: HTMLElement, line: ItemTooltipLine) {
		if (line.kind === "divider") {
			const divider = document.createElement("div");
			divider.className = "item-rarity-tooltip__divider";
			divider.textContent = "────◇◇────";
			toolTip.appendChild(divider);
			return;
		}

		const detail = document.createElement("div");
		if (line.kind === "tree") {
			detail.className = "item-rarity-tooltip__tree";
			const prefix = document.createElement("span");
			prefix.className = "item-rarity-tooltip__tree-prefix";
			prefix.textContent = line.branchContinues ? "├─◆ " : "└─◆ ";
			detail.appendChild(prefix);
			const value = document.createElement("span");
			value.className = "item-rarity-tooltip__tree-value";
			value.appendChild(document.createTextNode(line.text));
			ItemContainerImplementation.appendTooltipDeltas(value, line.deltas);
			detail.appendChild(value);
			toolTip.appendChild(detail);
			return;
		}

		switch (line.kind) {
		case "primary":
			detail.className = "item-rarity-tooltip__primary";
			break;
		case "bonus":
			detail.className = "item-rarity-tooltip__bonus";
			detail.appendChild(document.createTextNode("◆ "));
			break;
		case "affix":
			detail.className = "item-rarity-tooltip__affix";
			break;
		case "footer":
			detail.className = "item-rarity-tooltip__footer";
			break;
		default:
			detail.className = "item-rarity-tooltip__detail";
			break;
		}
		detail.appendChild(document.createTextNode(line.text));
		ItemContainerImplementation.appendTooltipDeltas(detail, line.deltas);
		toolTip.appendChild(detail);
	}

	private static appendTooltipDeltas(target: HTMLElement, deltas?: ItemTooltipDelta[]) {
		if (!deltas?.length) {
			return;
		}
		target.appendChild(document.createTextNode(" ("));
		deltas.forEach((delta, index) => {
			if (index > 0) {
				target.appendChild(document.createTextNode("–"));
			}
			const value = document.createElement("span");
			value.className = "item-tooltip-delta item-tooltip-delta--" + delta.direction;
			value.textContent = delta.text;
			target.appendChild(value);
		});
		target.appendChild(document.createTextNode(")"));
	}
'''
container = container[:start] + replacement + container[end:]
container_path.write_text(container)

# Make the mobile panel buttons update the actual collapsed CSS classes, not only button glow state.
replace_once(
    'src/js/stendhal/ui/LeftPanelToggleController.ts',
    '''\t\tthis.unsubscribeState = store.subscribe(({ leftPanelExpanded }) => {\n\t\t\tthis.component?.setExpanded(leftPanelExpanded);\n\t\t\tthis.component?.update();\n\t\t});''',
    '''\t\tthis.unsubscribeState = store.subscribe(({ leftPanelExpanded }) => {\n\t\t\tdocument.getElementById("client")?.classList.toggle(\n\t\t\t\t\t"left-panel-collapsed", !leftPanelExpanded);\n\t\t\tthis.component?.setExpanded(leftPanelExpanded);\n\t\t\tthis.component?.update();\n\t\t});'''
)
replace_once(
    'src/js/stendhal/ui/RightPanelToggleController.ts',
    '''\t\tthis.unsubscribeState = store.subscribe(({ rightPanelExpanded }) => {\n\t\t\tthis.component?.setExpanded(rightPanelExpanded);\n\t\t\tthis.component?.update();\n\t\t});''',
    '''\t\tthis.unsubscribeState = store.subscribe(({ rightPanelExpanded }) => {\n\t\t\tdocument.getElementById("client")?.classList.toggle(\n\t\t\t\t\t"right-panel-collapsed", !rightPanelExpanded);\n\t\t\tthis.component?.setExpanded(rightPanelExpanded);\n\t\t\tthis.component?.update();\n\t\t});'''
)

# Replace the flat web card with the same compact, textured hierarchy used by the Java client.
css_path = Path('src/js/css/main.css')
css = css_path.read_text()
start = css.index('.item-rarity-tooltip {')
end_marker = '.item-tooltip-delta--equal { color: var(--text-color); }'
end = css.index(end_marker, start) + len(end_marker)
new_css = r'''.item-rarity-tooltip {
	position: fixed;
	z-index: 100000;
	pointer-events: none;
	box-sizing: border-box;
	width: 195px;
	max-width: calc(100vw - 16px);
	padding: 7px 8px;
	white-space: normal;
	color: #f3efe7;
	background: var(--background-url) repeat;
	border-top: 1px solid var(--border-top-color);
	border-left: 1px solid var(--border-top-color);
	border-right: 1px solid var(--border-bottom-color);
	border-bottom: 1px solid var(--border-bottom-color);
	border-radius: 1px;
	box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.78);
	font-size: 12px;
	line-height: 1.24;
	text-align: left;
}

.item-rarity-tooltip.item-rarity-rare {
	box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.78), 0 0 5px rgba(74, 144, 226, 0.09);
}
.item-rarity-tooltip.item-rarity-epic {
	box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.78), 0 0 5px rgba(155, 89, 182, 0.12);
}
.item-rarity-tooltip.item-rarity-legendary {
	box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.78), 0 0 6px rgba(255, 140, 0, 0.14);
}

.item-rarity-tooltip__name {
	color: var(--item-rarity-color);
	font-weight: bold;
	font-size: 12px;
	letter-spacing: 0.03em;
}

.item-rarity-tooltip__rarity {
	color: #f3efe7;
	font-size: 11px;
}

.item-rarity-tooltip__upgrade {
	color: var(--item-rarity-color);
	font-size: 11px;
	margin-top: 3px;
}

.item-rarity-tooltip__comparison {
	color: #c6a58f;
	font-size: 11px;
	margin-top: 3px;
}

.item-rarity-tooltip__divider {
	color: #a37861;
	font-size: 11px;
	line-height: 1;
	text-align: center;
	margin: 4px 0 3px;
}

.item-rarity-tooltip__primary {
	color: #f3efe7;
	font-weight: bold;
	margin: 1px 0;
}

.item-rarity-tooltip__tree {
	display: flex;
	align-items: flex-start;
	font-size: 11px;
}

.item-rarity-tooltip__tree-prefix {
	flex: 0 0 auto;
	color: #a37861;
	white-space: pre;
}

.item-rarity-tooltip__tree-value {
	min-width: 0;
	color: #f3efe7;
}

.item-rarity-tooltip__detail,
.item-rarity-tooltip__bonus {
	color: #f3efe7;
	font-size: 11px;
}

.item-rarity-tooltip__bonus::first-letter {
	color: #a37861;
}

.item-rarity-tooltip__affix {
	color: #f28c28;
	font-size: 11px;
	font-weight: bold;
	margin-top: 3px;
}

.item-rarity-tooltip__footer {
	color: #c6a58f;
	font-size: 11px;
	text-align: right;
}

.item-tooltip-delta {
	font-weight: bold;
}

.item-tooltip-delta--better { color: #62d26f; }
.item-tooltip-delta--worse { color: #ef6a62; }
.item-tooltip-delta--equal { color: #f3efe7; }'''
css_path.write_text(css[:start] + new_css + css[end:])

# Sanity checks for the three regressions addressed by this patch.
assert 'target.classList.remove(rarity.cssClass)' in Path('src/js/stendhal/ui/component/ItemContainerImplementation.ts').read_text()
assert 'left-panel-collapsed' in Path('src/js/stendhal/ui/LeftPanelToggleController.ts').read_text()
assert 'right-panel-collapsed' in Path('src/js/stendhal/ui/RightPanelToggleController.ts').read_text()
assert 'legendary_armor_breaker' in Path('src/js/stendhal/ui/component/ItemTooltipPresentation.ts').read_text()
