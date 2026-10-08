"use strict";
const assert = require("node:assert/strict");
const { test } = require("node:test");
const { readFileSync } = require("node:fs");
const vm = require("node:vm");
const path = require("node:path");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

function harness() {
	class Element {
		constructor(tag) { this.tag = tag; this.children = []; this.listeners = new Map(); this.value = ""; }
		appendChild(child) { child.parent = this; this.children.push(child); return child; }
		append(...children) { children.forEach(child => this.appendChild(child)); }
		remove() { if (this.parent) { this.parent.children = this.parent.children.filter(child => child !== this); } }
		setAttribute(key, value) { this[key] = value; }
		addEventListener(event, listener) { this.listeners.set(event, listener); }
		click() { return this.listeners.get("click")?.(); }
		focus() { this.focused = true; }
		select() { this.selected = true; }
	}
	const classes = new Set(), listeners = new Map();
	const document = {
		visibilityState: "visible", body: new Element("body"),
		documentElement: { classList: { toggle(key, enabled) { enabled ? classes.add(key) : classes.delete(key); } } },
		createElement: tag => new Element(tag), createTextNode: text => ({ text }),
		addEventListener: (event, callback) => listeners.set(event, callback),
		removeEventListener: (event, callback) => { if (listeners.get(event) === callback) { listeners.delete(event); } }
	};
	const marauroa = { me: { name: "Admin", adminlevel: 5000 } };
	const stendhal = { data: { build: { version: "1.42", build: "test" } } };
	const clipboard = { texts: [], result: true };
	function load(relative, dependencies) {
		const code = ts.transpileModule(readFileSync(path.join(__dirname, relative), "utf8"), {
			compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
		}).outputText;
		const exports = {};
		vm.runInNewContext(code, { exports, document, navigator: { userAgent: "test phone" },
			require: key => { assert.ok(Object.hasOwn(dependencies, key), key); return dependencies[key]; }
		});
		return exports;
	}
	const { AdminDiagnostics: D } = load("AdminDiagnostics.ts", { marauroa: { marauroa } });
	const { AdminDiagnosticsTab: Tab } = load("../ui/dialog/settings/AdminDiagnosticsTab.ts", {
		"./AbstractSettingsTab": { AbstractSettingsTab: class { constructor(element) { this.componentElement = element; } } },
		"../../../util/AdminDiagnostics": { AdminDiagnostics: D }, "../../../stendhal": { stendhal },
		"../../../util/Clipboard": { copyTextToClipboard: async text => { clipboard.texts.push(text); return clipboard.result; } }
	});
	const { SettingsDialog: Settings } = load("../ui/dialog/SettingsDialog.ts", {
		"../../stendhal": { stendhal: { config: { get: () => "default" } } },
		"./settings/GeneralTab": { GeneralTab: class {} },
		"./settings/InputTab": { InputTab: class {} },
		"./settings/SoundTab": { SoundTab: class {} },
		"./settings/VisualsTab": { VisualsTab: class {} },
		"./settings/AdminDiagnosticsTab": { AdminDiagnosticsTab: Tab },
		"../../util/AdminDiagnostics": { AdminDiagnostics: D },
		"../toolkit/TabDialogContentComponent": { TabDialogContentComponent: class {
			constructor() { this.tabs = []; this.componentElement = { classList: { add() {} } }; }
			child() { return {}; }
			addTab(label, content) { this.tabs.push({ label, content }); }
			addButton() { return {}; }
			addCloseButton() { return {}; }
		} },
		"../toolkit/Tooltip": {}, "../../data/enum/Layout": { Layout: { TOP: 0 } },
		"../../util/Debug": {}, "../component/ToggleSwitch": {}
	});
	return { D, Tab, Settings, document, classes, listeners, marauroa, clipboard };
}

test("diagnostics starts disabled, rejects ordinary players and cannot be preconfigured while disabled", () => {
	const h = harness();
	assert.equal(h.D.get("enabled"), false);
	assert.equal(h.D.set("freezeIcons", true), false);
	for (const adminlevel of [undefined, 0, 600, "garbage"]) {
		h.marauroa.me.adminlevel = adminlevel;
		assert.equal(h.D.set("enabled", true), false);
		assert.equal(h.D.get("enabled"), false);
	}
	assert.equal(h.document.body.children.length, 0);
});

test("actual settings dialog exposes the diagnostic tab only to an administrator", () => {
	const h = harness();
	assert.ok(new h.Settings().tabs.some(tab => tab.label === "Diagnostyka (admin)"));
	h.marauroa.me.adminlevel = 0;
	assert.equal(new h.Settings().tabs.some(tab => tab.label === "Diagnostyka (admin)"), false);
	h.marauroa.me = undefined;
	assert.equal(new h.Settings().tabs.some(tab => tab.label === "Diagnostyka (admin)"), false);
});

