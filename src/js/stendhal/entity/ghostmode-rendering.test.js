"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function harness(self = false, administrator = true) {
	const exports = {};
	const marauroa = {me: {isAdmin: () => administrator}};
	const dependencies = {"./ActiveEntity": {ActiveEntity: class {}}, "marauroa": {marauroa}};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "RPEntity.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(source, {exports, require: name => dependencies[name] || {}});
	const entity = new exports.RPEntity();
	Object.assign(entity, {_x: 2, _y: 3, width: 1, height: 1, class: "player", ghostmode: "",
		getFaceDirection: () => ({val: 3}), isAdmin: () => administrator});
	if (self) marauroa.me = entity;
	const alphas = [];
	const ctx = {globalAlpha: 1, drawImage() {alphas.push(this.globalAlpha);}};
	return {entity, ctx, alphas, image: {width: 144, height: 256}};
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
