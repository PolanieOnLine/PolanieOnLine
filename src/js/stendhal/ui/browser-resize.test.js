"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

class Target {
	constructor() { this.listeners = new Map(); }
	addEventListener(type, listener) {
		if (!this.listeners.has(type)) this.listeners.set(type, new Set());
		this.listeners.get(type).add(listener);
	}
	removeEventListener(type, listener) { this.listeners.get(type)?.delete(listener); }
	dispatch(type) { for (const listener of this.listeners.get(type) || []) listener({type}); }
}

function load(filename, dependencies, globals = {}) {
	const exports = {};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText, {exports, console, ...globals, require: name => dependencies[name] || {}});
	return exports;
}

function frames() {
	const jobs = [];
	return {schedule: job => jobs.push(job), flush() {
		let turns = 0;
		while (jobs.length) { assert.ok(++turns < 30, "resize must settle"); jobs.splice(0).forEach(job => job()); }
	}};
}

function observerHarness(orientation = true, resizeObserver = true) {
	const window = new Target(); window.visualViewport = new Target();
	const orientationTarget = new Target(), raf = frames(), calls = [];
	const viewport = {updateCanvasSize: () => calls.push("canvas"), scheduleResize: () => calls.push("resize"), logViewportSize() {}};
	class MutationObserver { observe() {} }
	class ResizeObserver { observe() {} }
	const {UIUpdateObserver} = load("UIUpdateObserver.ts", {
		"../SingletonRepo": {singletons: {getViewPort: () => viewport}},
		"./UI": {ui: {onDisplayUpdate: () => calls.push("display")}}
	}, {window, screen: orientation ? {orientation: orientationTarget} : {},
		document: {getElementById: () => ({})}, MutationObserver,
		ResizeObserver: resizeObserver ? ResizeObserver : undefined, requestAnimationFrame: raf.schedule});
	return {observer: UIUpdateObserver.get(), window, orientationTarget, raf, calls};
}

test("rotation works without Screen Orientation or ResizeObserver APIs", () => {
	const h = observerHarness(false, false);
	assert.doesNotThrow(() => h.observer.init());
	h.window.dispatch("resize"); h.raf.flush();
	assert.ok(h.calls.includes("resize")); assert.ok(h.calls.includes("display"));
});

test("resize, visual viewport and orientation events coalesce into one display update", () => {
	const h = observerHarness(); h.observer.init();
	h.window.dispatch("resize"); h.orientationTarget.dispatch("change"); h.window.dispatch("orientationchange");
	h.window.visualViewport.dispatch("resize"); h.raf.flush();
	assert.deepEqual(h.calls, ["resize", "display"]);
	h.window.visualViewport.dispatch("resize"); h.raf.flush();
	assert.deepEqual(h.calls, ["resize", "display", "resize", "display"]);
});

test("display refresh positions the joystick after the game and chat have changed size", () => {
	let bottom = 700, joystickBottom = bottom;
	const calls = [];
	const {ui} = load("UI.ts", {
		"./UIComponentEnum": {UIComponentEnum: {BottomPanel: 3}},
		"./SoftwareJoystickController": {SoftwareJoystickController: {get: () => ({update() {
			joystickBottom = bottom; calls.push("joystick");
		}})}},
		"./quickmenu/QuickMenu": {QuickMenu: {refresh: () => calls.push("menu")}}
	});
	ui.constructor.displayReady = true;
	ui.wellKnownComponents.set(3, {refresh() {bottom = 300; calls.push("chat");}});
	ui.onDisplayUpdate();
	assert.equal(joystickBottom, 300, "joystick must use the final viewport, not its old portrait size");
	assert.deepEqual(calls, ["chat", "menu", "joystick"]);
});

