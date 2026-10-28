"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const elements = { "#hptext": {}, "#otherstats": {} };
	let text = "", writes = 0;
	Object.defineProperty(elements["#otherstats"], "innerText", {
		get: () => text, set: value => { text = value; writes++; }
	});
	class Component { child(selector) { return elements[selector]; } }
	class Bar { setVisible(value) { this.visible = value; } isVisible() { return this.visible; } draw() {} }
	class Item { constructor(name, quantity) { this.name = name; this.quantity = quantity; } }
	const slot = (...items) => ({ count: () => items.length, getByIndex: index => items[index] });
	const marauroa = { me: { hp: 100, base_hp: 100, atk: 20, atk_xp: 0, def: 20, def_xp: 0,
		atk_item: 1, def_item: 1, xp: 100, level: 10, pouch: slot(new Item("money", "92000000")),
		bag: slot(new Item("money", 123), new Item("polano", 50)),
		lhand: slot(new Item("money", 7)), rhand: slot(new Item("money", "8")),
		reserve: slot(new Item("money", 1000000)), bank: slot(new Item("money", 2000000)) } };
	const genericComponent = { update() {}, draw() {} };
	let stats;
	const ui = { registerComponent() {}, get: key => key === "stats" ? stats : genericComponent };
	const config = { getBoolean: () => false };
	const stendhal = { config, zone: { sortEntities() {} }, ui: { equip: { update() {} } } };
	const singletons = { getConfigManager: () => config, getQuickSlotsController: () => genericComponent };
	function load(filename, dependencies) {
		const exports = {};
		const source = ts.transpileModule(readFileSync(filename, "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText;
		vm.runInNewContext(source, { exports, document: { getElementById: () => ({ style: {} }), querySelectorAll: () => [] },
			require: name => dependencies[name] || {} });
		return exports;
	}
	const { PlayerStatsComponent } = load(path.join(__dirname, "PlayerStatsComponent.ts"), {
		"../toolkit/Component": { Component }, "./StatusesListComponent": { StatusesListComponent: class {} },
		"./StatBarComponent": { StatBarComponent: Bar }, "./KarmaBarComponent": { KarmaBarComponent: Bar },
		"../UI": { ui }, "../UIComponentEnum": { UIComponentEnum: {} },
		"../../SingletonRepo": { singletons }, "../../entity/Item": { Item },
		"marauroa": { marauroa }, "../../stendhal": { stendhal }
	});
	stats = new PlayerStatsComponent();
	const { GameLanguage } = load(path.join(__dirname, "../../util/GameLanguage.ts"), {});
	const { StendhalPerceptionListener } = load(path.join(__dirname, "../../PerceptionListener.ts"), {
		"marauroa": { marauroa, PerceptionListener: class {} }, "ui/UI": { ui },
		"ui/UIComponentEnum": { UIComponentEnum: { PlayerStats: "stats" } },
		"./stendhal": { stendhal }, "SingletonRepo": { singletons },
		"util/GameLanguage": { GameLanguage },
        "ui/dialog/ItemUpgradeDialog": {ItemUpgradeDialog: {onInventoryChanged() {}}},
		"Client": { Client: { get: () => ({ loaded: true }) } }
	});
	return { stats, marauroa, Item, slot, elements, GameLanguage, writes: () => writes,
		perception: () => new StendhalPerceptionListener().onPerceptionEnd(0, 0) };
}

test("character language is restored from the complete perception without changing money", () => {
	const h = harness();
	h.marauroa.me.language = "en";
	h.perception();
	assert.equal(h.GameLanguage.getLanguage(), "en");
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 92000138$/);
	h.marauroa.me.language = "pl";
	h.perception();
	assert.equal(h.GameLanguage.getLanguage(), "pl");
});

test("money total includes only carried money, not reserve, storage or other items", () => {
	const h = harness();
	h.stats.update("xp");
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 92000138$/);
});

test("slot-only quantity changes, removals and transfers refresh after a perception", () => {
	const h = harness();
	h.perception();
	h.marauroa.me.pouch.getByIndex(0).quantity = "92000010";
	h.perception();
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 92000148$/);
	h.marauroa.me.pouch = h.slot();
	h.marauroa.me.bag = h.slot(new h.Item("money", 92000133));
	h.perception();
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 92000148$/);
	h.marauroa.me.bag = h.slot();
	h.perception();
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 15$/);
});

test("missing or invalid quantities do not turn the money total into NaN", () => {
	const h = harness();
	h.marauroa.me.pouch = h.slot(new h.Item("money", undefined), new h.Item("money", "bad"),
		new h.Item("money", -10), new h.Item("money", 1.5));
	h.stats.update("money");
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 138$/);
});

test("large totals stay exact and unchanged perceptions do not rewrite the stats text", () => {
	const h = harness();
	h.marauroa.me.pouch = h.slot(new h.Item("money", "2147483647"));
	h.marauroa.me.bag = h.slot(new h.Item("money", 2147483647));
	h.perception();
	assert.match(h.elements["#otherstats"].innerText, /Pieniądze: 4294967309$/);
	const writes = h.writes();
	h.perception();
	assert.equal(h.writes(), writes);
	h.marauroa.me = null;
	assert.doesNotThrow(() => h.stats.update("money"));
	assert.doesNotThrow(() => h.perception());
});

test("the stats panel and bars fit the wider dock without removing panel collapsing", () => {
	const css = readFileSync(path.join(__dirname, "../../../css/main.css"), "utf8");
	assert.match(css, /--left-panel-width:\s*224px/);
	assert.match(css, /#leftColumn\s*\{[^}]*flex:\s*0 0 var\(--left-panel-width\)/);
	assert.match(css, /#stats\s*\{[^}]*box-sizing:\s*border-box[^}]*width:\s*100%[^}]*white-space:\s*pre-wrap[^}]*overflow-wrap:\s*anywhere/);
	assert.match(css, /#stats \.statbar\s*\{[^}]*width:\s*calc\(100% - 7px\)/);
	assert.match(css, /#client:not\(\.mobile-floating-ui\)\.left-panel-collapsed\s*\{[^}]*--left-panel-width:\s*0px/);
});
