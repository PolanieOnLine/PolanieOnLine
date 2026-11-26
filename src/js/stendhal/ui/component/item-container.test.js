"use strict";

const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

const stendhalRoot = path.resolve(__dirname, "../..");
const mainCSS = readFileSync(path.join(stendhalRoot, "../css/main.css"), "utf8");
const compiled = new Map();
compiled.set("ui/Inventory.ts", ts.transpileModule(readFileSync(path.join(stendhalRoot, "ui/Inventory.ts"), "utf8"), {
	compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
}).outputText);
compiled.set("entity/Chest.ts", ts.transpileModule(readFileSync(path.join(stendhalRoot, "entity/Chest.ts"), "utf8"), {
	compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
}).outputText);
for (const name of ["util/Point.ts", "ui/TouchHandler.ts", "util/GameLanguage.ts", "data/ItemRarity.ts", "data/GlyphAppearance.ts", "entity/ItemMap.ts", "entity/Item.ts", "ui/component/ItemSlotPresentation.ts", "ui/component/ItemSlotAnimation.ts", "ui/component/ItemTooltipPresentation.ts", "ui/component/ItemContainerImplementation.ts", "ui/dialog/ActionContextMenu.ts"]) {
	compiled.set(name, ts.transpileModule(readFileSync(path.join(stendhalRoot, name), "utf8"), {
		compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
	}).outputText);
}

// Source-backed CSS geometry, not a browser renderer. Pixel QA is separate.
function itemSlotGeometry() {
	const rule = mainCSS.replace(/\/\*[\s\S]*?\*\//g, "")
		.match(/(?:^|\})\s*div\.itemSlot\s*\{([^}]+)\}/);
	assert.ok(rule, "The actual inventory slot CSS rule exists");
	const declarations = Object.fromEntries(rule[1].split(";").filter(value => value.trim()).map(value => {
		const separator = value.indexOf(":");
		return [value.slice(0, separator).trim(), value.slice(separator + 1).trim()];
	}));
	function px(property) {
		assert.match(declarations[property], /^\d+px$/, `${property} remains a single pixel size`);
		return Number.parseInt(declarations[property], 10);
	}
	assert.equal(declarations["box-sizing"] || "content-box", "content-box");
	assert.match(declarations.border, /^\d+px solid transparent$/);
	const border = Number.parseInt(declarations.border, 10);
	const padding = px("padding");
	const width = px("width");
	const height = px("height");
	const content = { x: border + padding, y: border + padding, width, height };
	const borderBox = { x: 0, y: 0, width: width + 2 * (padding + border), height: height + 2 * (padding + border) };
	assert.equal(declarations.background, "none", "The interactive slot does not paint an animated background");
	assert.equal(declarations["mask-image"], undefined);
	assert.equal(declarations["border-image"], undefined);
	assert.equal(declarations["box-shadow"], undefined);
	assert.equal(declarations["border-radius"], "6px");
	return { border, content, borderBox };
}

