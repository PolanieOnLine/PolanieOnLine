"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

test("desktop retains its Menu button with either style while touch floating mode uses quick menu", () => {
    let touch = false, style = "floating", quickVisible;
    const panel = {style: {}}, classes = {toggle() {}, remove() {}};
    const store = {getState: () => ({leftPanelExpanded: true, rightPanelExpanded: true}),
        resetLeftPanelExpandedForDesktop() {}, refreshLayout() {}};
    const dependencies = {
        "../stendhal": {stendhal: {ui: {getMenuStyle: () => style}}},
        "./quickmenu/QuickMenu": {QuickMenu: {setVisible: value => { quickVisible = value; }}},
        "../SingletonRepo": {singletons: {getSessionManager: () => ({touchOnly: () => touch}),
            getLeftPanelToggleController: () => ({update() {}}), getRightPanelToggleController: () => ({update() {}})}},
        "./mobile/UiStateStore": {UiStateStore: {get: () => store}}
    };
    const exports = {};
    const document = {body: {classList: classes}, getElementById: id => id === "menupanel" ? panel : {classList: classes}};
    vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, "UI.ts"), "utf8"), {
        compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
    }).outputText, {exports, document, require: name => dependencies[name] || {}});
    const ui = exports.ui;
    ui.onMenuUpdate(); assert.notEqual(panel.style.display, "none"); assert.equal(quickVisible, true);
    touch = true; ui.onMenuUpdate(); assert.equal(panel.style.display, "none"); assert.equal(quickVisible, true);
    touch = false; ui.onMenuUpdate(); assert.notEqual(panel.style.display, "none");
    style = "traditional"; ui.onMenuUpdate(); assert.notEqual(panel.style.display, "none"); assert.equal(quickVisible, false);
    touch = true; ui.onMenuUpdate(); assert.notEqual(panel.style.display, "none");
});
