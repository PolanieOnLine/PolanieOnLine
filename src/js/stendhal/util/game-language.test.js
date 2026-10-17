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
	const loadedRequests = fetched.length;
	assert.equal(language.getText("Rarity"), "Rzadkość");
	language.synchronize("en");
	assert.equal(language.getText("Zamknij"), "Close");
	assert.equal(language.getText("Brak tłumaczenia"), "Brak tłumaczenia");
	language.synchronize("es");
	assert.equal(language.getLanguage(), "pl");
	await language.load("/data");
	assert.equal(fetched.length, loadedRequests, "Changing language does not reload catalogues");
	assert.ok(fetched.every(url => url.startsWith("/data/languages/")), "Only bundled game files are requested");
});

test("parser keeps equals signs, Polish accents and skips empty translations", () => {
	const {language} = harness();
	const parsed = language.parse("\uFEFF# comment\r\n  # comment\nźródło=value=extra\nmissing=   \nwrong\n");
	assert.equal(parsed.get("źródło"), "value=extra");
	assert.equal(parsed.has("missing"), false);
	assert.equal(parsed.size, 1);
});

test("release service and activity messages translate nested text but preserve player names and counts", async () => {
	const {language} = harness();
	await language.load("/data"); language.synchronize("en");
	assert.equal(language.getText("Gratulacje Zamknij dostałeś 12 punkt. Wygrałeś pyszną kanapkę!"),
		"Congratulations Zamknij, you scored 12 points. You won a delicious sandwich!");
	assert.equal(language.getText("Wciąż zajmuje się twoim zleceniem 3 skórzany kirys. Wróć za 2 godziny, aby odebrać."),
		"I am still working on your order for 3 leather cuirasses. Come back in 2 hours to collect it.");
	assert.equal(language.getText("Gratulacje! Zamknij wygrał grę."), "Congratulations! Zamknij won the game.");
	assert.equal(language.getText("Witaj w #'PolanieOnLine'! Odwiedź stronę - #polanieonline.eu! Pamiętaj, aby nikomu ~'nie udostępniać' hasła do swojego konta! Jeżeli znalazłeś jakieś błędy w grze to zgłoś to nam do ~'supportu'."),
		"Welcome to #'PolanieOnLine'! Visit our website - #polanieonline.eu! Remember ~'never to share' your account password with anyone! If you find any bugs in the game, please report them to ~'support'.");
});

test("Polish NPC link labels resolve to canonical keywords without changing arbitrary input", async () => {
	const {language} = harness();
	await language.load("/data");
	assert.equal(language.getKeywordLabel("memory"), "pamięć");
	assert.equal(language.getOriginalKeyword("pamięć"), "memory");
	assert.equal(language.getOriginalKeyword("nieznane hasło"), "nieznane hasło");
});

test("NPC link targets use their sentence context instead of unrelated UI or quest labels", async () => {
	const {language} = harness();
	await language.load("/data"); language.synchronize("en");
	for (const [source, caption, canonical] of [
		["Kontynuuj polowanie. Powiedz #załatwione, kiedy skończysz.", "done", "załatwione"],
		["Najpierw zakończ obecne polowanie albo powiedz #rezygnuję. Przerwane zadanie nie daje nagrody.", "cancel", "rezygnuję"],
		["Masz aktywne polowanie: wilk, 8 sztuk. Powiedz #załatwione lub #rezygnuję.", "cancel", "rezygnuję"],
		["Słowo już padło. Teraz nie mnie masz przekonywać. Zakonnik czeka na murze w północnej części zamku od strony wschodniej, by przyjąć twoją #wartę. To on ma zobaczyć, czy wytrwasz przy obowiązku.", "watch", "wartę"],
		["Mogę pokazać Tobie #mapę lub pokazać gdzie jest #bank, #biblioteka, #oberża, #świątynia, #kowal, #piekarnia, #publiczna #skrzynia lub gdzie jest stara #wioska.", "public chest", "publiczna skrzynia"],
	]) {
		const rendered = language.getText(source);
		assert.equal(language.getOriginalKeyword(caption, rendered), canonical);
		assert.equal(language.getOriginalKeyword(caption, "  " + rendered + "  "), canonical);
		assert.equal(language.getOriginalKeyword(caption, "Journal\n" + rendered + "\n"), canonical);
	}
	assert.equal(language.getOriginalKeyword("Cancel", "A player said #Cancel."), "Cancel");
});

test("full wait messages translate short numeric duration units without consuming a time list", async () => {
	const {language} = harness();
	await language.load("/data");
	language.synchronize("en");
	assert.equal(language.getText("Muzeum może Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za 3 dni."),
		"The museum can ask you to bring an item only once a week. Return in 3 days.");
	assert.equal(language.getText("Nie skończyłem jeszcze naprawy twego pierścienia życia. Powróć proszę za 2 godziny, 12 minut."),
		"I haven't finished fixing your ring of life. Please check back in 2 hours, 12 minutes.");
});

test("switching a settings label twice does not change an input or its saved value", async () => {
	const parent = {closest: () => null};
	const excluded = {closest: () => ({})};
	const label = {textContent: "  Zamknij  ", parentElement: parent};
	const input = {textContent: "Zamknij", parentElement: excluded};
	const selector = {value: "pl"};
	const attributes = new Map([["title", "Zamknij"]]);
	const tooltip = {closest: () => null, getAttribute: key => attributes.get(key) ?? null, setAttribute: (key, value) => attributes.set(key, value)};
	const caption = {textContent: "Ustawienia"};
	const button = {value: "Zamknij", closest: () => null};
	const root = {querySelector: () => selector, querySelectorAll: query => query.startsWith("[title]") ? [tooltip] : query.startsWith("input[") ? [button] : [],
		closest: selector => selector === ".windowcontent" ? ({parentElement: {querySelector: () => caption}}) : null};
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
	assert.equal(button.value, "Close");
	language.synchronize("pl");
	assert.equal(label.textContent, "  Zamknij  ");
	assert.equal(attributes.get("title"), "Zamknij");
	assert.equal(caption.textContent, "Ustawienia");
	assert.equal(button.value, "Zamknij");
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

test("achievement notifications translate title and requirement but keep the category and event data canonical", async () => {
	const {language} = harness();
	await language.load("/data");
	language.synchronize("en");
	const exports = {}, rendered = [];
	vm.runInNewContext(compile("../event/ReachedAchievementEvent.ts"), {exports, require: name => {
		if (name === "marauroa") return {RPEvent: class {}};
		if (name === "util/GameLanguage") return {GameLanguage: language};
		if (name === "stendhal") return {stendhal: {ui: {gamewindow: {addAchievementNotif: (...args) => rendered.push(args)}}}};
		throw new Error(name);
	}});
	const event = new exports.ReachedAchievementEvent();
	event.category = "quest.special.daily_item";
	event.title = "Pomocnik Ados";
	event.description = "Ukończono dzienne zadanie na przedmiot 10 razy";
	event.execute({});
	assert.deepEqual(rendered[0], ["quest.special.daily_item", "Ados Helper", "Completed the daily item quest 10 times"]);
	assert.equal(event.title, "Pomocnik Ados");
	assert.equal(event.description, "Ukończono dzienne zadanie na przedmiot 10 razy");
	language.synchronize("pl");
	event.execute({});
	assert.deepEqual(rendered[1], ["quest.special.daily_item", "Pomocnik Ados", "Ukończono dzienne zadanie na przedmiot 10 razy"]);
});