function createHarness() {
	const metrics = {
		queries: 0, itemLookups: 0, quantityWrites: 0, imageWrites: 0,
		positionWrites: 0, tooltipUpdates: 0, tooltipRemovals: 0, steps: 0
	};
	let now = 1000;
	class Clock extends Date {
		constructor(...args) { super(...(args.length ? args : [now])); }
		static now() { return now; }
	}
	class Element {
		constructor(id = "") {
			this.id = id;
			this.nodeType = 1;
			this.hidden = false;
			this.visible = true;
			this.attributes = new Map();
			this.listeners = new Map();
			this.children = [];
			this.classes = new Set();
			this.classList = {
				add: (...values) => values.forEach(value => this.classes.add(value)),
				remove: (...values) => values.forEach(value => this.classes.delete(value)),
				contains: value => this.classes.has(value)
			};
			this.style = new Proxy({setProperty(key, value) { this[key] = value; }}, {
				set: (style, key, value) => {
					if (key === "backgroundImage") { metrics.imageWrites++; }
					if (key === "backgroundPosition") { metrics.positionWrites++; }
					style[key] = value;
					return true;
				}
			});
		}
		set textContent(value) { metrics.quantityWrites++; this.replaceChildren(); this.text = value; }
		get textContent() { return (this.text || "") + this.children.map(child => child.textContent).join(""); }
		appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
		remove() { if (this.parentElement) { this.parentElement.children = this.parentElement.children.filter(child => child !== this); } }
		replaceChildren(...children) {
			for (const child of this.children) { child.parentElement = null; }
			this.children = []; this.text = "";
			children.forEach(child => this.appendChild(child));
		}
		set title(value) { this.attributes.set("title", value); }
		get title() { return this.attributes.get("title") || ""; }
		setAttribute(key, value) { this.attributes.set(key, value); }
		removeAttribute(key) { this.attributes.delete(key); }
		addEventListener(type, listener) {
			const listeners = this.listeners.get(type) || [];
			listeners.push(listener);
			this.listeners.set(type, listeners);
		}
		removeEventListener(type, listener) {
			this.listeners.set(type, (this.listeners.get(type) || []).filter(value => value !== listener));
		}
		getBoundingClientRect() { return {left: 10, bottom: 20}; }
		closest() {
			for (let element = this; element; element = element.parentElement) {
				if (element.hidden || element.attributes?.get("aria-hidden") === "true") { return element; }
			}
			return null;
		}
		checkVisibility() { return this.visible && !this.hidden; }
		getClientRects() { return this.visible && !this.hidden ? [{}] : []; }
	}
	const observers = [];
	class VisibilityObserver {
		constructor(callback) { this.callback = callback; this.elements = []; observers.push(this); }
		observe(element) { this.elements.push(element); }
		disconnect() { this.disconnected = true; }
		setVisible(element, visible) {
			this.callback([{ target: element, isIntersecting: visible }]);
		}
	}
	const document = { body: new Element(), visibilityState: "visible", createElement: () => new Element(), querySelectorAll: () => [], getElementById: () => ({focus() {}}) };
	document.createTextNode = text => ({textContent: text});
	const sentActions = [], dragFrames = [];
	const marauroa = { me: undefined, currentZoneName: "0_test",
		clientFramework: { sendAction: action => sentActions.push(action) } };
	const diagnostics = { enabled: false, freezeIcons: false, revision: 0 };
	const diagnosticCounts = new Map();
	const AdminDiagnostics = {
		get: key => diagnostics.enabled && !!diagnostics[key],
		getRevision: () => diagnostics.revision,
		inventory: (owner, visible, animated) => diagnosticCounts.set(owner, { visible, animated }),
		removeInventory: owner => diagnosticCounts.delete(owner)
	};
	const stendhal = { config: { getBoolean: () => false },
		ui: { touch: { isTouchEngaged: () => false, isLongTouch: () => false, setHolding() {}, holding: () => false },
			actionContextMenu: {set() {}},
			html: { esc: text => text, extractPosition: event => event, extractTarget: event => event.target } } };
	class MouseEvent {
		constructor(target, options = {}) {
			Object.assign(this, {target, currentTarget: target, button: 0, clientX: 10, clientY: 10}, options);
		}
		preventDefault() {}
	}
	class TouchEvent {
		constructor(type, target, options = {}) {
			Object.assign(this, {type, target, currentTarget: target, pageX: 10, pageY: 10,
				cancelable: true, defaultPrevented: false}, options);
			const touch = {target, pageX: this.pageX, pageY: this.pageY};
			this.touches = type === "touchend" ? [] : [touch];
			this.targetTouches = this.touches;
			this.changedTouches = [touch];
		}
		preventDefault() { if (this.cancelable) this.defaultPrevented = true; }
		stopPropagation() {}
	}
	class DragEvent {
		constructor(target) {
			this.type = "dragstart";
			this.target = target;
			this.dataTransfer = { setDragImage: image => { this.image = image; } };
		}
		preventDefault() { this.prevented = true; }
		stopPropagation() {}
	}
	const images = new Map();
	const spriteStore = {
		get: filename => images.get(filename) || { width: 0, height: 0 },
		checkPath: filename => images.get(filename)?.src || filename,
		getAreaOf: (image, width, height, x, y) => {
			const frame = { image, width, height, x, y };
			dragFrames.push(frame);
			return frame;
		}
	};
	const singletons = { getSpriteStore: () => spriteStore, getConfigManager: () => stendhal.config,
		getJoystickController: () => ({isEngaged: () => false}), getHeldObjectManager: () => ({
			onRelease() {}, set(heldObject) { stendhal.ui.heldObject = heldObject; }
		}) };
	const raritySlotCalls = [], groundCalls = [];
	const rarityEffects = { ItemRarityEffects: {
		updateSlot(target, item) {
			raritySlotCalls.push({ target, item, quantity: target.textContent,
				frame: item?.getXFrameIndex(), state: item?.state, rarity: item?.getRarity()?.id });
		},
		drawGround(context, item, x, y) {
			groundCalls.push({ context, item, x, y, offsetX: item.sprite.offsetX, offsetY: item.sprite.offsetY });
		}
	} };
	function load(name, dependencies) {
		const exports = {};
		vm.runInNewContext(compiled.get(name), {
			exports, Date: Clock, document, console, window: {innerWidth: 1024, innerHeight: 768, TouchEvent},
			fetch: async url => ({ok: true, text: async () => readFileSync(path.resolve(stendhalRoot, "../../../data/languages", path.basename(url)), "utf8")}),
			HTMLElement: Element, IntersectionObserver: VisibilityObserver, DragEvent, MouseEvent, TouchEvent,
			getComputedStyle: element => ({ visibility: element.visible ? "visible" : "hidden" }),
			require: name => {
				assert.ok(Object.hasOwn(dependencies, name), `Unexpected dependency: ${name}`);
				return dependencies[name];
			}
		}, { filename: name.replace(/\.ts$/, ".js") });
		return exports;
	}
	const { Point } = load("util/Point.ts", {});
	const { TouchHandler } = load("ui/TouchHandler.ts", {"../stendhal": {stendhal}, "../util/Point": {Point}});
	stendhal.ui.touch = TouchHandler.get();
	const { ItemRarity } = load("data/ItemRarity.ts", {});
    const glyphAppearance = load("data/GlyphAppearance.ts", {});
	const { GameLanguage } = load("util/GameLanguage.ts", {});
	class Entity {
		set(key, value) { this[key] = value; }
		buildActions(list) { list.push({ title: "Zobacz", type: "look" }); }
		getIdPath() { return `[7\tbag\t${this.id}]`; }
	}
	const { ItemMap } = load("entity/ItemMap.ts", { "../SingletonRepo": { singletons }, marauroa: { marauroa } });
	const { Item } = load("entity/Item.ts", {
		"./ItemMap": { ItemMap },
		"./Entity": { Entity }, "../sprite/TextSprite": { TextSprite: class {} },
		"../sprite/ItemRarityEffects": rarityEffects,
		"../data/Paths": { Paths: { sprites: "/sprites" } },
        "../data/GlyphAppearance": glyphAppearance,
		"../data/ItemRarity": { ItemRarity }, "../SingletonRepo": { singletons },
		"../util/GameLanguage": { GameLanguage },
		marauroa: { marauroa }
	});
	const slotPresentation = load("ui/component/ItemSlotPresentation.ts", {
		"../../data/Paths": { Paths: { gui: "/gui" } }
	});
	const tooltipPresentation = load("ui/component/ItemTooltipPresentation.ts", {
		marauroa: {marauroa}, "../../entity/Item": {Item}, "../../util/GameLanguage": {GameLanguage}
	});
	const slotAnimation = load("ui/component/ItemSlotAnimation.ts", {
        "../../data/GlyphAppearance": glyphAppearance,
		"../../SingletonRepo": {singletons}, "../../sprite/ItemRarityEffects": rarityEffects,
		"../../util/AdminDiagnostics": {AdminDiagnostics}
	});
	const { ItemContainerImplementation: Container } = load("ui/component/ItemContainerImplementation.ts", {
        "../../data/GlyphAppearance": glyphAppearance,
		marauroa: { marauroa }, "../../stendhal": { stendhal }, "../UI": { ui: {createSingletonFloatingWindow() {}} },
		"../dialog/ActionContextMenu": {ActionContextMenu: class {}}, "../dialog/DropQuantitySelectorDialog": {},
		"../dialog/ItemUpgradeDialog": {ItemUpgradeDialog: {canSelectItem: () => false}}, "../../entity/Item": { Item },
		"../../SingletonRepo": { singletons }, "../../util/Point": {Point},
		"../../data/Paths": { Paths: { sprites: "/sprites", gui: "/gui" } },
		"../../data/ItemRarity": { ItemRarity }, "./ItemTooltipPresentation": tooltipPresentation,
		"../../sprite/ItemRarityEffects": rarityEffects,
		"../../util/AdminDiagnostics": { AdminDiagnostics },
		"../../util/GameLanguage": { GameLanguage },
		"./ItemSlotPresentation": slotPresentation,
		"./ItemSlotAnimation": slotAnimation
	});
	const { ActionContextMenu } = load("ui/dialog/ActionContextMenu.ts", {
		"../../util/GameLanguage": { GameLanguage },
		"../UI": { ui: {} }, "../UIComponentEnum": {}, "../component/ChatInputComponent": {},
		"../toolkit/Component": { Component: class {
			constructor() { this.componentElement = { addEventListener() {}, dispatchEvent() {} }; }
		} }, marauroa: { marauroa }, "../../stendhal": { stendhal }
	});
	const { Inventory } = load("ui/Inventory.ts", {marauroa: {marauroa}, "../stendhal": {stendhal}});
	stendhal.ui.equip = Inventory.get();
	const { Chest } = load("entity/Chest.ts", {
		"./PopupInventory": {PopupInventory: class extends Entity {}},
		"../data/color/Color": {Color: {}}, "../util/Chat": {},
		"../data/Paths": {Paths: {sprites: "/sprites"}}, marauroa: {marauroa}, "../stendhal": {stendhal},
		"../ui/component/ItemInventoryComponent": {ItemInventoryComponent: class {setConfigId() {}}},
		"../ui/toolkit/FloatingWindow": {FloatingWindow: class {
			constructor() {this.componentElement = new Element();}
			isOpen() {return true;} setId() {} enableMinimizeButton() {} setFixedWidth() {} setMinimized() {}
		}}
	});
	const updateToolTip = Container.updateToolTipFor;
	Container.updateToolTipFor = (...args) => { metrics.tooltipUpdates++; updateToolTip(...args); };
	function makeItem(id, { width = 96, height = 32, quantity = "2", state = 0, rarity } = {}) {
		const item = new Item();
		item.set("id", id);
		item.set("class", "food");
		item.set("subclass", `item_${id}`);
		item.set("name", `Item ${id}`);
		item.set("quantity", quantity);
		item.set("state", state);
		if (rarity) { item.set("rarity_id", rarity); }
		images.set(item.sprite.filename, { width, height });
		const stepAnimation = item.stepAnimation.bind(item);
		item.stepAnimation = time => { metrics.steps++; stepAnimation(time); };
		return item;
	}
	function makeOwner(items) {
		const slot = {
			items,
			count() { return this.items.length; },
			getByIndex(index) { metrics.itemLookups++; return this.items[index]; }
		};
		return { bag: slot };
	}
	function makeParent(size) {
		const parent = new Element("bag-window");
		parent.elements = Array.from({ length: size }, (_, index) => new Element(`bag${index}`));
		for (const element of parent.elements) { element.classList.add("itemSlot"); }
		for (const element of parent.elements) { element.parentElement = parent; }
		parent.querySelector = selector => {
			metrics.queries++;
			return parent.elements.find(element => `#${element.id}` === selector) || null;
		};
		return parent;
	}
	function makeContainer(items, { size = items.length, object, defaultImage } = {}) {
		const owner = object || makeOwner(items);
		const parent = makeParent(size);
		const container = new Container(parent, "bag", size, owner, "", false, defaultImage);
		return { container, parent, owner };
	}
	function resetMetrics() { for (const key of Object.keys(metrics)) { metrics[key] = 0; } }
	return {
		metrics, document, marauroa, images, observers, Container, Chest, ItemRarity, GameLanguage, raritySlotCalls, groundCalls, stendhal, MouseEvent,
		diagnostics, diagnosticCounts,
		makeItem, makeOwner, makeParent, makeContainer, resetMetrics, ActionContextMenu, sentActions, dragFrames, DragEvent, TouchEvent,
		setNow: value => { now = value; }
	};
}

function iconOf(slot) { return slot.children.find(child => child.className === "item-slot-icon"); }

