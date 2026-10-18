"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const metrics = {draws: [], encodes: 0, outlines: 0, encodedSizes: []};
	const images = [];
	const flags = {hideRarity: false, decode: true, unreadable: false, touch: false};
	const source = {width: 96, height: 64, src: "sword.png", complete: true};
	class Element {
		constructor() { this.style = {}; this.children = []; }
		setAttribute() {}
		appendChild(child) { this.children.push(child); child.parentElement = this; }
		remove() { this.parentElement.children = this.parentElement.children.filter(child => child !== this); }
		getContext() { return {drawImage: (...args) => metrics.draws.push(args)}; }
		toDataURL() {
			metrics.encodes++;
			metrics.encodedSizes.push([this.width, this.height]);
			if (flags.unreadable) throw new Error("Tainted canvas");
			return "data:image/png;base64,atlas" + metrics.encodes;
		}
	}
	const exports = {};
	const dependencies = {
		"../../SingletonRepo": {singletons: {getSpriteStore: () => ({get: () => source})}},
		"../../sprite/ItemRarityEffects": {ItemRarityEffects: {getFrame: () => {metrics.outlines++; return {outline: true};}}},
		"../../util/AdminDiagnostics": {AdminDiagnostics: {get: key => !!flags[key]}}
	};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, "ItemSlotAnimation.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText, {exports, window: {matchMedia: () => ({matches: flags.touch})}, document: {createElement: tag => {
		if (tag === "img") {const image = {complete: flags.decode, naturalWidth: flags.decode ? 102 : 0}; images.push(image); return image;}
		return new Element();
	}}, require: name => {assert.ok(dependencies[name], name); return dependencies[name];}});
	const item = {frame: 0, row: 0, rarity: {id: "rare"}, sprite: {filename: "sword.png"},
		getXFrameIndex() {return this.frame;}, getYFrameIndex() {return this.row;}, getRarity() {return this.rarity;}};
	return {Animation: exports.ItemSlotAnimation, item, source, flags, metrics, images, Element};
}

test("animated slots share prepainted frames and change only transforms after warmup", () => {
	const h = harness();
	const slots = Array.from({length: 36}, () => ({target: new h.Element(), icon: new h.Element()}));
	for (const {target, icon} of slots) assert.equal(h.Animation.update(target, icon, h.item), true);
	assert.equal(h.metrics.encodes, 1);
	assert.equal(h.metrics.outlines, 3);
	assert.equal(h.metrics.draws.length, 6);
	for (let frame = 0; frame < 30; frame++) {
		h.item.frame = frame % 3;
		for (const {target, icon} of slots) {
			assert.equal(h.Animation.update(target, icon, h.item), true);
			assert.equal(target.children.length, 1);
			assert.equal(target.children[0].children[0].style.transform, `translate3d(${-h.item.frame * 34}px, 0, 0)`);
			assert.equal(icon.style.visibility, "hidden");
		}
	}
	assert.equal(h.metrics.draws.length, 6, "No canvas painting, readback or PNG encoding per animation tick");
	assert.equal(h.metrics.encodes, 1);
	assert.deepEqual(h.metrics.draws[0].slice(1), [0, 0, 32, 32, 1, 1, 32, 32]);
	assert.equal(h.metrics.draws[1][0].outline, true, "Keep outlines above translucent source pixels");
});

test("state rows and diagnostic outline visibility select their own reusable atlases", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.Animation.update(target, icon, h.item);
	h.item.row = 1; h.Animation.update(target, icon, h.item);
	assert.deepEqual(h.metrics.draws[6].slice(1), [0, 32, 32, 32, 1, 1, 32, 32]);
	h.flags.hideRarity = true; h.Animation.update(target, icon, h.item);
	assert.equal(h.metrics.encodes, 3);
	assert.equal(h.metrics.outlines, 6);
	h.flags.hideRarity = false; h.item.row = 0; h.Animation.update(target, icon, h.item);
	assert.equal(h.metrics.encodes, 3);
	assert.equal(target.children.length, 1);
});

test("pending atlas decoding keeps original icons visible until the replacement is ready", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.flags.decode = false;
	assert.equal(h.Animation.update(target, icon, h.item), false);
	assert.notEqual(icon.style.visibility, "hidden");
	assert.equal(target.children.length, 0);
	h.images[0].complete = true; h.images[0].naturalWidth = 102;
	assert.equal(h.Animation.update(target, icon, h.item), true);
	assert.equal(h.metrics.encodes, 1);
});

test("unreadable or static replacement images restore the original icon without repeated conversion", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.Animation.update(target, icon, h.item);
	h.source.width = 32;
	assert.equal(h.Animation.update(target, icon, h.item), false);
	assert.equal(target.children.length, 0);
	assert.equal(icon.style.visibility, "");
	h.source.width = 64; h.source.src = "tainted.png"; h.flags.unreadable = true;
	for (let tick = 0; tick < 20; tick++) assert.equal(h.Animation.update(target, icon, h.item), false);
	assert.equal(h.metrics.encodes, 2);
});

