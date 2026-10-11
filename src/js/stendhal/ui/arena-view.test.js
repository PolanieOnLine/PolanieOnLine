"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness(dpr = 1, tileScale = 1) {
	const queued = [], draws = [], transforms = [];
	const canvas = { width: 800 * dpr, height: 600 * dpr };
	const marauroa = { me: { _x: 32, _y: 47 }, currentZoneName: "instance_A", clientFramework: {} };
	const stendhal = { ui: { equip: { animate() {} } }, zone: {}, data: {} };
	const document = { visibilityState: "visible", getElementById: id => id === "viewport" ? canvas : { style: {} } };
	function load(relative, dependencies = {}) {
		const exports = {};
		vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, relative), "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText, { exports, document, window: { devicePixelRatio: dpr }, performance: { now: () => 20 },
			queueMicrotask: run => queued.push(run), requestAnimationFrame() {}, console: { log() {} },
			require: name => dependencies[name] || {} });
		return exports;
	}
	const { TileMap } = load("../data/TileMap.ts");
	const map = Object.create(TileMap.prototype);
	Object.assign(map, { currentZoneName: "int_tarnow_challenge_arena", displayZoneName: "instance_A",
		zoneSizeX: 64, zoneSizeY: 64, parallax: { draw() {} }, strategy: { render: () => draws.push("map") } });
	const { ViewPort } = load("ViewPort.ts", { marauroa: { marauroa }, "../stendhal": { stendhal },
		"./UI": { ui: { get: () => ({ draw() {}, animate() {} }) } }, "./UIComponentEnum": { UIComponentEnum: {} },
		"../util/Debug": { Debug: { isActive: () => false } },
		"../util/AdminDiagnostics": { AdminDiagnostics: { get: () => false } } });
	const viewport = Object.create(ViewPort.prototype);
	Object.assign(viewport, { map, devicePixelRatio: dpr, tileScale, logicalWidth: 800, logicalHeight: 600,
		targetTileWidth: 32, targetTileHeight: 32, lastDeltaMs: 100, maxDeltaMs: 250,
		cameraX: 0, cameraY: 0, offsetX: 0, offsetY: 0,
		ctx: { canvas, setTransform: (...args) => transforms.push(args) }, weatherRenderer: { draw() {} },
		updateCanvasSize: () => false, clearViewportCanvas() {}, applyHSLFilter() {}, drawEntitiesTop() {},
		drawEmojiSprites() {}, drawTextSprites() {} });
	stendhal.ui.viewport = stendhal.ui.gamewindow = viewport;
	const { ViewChangeEvent } = load("../event/ViewChangeEvent.ts", {
		marauroa: { RPEvent: class {} }, "../stendhal": { stendhal }
	});
	const { MiniMapComponent } = load("component/MiniMapComponent.ts", {
		marauroa: { marauroa }, "../toolkit/Component": { Component: class {} },
		"../../data/color/Color": { Color: { parseRGB: () => ({}) } }
	});
	const minimap = Object.create(MiniMapComponent.prototype);
	Object.assign(minimap, { map, width: 128, height: 128, xOffset: 0, yOffset: 0,
		componentElement: { getContext: () => ({ setTransform() {}, fillRect() {}, translate() {} }) },
		zoneChange() {}, updateBasePosition() {}, drawBackground() {}, drawEntities: () => draws.push("minimap") });
	return { map, viewport, minimap, marauroa, canvas, draws, transforms, queued, load, stendhal,
		view(x = 32, y = 19) { Object.assign(new ViewChangeEvent(), { x, y }).execute(marauroa.me);
			while (queued.length) queued.shift()(); } };
}

test("a private arena renders both the live world and minimap despite template layer names", () => {
	const h = harness(); h.view(); h.viewport.draw(); h.minimap.draw();
	assert.deepEqual(h.draws, ["map", "minimap"]);
	assert.equal(h.viewport.offsetX, 624);
	assert.equal(h.viewport.offsetY, 308);
});

test("old map contents are not drawn while changing to another private arena or the lobby", () => {
	const h = harness();
	for (const zone of ["instance_B", "int_tarnow_challenge_arena", "0_tarnow_city"]) {
		h.marauroa.currentZoneName = zone;
		h.viewport.draw(); h.minimap.draw();
	}
	assert.deepEqual(h.draws, []);
	h.map.displayZoneName = "instance_B";
	h.marauroa.currentZoneName = "instance_B";
	h.viewport.draw(); assert.deepEqual(h.draws, ["map"]);
});

