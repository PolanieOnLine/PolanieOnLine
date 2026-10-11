"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	class RPObject { set(key, value) { this[key] = value; } }
	const stendhal = { ui: { html: { niceName: name => name[0].toUpperCase() + name.slice(1) } } };
	const common = { marauroa: { RPObject }, "../stendhal": { stendhal }, "../data/Paths": { Paths: { sprites: "/sprites" } } };
	function load(filename, dependencies = {}) {
		const exports = {};
		const source = ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText;
		vm.runInNewContext(source, { exports, require: name => dependencies[name] || common[name] || {} });
		return exports;
	}
	const { Entity } = load("Entity.ts");
	return {
		Entity,
		UseableEntity: load("UseableEntity.ts", { "./Entity": { Entity } }).UseableEntity,
		VisibleEntity: load("VisibleEntity.ts", { "./Entity": { Entity } }).VisibleEntity
	};
}

test("observation orb uses a Polish label without changing the use command", () => {
	const { UseableEntity } = harness();
	const orb = new UseableEntity();
	orb.set("class", "orb");
	orb.set("name", "view_orb");
	const actions = [];
	orb.buildActions(actions);
	assert.equal(actions[0].title, "Użyj");
	assert.equal(actions[0].type, "use");
	assert.equal(actions[1].title, "Zobacz");
	assert.equal(actions[1].type, "look");
});

test("quest-useable objects share the same Polish use label", () => {
	const { VisibleEntity } = harness();
	const entity = new VisibleEntity();
	entity.set("class", "questuseable");
	const actions = [];
	entity.buildActions(actions);
	assert.equal(actions[0].title, "Użyj");
	assert.equal(actions[0].type, "use");
});

test("custom menu labels supplied by the server remain unchanged", () => {
	const { UseableEntity } = harness();
	const entity = new UseableEntity();
	entity.set("menu", "Obserwuj walkę|watch");
	const actions = [];
	entity.buildActions(actions);
	assert.equal(actions[0].title, "Obserwuj walkę");
	assert.equal(actions[0].type, "use");
});

test("look and other action aliases preserve their commands", () => {
	const { Entity } = harness();
	const entity = new Entity();
	const actions = [];
	entity.buildActions(actions);
	assert.equal(actions[0].title, "Zobacz");
	assert.equal(actions[0].type, "look");
	entity.set("action", "read");
	const reading = [];
	entity.buildActions(reading);
	assert.equal(reading[0].title, "Read");
	assert.equal(reading[0].type, "look");
});
