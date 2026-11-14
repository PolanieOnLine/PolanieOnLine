/***************************************************************************
 *                   (C) Copyright 2016-2023 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

"use strict";

let formatLogEntry;
let nodeTest;
let assert;

if (typeof window === "undefined") {
	assert = require("node:assert/strict");
	nodeTest = require("node:test").test;
	const {readFileSync} = require("node:fs");
	const path = require("node:path");
	const vm = require("node:vm");
	const ts = require("typescript");
	const dependencies = {
		"stendhal": {stendhal: {}},
		"../stendhal": {stendhal: {}},
		"../ui/UI": {ui: {}},
		"../ui/UIComponentEnum": {UIComponentEnum: {}},
		"marauroa": {marauroa: {}}
	};
	function loadModule(file) {
		const exports = {};
		const source = ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
			compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
		}).outputText;
		vm.runInNewContext(source, {exports, require: name => {
			assert.ok(dependencies[name], "Unexpected dependency: " + name);
			return dependencies[name];
		}});
		return exports;
	}
	dependencies["../ui/HTMLManager"] = loadModule("HTMLManager.ts");
	formatLogEntry = loadModule("../util/Chat.ts").Chat.formatLogEntry;
} else {
	const stendhal = window.stendhal = window.stendhal || {};
	formatLogEntry = message => stendhal.ui.chatLog.formatLogEntry(message);
}

function test(input, output) {
	if (nodeTest) {
		nodeTest("chat formatting: " + JSON.stringify(input), () => {
			assert.equal(formatLogEntry(input), output);
		});
		return;
	}
	var res = formatLogEntry(input);
	if (res === output) {
		console.log(input, "|", output);
	} else {
		console.warn(input, "|", output, "|", res);
		window.failedtests.push("chatlog: " + input + " | " + output + " | " + res);
	}
}

test("",            "");
test("a",           "a");
test("a <oops",     "a &lt;oops");
test("#a",          "<span class=\"logh\">a</span>");
test("##a",         "#a");
test("#a#b",        "<span class=\"logh\">a#b</span>");
test("#a.",         "<span class=\"logh\">a</span>.");
test("a #b c d",    "a <span class=\"logh\">b</span> c d");
test("a #'b c' d",  "a <span class=\"logh\">b c</span> d");
test("a §b c d",    "a <span class=\"logi\">b</span> c d");   // not used
test("a §'b c' d",  "a <span class=\"logi\">b c</span> d");
test("a #§'b c' d", "a <span class=\"logh\"><span class=\"logi\">b c</span></span> d");
test("\\\\a",       "\\a");
test("\\§a",        "§a");
test("#https://polanieonline.eu", "<span class=\"logh\">https://polanieonline.eu</span>");
test("\\<img src=x onerror=alert(1)>", "&lt;img src=x onerror=alert(1)&gt;");
test("<script>alert(1)</script>", "&lt;script&gt;alert(1)&lt;/script&gt;");
test("a & b", "a &amp; b");
test("&lt;img&gt;", "&amp;lt;img&amp;gt;");
test("a > b", "a &gt; b");
test('"tekst"', "&quot;tekst&quot;");
test("\\&\\<\\>\\\"", "&amp;&lt;&gt;&quot;");
test("koniec\\", "koniec\\");
test("\\", "\\");
test("a ~b c", "a <span class=\"logu\">b</span> c");
test("a ~'b c' d", "a <span class=\"logu\">b c</span> d");
test("a ¡b c", "a <span class=\"logadmin\">b</span> c");
test("a ¡'b c' d", "a <span class=\"logadmin\">b c</span> d");
test("## §§ ~~ ¡¡", "# § ~ ¡");
test("\\# \\§ \\~ \\¡", "# § ~ ¡");
test("Żółć 🧭", "Żółć 🧭");

for (const [marker, className] of [["#", "logh"], ["§", "logi"], ["~", "logu"], ["¡", "logadmin"]]) {
	test(marker + "'\\<img src=x onerror=alert(1)>'",
		'<span class="' + className + '">&lt;img src=x onerror=alert(1)&gt;</span>');
}