function quickTransferFixture() {
	const h = createHarness();
	h.marauroa.me = {id: 7};
	h.marauroa.currentZone = {};
	function chest(id) {
		const object = {id, open: true, inventory: {isOpen: () => true, isMinimized: () => false},
			getDistanceTo: () => 1, getIdPath: () => `[${id}]`};
		h.marauroa.currentZone[id] = object;
		const container = h.makeContainer([], {object}).container;
		container.getSlot = () => "content";
		h.stendhal.ui.equip.add(container);
		return {object, container, activate: () => h.stendhal.ui.equip.activateChest?.(object)};
	}
	const destination = chest(42);
	const item = h.makeItem(1, {quantity: "99"});
	const source = h.makeContainer([item]);
	destination.activate();
	function down(options) { source.container.onMouseDown(new h.MouseEvent(source.parent.elements[0], options)); }
	function up(options) { source.container.onMouseUp(new h.MouseEvent(source.parent.elements[0], options)); }
	return {...h, chest, destination, item, source, down, up};
}

function dispatchSlotEvent(target, type, event) {
	for (const listener of target.listeners.get(type) || []) listener(event);
}

// Browsers send compatibility mouse events after an uncancelled touchend.
function finishTouch(h, target, options) {
	const end = new h.TouchEvent("touchend", target, options);
	dispatchSlotEvent(target, "touchend", end);
	if (!end.defaultPrevented) {
		dispatchSlotEvent(target, "mousedown", new h.MouseEvent(target));
		dispatchSlotEvent(target, "mouseup", new h.MouseEvent(target));
	}
	return end;
}

function tapSlot(h, target, start = 1000, duration = 30) {
	h.setNow(start);
	dispatchSlotEvent(target, "touchstart", new h.TouchEvent("touchstart", target));
	h.setNow(start + duration);
	return finishTouch(h, target);
}

test("one inventory tap emits one use despite browser compatibility mouse events", () => {
	const h = createHarness(), item = h.makeItem(1);
	const {parent} = h.makeContainer([item]);
	tapSlot(h, parent.elements[0]);
	assert.equal(h.sentActions.length, 1);
	assert.equal(h.sentActions[0].type, "use");
	assert.equal(h.sentActions[0].target_path, "[7\tbag\t1]");
	assert.equal(h.stendhal.ui.touch.isTouchEngaged(), false);
});

test("two deliberate rapid taps each use once without a cooldown", () => {
	const h = createHarness(), {parent} = h.makeContainer([h.makeItem(1)]);
	tapSlot(h, parent.elements[0], 1000);
	tapSlot(h, parent.elements[0], 1100);
	assert.deepEqual(h.sentActions.map(action => action.type), ["use", "use"]);
});

test("double-click preference requires two actual taps and resets after the pair", () => {
	const h = createHarness(), {parent} = h.makeContainer([h.makeItem(1)]);
	h.stendhal.config.getBoolean = key => key === "inventory.double-click";
	tapSlot(h, parent.elements[0], 1000);
	assert.equal(h.sentActions.length, 0, "Compatibility mouse events must not complete a double tap");
	tapSlot(h, parent.elements[0], 1100);
	assert.equal(h.sentActions.length, 1);
	tapSlot(h, parent.elements[0], 1200);
	assert.equal(h.sentActions.length, 1, "The next gesture needs its own second tap");
});

test("a long inventory touch opens only the context menu without using or transferring", () => {
	const h = quickTransferFixture(), menus = [];
	h.stendhal.ui.actionContextMenu.set = menu => menus.push(menu);
	tapSlot(h, h.source.parent.elements[0], 1000, 400);
	assert.equal(menus.length, 1);
	assert.equal(h.sentActions.length, 0);
	assert.equal(h.stendhal.ui.touch.holding(), false);
});

test("a touch tap with a chest open keeps its use action without a compatibility transfer", () => {
	const h = quickTransferFixture();
	tapSlot(h, h.source.parent.elements[0]);
	assert.deepEqual(h.sentActions.map(action => action.type), ["use"]);
	h.down(); h.up();
	assert.deepEqual(h.sentActions.map(action => action.type), ["use", "equip"], "A real mouse click still transfers immediately");
});

test("touch drag to a chest emits one equip and no compatibility use", () => {
	const h = quickTransferFixture(), target = h.source.parent.elements[0];
	h.item.set("quantity", "1");
	h.stendhal.ui.html.parseSlotName = () => "content";
	h.stendhal.ui.html.extractTarget = () => h.destination.container.getParentElement();
	h.stendhal.ui.equip.getByElement = () => h.destination.container;
	dispatchSlotEvent(target, "touchstart", new h.TouchEvent("touchstart", target));
	dispatchSlotEvent(target, "touchmove", new h.TouchEvent("touchmove", target, {pageX: 60}));
	assert.equal(h.stendhal.ui.touch.holding(), true);
	finishTouch(h, target, {pageX: 60});
	assert.equal(h.sentActions.length, 1);
	assert.equal(h.sentActions[0].type, "equip");
	assert.equal(h.sentActions[0].target_path, "[42\tcontent]");
	assert.equal(h.stendhal.ui.touch.holding(), false);
});

test("a short left click sends the full stack to the open chest before use", () => {
	const h = quickTransferFixture();
	h.stendhal.config.getBoolean = key => key === "inventory.double-click";
	h.down(); h.up();
	assert.equal(h.sentActions.length, 1);
	assert.equal(h.sentActions[0].type, "equip");
	assert.equal(h.sentActions[0].source_path, "[7\tbag\t1]");
	assert.equal(h.sentActions[0].target_path, "[42\tcontent]");
	assert.equal(h.sentActions[0].zone, "0_test");
	assert.equal(Object.hasOwn(h.sentActions[0], "clicked"), false);
	assert.equal(Object.hasOwn(h.sentActions[0], "quantity"), false, "The normal equip action moves the full stack");
});

test("the actual Chest registers newly opened windows and reactivates on focus but ordinary updates do not steal focus", () => {
	const h = createHarness(), activations = [];
	h.stendhal.config.getWindowState = () => ({x: 0, y: 0});
	h.stendhal.ui.equip.activateChest = chest => activations.push(chest);
	const chest = new h.Chest();
	chest.openInventoryWindow(false);
	assert.equal(activations.length, 1);
	assert.equal(activations[0], chest);
	chest.openInventoryWindow(false);
	assert.equal(activations.length, 1);
	chest.openInventoryWindow();
	assert.equal(activations.length, 2);
	for (const listener of chest.inventory.componentElement.listeners.get("mousedown")) listener({target: chest.inventory.componentElement});
	for (const listener of chest.inventory.componentElement.listeners.get("focusin")) listener({target: chest.inventory.componentElement});
	assert.equal(activations.length, 4);
	const populatedSlot = {dataItem: h.makeItem(1)};
	const itemEvent = {target: {closest: () => populatedSlot}};
	for (const listener of chest.inventory.componentElement.listeners.get("mousedown")) listener(itemEvent);
	for (const listener of chest.inventory.componentElement.listeners.get("focusin")) listener(itemEvent);
	assert.equal(activations.length, 4, "Clicking another chest's populated slot preserves the current destination");
});

test("drag, movement, long hold and a release without a press never transfer or use", () => {
	for (const gesture of ["drag", "move", "hold", "release"]) {
		const h = quickTransferFixture();
		if (gesture !== "release") h.down();
		if (gesture === "drag") {
			h.source.container.onDragStart(new h.DragEvent(h.source.parent.elements[0]));
			h.stendhal.ui.heldObject = undefined; // drop already consumed the drag
		}
		if (gesture === "move") h.source.container.onMouseMove(new h.MouseEvent(h.source.parent.elements[0], {clientX: 30}));
		if (gesture === "hold") h.setNow(1400);
		h.up();
		assert.equal(h.sentActions.length, 0, gesture);
	}
});

test("a manual drag drop sends only its normal equip and the later mouse release does not deposit again", () => {
	const h = quickTransferFixture(); h.down();
	h.source.container.onDragStart(new h.DragEvent(h.source.parent.elements[0]));
	h.stendhal.ui.html.parseSlotName = () => "content";
	h.destination.container.onDrop(new h.DragEvent(h.source.parent.elements[0]));
	h.up();
	assert.equal(h.sentActions.length, 1);
	assert.equal(h.sentActions[0].type, "equip");
	assert.equal(h.sentActions[0].target_path, "[42\tcontent]");
});

