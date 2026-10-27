"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

const exportsObject = {};
const source = ts.transpileModule(readFileSync(path.join(__dirname,
		"ItemTooltipPresentation.ts"), "utf8"), {
	compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;
vm.runInNewContext(source, {exports: exportsObject,
	require: name => {
		if (name === "marauroa") return {marauroa: {}};
		if (name === "../../util/GameLanguage") {
			return {GameLanguage: {getText: text => text, getLanguage: () => "pl"}};
		}
		return {};
	}});

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
