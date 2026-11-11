/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");
const compiled = ts.transpileModule(readFileSync(path.join(__dirname, "NavigationCompass.ts"), "utf8"), {
	compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;
const exportsObject = {};
vm.runInNewContext(compiled, {exports: exportsObject, require: () => ({Paths: {gui: "/data/gui"}}),
	Image: class {complete = false; set src(value) {this.url = value;}}});
const {parseNavigationTargets, compassPosition, compassBounds, drawNavigationCompass} = exportsObject;
const target = (x, y) => ({id: "goal", x, y, main: true, label: "Cel"});

test("cardinal mapping and movement use continuous coordinates", () => {
	assert.equal(compassPosition(target(0, -10), 0, 0), .5);
	assert.equal(compassPosition(target(-10, 0), 0, 0), .25);
	assert.equal(compassPosition(target(10, 0), 0, 0), .75);
	assert.equal(compassPosition(target(0, 10), 0, 0), 1);
	assert.equal(compassPosition(target(0, 0), 0, 0), .5);
	assert.notEqual(compassPosition(target(4, -5), 0, 0), compassPosition(target(4, -5), .5, 0));
});
test("private target map rejects stale zone and malformed targets", () => {
	const snapshot = {_zone: "zone", goal: "3\t4\tmain\tCel", side: "0\t0\tside\tŚlady", a: "NaN\t2\tmain\tx",
		b: "1\tInfinity\tside\tx", c: "1\t2\tother\tx", d: "1\t2\tmain\t", e: "1\t2\tmain\tx\ty", f: "\t2\tmain\tx"};
	const parsed = parseNavigationTargets(snapshot, "zone");
	assert.equal(parsed.length, 2); assert.equal(parsed[0].label, "Cel"); assert.equal(parsed[1].main, false);
	assert.equal(parseNavigationTargets(snapshot, "other").length, 0);
	assert.equal(parseNavigationTargets(null, "zone").length, 0);
	assert.equal(parseNavigationTargets({goal: "1\t2\tmain\tx"}, "zone").length, 0);
});
test("bar fits 320px viewport and caps width on desktop", () => {
	for (const viewport of [0, 20, 80, 320, 640, 1920]) {
		const bounds = compassBounds(viewport);
		assert.ok(bounds.width >= 0 && bounds.width <= 560);
		assert.ok(bounds.left >= 0 && bounds.left + bounds.width <= viewport);
	}
	assert.equal(compassBounds(320).width, 296); assert.equal(compassBounds(1920).width, 560);
});
function context() {
	const texts = [], operations = [];
	const ctx = {texts, operations, globalAlpha: 1,
		save() {operations.push("save");}, restore() {operations.push("restore");},
		setTransform() {}, beginPath() {}, rect() {}, clip() {}, fillRect() {}, strokeRect() {},
		moveTo() {}, lineTo() {}, closePath() {}, fill() {},
		fillText(text) {texts.push(text);}, measureText(text) {return {width: text.length * 6};}};
	return ctx;
}
test("locked and logout hide overlay, unlocked empty compass retains only cardinals", () => {
	const ctx = context();
	drawNavigationCompass(ctx, undefined, "zone", 320);
	drawNavigationCompass(ctx, {compass_unlocked: 0, _x: 0, _y: 0}, "zone", 320);
	assert.equal(ctx.operations.length, 0);
	drawNavigationCompass(ctx, {compass_unlocked: 1, _x: 0, _y: 0}, "zone", 320);
	assert.deepEqual(ctx.texts, ["S", "W", "N", "E", "S"]);
	assert.deepEqual(ctx.operations, ["save", "restore"]);
});
test("distance changes every frame, nearest caption survives collisions", () => {
	const ctx = context();
	const player = {compass_unlocked: 1, _x: 0, _y: 0, compass_targets: {_zone: "zone", goal: "5\t0\tmain\tCel", other: "10\t0\tside\tDalszy"}};
	drawNavigationCompass(ctx, player, "zone", 320);
	assert.ok(ctx.texts.includes("5")); assert.ok(ctx.texts.includes("Cel")); assert.ok(!ctx.texts.includes("10"));
	ctx.texts.length = 0; player._x = .6;
	drawNavigationCompass(ctx, player, "zone", 320);
	assert.ok(ctx.texts.includes("4"));
});