test("right click keeps the context menu and sends no transfer or use", () => {
	const h = quickTransferFixture(); h.down({button: 2}); h.up({button: 2});
	assert.equal(h.sentActions.length, 0);
});

test("closed, minimized, hidden, removed, distant, changed-zone and changed-user targets cancel a pending click", () => {
	for (const stale of ["closed", "minimized", "hidden", "removed", "distance", "zone", "user"]) {
		const h = quickTransferFixture(); h.down();
		if (stale === "closed") h.destination.object.open = false;
		if (stale === "minimized") h.destination.object.inventory.isMinimized = () => true;
		if (stale === "hidden") h.destination.container.getParentElement().hidden = true;
		if (stale === "removed") delete h.marauroa.currentZone[42];
		if (stale === "distance") h.destination.object.getDistanceTo = () => 5;
		if (stale === "zone") h.marauroa.currentZoneName = "other";
		if (stale === "user") h.marauroa.me = {id: 8};
		h.up(); assert.equal(h.sentActions.length, 0, stale);
	}
});

test("most recently focused valid chest wins and closing it falls back to the previous chest", () => {
	const h = quickTransferFixture(), second = h.chest(43);
	second.activate(); h.down(); h.up();
	assert.equal(h.sentActions.at(-1).target_path, "[43\tcontent]");
	h.destination.activate(); h.down(); h.up();
	assert.equal(h.sentActions.at(-1).target_path, "[42\tcontent]");
	h.destination.object.open = false; h.down(); h.up();
	assert.equal(h.sentActions.at(-1).target_path, "[43\tcontent]");
});

test("other slotted sources deposit, ground and chest ancestors do not, and the current chest keeps quick pickup", () => {
	for (const sourcePath of ["[7\trighthand\t1]", "[40\tcontent\t1]"]) {
		const h = quickTransferFixture(); h.item.getIdPath = () => sourcePath;
		h.down(); h.up(); assert.equal(h.sentActions[0].target_path, "[42\tcontent]");
	}
	for (const sourcePath of ["[1]", "[42]"]) {
		const h = quickTransferFixture(); h.item.getIdPath = () => sourcePath;
		h.down(); h.up(); assert.equal(h.sentActions.length, 0, sourcePath);
	}
	const ancestor = quickTransferFixture();
	ancestor.destination.object.getIdPath = () => "[7\tbag\t1\tcontent\t42]";
	ancestor.down(); ancestor.up(); assert.equal(ancestor.sentActions.length, 0, "Containers cannot move into descendants");
	const h = quickTransferFixture(); h.item.getIdPath = () => "[42\tcontent\t1]";
	h.source.container.quickPickup = true;
	h.down(); h.up();
	assert.equal(h.sentActions[0].target_path, "[7\tbag]");
	assert.equal(h.sentActions[0].clicked, "");
});

test("an item in another open chest deposits into the selected chest while its own contents go to the bag", () => {
	const h = quickTransferFixture(), other = h.chest(43);
	function clickContents(chest) {
		const item = h.makeItem(3); item.getIdPath = () => `[${chest.object.id}\tcontent\t3]`;
		chest.object.content = h.makeOwner([item]).bag;
		const parent = h.makeParent(1); parent.elements[0].id = "content0";
		const container = new h.Container(parent, "content", 1, chest.object, "", true);
		container.onMouseDown(new h.MouseEvent(parent.elements[0]));
		container.onMouseUp(new h.MouseEvent(parent.elements[0]));
	}
	clickContents(other);
	assert.equal(h.sentActions.at(-1).target_path, "[42\tcontent]");
	clickContents(h.destination);
	assert.equal(h.sentActions.at(-1).target_path, "[7\tbag]");
});

test("modifier clicks retain their existing use behavior instead of quick deposit", () => {
	for (const modifier of ["ctrlKey", "shiftKey", "metaKey", "altKey"]) {
		const h = quickTransferFixture(); h.down({[modifier]: true}); h.up({[modifier]: true});
		assert.equal(h.sentActions[0].type, "use", modifier);
	}
});

test("detached reward previews share the inventory hover tooltip and dispose all handlers", () => {
	const h = createHarness(), item = h.makeItem(1);
	item.set("name", "płaszcz chaosu");
	item.set("tooltip_stats", {category: "armour", def: "22", min_level: "110"});
	const target = h.document.createElement("div");
	const dispose = h.Container.bindToolTip(target, item);
	for (const handler of target.listeners.get("mouseenter")) {
		handler({clientX: 10, clientY: 10});
	}
	assert.equal(h.document.body.children.length, 1);
	assert.match(h.document.body.children[0].textContent, /22 pkt\. pancerza/);
	assert.match(h.document.body.children[0].textContent, /110/);
	for (const handler of target.listeners.get("mouseleave")) handler();
	assert.equal(h.document.body.children.length, 0);
	for (const handler of target.listeners.get("focus")) handler();
	assert.equal(h.document.body.children.length, 1);
	dispose();
	assert.equal(h.document.body.children.length, 0);
	assert.equal(target.dataItem, undefined);
	assert.equal(target.title, "");
	assert.ok([...target.listeners.values()].every(list => list.length === 0));
	assert.equal(h.sentActions.length, 0);
});

test("ordinary items retain a name tooltip without rarity or published statistics", () => {
	const h = createHarness(), item = h.makeItem(1);
	item.set("name", "polano");
	const {parent} = h.makeContainer([item]);
	assert.equal(parent.elements[0].title, "polano");
});

test("ordinary items with category-only metadata show a popup on hover", () => {
	const h = createHarness(), item = h.makeItem(1);
	item.set("name", "polano");
	item.set("tooltip_stats", {category: "other"});
	const {parent} = h.makeContainer([item]);
	const slot = parent.elements[0];
	for (const listener of slot.listeners.get("mouseenter") || []) {
		listener({currentTarget: slot, clientX: 10, clientY: 10});
	}
	assert.equal(h.document.body.children.length, 1);
	assert.equal(h.document.body.children[0].textContent, "POLANO");
	for (const listener of slot.listeners.get("mouseleave") || []) { listener({currentTarget: slot}); }
	assert.equal(h.document.body.children.length, 0);
	assert.equal(slot.title, "polano");
});

test("marked scroll popup shows the saved destination with category-only metadata", () => {
    const h = createHarness(), item = h.makeItem(1);
    item.set("class", "scroll"); item.set("name", "zwój zapisany");
    item.set("dest", "int_zakopane_townhall,15,7");
    item.set("tooltip_stats", {category: "other"});
    const {parent} = h.makeContainer([item]); const slot = parent.elements[0];
    for (const listener of slot.listeners.get("mouseenter") || []) listener({currentTarget: slot, clientX: 10, clientY: 10});
    const tooltip = h.document.body.children[0];
    assert.match(tooltip.textContent, /ZWÓJ ZAPISANY/);
    assert.match(tooltip.textContent, /int_zakopane_townhall 15,7/);
    assert.equal(tooltip.style["--item-rarity-color"], "var(--text-color, #fff)");
});

test("common equipment, potions and food show their server statistics on hover", () => {
	for (const [name, stats, expected] of [
		["zdobyczny hełm", {category: "armour", def: "124"}, /124 pkt\. pancerza/],
		["wielki eliksir", {category: "other", consumable_healing_instant: "true",
			consumable_healing_amount: "800"}, /Leczenie: \+800 PW/],
		["kanapka", {category: "other", consumable_healing_regen: "10",
			consumable_healing_frequency_seconds: "3", consumable_healing_duration_seconds: "36"},
			/Leczenie: \+10 PW co 3 s.*Czas leczenia: 36 s/]
	]) {
		const h = createHarness(), item = h.makeItem(1);
		item.set("name", name);
		item.set("tooltip_stats", stats);
		const {parent} = h.makeContainer([item]);
		const slot = parent.elements[0];
		for (const listener of slot.listeners.get("mouseenter") || []) {
			listener({currentTarget: slot, clientX: 10, clientY: 10});
		}
		assert.equal(h.document.body.children.length, 1, name);
		assert.match(h.document.body.children[0].textContent, expected, name);
	}
});

