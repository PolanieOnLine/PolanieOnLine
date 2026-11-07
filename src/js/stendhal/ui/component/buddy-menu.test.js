"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function menu(adminlevel, online = true) {
	const sent = [];
	const exports = {};
	const marauroa = {me: {adminlevel}, currentZoneName: "test",
		clientFramework: {sendAction: action => sent.push(action)}};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "BuddyListComponent.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(source, {exports, require: name => name === "marauroa"
		? {marauroa} : {Component: class {}}});
	const component = Object.create(exports.BuddyListComponent.prototype);
	component.current = {className: online ? "online" : "offline", textContent: "Zed Destiny"};
	const actions = [];
	component.buildActions(actions);
	return {actions, component, sent};
}

test("online buddy actions follow Java order with teleport after invite", () => {
	const h = menu(1000);
	assert.deepEqual(h.actions.map(action => action.title.replace(/^\(\*\)\s*/, "")),
		["Porozmawiaj", "Gdzie jest", "Zaproś", "Teleportuj do", "Usuń"]);
	h.actions[2].action(h.component);
	assert.equal(h.sent[0].type, "group_management");
	assert.equal(h.sent[0].action, "invite");
	assert.equal(h.sent[0].params, "Zed Destiny");
	h.actions[3].action(h.component);
	assert.equal(h.sent[1].type, "teleportto");
	assert.equal(h.sent[1].target, "Zed Destiny");
});

test("regular players have invite without administrator teleport and offline buddies stay unchanged", () => {
	assert.deepEqual(menu(0).actions.map(action => action.title),
		["Porozmawiaj", "Gdzie jest", "Zaproś", "Usuń"]);
	assert.deepEqual(menu(1000, false).actions.map(action => action.title),
		["Zostaw wiadomość", "Usuń"]);
});
