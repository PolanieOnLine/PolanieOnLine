"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function harness() {
	class Element {
		constructor() {
			this.children = [];
			this.listeners = new Map();
			this.attributes = {};
			this.style = {setProperty() {}};
			this.scrollTop = 0;
			this.clientHeight = 80;
			this._html = "";
			const classes = new Set();
			this.classList = {add: value => classes.add(value), remove: value => classes.delete(value),
				contains: value => classes.has(value), toggle(value, enabled) {enabled ? classes.add(value) : classes.delete(value);}};
		}
		get scrollHeight() {return this.children.length * 20;}
		get innerHTML() {return this._html + this.children.map(child => child.innerHTML || child.textContent || "").join("");}
		set innerHTML(value) {this._html = value; this.children = [];}
		appendChild(child) {this.children.push(child); return child;}
		append(child) {this.appendChild(child);}
		setAttribute(key, value) {this.attributes[key] = value;}
		addEventListener(type, listener) {this.listeners.set(type, listener);}
		removeEventListener(type) {this.listeners.delete(type);}
		emit(type) {this.listeners.get(type)?.({currentTarget: this});}
		cloneNode(deep) {
			const clone = new Element();
			clone._html = this._html;
			clone.textContent = this.textContent;
			clone.className = this.className;
			if (deep) clone.children = this.children.map(child => child.cloneNode ? child.cloneNode(true) : {...child});
			return clone;
		}
	}
	const root = new Element();
	class Component {
		constructor(element) {this.componentElement = typeof element === "string" ? root : element;}
		isVisible() {return true;}
	}
	let tabs;
	class TabPanelComponent extends Component {
		constructor(layout) {
			super(new Element());
			this.layout = layout;
			this.current = 0;
			this.children = [];
			this.buttons = new Element();
			this.componentElement.append(this.buttons);
			this.componentElement.querySelector = () => this.buttons;
			tabs = this;
		}
		child() {return this.buttons;}
		addTab(label, content) {
			const button = new Element();
			button.innerText = label;
			this.buttons.append(button);
			this.children.push(content);
			this.componentElement.append(content.componentElement);
		}
		getCurrentIndex() {return this.current;}
		setCurrentTab(index) {this.current = index; this.onTabChanged?.();}
	}
	let clipboard;
	const document = {createElement: () => new Element(), createTextNode: text => ({textContent: text})};
	const dependencies = {
		"../toolkit/Component": {Component}, "../toolkit/Panel": {Panel: Component},
		"../toolkit/TabPanelComponent": {TabPanelComponent}, "../../data/enum/Layout": {Layout: {BOTTOM: "bottom"}},
		"../../util/GameLanguage": {GameLanguage: {getText: text => text, registerUI() {}, translateUI() {}}},
		"../../stendhal": {stendhal: {config: {get: () => "Arial"}}},
		"../../util/Chat": {Chat: {formatLogEntry: text => text}}
	};
	const exports = {};
	const source = ts.transpileModule(readFileSync(path.join(__dirname, "ChatLogComponent.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	vm.runInNewContext(source, {exports, document, window: {},
		navigator: {clipboard: {writeText: text => {clipboard = text;}}},
		require: name => dependencies[name] || {}});
	const log = new exports.ChatLogComponent();
	return {log, get tabs() {return tabs;}, get clipboard() {return clipboard;},
		pane(index) {return tabs.children[index].componentElement;},
		rows(index) {return this.pane(index).children.map(row => row.innerHTML);}};
}

test("chat uses the common bottom tab panel with main and private channels", () => {
	const h = harness();
	assert.ok(h.tabs, "missing chat tabs");
	assert.equal(h.tabs.layout, "bottom");
	assert.deepEqual(h.tabs.buttons.children.map(button => button.innerText), ["Główny", "Prywatny"]);
});

test("private channel mirrors Java filtering while main retains every message type", () => {
	const h = harness();
	assert.ok(h.tabs, "missing chat tabs");
	for (const type of ["normal", "privmsg", "group", "support", "tutorial", "quest_invitation", "significant_positive"]) {
		h.log.addLine(type, type);
	}
	assert.equal(h.rows(0).length, 7);
	assert.equal(h.rows(1).length, 4);
	assert.ok(h.tabs.buttons.children[1].classList.contains("chat-unread"));
	h.tabs.setCurrentTab(1);
	assert.equal(h.tabs.buttons.children[1].classList.contains("chat-unread"), false);
	h.log.addLine("client", "local response");
	assert.equal(h.rows(0).length, 7);
	assert.equal(h.rows(1).length, 5);
});

test("channel switches and hidden incoming messages preserve the reader's scroll position", () => {
	const h = harness();
	assert.ok(h.tabs, "missing chat tabs");
	for (let i = 0; i < 20; i++) h.log.addLine("normal", "line " + i);
	h.pane(0).scrollTop = 42;
	h.pane(0).emit("scroll");
	h.tabs.setCurrentTab(1);
	h.log.addLine("normal", "new public message");
	h.tabs.setCurrentTab(0);
	assert.equal(h.pane(0).scrollTop, 42);
});

test("clear and export apply to the selected channel without deleting the other history", () => {
	const h = harness();
	assert.ok(h.tabs, "missing chat tabs");
	h.log.addLine("normal", "public message");
	h.log.addLine("privmsg", "private message");
	h.tabs.setCurrentTab(1);
	h.log.exportContents();
	assert.match(h.clipboard, /private message/);
	assert.doesNotMatch(h.clipboard, /public message/);
	h.log.clear();
	assert.equal(h.rows(1).length, 0);
	assert.equal(h.rows(0).length, 2);
});
