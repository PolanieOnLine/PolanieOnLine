"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function load(file, dependencies = {}, globals = {}) {
	const exports = {};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(source, {exports, require: name => dependencies[name] || {}, ...globals});
	return exports;
}

const visuals = load("LifeTideVisuals.ts");

function context() {
	const calls = [];
	const stack = [];
	return {calls, globalAlpha: 0.5, fillStyle: "green", beginPath() {},
		save() {stack.push([this.globalAlpha, this.fillStyle]);},
		restore() {[this.globalAlpha, this.fillStyle] = stack.pop();},
		fillRect(x, y, width, height) {calls.push({x, y, width, height, color: this.fillStyle, alpha: this.globalAlpha});}};
}

test("2000 surplus over 10000 HP covers twenty percent and preserves the canvas state", () => {
	const ctx = context();
	visuals.drawLifeTideOverlay(ctx, 4, 3, 100, 4, 2000 / 10000, 0);
	assert.equal(Math.max(...ctx.calls.map(call => call.x + call.width)), 24);
	assert.equal(ctx.calls[0].color, "#b67cf0");
	assert.ok(ctx.calls[0].alpha > 0 && ctx.calls[0].alpha < 0.5);
	assert.equal(ctx.globalAlpha, 0.5);
	assert.equal(ctx.fillStyle, "green");
});

test("breathing changes only opacity and no surplus leaves the bar untouched", () => {
	const ctx = context();
	const phases = [0, 1200, 2400].map(time => {
		const phase = context();
		visuals.drawLifeTideOverlay(phase, 0, 0, 100, 4, 0.2, time);
		return phase.calls;
	});
	assert.equal(phases[0][0].width, phases[1][0].width);
	assert.ok(phases[0][0].alpha < phases[1][0].alpha);
	assert.ok(phases[1][0].alpha - phases[0][0].alpha >= 0.15,
		"breathing must remain visible even when the parent canvas is half transparent");
	assert.equal(phases[0][0].alpha, phases[2][0].alpha);
	for (const ratio of [0, -1, NaN, Infinity]) visuals.drawLifeTideOverlay(ctx, 0, 0, 100, 4, ratio, 0);
	assert.equal(ctx.calls.length, 0);
});

test("normal HP does not read the animation clock", () => {
	let clockReads = 0;
	const {drawLifeTideOverlay} = load("LifeTideVisuals.ts", {}, {
		Date: {now() {clockReads++; return 0;}}
	});
	drawLifeTideOverlay(context(), 0, 0, 100, 4, 0);
	assert.equal(clockReads, 0);
});

test("diagonal gaps reveal the underlying HP bar", () => {
	const ctx = context();
	visuals.drawLifeTideOverlay(ctx, 0, 0, 100, 4, 0.2, 0);
	const covered = (x, y) => ctx.calls.some(call => call.x <= x && x < call.x + call.width
		&& call.y <= y && y < call.y + call.height);
	assert.equal(covered(2, 1), true);
	assert.equal(covered(6, 1), false);
	assert.equal(covered(4, 0), true);
	assert.equal(covered(4, 1), false);
	assert.equal(covered(20, 1), false);
});

test("the HUD animates only surplus and cancels its pending frame at normal HP", () => {
	const ctx = context();
	const pending = new Map();
	const canvas = {width: 100, height: 4, isConnected: true,
		getContext: () => ctx, getClientRects: () => [1]};
	let next = 0;
	const {StatBarComponent} = load("../ui/component/StatBarComponent.ts", {
		"../toolkit/Component": {Component: class {constructor() {this.componentElement = canvas;}}},
		"../../data/color/Color": {Color: {BLACK: "black", getStatBarColor: () => "green"}},
		"../../util/LifeTideVisuals": visuals
	}, {document: {hidden: false}, requestAnimationFrame: callback => {pending.set(++next, callback); return next;},
		cancelAnimationFrame: id => pending.delete(id)});
	const bar = new StatBarComponent();
	bar.draw(1.2, 0.2);
	assert.equal(Math.max(...ctx.calls.filter(call => call.color === "#b67cf0")
		.map(call => call.x + call.width)), 20);
	assert.equal(pending.size, 1);
	bar.draw(1);
	assert.equal(pending.size, 0);
	assert.equal(ctx.calls.at(-1).color, "green");
	canvas.isConnected = false;
	bar.draw(1.2, 0.2);
	assert.equal(pending.size, 0);
});

test("a surplus HUD drawn in a background tab retains its next animation frame", () => {
	const ctx = context();
	const pending = [];
	const canvas = {width: 100, height: 4, isConnected: true,
		getContext: () => ctx, getClientRects: () => [1]};
	const document = {hidden: true};
	const {StatBarComponent} = load("../ui/component/StatBarComponent.ts", {
		"../toolkit/Component": {Component: class {constructor() {this.componentElement = canvas;}}},
		"../../data/color/Color": {Color: {BLACK: "black", getStatBarColor: () => "green"}},
		"../../util/LifeTideVisuals": visuals
	}, {document, requestAnimationFrame: callback => {pending.push(callback); return pending.length;},
		cancelAnimationFrame() {}});
	const bar = new StatBarComponent();
	bar.draw(1.2, 0.2);
	assert.equal(pending.length, 1, "the browser must have a frame to resume when the tab becomes visible");
	document.hidden = false;
	ctx.calls.length = 0;
	pending.shift()();
	assert.ok(ctx.calls.some(call => call.color === "#b67cf0"));
	assert.equal(pending.length, 1);
});

test("hiding and enabling the HP bar resumes surplus breathing without an HP change", () => {
	const ctx = context();
	const pending = new Map();
	let visible = true;
	let next = 0;
	const canvas = {width: 100, height: 4, isConnected: true,
		getContext: () => ctx, getClientRects: () => visible ? [1] : []};
	const {StatBarComponent} = load("../ui/component/StatBarComponent.ts", {
		"../toolkit/Component": {Component: class {
			constructor() {this.componentElement = canvas;}
			setVisible(value = true) {visible = value;}
		}},
		"../../data/color/Color": {Color: {BLACK: "black", getStatBarColor: () => "green"}},
		"../../util/LifeTideVisuals": visuals
	}, {document: {hidden: false}, requestAnimationFrame: callback => {pending.set(++next, callback); return next;},
		cancelAnimationFrame: id => pending.delete(id)});
	const bar = new StatBarComponent();
	bar.draw(1.2, 0.2);
	bar.setVisible(false);
	assert.equal(pending.size, 0, "hidden HP bars must stop their pulse");
	bar.setVisible(true);
	assert.equal(pending.size, 1, "showing unchanged HP must restart breathing");
});
