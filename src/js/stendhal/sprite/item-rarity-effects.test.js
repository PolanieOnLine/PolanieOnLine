"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const metrics = { reads: 0, draws: 0, clears: 0, created: 0, appends: 0 };
	const images = new Map();
	class Canvas {
		constructor() { this.width = 0; this.height = 0; metrics.created++; }
		setAttribute() {}
		remove() {
			if (this.parentElement) {
				this.parentElement.children = this.parentElement.children.filter(child => child !== this);
				this.parentElement = undefined;
			}
		}
		getContext() {
			return {
				drawImage: (image, ...coordinates) => { metrics.draws++; this.drawn = { image, coordinates }; },
				clearRect: () => { metrics.clears++; },
				createImageData: (width, height) => ({ width, height, data: new Uint8ClampedArray(width * height * 4) }),
				putImageData: pixels => { this.pixels = pixels; },
				getImageData: () => {
					metrics.reads++;
					if (this.drawn.image.unreadable) { throw new Error("Unreadable image"); }
					const data = new Uint8ClampedArray(32 * 32 * 4);
					data[(16 * 32 + 16) * 4 + 3] = 255;
					return { width: 32, height: 32, data };
				}
			};
		}
	}
	class Element {
		constructor() { this.children = []; }
		appendChild(canvas) { metrics.appends++; this.children.push(canvas); canvas.parentElement = this; }
		replaceQuantity() { for (const child of this.children) { child.parentElement = undefined; } this.children = []; }
	}
	function load(relative, dependencies = {}) {
		const filename = path.resolve(__dirname, relative);
		const source = ts.transpileModule(readFileSync(filename, "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText;
		const exports = {};
		vm.runInNewContext(source, {
			exports, Uint8ClampedArray,
			document: { createElement: tag => { assert.equal(tag, "canvas"); return new Canvas(); } },
			require: name => { assert.ok(Object.hasOwn(dependencies, name)); return dependencies[name]; }
		});
		return exports;
	}
	const { ItemRarity } = load("../data/ItemRarity.ts");
	const { ItemRarityEffects: Effects } = load("ItemRarityEffects.ts", {
		"../SingletonRepo": { singletons: { getSpriteStore: () => ({ get: filename => images.get(filename) }) } }
	});
	function item(image, rarity = ItemRarity.RARE) {
		images.set("item.png", image);
		return {
			sprite: { filename: "item.png", offsetX: 0, offsetY: 0 }, state: 0,
			getRarity: () => rarity, getXFrameIndex() { return this.sprite.offsetX / 32; },
			getWidth: () => 1, getHeight: () => 1
		};
	}
	return { metrics, Effects, ItemRarity, item, Element };
}

function source(width = 5, height = 5) {
	return { width, height, data: new Uint8ClampedArray(width * height * 4) };
}
function opaque(pixels, x, y, alpha = 255) { pixels.data[(y * pixels.width + x) * 4 + 3] = alpha; }
function pixel(pixels, x, y) { return Array.from(pixels.data.slice((y * pixels.width + x) * 4, (y * pixels.width + x) * 4 + 4)); }

test("slot outline follows four alpha neighbors, not diagonal pixels or the slot rectangle", () => {
	const h = harness(), input = source();
	opaque(input, 2, 2);
	const result = h.Effects.createPixels(input, h.ItemRarity.COMMON, "outline");
	assert.deepEqual([result.width, result.height], [7, 7]);
	for (const [x, y] of [[2, 3], [4, 3], [3, 2], [3, 4]]) { assert.deepEqual(pixel(result, x, y), [158, 158, 158, 255]); }
	assert.equal(result.data.filter((value, index) => index % 4 === 3 && value > 0).length, 4);
	assert.equal(pixel(result, 3, 3)[3], 0);
	assert.equal(pixel(result, 2, 2)[3], 0);
});

test("outline uses Java's alpha threshold, colors and per-rarity opacity", () => {
	const h = harness();
	for (const [rarity, expectedAlpha] of [[h.ItemRarity.COMMON, 255], [h.ItemRarity.RARE, 128], [h.ItemRarity.EPIC, 179], [h.ItemRarity.LEGENDARY, 102]]) {
		const input = source(); opaque(input, 2, 2, 16);
		assert.ok(h.Effects.createPixels(input, rarity, "outline").data.every(value => value === 0));
		opaque(input, 2, 2, 17);
		const color = rarity.colorHex.slice(1).match(/../g).map(value => parseInt(value, 16));
		assert.deepEqual(pixel(h.Effects.createPixels(input, rarity, "outline"), 2, 3), [...color, expectedAlpha]);
	}
});

test("ground glow fades over three pixels and never paints opaque source pixels", () => {
	const h = harness(), input = source(); opaque(input, 2, 2); opaque(input, 3, 2);
	const result = h.Effects.createPixels(input, h.ItemRarity.LEGENDARY, "ground");
	assert.deepEqual([result.width, result.height], [11, 11]);
	assert.deepEqual(pixel(result, 4, 5), [255, 140, 0, 153]);
	assert.equal(pixel(result, 3, 5)[3], 102);
	assert.equal(pixel(result, 2, 5)[3], 51);
	assert.equal(pixel(result, 5, 5)[3], 0);
	assert.equal(pixel(result, 6, 5)[3], 0);
	assert.equal(pixel(result, 1, 5)[3], 0);
});

test("ground strength and source alpha match the Java rarity values", () => {
	const h = harness(), input = source(); opaque(input, 2, 2);
	for (const [rarity, alpha] of [[h.ItemRarity.COMMON, 153], [h.ItemRarity.RARE, 128], [h.ItemRarity.EPIC, 102], [h.ItemRarity.LEGENDARY, 153]]) {
		assert.equal(pixel(h.Effects.createPixels(input, rarity, "ground"), 4, 5)[3], alpha);
	}
	opaque(input, 2, 2, 17);
	assert.equal(pixel(h.Effects.createPixels(input, h.ItemRarity.COMMON, "ground"), 4, 5)[3], 10);
});

test("frames, states, rarity and effect mode have independent cached small canvases", () => {
	const h = harness(), image = { width: 96, height: 64, src: "sheet.png" };
	const requests = [[0, 0, h.ItemRarity.RARE, "outline"], [32, 0, h.ItemRarity.RARE, "outline"],
		[0, 32, h.ItemRarity.RARE, "outline"], [0, 0, h.ItemRarity.EPIC, "outline"], [0, 0, h.ItemRarity.RARE, "ground"]];
	const results = requests.map(args => h.Effects.getFrame(image, ...args));
	assert.equal(new Set(results).size, requests.length);
	assert.deepEqual(results.map(canvas => canvas.width), [34, 34, 34, 34, 38]);
	const reads = h.metrics.reads;
	for (let repeat = 0; repeat < 100; repeat++) {
		requests.forEach((args, index) => assert.equal(h.Effects.getFrame(image, ...args), results[index]));
	}
	assert.equal(h.metrics.reads, reads, "No per-frame pixel readback or silhouette regeneration");
});

test("pending, invalid and unreadable images do not break normal rendering; fallback can recover", () => {
	const h = harness(), image = { width: 64, height: 32, src: "sheet.png", complete: false };
	assert.equal(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"), undefined);
	assert.equal(h.metrics.reads, 0);
	image.complete = true;
	assert.ok(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"));
	assert.equal(h.Effects.getFrame(image, 64, 0, h.ItemRarity.RARE, "outline"), undefined);
	image.src = "unreadable.png"; image.unreadable = true;
	assert.equal(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"), undefined);
	const reads = h.metrics.reads;
	assert.equal(h.Effects.getFrame(image, 32, 0, h.ItemRarity.RARE, "outline"), undefined);
	assert.equal(h.metrics.reads, reads);
	image.src = "fallback.png"; image.unreadable = false; image.width = 32;
	assert.ok(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"));
});

test("36 animated rare items reuse their warmed frames without additional pixel readback", () => {
	const h = harness();
	const images = Array.from({ length: 36 }, (_, index) => ({ width: 256, height: 32, src: `item-${index}.png` }));
	for (let frame = 0; frame < 8; frame++) {
		for (const image of images) { h.Effects.getFrame(image, frame * 32, 0, h.ItemRarity.RARE, "outline"); }
	}
	assert.equal(h.metrics.reads, 36 * 8);
	const draws = h.metrics.draws, created = h.metrics.created;
	for (let cycle = 0; cycle < 10; cycle++) {
		for (let frame = 0; frame < 8; frame++) {
			for (const image of images) { h.Effects.getFrame(image, frame * 32, 0, h.ItemRarity.RARE, "outline"); }
		}
	}
	assert.equal(h.metrics.reads, 36 * 8);
	assert.equal(h.metrics.draws, draws);
	assert.equal(h.metrics.created, created);
});

test("effect cache has a bounded byte budget with least-recently-used eviction", () => {
	const h = harness(), image = { width: 96, height: 32, src: "sheet.png" };
	h.Effects.CACHE_BYTES = 34 * 34 * 4 * 2;
	const first = h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline");
	const second = h.Effects.getFrame(image, 32, 0, h.ItemRarity.RARE, "outline");
	assert.equal(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"), first);
	h.Effects.getFrame(image, 64, 0, h.ItemRarity.RARE, "outline");
	assert.ok(h.Effects.cacheBytes <= h.Effects.CACHE_BYTES);
	assert.equal(h.Effects.getFrame(image, 0, 0, h.ItemRarity.RARE, "outline"), first);
	assert.notEqual(h.Effects.getFrame(image, 32, 0, h.ItemRarity.RARE, "outline"), second);
});

test("slot overlay is reused, follows animation and state, and survives quantity replacement", () => {
	const h = harness(), image = { width: 64, height: 64, src: "sheet.png" };
	const item = h.item(image), target = new h.Element();
	h.Effects.updateSlot(target, item);
	const overlay = target.children[0], reads = h.metrics.reads, draws = h.metrics.draws;
	for (let frame = 0; frame < 60; frame++) { h.Effects.updateSlot(target, item); }
	assert.equal(h.metrics.reads, reads);
	assert.equal(h.metrics.draws, draws);
	assert.equal(h.metrics.appends, 1);
	target.replaceQuantity(); h.Effects.updateSlot(target, item);
	assert.equal(target.children[0], overlay);
	assert.equal(h.metrics.draws, draws);
	item.sprite.offsetX = 32; h.Effects.updateSlot(target, item);
	assert.equal(h.metrics.reads, reads + 1);
	item.state = 1; h.Effects.updateSlot(target, item);
	assert.equal(h.metrics.reads, reads + 2);
	assert.equal(target.children[0], overlay);
	h.Effects.updateSlot(target);
	assert.equal(target.children.length, 0);
});

test("unknown rarity remains undecorated and removes a stale outline", () => {
	const h = harness(), item = h.item({ width: 32, height: 32 }), target = new h.Element();
	h.Effects.updateSlot(target, item);
	item.getRarity = () => h.ItemRarity.fromId("unknown");
	const reads = h.metrics.reads;
	h.Effects.updateSlot(target, item);
	assert.equal(target.children.length, 0);
	assert.equal(h.metrics.reads, reads);
});

test("ground glow uses the active frame and row, correct centering and skips contained items", () => {
	const h = harness(), item = h.item({ width: 64, height: 64 });
	item.sprite.offsetX = item.sprite.offsetY = 32; item.getWidth = () => 2;
	const calls = [], context = { drawImage: (...args) => calls.push(args) };
	h.Effects.drawGround(context, item, 100, 200);
	assert.deepEqual(calls[0].slice(1), [113, 197]);
	assert.equal(calls[0][0].width, 38);
	item._parent = {};
	h.Effects.drawGround(context, item, 100, 200);
	assert.equal(calls.length, 1);
	delete item._parent; item.getRarity = () => undefined;
	h.Effects.drawGround(context, item, 100, 200);
	assert.equal(calls.length, 1);
});
