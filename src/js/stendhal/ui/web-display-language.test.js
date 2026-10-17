"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require("typescript");
const root = path.resolve(__dirname, "../../../..");
function load(file, deps = {}, globals = {}) {
 const exports = {};
 const source = ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
  compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
 }).outputText;
 vm.runInNewContext(source, {exports, console, require: name => deps[name] || new Proxy({}, {get: () => class {}}), ...globals});
 return exports;
}
async function language(globals = {}) {
 const {GameLanguage} = load("../util/GameLanguage.ts", {}, {fetch: async url => ({ok: true,
  text: async () => readFileSync(path.join(root, "data/languages", path.basename(url)), "utf8")}), ...globals});
 await GameLanguage.load("/data"); GameLanguage.synchronize("en"); return GameLanguage;
}
class Element {
 constructor() { this.style = {}; this.dataset = {}; this.children = []; this.attrs = new Map(); this.listeners = {};
  this.classList = {add() {}, remove() {}, toggle() {}}; }
 appendChild(child) { this.children.push(child); return child; }
 append(...children) { this.children.push(...children); }
 replaceChildren() { this.children = []; }
 setAttribute(key, value) { this.attrs.set(key, value); }
 getAttribute(key) { return this.attrs.get(key) ?? null; }
 addEventListener(type, fn) { this.listeners[type] = fn; }
 closest() { return null; }
 matches() { return this.attrs.size > 0; }
 querySelectorAll() { return []; }
}
test("registered UI translates its root attributes and can restore Polish, including excluded roots", async () => {
 const document = {querySelectorAll: () => [], createTreeWalker: () => ({nextNode: () => null})};
 const g = await language({document, NodeFilter: {SHOW_TEXT: 4}});
 const root = new Element(); root.setAttribute("title", "Ten obszar wygląda na bezpieczny.");
 root.setAttribute("aria-label", "Atakuj najbliższy cel");
 g.registerUI(root);
 assert.equal(root.getAttribute("title"), "This area seems safe.");
 assert.equal(root.getAttribute("aria-label"), g.getText("Atakuj najbliższy cel"));
 g.synchronize("pl"); assert.equal(root.getAttribute("title"), "Ten obszar wygląda na bezpieczny.");
 const excluded = new Element(); excluded.setAttribute("placeholder", "Zamknij"); excluded.closest = () => excluded;
 g.synchronize("en"); g.translateUI(excluded); assert.equal(excluded.getAttribute("placeholder"), "Zamknij");
});
test("account validation alerts translate while account inputs remain exactly as entered", async () => {
 const g = await language(), alerts = [], accounts = [];
 const fields = Object.fromEntries(["input[type=submit]", "#username", "#password", "#passwordrepeat", "#email", "a"].map(key => [key, new Element()]));
 class Dialog { child(key) { return fields[key]; } }
 const {CreateAccountDialog} = load("dialog/CreateAccountDialog.ts", {
  "../../util/GameLanguage": {GameLanguage: g}, "../toolkit/DialogContentComponent": {DialogContentComponent: Dialog},
  marauroa: {marauroa: {clientFramework: {createAccount: (...args) => accounts.push(args)}}}
 }, {alert: text => alerts.push(text)});
 new CreateAccountDialog(); const submit = () => fields["input[type=submit]"].listeners.click({preventDefault() {}});
 submit(); assert.equal(alerts[0], g.getText("Proszę wypełnić wszystkie wymagane pola.")); assert.notEqual(alerts[0], "Proszę wypełnić wszystkie wymagane pola.");
 fields["#username"].value = "Zamknij"; fields["#password"].value = "Witaj."; fields["#passwordrepeat"].value = "other";
 submit(); assert.equal(alerts[1], g.getText("Hasło i powtórzenie hasła nie są takie same."));
 fields["#passwordrepeat"].value = "Witaj."; fields["#email"].value = "test@example.com"; submit();
 assert.deepEqual(accounts, [["Zamknij", "Witaj.", "test@example.com"]]);
});
test("quick-slot drop warnings translate without assigning an invalid item", async () => {
 const g = await language(), logs = [], stendhal = {ui: {heldObject: {path: "1/bag/2"}}}; let releases = 0;
 const {QuickSlots} = load("interaction/QuickSlots.ts", {"../../util/GameLanguage": {GameLanguage: g},
  "../../util/Chat": {Chat: {log: (type, text) => logs.push({type, text})}}, "../../stendhal": {stendhal},
  "../../SingletonRepo": {singletons: {getHeldObjectManager: () => ({onRelease: () => releases++})}}});
 const slots = Object.create(QuickSlots.prototype), event = {preventDefault() {}, stopPropagation() {}};
 slots.findItemByTargetPath = () => undefined; slots.onDrop(event, {});
 assert.equal(logs.at(-1).text, g.getText("Nie udało się znaleźć przedmiotu w ekwipunku."));
 stendhal.ui.heldObject = {path: "1/bag/2"}; slots.allowedClasses = new Set(["food"]);
 slots.findItemByTargetPath = () => ({class: "sword"}); slots.onDrop(event, {});
 assert.equal(logs.at(-1).text, g.getText("Ten przedmiot nie może trafić do szybkiego slotu."));
 assert.equal(logs.at(-1).type, "warning"); assert.equal(releases, 2); assert.equal(stendhal.ui.heldObject, undefined);
});
test("cauldron countdown translates each authored part before joining and preserves timing", async () => {
 const g = await language();
 const {GoldenCauldronComponent} = load("component/GoldenCauldronComponent.ts", {"../../util/GameLanguage": {GameLanguage: g}}, {Date: {now: () => 1000}, window: {clearInterval() {}}});
 const c = Object.create(GoldenCauldronComponent.prototype); c.statusLabel = {}; c.baseStatus = "Kocioł nie pracuje.";
 c.readyAt = 66000; c.refreshStatusLabel();
 assert.equal(c.statusLabel.textContent, "The cauldron is idle. Time remaining: 01:05.");
 c.readyAt = 11000; c.refreshStatusLabel(); assert.equal(c.statusLabel.textContent, "The cauldron is idle. Time remaining: 10 s.");
 g.synchronize("pl"); c.refreshStatusLabel(); assert.equal(c.statusLabel.textContent, "Kocioł nie pracuje. Pozostało 10 s.");
});
test("upgrade preview translates dynamic chance, cost and resource ownership tooltips", async () => {
 const g = await language(), document = {createElement: () => new Element()};
 class Dialog {constructor() {this.componentElement = new Element();} addButton() {return new Element();}}
 const {ItemUpgradeDialog} = load("dialog/ItemUpgradeDialog.ts", {"../../util/GameLanguage": {GameLanguage: g},
  "../toolkit/DialogContentComponent": {DialogContentComponent: Dialog},
  "../component/ItemSlotPresentation": {getItemSlotLayers: () => ({icon: {style: {}}, quantityLabel: {}})}}, {document});
 const d = new ItemUpgradeDialog();
 d.apply({status: "READY", success_percent: 75, fee_text: "250 money", material_names: ["polano"], material_values: [3], owned_material_values: [2]});
 assert.equal(d.chance.textContent, "Success chance: 75%"); assert.equal(d.fee.textContent, "Cost: 250 money");
 assert.equal(d.materials.children[0].title, "wood: owned 2, required 3");
 d.apply({status: "SELECT_ITEM"}); assert.equal(d.chance.textContent, "Success chance: —"); assert.equal(d.fee.textContent, "Cost: —");
});
test("bestiary window titles translate the unknown, rare and abnormal explanations", async () => {
 const g = await language();
 for (const [source, expected] of [
  ['Bestiariusz: "???" = niewiadome', 'Bestiary: "???" = unknown'],
  ['Bestiariusz: "rzadkie" stwory nie są wymagane do osiągnięć', 'Bestiary: "rare" creatures are not required for achievements'],
  ['Bestiariusz: "anormalne" stwory nie są wymagane do osiągnięć', 'Bestiary: "abnormal" creatures are not required for achievements'],
  ['Bestiariusz: "rzadkie" i "anormalne" stwory nie są wymagane do osiągnięć', 'Bestiary: "rare" and "abnormal" creatures are not required for achievements']
 ]) assert.equal(g.getText(source), expected);
});
test("creature hover armor labels follow the viewer language without changing server armor types", async () => {
 const g = await language();
 const renderer = load("render/CreatureArmorRenderer.ts");
 const {Creature} = load("../entity/Creature.ts", {"../util/GameLanguage": {GameLanguage: g},
  "../ui/render/CreatureArmorRenderer": renderer});
 const c = Object.create(Creature.prototype); c.armor_type = "medium";
 assert.equal(c.getArmorLabel(), "Medium armour"); assert.equal(c.armor_type, "medium");
 g.synchronize("pl"); assert.equal(c.getArmorLabel(), "Pancerz średni");
 c.armor_type = "unknown"; assert.equal(c.getArmorLabel(), undefined);
});
test("debug errors and headed weather warnings translate and keep the supplied weather path", async () => {
 const g = await language(), logs = [];
 const {DebugAction} = load("../action/DebugAction.ts", {"../util/GameLanguage": {GameLanguage: g},
  "../util/Chat": {Chat: {log() {}, logH: (type, text) => logs.push({type, text})}},
  "../stendhal": {stendhal: {config: {getBoolean: () => false}}},
  "../data/Paths": {Paths: {weather: "/weather"}},
  "../SingletonRepo": {singletons: {getSpriteStore: () => ({getCached: () => undefined})}}});
 const action = new DebugAction(); action.execute("debug", [], "");
 assert.equal(logs.at(-1).text, "Expected parameters");
 action.execute("debug", ["weather", "Zamknij"], "");
 assert.equal(logs.at(-2).text, "Weather is disabled.");
 assert.equal(logs.at(-1).text, "unknown weather: /weather/Zamknij.png");
 assert.equal(logs.at(-1).type, "error");
});
