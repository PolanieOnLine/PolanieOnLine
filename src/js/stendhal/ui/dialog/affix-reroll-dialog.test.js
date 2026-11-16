"use strict";
const assert = require("node:assert/strict");
const {existsSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync} = require("node:fs");
const {execFileSync} = require("node:child_process");
const {tmpdir} = require("node:os");
const {pathToFileURL} = require("node:url");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

function source(filename) {
	return ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText;
}

function snapshot(extra = {}) {
	return {phase: "open", window_id: "window-1", npc_id: "47", status: "ready", message: "",
		candidate_paths: ["9/bag/1", "9/bag/2"], candidate_names: ["miecz", "miecz"],
		candidate_classes: ["sword", "sword"], candidate_subclasses: ["broadsword", "broadsword"],
		candidate_rarities: ["rare", "epic"], candidate_levels: ["2", "5"],
		selected_path: "", selected_affix: "", affix_ids: [], affix_labels: [], stone_count: "4", can_roll: "0", ...extra};
}

function selected(extra = {}) {
	return snapshot({phase: "select", selected_path: "9/bag/2", item_name: "miecz", item_class: "sword", item_subclass: "broadsword",
		affix_ids: ["swift", "strong"], affix_labels: ["Szybkość ataku +3%", "Siła +2"], ...extra});
}

function quote(extra = {}) {
	return selected({selected_affix: "swift", request_token: "quote-1", can_roll: "1", ...extra});
}

function offer(extra = {}) {
	return quote({phase: "offer", offer_token: "offer-1", current_label: "Szybkość ataku +3%", new_label: "Maksymalne zdrowie +12",
		can_roll: "0", stone_count: "3", ...extra});
}

function harness() {
	const actions = [], frames = [], timers = new Map(); let now = 0, nextTimer = 0;
	class Element {
		constructor(tag) {
			this.tagName = tag; this.children = []; this.dataset = {}; this.className = ""; this.textContent = ""; this.style = {};
			this.classList = {add: (...names) => { this.className += " " + names.join(" "); }};
		}
		append(...children) { children.forEach(child => this.appendChild(child)); }
		appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
		replaceChildren(...children) { this.children = []; this.textContent = ""; this.append(...children); }
		addEventListener(name, handler) { this["on" + name] = handler; }
		setAttribute(name, value) { this[name] = value; }
		contains(element) { return this.children.some(child => child === element || child.contains(element)); }
		focus() { this.focused = true; }
	}
	class DialogContentComponent {
		constructor() { this.componentElement = new Element("div"); }
		setFrame(frame) { this.frame = frame; }
		close() { this.frame.close(); }
	}
	class FloatingWindow {
		constructor(title, content) { this.title = title; this.content = content; frames.push(this); }
		setId(id) { this.id = id; }
		setResponsiveWidth(value) { this.responsiveWidth = value; }
		close() { this.content.onParentClose(); }
	}
	const marauroa = {me: {}, clientFramework: {sendAction: action => actions.push(action)}};
	const dependencies = {
		marauroa: {marauroa, RPEvent: class {}},
		"../../data/Paths": {Paths: {sprites: "/data/sprites"}},
		"../../SingletonRepo": {singletons: {getSpriteStore: () => ({checkPath: value => value, get: () => ({width: 64})})}},
		"../../util/GameLanguage": {GameLanguage: {getText: value => value}},
		"../toolkit/DialogContentComponent": {DialogContentComponent}, "../toolkit/FloatingWindow": {FloatingWindow}
	};
	function load(filename) {
		const exports = {};
		vm.runInNewContext(source(filename), {exports, document: {createElement: tag => new Element(tag)},
			Date: {now: () => now}, Math, Number, setInterval: fn => { timers.set(++nextTimer, fn); return nextTimer; },
			clearInterval: id => timers.delete(id), require: name => { assert.ok(dependencies[name], name); return dependencies[name]; }});
		return exports;
	}
	dependencies["../../data/ItemRarity"] = load("../../data/ItemRarity.ts");
	const Dialog = load("AffixRerollDialog.ts").AffixRerollDialog;
	dependencies["../ui/dialog/AffixRerollDialog"] = {AffixRerollDialog: Dialog};
	const Event = load("../../event/AffixRerollEvent.ts").AffixRerollEvent;
	return {Dialog, Event, actions, frames, timers, marauroa,
		advance: milliseconds => { now += milliseconds; for (const fn of timers.values()) fn(); },
		open: data => { Dialog.show(snapshot(data)); return frames.at(-1)?.content; },
		reply: data => Dialog.show({...data, request_id: actions.at(-1)?.request_id})};
}

