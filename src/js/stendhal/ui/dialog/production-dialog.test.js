"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

function harness() {
    const actions = [], frames = [], timers = new Map(); let timer = 0;
    class Element {
        constructor() {
            this.children = []; this.className = ""; this.value = "1"; this.textContent = "";
            this.style = {setProperty(key, value) { this[key] = value; }};
            this.classList = {add() {}, toggle() {}};
        }
        append(...children) { children.forEach(child => this.appendChild(child)); }
        appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
        replaceChildren(...children) { this.children = []; this.append(...children); }
        addEventListener(name, handler) { this["on" + name] = handler; }
        setAttribute(key, value) { this[key] = value; }
    }
    class DialogContentComponent {
        constructor() { this.componentElement = new Element(); }
        setFrame(frame) { this.frame = frame; }
        close() { this.frame.close(); }
        addButton(label, onclick) { const button = new Element(); button.textContent = label; button.onclick = onclick; return button; }
    }
    class FloatingWindow {
        constructor(title, content) { this.content = content; frames.push(this); }
        setId() {}
        close() { this.content.onParentClose(); }
    }
    const marauroa = {me: {}, currentZoneName: "krakow", clientFramework: {sendAction: action => actions.push(action)}};
    const document = {createElement: () => new Element()};
    const dependencies = {
        marauroa: {marauroa}, "../../data/Paths": {Paths: {sprites: "/data/sprites", gui: "/data/gui"}},
        "../../SingletonRepo": {singletons: {getSpriteStore: () => ({checkPath: value => value, get: () => ({width: 64})})}},
        "../../util/GameLanguage": {GameLanguage: {getText: value => value}},
        "../toolkit/DialogContentComponent": {DialogContentComponent}, "../toolkit/FloatingWindow": {FloatingWindow}
    };
    function load(filename) {
        const exports = {};
        vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
            compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
        }).outputText, {exports, document, Date, Math, Number,
            setInterval: fn => { timers.set(++timer, fn); return timer; }, clearInterval: id => timers.delete(id),
            setTimeout: fn => { timers.set(++timer, fn); return timer; }, clearTimeout: id => timers.delete(id),
            require: name => { assert.ok(dependencies[name], name); return dependencies[name]; }});
        return exports;
    }
    dependencies["../component/ItemSlotPresentation"] = load("../component/ItemSlotPresentation.ts");
    const Dialog = load("ProductionDialog.ts").ProductionDialog;
    function worksiteEntity() {
        class Entity {
            set(key, value) { this[key === "name" ? "_name" : key] = value; }
            getDefaultAction() { return {type: "use", target: "#" + this.id}; }
        }
        dependencies["./Entity"] = {Entity};
        dependencies["../stendhal"] = {stendhal: {}};
        dependencies["../data/Paths"] = dependencies["../../data/Paths"];
        dependencies["../ui/dialog/ItemUpgradeDialog"] = {ItemUpgradeDialog: {glyphOpenAction: () => ({type: "glyph"})}};
        dependencies["../ui/dialog/ProductionDialog"] = {ProductionDialog: Dialog};
        return new (load("../../entity/UseableEntity.ts").UseableEntity)();
    }
    return {Dialog, actions, frames, timers, marauroa, worksiteEntity};
}

function snapshot(overrides = {}) {
    return Object.assign({phase: "open", window_id: "window-1", request_id: "", npc_id: 7, npc_name: "Smolisław",
        name: "węgiel drzewny", class: "resource", subclass: "charcoal", amount: 10, max_amount: 1000,
        duration: 300, can_start: 1, status: "ok", message: "", order_state: "idle", order_amount: 0, remaining: 0,
        order_duration: 0, material_names: ["polano", "monety"], material_classes: ["resource", "money"],
        material_subclasses: ["wood", "money"], material_values: ["20", "50"], owned_material_values: ["50", "100"]}, overrides);
}

test("clicking the charcoal mound selects production while other usable objects keep their action", () => {
    const h = harness(); const entity = h.worksiteEntity(); entity.id = 24;
    entity.set("class", "furniture"); entity.set("name", "charcoal_mound"); entity.set("width", 4); entity.set("height", 3);
    const action = entity.getDefaultAction();
    assert.equal(action.type, "production_action"); assert.equal(action.npc_id, "24");
    assert.ok(action.request_id); assert.match(entity.sprite.filename, /furniture\/charcoal_mound.png$/);
    assert.equal(entity.sprite.width, 128); assert.equal(entity.sprite.height, 96);
    entity.set("name", "production_table_cooking"); entity.set("width", 2); entity.set("height", 1);
    assert.equal(entity.getDefaultAction().type, "production_action");
    assert.equal(entity.sprite.width, 64); assert.equal(entity.sprite.height, 32);
    assert.match(entity.sprite.filename, /furniture\/production_table_cooking.png$/);
    entity.set("name", "switch"); assert.equal(entity.getDefaultAction().type, "use");
});

