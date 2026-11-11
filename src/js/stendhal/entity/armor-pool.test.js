"use strict";
const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	class ActiveEntity { set(key, value) { this[key] = value; } }
	const sounds = [], floaters = [];
	const dependencies = {
		"./ActiveEntity": { ActiveEntity },
		"../data/color/Color": { Color: { getStatBarColor: () => "#00ff00" } },
		"../util/GameLanguage": { GameLanguage: { getText: text => text } },
		"../stendhal": { stendhal: { sound: { playLocalizedEffect: (...args) => sounds.push(args) } } }
	};
	const visuals = {exports: {}};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname,
		"../util/LifeTideVisuals.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText, visuals);
	dependencies["../util/LifeTideVisuals"] = visuals.exports;
	const exports = {};
	const code = ts.transpileModule(readFileSync(path.join(__dirname, "RPEntity.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText;
	vm.runInNewContext(code, { exports, require: name => dependencies[name] || {}, Date });
	const entity = new exports.RPEntity();
	Object.assign(entity, { width: 1, height: 1, drawWidth: 32, drawHeight: 32, hp: 100, base_hp: 100 });
	entity.addFloater = (...args) => floaters.push(args);
	return { entity, floaters, sounds };
}

test("armor and health have separate three-pixel bands without changing ordinary bars", () => {
	const { entity } = harness();
	const fills = [];
	const ctx = { beginPath() {}, rect() {}, stroke() {}, fillRect(...rect) { fills.push([this.fillStyle, ...rect]); } };
	entity.set("base_armor_hp", "100");
	entity.set("armor_hp", "50");
	entity.drawHealthBar(ctx, 100, 200);
	assert.equal(entity.getHealthBarHeight(), 9);
	assert.deepEqual(fills.at(-1), ["#b9c4cf", 100, 191, 16, 3]);
	assert.deepEqual(fills.find(fill => fill[0] === "#00ff00"), ["#00ff00", 100, 195, 32, 3]);
	fills.length = 0;
	entity.set("armor_hp", "0");
	entity.drawHealthBar(ctx, 100, 200);
	assert.equal(entity.getHealthBarHeight(), 9);
	assert.deepEqual(fills.find(fill => fill[0] === "#00ff00"), ["#00ff00", 100, 195, 32, 3]);
	assert.deepEqual(fills.find(fill => fill[0] === "#373f48"), ["#373f48", 100, 191, 32, 3]);
	entity.set("base_armor_hp", "0");
	fills.length = 0;
	entity.drawHealthBar(ctx, 100, 200);
	assert.equal(entity.getHealthBarHeight(), 6);
	assert.deepEqual(fills.find(fill => fill[0] === "#00ff00"), ["#00ff00", 100, 194, 32, 4]);
});

test("armor loss has gray numbers and break feedback without changing health", () => {
	const { entity, floaters, sounds } = harness();
	entity.set("base_armor_hp", "100");
	entity.set("armor_hp", "100");
	assert.equal(floaters.length, 0);
	entity.set("armor_hp", "25");
	assert.deepEqual(floaters[0], ["-75", "#9aa4af"]);
	assert.equal(sounds.length, 0);
	entity.set("armor_hp", "0");
	assert.ok(floaters.some(([text]) => text === "Rozbite opancerzenie"));
	assert.equal(sounds.length, 1);
	assert.ok(sounds[0].includes("pol-armor-break"));
	entity.set("armor_hp", "0");
	assert.equal(sounds.length, 1);
	assert.equal(entity.hp, 100);
});