function viewportHarness() {
	const raf = frames();
	const size = {width: 390, height: 844};
	const window = {devicePixelRatio: 2, visualViewport: {width: 390, height: 844}, innerWidth: 390, innerHeight: 844};
	const root = {classList: {contains: name => name === "mobile-floating-ui"}};
	const canvas = {style: {height: "844px", width: "390px", setProperty(key, value) { this[key] = value; }, removeProperty(key) { delete this[key]; }}, width: 0, height: 0,
		getBoundingClientRect() { return {width: parseFloat(this.style.width), height: parseFloat(this.style.height)}; },
		getContext: () => ({imageSmoothingEnabled: false})};
	const middle = {getBoundingClientRect: () => ({width: size.width, bottom: size.height})};
	const elements = {client: root, viewport: canvas, middleColumn: middle};
	const document = {body: root, documentElement: {clientWidth: 390, clientHeight: 844},
		getElementById: id => elements[id]};
	const getComputedStyle = element => element === canvas ? {
		height: canvas.style.height, maxHeight: canvas.style["max-height"]?.startsWith("var(") ? `${size.height}px` : canvas.style["max-height"] || `${size.height}px`,
		getPropertyValue: key => key === "--viewport-max-height" ? "100dvh" : key === "--viewport-max-width" ? "100%" : ""
	} : {fontSize: "16px", paddingLeft: "0px", paddingRight: "0px", marginLeft: "0px", marginRight: "0px"};
	const {ViewPort} = load("ViewPort.ts", {"../SingletonRepo": {singletons: {getConfigManager: () => ({get: () => undefined})}}},
		{window, document, getComputedStyle, requestAnimationFrame: raf.schedule});
	const viewport = Object.create(ViewPort.prototype);
	viewport.heightNeedsMeasurement = true; viewport.lastCssHeight = 844;
	viewport.chatFloating = true; viewport.initialCanvasHeight = 600;
	function setSize(width, height) {
		size.width = width; size.height = height;
		Object.assign(window, {innerWidth: width, innerHeight: height});
		Object.assign(window.visualViewport, {width, height});
		Object.assign(document.documentElement, {clientWidth: width, clientHeight: height});
	}
	function rotate(width, height) { setSize(width, height); viewport.updateCanvasSize(); }
	return {viewport, canvas, window, size, rotate, setSize, raf, elements};
}

test("mobile canvas follows portrait-landscape-portrait including its backing buffer", () => {
	const h = viewportHarness();
	h.rotate(390, 844);
	assert.equal(h.canvas.style.height, "844px");
	h.rotate(844, 390);
	assert.equal(h.canvas.style.width, "844px"); assert.equal(h.canvas.style.height, "390px");
	assert.equal(h.canvas.width, 1688); assert.equal(h.canvas.height, 780);
	h.rotate(390, 844);
	assert.equal(h.canvas.style.width, "390px"); assert.equal(h.canvas.style.height, "844px");
	assert.equal(h.canvas.width, 780); assert.equal(h.canvas.height, 1688);
});

test("panel-only resizing still preserves height and desktop does not get a mobile override", () => {
	const h = viewportHarness(); h.rotate(844, 390);
	h.size.height = 350;
	h.viewport.updateCanvasSize({preserveHeight: true});
	assert.equal(h.canvas.style.height, "390px");
	h.viewport.isMobileFloatingUi = () => false;
	h.rotate(1024, 768);
	assert.equal(h.canvas.style.height, "390px");
});

test("visible viewport height bounds the mobile canvas when browser chrome changes", () => {
	const h = viewportHarness();
	h.window.visualViewport.height = 700;
	h.viewport.updateCanvasSize();
	assert.equal(h.canvas.style.height, "700px");
});

test("a browser rotation takes precedence over an already queued panel-only resize", () => {
	const h = viewportHarness(); h.rotate(844, 390);
	h.viewport.scheduleResize(true);
	h.setSize(390, 844);
	h.viewport.scheduleResize(false);
	h.raf.flush();
	assert.equal(h.canvas.style.height, "844px");
	assert.equal(h.canvas.height, 1688);
});

test("default docked chat keeps room for its input and messages through rotation", () => {
	const h = viewportHarness(); h.viewport.chatFloating = false;
	h.elements.bottomPanel = {querySelector: () => ({getBoundingClientRect: () => ({height: 32})})};
	h.rotate(390, 844);
	assert.equal(h.canvas.style.height, "600px");
	h.rotate(844, 390);
	assert.ok(parseFloat(h.canvas.style.height) <= 292.5);
	assert.ok(parseFloat(h.canvas.style.height) >= 240);
	h.rotate(390, 844);
	assert.equal(h.canvas.style.height, "600px");
});