test("normal maps and existing legacy private maps remain supported", () => {
	const h = harness();
	h.map.displayZoneName = h.map.currentZoneName = "0_tarnow_city";
	assert.equal(h.map.isForZone("0_tarnow_city"), true);
	assert.equal(h.map.isForZone("instance_B"), false);
	h.map.displayZoneName = h.map.currentZoneName = "int_vault";
	assert.equal(h.map.isForZone("private_vault"), true);
});

test("camera center uses world pixels at high DPI and mobile tile scaling", () => {
	for (const [dpr, scale] of [[1, 1], [2, 1], [3, 1.5]]) {
		const h = harness(dpr, scale); h.view(); h.viewport.adjustView(h.canvas);
		assert.equal(h.viewport.offsetX, Math.round(1024 - 400 / scale));
		assert.equal(h.viewport.offsetY, Math.round(608 - 300 / scale));
		assert.equal(h.transforms[0][0], dpr * scale);
	}
});

test("frozen camera stays centered after resize and returns to following the player", () => {
	const h = harness(2); h.view(); h.viewport.adjustView(h.canvas);
	h.viewport.logicalWidth = 400; h.viewport.logicalHeight = 300;
	h.canvas.width = 800; h.canvas.height = 600;
	h.viewport.adjustView(h.canvas);
	assert.equal(h.viewport.offsetX, 824); assert.equal(h.viewport.offsetY, 458);
	h.viewport.freeze = false; h.viewport.adjustView(h.canvas);
	assert.equal(h.viewport.offsetX, 840); assert.equal(h.viewport.offsetY, 1370);
});

test("private zone metadata is kept regardless of map transfer entry order and reset in lobby", () => {
	const h = harness();
	const { Client } = h.load("../Client.ts", { marauroa: { marauroa: h.marauroa },
		"./PerceptionListener": { StendhalPerceptionListener: class {} },
		"data/TileMap": { TileMap: { get: () => h.map } } });
	const client = Client.get();
	client.onDataMap = data => data;
	client.registerMarauroaEventHandlers();
	h.map.decodeTileset = h.map.decodeMapLayer = () => {};
	h.map.mapLayerGroup = () => [];
	h.map.strategy.onMapLoaded = () => {};
	const entries = [
		{ name: "int_tarnow_challenge_arena.data_map", data: { runtime_zone: "instance_B" } },
		{ name: "int_tarnow_challenge_arena.tilesets", data: "tiles" }
	];
	for (const items of [entries, entries.slice().reverse()]) {
		h.marauroa.clientFramework.onTransfer(items);
		assert.equal(h.map.displayZoneName, "instance_B");
		assert.equal(h.map.currentZoneName, "int_tarnow_challenge_arena");
	}
	h.marauroa.clientFramework.onTransfer([{ name: "int_tarnow_challenge_arena.data_map", data: {} }, entries[1]]);
	assert.equal(h.map.displayZoneName, "int_tarnow_challenge_arena");
});

test("clicks and movement keys cannot release the observer camera until observation ends", () => {
	const h = harness(); const actions = [];
	let watching = true;
	h.marauroa.clientFramework.sendAction = action => actions.push(action);
	const direction = { STOP: {}, UP: { val: 1 } };
	const { User } = h.load("../entity/User.ts", { marauroa: { marauroa: h.marauroa },
		"../stendhal": { stendhal: h.stendhal }, "./Player": { Player: class {} },
		"../ui/dialog/ArenaObservationDialog": { ArenaObservationDialog: { isWatching: () => watching } },
		"../util/Direction": { Direction: direction } });
	const user = Object.create(User.prototype);
	h.view();
	user.setDirection(direction.UP); user.moveTo(32, 20); user.moveTo({ x: "32", y: "20" });
	assert.equal(h.viewport.freeze, true); assert.equal(actions.length, 0);
	watching = false;
	user.setDirection(direction.UP); user.moveTo(32, 20);
	assert.equal(h.viewport.freeze, false);
	assert.deepEqual(actions.map(action => action.type), ["move", "moveto"]);
});
