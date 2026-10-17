"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");

test("minimap catalogue translates location suffixes and preserves names and signed levels", async () => {
 const exports = {};
 const source = ts.transpileModule(readFileSync(path.join(__dirname, "GameLanguage.ts"), "utf8"), {
  compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
 }).outputText;
 vm.runInNewContext(source, {exports, console, fetch: async url => ({ok: true,
  text: async () => readFileSync(path.resolve(__dirname, "../../../../data/languages", path.basename(url)), "utf8")})});
 const language = exports.GameLanguage;
 await language.load("/data"); language.synchronize("en");
 for (const [polish, english] of [
  ["Semos tavern 0, budynek", "Semos tavern 0, building"],
  ["Wieliczka salt mines W, poziom -1", "Wieliczka salt mines W, level -1"],
  ["Kikareukin clouds, poziom 7", "Kikareukin clouds, level 7"],
  ["Koscielisko NE, świat alternatywny", "Koscielisko NE, alternate world"],
  ["Plecak, budynek", "Plecak, building"]
 ]) {
  assert.equal(language.getText(polish), english);
  language.synchronize("pl"); assert.equal(language.getText(polish), polish);
  language.synchronize("en");
 }
});
