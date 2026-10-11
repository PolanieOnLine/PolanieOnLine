"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	class Element {
		constructor() { this.style = {}; this.listeners = new Map(); this.title = ""; }
		addEventListener(type, listener) { this.listeners.set(type, listener); }
		setAttribute() { }
		removeAttribute(name) { if (name === "title") this.title = ""; }
	}
	class Creature {
		constructor(label) { this.label = label; }
		getArmorLabel() { return this.label; }
		getCursor() { return "crosshair"; }
	}
	const elements = Object.fromEntries(["viewport", "body", "menubutton", "soundbutton", "click-indicator"]
		.map(id => [id, new Element()]));
	const document = { getElementById: id => elements[id], addEventListener() { } };
	const coordinates = [];
	let entity;
	const stendhal = {
		ui: { html: { extractPosition: event => ({ canvasRelativeX: event.clientX, canvasRelativeY: event.clientY }) } },
		zone: { entityAt(x, y) { coordinates.push([x, y]); return entity; } }
	};
	const singletons = {
		getKeyHandler: () => ({ onKeyDown() { }, onKeyUp() { } }),
		getJoystickController: () => ({ registerGlobalEventHandlers() { } })
	};
	function load(filename, dependencies) {
		const exports = {};
		const source = ts.transpileModule(readFileSync(filename, "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText;
		vm.runInNewContext(source, { exports, document, window: { addEventListener() { } },
			require: name => dependencies[name] || {} });
		return exports;
	}
	const { ViewPort } = load(path.join(__dirname, "ViewPort.ts"), {
		"../stendhal": { stendhal }, "../entity/Creature": { Creature }
	});
	const viewport = Object.create(ViewPort.prototype);
	viewport.offsetX = 12;
	viewport.offsetY = 8;
	stendhal.ui.gamewindow = viewport;
	const { Client } = load(path.join(__dirname, "../Client.ts"), {
		"./stendhal": { stendhal }, "./SingletonRepo": { singletons }, "./data/Paths": { Paths: { gui: "/gui" } }
	});
	stendhal.main = Client.get();
	stendhal.main.registerBrowserEventHandlers();
	const canvas = elements.viewport;
	return { canvas, viewport, Creature, coordinates,
		move(target) {
			entity = target;
			// Browsers call event listeners with the canvas, not ViewPort, as this.
			canvas.listeners.get("mousemove").call(canvas,
				{ type: "mousemove", target: canvas, currentTarget: canvas, clientX: 3, clientY: 7 });
		} };
}

test("registered mouse movement keeps the viewport receiver and shows creature armor", () => {
	const h = harness();
	assert.doesNotThrow(() => h.move(new h.Creature("Pancerz ciężki")));
	assert.equal(h.canvas.title, "Pancerz ciężki");
	assert.equal(h.canvas.style.cursor, "crosshair");
	assert.deepEqual(h.coordinates, [[15, 15]]);
});

test("moving between creatures updates armor without breaking mouse input", () => {
	const h = harness();
	h.move(new h.Creature("Pancerz ciężki"));
	h.move(new h.Creature("Pancerz lekki"));
	assert.equal(h.canvas.title, "Pancerz lekki");
	h.move(new h.Creature(undefined));
	assert.equal(h.canvas.title, "");
});

test("moving over ground or a non-creature clears the armor hint and updates the cursor", () => {
	const h = harness();
	h.move(new h.Creature("Pancerz średni"));
	assert.doesNotThrow(() => h.move({ getCursor: () => "pointer" }));
	assert.equal(h.canvas.title, "");
	assert.equal(h.canvas.style.cursor, "pointer");
});