function choose(h) {
	const d = h.open();
	d.items.children[1].onclick(); h.reply(selected());
	d.affixes.children[0].onclick(); h.reply(quote());
	return d;
}

test("selection distinguishes duplicate names, displays sprites/rarity/upgrades and only uses server affixes", () => {
	const h = harness(), d = h.open();
	assert.equal(h.frames[0].responsiveWidth, true);
	assert.equal(h.frames[0].title, "Zoryk — przemiana afiksów");
	assert.equal(d.items.children[0].children[1].children[0].textContent, "miecz +2");
	assert.equal(d.items.children[1].children[1].children[1].textContent, "Epicki · #2");
	assert.match(d.items.children[0].children[0].style.backgroundImage, /items\/sword\/broadsword.png/);
	assert.match(d.costCount.parentElement.children[0].style.backgroundImage, /resource\/transformation_stone.png/);
	d.items.children[1].onclick(); d.items.children[1].onclick();
	assert.equal(h.actions.length, 1);
	assert.deepEqual(Array.from(h.actions[0].target_path), ["9", "bag", "2"]);
	assert.equal(h.actions[0].affix_id, undefined);
	h.reply(selected({affix_ids: ["strong"], affix_labels: ["Siła +2"]}));
	assert.equal(d.affixes.children.length, 1); assert.equal(d.affixes.children[0].textContent, "Siła +2");
	d.affixes.children[0].onclick();
	assert.equal(h.actions.at(-1).affix_id, "strong");
	assert.equal(d.roll.disabled, true);
});

test("roll requires the server quote and explicit cost confirmation; replay and double click cannot roll twice", () => {
	const h = harness(), d = choose(h);
	assert.equal(d.roll.disabled, false);
	d.confirmRoll.onclick(); assert.equal(h.actions.length, 2);
	d.roll.onclick(); assert.equal(d.confirmation.hidden, false); assert.equal(h.actions.length, 2);
	assert.match(d.confirmation.children[0].textContent, /nawet jeśli zachowasz obecną/);
	d.confirmRoll.onclick(); d.confirmRoll.onclick();
	assert.equal(h.actions.length, 3); assert.equal(h.actions[2].command, "roll");
	assert.equal(h.actions[2].request_token, "quote-1"); assert.equal(h.actions[2].affix_id, "swift");
	assert.equal(d.refreshButton.disabled, true);
	h.reply(offer()); const applied = d.data;
	h.reply(quote({message: "stale replay"})); assert.equal(d.data, applied);
	assert.equal(d.currentLabel.textContent, "Szybkość ataku +3%");
	assert.equal(d.newLabel.textContent, "Maksymalne zdrowie +12");
	assert.equal(d.keep.disabled, false); assert.equal(d.roll.hidden, true);
	d.keep.onclick(); d.accept.onclick(); assert.equal(h.actions.length, 4);
	assert.equal(h.actions[3].command, "keep"); assert.equal(h.actions[3].offer_token, "offer-1");
	assert.match(d.costCount.textContent, /Posiadasz: 3/);
	h.reply(quote({phase: "result", request_token: "quote-2", stone_count: "3", message: "Zachowano cechę."}));
	assert.equal(d.keep.hidden, true); assert.equal(d.roll.disabled, false);
	assert.match(d.costCount.textContent, /Posiadasz: 3/);
});

