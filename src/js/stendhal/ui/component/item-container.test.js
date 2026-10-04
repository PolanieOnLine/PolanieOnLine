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
for (const name of ["data/ItemRarity.ts", "entity/Item.ts", "ui/component/ItemContainerImplementation.ts",
	"ui/component/ItemInventoryComponent.ts", "ui/interaction/QuickSlots.ts"]) {
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
	assert.equal(declarations["background-origin"] || "padding-box", "padding-box");
	assert.match(declarations.border, /^\d+px solid transparent$/);
	const border = Number.parseInt(declarations.border, 10);
	const padding = px("padding");
	const width = px("width");
	const height = px("height");
	const content = { x: border + padding, y: border + padding, width, height };
	const borderBox = { x: 0, y: 0, width: width + 2 * (padding + border), height: height + 2 * (padding + border) };
	const clips = declarations["background-clip"]?.split(",").map(value => value.trim());
	assert.deepEqual(clips, ["content-box", "border-box"], "Only the icon is clipped; the fill keeps its original bounds");
	assert.equal(declarations["background-image"], "none, none", "Uninitialized and material slots also retain two paint layers");
	assert.equal(declarations["background-color"], "rgba(72, 43, 23, 0.85)");
	assert.equal(declarations["border-radius"], "6px");
	assert.equal(declarations["background-repeat"], "no-repeat !important");
	return { border, content, borderBox };
}

test("inventory grids retain their column count when resized, including nonstandard sizes", () => {
	const exports = {};
	vm.runInNewContext(compiled.get("ui/component/ItemInventoryComponent.ts"), {
		exports,
		require: name => name === "../toolkit/Component" ? { Component: class {} } : {}
	});
	const grid = Object.create(exports.ItemInventoryComponent.prototype);
	const classes = new Set();
	const properties = new Map();
	grid.componentElement = {
		classList: { add: name => classes.add(name), remove: name => classes.delete(name) },
		style: { setProperty: (name, value) => properties.set(name, value) }
	};
	grid.slot = "bag";
	grid.suffix = "-test-";
	grid.oldSizeX = 0;
	for (const columns of [2, 3, 4, 5, 6, 8, 6]) {
		grid.setSize(columns, 3);
		assert.equal(properties.get("--inventory-columns"), String(columns));
		assert.equal((grid.componentElement.innerHTML.match(/class='itemSlot'/g) || []).length, columns * 3);
		assert.deepEqual([...classes], [`inventorypopup_${columns}`]);
	}
	assert.match(mainCSS, /grid-template-columns:\s*repeat\(var\(--inventory-columns\), 40px\)/);
});

test("quick slots keep source sheets unscaled and center icons and empty button layers", () => {
	let atlas;
	const exports = {};
	vm.runInNewContext(compiled.get("ui/interaction/QuickSlots.ts"), {
		exports,
		require: name => {
			if (name === "../toolkit/Component") return { Component: class {} };
			if (name === "../../SingletonRepo") return { singletons: { getSpriteStore: () => ({
				getItemIconAtlas: () => atlas, checkPath: filename => filename
			}) } };
			if (name === "../../data/Paths") return { Paths: { sprites: "/sprites", gui: "/gui" } };
			if (name === "../component/ItemContainerImplementation") return { ItemContainerImplementation: {
				updateCursorFor() {}, updateToolTipFor() {}
			} };
			return {};
		}
	});
	const quick = Object.create(exports.QuickSlots.prototype);
	const slot = { style: {} };
	quick.slotCounts = new Map();
	const item = { class: "food", subclass: "test", state: 2, sprite: { filename: "test.png" },
		isAnimated: () => true, stepAnimation() {}, getXFrameIndex: () => 1 };
	quick.updateSlotVisual(slot, item);
	assert.equal(slot.style.backgroundPosition, "-28px -60px, center center");
	assert.equal(slot.style.backgroundSize, "auto, contain");
	item.isAnimated = () => false;
	atlas = { dataUrl: "atlas.png", positions: new Map([["food/test/2", { x: 64, y: 32 }]]) };
	quick.updateSlotVisual(slot, item);
	assert.equal(slot.style.backgroundPosition, "-60px -28px, center center");
	assert.equal(slot.style.backgroundSize, "auto, contain");
	quick.setEmptySlotVisual(slot);
	assert.equal(slot.style.backgroundImage, "none, url(/gui/panel/empty_btn.png)");
	assert.equal(slot.style.backgroundPosition, "4px 4px, center center");
	const rules = [...mainCSS.matchAll(/(?:^|\})\s*\.quick-slot\s*\{([^}]+)\}/g)];
	assert.equal(rules.length, 1, "Touch devices do not shrink the 40px slot");
	assert.match(rules[0][1], /box-sizing:\s*border-box/);
	assert.match(rules[0][1], /width:\s*40px/);
	assert.match(rules[0][1], /height:\s*40px/);
	assert.match(rules[0][1], /padding:\s*4px/);
	assert.match(rules[0][1], /background-clip:\s*content-box, border-box/);
});

