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
        constructor(tag) {
            this.tagName = tag; this.children = []; this.dataset = {}; this.style = {};
            this.value = ""; this.textContent = ""; this.scrollTop = 0; this.naturalWidth = 32;
            this.classList = {add() {}, toggle() {}};
        }
        append(...children) { children.forEach(child => this.appendChild(child)); }
        appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
        replaceChildren(...children) { this.children = []; this.append(...children); }
        addEventListener(name, handler) { this["on" + name] = handler; }
        setAttribute(key, value) { this[key] = value; }
        removeAttribute(key) { delete this[key]; }
        querySelectorAll() {
            return this.children.flatMap(child => [child, ...child.querySelectorAll()]).filter(child => child.dataset.shopSprite);
        }
    }
    const document = {createElement: tag => new Element(tag), createTextNode: text => Object.assign(new Element("text"), {textContent:text})};
    class DialogContentComponent {
        constructor() { this.componentElement = new Element("div"); }
        setFrame(frame) { this.frame = frame; }
        addButton(label, onclick) { return Object.assign(new Element("button"), {textContent:label, onclick}); }
    }
    class FloatingWindow {
        constructor(title, content) { this.content = content; frames.push(this); }
        setId() {}
        setTitle(title) { this.title = title; }
        close() { this.content.onParentClose(); }
    }
    class Item {
        getRarity() { return this.rarity_id ? {id:this.rarity_id, polishDisplayName:this.rarity_id, colorHex:"#ff8c00"} : undefined; }
        getDisplayName() { return this.name || ""; }
    }
    const dependencies = {
        marauroa:{marauroa:{me:{},clientFramework:{sendAction: action => actions.push(action)}}},
        "../../data/Paths":{Paths:{sprites:"/data/sprites"}},
        "../../entity/Item":{Item},
        "../../SingletonRepo":{singletons:{getConfigManager:()=>({getBoolean:()=>true})}},
        "../../util/GameLanguage":{GameLanguage:{getText:text=>text,getLanguage:()=>"pl"}},
        "../toolkit/DialogContentComponent":{DialogContentComponent}, "../toolkit/FloatingWindow":{FloatingWindow}
    };
    function load(file) {
        const exports = {};
        vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname,file),"utf8"), {
            compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}
        }).outputText, {exports, document, Date:{now:()=>200}, Math, Number,
            setInterval:fn=>{timers.set(++timer,fn);return timer;},clearInterval:id=>timers.delete(id),
            require:name=>{assert.ok(dependencies[name],name);return dependencies[name];}});
        return exports;
    }
    dependencies["../component/ItemTooltipPresentation"] = load("../component/ItemTooltipPresentation.ts");
    const Dialog = load("NpcShopDialog.ts").NpcShopDialog;
    return {Dialog, actions, frames, timers};
}

function catalogue(overrides={}) {
    const snapshot = name => JSON.stringify({name,rarity_id:"rare",tooltip_stats:{category:"weapon",damage_min:"5",damage_max:"10",attack_interval_seconds:"1",attacks_per_second:"1"}});
    return {phase:"open",npc_id:7,npc_name:"Kupiec",window_id:"shop-session",owned_money_value:"1000",owned_money_text:"1000 monet",buying_penalty:"1",
        sell_names:["sztylecik"],sell_classes:["sword"],sell_subclasses:["broadsword"],sell_prices:["5 monet"],sell_price_values:["5"],sell_stackable:["1"],sell_quantities:["1000"],sell_items:[snapshot("sztylecik")],
        buy_names:["sztylecik","sztylecik"],buy_keys:["first","second"],buy_classes:["sword","sword"],buy_subclasses:["broadsword","broadsword"],buy_prices:["10 monet","20 monet"],buy_price_values:["10","20"],buy_quantities:["1","1"],buy_stackable:["0","0"],buy_items:[snapshot("sztylecik"),snapshot("sztylecik")],...overrides};
}

test("sale submits the concrete item key and locks polling during checkout",()=>{
    const h=harness();h.Dialog.show(catalogue()); const dialog=h.frames[0].content;
    dialog.sellTab.onclick();dialog.listBox.children[1].onclick();dialog.requestButton.onclick();
    assert.equal(h.actions[0].selection,"second");assert.equal(h.actions[0].window_id,"shop-session");
    assert.equal(h.actions[0].mode,"sell");assert.equal(h.actions[0].request_id,"1");
    for(const fn of h.timers.values())fn();assert.equal(h.actions.length,1);
    assert.equal(dialog.requestButton.disabled,true);
});

