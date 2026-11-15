"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

function harness() {
	const actions = [], frames = [], timers = new Map();
	class Element {
		constructor() {
			this.children = []; this.dataset = {}; this.className = ""; this.textContent = "";
			this.style = {setProperty(key, value) { this[key] = value; }};
			this.classList = {
				contains: value => this.className.split(" ").includes(value),
				add: value => { this.classList.toggle(value, true); },
				toggle: (value, enabled) => {
					const classes = new Set(this.className.split(" ").filter(Boolean));
					if (enabled) classes.add(value); else classes.delete(value);
					this.className = [...classes].join(" ");
				}
			};
		}
		append(...children) { children.forEach(child => this.appendChild(child)); }
		appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
		replaceChildren(...children) { this.children.forEach(child => child.parentElement = undefined); this.children = []; this.append(...children); }
		addEventListener(name, handler) { this["on" + name] = handler; }
		setAttribute(key, value) { this[key] = value; }
		getAttribute(key) { return this[key] ?? null; }
		removeAttribute(key) { delete this[key]; }
		contains(element) { return this.children.some(child => child === element || child.contains(element)); }
		scrollBy(options) { this.scrollTop = Math.min(this.scrollHeight - this.clientHeight, Math.max(0, (this.scrollTop || 0) + options.top)); }
	}
	class DialogContentComponent {
		constructor() { this.componentElement = new Element(); }
		setFrame(frame) { this.frame = frame; }
		close() { this.frame.close(); }
		addCloseButton() {}
	}
	class FloatingWindow {
		constructor(title, content) { this.content = content; frames.push(this); }
		setId() {}
		close() { this.content.onParentClose(); }
	}
	const marauroa = {me: {}, clientFramework: {sendAction: action => actions.push(action)}};
	const dependencies = {
		marauroa: {marauroa}, "../../data/Paths": {Paths: {sprites: "/data/sprites", gui: "/data/gui"}},
		"../../SingletonRepo": {singletons: {getSpriteStore: () => ({checkPath: value => value, get: () => ({width: 64})})}},
		"../../util/GameLanguage": {GameLanguage: {getText: value => value, getLanguage: () => "pl"}},
		"../toolkit/DialogContentComponent": {DialogContentComponent}, "../toolkit/FloatingWindow": {FloatingWindow}
	};
	function load(filename) {
		const exports = {};
		vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
			compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
		}).outputText, {exports, document: {createElement: () => new Element()}, Date, Math, Number, Intl,
			setInterval: fn => { const id = timers.size + 1; timers.set(id, fn); return id; }, clearInterval: id => timers.delete(id),
			require: name => { assert.ok(dependencies[name], name); return dependencies[name]; }});
		return exports;
	}
	dependencies["../component/ItemSlotPresentation"] = load("../component/ItemSlotPresentation.ts");
	const Dialog = load("AdventCalendarDialog.ts").AdventCalendarDialog;
	const states = Array(24).fill("available");
	function reply(overrides = {}) {
		Dialog.show({request_id: actions.at(-1).request_id, status: "ok", date: "2026-12-24", year: 2026,
			today: 24, layout: states.map((_, i) => `${i + 1},${i % 6},${Math.floor(i / 6)},1,1`),
			states: states.slice(), revealed_day: 0, ...overrides});
	}
	Dialog.open(); reply();
	return {Dialog, actions, frames, timers, marauroa, states, reply, dialog: frames[0].content};
}

function gift(day, overrides = {}) {
	return {status: "received", revealed_day: day, reward_names: ["money"], reward_classes: ["money"],
		reward_subclasses: ["gold"], reward_quantities: ["250"], ...overrides};
}

test("claim opens its door with inline sprites, other previews survive refresh and subsequent claims", () => {
	const h = harness(); const first = h.dialog.doors.get(1);
	first.onclick(); assert.equal(h.actions.at(-1).command, "claim");
	h.states[0] = "claimed"; h.reply(gift(1));
	assert.ok(first.classList.contains("is-open"));
	assert.ok(first.classList.contains("has-preview"));
	assert.equal(h.dialog.previews.get(1).children.length, 1);
	const slot = h.dialog.previews.get(1).children[0].children[0];
	assert.equal(slot.children[1].textContent, "250");
	assert.match(slot.children[0].style.backgroundImage, /items\/money\/gold.png/);
	h.dialog.send("refresh"); h.reply();
	assert.equal(h.dialog.previews.get(1).children[0].children[0], slot);
	h.dialog.doors.get(2).onclick(); h.states[1] = "claimed"; h.reply(gift(2));
	assert.equal(h.dialog.previews.get(1).children.length, 1);
	assert.equal(h.dialog.previews.get(2).children.length, 1);
	assert.equal(h.dialog.sprites.size, 2);
});

test("reopening keeps claimed doors open with a check and reveals icons only after view", () => {
	const h = harness(); h.dialog.doors.get(24).onclick(); h.states[23] = "claimed"; h.reply(gift(24));
	const stale = h.actions.at(-1).request_id;
	h.frames[0].close(); assert.equal(h.dialog.sprites.size, 0);
	assert.equal(h.dialog.previews.get(24).children.length, 0); assert.equal(h.timers.size, 0);
	h.Dialog.open(); h.reply(); const reopened = h.frames[1].content; const door = reopened.doors.get(24);
	assert.ok(door.classList.contains("is-open")); assert.ok(door.classList.contains("is-claimed"));
	assert.ok(!door.classList.contains("has-preview"));
	h.Dialog.show({request_id: stale, ...gift(24)});
	assert.equal(reopened.previews.get(24).children.length, 0);
	door.onclick(); assert.equal(h.actions.at(-1).command, "view"); h.reply(gift(24, {status: "claimed"}));
	assert.equal(reopened.previews.get(24).children.length, 1);
});