test("chat refresh restores responsive CSS limits rather than the first orientation's pixel cap", () => {
	const h = viewportHarness(); h.rotate(844, 390);
	h.viewport.initialStyle = h.viewport.resolveInitialStyles(h.canvas).styles;
	h.viewport.onChatPanelRefresh(false);
	h.rotate(390, 844);
	assert.equal(h.canvas.style.height, "600px");
	h.viewport.onChatPanelRefresh(true);
	h.viewport.updateCanvasSize();
	assert.equal(h.canvas.style.height, "844px");
});

function floatingHarness() {
	const window = new Target(), raf = frames();
	window.innerWidth = 844; window.innerHeight = 390;
	window.visualViewport = Object.assign(new Target(), {width: 844, height: 390});
	const natural = {width: 800, height: 200};
	const root = Object.assign(new Target(), {
		style: {removeProperty(key) { delete this[key === "min-width" ? "minWidth" : key]; }},
		classList: {add() {}, toggle() {}}, isConnected: true,
		getBoundingClientRect() { return {x: parseFloat(this.style.left) || 0, y: parseFloat(this.style.top) || 0,
			width: Math.max(natural.width, parseFloat(this.style.minWidth) || 0), height: natural.height}; },
		remove() { this.isConnected = false; }
	});
	const children = Object.fromEntries([".windowtitlebar", ".windowtitle", ".windowcontent", ".windowtitleclose"]
		.map(key => [key, Object.assign(new Target(), {style: {}, append() {}})]));
	const resizeObservers = [];
	class ResizeObserver {
		constructor(callback) { this.callback = callback; resizeObservers.push(this); }
		observe() {} disconnect() { this.disconnected = true; }
	}
	class Component {
		constructor() { this.componentElement = root; }
		child(key) { return children[key] || null; }
	}
	const content = {componentElement: Object.assign(new Target(), {style: {}}), onMoved() {}, onParentClose() {}};
	const {FloatingWindow} = load("toolkit/FloatingWindow.ts", {
		"./Component": {Component}, "../../stendhal": {stendhal: {config: {applyTheme() {}, setWindowState() {}}}},
		"../UI": {ui: {getPageOffset: () => ({x: 0, y: 0})}},
		"../../util/Point": {Point: class {constructor(x, y) {this.x = x; this.y = y;}}},
		"../../util/GameLanguage": {GameLanguage: {registerUI() {}, unregisterUI() {}}}
	}, {window, document: {documentElement: {getBoundingClientRect: () => ({width: window.innerWidth, height: 1500})},
		getElementById: () => ({appendChild() {}})}, ResizeObserver, requestAnimationFrame: raf.schedule,
		queueMicrotask: raf.schedule});
	const frame = new FloatingWindow("calendar", content, 24, 600);
	return {frame, window, raf, root, natural, resizeObservers};
}

test("CSS-responsive floating window releases captured minimum width during rotation", () => {
	const h = floatingHarness(); h.raf.flush();
	assert.equal(h.root.style.minWidth, "800px");
	h.frame.setResponsiveWidth(true);
	h.natural.width = 366; h.window.innerWidth = h.window.visualViewport.width = 390;
	h.window.innerHeight = h.window.visualViewport.height = 844;
	h.window.dispatch("resize"); h.raf.flush();
	assert.ok(!h.root.style.minWidth, "landscape width must not lock portrait size");
	assert.ok(parseFloat(h.root.style.left) + 366 <= 390);
	assert.ok(parseFloat(h.root.style.top) + 200 <= 844);
});

test("floating windows stay in the visible screen and remove resize subscriptions on close", () => {
	const h = floatingHarness(); h.raf.flush();
	assert.ok(parseFloat(h.root.style.top) + 200 <= 390, "long document height must not push window offscreen");
	h.root.style.top = "700px";
	h.window.visualViewport.height = 300;
	h.window.visualViewport.dispatch("resize"); h.raf.flush();
	assert.ok(parseFloat(h.root.style.top) + 200 <= 300);
	h.frame.close();
	assert.equal(h.window.listeners.get("resize")?.size || 0, 0);
	assert.equal(h.window.visualViewport.listeners.get("resize")?.size || 0, 0);
	assert.ok(h.resizeObservers.every(observer => observer.disconnected));
});

test("explicit fixed-width windows retain their width policy", () => {
	const h = floatingHarness(); h.raf.flush();
	h.frame.setFixedWidth(320); h.frame.setResponsiveWidth(true); h.raf.flush();
	assert.equal(h.root.style.width, "320px");
	assert.equal(h.root.style.minWidth, "320px");
	h.frame.close();
});
