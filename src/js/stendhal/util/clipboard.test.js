"use strict";
const assert = require("node:assert/strict");
const { test } = require("node:test");
const { readFileSync } = require("node:fs");
const vm = require("node:vm");
const path = require("node:path");
const ts = require(process.env.STENDHAL_TEST_TYPESCRIPT || "typescript");
const code = ts.transpileModule(readFileSync(path.join(__dirname, "Clipboard.ts"), "utf8"), {
	compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
}).outputText;

function harness({ api = "absent", fallback = true } = {}) {
	const metrics = { apiTexts: [], commands: [], fields: [], restores: 0, selected: false, range: [] };
	const navigator = {};
	if (api !== "absent") {
		navigator.clipboard = { writeText: async text => {
			metrics.apiTexts.push(text);
			if (api === "reject") { throw new Error("Denied"); }
		} };
	}
	const document = {
		activeElement: { focus: options => { assert.equal(options.preventScroll, true); metrics.restores++; } },
		body: { appendChild: field => metrics.fields.push(field) },
		createElement: tag => {
			assert.equal(tag, "textarea");
			return {
				style: {}, setAttribute() {}, focus() {},
				select: () => { metrics.selected = true; },
				setSelectionRange: (...args) => { metrics.range = args; },
				remove() { metrics.fields = metrics.fields.filter(field => field !== this); }
			};
		}
	};
	if (fallback !== "absent") {
		document.execCommand = command => {
			metrics.commands.push(command);
			assert.equal(metrics.fields[0].readOnly, true);
			if (fallback === "throw") { throw new Error("Denied"); }
			return fallback;
		};
	}
	const exports = {};
	vm.runInNewContext(code, { exports, navigator, document });
	return { copy: exports.copyTextToClipboard, metrics };
}

test("standard clipboard writes the entire Unicode report without selection or fallback", async () => {
	const h = harness({ api: "success" });
	const text = "Próba: otwarty plecak\nŻółć 🔥\n---\nDruga próbka";
	assert.equal(await h.copy(text), true);
	assert.deepEqual(h.metrics.apiTexts, [text]);
	assert.deepEqual(h.metrics.commands, []);
	assert.equal(h.metrics.selected, false);
});

test("WebView without Clipboard API uses direct copy fallback and restores focus", async () => {
	const h = harness();
	assert.equal(await h.copy("Pełny raport\nDruga próbka"), true);
	assert.deepEqual(h.metrics.commands, ["copy"]);
	assert.equal(h.metrics.selected, true);
	assert.deepEqual(h.metrics.range, [0, "Pełny raport\nDruga próbka".length]);
	assert.equal(h.metrics.fields.length, 0);
	assert.equal(h.metrics.restores, 1);
});

test("clipboard permission rejection falls back instead of reporting false success", async () => {
	const h = harness({ api: "reject" });
	assert.equal(await h.copy("Raport"), true);
	assert.deepEqual(h.metrics.apiTexts, ["Raport"]);
	assert.deepEqual(h.metrics.commands, ["copy"]);
});

test("failed or missing copy fallback returns false and always cleans up the temporary field", async () => {
	for (const fallback of [false, "throw", "absent"]) {
		const h = harness({ api: "reject", fallback });
		assert.equal(await h.copy("Raport"), false);
		assert.equal(h.metrics.fields.length, 0);
		assert.equal(h.metrics.restores, 1);
	}
});

test("empty report never overwrites the user's clipboard", async () => {
	const h = harness({ api: "success" });
	assert.equal(await h.copy(""), false);
	assert.deepEqual(h.metrics.apiTexts, []);
	assert.deepEqual(h.metrics.commands, []);
});
