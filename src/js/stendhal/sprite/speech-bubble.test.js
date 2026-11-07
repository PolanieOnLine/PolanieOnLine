"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function harness() {
	const view = {offsetX: 0, offsetY: 0, width: 320, height: 240, targetTileHeight: 32, textSprites: []};
	view.getWorldViewportSize = () => ({width: view.worldWidth ?? view.width, height: view.worldHeight ?? view.height});
	let removed;
	view.removeTextBubble = sprite => {removed = sprite;};
	const stendhal = {ui: {gamewindow: view, html: {extractPosition: event => event}}};
	const dependencies = {
		"../stendhal": {stendhal},
		"../data/color/Color": {Color: {WHITE: "white", BLACK: "black", CHAT_NORMALBLACK: "black"}},
		"../util/Pair": {Pair: class {constructor(first, second) {this.first = first; this.second = second;}}},
		"../util/Speech": {Speech: {drawBubbleRounded() {}}}
	};
	function load(file) {
		const exports = {};
		const source = ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
			compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
		}).outputText;
		vm.runInNewContext(source, {exports, require: name => dependencies[name] || {},
			document: {getElementById: () => ({getBoundingClientRect: () => ({x: 0, y: 0})})}});
		return exports;
	}
	const {TextBubble} = load("TextBubble.ts");
	dependencies["./TextBubble"] = {TextBubble};
	const {SpeechBubble} = load("SpeechBubble.ts");
	const ctx = {measureText: text => ({width: text.length * 7}), save() {}, restore() {}, fillText() {}};
	return {view, TextBubble, ctx, get removed() {return removed;},
		bubble(x, y, text = "hello there", height = 1) {
			const entity = {_x: x, _y: y, width: 1, height, inView: () => true};
			const bubble = new SpeechBubble(text, entity);
			view.textSprites.push(bubble);
			bubble.draw(ctx);
			return bubble;
		}};
}

test("speech body bottom follows the Java entity anchor and its tail starts at the right edge", () => {
	const h = harness();
	const bubble = h.bubble(2, 3, "hi", 2);
	assert.equal(bubble.getY() + bubble.getHeight(), 3 * 32);
	assert.equal(bubble.getX() - 8, 3 * 32);
});

test("overlapping messages from different nearby characters get separate rows", () => {
	const h = harness();
	const first = h.bubble(1, 3);
	const second = h.bubble(2, 3);
	const overlaps = first.getX() < second.getX() + second.getWidth()
		&& second.getX() < first.getX() + first.getWidth()
		&& first.getY() < second.getY() + second.getHeight()
		&& second.getY() < first.getY() + first.getHeight();
	assert.equal(overlaps, false);
});

test("stacked bubbles stay within all viewport edges", () => {
	const h = harness();
	for (let i = 0; i < 4; i++) {
		const bubble = h.bubble(9, 7.4, "message " + i);
		assert.ok(bubble.getX() - 8 >= h.view.offsetX + 1);
		assert.ok(bubble.getY() >= h.view.offsetY + 1);
		assert.ok(bubble.getX() + bubble.getWidth() <= h.view.offsetX + h.view.width - 1);
		assert.ok(bubble.getY() + bubble.getHeight() <= h.view.offsetY + h.view.height - 1);
	}
});

test("clicking the bottom of the visible speech bubble removes it without a baseline offset", () => {
	const h = harness();
	const bubble = h.bubble(2, 3);
	const event = {clientX: bubble.getX() + 5,
		clientY: bubble.getY() + bubble.getHeight() - 2, stopPropagation() {}};
	bubble.onClick(event);
	assert.equal(h.removed, bubble);
});

test("high DPI buffers do not enlarge the visible world area used to clamp bubbles", () => {
	const h = harness();
	Object.assign(h.view, {width: 640, height: 480, worldWidth: 320, worldHeight: 240, offsetX: 64, offsetY: 32});
	for (let i = 0; i < 3; i++) {
		const bubble = h.bubble(11, 8.4, "edge " + i);
		assert.ok(bubble.getX() + bubble.getWidth() <= 64 + 320 - 1);
		assert.ok(bubble.getY() + bubble.getHeight() <= 32 + 240 - 1);
	}
});

test("bubble clicks use scaled canvas coordinates rather than CSS pointer coordinates", () => {
	const h = harness();
	const bubble = h.bubble(2, 3);
	bubble.onClick({clientX: 500, clientY: 500, canvasRelativeX: bubble.getX() + 2,
		canvasRelativeY: bubble.getY() + 2, stopPropagation() {}});
	assert.equal(h.removed, bubble);
});

test("notification hit testing still uses the body drawn above its text baseline", () => {
	const h = harness();
	class Notice extends h.TextBubble {
		constructor() {super("notice"); this.x = 20; this.y = 40; this.width = 80; this.height = 20;}
		draw() {}
	}
	const notice = new Notice();
	assert.equal(notice.clipsPoint(25, 26), true);
	assert.equal(notice.clipsPoint(25, 59), false);
});
