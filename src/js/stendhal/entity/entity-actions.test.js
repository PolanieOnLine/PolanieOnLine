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

test("only speaking NPCs offer a targeted conversation without player chat", () => {
	class RPEntity {
		buildActions(list) { list.push({ title: "Zobacz", type: "look" }); }
	}
	const exports = {};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "NPC.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText;
	const marauroa = { me: { name: "MenuPlayer" } };
	vm.runInNewContext(source, { exports, require: name => name === "./RPEntity" ? { RPEntity }
		: name === "marauroa" ? { marauroa } : name === "../data/color/Color" ? { Color: { NPC: "blue" } } : {} });
	const speaking = new exports.NPC();
	speaking.talkable = "";
	const actions = [];
	speaking.buildActions(actions);
	assert.equal(actions[0].title, "Porozmawiaj");
	assert.equal(actions[0].type, "talk");
	assert.equal(actions[0].action, undefined, "uses the existing targeted action sender, not chat");
	assert.equal(actions[1].type, "look");
	const silent = new exports.NPC();
	const silentActions = [];
	silent.buildActions(silentActions);
	assert.equal(silentActions.some(action => action.type === "talk"), false);
	for (const player of ["MenuPlayer", "OtherPlayer", undefined]) {
		if (player === undefined) delete speaking.conversation_player;
		else speaking.conversation_player = player;
		const current = [];
		speaking.buildActions(current);
		assert.equal(current.some(action => action.type === "talk"), player !== "MenuPlayer");
		assert.equal(current.some(action => action.type === "look"), true);
	}
	marauroa.me = null;
	assert.doesNotThrow(() => speaking.buildActions([]));
});

test("conversation menu sends one NPC id and its zone instead of a public greeting", () => {
	const sent = [];
	const marauroa = { currentZoneName: "npc_zone", clientFramework: { sendAction: action => sent.push(action) } };
	const exports = {};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "../ui/dialog/ActionContextMenu.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText;
	vm.runInNewContext(source, { exports, require: name => name === "marauroa" ? { marauroa }
		: name === "../toolkit/Component" ? { Component: class {} } : {} });
	exports.ActionContextMenu.prototype.executeFallbackAction.call({ entity: { id: 17, getIdPath: () => "[17]" } }, "talk");
	assert.equal(sent.length, 1);
	assert.deepEqual(JSON.parse(JSON.stringify(sent[0])), { type: "talk", target_path: "[17]", zone: "npc_zone", target: "#17" });
	assert.equal(sent[0].text, undefined);
});