test("utility items, keys and trophies have hover popups even without tooltip metadata", () => {
	for (const name of ["środek uspokajający", "spis", "klucz do skrzyni", "róg demona", "serce olbrzyma"]) {
		const h = createHarness(), item = h.makeItem(1);
		item.set("name", name);
		const {parent} = h.makeContainer([item]);
		const slot = parent.elements[0];
		for (const listener of slot.listeners.get("mouseenter") || []) {
			listener({currentTarget: slot, clientX: 10, clientY: 10});
		}
		assert.equal(h.document.body.children.length, 1, name);
		assert.equal(h.document.body.children[0].textContent, name.toUpperCase());
	}
});

test("only money stacks show their full quantity even while the icon still says 992M", () => {
	const h = createHarness(), item = h.makeItem(1, {width: 32, quantity: "992550648"});
	item.set("name", "money");
	item.set("class", "money");
	const {container, parent} = h.makeContainer([item]);
	const slot = parent.elements[0];
	assert.equal(slot.textContent, "992M");
	assert.equal(slot.title, "Łącznie: 992550648 monet");
	item.set("quantity", "992550348");
	container.update();
	assert.equal(slot.textContent, "992M");
	assert.equal(slot.title, "Łącznie: 992550348 monet");
	assert.equal(slot.attributes.get("aria-label"), slot.title);
	item.set("quantity", "2147483647");
	container.update();
	assert.equal(slot.title, "Łącznie: 2147483647 monet");
	item.set("quantity", "1");
	container.update();
	assert.equal(slot.title, "Łącznie: 1 moneta");
	item.set("name", "polano");
	container.update();
	assert.equal(slot.title, "polano");
});

test("money tooltips use the panel wording, correct coin forms and viewer language", async () => {
	const h = createHarness(), item = h.makeItem(1);
	item.set("name", "money");
	for (const [quantity, noun] of [[0,"monet"], [1,"moneta"], [2,"monety"], [5,"monet"], [12,"monet"], [22,"monety"], [101,"monet"]]) {
		item.set("quantity", quantity);
		assert.equal(item.getToolTip(), "Łącznie: " + quantity + " " + noun);
	}
	await h.GameLanguage.load("/data");
	h.GameLanguage.synchronize("en");
	item.set("quantity", 1);
	assert.equal(item.getToolTip(), "Total: 1 coin");
	item.set("quantity", 22);
	assert.equal(item.getToolTip(), "Total: 22 coins");
});

test("the money quantity tooltip is not hidden by structured item tooltip metadata", () => {
	const h = createHarness(), item = h.makeItem(1, {width: 32, quantity: "992550648"});
	item.set("name", "money");
	item.set("tooltip_stats", {category: "other", value: "1"});
	const {parent} = h.makeContainer([item]);
	const slot = parent.elements[0];
	for (const listener of slot.listeners.get("mouseenter") || []) {
		listener({currentTarget: slot, clientX: 0, clientY: 0});
	}
	assert.equal(slot.title, "Łącznie: 992550648 monet");
});

test("animation and quantity updates never rewrite the static slot or replace its layers", () => {
	const h = createHarness(), item = h.makeItem(1);
	const { container, parent } = h.makeContainer([item]);
	const slot = parent.elements[0], icon = iconOf(slot), label = slot.children[1];
	const texture = slot.style["--item-slot-texture"];
	for (let frame = 0; frame < 20; frame++) { container.animate(1000 + frame * 100); }
	item.set("quantity", "150"); container.update();
	assert.equal(iconOf(slot), icon);
	assert.equal(slot.children[1], label);
	assert.equal(slot.textContent, "150");
	assert.equal(slot.style.backgroundImage, undefined);
	assert.equal(slot.style.backgroundPosition, undefined);
	assert.equal(slot.style["--item-slot-texture"], texture);
	container.init(1);
	assert.equal(iconOf(slot), icon, "Reinitializing a reused DOM slot does not duplicate layers");
	assert.equal(slot.children.length, 2);
});

test("static texture, icon and quantity CSS retain independent paint and hit-test layers", () => {
	function declarations(selector) {
		const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
		const rule = mainCSS.match(new RegExp(escaped + "\\s*\\{([^}]+)\\}"));
		assert.ok(rule, selector);
		return Object.fromEntries(rule[1].split(";").filter(value => value.trim()).map(value => {
			const colon = value.indexOf(":");
			return [value.slice(0, colon).trim(), value.slice(colon + 1).trim()];
		}));
	}
	const icon = declarations(".item-slot-icon"), texture = declarations("div.itemSlot::before");
	assert.equal(icon.width, "32px"); assert.equal(icon.height, "32px");
	assert.equal(icon.left, "1px"); assert.equal(icon.top, "1px");
	assert.equal(icon["background-repeat"], "no-repeat");
	assert.equal(texture.width, "38px"); assert.equal(texture.height, "38px");
	assert.equal(texture.left, "-2px"); assert.equal(texture.top, "-2px");
	assert.match(texture.background, /slot\.png/);
	for (const layer of [icon, texture, declarations(".item-slot-quantity"), declarations(".item-rarity-outline")]) {
		assert.equal(layer["pointer-events"], "none", "Only the original slot handles drag and touch events");
		assert.equal(layer["mask-image"], undefined);
		assert.equal(layer["border-image"], undefined);
		assert.equal(layer["box-shadow"], undefined);
	}
	assert.equal(declarations(".item-rarity-outline")["z-index"], "2");
	assert.equal(declarations(".item-slot-quantity")["z-index"], "3");
});

test("the actual CSS crops every frame and state to exactly one 32 px icon", () => {
	const { content } = itemSlotGeometry();
	assert.deepEqual(content, { x: 3, y: 3, width: 32, height: 32 });
	for (let state = 0; state < 3; state++) {
		const h = createHarness();
		const item = h.makeItem(1, { width: 96, height: 96, state });
		const { container, parent } = h.makeContainer([item]);
		const element = iconOf(parent.elements[0]);
		assert.equal(element.style.backgroundImage, `url(${item.sprite.filename})`);
		for (let frame = 0; frame < 3; frame++) {
			container.animate(1000 + frame * 100);
			assert.equal(item.getXFrameIndex(), frame);
			const position = element.style.backgroundPosition.match(/^(-?\d+)px (-?\d+)px$/);
			assert.ok(position, "The renderer keeps the unscaled source-sheet offset");
			// The independent 32px icon has no padding or border and uses unscaled offsets.
			const sheetX = 0 - Number(position[1]);
			const sheetY = 0 - Number(position[2]);
			assert.deepEqual([sheetX, sheetY, sheetX + content.width, sheetY + content.height],
				[frame * 32, state * 32, (frame + 1) * 32, (state + 1) * 32]);
		}
	}
});

test("local diagnostic freezes only inventory frames and restores them without reloading", () => {
	const h = createHarness();
	const item = h.makeItem(1), { container } = h.makeContainer([item]);
	container.animate(1000);
	container.animate(1100);
	const frozenFrame = item.getXFrameIndex();
	h.diagnostics.enabled = h.diagnostics.freezeIcons = true;
	h.diagnostics.revision++;
	h.resetMetrics();
	container.animate(1200); container.animate(1300);
	assert.equal(item.getXFrameIndex(), frozenFrame);
	assert.equal(h.metrics.steps, 0);
	assert.deepEqual(h.diagnosticCounts.get(container), { visible: 1, animated: 1 });
	const context = { drawImage() {} };
	item.drawSpriteAt = () => {};
	item.quantityTextSprite = { getTextMetrics: () => ({ width: 1 }), draw() {} };
	h.setNow(1400); item.draw(context);
	assert.notEqual(item.getXFrameIndex(), frozenFrame, "World animation does not use diagnostic switches");
	h.diagnostics.freezeIcons = false; h.diagnostics.revision++;
	container.animate(1500);
	assert.ok(h.metrics.steps > 0);
	container.dispose();
	assert.equal(h.diagnosticCounts.has(container), false);
});

test("diagnostic revision refreshes decorations on static items, including hidden-then-shown bags", () => {
	const h = createHarness();
	const { container, parent } = h.makeContainer([h.makeItem(1, { width: 32, rarity: "rare" })]);
	h.raritySlotCalls.length = 0;
	h.diagnostics.revision++;
	parent.hidden = true; container.animate(1000);
	assert.equal(h.raritySlotCalls.length, 0);
	parent.hidden = false; container.animate(1100);
	assert.ok(h.raritySlotCalls.length > 0);
});

