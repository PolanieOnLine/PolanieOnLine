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
const translationKeys = [];
vm.runInNewContext(compiled, {exports: exportsObject, require: name => {
	if (name.endsWith("/Paths")) return {Paths: {gui: "/data/gui"}};
	if (name.endsWith("/GameLanguage")) return {GameLanguage: {getText(key) {translationKeys.push(key); return key;}}};
	throw new Error("Unexpected import: " + name);
}, Image: class {complete = false; set src(value) {this.url = value;}}});
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
test("private target map supports legacy targets and optional total route steps", () => {
	const parsed = parseNavigationTargets({_zone: "zone", goal: "3\t4\tmain\tCel", route: "3\t4\tside\tDroga\t123", arrived: "0\t0\tmain\tKoniec\t0"}, "zone");
	assert.equal(parsed.length, 3);
	assert.equal(parsed[0].remainingSteps, undefined);
	assert.equal(parsed[1].remainingSteps, 123);
	assert.equal(parsed[2].remainingSteps, 0);
	assert.equal(parseNavigationTargets({_zone: "zone", max: "1\t2\tmain\tCel\t2147483647"}, "zone")[0].remainingSteps, 2147483647);
});
test("private target map rejects stale zone and malformed targets or steps", () => {
	const snapshot = {_zone: "zone", goal: "3\t4\tmain\tCel", side: "0\t0\tside\tŚlady", a: "NaN\t2\tmain\tx",
		b: "1\tInfinity\tside\tx", c: "1\t2\tother\tx", d: "1\t2\tmain\t", e: "1\t2\tmain\tx\ty", f: "\t2\tmain\tx"};
	for (const [index, invalid] of ["", "-1", "1.5", "NaN", "Infinity", "1e3", " 2 ", "2147483648", "9007199254740992", "2\textra"].entries()) snapshot["bad" + index] = "1\t2\tmain\tx\t" + invalid;
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
	const texts = [], operations = [], markers = [];
	return {texts, operations, markers, globalAlpha: 1,
		save() {operations.push("save");}, restore() {operations.push("restore");},
		setTransform() {}, beginPath() {}, rect() {}, clip() {}, fillRect() {}, strokeRect() {},
		moveTo(x, y) {markers.push({x, y});}, lineTo() {}, closePath() {}, fill() {},
		stroke() {operations.push("stroke:" + this.strokeStyle);}, arc(x, y, radius) {markers.push({x, y, radius});},
		fillText(text) {texts.push(text);}, measureText(text) {return {width: text.length * 6};}};
}
function player(targets) {return {compass_unlocked: 1, _x: 0, _y: 0, compass_targets: {_zone: "zone", ...targets}};}
function draw(p, viewport = 560) {const ctx = context();drawNavigationCompass(ctx, p, "zone", viewport);return ctx;}
function footer(ctx) {return ctx.texts.at(-1);}

test("using the compass hides and restores the unlocked bar", () => {
	const p = player({goal: "5\t0\tmain\tCel"});
	assert.ok(draw(p).operations.length > 0);
	p.compass_hidden = 1;
	assert.deepEqual(draw(p).operations, []);
	p.compass_hidden = 0;
	assert.ok(draw(p).texts.includes("5 kr."));
});
test("locked and logout hide overlay, empty compass retains only original cardinals", () => {
	const ctx = context();
	drawNavigationCompass(ctx, undefined, "zone", 320);
	drawNavigationCompass(ctx, {compass_unlocked: 0, _x: 0, _y: 0}, "zone", 320);
	assert.equal(ctx.operations.length, 0);
	drawNavigationCompass(ctx, player({}), "zone", 320);
	assert.deepEqual(ctx.texts, ["S", "W", "N", "E", "S"]);
	assert.deepEqual(ctx.operations, ["save", "restore"]);
});
test("distance prefers total walking steps and preserves hypot fallback", () => {
	const legacy = player({goal: "5\t0\tmain\tCel"});
	assert.ok(draw(legacy).texts.includes("5 kr."));
	legacy._x = .6; assert.ok(draw(legacy).texts.includes("4 kr."));
	const routed = player({goal: "3\t4\tmain\tCel\t123"});
	assert.ok(draw(routed).texts.includes("123 kr."));
	routed._x = .6; assert.ok(draw(routed).texts.includes("123 kr."));
	routed.compass_targets.goal = "3\t4\tmain\tCel\t0"; assert.ok(draw(routed).texts.includes("0 kr."));
	assert.ok(translationKeys.includes("kr."));
});
test("primary starts with deterministic main id, survives changing proximity and marker collisions", () => {
	const p = player({"side:a": "1\t0\tside\tPoboczny", "main:z": "5\t0\tmain\tDrugi", "main:a": "50\t0\tmain\tGłówny"});
	let ctx = draw(p);
	assert.equal(footer(ctx), "Idź na wschód · Główny");
	assert.ok(ctx.texts.includes("50 kr."));
	assert.equal(ctx.texts.filter(text => text.endsWith(" kr.")).length, 1);
	assert.ok(ctx.operations.includes("stroke:#ffffff"));
	p._x = 49;
	ctx = draw(p); assert.equal(footer(ctx), "Idź na wschód · Główny"); assert.ok(ctx.texts.includes("1 kr."));
	p.compass_targets["main:0"] = "49\t0\tmain\tNowy";
	assert.equal(footer(draw(p)), "Idź na wschód · Główny");
	delete p.compass_targets["main:a"];
	assert.equal(footer(draw(p)), "Jesteś na miejscu · Nowy");
});
test("all eight direction instructions and arrival are translated", () => {
	const cases = [[0,-10,"północ"],[10,-10,"północny wschód"],[10,0,"wschód"],[10,10,"południowy wschód"],
		[0,10,"południe"],[-10,10,"południowy zachód"],[-10,0,"zachód"],[-10,-10,"północny zachód"]];
	for (const [x,y,direction] of cases) {
		const key = "Idź na " + direction;
		assert.equal(footer(draw(player({goal: `${x}\t${y}\tmain\tCel`}))), key + " · Cel");
		assert.ok(translationKeys.includes(key));
	}
	assert.equal(footer(draw(player({goal: "0\t0\tmain\tCel"}))), "Jesteś na miejscu · Cel");
	assert.ok(translationKeys.includes("Jesteś na miejscu"));
});
test("footer is abbreviated with literal dots on narrow screens", () => {
	const ctx = draw(player({goal: "10\t0\tmain\tBardzo długa nazwa celu prowadzącego do kolejnej mapy"}), 160);
	assert.ok(footer(ctx).endsWith("..."));
	assert.ok(ctx.measureText(footer(ctx)).width <= compassBounds(160).width - 20);
});
test("reaching an intermediate waypoint with remaining route steps means continue walking", () => {
	const p = player({goal: "0\t0\tmain\tCel\t7"});
	assert.equal(footer(draw(p)), "Idź dalej · Cel");
	assert.ok(translationKeys.includes("Idź dalej"));
	p.compass_targets.goal = "0\t0\tmain\tCel\t0";
	assert.equal(footer(draw(p)), "Jesteś na miejscu · Cel");
	p.compass_targets.goal = "0\t0\tmain\tCel";
	assert.equal(footer(draw(p)), "Jesteś na miejscu · Cel");
});
test("selection is scoped to player, clears on logout and when targets disappear", () => {
	const first = player({"main:z": "5\t0\tmain\tPierwszy"});
	assert.equal(footer(draw(first)), "Idź na wschód · Pierwszy");
	first.compass_targets["main:a"] = "10\t0\tmain\tNowy";
	assert.equal(footer(draw(first)), "Idź na wschód · Pierwszy");
	const second = player({"main:z": "5\t0\tmain\tPierwszy", "main:a": "10\t0\tmain\tNowy"});
	assert.equal(footer(draw(second)), "Idź na wschód · Nowy");
	draw(undefined);
	assert.equal(footer(draw(first)), "Idź na wschód · Nowy");
	first.compass_targets = {_zone: "zone"}; assert.deepEqual(draw(first).texts, ["S", "W", "N", "E", "S"]);
	first.compass_targets = {_zone: "zone", "main:b": "10\t0\tmain\tPowrót"};
	assert.equal(footer(draw(first)), "Idź na wschód · Powrót");
});
