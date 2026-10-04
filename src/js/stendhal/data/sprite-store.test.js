"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

const source = readFileSync(path.join(__dirname, "SpriteStore.ts"), "utf8");
const compiled = ts.transpileModule(source, {
	compilerOptions: {
		module: ts.ModuleKind.CommonJS,
		target: ts.ScriptTarget.ES2022
	}
}).outputText;

function createStore({ bitmapSupported = true } = {}) {
	const operations = [];
	const images = [];
	class MockImage extends EventTarget {
		constructor() {
			super();
			this.complete = false;
			this.width = 0;
			this.height = 0;
			images.push(this);
		}

		load(width = 96, height = 32) {
			this.width = width;
			this.height = height;
			this.complete = true;
			this.dispatchEvent(new Event("load"));
			this.onload?.();
		}
	}
	const exports = {};
	const dependencies = {
		"../stendhal": { stendhal: {} },
		"./Paths": { Paths: { sprites: "/sprites" } },
		"../sprite/image/ImageFilter": { ImageFilter: class {} }
	};
	const context = {
		exports,
		require: (name) => {
			assert.ok(Object.hasOwn(dependencies, name), `Unexpected dependency: ${name}`);
			return dependencies[name];
		},
		Image: MockImage,
		console
	};
	if (bitmapSupported) {
		context.createImageBitmap = (image) => new Promise((resolve, reject) => {
			operations.push({
				image,
				resolve: () => resolve({ width: image.width, height: image.height }),
				reject
			});
		});
	}
	vm.runInNewContext(compiled, context, { filename: "SpriteStore.js" });
	return { store: new exports.SpriteStore(), operations, images };
}

test("repeated reads and promise readers share one pending bitmap", async () => {
	const { store, operations } = createStore();
	const image = store.get("/sprites/items/animated.png");
	image.load();
	for (let i = 0; i < 60; i++) {
		assert.equal(store.get("/sprites/items/animated.png"), image);
	}
	const first = store.getWithPromise("/sprites/items/animated.png");
	const second = store.getWithPromise("/sprites/items/animated.png");
	let settled = false;
	first.then(() => { settled = true; });
	await Promise.resolve();
	assert.equal(settled, false, "Promise readers must wait for bitmap preparation");
	assert.equal(operations.length, 1);

	operations[0].resolve();
	assert.equal(await first, image);
	assert.equal(await second, image);
	assert.equal(image.bitmapWidth, 96);
	assert.equal(image.bitmapHeight, 32);
	assert.equal(store.get("/sprites/items/animated.png"), image);
	assert.equal(operations.length, 1, "A prepared bitmap is reused");
});

test("a failed conversion keeps the image usable and allows another attempt", async () => {
	const { store, operations } = createStore();
	const image = store.get("/sprites/items/retry.png");
	image.load(64, 48);
	const first = store.getWithPromise("/sprites/items/retry.png");
	assert.equal(operations.length, 1);
	operations[0].reject(new Error("Conversion failed"));
	assert.equal(await first, image);
	assert.equal(image.bitmap, undefined);
	assert.equal(image.bitmapWidth, 64);
	assert.equal(image.bitmapHeight, 48);

	const retry = store.getWithPromise("/sprites/items/retry.png");
	assert.equal(operations.length, 2, "A failed operation must not block retries");
	operations[1].resolve();
	assert.equal(await retry, image);
	assert.ok(image.bitmap);
});

test("different images prepare their bitmaps independently", async () => {
	const { store, operations } = createStore();
	const firstImage = store.get("/sprites/items/first.png");
	const secondImage = store.get("/sprites/items/second.png");
	firstImage.load();
	secondImage.load(128, 64);
	const first = store.getWithPromise("/sprites/items/first.png");
	const second = store.getWithPromise("/sprites/items/second.png");
	assert.equal(operations.length, 2);
	assert.equal(operations[0].image, firstImage);
	assert.equal(operations[1].image, secondImage);

	operations[1].resolve();
	assert.equal(await second, secondImage);
	assert.equal(firstImage.bitmap, undefined);
	operations[0].resolve();
	assert.equal(await first, firstImage);
});

test("promise reads wait for load and then for the shared bitmap", async () => {
	const { store, operations, images } = createStore();
	const first = store.getWithPromise("/sprites/items/loading.png");
	const second = store.getWithPromise("/sprites/items/loading.png");
	let settled = false;
	first.then(() => { settled = true; });
	await Promise.resolve();
	assert.equal(operations.length, 0, "Unloaded images are not converted");
	assert.equal(settled, false);

	images[0].load();
	await Promise.resolve();
	assert.equal(operations.length, 1, "Load listeners share the conversion");
	assert.equal(settled, false);
	operations[0].resolve();
	assert.equal(await first, images[0]);
	assert.equal(await second, images[0]);
});

test("browsers without ImageBitmap still return the loaded image", async () => {
	const { store, operations } = createStore({ bitmapSupported: false });
	const image = store.get("/sprites/items/fallback.png");
	image.load(32, 64);
	assert.equal(await store.getWithPromise("/sprites/items/fallback.png"), image);
	assert.equal(image.bitmapWidth, 32);
	assert.equal(image.bitmapHeight, 64);
	assert.equal(image.bitmap, undefined);
	assert.equal(operations.length, 0);
});