test("empty and default slots preserve the shared static texture and original geometry", () => {
	const { content, borderBox } = itemSlotGeometry();
	assert.deepEqual(borderBox, { x: 0, y: 0, width: 38, height: 38 });
	assert.ok(content.x > borderBox.x && content.y > borderBox.y);
	assert.ok(content.x + content.width < borderBox.width && content.y + content.height < borderBox.height,
		"The static texture surrounds the independent icon without changing slot geometry");
	for (const defaultImage of [undefined, "slot-head.png"]) {
		const h = createHarness();
		const { container, parent } = h.makeContainer([], { size: 1, defaultImage });
		assert.equal(parent.elements[0].style["--item-slot-texture"], "url(/gui/slot.png)");
		assert.equal(iconOf(parent.elements[0]).style.backgroundImage,
			defaultImage ? "url(/gui/slot-head.png)" : "none");
		assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "0px 0px");
		h.resetMetrics();
		container.update();
		container.animate(1000);
		assert.equal(h.metrics.imageWrites, 0);
		assert.equal(h.metrics.positionWrites, 0);
		assert.equal(h.metrics.steps, 0);
	}
});

test("36 animated items need no inventory lookups or content writes in 60 world frames", () => {
	const h = createHarness();
	const items = Array.from({ length: 36 }, (_, index) => h.makeItem(index));
	const { container, parent } = h.makeContainer(items);
	assert.equal(h.metrics.queries, 36, "Slot elements are resolved during initialization");
	h.resetMetrics();
	for (let frame = 0; frame < 60; frame++) { container.animate(1000 + frame * 16); }
	assert.equal(h.metrics.queries, 0);
	assert.equal(h.metrics.itemLookups, 0);
	assert.equal(h.metrics.quantityWrites, 0);
	assert.equal(h.metrics.imageWrites, 0);
	assert.equal(h.metrics.tooltipUpdates, 0);
	assert.ok(h.metrics.positionWrites > 0);
	assert.ok(h.metrics.positionWrites <= 36 * 10, "Animation does not write at display refresh rate");
	assert.ok(parent.elements.every(element => element.dataItem));
});

test("animation advances at 100 ms and only changed positions are written", () => {
	const h = createHarness();
	const item = h.makeItem(1);
	const { container, parent } = h.makeContainer([item]);
	container.animate(1000);
	h.resetMetrics();
	container.animate(1099);
	assert.equal(h.metrics.steps, 0);
	assert.equal(h.metrics.positionWrites, 0);
	container.animate(1100);
	assert.equal(item.getXFrameIndex(), 1);
	assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "-32px 0px");
	assert.equal(h.metrics.positionWrites, 1);
	container.animate(1199);
	assert.equal(h.metrics.steps, 1);
	container.animate(1200);
	assert.equal(item.getXFrameIndex(), 2);
	assert.equal(h.metrics.positionWrites, 2);
});

test("inventory rendering passes the active rarity frame and removes stale decorations", () => {
	const h = createHarness(), item = h.makeItem(1, { rarity: "rare", height: 64, state: 1 });
	const { container, parent, owner } = h.makeContainer([item]);
	assert.equal(h.raritySlotCalls.at(-1).target, parent.elements[0]);
	assert.equal(h.raritySlotCalls.at(-1).rarity, "rare");
	container.animate(1000); container.animate(1100);
	assert.equal(h.raritySlotCalls.at(-1).frame, 1);
	assert.equal(h.raritySlotCalls.at(-1).state, 1);
	const calls = h.raritySlotCalls.length;
	h.document.visibilityState = "hidden"; container.animate(1200);
	assert.equal(h.raritySlotCalls.length, calls);
	h.document.visibilityState = "visible";
	item.set("quantity", "7"); item.set("rarity_id", "legendary"); container.update();
	assert.equal(h.raritySlotCalls.at(-1).quantity, "7");
	assert.equal(h.raritySlotCalls.at(-1).rarity, "legendary");
	owner.bag.items = []; container.update();
	assert.equal(h.raritySlotCalls.at(-1).item, undefined);
});

test("a pending static rare item gets its outline immediately after the image loads", () => {
	const h = createHarness(), item = h.makeItem(1, { rarity: "rare", width: 0, height: 0 });
	const { container } = h.makeContainer([item]);
	const calls = h.raritySlotCalls.length;
	h.images.set(item.sprite.filename, { width: 32, height: 32 });
	container.animate(1000);
	assert.equal(h.raritySlotCalls.length, calls + 1);
	assert.equal(h.raritySlotCalls.at(-1).rarity, "rare");
	assert.equal(h.raritySlotCalls.at(-1).frame, 0);
	container.animate(1100);
	assert.equal(h.raritySlotCalls.length, calls + 1, "Loaded static icons leave the animation loop");
});

test("world item drawing decorates the current frame and state before painting the item", () => {
	const h = createHarness(), item = h.makeItem(1, { rarity: "rare", height: 64, state: 1 });
	item.x = 3; item.y = 4;
	item.drawAt = () => { assert.equal(h.groundCalls.at(-1).item, item); };
	const context = {};
	item.draw(context);
	assert.equal(h.groundCalls.at(-1).context, context);
	assert.deepEqual([h.groundCalls.at(-1).x, h.groundCalls.at(-1).y], [96, 128]);
	assert.equal(h.groundCalls.at(-1).offsetY, 32, "First frame retains the selected state row");
	h.setNow(1100); item.draw(context);
	assert.equal(h.groundCalls.at(-1).offsetX, 32);
	assert.equal(h.groundCalls.at(-1).offsetY, 32);
});

test("updates immediately reflect quantity, state and rarity changed on the same Item", () => {
	const h = createHarness();
	const item = h.makeItem(1, { rarity: "rare" });
	const { container, parent } = h.makeContainer([item]);
	h.resetMetrics();
	item.set("quantity", "7");
	item.set("state", 2);
	item.set("rarity_id", "epic");
	container.update();
	const element = parent.elements[0];
	assert.equal(element.dataItem, item);
	assert.equal(element.textContent, "7");
	assert.equal(iconOf(element).style.backgroundPosition, "0px -64px");
	assert.ok(element.classList.contains("item-rarity-epic"));
	assert.equal(element.classList.contains("item-rarity-rare"), false);
	assert.match(element.title, /Epicki/);
	assert.equal(h.metrics.quantityWrites, 1);
	assert.equal(h.metrics.imageWrites, 0);
	assert.equal(h.metrics.tooltipUpdates, 1);
});

test("unchanged perceptions preserve a hovered tooltip and avoid DOM writes", () => {
	const h = createHarness();
	const { container, parent } = h.makeContainer([h.makeItem(1, { rarity: "rare" })]);
	const element = parent.elements[0];
	h.Container.rarityToolTipTarget = element;
	h.Container.rarityToolTip = { remove: () => { h.metrics.tooltipRemovals++; } };
	h.resetMetrics();
	for (let i = 0; i < 5; i++) { container.update(); }
	assert.equal(h.metrics.queries, 0);
	assert.equal(h.metrics.imageWrites, 0);
	assert.equal(h.metrics.positionWrites, 0);
	assert.equal(h.metrics.quantityWrites, 0);
	assert.equal(h.metrics.tooltipUpdates, 0);
	assert.equal(h.metrics.tooltipRemovals, 0);
	assert.equal(h.Container.rarityToolTipTarget, element);
});

test("in-place tooltip statistics and display-name changes refresh presentation", () => {
	const h = createHarness();
	const item = h.makeItem(1);
	item.set("tooltip_stats", { attack: 4 });
	const { container } = h.makeContainer([item]);
	h.resetMetrics();
	item.tooltip_stats.attack = 5;
	container.update();
	assert.equal(h.metrics.tooltipUpdates, 1);
	item.set("title", "Renamed item");
	container.update();
	assert.equal(h.metrics.tooltipUpdates, 2);
	assert.equal(h.metrics.imageWrites, 0);
	assert.equal(h.metrics.quantityWrites, 0);
});

