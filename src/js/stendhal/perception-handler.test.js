const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");
const runtime = require("marauroa");

const compiled = ts.transpileModule(readFileSync(path.join(__dirname, "PerceptionHandler.ts"), "utf8"), {
	compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
}).outputText;
const exported = {};
vm.runInNewContext(compiled, { exports: exported, require, console });

function harness(nested = false) {
	const { marauroa, RPObject, RPZone, PerceptionListener } = runtime;
	marauroa.currentZone = new RPZone();
	marauroa.perceptionListener = new PerceptionListener();
	marauroa.perceptionHandler = new exported.StendhalPerceptionHandler();
	marauroa.rpobjectFactory.register("player", RPObject);
	marauroa.rpobjectFactory.register("item", RPObject);
	const item = id => ({ c: "item", a: { id, name: "test sword", class: "sword", subclass: "test", rarity_id: "legendary", state: 1 },
		m: { tooltip_stats: { a: { category: "weapon", atk: "12", legendary_deep_wounds: "1" } } } });
	const inventory = nested ? [{ c: "item", a: { id: 10, name: "bag" }, s: { content: [item(2), item(3), item(4)] } }]
		: [item(2), item(3), item(4)];
	marauroa.perceptionHandler.apply({ sync: false, s: 1, aM: { c: "player", a: { id: 1 }, s: { bag: inventory } } });
	const owner = marauroa.currentZone[1];
	const slot = nested ? owner.bag.get(10).content : owner.bag;
	const original = slot.get(3);
	const wrap = change => ({ c: "player", a: { id: 1 }, s: { bag: nested
		? [{ c: "item", a: { id: 10 }, s: { content: [change] } }] : [change] } });
	return { marauroa, owner, slot, original, wrap };
}

for (const nested of [false, true]) {
	test(`private map deletions preserve item identity and order in ${nested ? "nested container" : "bag"}`, () => {
		const h = harness(nested);
		const removed = { c: "item", a: { id: 3 }, m: { tooltip_stats: { a: { legendary_deep_wounds: "1" } } } };
		const added = { c: "item", a: { id: 3 }, m: { tooltip_stats: { a: { legendary_cleave: "1" } } } };
		h.marauroa.perceptionHandler.apply({ sync: false, s: 2, dM: h.wrap(removed), aM: h.wrap(added) });
		assert.equal(h.slot.get(3), h.original);
		assert.deepEqual([0, 1, 2].map(index => h.slot.getByIndex(index).id), [2, 3, 4]);
		assert.equal(h.original.name, "test sword");
		assert.equal(h.original.class, "sword");
		assert.equal(h.original.subclass, "test");
		assert.equal(h.original.rarity_id, "legendary");
		assert.equal(h.original.tooltip_stats.atk, "12");
		assert.equal(h.original.tooltip_stats.legendary_deep_wounds, undefined);
		assert.equal(h.original.tooltip_stats.legendary_cleave, "1");
	});
}

test("partial scalar removals keep the item and its remaining fields", () => {
	const h = harness();
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2, dM: h.wrap({ c: "item", a: { id: 3, state: 1 } }) });
	assert.equal(h.slot.get(3), h.original);
	assert.equal(h.original.state, undefined);
	assert.equal(h.original.name, "test sword");
});

test("actual id-only deletions remove the item and preserve its neighbours", () => {
	const h = harness(true);
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2, dM: h.wrap({ c: "item", a: { id: 3 } }) });
	assert.equal(h.slot.get(3), undefined);
	assert.deepEqual([0, 1].map(index => h.slot.getByIndex(index).id), [2, 4]);
});

test("whole-map deletion clears the map without removing the carried item", () => {
	const h = harness();
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2,
		dM: h.wrap({ c: "item", a: { id: 3 }, m: { tooltip_stats: { a: {} } } }) });
	assert.equal(h.slot.get(3), h.original);
	assert.equal(h.original.tooltip_stats, undefined);
	assert.equal(h.original.name, "test sword");
});

test("server map identity metadata distinguishes entry removal from whole-map removal", () => {
	const h = harness();
	// Java RPObject.writeToJson emits id/zoneid even for empty map deltas.
	// RPObject.put(map, key, value) reserves both names, so they cannot be authored entries.
	const entryRemoval = JSON.parse('{"tooltip_stats":{"c":"","a":{"zoneid":"","id":"-1","legendary_deep_wounds":"0"}}}');
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2,
		dM: h.wrap({ c: "item", a: { id: 3 }, m: entryRemoval }) });
	assert.equal(h.slot.get(3), h.original);
	assert.equal(h.original.tooltip_stats.legendary_deep_wounds, undefined);
	assert.equal(h.original.tooltip_stats.category, "weapon");
	assert.equal(h.original.tooltip_stats.atk, "12");
	const mapRemoval = JSON.parse('{"tooltip_stats":{"c":"","a":{"zoneid":"","id":"-1"}}}');
	h.marauroa.perceptionHandler.apply({ sync: false, s: 3,
		dM: h.wrap({ c: "item", a: { id: 3 }, m: mapRemoval }) });
	assert.equal(h.slot.get(3), h.original);
	assert.equal(h.original.tooltip_stats, undefined);
	assert.equal(h.original.name, "test sword");
});

test("public modifications use the same map-safe receiver", () => {
	const h = harness();
	h.marauroa.perceptionHandler.apply({ sync: false, s: 2, dA: [h.wrap({ c: "item", a: { id: 3 },
		m: { tooltip_stats: { a: { legendary_deep_wounds: "1" } } } })] });
	assert.equal(h.slot.get(3), h.original);
	assert.equal(h.original.tooltip_stats.legendary_deep_wounds, undefined);
});