test("switches are local to one runtime, reset on disable, character change, logout or privilege loss", () => {
	for (const reason of ["disable", "character", "logout", "privilege"]) {
		const h = harness();
		h.D.set("enabled", true); h.D.set("freezeIcons", true); h.D.set("simpleSlots", true); h.D.set("hideRarity", true);
		assert.equal(h.D.get("freezeIcons"), true);
		assert.equal(h.classes.has("admin-diagnostic-simple-slots"), true);
		assert.equal(harness().D.get("enabled"), false, "Another client is unaffected");
		h.D.frame(1000, 1001); h.D.frame(1020, 1021);
		assert.equal(h.document.body.children.length, 1);
		if (reason === "disable") { h.D.set("enabled", false); }
		if (reason === "character") { h.marauroa.me = { name: "OtherAdmin", adminlevel: 5000 }; }
		if (reason === "logout") { h.marauroa.me = undefined; }
		if (reason === "privilege") { h.marauroa.me.adminlevel = 0; }
		assert.equal(h.D.get("enabled"), false);
		assert.equal(h.classes.size, 0);
		assert.equal(h.document.body.children.length, 0);
		assert.equal(h.listeners.size, 0);
		assert.equal(h.D.sample().frames, 0);
	}
});

test("frame measurements use actual draws, preserve stalls, bound storage and exclude background gaps", () => {
	const h = harness(), owner = {};
	h.D.set("enabled", true);
	h.D.inventory(owner, 14, 10);
	h.D.frame(1000, 1002); h.D.frame(1020, 1024); h.D.frame(1080, 1086);
	let s = h.D.sample();
	assert.equal(s.frames, 2); assert.equal(s.fps, 25); assert.equal(s.frameMs, 40);
	assert.equal(s.p95Ms, 60); assert.equal(s.maxMs, 60); assert.equal(s.slowFrames, 1); assert.equal(s.drawMs, 5);
	assert.equal(s.visibleItems, 14); assert.equal(s.animatedItems, 10);
	h.document.visibilityState = "hidden"; h.listeners.get("visibilitychange")();
	h.D.frame(5000, 5001);
	h.document.visibilityState = "visible"; h.listeners.get("visibilitychange")();
	h.D.frame(9000, 9001);
	assert.equal(h.D.sample().frames, 2, "Suspension is not counted as a stalled game frame");
	for (let i = 1; i <= 500; i++) { h.D.frame(9000 + i * 20, 9001 + i * 20); }
	s = h.D.sample(); assert.equal(s.frames, 300); assert.equal(s.fps, 50);
	h.D.removeInventory(owner); assert.equal(h.D.sample().visibleItems, 0);
	h.D.set("freezeIcons", true); assert.equal(h.D.sample().frames, 0, "Each experiment starts a clean sample");
	assert.match(h.D.report({ version: "1.42", build: "test" }), /1\.42, build test/);
});

test("administrator UI controls experiments immediately and accumulates copyable labelled samples", () => {
	const h = harness(), tab = new h.Tab();
	const labels = tab.componentElement.children.filter(element => element.tag === "label");
	const [enabled, freeze, simple, rarity] = labels.map(label => label.children[0]);
	assert.equal(freeze.disabled, true);
	enabled.checked = true; enabled.listeners.get("change")();
	assert.equal(freeze.disabled, false);
	freeze.checked = true; freeze.listeners.get("change")();
	assert.equal(h.D.get("freezeIcons"), true);
	simple.checked = true; simple.listeners.get("change")();
	assert.equal(h.classes.has("admin-diagnostic-simple-slots"), true);
	rarity.checked = true; rarity.listeners.get("change")();
	assert.equal(h.D.get("hideRarity"), true);
	const buttons = tab.componentElement.children.filter(element => element.tag === "button");
	const output = tab.componentElement.children.find(element => element.tag === "textarea");
	h.D.frame(1000, 1001); h.D.frame(1020, 1021); buttons[0].click(); buttons[0].click();
	assert.match(output.value, /FPS renderera: 50\.0/); assert.match(output.value, /\n---\n/);
	buttons[4].click(); assert.equal(output.focused, true); assert.equal(output.selected, true);
	buttons[2].click(); assert.equal(h.D.get("enabled"), false); assert.equal(freeze.checked, false);
	assert.equal(freeze.disabled, true);
	const reopened = new h.Tab().componentElement.children.find(element => element.tag === "textarea");
	assert.equal(reopened.value, output.value, "Closing settings between samples preserves the report");
	h.marauroa.me = { name: "Different admin", adminlevel: 5000 };
	assert.equal(h.D.getReports(), "", "Reports do not leak to another character");
});

test("copy report button copies the complete saved report, reports failure honestly and rejects empty reports", async () => {
	const h = harness(), tab = new h.Tab();
	const buttons = tab.componentElement.children.filter(element => element.tag === "button");
	const copy = buttons.find(button => button.textContent === "Kopiuj raport");
	const status = tab.componentElement.children.find(element => element.role === "status");
	await copy.click();
	assert.match(status.textContent, /Brak zapisanych próbek/);
	assert.equal(h.clipboard.texts.length, 0);
	h.D.set("enabled", true);
	h.D.frame(1000, 1001); h.D.frame(1020, 1021); buttons[0].click(); buttons[0].click();
	await copy.click();
	assert.equal(h.clipboard.texts[0], h.D.getReports());
	assert.match(h.clipboard.texts[0], /\n---\n/);
	assert.match(status.textContent, /skopiowany do schowka/);
	assert.equal(copy.disabled, false);
	h.clipboard.result = false; await copy.click();
	assert.match(status.textContent, /zablokowała kopiowanie/);
	assert.equal(copy.disabled, false);
});
