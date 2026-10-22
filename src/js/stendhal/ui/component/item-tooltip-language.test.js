"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");
const root = path.resolve(__dirname, "../../../../..");
const sourceRoot = path.join(root, "src/js/stendhal");

async function harness() {
	const compile = file => ts.transpileModule(readFileSync(path.join(sourceRoot, file), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	const languageExports = {};
	vm.runInNewContext(compile("util/GameLanguage.ts"), {exports: languageExports, console,
		fetch: async url => ({ok: true, text: async () => readFileSync(path.join(root, "data/languages", path.basename(url)), "utf8")})});
	const language = languageExports.GameLanguage;
	await language.load("/data");
	class Item {
		constructor(stats) { this.tooltip_stats = stats; this.name = "canonical_sword"; }
		getDisplayName() { return "Sword"; }
	}
	const exports = {};
	const marauroa = {};
	vm.runInNewContext(compile("ui/component/ItemTooltipPresentation.ts"), {exports, require: name => {
		if (name === "marauroa") return {marauroa};
		if (name === "../../entity/Item") return {Item};
		if (name === "../../util/GameLanguage") return {GameLanguage: language};
		throw new Error(name);
	}});
	return {language, Item, marauroa,
		build: (item, compare=false) => exports.buildStructuredItemTooltip(item, compare)};
}

test("equipment attack penalties survive missing or zero other attack values", async () => {
	const h = await harness();
	for (const [attack, expected] of [
		[{atk: "-5"}, "-5 ataku"],
		[{atk: "-5", ratk: "0"}, "-5 ataku"],
		[{ratk: "-4"}, "-4 ataku"],
		[{atk: "0", ratk: "-4"}, "-4 ataku"],
		[{atk: "-5", ratk: "-3"}, "-3 ataku"],
		[{atk: "4", ratk: "7"}, "+7 ataku"]
	]) {
		const item = new h.Item({category: "armour", def: "40", ...attack});
		const text = h.build(item).lines.map(line => line.text).join("\n");
		assert.ok(text.includes(expected), text);
		assert.match(text, /40 pkt\. pancerza/);
	}
});

test("attack comparison retains equipped penalties and marks a smaller penalty as better", async () => {
	const h = await harness();
	const equipped = new h.Item({category: "accessory", atk: "-5"});
	h.marauroa.me = {finger: {count: () => 1, getByIndex: () => equipped}};
	const item = new h.Item({category: "accessory", equipment_slots: "finger", atk: "-2"});
	const line = h.build(item, true).lines.find(line => line.text === "-2 ataku");
	assert.ok(line, "The tooltip must show the current attack penalty");
	assert.equal(line.deltas[0].text, "+3");
	assert.equal(line.deltas[0].direction, "better");
});

test("zero attack without comparison remains hidden", async () => {
	const h = await harness();
	const item = new h.Item({category: "accessory", atk: "0"});
	assert.ok(h.build(item).lines.every(line => !line.text.includes(" ataku")));
});

test("life steal is separate from its effectiveness affix and total in Polish and English", async () => {
	const h = await harness();
	const item = new h.Item({category: "weapon", lifesteal: "0.075", base_lifesteal: "0.075",
		affix_lifesteal: "0", effective_lifesteal: "0.09", lifesteal_effectiveness: "20"});
	let text = h.build(item).lines.map(line => line.text).join("\n");
	assert.match(text, /Kradzież życia: 9% \(\+1.5%\)/);
	assert.doesNotMatch(text, /Łączna kradzież życia/);
	assert.match(text, /\+20% skuteczności kradzieży życia/);
	assert.doesNotMatch(text, /\+7.5% kradzieży życia|bazow/i);
	h.language.synchronize("en");
	text = h.build(item).lines.map(line => line.text).join("\n");
	assert.match(text, /Life steal: 9% \(\+1.5%\)/);
	assert.doesNotMatch(text, /Total life steal/);
	assert.match(text, /\+20% life steal effectiveness/);
	assert.doesNotMatch(text, /kradzie|skutecz|Łączna/);
});

test("a life steal grant is an affix, not a core stat", async () => {
	const h = await harness();
	const item = new h.Item({category: "weapon", lifesteal: "0.07", base_lifesteal: "0",
		affix_lifesteal: "0.07", effective_lifesteal: "0.07"});
	const text = h.build(item).lines.map(line => line.text).join("\n");
	assert.match(text, /\+7% kradzieży życia/);
	assert.doesNotMatch(text, /Kradzież życia:/);
});

test("a plain item compares missing life steal against the equipped core stat", async () => {
	const h = await harness();
	const equipped = new h.Item({category: "accessory", lifesteal: "0.075", base_lifesteal: "0.075",
		affix_lifesteal: "0", effective_lifesteal: "0.09", lifesteal_effectiveness: "20"});
	h.marauroa.me = {finger: {count: () => 1, getByIndex: () => equipped}};
	const item = new h.Item({category: "accessory", equipment_slots: "finger"});
	const lines = h.build(item, true).lines;
	assert.ok(lines.some(line => line.text === "Kradzież życia: 0%"));
	const total = lines.find(line => line.text === "Kradzież życia: 0%");
	assert.ok(total);
	assert.equal(total.deltas[0].text, "-9,0%");
	assert.equal(total.deltas[0].direction, "worse");
	assert.ok(lines.every(line => !line.text.includes("+0% kradzieży życia")));
});

test("weapon and healing tooltips translate labels while retaining exact stats and canonical names", async () => {
	const h = await harness();
	const stats = {category: "weapon", damage_min: "10", damage_max: "20", attacks_per_second: "2",
		damage_type: "fire", range: "4", improve: "4", max_improves: "5", lifesteal: "0.05",
		critical_chance: "7", health: "50", min_level: "10", durability: "100", uses: "3",
		consumable_healing_regen: "3", consumable_healing_frequency_seconds: "2", consumable_healing_duration_seconds: "10"};
	const item = new h.Item({...stats});
	assert.match(h.build(item).lines.map(line => line.text).join("\n"), /obrażeń/);
	h.language.synchronize("en");
	const result = h.build(item);
	const text = result.lines.map(line => line.text).join("\n");
	assert.match(text, /30\.0 damage per second/);
	assert.match(text, /Damage type: Fire/);
	assert.match(text, /life steal/);
	assert.match(text, /Healing: \+3 HP every 2 s/);
	assert.match(text, /Durability: 97\/100/);
	assert.equal(result.upgradeText, "Upgrade: +4 / +5");
	assert.doesNotMatch(text, /[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/);
	assert.deepEqual(item.tooltip_stats, stats);
	assert.equal(item.name, "canonical_sword");
	h.language.synchronize("pl");
	assert.equal(h.build(item).upgradeText, "Ulepszenie: +4 / +5");
});

test("all legendary affix descriptions and regular item bonuses have readable English display text", async () => {
	const h = await harness(); h.language.synchronize("en");
	const stats = {category: "armour", def: "85", resistance_fire: "107", spiked_plating: "0.04",
		hunter_mark: "1", giant_slayer: "1", affix_flat_attack_bonus: "3", accuracy_bonus: "7"};
	for (const key of ["deep_wounds", "armor_breaker", "longshot", "executioner", "duel_master", "crushing_blow",
		"stunning_force", "binding_strike", "merciless_reach", "falcon_eye", "first_salvo", "power_overload",
		"arcane_focus", "wall_of_gord", "iron_will", "unyielding_protection", "hero_eye", "guardian_seal"])
		stats["legendary_" + key] = "1";
	const text = h.build(new h.Item(stats)).lines.map(line => line.text).join("\n");
	assert.match(text, /85 armour points/);
	assert.match(text, /Spiked Plating: reflect 4\.0%/);
	assert.match(text, /Stunning Force: 15% chance to stun/);
	assert.match(text, /Falcon Eye:/);
	assert.match(text, /critical hit chance/);
	assert.doesNotMatch(text, /[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/);
	assert.doesNotMatch(text, /bonusu|pancerza|zdrowia|obrażeń|szansy|Zyskujesz/);
});
