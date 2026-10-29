"use strict";
const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function load(filename, dependencies) {
	const exports = {};
	const compiled = ts.transpileModule(readFileSync(filename, "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText;
	vm.runInNewContext(compiled, { exports, require: name => dependencies[name] || {} });
	return exports;
}

function harness() {
	const images = new Map();
	const draws = []; const frames = [];
	let saves = 0; let restores = 0;
	const ctx = {
		imageSmoothingEnabled: true,
		save() { saves++; this.oldSmoothing = this.imageSmoothingEnabled; },
		restore() { restores++; this.imageSmoothingEnabled = this.oldSmoothing; },
		drawImage(...args) { draws.push(args); },
		strokeRect(...args) { frames.push(args); }
	};
	const renderer = load(path.join(__dirname, "CreatureArmorRenderer.ts"), {
		"../../data/Paths": { Paths: { sprites: "/sprites" } },
		"../../SingletonRepo": { singletons: { getSpriteStore: () => ({
			get(filename) {
				if (!images.has(filename)) images.set(filename, { complete: true, width: 16, height: 16 });
				return images.get(filename);
			}
		}) } }
	});
	return { renderer, ctx, images, draws, frames, state: () => ({ saves, restores }) };
}

test("only known server armor types have Polish labels", () => {
	const { renderer } = harness();
	for (const value of [undefined, null, "unknown", "__proto__", "constructor", 1]) {
		assert.equal(renderer.readCreatureArmor(value), undefined);
		assert.equal(renderer.getCreatureArmorLabel(value), undefined);
	}
	assert.equal(renderer.readCreatureArmor("none"), "none");
	assert.equal(renderer.getCreatureArmorLabel("none"), undefined);
	assert.equal(renderer.getCreatureArmorLabel("light"), "Pancerz lekki");
	assert.equal(renderer.getCreatureArmorLabel("medium"), "Pancerz średni");
	assert.equal(renderer.getCreatureArmorLabel("heavy"), "Pancerz ciężki");
});

test("native icons use cached images without scaling, animation or canvas state leaks", () => {
	const h = harness();
	for (const armor of ["light", "medium", "heavy"]) {
		for (let frame = 0; frame < 60; frame++) h.renderer.drawCreatureArmor(h.ctx, armor, 30, 10, 32, 4);
	}
	assert.equal(h.images.size, 3);
	assert.equal(h.draws.length, 180);
	assert.deepEqual(h.draws[0].slice(1), [12, 10]);
	assert.equal(h.draws[0][0], h.draws[59][0]);
	assert.equal(h.ctx.imageSmoothingEnabled, true);
	assert.deepEqual(h.state(), { saves: 180, restores: 180 });
	const [x, y, width, height] = h.frames[0];
	assert.ok(x < 30 && y < 10 && x + width > 62 && y + height > 14);
});

test("unarmored and unknown targets do not load or draw an armor icon", () => {
	const h = harness();
	for (const armor of ["none", undefined, "unknown"]) h.renderer.drawCreatureArmor(h.ctx, armor, 30, 10, 32, 4);
	assert.equal(h.draws.length, 0);
	assert.equal(h.frames.length, 0);
	assert.equal(h.images.size, 0);
});

test("the actual creature view shows armor only for our current target and respects hidden HP", () => {
	const h = harness();
	const marauroa = { me: { getAttackTarget: () => target } };
	let target; let healthBars = 0;
	class RPEntity {
		constructor() { this.titleDrawYOffset = -2; }
		drawHealthBar() { healthBars++; }
	}
	const { Creature } = load(path.resolve(__dirname, "../../entity/Creature.ts"), {
		"./RPEntity": { RPEntity },
		"../data/color/Color": { Color: { CREATURE: "red" } },
		"../util/GameLanguage": { GameLanguage: { getText: text => text } },
		"marauroa": { marauroa },
		"../ui/render/CreatureArmorRenderer": h.renderer
	});
	const creature = new Creature();
	Object.assign(creature, { width: 2, height: 2, drawWidth: 48, drawHeight: 64, armor_type: "heavy" });
	creature.drawHealthBar(h.ctx, 30, 40);
	assert.equal(h.draws.length, 0);
	target = creature;
	creature.drawHealthBar(h.ctx, 30, 40);
	assert.equal(h.draws.length, 1);
	assert.deepEqual(h.draws[0].slice(1), [20, 32]);
	creature.armor_type = "light";
	creature.base_armor_hp = 1122;
	creature.armor_hp = 1122;
	assert.equal(creature.getArmorLabel(), "Pancerz lekki");
	creature.armor_hp = 0;
	assert.equal(creature.getArmorLabel(), "Pancerz lekki");
	creature.armor_hp = 1122;
	creature.drawHealthBar(h.ctx, 30, 40);
	assert.equal(h.draws.length, 2);
	target = new Creature();
	creature.drawHealthBar(h.ctx, 30, 40);
	assert.equal(h.draws.length, 2);
	target = creature;
	creature.no_hpbar = "";
	creature.drawHealthBar(h.ctx, 30, 40);
	assert.equal(h.draws.length, 2);
	assert.equal(healthBars, 4);
});

test("desktop hover shows armor without replacing cursor actions and clears on other entities", () => {
	const { renderer } = harness();
	const { Creature } = load(path.resolve(__dirname, "../../entity/Creature.ts"), {
		"./RPEntity": { RPEntity: class {} },
		"../data/Paths": { Paths: { sprites: "/sprites" } },
		"../data/color/Color": { Color: { CREATURE: "red" } },
		"../util/GameLanguage": { GameLanguage: { getText: text => text } },
		"../ui/render/CreatureArmorRenderer": renderer
	});
	const canvas = { style: {}, title: "", removeAttribute(name) {
		assert.equal(name, "title"); this.title = "";
	} };
	let entity = new Creature(); entity.armor_type = "heavy";
	const stendhal = { ui: {
		html: { extractPosition: () => ({ canvasRelativeX: 10, canvasRelativeY: 20 }) },
		gamewindow: { offsetX: 0, offsetY: 0, getElement: () => canvas }
	}, zone: { entityAt: () => entity } };
	const { ViewPort } = load(path.resolve(__dirname, "../ViewPort.ts"), {
		"../stendhal": { stendhal }, "../entity/Creature": { Creature }
	});
	const viewport = { getElement: () => canvas };
	ViewPort.prototype.onMouseMove.call(viewport, {});
	assert.equal(canvas.title, "Pancerz ciężki");
	assert.equal(canvas.style.cursor, "url(/sprites/cursor/attack.png) 1 3, auto");
	entity.armor_type = "light";
	ViewPort.prototype.onMouseMove.call(viewport, {});
	assert.equal(canvas.title, "Pancerz lekki");
	entity.armor_type = "none";
	ViewPort.prototype.onMouseMove.call(viewport, {});
	assert.equal(canvas.title, "");
	assert.equal(canvas.style.cursor, "url(/sprites/cursor/attack.png) 1 3, auto");
	entity.armor_type = "medium";
	ViewPort.prototype.onMouseMove.call(viewport, {});
	assert.equal(canvas.title, "Pancerz średni");
	entity = { getCursor: () => "default" };
	ViewPort.prototype.onMouseMove.call(viewport, {});
	assert.equal(canvas.title, "");
	assert.equal(canvas.style.cursor, "default");
});