test("changed paid items can only keep the old affix, never accept a stale offer", () => {
	const h = harness(), d = h.open({...offer(), phase: "open", can_accept: "0", status: "CHANGED_ITEM"});
	assert.equal(d.keep.disabled, false);
	assert.equal(d.accept.disabled, true);
	d.accept.onclick(); assert.equal(h.actions.length, 0);
	d.keep.onclick(); assert.equal(h.actions.length, 1);
	assert.equal(h.actions[0].command, "keep");
});

test("wrong window/NPC/request and unsolicited non-open responses cannot change a live dialog", () => {
	const h = harness(); h.Dialog.show(offer()); assert.equal(h.frames.length, 0);
	const d = choose(h); d.refreshButton.onclick(); const data = d.data;
	for (const change of [{window_id: "other"}, {npc_id: "48"}, {request_id: "old"}, {phase: "unknown"}]) {
		h.Dialog.show({...offer(), request_id: h.actions.at(-1).request_id, ...change});
		assert.equal(d.data, data); assert.equal(d.waiting, true);
	}
	h.reply(quote()); assert.equal(d.waiting, false);
});

test("closing preserves the server offer, clears local state/timers, rejects old opens, and a new open resumes it", () => {
	const h = harness(), d = h.open({...offer(), phase: "open"});
	assert.equal(d.keep.disabled, false);
	h.frames[0].close();
	assert.equal(h.actions.length, 1); assert.equal(h.actions[0].command, "close");
	assert.equal(h.actions[0].npc_id, "47"); assert.equal(h.actions[0].window_id, "window-1");
	assert.equal(h.timers.size, 0); assert.equal(d.componentElement.children.length, 0); assert.equal(d.sprites.size, 0);
	h.Dialog.show({...offer(), phase: "open"}); h.reply(offer()); assert.equal(h.frames.length, 1);
	const resumed = h.open({...offer(), phase: "open", window_id: "window-2"});
	assert.equal(h.frames.length, 2); assert.equal(resumed.newLabel.textContent, "Maksymalne zdrowie +12");
	resumed.accept.onclick();
	assert.equal(h.actions.at(-1).command, "accept"); assert.equal(h.actions.at(-1).offer_token, "offer-1");
});

test("lost roll response requires Refresh and never retries the paid operation; late superseded response is ignored", () => {
	const h = harness(), d = choose(h);
	d.roll.onclick(); d.confirmRoll.onclick(); const rollId = h.actions.at(-1).request_id;
	h.advance(10001);
	assert.equal(d.roll.disabled, true); assert.equal(d.refreshButton.disabled, false);
	assert.match(d.status.textContent, /Odśwież/);
	d.roll.onclick(); d.confirmRoll.onclick(); d.items.children[0].onclick();
	assert.equal(h.actions.length, 3);
	d.refreshButton.onclick(); assert.equal(h.actions[3].command, "refresh");
	h.Dialog.show({...offer(), request_id: rollId}); assert.equal(d.waiting, true);
	h.reply(offer()); assert.equal(d.waiting, false); assert.equal(d.accept.disabled, false);
	h.advance(50000); assert.equal(h.actions.length, 4);
});

test("current player owns every event/window and changing player prevents old actions or replies", () => {
	const h = harness(), event = Object.assign(new h.Event(), snapshot());
	event.execute({}); assert.equal(h.frames.length, 0);
	event.execute(h.marauroa.me); const d = h.frames[0].content;
	h.marauroa.me = {}; d.refreshButton.onclick(); assert.equal(h.actions.length, 0);
	h.advance(100); assert.equal(h.timers.size, 0); assert.equal(h.actions.length, 0);
	h.Dialog.show(offer()); assert.equal(h.frames.length, 1);
	Object.assign(new h.Event(), snapshot({window_id: "new-owner"})).execute(h.marauroa.me);
	assert.equal(h.frames.length, 2);
});

test("leaving the NPC zone closes the workbench without resolving its offer or sending a mutation", () => {
	const h = harness(); h.marauroa.currentZoneName = "zoryk-room";
	const d = h.open({...offer(), phase: "open"});
	h.marauroa.currentZoneName = "outside";
	d.accept.onclick();
	assert.equal(h.actions.length, 1); assert.equal(h.actions[0].command, "close");
	assert.equal(h.timers.size, 0); assert.equal(d.componentElement.children.length, 0);
	h.reply(offer()); assert.equal(h.frames.length, 1);
});