function createHarness() {
	const metrics = {
		queries: 0, itemLookups: 0, quantityWrites: 0, imageWrites: 0,
		positionWrites: 0, tooltipUpdates: 0, tooltipRemovals: 0, steps: 0
	};
	let now = 1000;
	class Clock extends Date {
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
			this.classes = new Set();
			this.classList = {
				add: (...values) => values.forEach(value => this.classes.add(value)),
				remove: (...values) => values.forEach(value => this.classes.delete(value)),
				contains: value => this.classes.has(value)
			};
			this.style = new Proxy({}, {
				set: (style, key, value) => {
					if (key === "backgroundImage") { metrics.imageWrites++; }
					if (key === "backgroundPosition") { metrics.positionWrites++; }
					style[key] = value;
					return true;
				}
			});
		}
		set textContent(value) { metrics.quantityWrites++; this.text = value; }
		get textContent() { return this.text || ""; }
		set title(value) { this.attributes.set("title", value); }
		get title() { return this.attributes.get("title") || ""; }
		setAttribute(key, value) { this.attributes.set(key, value); }
		removeAttribute(key) { this.attributes.delete(key); }
		addEventListener(type, listener) {
			const listeners = this.listeners.get(type) || [];
			listeners.push(listener);
			this.listeners.set(type, listeners);
		}
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
	const document = { visibilityState: "visible" };
	const marauroa = { me: undefined };
	const stendhal = { config: { getBoolean: () => false } };
	const images = new Map();
	const spriteStore = {
		get: filename => images.get(filename) || { width: 0, height: 0 },
		checkPath: filename => images.get(filename)?.src || filename
	};
	const singletons = { getSpriteStore: () => spriteStore };
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
			exports, Date: Clock, document, console,
			HTMLElement: Element, IntersectionObserver: VisibilityObserver,
			getComputedStyle: element => ({ visibility: element.visible ? "visible" : "hidden" }),
			require: name => {
				assert.ok(Object.hasOwn(dependencies, name), `Unexpected dependency: ${name}`);
				return dependencies[name];
			}
		}, { filename: name.replace(/\.ts$/, ".js") });
		return exports;
	}
	const { ItemRarity } = load("data/ItemRarity.ts", {});
	class Entity {
		set(key, value) { this[key] = value; }
	}
	const { Item } = load("entity/Item.ts", {
		"./ItemMap": { ItemMap: { getCursor: () => "itemdrop" } },
		"./Entity": { Entity }, "../sprite/TextSprite": { TextSprite: class {} },
		"../sprite/ItemRarityEffects": rarityEffects,
		"../data/Paths": { Paths: { sprites: "/sprites" } },
		"../data/ItemRarity": { ItemRarity }, "../SingletonRepo": { singletons },
		marauroa: { marauroa }
	});
	const { ItemContainerImplementation: Container } = load("ui/component/ItemContainerImplementation.ts", {
		marauroa: { marauroa }, "../../stendhal": { stendhal }, "../UI": { ui: {} },
		"../dialog/ActionContextMenu": {}, "../dialog/DropQuantitySelectorDialog": {},
		"../dialog/ItemUpgradeDialog": {}, "../../entity/Item": { Item },
		"../../SingletonRepo": { singletons }, "../../util/Point": {},
		"../../data/Paths": { Paths: { sprites: "/sprites", gui: "/gui" } },
		"../../data/ItemRarity": { ItemRarity }, "./ItemTooltipPresentation": {},
		"../../sprite/ItemRarityEffects": rarityEffects
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
		metrics, document, marauroa, images, observers, Container, ItemRarity, raritySlotCalls, groundCalls,
		makeItem, makeOwner, makeParent, makeContainer, resetMetrics,
		setNow: value => { now = value; }
	};
}