test("future, failed, inactive, mismatched and unsolicited rewards never reveal a gift", () => {
	const h = harness();
	h.dialog.send("refresh"); h.states[0] = "future"; h.reply(gift(1));
	assert.equal(h.dialog.sprites.size, 0);
	h.states[0] = "available"; h.dialog.doors.get(1).onclick(); h.reply(gift(1, {status: "full"}));
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.states[1] = "claimed"; h.reply(gift(2));
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.send("refresh"); h.reply(gift(1)); assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.reply(gift(1)); assert.equal(h.dialog.sprites.size, 1);
	h.dialog.send("refresh"); h.reply(gift(1, {status: "inactive"}));
	assert.equal(h.dialog.sprites.size, 0); assert.equal(h.dialog.previews.get(1).children.length, 0);
	assert.ok(h.dialog.doors.get(1).disabled); assert.ok(!h.dialog.doors.get(1).classList.contains("is-open"));
});

test("state rollback, year changes and account changes discard previews", () => {
	const h = harness(); h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.reply(gift(1));
	h.states[0] = "available"; h.dialog.send("refresh"); h.reply();
	assert.equal(h.dialog.previews.get(1).children.length, 0); assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.reply(gift(1));
	h.dialog.send("refresh"); h.reply({date: "2027-12-01", year: 2027});
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.reply(gift(1, {year: 2027}));
	h.marauroa.me = {}; [...h.timers.values()][0]();
	assert.equal(h.dialog.sprites.size, 0); assert.equal(h.timers.size, 0);
});

test("multiple inline rewards retain exact accessible amounts while large quantity badges stay compact", () => {
	const h = harness(); h.dialog.doors.get(19).onclick(); h.states[18] = "claimed";
	h.reply(gift(19, {reward_names: ["money", "eliksir", "jabłko"], reward_classes: ["money", "drink", "food"],
		reward_subclasses: ["gold", "potion", "apple_red"], reward_quantities: ["25000", "3", "1"]}));
	const rewards = h.dialog.previews.get(19).children;
	assert.equal(rewards.length, 3); assert.equal(h.dialog.sprites.size, 3);
	assert.equal(rewards[0].children[0].children[1].textContent, "25K");
	assert.equal(rewards[2].children[0].children[1].textContent, "");
	assert.equal(rewards[0].title, "monety × 25000");
	assert.match(h.dialog.doors.get(19).getAttribute("aria-label"), /monety × 25000, eliksir × 3, jabłko × 1/);
	const request = h.actions.at(-1).request_id;
	h.Dialog.show({request_id: request, ...gift(19)});
	assert.equal(h.dialog.previews.get(19).children.length, 3);
});

test("a matching late claim reply is accepted once after the interface timeout", () => {
	const h = harness(); const door = h.dialog.doors.get(1); door.onclick();
	h.dialog.sentAt = Date.now() - 9000; [...h.timers.values()][0]();
	assert.equal(h.dialog.waiting, false); assert.equal(door.disabled, false);
	h.states[0] = "claimed"; h.reply(gift(1));
	assert.ok(door.classList.contains("is-open")); assert.equal(h.dialog.sprites.size, 1);
	const slot = h.dialog.previews.get(1).children[0];
	h.reply(gift(1, {reward_quantities: ["999"]}));
	assert.equal(h.dialog.previews.get(1).children[0], slot);
	assert.equal(slot.children[0].children[1].textContent, "250");
});

test("larger reward bundles remain accessible and can scroll inside their own door by keyboard", () => {
	const h = harness(); const door = h.dialog.doors.get(19); door.onclick(); h.states[18] = "claimed";
	h.reply(gift(19, {reward_names: ["A", "B", "C", "D", "E"], reward_classes: Array(5).fill("money"),
		reward_subclasses: Array(5).fill("gold"), reward_quantities: ["1", "2", "3", "4", "5"]}));
	const preview = h.dialog.previews.get(19);
	assert.equal(preview.children.length, 5); assert.equal(h.dialog.sprites.size, 5);
	assert.match(door.getAttribute("aria-label"), /A × 1, B × 2, C × 3, D × 4, E × 5/);
	assert.equal(door.title, "A × 1\nB × 2\nC × 3\nD × 4\nE × 5");
	preview.scrollHeight = 100; preview.clientHeight = 50;
	let prevented = false; const actions = h.actions.length;
	door.onkeydown({key: "ArrowDown", preventDefault() { prevented = true; }});
	assert.equal(prevented, true); assert.equal(preview.scrollTop, 28);
	door.onkeydown({key: "ArrowDown", preventDefault() {}});
	assert.equal(preview.scrollTop, 50); assert.equal(h.actions.length, actions);
	door.onkeydown({key: "ArrowUp", preventDefault() {}});
	assert.equal(preview.scrollTop, 22);
});