test("slot removal and disposal remove only the animation layer and restore the icon", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element(), quantity = new h.Element();
	target.appendChild(quantity);
	h.Animation.update(target, icon, h.item);
	h.Animation.clear(target, icon);
	assert.deepEqual(target.children, [quantity]);
	assert.equal(icon.style.visibility, "");
	assert.doesNotThrow(() => h.Animation.clear(target, icon));
});

test("shared-cache eviction never rebuilds an atlas still owned by an animated slot", () => {
	const h = harness(), first = new h.Element(), second = new h.Element(), icon = new h.Element();
	h.Animation.MAX_BYTES = 102 * 34 * 4;
	h.Animation.update(first, icon, h.item);
	h.item.row = 1; h.Animation.update(second, new h.Element(), h.item);
	assert.ok(h.Animation.bytes <= h.Animation.MAX_BYTES);
	h.item.row = 0;
	for (let frame = 0; frame < 30; frame++) {h.item.frame = frame % 3; h.Animation.update(first, icon, h.item);}
	assert.equal(h.metrics.encodes, 2);
});

test("reinitializing slot children reattaches the cached animation instead of hiding the item", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.Animation.update(target, icon, h.item);
	const clip = target.children[0];
	clip.parentElement = null; target.children = [];
	assert.equal(h.Animation.update(target, icon, h.item), true);
	assert.equal(target.children[0], clip);
	assert.equal(h.metrics.encodes, 1);
});

for (const count of [2, 3, 4, 8, 18]) test(`${count}-frame touch icons switch locally rasterized bitmaps without translating their pixel grid`, () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.source.width = count * 32; h.flags.touch = true;
	assert.equal(h.Animation.update(target, icon, h.item), true);
	const sheet = target.children[0].children[0];
	assert.equal(sheet.children.length, count);
	assert.deepEqual(h.metrics.encodedSizes.slice(1), Array.from({length: count}, () => [34, 34]),
		"Every mobile frame has its own bitmap with the same local pixel origin");
	const paints = h.metrics.draws.length;
	for (let i = 0; i < 20; i++) {
		h.item.frame = i % count; h.Animation.update(target, icon, h.item);
		assert.equal(sheet.style.transform, undefined);
		assert.equal(sheet.children[h.item.frame].style.opacity, "1");
		assert.equal(sheet.children[h.item.frame].style.backgroundPosition, "0px 0px");
		for (let n = 0; n < count; n++) if (n !== h.item.frame) assert.equal(sheet.children[n].style.opacity, "0");
	}
	assert.equal(h.metrics.draws.length, paints);
	assert.equal(h.metrics.encodes, count + 1);
});

test("touch slots share decoded local frame bitmaps instead of encoding per slot or tick", () => {
	const h = harness();h.flags.touch = true;h.source.width = 64;
	for (let n = 0; n < 36; n++) {
		const target = new h.Element(), icon = new h.Element();
		for (let tick = 0; tick < 20; tick++) {h.item.frame = tick % 2;h.Animation.update(target, icon, h.item);}
	}
	assert.equal(h.metrics.encodes, 3, "One strip plus two shared local bitmaps, independent of slot and tick count");
});

test("touch icons stay on the ordinary renderer until every local bitmap is decoded", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.flags.touch = true;h.flags.decode = false;h.source.width = 64;
	assert.equal(h.Animation.update(target, icon, h.item), false);
	assert.notEqual(icon.style.visibility, "hidden");
	for (const image of h.images.slice(0, 2)) {image.complete = true;image.naturalWidth = 34;}
	assert.equal(h.Animation.update(target, icon, h.item), false, "A pending later frame must not produce a blank animation tick");
	h.images[2].complete = true;h.images[2].naturalWidth = 34;
	assert.equal(h.Animation.update(target, icon, h.item), true);
	assert.equal(h.metrics.encodes, 3);
});

test("mobile frame memory is bounded while live slots retain their already decoded bitmaps", () => {
	const h = harness(), first = new h.Element(), icon = new h.Element();
	h.flags.touch = true;h.source.width = 64;h.Animation.MAX_BYTES = 2 * 68 * 34 * 4;
	h.Animation.update(first, icon, h.item);
	h.item.row = 1;h.Animation.update(new h.Element(), new h.Element(), h.item);
	assert.ok(h.Animation.bytes <= h.Animation.MAX_BYTES);
	h.item.row = 0;
	for (let tick = 0; tick < 20; tick++) {h.item.frame = tick % 2;h.Animation.update(first, icon, h.item);}
	assert.equal(h.metrics.encodes, 6, "Eviction must not re-encode live mobile bitmaps each tick");
});

test("replacing a touch icon with a longer animation rebuilds its fixed frame set", () => {
	const h = harness(), target = new h.Element(), icon = new h.Element();
	h.flags.touch = true; h.source.width = 64;
	h.Animation.update(target, icon, h.item);
	h.source.width = 256; h.source.src = "other.png"; h.item.frame = 7;
	h.Animation.update(target, icon, h.item);
	assert.equal(target.children.length, 1);
	const sheet = target.children[0].children[0];
	assert.equal(sheet.children.length, 8);
	assert.equal(sheet.children[7].style.opacity, "1");
	assert.equal(sheet.children[0].style.opacity, "0");
});