test("product and materials use the shared slot texture and quantity index", () => {
    const h = harness(); h.Dialog.show(snapshot()); const dialog = h.frames[0].content;
    assert.match(dialog.product.style["--item-slot-texture"], /gui\/slot.png/);
    assert.equal(dialog.product.children[1].textContent, "10");
    assert.equal(dialog.materials.children.length, 2);
    assert.equal(dialog.materials.children[0].children[0].children[1].textContent, "20");
    assert.equal(dialog.start.disabled, false);
});

test("closing cancels timers and delayed events cannot reopen the window", () => {
    const h = harness(); const data = snapshot(); h.Dialog.show(data); h.frames[0].close();
    assert.equal(h.timers.size, 0);
    h.Dialog.show(data); h.Dialog.show(snapshot({phase: "update"}));
    assert.equal(h.frames.length, 1);
    assert.equal(h.actions.at(-1).command, "close");
});

test("older quantity responses cannot reset the current selection", () => {
    const h = harness(); h.Dialog.show(snapshot()); const dialog = h.frames[0].content;
    dialog.amount.value = "100"; dialog.amount.onchange();
    dialog.send("refresh"); const request = h.actions.at(-1);
    h.Dialog.show(snapshot({phase: "update", request_id: "old", amount: 10}));
    assert.equal(dialog.amount.value, "100");
    h.Dialog.show(snapshot({phase: "update", request_id: request.request_id, amount: 100, duration: 3000,
        material_values: ["200", "500"], can_start: 0}));
    assert.equal(dialog.amount.value, "100"); assert.equal(dialog.start.disabled, true);
});

test("an in-flight refresh cannot replace a quantity entered during the debounce", () => {
    const h = harness(); h.Dialog.show(snapshot()); const dialog = h.frames[0].content;
    dialog.send("refresh"); const request = h.actions.at(-1);
    dialog.amount.value = "100"; dialog.amount.onchange();
    h.Dialog.show(snapshot({phase: "update", request_id: request.request_id, amount: 10}));
    assert.equal(dialog.amount.value, "100"); assert.equal(dialog.start.disabled, true);
});

test("an opening response from the previous map is discarded before a dialog exists", () => {
    const h = harness(); const request = h.Dialog.openAction(7);
    h.marauroa.currentZoneName = "zakopane"; h.Dialog.onZoneChanged();
    h.Dialog.show(snapshot({request_id: request.request_id}));
    assert.equal(h.frames.length, 0);
});

test("a persisted ready order has an enabled collection action, leaving the map closes it", () => {
    const h = harness(); h.Dialog.show(snapshot({order_state: "ready", order_amount: 100, order_duration: 3000, can_start: 0}));
    const dialog = h.frames[0].content; assert.equal(dialog.collect.disabled, false);
    dialog.collect.onclick(); assert.equal(h.actions.at(-1).command, "collect");
    h.marauroa.currentZoneName = "zakopane"; h.Dialog.onZoneChanged();
    assert.equal(h.actions.at(-1).command, "close"); assert.equal(h.timers.size, 0);
});

test("recipe selection invalidates old previews and keeps catalogue options stable on refresh", () => {
    const h = harness();
    const catalog = {recipe_ids: ["iron", "gold"], recipe_names: ["sztabka żelaza", "sztabka złota"], selected_recipe: "iron"};
    h.Dialog.show(snapshot(catalog)); const dialog = h.frames[0].content;
    const option = dialog.recipes.children[0];
    dialog.send("refresh"); const oldRequest = h.actions.at(-1);
    dialog.recipes.value = "gold"; dialog.recipes.onchange(); const selected = h.actions.at(-1);
    assert.equal(selected.recipe, "gold"); assert.equal(dialog.start.disabled, true);
    h.Dialog.show(snapshot({...catalog, phase: "update", request_id: oldRequest.request_id}));
    assert.equal(dialog.recipes.value, "gold");
    h.Dialog.show(snapshot({...catalog, phase: "update", request_id: selected.request_id, selected_recipe: "gold", name: "sztabka złota"}));
    assert.equal(dialog.recipes.children[0], option);
    assert.equal(dialog.name.textContent, "sztabka złota");
    dialog.start.onclick(); assert.equal(h.actions.at(-1).recipe, "gold");
    assert.equal(dialog.recipes.disabled, true);
});

test("batched quantities show actual ingredient costs and the persisted order's product", () => {
    const h = harness(); h.Dialog.show(snapshot({name: "strzała", amount: 5, amount_step: 5, duration: 30,
        material_values: ["2", "1"], order_state: "working", order_name: "strzała złota", order_amount: 15, order_duration: 120, remaining: 60}));
    const dialog = h.frames[0].content;
    assert.equal(dialog.amount.min, "5"); assert.equal(dialog.amount.step, "5");
    assert.equal(dialog.materials.children[0].children[2].textContent, "50 / 2");
    assert.equal(dialog.orderLabel.textContent, "strzała złota: 15");
    dialog.amount.value = "10"; dialog.amount.onchange();
    assert.equal(dialog.materials.children[0].children[2].textContent, "50 / 4");
    assert.equal(dialog.duration.textContent, "Czas produkcji: 1 min 0 s");
});
