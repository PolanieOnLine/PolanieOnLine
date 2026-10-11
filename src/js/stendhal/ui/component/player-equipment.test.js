"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const containers = [], actions = [];
	class Element {
		constructor() { this.style = {}; }
	}
	class Button extends Element { }
	const elements = { equipment: new Element(), pouch0: new Element() };
	const document = { getElementById: id => elements[id] || null };
	class Component { isVisible() { return true; } }
	class Container {
		constructor(parent, name, size, unused, title, draggable, image) {
			containers.push({ name, size, image });
		}
		markDirty() { }
		update() { }
		animate() { }
	}
	const marauroa = { me: { features: {} }, currentZoneName: "test-zone",
		clientFramework: { sendAction: action => actions.push(action) } };
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "PlayerEquipmentComponent.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText;
	const exports = {};
	const dependencies = {
		"../toolkit/Component": { Component },
		"./ItemContainerImplementation": { ItemContainerImplementation: Container },
		"./InventoryWindowController": { InventoryWindowController: { setTitle() { } } },
		marauroa: { marauroa }
	};
	vm.runInNewContext(source, { exports, document, HTMLElement: Element, HTMLButtonElement: Button,
		window: { addEventListener() { } }, require: name => {
			assert.ok(dependencies[name], `Known dependency: ${name}`);
			return dependencies[name];
		} });
	return { component: new exports.PlayerEquipmentComponent(), containers, elements, marauroa, actions };
}

test("equipment retains one primary pouch and twelve reserve equipment slots", () => {
	const h = harness();
	assert.equal(h.containers.length, 25);
	assert.equal(h.containers.filter(slot => slot.name === "pouch").length, 1);
	assert.equal(h.containers.filter(slot => slot.name.endsWith("_set")).length, 12);
	assert.equal(h.containers.some(slot => slot.name === "pouch_set"), false);
	assert.equal(h.containers.find(slot => slot.name === "feet_set").image, "slot-boots.png");
});

test("pouch feature reveals only the primary pouch and equipment swapping still sends its action", () => {
	const h = harness();
	assert.equal(h.elements.pouch0.style.display, "none");
	h.marauroa.me.features.pouch = true;
	h.component.update();
	assert.equal(h.elements.pouch0.style.display, "block");
	h.component.swapSets();
	assert.equal(h.actions.length, 1);
	assert.equal(h.actions[0].type, "setchange");
	assert.equal(h.actions[0].zone, "test-zone");
});

test("actual HTML and CSS no longer expose a reserve pouch", () => {
	const root = path.resolve(__dirname, "../../..");
	assert.doesNotMatch(readFileSync(path.join(root, "polanieonline.html"), "utf8"), /pouch_set/);
	assert.doesNotMatch(readFileSync(path.join(root, "css/main.css"), "utf8"), /pouch_set/);
});
