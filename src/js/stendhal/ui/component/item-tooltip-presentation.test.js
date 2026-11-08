"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

class TooltipNode {
	constructor() {
		this.children = [];
		this.style = {};
		this.classList = {add: () => {}};
	}
	appendChild(child) {
		this.children.push(child);
	}
	set textContent(text) {
		this.children = [];
		this.text = text;
	}
	get textContent() {
		return (this.text || "") + this.children.map(child => child.textContent).join("");
	}
}
const document = {
	createElement: () => new TooltipNode(),
	createTextNode: text => Object.assign(new TooltipNode(), {text})
};
const exportsObject = {};
const source = ts.transpileModule(readFileSync(path.join(__dirname,
		"ItemTooltipPresentation.ts"), "utf8"), {
	compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;
vm.runInNewContext(source, {exports: exportsObject, document,
	require: name => {
		if (name === "marauroa") return {marauroa: {}};
		if (name === "../../util/GameLanguage") {
			return {GameLanguage: {getText: text => text, getLanguage: () => "pl"}};
		}
		return {};
	}});

test("new combat affixes explain armor damage, critical protection and legendary triggers", () => {
	const text = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
		category: "weapon", armor_crushing: "0.15", critical_hardening: "0.08",
		legendary_cleave: "1", legendary_tenacity: "1", legendary_breach: "1",
		legendary_quick_recovery: "1"
	}}, false).lines.map(line => line.text).join("\n");
	for (const expected of ["Kruszenie", "15%", "Hartowanie", "8%", "Rozmach",
		"Zaciętość", "Przełamanie", "Szybkie otrząśnięcie", "6 s", "5 s", "30%"]) {
		assert.ok(text.includes(expected), expected);
	}
});

test("glyph percentages retain units and explain thresholds and cooldowns", () => {
	const item = {tooltip_stats: {
		category: "other", critical_bonus_reduction: "25", out_of_combat_regen: "1",
		consumable_healing_bonus: "20", physical_dodge_chance: "5",
		low_health_damage_reduction: "10", nightmare_chance: "10", rate_increase: "1"
	}};
	const text = exportsObject.buildStructuredItemTooltip(item, false).lines
			.map(line => line.text).join("\n");
	for (const expected of ["25% redukcji dodatkowych obrażeń krytycznych",
		"1% maksymalnych PW regeneracji co 5 s", "limit 20 PW; po 10 s bez zadawania i otrzymywania obrażeń",
		"20% leczenia wyłącznie z przedmiotów zużywalnych",
		"5% szansy uniku bezpośredniego", "bez krytyków",
		"10% redukcji otrzymanych obrażeń poniżej 35% PW",
		"10% szansy po trafieniu", "o 10% przez 6 s",
		"odnowienie 12 s na źródło; bez kumulacji",
		"1 zmniejszenia wagi broni (minimum 2)"]) {
		assert.ok(text.includes(expected), expected);
	}
});

test("glyph level, fractional speed and recovery cap match the current level", () => {
    const tooltip = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
        category: "other", glyph_level: "2", rate_increase: "0.7",
        out_of_combat_regen: "0.75", out_of_combat_regen_cap: "14"
    }}, false);
    assert.equal(tooltip.titleSuffix, " II");
    const text = tooltip.lines.map(line => line.text).join("\n");
    assert.ok(text.includes("0,7 zmniejszenia wagi broni"));
    assert.ok(text.includes("0,75% maksymalnych PW"));
    assert.ok(text.includes("limit 14 PW"));
});

test("ordinary items do not acquire glyph effect lines", () => {
	const text = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
		category: "other", health: "5"
	}}, false).lines.map(line => line.text).join("\n");
	assert.ok(!text.includes("odnowienie"));
	assert.ok(!text.includes("poza walką"));
	assert.ok(!text.includes("minimum 2"));
});

test("knowledge shows creature experience separately from affixes", () => {
	const tooltip = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
		category: "other", combat_experience_bonus: "10"
	}}, false);
	assert.ok(tooltip.lines.some(line => line.kind === "bonus"
			&& line.text === "+10% doświadczenia z potworów"));
	assert.ok(!tooltip.lines.some(line => line.kind === "affix"));
});


test("ordinary descriptive affixes use the standard diamond bonus rows", () => {
	const lines = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
		category: "accessory", def: "10", steady_hand: "0.1", poison_potency: "0.25",
		armor_crushing: "0.15", critical_hardening: "0.08"
	}}, false).lines;
	for (const title of ["Pewna ręka", "Zjadliwość", "Kruszenie", "Hartowanie"]) {
		const line = lines.find(line => line.text.startsWith(title + ":"));
		assert.equal(line.kind, "bonus", title);
		const target = new TooltipNode();
		exportsObject.appendStructuredTooltipLine(target, line);
		assert.equal(target.children[0].className, "item-rarity-tooltip__bonus");
		assert.ok(target.textContent.startsWith("◆ " + title + ":"));
	}
});

test("every new legendary uses orange words and cream numeric fragments without diamonds", () => {
	const values = {
		legendary_defense_circle: ["3%", "12%"], legendary_hunters_pace: ["10%", "4 s"],
		legendary_last_stand: ["30%", "20%"], legendary_life_tide: ["20%", "10 s", "20 PZ", "2 s"],
		legendary_cleave: ["4", "30%", "2"], legendary_tenacity: ["3%", "15%", "5 s"],
		legendary_breach: ["15%", "6 s"], legendary_quick_recovery: ["30%"],
		legendary_iron_will: ["+20 pkt proc."]
	};
	for (const [key, expected] of Object.entries(values)) {
		const line = exportsObject.buildStructuredItemTooltip({tooltip_stats: {
			category: "accessory", [key]: "1"
		}}, false).lines.find(line => line.kind === "affix");
		const target = new TooltipNode();
		exportsObject.appendStructuredTooltipLine(target, line);
		const row = target.children[0];
		assert.equal(row.className, "item-rarity-tooltip__affix");
		assert.equal(row.textContent, line.text);
		assert.ok(!row.textContent.includes("◆"));
		const numbers = row.children[0].children.filter(node => node.style.color === "#f3e2b8")
			.map(node => node.textContent);
		assert.deepEqual(numbers, expected, key);
	}
});