test("reorder and removal update dataItem and remove stale quantity and rarity", () => {
	const h = createHarness();
	const first = h.makeItem(1, { rarity: "rare" });
	const second = h.makeItem(2, { rarity: "epic", quantity: "8" });
	const { container, parent, owner } = h.makeContainer([first, second], { size: 3 });
	owner.bag.items = [second, first];
	container.render();
	assert.equal(parent.elements[0].dataItem, second);
	assert.equal(parent.elements[1].dataItem, first);
	owner.bag.items = [first];
	container.update();
	assert.equal(parent.elements[0].dataItem, first);
	assert.equal(parent.elements[1].dataItem, undefined);
	assert.equal(parent.elements[1].textContent, "");
	assert.equal(iconOf(parent.elements[1]).style.backgroundImage, "none");
	assert.equal(parent.elements[1].title, "");
	assert.equal(parent.elements[1].classList.contains("item-rarity"), false);
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 1, "Removed items must not remain in the animation list");
});

test("init after a DOM resize replaces cached elements and visibility observations", () => {
	const h = createHarness();
	const items = [h.makeItem(1), h.makeItem(2)];
	const { container, parent } = h.makeContainer(items, { size: 2 });
	const oldElements = parent.elements;
	const oldObserver = h.observers[0];
	const replacement = h.makeParent(3);
	parent.elements = replacement.elements;
	h.resetMetrics();
	container.init(3);
	assert.equal(h.metrics.queries, 3);
	assert.equal(oldObserver.disconnected, true);
	assert.deepEqual(h.observers[1].elements, parent.elements);
	assert.equal(parent.elements[0].dataItem, items[0]);
	assert.equal(parent.elements[1].dataItem, items[1]);
	assert.equal(parent.elements[2].dataItem, undefined);
	const oldPositions = oldElements.map(element => iconOf(element).style.backgroundPosition);
	container.animate(1000);
	container.animate(1100);
	assert.deepEqual(oldElements.map(element => iconOf(element).style.backgroundPosition), oldPositions);
	assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "-32px 0px");
});

test("visibility observations tolerate an absent slot element", () => {
	const h = createHarness();
	const parent = h.makeParent(2);
	parent.elements = parent.elements.slice(1);
	const owner = h.makeOwner([h.makeItem(1), h.makeItem(2)]);
	const container = new h.Container(parent, "bag", 2, owner, "", false);
	assert.equal(parent.elements[0].dataItem, owner.bag.items[1]);
	assert.doesNotThrow(() => h.observers[0].setVisible(parent.elements[0], false));
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 0);
});

test("closed containers, hidden pages and offscreen slots do not step animations", () => {
	const h = createHarness();
	const { container, parent } = h.makeContainer([h.makeItem(1), h.makeItem(2)]);
	parent.hidden = true;
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 0);
	parent.hidden = false;
	h.document.visibilityState = "hidden";
	container.animate(1100);
	assert.equal(h.metrics.steps, 0);
	h.document.visibilityState = "visible";
	h.observers[0].setVisible(parent.elements[0], false);
	h.observers[0].setVisible(parent.elements[1], false);
	container.animate(1200);
	assert.equal(h.metrics.steps, 0);
	h.observers[0].setVisible(parent.elements[1], true);
	container.animate(1300);
	assert.equal(h.metrics.steps, 1);
	assert.equal(h.metrics.itemLookups, 0);
});

test("Document-parented equipment checks the slot's aria-hidden ancestor", () => {
	const h = createHarness();
	const parent = h.makeParent(2);
	parent.nodeType = 9;
	const equipmentWindow = h.makeParent(0);
	equipmentWindow.setAttribute("aria-hidden", "true");
	for (const element of parent.elements) { element.parentElement = equipmentWindow; }
	const owner = h.makeOwner([h.makeItem(1), h.makeItem(2)]);
	const container = new h.Container(parent, "bag", 2, owner, "", false);
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 0);
	equipmentWindow.setAttribute("aria-hidden", "false");
	container.animate(1100);
	assert.equal(h.metrics.steps, 2);
	assert.equal(h.metrics.queries, 0);
	assert.equal(h.metrics.itemLookups, 0);
});

test("an image not ready during render starts animating after it loads", () => {
	const h = createHarness();
	const item = h.makeItem(1, { width: 0, height: 0 });
	const { container, parent } = h.makeContainer([item]);
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 0);
	h.images.set(item.sprite.filename, { width: 96, height: 32 });
	container.animate(1100);
	assert.equal(h.metrics.steps, 1);
	container.animate(1200);
	assert.equal(item.getXFrameIndex(), 1);
	assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "-32px 0px");
	assert.equal(h.metrics.queries, 0);
	assert.equal(h.metrics.itemLookups, 0);
});

test("an asynchronous image fallback updates a pending icon without a content refresh", () => {
	const h = createHarness();
	const item = h.makeItem(1, { width: 0, height: 0 });
	const { container, parent } = h.makeContainer([item]);
	container.animate(1000);
	h.resetMetrics();
	h.images.set(item.sprite.filename, { width: 32, height: 32, src: "/sprites/failsafe.png" });
	container.animate(1100);
	assert.equal(iconOf(parent.elements[0]).style.backgroundImage, "url(/sprites/failsafe.png)");
	assert.equal(h.metrics.imageWrites, 1);
	assert.equal(h.metrics.queries, 0);
	assert.equal(h.metrics.itemLookups, 0);
	assert.equal(h.metrics.quantityWrites, 0);
	assert.equal(h.metrics.steps, 0, "The loaded static fallback leaves the animation list");
	container.animate(1200);
	assert.equal(h.metrics.imageWrites, 1);
});

test("an empty placeholder resets offsets left by an animated item state", () => {
	const h = createHarness();
	const item = h.makeItem(1, { state: 3, rarity: "epic" });
	const { container, parent, owner } = h.makeContainer([item], { defaultImage: "slot-head.png" });
	container.animate(1000);
	container.animate(1100);
	assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "-32px -96px");
	owner.bag.items = [];
	container.update();
	assert.equal(iconOf(parent.elements[0]).style.backgroundPosition, "0px 0px");
	assert.equal(iconOf(parent.elements[0]).style.backgroundImage, "url(/gui/slot-head.png)");
	assert.equal(parent.elements[0].dataItem, undefined);
	assert.equal(parent.elements[0].classList.contains("item-rarity"), false);
});

test("visibility checks work when the browser has no checkVisibility method", () => {
	const h = createHarness();
	const { container, parent } = h.makeContainer([h.makeItem(1)]);
	parent.checkVisibility = undefined;
	parent.visible = false;
	h.resetMetrics();
	container.animate(1000);
	assert.equal(h.metrics.steps, 0);
	parent.visible = true;
	container.animate(1100);
	assert.equal(h.metrics.steps, 1);
});

test("disposing a container disconnects visibility tracking and removes its hovered tooltip", () => {
	const h = createHarness();
	const { container, parent } = h.makeContainer([h.makeItem(1, { rarity: "epic" })]);
	h.Container.rarityToolTipTarget = parent.elements[0];
	h.Container.rarityToolTip = { remove: () => { h.metrics.tooltipRemovals++; } };
	container.dispose();
	assert.equal(h.observers[0].disconnected, true);
	assert.equal(h.metrics.tooltipRemovals, 1);
	assert.equal(h.Container.rarityToolTipTarget, undefined);
});

test("the animated owner change replaces cached contents before stepping", () => {
	const h = createHarness();
	const oldItem = h.makeItem(1);
	const newItem = h.makeItem(2);
	const parent = h.makeParent(1);
	h.marauroa.me = h.makeOwner([oldItem]);
	const container = new h.Container(parent, "bag", 1, null, "", false);
	h.marauroa.me = h.makeOwner([newItem]);
	h.resetMetrics();
	container.animate(1000);
	assert.equal(parent.elements[0].dataItem, newItem);
	assert.equal(h.metrics.steps, 1);
	assert.equal(oldItem.frameTimeStamp, 0);
	assert.equal(newItem.frameTimeStamp, 1000);
	const explicitOwner = h.makeOwner([oldItem]);
	container.object = explicitOwner;
	container.update();
	assert.equal(parent.elements[0].dataItem, oldItem);
});

test("static icons never enter the animation loop", () => {
	const h = createHarness();
	const item = h.makeItem(1, { width: 32, height: 32 });
	const { container } = h.makeContainer([item]);
	assert.equal(item.isAnimated(), false);
	h.resetMetrics();
	for (let frame = 0; frame < 60; frame++) { container.animate(1000 + frame * 16); }
	assert.equal(h.metrics.steps, 0);
	assert.equal(h.metrics.positionWrites, 0);
	assert.equal(h.metrics.imageWrites, 0);
});

