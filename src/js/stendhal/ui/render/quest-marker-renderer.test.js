/*
 * Copyright (C) 2026 - PolanieOnLine
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

const source = readFileSync(path.join(__dirname, "QuestMarkerRenderer.ts"), "utf8");
const compiled = ts.transpileModule(source, {
	compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;

function renderer() {
	const canvases = [];
	const exports = {};
	vm.runInNewContext(compiled, {
		exports,
		document: {
			createElement(tag) {
				assert.equal(tag, "canvas");
				const operations = [];
				const graphics = {
					fillStyle: "",
					fillRect(x, y, width, height) {
						operations.push({color: this.fillStyle, x, y, width, height});
					}
				};
				const canvas = {operations, getContext: () => graphics};
				canvases.push(canvas);
				return canvas;
			}
		}
	}, {filename: "QuestMarkerRenderer.js"});
	return {exports, canvases};
}

test("markers are private snapshots tied to the current zone and NPC", () => {
	const {exports: {readQuestMarker}} = renderer();
	const markers = {_zone: "zone", 7: "ready", 8: "unknown", 9: "__proto__"};
	assert.equal(readQuestMarker(markers, "zone", 7), "ready");
	assert.equal(readQuestMarker(markers, "other_zone", 7), undefined);
	assert.equal(readQuestMarker(markers, "zone", 8), undefined);
	assert.equal(readQuestMarker(markers, "zone", 9), undefined);
	assert.equal(readQuestMarker(markers, "zone", 10), undefined);
	assert.equal(readQuestMarker(undefined, "zone", 7), undefined);
	assert.equal(readQuestMarker(null, "zone", 7), undefined);
	assert.equal(readQuestMarker(markers, undefined, 7), undefined);
	assert.equal(readQuestMarker(markers, "zone", undefined), undefined);
});

test("four static 12x16 glyphs are cached rather than regenerated each frame", () => {
	const {exports: {drawQuestMarker}, canvases} = renderer();
	const draws = [];
	const context = {drawImage: (...args) => draws.push(args)};
	const colors = {
		available: "#f4d06f", repeatable: "#79b9eb", ready: "#82d878", progress: "#a4a49b"
	};
	for (const marker of Object.keys(colors)) {
		for (let frame = 0; frame < 60; frame++) {
			drawQuestMarker(context, marker, 10.4, 20.6);
		}
		const canvas = canvases.at(-1);
		assert.equal(canvas.width, 12);
		assert.equal(canvas.height, 16);
		assert.ok(canvas.operations.some(op => op.color === colors[marker]));
		assert.ok(canvas.operations.some(op => op.color === "#211b16"));
		assert.ok(canvas.operations.every(op => op.x >= 0 && op.y >= 0
			&& op.x + op.width <= 12 && op.y + op.height <= 16));
	}
	assert.equal(canvases.length, 4);
	assert.equal(draws.length, 240);
	assert.equal(draws[0][1], 10);
	assert.equal(draws[0][2], 21);
	assert.equal(draws[0][0], draws[59][0]);
});

test("the actual NPC view draws only our marker, above its name and within the viewport", () => {
	const {exports: markerRenderer} = renderer();
	const npcSource = readFileSync(path.resolve(__dirname, "../../entity/NPC.ts"), "utf8");
	const npcCompiled = ts.transpileModule(npcSource, {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	const marauroa = {me: {quest_markers: {_zone: "zone", 7: "ready"}}, currentZoneName: "zone"};
	const stendhal = {ui: {gamewindow: {offsetY: 0}}};
	class RPEntity {
		constructor() {
			this.statusBarYOffset = 0;
			this.titleDrawYOffset = 0;
		}
		drawTitle() {}
		drawHealthBar() {}
	}
	const dependencies = {
		"./RPEntity": {RPEntity},
		"../data/EntityOverlayRegistry": {EntityOverlayRegistry: {}},
		"../data/color/Color": {Color: {NPC: "blue"}},
		"../data/Paths": {Paths: {}},
		"marauroa": {marauroa},
		"../stendhal": {stendhal},
		"../ui/render/QuestMarkerRenderer": markerRenderer
	};
	const exports = {};
	vm.runInNewContext(npcCompiled, {
		exports,
		require(name) {
			assert.ok(Object.hasOwn(dependencies, name), "Unexpected dependency " + name);
			return dependencies[name];
		}
	}, {filename: "NPC.js"});
	const npc = new exports.NPC();
	Object.assign(npc, {id: 7, _x: 2, _y: 3, width: 1, height: 1, drawHeight: 48});
	const draws = [];
	const context = {drawImage: (...args) => draws.push(args)};
	npc.drawTop(context);
	assert.equal(draws.length, 1);
	assert.equal(draws[0][1], 74);
	assert.equal(draws[0][2], 42);
	stendhal.ui.gamewindow.offsetY = 50;
	npc.drawTop(context);
	assert.equal(draws.at(-1)[2], 50);
	// A public attribute on this NPC is not authoritative for the local player.
	marauroa.me = {};
	npc.quest_markers = {_zone: "zone", 7: "available"};
	npc.drawTop(context);
	assert.equal(draws.length, 2);
	marauroa.me = {quest_markers: {_zone: "other_zone", 7: "ready"}};
	npc.drawTop(context);
	assert.equal(draws.length, 2);
	marauroa.me = {quest_markers: {_zone: "zone", 7: "ready"}};
	npc.unnamed = true;
	npc.drawTop(context);
	assert.equal(draws.length, 2);
});