test("all untrusted labels are literal text, malformed sprite paths are inert, wrapped lists preserve empty columns", () => {
	const h = harness(), attack = '<img src=x onerror="alert(1)">';
	const d = h.open({...quote(), phase: "open", message: attack, item_name: attack,
		candidate_names: [attack, "safe"], candidate_classes: ['x")},body{color:red}', "sword"],
		affix_labels: [attack, "Siła +2"]});
	assert.equal(d.status.textContent, attack); assert.equal(d.identity.children[1].textContent, attack);
	assert.equal(d.items.children[0].children[1].children[0].textContent, attack + " +2");
	assert.equal(d.items.children[0].children[0].style.backgroundImage, undefined);
	assert.equal(d.affixes.children[0].textContent, attack);
	assert.deepEqual(Array.from(d.list("[first\t\tthird]")), ["first", "", "third"]);
	assert.doesNotMatch(readFileSync(path.join(__dirname, "AffixRerollDialog.ts"), "utf8"), /innerHTML|insertAdjacentHTML/);
});

test("legendary quotes show the server's three-stone cost and changed slot marker", () => {
	const h = harness(), d = h.open({...quote({stone_cost: "3", rerolled_affix: "swift"}), phase: "open"});
	assert.match(d.costCount.textContent, /3 ×/);
	assert.ok(d.affixes.children[0].children.some(child => child.className === "affix-rerolled-icon"));
	assert.equal(d.affixes.children[1].children.length, 0);
	d.roll.onclick();
	assert.match(d.confirmation.children[0].textContent, /3/);
});

test("server can_roll is authoritative even with enough stones and a token", () => {
	for (const changed of [{can_roll: "0"}, {request_token: ""}, {selected_affix: ""}]) {
		const h = harness(), d = h.open({...quote(changed), phase: "open"});
		assert.equal(d.roll.disabled, true); d.roll.onclick(); d.confirmRoll.onclick(); assert.equal(h.actions.length, 0);
	}
});

function browserFixture() {
	const root = path.resolve(__dirname, "../../../../.."), assets = pathToFileURL(path.join(root, "data") + path.sep).href;
	const css = readFileSync(path.join(root, "src/js/css/main.css"), "utf8").replaceAll("url(/data/", `url(${assets}`);
	return `<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>${css}\nbody{margin:8px;background:#211b14;} .preview-frame{display:inline-block;border:2px solid #85673f;background:var(--background-url);max-width:calc(100vw - 20px);} .preview-title{padding:4px 8px;color:var(--text-color);font-weight:bold;}</style>
<body><script>
const frames=[],actions=[],marauroa={me:{},clientFramework:{sendAction:a=>actions.push(a)}};
class DialogContentComponent{constructor(){this.componentElement=document.createElement('div');}setFrame(frame){this.frame=frame;}close(){this.frame.close();}}
class FloatingWindow{constructor(title,content){this.content=content;frames.push(this);this.element=document.createElement('div');this.element.className='preview-frame';const bar=document.createElement('div');bar.className='preview-title';bar.textContent=title;this.element.append(bar,content.componentElement);document.body.append(this.element);}setId(){}setResponsiveWidth(){}close(){this.content.onParentClose();this.element.remove();}}
const dependencies={marauroa:{marauroa},'../../data/Paths':{Paths:{sprites:${JSON.stringify(assets + "sprites")}}},
'../../SingletonRepo':{singletons:{getSpriteStore:()=>({checkPath:p=>p,get:()=>({width:32})})}},'../../util/GameLanguage':{GameLanguage:{getText:s=>s}},'../toolkit/DialogContentComponent':{DialogContentComponent},'../toolkit/FloatingWindow':{FloatingWindow}};
function load(code){const exports={};new Function('exports','require',code)(exports,name=>dependencies[name]);return exports;}
dependencies['../../data/ItemRarity']=load(${JSON.stringify(source("../../data/ItemRarity.ts"))});
const Dialog=load(${JSON.stringify(source("AffixRerollDialog.ts"))}).AffixRerollDialog;
const data=${JSON.stringify(offer({phase: "open"}))};Dialog.show(data);const dialog=frames[0].content;
function reply(extra={}){Dialog.show({...data,phase:'offer',...extra,request_id:actions.at(-1).request_id});}
window.fixture={Dialog,dialog,actions,reply,data};
</script>`;
}

