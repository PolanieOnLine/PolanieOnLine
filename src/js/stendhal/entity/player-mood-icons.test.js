/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
"use strict";
const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const exports = {};
	const draws = [];
	const images = {
		"/sprites/ideas/away.png": {width: 128, height: 32},
		"/sprites/ideas/grumpy.png": {width: 100, height: 25}
	};
	const dependencies = {
		"./ActiveEntity": {ActiveEntity: class {}},
		"../data/Paths": {Paths: {sprites: "/sprites"}},
		"../SingletonRepo": {singletons: {getSpriteStore: () => ({get: name => images[name]})}}
	};
	const compiled = ts.transpileModule(readFileSync(path.join(__dirname, "RPEntity.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(compiled, {exports, require: name => dependencies[name] || {}});
	const entity = new exports.RPEntity();
	Object.assign(entity, {_x: 5, _y: 4, width: 1, height: 1, drawWidth: 48, drawHeight: 64,
		drawOffsetX: 152, drawOffsetY: 96});
	const context = {drawImage: (...args) => draws.push(args)};
	return {entity, draws, context, images};
}

test("grumpy is lowered six pixels and away uses the matching right-side anchor", () => {
	const {entity, draws, context, images} = harness();
	Object.assign(entity, {away: "afk", grumpy: "busy"});
	entity.drawStatusIcons(context);
	assert.equal(draws.length, 2);
	assert.equal(draws[0][0], images["/sprites/ideas/away.png"]);
	assert.deepEqual(draws[0].slice(5), [178, 102, 32, 32]);
	assert.equal(draws[1][0], images["/sprites/ideas/grumpy.png"]);
	assert.deepEqual(draws[1].slice(5), [152, 102, 25, 25]);
});

test("icons follow wide outfits and fractional movement without changing animation frames", () => {
	const {entity, draws, context} = harness();
	Object.assign(entity, {away: "afk", grumpy: "busy", drawWidth: 64,
		drawOffsetX: 144.25, drawOffsetY: 64.5});
	entity.drawStatusIcons(context);
	assert.deepEqual(draws[0].slice(5), [186.25, 70.5, 32, 32]);
	assert.deepEqual(draws[1].slice(5), [144.25, 70.5, 25, 25]);
	assert.equal(draws[0][3], 32);
	assert.equal(draws[1][3], 25);
});

test("fallback anchors match outfit bounds before cached offsets are available", () => {
	const {entity, draws, context} = harness();
	Object.assign(entity, {away: "afk", grumpy: "busy"});
	delete entity.drawOffsetX;
	delete entity.drawOffsetY;
	entity.drawStatusIcons(context);
	assert.deepEqual(draws[0].slice(5), [178, 102, 32, 32]);
	assert.deepEqual(draws[1].slice(5), [152, 102, 25, 25]);
});

test("hiding and showing mood icons does not shift them again or leave hidden sprites", () => {
	const {entity, draws, context} = harness();
	for (let repeat = 0; repeat < 3; repeat++) {
		entity.away = "afk";
		entity.grumpy = "busy";
		entity.drawStatusIcons(context);
		assert.deepEqual(draws.at(-2).slice(5), [178, 102, 32, 32]);
		assert.deepEqual(draws.at(-1).slice(5), [152, 102, 25, 25]);
		delete entity.away;
		delete entity.grumpy;
		const count = draws.length;
		entity.drawStatusIcons(context);
		assert.equal(draws.length, count);
	}
});
