"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require("typescript");

function load(file, imports, globals = {}) {
	const exports = {};
	const source = readFileSync(path.join(__dirname, file), "utf8");
	vm.runInNewContext(ts.transpileModule(source, {compilerOptions: {
		module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022
	}}).outputText, {exports, require: name => {
		if (!(name in imports)) throw new Error(name);
		return imports[name];
	}, console, ...globals});
	return exports;
}

function storeHarness() {
	let loader;
	class JSONLoader {
		constructor() { loader = this; }
		load() {}
	}
	const {TileStore} = load("../data/TileStore.ts", {
		"./Paths": {Paths: {tileset: "/tiles", weather: "/weather"}},
		"../util/JSONLoader": {JSONLoader}
	});
	const store = TileStore.get();
	store.init();
	return {store, ready(data) {
		loader.data = {landscape: data, weather: {}};
		loader.onDataReady();
	}};
}

test("every flame tile advances, including tiles initially placed on later frames", () => {
	const h = storeHarness();
	h.ready({fire: ["*@300 0:1:2:3:4:5"]});
	for (let start = 0; start < 6; start++) {
		assert.equal(h.store.getLandscapeFrame("/tiles/fire.png", start, 0), start);
		assert.equal(h.store.getLandscapeFrame("/tiles/fire.png", start, 300), (start + 1) % 6);
		assert.equal(h.store.getLandscapeFrame("/tiles/fire.png", start, 1800), start);
	}
});

test("individual delays do not leak into following frames, and static tiles stay unchanged", () => {
	const h = storeHarness();
	h.ready({clock: ["7@500   7@100:8:9@200"]});
	assert.deepEqual(Array.from(h.store.getLandscapeMap()["/tiles/clock.png"][7].delays), [100, 500, 200]);
	assert.equal(h.store.getLandscapeFrame("/tiles/clock.png", 7, 100), 8);
	assert.equal(h.store.getLandscapeFrame("/tiles/clock.png", 7, 599), 8);
	assert.equal(h.store.getLandscapeFrame("/tiles/clock.png", 7, 600), 9);
	assert.equal(h.store.getLandscapeFrame("/tiles/clock.png", 7, 800), 7);
	assert.equal(h.store.getLandscapeFrame("/tiles/clock.png", 6, 700), 6);
});

test("the actual bank flame definition animates all six placed frames", () => {
	const h = storeHarness();
	const definition = JSON.parse(readFileSync(path.join(__dirname, "../../../../data/maps/tileset/animation.json"), "utf8"));
	h.ready(definition.landscape);
	for (let frame = 0; frame < 6; frame++) {
		assert.notEqual(h.store.getLandscapeFrame("/tiles/furniture/light/flames.png", frame, 300), frame);
	}
});

test("combined atlas redraws only changed animated cells, preserving other layers and flips", async () => {
	const h = storeHarness();
	let clock = 0;
	const draws = [];
	let closed = 0;
	class Canvas {
		constructor(width, height) {
			this.width = width; this.height = height;
			this.ctx = {drawImage: (...args) => draws.push(args), clearRect() {},
				translate() {}, transform() {}, fillRect() {}};
		}
		getContext() { return this.ctx; }
	}
	class MapOfSets extends Map {
		add(key, value) {
			if (!this.has(key)) this.set(key, new Set());
			this.get(key).add(value);
		}
	}
	const globals = {OffscreenCanvas: Canvas, Date: {now: () => clock},
		fetch: async () => ({ok: true, blob: async () => ({})}),
		createImageBitmap: async () => ({width: 96, height: 32, close() { closed++; }})};
	const {CombinedTilesetImageLoader} = load("CombinedTilesetImageLoader.ts", {
		"../util/MapOfSets": {MapOfSets}, "../util/Debug": {Debug: {isActive: () => false}},
		"../stendhal": {stendhal: {data: {build: {version: "test"}}}},
		"../data/TileStore": {TileStore: {get: () => h.store}}
	}, globals);
	const map = {tileWidth: 32, tileHeight: 32, firstgids: [1], tilesetFilenames: ["/tiles/fire.png"],
		getTilesetForGid: () => 0};
	const atlas = {tilesPerRow: 2, ctx: new Canvas(64, 64).ctx};
	const loader = new CombinedTilesetImageLoader(map, new Map([
		[0, [0, 3, 0x80000001]], [1, [0, 3]]
	]), atlas);
	loader.load();
	await new Promise(resolve => setImmediate(resolve));
	assert.equal(closed, 0, "source images must remain usable for animated cells");
	draws.length = 0;
	// Definitions can arrive after map images: animation must start without re-entering the map.
	h.ready({fire: ["*@300 0:1"]});
	loader.updateAnimations(0);
	draws.length = 0;
	clock = 300;
	loader.updateAnimations(300);
	assert.equal(draws.length, 2, "redraw the floor and flipped flame, but not the static atlas cell");
	assert.equal(draws[0][1], 64, "preserve the static lower layer");
	assert.equal(draws[1][1], 32, "use the next flame frame");
	draws.length = 0;
	loader.updateAnimations(450);
	assert.equal(draws.length, 0, "do not rebuild cells between frame changes");
	loader.close();
	assert.equal(closed, 1);
});