const edge = process.platform === "win32" && [process.env["PROGRAMFILES(X86)"], process.env.PROGRAMFILES]
	.filter(Boolean).map(directory => path.join(directory, "Microsoft/Edge/Application/msedge.exe")).find(existsSync);

test("live browser fits desktop, portrait, landscape and rotation while keeping offer identity and controls usable", {skip: !edge}, () => {
	const directory = mkdtempSync(path.join(tmpdir(), "polanie-affix-layout-"));
	try {
		const filename = path.join(directory, "layout.html");
		writeFileSync(filename, `<!doctype html><body><iframe id="preview" style="border:0;width:390px;height:844px"></iframe><pre id="result">PENDING</pre><script>
const frame=document.getElementById('preview'),result=document.getElementById('result'),check=(ok,message)=>{if(!ok)throw new Error(message);};
const settle=()=>new Promise(resolve=>setTimeout(resolve,60));
frame.onload=async()=>{try{const f=frame.contentWindow.fixture,d=f.dialog;
for(const [width,height]of [[390,844],[320,568],[844,390],[640,360],[1024,768],[390,844]]){
frame.style.width=width+'px';frame.style.height=height+'px';await settle();const w=frame.contentWindow,el=d.componentElement,box=el.getBoundingClientRect();
check(el.scrollWidth<=el.clientWidth,'horizontal overflow '+width);check(box.bottom<=height,'dialog exceeds viewport '+width);
check(w.document.documentElement.scrollWidth<=width,'page horizontal overflow '+width);
for(const button of [d.keep,d.accept,d.refreshButton]){check(!button.disabled&&!button.hidden,'offer control unavailable');check(button.getBoundingClientRect().height>=44,'touch control too small');}
check(d.newLabel.textContent==='Maksymalne zdrowie +12','offer lost on rotation');}
d.refreshButton.click();const request=f.actions.at(-1).request_id;frame.style.width='844px';frame.style.height='390px';await settle();
check(d.waiting&&f.actions.length===1,'rotation changed request');f.reply();check(!d.waiting&&f.actions.at(-1).request_id===request,'reply lost after rotation');
f.Dialog.show({...f.data,window_id:'confirm-preview',offer_token:'',phase:'open',can_roll:'1'});const active=f.Dialog.active;active.roll.click();await settle();
check(!active.confirmation.hidden,'confirmation missing');check(active.componentElement.scrollWidth<=active.componentElement.clientWidth,'confirmation overflow');
result.textContent='PASS desktop;portrait;landscape;rotation;offer;confirmation;44px controls';}catch(error){result.textContent='FAIL '+error.stack;}};
frame.srcdoc=${JSON.stringify(browserFixture()).replaceAll("<", "\\u003c")};</script>`);
		const output = execFileSync(edge, ["--headless=new", "--disable-gpu", "--no-first-run", "--allow-file-access-from-files",
			`--user-data-dir=${path.join(directory, "profile")}`, "--dump-dom", "--virtual-time-budget=2000", pathToFileURL(filename).href],
			{encoding: "utf8", windowsHide: true, timeout: 20000, stdio: ["ignore", "pipe", "ignore"]});
		assert.match(output.match(/<pre id="result">([^<]*)<\/pre>/)?.[1] || "missing result", /^PASS desktop;/);
	} finally {
		if (directory.startsWith(path.join(tmpdir(), "polanie-affix-layout-"))) rmSync(directory, {recursive: true, force: true});
	}
});

if (process.env.AFFIX_REROLL_PREVIEW) {
	mkdirSync(path.dirname(process.env.AFFIX_REROLL_PREVIEW), {recursive: true});
	writeFileSync(process.env.AFFIX_REROLL_PREVIEW, browserFixture());
}
