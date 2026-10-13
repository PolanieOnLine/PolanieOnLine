"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");
const repo = path.resolve(__dirname, "../../../..");
const compile = name => ts.transpileModule(readFileSync(path.join(__dirname, name), "utf8"), {
	compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;

function harness(document) {
	const exports = {};
	const fetched = [];
	vm.runInNewContext(compile("GameLanguage.ts"), {exports, document, NodeFilter: {SHOW_TEXT: 4},
		console, fetch: async url => {
			fetched.push(url);
			return {ok: true, text: async () => readFileSync(path.join(repo, "data/languages", path.basename(url)), "utf8")};
		}});
	return {language: exports.GameLanguage, fetched};
}

test("reads the bundled PL/EN dictionaries and preserves missing text", async () => {
	const {language, fetched} = harness();
	await language.load("/data");
	assert.equal(language.getText("Rarity"), "Rzadkość");
	language.synchronize("en");
	assert.equal(language.getText("Zamknij"), "Close");
	assert.equal(language.getText("Brak tłumaczenia"), "Brak tłumaczenia");
	language.synchronize("es");
	assert.equal(language.getLanguage(), "pl");
	await language.load("/data");
	assert.equal(fetched.length, 2, "Changing language never calls a translation service");
});

test("parser keeps equals signs, Polish accents and skips empty translations", () => {
	const {language} = harness();
	const parsed = language.parse("\uFEFF# comment\r\n  # comment\nźródło=value=extra\nmissing=   \nwrong\n");
	assert.equal(parsed.get("źródło"), "value=extra");
	assert.equal(parsed.has("missing"), false);
	assert.equal(parsed.size, 1);
});

test("switching a settings label twice does not change an input or its saved value", async () => {
	const parent = {closest: () => null};
	const excluded = {closest: () => ({})};
	const label = {textContent: "  Zamknij  ", parentElement: parent};
	const input = {textContent: "Zamknij", parentElement: excluded};
	const selector = {value: "pl"};
	const attributes = new Map([["title", "Zamknij"]]);
	const tooltip = {getAttribute: key => attributes.get(key) ?? null, setAttribute: (key, value) => attributes.set(key, value)};
	const caption = {textContent: "Ustawienia"};
	const root = {querySelector: () => selector, querySelectorAll: () => [tooltip],
		closest: () => ({parentElement: {querySelector: () => caption}})};
	const document = {querySelectorAll: () => [root], createTreeWalker: () => {
		let index = 0; const nodes = [label, input];
		return {nextNode: () => nodes[index++] || null};
	}};
	const {language} = harness(document);
	await language.load("/data");
	language.synchronize("en");
	assert.equal(label.textContent, "  Close  ");
	assert.equal(input.textContent, "Zamknij");
	assert.equal(selector.value, "en");
	assert.equal(attributes.get("title"), "Close");
	assert.equal(caption.textContent, "Settings");
	language.synchronize("pl");
	assert.equal(label.textContent, "  Zamknij  ");
	assert.equal(attributes.get("title"), "Zamknij");
	assert.equal(caption.textContent, "Ustawienia");
});

test("the same NPC event is displayed per viewer while player chat is untouched", async () => {
	const {language} = harness();
	await language.load("/data");
	const exports = {};
	vm.runInNewContext(compile("../event/TextEvent.ts"), {exports, require: name => {
		if (name === "marauroa") return {RPEvent: class {}};
		if (name === "util/GameLanguage") return {GameLanguage: language};
		throw new Error(name);
	}});
	const event = new exports.TextEvent();
	event.text = "Witaj.";
	const replies = [];
	const npc = {type: "npc", say: text => replies.push(text)};
	event.execute(npc);
	language.synchronize("en");
	event.execute(npc);
	assert.deepEqual(replies, ["Witaj.", "Hello."]);
	assert.equal(event.text, "Witaj.");
	event.execute({type: "player", say: text => assert.equal(text, "Witaj.")});
});
