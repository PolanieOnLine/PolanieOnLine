"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	const actions = [], frames = [], queued = [];
	class Element {
		constructor() { this.style = {}; this.children = []; this.value = ""; }
		appendChild(child) { this.children.push(child); if (this.options.length === 1) this.value = child.value; }
		replaceChildren() { this.children = []; this.value = ""; }
		setAttribute() { }
		get options() { return this.children; }
	}
	class DialogContentComponent {
		constructor() { this.componentElement = new Element(); this.buttons = []; }
		setFrame(frame) { this.frame = frame; }
		close() { this.frame.close(); }
		addButton(label, onclick) { const button = new Element(); button.label = label; button.onclick = onclick;
			this.buttons.push(button); return button; }
	}
	const viewport = { freeze: true };
	const marauroa = { me: {}, clientFramework: { sendAction: action => actions.push(action) } };
	const ui = { createSingletonFloatingWindow(title, content, x, y) {
		const frame = { title, content, x, y, close() { content.onParentClose(); } };
		frames.push(frame); return frame;
	} };
	const dependencies = { marauroa: { marauroa }, "../UI": { ui },
		"../toolkit/DialogContentComponent": { DialogContentComponent }, "../../stendhal": { stendhal: { ui: { viewport } } } };
	const exports = {};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, "ArenaObservationDialog.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText, { exports, document: { createElement: () => new Element() }, queueMicrotask: fn => queued.push(fn),
		require: name => { assert.ok(dependencies[name], `Known dependency: ${name}`); return dependencies[name]; } });
	return { Dialog: exports.ArenaObservationDialog, actions, frames, viewport, queued, marauroa };
}

function event(phase, names = ["Alicja", "Bartek"], title = "Wybierz gracza do obserwacji") {
	return { arena_phase: phase, title, options: names.map(name => `obserwuj ${name}|~|${name}|~|`).join("\t") };
}

test("chooser preserves names and sends silent observation controls instead of chat", () => {
	const h = harness(); h.Dialog.show(event("open"));
	const dialog = h.frames[0].content;
	assert.deepEqual(Array.from(dialog.players.options, option => option.textContent), ["Alicja", "Bartek"]);
	dialog.players.value = "Bartek"; dialog.observe.onclick();
	assert.equal(h.actions[0].type, "arena_observation");
	assert.equal(h.actions[0].operation, "watch"); assert.equal(h.actions[0].target, "Bartek");
	assert.equal(h.actions[0].text, undefined);
	assert.equal(dialog.leave.hidden, true);
	h.Dialog.show(event("watch", ["Alicja", "Bartek"], "Obserwujesz: Bartek"));
	assert.equal(h.frames.length, 1); assert.equal(dialog.players.value, "Bartek");
	assert.equal(dialog.status.textContent, "Obserwujesz: Bartek");
	assert.equal(dialog.leave.hidden, false); dialog.leave.onclick();
	assert.equal(h.actions[1].type, "arena_observation"); assert.equal(h.actions[1].operation, "return");
	assert.equal(h.actions[1].text, undefined);
});

test("empty initial list never opens a window", () => {
	const h = harness(); h.Dialog.show(event("open", []));
	assert.equal(h.frames.length, 0); assert.equal(h.actions.length, 0);
});

test("refresh is silent and an emptied list closes the existing chooser without echo", () => {
	const h = harness(); h.Dialog.show(event("open"));
	const dialog = h.frames[0].content;
	dialog.buttons.find(button => button.label === "Odśwież").onclick();
	assert.equal(h.actions[0].type, "arena_observation"); assert.equal(h.actions[0].operation, "list");
	assert.equal(h.actions[0].text, undefined);
	h.Dialog.show(event("open", []));
	assert.equal(h.Dialog.active, undefined); assert.equal(h.actions.length, 1);
});

test("watching keeps the return button available even if the refreshed list is empty", () => {
	const h = harness(); h.Dialog.show(event("watch", []));
	const dialog = h.frames[0].content;
	assert.equal(dialog.observe.disabled, true); assert.equal(dialog.leave.hidden, false);
	dialog.leave.onclick(); assert.equal(h.actions[0].operation, "return");
});

test("closing sends a return once; server closure does not echo a new command", () => {
	const h = harness(); h.Dialog.show(event("watch")); h.frames[0].close();
	assert.equal(h.actions.length, 1); assert.equal(h.actions[0].type, "arena_observation");
	assert.equal(h.actions[0].operation, "close"); assert.equal(h.actions[0].text, undefined);
	h.Dialog.show(event("open"));
	h.queued.push(() => { h.viewport.freeze = true; });
	h.Dialog.show(event("close", []));
	assert.equal(h.actions.length, 1);
	for (const run of h.queued) run();
	assert.equal(h.viewport.freeze, false);
});

test("names and titles remain plain text instead of executable markup", () => {
	const h = harness(); const name = "<img src=x onerror=alert(1)>";
	h.Dialog.show(event("watch", [name], "<b>Obserwujesz</b>"));
	const dialog = h.frames[0].content;
	assert.equal(dialog.status.textContent, "<b>Obserwujesz</b>");
	assert.equal(dialog.players.options[0].textContent, name);
	assert.equal(dialog.status.innerHTML, undefined);
});

test("arena events are private and do not overwrite NPC chat shortcuts", () => {
	const h = harness(); const exports = {}; const chat = { attending: "Kupiec", options: ["oferta"] };
	const dependencies = { marauroa: { marauroa: h.marauroa, RPEvent: class {} },
		"../util/Chat": { Chat: chat }, "../ui/dialog/ChatOptionsDialog": { ChatOptionsDialog: {} },
		"../ui/dialog/ArenaObservationDialog": { ArenaObservationDialog: h.Dialog } };
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, "../../event/ChatOptionsEvent.ts"), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText, { exports, require: name => dependencies[name] });
	const notification = Object.assign(new exports.ChatOptionsEvent(), event("open"), { npc: "__arena_observation__" });
	notification.execute({}); assert.equal(h.frames.length, 0);
	notification.execute(h.marauroa.me); assert.equal(h.frames.length, 1);
	assert.equal(chat.attending, "Kupiec"); assert.deepEqual(chat.options, ["oferta"]);
});
