"use strict";
const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const { test } = require("node:test");
const ts = require("typescript");

function harness(buddies = {}) {
 const logs = [];
 const marauroa = { me: { _name: "Local", buddies } };
 const stendhal = { players: [], playerInGame: true };
 const deps = {
  marauroa: { marauroa, RPEvent: class {} },
  "../stendhal": { stendhal },
  "../util/Chat": { Chat: { log: (...args) => logs.push(args) } },
  "../util/GameLanguage": { GameLanguage: { getText: text => text } }
 };
 function event(file, type, name) {
  const exports = {};
  const js = ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
   compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  vm.runInNewContext(js, { exports, require: name => deps[name] });
  const instance = new exports[type]();
  instance.name = name;
  instance.execute();
 }
 return { logs, marauroa, stendhal,
  login: name => event("PlayerLoggedOnEvent.ts", "PlayerLoggedOnEvent", name),
  logout: name => event("PlayerLoggedOutEvent.ts", "PlayerLoggedOutEvent", name) };
}

test("strangers update completion names without announcing their presence", () => {
 const h = harness();
 h.login("Stranger");
 assert.equal(h.stendhal.players.includes("Stranger"), true);
 h.logout("Stranger");
 assert.equal(h.stendhal.players.includes("Stranger"), false);
 assert.equal(h.logs.length, 0);
});

test("friends get one greeting and farewell, not duplicate presence notices", () => {
 const h = harness({ Friend: "false" });
 h.login("Friend"); h.login("Friend");
 h.logout("Friend"); h.logout("Friend");
 assert.deepEqual(h.logs, [
  ["information", "Friend zawitał do PolanieOnLine."],
  ["information", "Friend opuścił PolanieOnLine."]
 ]);
});

test("unseen hidden friends cannot be exposed by a logout event", () => {
 const h = harness({ HiddenAdmin: "false" });
 h.logout("HiddenAdmin");
 assert.equal(h.logs.length, 0);
});

test("inherited buddy properties are not friends", () => {
 const h = harness(Object.create({ Stranger: "true" }));
 h.login("Stranger"); h.logout("Stranger");
 assert.equal(h.logs.length, 0);
});

test("initial lists and the local player remain silent", () => {
 const h = harness({ Friend: "true", Local: "true" });
 h.stendhal.playerInGame = false;
 h.login("Friend"); h.logout("Friend");
 h.stendhal.playerInGame = true;
 h.login("Local"); h.logout("Local");
 assert.equal(h.logs.length, 0);
});