test("refresh preserves same-name item identity and clears it when that item disappears",()=>{
    const h=harness();h.Dialog.show(catalogue());const d=h.frames[0].content;
    d.sellTab.onclick();d.listBox.children[1].onclick();
    h.Dialog.show(catalogue({phase:"refresh",buy_keys:["second","first"],buy_price_values:["20","10"]}));
    assert.equal(d.selected.key,"second");assert.equal(d.selected.priceValue,20);
    h.Dialog.show(catalogue({phase:"result",buy_names:["sztylecik"],buy_keys:["first"],buy_items:[catalogue().buy_items[0]]}));
    assert.equal(d.selected,undefined);assert.equal(d.requestButton.disabled,true);
});

test("maximum and total match the server's final rounding of the merchant penalty",()=>{
    const h=harness();h.Dialog.show(catalogue({owned_money_value:"22",buying_penalty:"1.5"}));const d=h.frames[0].content;
    d.listBox.children[0].onclick();d.presets[3].onclick();
    assert.equal(d.quantity.value,"3");assert.equal(d.quoteTotal.textContent,"Łącznie: 22 monety");
    assert.equal(d.requestButton.disabled,false);
    d.quantity.value="4";d.quantity.oninput();assert.equal(d.requestButton.disabled,true);
});

test("closing stops both timers and a delayed event cannot reopen the same session",()=>{
    const h=harness();h.Dialog.show(catalogue());assert.equal(h.timers.size,2);
    h.frames[0].close();assert.equal(h.timers.size,0);assert.equal(h.actions[0].command,"close");
    h.Dialog.show(catalogue());assert.equal(h.frames.length,1);
    h.Dialog.show(catalogue({window_id:"new-session"}));assert.equal(h.frames.length,2);
});

test("icons use native sprite frames while the selected item's structured statistics are rendered",()=>{
    const h=harness();h.Dialog.show(catalogue());const d=h.frames[0].content;
    d.listBox.children[0].onclick();d.detailIcon.naturalWidth=96;
    for(const fn of h.timers.values())fn();
    assert.equal(d.detailIcon.style.objectPosition,"-64px 0px");
    assert.ok(d.detailStats.children.length>1);
});



test("polling waits for its previous reply without blocking selection or checkout",()=>{
    const h=harness();h.Dialog.show(catalogue());const d=h.frames[0].content;
    for(const fn of h.timers.values()){fn();fn();}
    assert.equal(h.actions.length,1);assert.equal(h.actions[0].command,"refresh");
    d.listBox.children[0].onclick();assert.equal(d.requestButton.disabled,false);
    h.Dialog.show(catalogue({phase:"refresh",request_id:"1"}));
    for(const fn of h.timers.values())fn();assert.equal(h.actions.length,2);
});


test("empty-bag buying catalogue remains browsable but cannot sell missing items",()=>{
    const h=harness();h.Dialog.show(catalogue({buy_names:["sztylecik"],buy_keys:["unavailable:sztylecik"],buy_quantities:["0"]}));
    const d=h.frames[0].content;
    assert.equal(d.sellTab.disabled,false);
    d.sellTab.onclick();d.listBox.children[0].onclick();
    assert.equal(d.detailName.textContent,"sztylecik");
    assert.equal(d.maximum(),0);
    assert.equal(d.requestButton.disabled,true);
    d.requestButton.onclick();
    assert.equal(h.actions.length,0);
});


test("item details show rarity beside the name and description before statistics",()=>{
    const h=harness();h.Dialog.show(catalogue({sell_descriptions:["Opis przedmiotu"]}));
    const d=h.frames[0].content;d.listBox.children[0].onclick();
    assert.equal(d.detailRarity.textContent,"rare");
    assert.equal(d.detailName.parentElement.children.at(-1),d.detailRarity);
    assert.equal(d.detailDescription.parentElement.children[0],d.detailDescription);
    assert.equal(d.detailDescription.parentElement.children[1],d.detailStats);
    assert.ok(!d.detailStats.children.some(row=>row.textContent==="rare"));
});