test("the actual CSS crops every frame and state to exactly one 32 px icon", () => {
	const { border, content } = itemSlotGeometry();
	assert.deepEqual(content, { x: 4, y: 4, width: 32, height: 32 });
	for (let state = 0; state < 3; state++) {
		const h = createHarness();
		const item = h.makeItem(1, { width: 96, height: 96, state });
		const { container, parent } = h.makeContainer([item]);
		const element = parent.elements[0];
		assert.equal(element.style.backgroundImage, `url(${item.sprite.filename}), none`);
		for (let frame = 0; frame < 3; frame++) {
			container.animate(1000 + frame * 100);
			assert.equal(item.getXFrameIndex(), frame);
			const position = element.style.backgroundPosition.match(/^(-?\d+)px (-?\d+)px$/);
			assert.ok(position, "The renderer keeps the unscaled source-sheet offset");
			// Default background-origin is padding-box. Translate the actual clip
			// rectangle into sheet coordinates; neither adjacent column nor row fits.
			const sheetX = content.x - (border + Number(position[1]));
			const sheetY = content.y - (border + Number(position[2]));
			assert.deepEqual([sheetX, sheetY, sheetX + content.width, sheetY + content.height],
				[frame * 32, state * 32, (frame + 1) * 32, (state + 1) * 32]);
		}
	}
});

test("rarity outline stays centered around the icon inside the 40px slot", () => {
	const { border, content } = itemSlotGeometry();
	const rule = mainCSS.match(/\.item-rarity-outline\s*\{([^}]+)\}/)[1];
	const px = property => Number(rule.match(new RegExp(`\\b${property}:\\s*(\\d+)px`))[1]);
	assert.equal(border + px("left"), content.x - 1);
	assert.equal(border + px("top"), content.y - 1);
	assert.equal(px("width"), content.width + 2);
	assert.equal(px("height"), content.height + 2);
});

test("empty and default slots keep a second layer for padding and border fill", () => {
	const { content, borderBox } = itemSlotGeometry();
	assert.deepEqual(borderBox, { x: 0, y: 0, width: 40, height: 40 });
	assert.ok(content.x > borderBox.x && content.y > borderBox.y);
	assert.ok(content.x + content.width < borderBox.width && content.y + content.height < borderBox.height,
		"The bottom-layer clip includes the padding and transparent border, not just the icon");
	for (const defaultImage of [undefined, "slot-head.png"]) {
		const h = createHarness();
		const { container, parent } = h.makeContainer([], { size: 1, defaultImage });
		assert.equal(parent.elements[0].style.backgroundImage,
			defaultImage ? "url(/gui/slot-head.png), none" : "none, none");
		assert.equal(parent.elements[0].style.backgroundPosition, "2px 2px");
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
	assert.equal(parent.elements[0].style.backgroundPosition, "-30px 2px");
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
	assert.equal(element.style.backgroundPosition, "2px -62px");
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
	assert.equal(parent.elements[1].style.backgroundImage, "none, none");
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
	const oldPositions = oldElements.map(element => element.style.backgroundPosition);
	container.animate(1000);
	container.animate(1100);
	assert.deepEqual(oldElements.map(element => element.style.backgroundPosition), oldPositions);
	assert.equal(parent.elements[0].style.backgroundPosition, "-30px 2px");
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
	assert.equal(parent.elements[0].style.backgroundPosition, "-30px 2px");
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
	assert.equal(parent.elements[0].style.backgroundImage, "url(/sprites/failsafe.png), none");
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
	assert.equal(parent.elements[0].style.backgroundPosition, "-30px -94px");
	owner.bag.items = [];
	container.update();
	assert.equal(parent.elements[0].style.backgroundPosition, "2px 2px");
	assert.equal(parent.elements[0].style.backgroundImage, "url(/gui/slot-head.png), none");
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