test("changing the actual Item sprite resets its animation cache and frame count", () => {
	const h = createHarness();
	const item = h.makeItem(1);
	assert.equal(item.isAnimated(), true);
	item.stepAnimation(1000);
	item.stepAnimation(1100);
	item.stepAnimation(1200);
	assert.equal(item.getXFrameIndex(), 2);

	h.images.set("/sprites/items/food/two_frames.png", { width: 64, height: 32 });
	item.set("subclass", "two_frames");
	assert.equal(item.getXFrameIndex(), 0);
	assert.equal(item.isAnimated(), true);
	item.stepAnimation(1300);
	item.stepAnimation(1400);
	assert.equal(item.getXFrameIndex(), 1);
	item.stepAnimation(1500);
	assert.equal(item.getXFrameIndex(), 0, "The new two-frame sprite wraps after two frames");

	h.images.set("/sprites/items/food/static.png", { width: 32, height: 32 });
	item.set("subclass", "static");
	assert.equal(item.isAnimated(), false, "Cached animation detection follows the new sprite");
	assert.equal(item.getXFrameIndex(), 0);
});

function returnRing(h, amount = 0, state = 0) {
	const item = h.makeItem(1, { width: 32, height: 64, quantity: "1", state });
	item.set("class", "ring");
	item.set("subclass", "ametyst-ring");
	item.set("name", "pierścień powrotu");
	item.set("amount", amount);
	h.images.set(item.sprite.filename, { width: 32, height: 64 });
	item._parent = {};
	return item;
}

test("return ring offers Use first and sends the normal use action for its inventory path", () => {
	const h = createHarness(), ring = returnRing(h);
	h.marauroa.me = {};
	const menu = new h.ActionContextMenu(ring);
	assert.equal(menu.actions[0].title, "Użyj");
	assert.equal(menu.actions[0].type, "use");
	assert.equal(menu.actions.filter(action => action.type === "use").length, 1);
	assert.match(ring.getCursor(0, 0), /\/cursor\/activity\.png/);
	menu.executeFallbackAction(menu.actions[0].type);
	assert.equal(h.sentActions.length, 1);
	assert.equal(h.sentActions[0].type, "use");
	assert.equal(h.sentActions[0].target_path, ring.getIdPath());
	assert.equal(h.sentActions[0].zone, "0_test");
	const ordinaryRing = h.makeItem(2);
	ordinaryRing.set("class", "ring");
	const actions = [];
	ordinaryRing.buildActions(actions);
	assert.equal(actions.some(action => action.type === "use"), false);
});

test("return ring follows amount, not state, for both numeric and wire-string values", () => {
	const h = createHarness();
	for (const [amount, state, row] of [[0, 0, 1], ["0", "1", 1], [1, 1, 0], ["1", "0", 0]]) {
		const ring = returnRing(h, amount, state);
		assert.equal(ring.getYFrameIndex(), row);
		ring.stepAnimation(1000);
		assert.equal(ring.sprite.offsetY, row * 32);
		assert.equal(ring.isAnimated(), false, "Vertical rows are states, not animation frames");
	}
	const ring = returnRing(h);
	delete ring.amount;
	assert.equal(ring.getYFrameIndex(), 0, "Missing amount matches the Java working-ring default");
});

test("return ring updates bag and equipment icons immediately on save, return and reset", () => {
	const h = createHarness(), ring = returnRing(h);
	const bag = h.makeContainer([ring]), equipment = h.makeContainer([ring]);
	for (const amount of [0, 1, 0, "1", "0"]) {
		ring.set("amount", amount);
		// Deliberately keep a contradictory state to guard against the old rendering.
		ring.set("state", Number(amount));
		bag.container.update(); equipment.container.update();
		const expected = Number(amount) > 0 ? "0px 0px" : "0px -32px";
		assert.equal(iconOf(bag.parent.elements[0]).style.backgroundPosition, expected);
		assert.equal(iconOf(equipment.parent.elements[0]).style.backgroundPosition, expected);
	}
	h.resetMetrics();
	bag.container.animate(1000);
	assert.equal(h.metrics.steps, 0, "Static state changes do not add per-frame inventory work");
});

test("a received return-ring perception dims the existing icon without losing its name or bag position", () => {
	const h = createHarness(), ring = returnRing(h, 1, 1);
	const before = h.makeItem(2), after = h.makeItem(3);
	const { container, parent, owner } = h.makeContainer([before, ring, after]);
	owner.id = 7;
	owner.set = (key, value) => { owner[key] = value; };
	owner.bag.get = id => owner.bag.items.find(item => String(item.id) === String(id));
	owner.bag.del = id => { owner.bag.items = owner.bag.items.filter(item => String(item.id) !== String(id)); };
	owner.bag.add = item => { owner.bag.items.push(item); };
	h.marauroa.rpobjectFactory = { create: () => h.makeItem(99) };
	// Exercise the actual Marauroa receiver extracted by compilejs, rather than
	// setting amount directly. Hidden itemdata must not generate an ID-only
	// deletion before this partial delta, which would recreate an unnamed ring.
	const receiver = path.join(path.dirname(require.resolve("marauroa")), "perception.js");
	vm.runInNewContext(readFileSync(receiver, "utf8"), { window: { marauroa: h.marauroa }, console });
	h.marauroa.currentZone[7] = owner;
	h.marauroa.perceptionListener.onPerceptionEnd = () => container.update();
	assert.equal(iconOf(parent.elements[1]).style.backgroundPosition, "0px 0px");
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2, zoneid: "0_test", aM: {
		c: "player", a: { id: 7 }, s: { bag: [{ c: "item", a: {
			id: ring.id, class: "ring", subclass: "ametyst-ring", amount: "0", state: "0"
		} }] }
	} });
	assert.equal(owner.bag.items[1], ring);
	assert.deepEqual(owner.bag.items.map(item => item.id), [2, 1, 3]);
	assert.equal(ring.name, "pierścień powrotu");
	assert.equal(iconOf(parent.elements[1]).style.backgroundPosition, "0px -32px");
	const actions = [];
	ring.buildActions(actions);
	assert.equal(actions[0].type, "use");
});

test("tooltip keeps life-steal gain with its value and comparison in a separate right-hand group", () => {
	const h = createHarness();
	const tooltip = h.document.createElement("div");
	h.Container.appendStructuredTooltipLine(tooltip, {
		kind: "bonus", text: "+12% kradzieży życia (+2%)",
		deltas: [{ text: "+3,0%", direction: "better" }]
	});
	const row = tooltip.children[0];
	const comparison = row.children.find(child => child.className === "item-tooltip-comparison");
	assert.ok(comparison, "Comparison must have its own right-hand group");
	assert.equal(row.children[0].textContent, "◆ +12% kradzieży życia (+2%)");
	assert.equal(comparison.textContent, " (+3,0%)");
	assert.equal(comparison.children[1].className, "item-tooltip-delta item-tooltip-delta--better");
});

test("tooltip damage-range comparisons stay together beside the tree value", () => {
	const h = createHarness();
	const tooltip = h.document.createElement("div");
	h.Container.appendStructuredTooltipLine(tooltip, {
		kind: "tree", text: "[5–20] pkt. obrażeń za trafienie", branchContinues: true,
		deltas: [{ text: "-2", direction: "worse" }, { text: "+5", direction: "better" }]
	});
	const row = tooltip.children[0];
	assert.equal(row.children[1].textContent, "[5–20] pkt. obrażeń za trafienie");
	assert.equal(row.children[2].className, "item-tooltip-comparison");
	assert.equal(row.children[2].textContent, " (-2–+5)");
});

test("return ring uses the same state row on the ground and while dragging", () => {
	const h = createHarness(), ring = returnRing(h);
	const { container, parent } = h.makeContainer([ring]);
	ring.drawAt = () => {};
	ring.set("x", 2); ring.set("y", 3);
	for (const amount of [0, 1, 0]) {
		ring.set("amount", amount);
		ring.draw({});
		assert.equal(h.groundCalls.at(-1).offsetY, amount > 0 ? 0 : 32);
		const event = new h.DragEvent(parent.elements[0]);
		container.onDragStart(event);
		assert.equal(h.dragFrames.at(-1).width, 32);
		assert.equal(h.dragFrames.at(-1).height, 32);
		assert.equal(h.dragFrames.at(-1).y, amount > 0 ? 0 : 32);
	}
});
