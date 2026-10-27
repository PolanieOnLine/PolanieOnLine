"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");
const root = path.resolve(__dirname, "../../../..");

async function harness() {
	const compile = file => ts.transpileModule(readFileSync(path.join(root, "src/js/stendhal", file), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
	const languageExports = {};
	vm.runInNewContext(compile("util/GameLanguage.ts"), {exports: languageExports, console,
		fetch: async url => ({ok: true, text: async () => readFileSync(path.join(root, "data/languages", path.basename(url)), "utf8")})});
	const language = languageExports.GameLanguage;
	await language.load("/data");
	const chatExports = {};
	vm.runInNewContext(compile("util/Chat.ts"), {exports: chatExports, require: () => ({})});
	class Element {
		constructor(tag) { this.tag = tag; this.children = []; this.classList = {add() {}}; }
		appendChild(child) { this.children.push(child); }
	}
	class DialogContentComponent { constructor() { this.componentElement = new Element("div"); } }
	let frame;
	const exports = {};
	const dependencies = {
		marauroa: {RPEvent: class {}},
		"../ui/UI": {ui: {createSingletonFloatingWindow(title, content) { frame = {title, content}; return frame; }}},
		"../ui/toolkit/DialogContentComponent": {DialogContentComponent},
		"../util/Chat": chatExports,
		"../util/GameLanguage": {GameLanguage: language},
		"../data/Paths": {Paths: {sprites: "/sprites"}},
		"../data/ItemRarity": {ItemRarity: {fromId: () => undefined}},
		"../SingletonRepo": {singletons: {getSpriteStore: () => ({get: () => new Element("img")})}},
		"../stendhal": {stendhal: {ui: {globalInternalWindow: {set() {}}}}}
	};
	vm.runInNewContext(compile("event/ShowItemListEvent.ts"), {exports,
		document: {createElement: tag => new Element(tag)}, require: name => {
			assert.ok(dependencies[name], name); return dependencies[name];
		}});
	return {language, show(item) {
		const event = Object.assign(new exports.ShowItemListEvent(), {
			title: "Menu Tawerny Semos", caption: "Możesz kupić te rzeczy pytając Margaret.",
			content: {0: {a: item}}
		});
		event.execute({});
		const table = frame.content.componentElement.children[0];
		return {title: frame.title, caption: table.children[0].textContent,
			headers: table.children[1].children[0].children.map(cell => cell.textContent),
			price: table.children[2].children[0].children[1].textContent,
			description: table.children[2].children[0].children[2].innerHTML};
	}};
}

function beer() {
	return {name: "sok z chmielu", class: "drink", subclass: "beer", price: "-7",
		description_info: "Oto sok z chmielu. Najlepszy na upalne dni. Parametry (PZ: 6 CYKL: 5s REGEN: 1)."};
}

test("English shop translates its visible content and underlines the item while preserving canonical data", async () => {
	const h = await harness(); h.language.synchronize("en");
	const item = beer(), before = {...item}, view = h.show(item);
	assert.equal(view.title, "Semos Tavern Menu");
	assert.equal(view.caption, "You can buy these items by asking Margaret.");
	assert.deepEqual(Array.from(view.headers), ["Item", "Price", "Description"]);
	assert.match(view.description, /<span class="logi">beer<\/span>/);
	assert.match(view.description, /Perfect for hot days\./);
	assert.match(view.description, /Stats \(HP: 6 INTERVAL: 5s REGEN: 1\)\./);
	assert.equal(view.price, "7");
	assert.deepEqual(item, before);
});

test("Polish shop retains description and safely underlines a name containing markup", async () => {
	const h = await harness();
	const item = {...beer(), name: "napój <lager>", description_info: "Oto napój <lager>. Parametry (PZ: 6)."};
	const view = h.show(item);
	assert.equal(view.title, "Menu Tawerny Semos");
	assert.deepEqual(Array.from(view.headers), ["Przedmiot", "Cena", "Opis"]);
	assert.match(view.description, /<span class="logi">napój &lt;lager[^<]*<\/span>/);
	assert.doesNotMatch(view.description, /<lager>/);
	assert.match(view.description, /Parametry \(PZ: 6\)/);
});

test("existing item markers keep a single underline", async () => {
	const h = await harness();
	const view = h.show({...beer(), description_info: "Oto §'sok z chmielu'."});
	assert.equal(view.description, 'Oto <span class="logi">sok z chmielu</span>.');
});

test("English catalogue translates the server's already marked item description", async () => {
	const h = await harness(); h.language.synchronize("en");
	const item = beer();
	item.description_info = item.description_info.replace("sok z chmielu", "§'sok z chmielu'");
	const view = h.show(item);
	assert.match(view.description, /You see <span class="logi">beer<\/span>\. Perfect for hot days\./);
});

test("English equipment and partial consumable statistics preserve numbers and punctuation", async () => {
	const h = await harness(); h.language.synchronize("en");
	for (const [source, expected] of [
		["ATK: 12 OBR: 34 STR: 5 ZASIĘG: 6", "ATK: 12 DEF: 34 RATK: 5 RANGE: 6"],
		["PZ: 6", "HP: 6"], ["PZ: 6 CYKL: 5s", "HP: 6 INTERVAL: 5s"],
		["OBR: 34 CUSTOM-OBR: 12 CUSTOM_OBR: 9 opis: 7", "DEF: 34 CUSTOM-OBR: 12 CUSTOM_OBR: 9 opis: 7"]
	]) {
		const item = {...beer(), description_info: `CUSTOM-OBR: 90. Parametry (${source}).`};
		assert.equal(h.show(item).description, `CUSTOM-OBR: 90. Stats (${expected}).`);
	}
});
