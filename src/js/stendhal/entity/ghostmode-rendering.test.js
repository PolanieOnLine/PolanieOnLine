"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function harness(self = false, administrator = true) {
	const exports = {};
	const pending = [];
	const marauroa = {me: {isAdmin: () => administrator}};
	const dependencies = {"./ActiveEntity": {ActiveEntity: class {
		set(key, value) {this[key] = value;}
	}}, "marauroa": {marauroa}};
	dependencies["../data/color/Color"] = {Color: {getStatBarColor: () => "green"}};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "RPEntity.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(source, {exports, require: name => dependencies[name] || {},
		queueMicrotask: callback => pending.push(callback)});
	const entity = new exports.RPEntity();
	Object.assign(entity, {_x: 2, _y: 3, width: 1, height: 1, class: "player", ghostmode: "",
		getFaceDirection: () => ({val: 3}), isAdmin: () => administrator});
	if (self) marauroa.me = entity;
	const alphas = [];
	const ctx = {globalAlpha: 1, drawImage() {alphas.push(this.globalAlpha);}};
	return {entity, ctx, alphas, flush: () => pending.splice(0).forEach(callback => callback()),
		image: {width: 144, height: 256}};
}

test("administrators see their own and other ghosts at half normal visibility like Java", () => {
	for (const self of [true, false]) {
		const h = harness(self);
		h.entity.drawSpriteImage(h.ctx, h.image);
		assert.equal(h.alphas[0], 0.5);
		h.entity.visibility = "20";
		h.entity.drawSpriteImage(h.ctx, h.image);
		assert.equal(h.alphas[1], 0.1);
		assert.equal(h.ctx.globalAlpha, 1);
	}
});

test("visibility composes with canvas alpha and turning ghost mode off restores normal appearance", () => {
	const h = harness(true);
	h.ctx.globalAlpha = 0.4;
	h.entity.visibility = "50";
	h.entity.drawSpriteImage(h.ctx, h.image);
	assert.equal(h.alphas[0], 0.1);
	delete h.entity.ghostmode;
	h.entity.drawSpriteImage(h.ctx, h.image);
	assert.equal(h.alphas[1], 0.2);
	assert.equal(h.ctx.globalAlpha, 0.4);
});

test("regular players cannot render ghost mode administrators", () => {
	const h = harness(false, false);
	let draws = 0;
	for (const method of ["drawCombat", "drawMain", "drawAttack", "drawStatusIcons"]) {
		h.entity[method] = () => {draws++;};
	}
	h.entity.draw(h.ctx);
	assert.equal(draws, 0);
});

test("surplus health stays within the health bar while keeping the actual HP value", () => {
	const h = harness(true);
	Object.assign(h.entity, {hp: 12000, base_hp: 10000, drawWidth: 32, drawHeight: 48,
		titleDrawYOffset: 0});
	const widths = [];
	const ctx = {beginPath() {}, rect() {}, stroke() {}, fillRect(x, y, width) {widths.push(width);}};
	h.entity.drawHealthBar(ctx, 0, 0);
	assert.equal(h.entity.hp, 12000);
	assert.deepEqual(widths, [32, 32]);
});

test("temporary high HP overrides the legacy field without duplicate hit notifications", () => {
	const h = harness(true);
	h.entity.hp = 32767;
	const changes = [];
	h.entity.onHPChanged = value => changes.push(value);
	h.entity.set("modified_hp", "39320");
	h.flush();
	assert.equal(h.entity.getCurrentHP(), 39320);
	h.entity.set("hp", "32767");
	h.entity.set("modified_hp", "39300");
	h.flush();
	assert.equal(h.entity.getCurrentHP(), 39300);
	assert.deepEqual(changes, [6553, -20]);
	h.entity.set("hp", "32700");
	h.entity.set("modified_hp", "32700");
	h.flush();
	assert.equal(h.entity.getCurrentHP(), 32700);
	assert.deepEqual(changes, [6553, -20, -6600]);
});

test("perception applies a high-HP override once and initial high-HP objects stay silent", () => {
	for (const initial of [true, false]) {
		const h = harness(true);
		if (!initial) h.entity.hp = 30000;
		const changes = [];
		h.entity.onHPChanged = value => changes.push(value);
		// The legacy value can precede its override within the same network update.
		h.entity.set("hp", "32767");
		h.entity.set("modified_hp", "36000");
		h.flush();
		assert.equal(h.entity.getCurrentHP(), 36000);
		assert.deepEqual(changes, initial ? [] : [6000]);
	}
});
