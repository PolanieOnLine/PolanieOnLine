"use strict";
const assert = require("node:assert/strict");
const {existsSync, mkdtempSync, readFileSync, rmSync, writeFileSync} = require("node:fs");
const {execFileSync} = require("node:child_process");
const {tmpdir} = require("node:os");
const {pathToFileURL} = require("node:url");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
const {test} = require("node:test");

const portraitLayout = [[12, 0, 0, 2, 1], [3, 2, 0, 1, 1], [19, 3, 0, 1, 1], [7, 4, 0, 1, 1], [21, 5, 0, 1, 1],
	[1, 0, 1, 1, 1], [15, 1, 1, 1, 1], [9, 2, 1, 1, 2], [22, 3, 1, 1, 1], [5, 4, 1, 1, 1], [18, 5, 1, 1, 1],
	[4, 0, 2, 1, 1], [13, 1, 2, 1, 1], [2, 3, 2, 1, 1], [20, 4, 2, 2, 1], [10, 0, 3, 1, 1], [23, 1, 3, 1, 1],
	[6, 2, 3, 1, 1], [16, 3, 3, 1, 1], [24, 4, 3, 2, 2], [8, 0, 4, 1, 1], [14, 1, 4, 1, 1], [17, 2, 4, 1, 1], [11, 3, 4, 1, 1]];

function harness() {
	const actions = [], frames = [], timers = new Map();
	class Element {
		constructor() {
			this.children = []; this.dataset = {}; this.className = ""; this.textContent = "";
			this.style = {setProperty(key, value) { this[key] = value; }};
			this.classList = {
				contains: value => this.className.split(" ").includes(value),
				add: value => { this.classList.toggle(value, true); },
				toggle: (value, enabled) => {
					const classes = new Set(this.className.split(" ").filter(Boolean));
					if (enabled) classes.add(value); else classes.delete(value);
					this.className = [...classes].join(" ");
				}
			};
		}
		append(...children) { children.forEach(child => this.appendChild(child)); }
		appendChild(child) { child.parentElement = this; this.children.push(child); return child; }
		replaceChildren(...children) { this.children.forEach(child => child.parentElement = undefined); this.children = []; this.append(...children); }
		addEventListener(name, handler) { this["on" + name] = handler; }
		setAttribute(key, value) { this[key] = value; }
		getAttribute(key) { return this[key] ?? null; }
		removeAttribute(key) { delete this[key]; }
		contains(element) { return this.children.some(child => child === element || child.contains(element)); }
		scrollBy(options) { this.scrollTop = Math.min(this.scrollHeight - this.clientHeight, Math.max(0, (this.scrollTop || 0) + options.top)); }
	}
	class DialogContentComponent {
		constructor() { this.componentElement = new Element(); }
		setFrame(frame) { this.frame = frame; }
		close() { this.frame.close(); }
		addCloseButton() {}
	}
	class FloatingWindow {
		constructor(title, content) { this.content = content; frames.push(this); }
		setId() {}
		setResponsiveWidth(enabled) { this.responsiveWidth = enabled; }
		close() { this.content.onParentClose(); }
	}
	const marauroa = {me: {advent_calendar_available: 1}, clientFramework: {sendAction: action => actions.push(action)}};
	const dependencies = {
		marauroa: {marauroa}, "../../data/Paths": {Paths: {sprites: "/data/sprites", gui: "/data/gui"}},
		"../../SingletonRepo": {singletons: {getSpriteStore: () => ({checkPath: value => value, get: () => ({width: 64})})}},
		"../../util/GameLanguage": {GameLanguage: {getText: value => value, getLanguage: () => "pl"}},
		"../toolkit/DialogContentComponent": {DialogContentComponent}, "../toolkit/FloatingWindow": {FloatingWindow}
	};
	function load(filename) {
		const exports = {};
		vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
			compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
		}).outputText, {exports, document: {createElement: () => new Element()}, Date, Math, Number, Intl,
			setInterval: fn => { const id = timers.size + 1; timers.set(id, fn); return id; }, clearInterval: id => timers.delete(id),
			require: name => { assert.ok(dependencies[name], name); return dependencies[name]; }});
		return exports;
	}
	dependencies["../component/ItemSlotPresentation"] = load("../component/ItemSlotPresentation.ts");
	const Dialog = load("AdventCalendarDialog.ts").AdventCalendarDialog;
	const states = Array(24).fill("available");
	function reply(overrides = {}) {
		Dialog.show({request_id: actions.at(-1).request_id, status: "ok", date: "2026-12-24", year: 2026,
			today: 24, layout: portraitLayout.map(door => door.join(",")),
			states: states.slice(), revealed_day: 0, ...overrides});
	}
	Dialog.open(); reply();
	return {Dialog, actions, frames, timers, marauroa, states, reply, dialog: frames[0].content};
}

function gift(day, overrides = {}) {
	return {status: "received", revealed_day: day, reward_names: ["money"], reward_classes: ["money"],
		reward_subclasses: ["gold"], reward_quantities: ["250"], ...overrides};
}

test("server availability closes the calendar and prevents reopening it through the command", () => {
	const h = harness();
	h.marauroa.me.advent_calendar_available = 0;
	const sent = h.actions.length, windows = h.frames.length;
	[...h.timers.values()][0]();
	assert.equal(h.timers.size, 0);
	h.Dialog.open();
	assert.equal(h.actions.length, sent);
	assert.equal(h.frames.length, windows);
	assert.equal(h.Dialog.isAvailable(), false);
});

test("an application menu already open follows calendar availability and releases its timer on close", () => {
	let available = false;
	const timers = new Map(), buttons = new Map();
	class DialogContentComponent {
		constructor() {
			this.componentElement = {addEventListener() {}, querySelector: selector => buttons.get(selector.match(/menubutton\.([^']+)/)[1]),
				set innerHTML(value) {for (const match of value.matchAll(/id="menubutton\.([^"]+)"/g)) buttons.set(match[1], {style: {}});}};
		}
	}
	const exports = {}, dependencies = {
		"../toolkit/DialogContentComponent": {DialogContentComponent},
		"./AdventCalendarDialog": {AdventCalendarDialog: {isAvailable: () => available}},
		"../../util/Debug": {Debug: {isActive: () => false}},
		"../../stendhal": {stendhal: {ui: {html: {esc: text => text}}}}
	};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, "ApplicationMenuDialog.ts"), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText, {exports, setInterval: fn => {timers.set(1, fn); return 1;}, clearInterval: id => timers.delete(id),
		require: name => dependencies[name] || {}});
	const menu = new exports.ApplicationMenuDialog();
	assert.equal(buttons.get("adventcalendar")?.style.display, "none");
	available = true; timers.get(1)();
	assert.equal(buttons.get("adventcalendar").style.display, "");
	available = false; timers.get(1)();
	assert.equal(buttons.get("adventcalendar").style.display, "none");
	menu.onParentClose();
	assert.equal(timers.size, 0);
});

test("claim opens its door with inline sprites, other previews survive refresh and subsequent claims", () => {
	const h = harness(); const first = h.dialog.doors.get(1);
	first.onclick(); assert.equal(h.actions.at(-1).command, "claim");
	h.states[0] = "claimed"; h.reply(gift(1));
	assert.ok(first.classList.contains("is-open"));
	assert.ok(first.classList.contains("has-preview"));
	assert.equal(h.dialog.previews.get(1).children.length, 1);
	const slot = h.dialog.previews.get(1).children[0].children[0];
	assert.equal(slot.children[1].textContent, "250");
	assert.match(slot.children[0].style.backgroundImage, /items\/money\/gold.png/);
	h.dialog.send("refresh"); h.reply();
	assert.equal(h.dialog.previews.get(1).children[0].children[0], slot);
	h.dialog.doors.get(2).onclick(); h.states[1] = "claimed"; h.reply(gift(2));
	assert.equal(h.dialog.previews.get(1).children.length, 1);
	assert.equal(h.dialog.previews.get(2).children.length, 1);
	assert.equal(h.dialog.sprites.size, 2);
});

test("only a successful new claim starts opening; refresh keeps it and viewing never replays it", () => {
	const h = harness(); const door = h.dialog.doors.get(1);
	door.onclick();
	assert.ok(!door.classList.contains("is-opening"));
	h.reply(gift(1, {status: "full"}));
	assert.ok(!door.classList.contains("is-opening"));
	assert.ok(!door.classList.contains("is-open"));
	door.onclick(); h.states[0] = "claimed"; h.reply(gift(1));
	assert.ok(door.classList.contains("is-opening"));
	const reward = h.dialog.previews.get(1).children[0];
	h.dialog.send("refresh"); h.reply();
	assert.ok(door.classList.contains("is-opening"));
	assert.equal(h.dialog.previews.get(1).children[0], reward);
	door.onclick(); h.reply(gift(1, {status: "claimed"}));
	assert.ok(door.classList.contains("is-open"));
	assert.ok(!door.classList.contains("is-opening"));
	h.frames[0].close(); h.Dialog.open(); h.reply();
	const reopened = h.frames[1].content.doors.get(1);
	assert.ok(reopened.classList.contains("is-open"));
	assert.ok(!reopened.classList.contains("is-opening"));
});

test("unavailable numbers dim by calendar state while pending available doors stay bright", () => {
	const h = harness();
	h.states[0] = "future"; h.states[1] = "expired"; h.states[2] = "claimed";
	h.dialog.send("refresh"); h.reply();
	for (const day of [1, 2]) assert.ok(h.dialog.doors.get(day).classList.contains("is-unavailable"));
	for (const day of [3, 4, 24]) assert.ok(!h.dialog.doors.get(day).classList.contains("is-unavailable"));
	h.dialog.doors.get(4).onclick();
	assert.ok(h.dialog.doors.get(4).disabled);
	assert.ok(!h.dialog.doors.get(4).classList.contains("is-unavailable"));
	h.reply({status: "inactive"});
	for (const door of h.dialog.doors.values()) assert.ok(door.classList.contains("is-unavailable"));
	assert.ok(h.dialog.doors.get(24).classList.contains("is-today"));
});

test("reopening keeps claimed doors open with a check and reveals icons only after view", () => {
	const h = harness(); h.dialog.doors.get(24).onclick(); h.states[23] = "claimed"; h.reply(gift(24));
	const stale = h.actions.at(-1).request_id;
	h.frames[0].close(); assert.equal(h.dialog.sprites.size, 0);
	assert.equal(h.dialog.previews.get(24).children.length, 0); assert.equal(h.timers.size, 0);
	h.Dialog.open(); h.reply(); const reopened = h.frames[1].content; const door = reopened.doors.get(24);
	assert.ok(door.classList.contains("is-open")); assert.ok(door.classList.contains("is-claimed"));
	assert.ok(!door.classList.contains("has-preview"));
	h.Dialog.show({request_id: stale, ...gift(24)});
	assert.equal(reopened.previews.get(24).children.length, 0);
	door.onclick(); assert.equal(h.actions.at(-1).command, "view"); h.reply(gift(24, {status: "claimed"}));
	assert.equal(reopened.previews.get(24).children.length, 1);
});

test("future, failed, inactive, mismatched and unsolicited rewards never reveal a gift", () => {
	const h = harness();
	h.dialog.send("refresh"); h.states[0] = "future"; h.reply(gift(1));
	assert.equal(h.dialog.sprites.size, 0);
	h.states[0] = "available"; h.dialog.doors.get(1).onclick(); h.reply(gift(1, {status: "full"}));
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.states[1] = "claimed"; h.reply(gift(2));
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.send("refresh"); h.reply(gift(1)); assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.reply(gift(1)); assert.equal(h.dialog.sprites.size, 1);
	h.dialog.send("refresh"); h.reply(gift(1, {status: "inactive"}));
	assert.equal(h.dialog.sprites.size, 0); assert.equal(h.dialog.previews.get(1).children.length, 0);
	assert.ok(h.dialog.doors.get(1).disabled); assert.ok(!h.dialog.doors.get(1).classList.contains("is-open"));
});

test("state rollback, year changes and account changes discard previews", () => {
	const h = harness(); h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.reply(gift(1));
	h.states[0] = "available"; h.dialog.send("refresh"); h.reply();
	assert.equal(h.dialog.previews.get(1).children.length, 0); assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.reply(gift(1));
	h.dialog.send("refresh"); h.reply({date: "2027-12-01", year: 2027});
	assert.equal(h.dialog.sprites.size, 0);
	h.dialog.doors.get(1).onclick(); h.reply(gift(1, {year: 2027}));
	h.marauroa.me = {}; [...h.timers.values()][0]();
	assert.equal(h.dialog.sprites.size, 0); assert.equal(h.timers.size, 0);
});

test("multiple inline rewards retain exact accessible amounts while large quantity badges stay compact", () => {
	const h = harness(); h.dialog.doors.get(19).onclick(); h.states[18] = "claimed";
	h.reply(gift(19, {reward_names: ["money", "eliksir", "jabłko"], reward_classes: ["money", "drink", "food"],
		reward_subclasses: ["gold", "potion", "apple_red"], reward_quantities: ["25000", "3", "1"]}));
	const rewards = h.dialog.previews.get(19).children;
	assert.equal(rewards.length, 3); assert.equal(h.dialog.sprites.size, 3);
	assert.equal(rewards[0].children[0].children[1].textContent, "25K");
	assert.equal(rewards[2].children[0].children[1].textContent, "");
	assert.equal(rewards[0].title, "monety × 25000");
	assert.match(h.dialog.doors.get(19).getAttribute("aria-label"), /monety × 25000, eliksir × 3, jabłko × 1/);
	const request = h.actions.at(-1).request_id;
	h.Dialog.show({request_id: request, ...gift(19)});
	assert.equal(h.dialog.previews.get(19).children.length, 3);
});

test("a matching late claim reply is accepted once after the interface timeout", () => {
	const h = harness(); const door = h.dialog.doors.get(1); door.onclick();
	h.dialog.sentAt = Date.now() - 9000; [...h.timers.values()][0]();
	assert.equal(h.dialog.waiting, false); assert.equal(door.disabled, false);
	h.states[0] = "claimed"; h.reply(gift(1));
	assert.ok(door.classList.contains("is-open")); assert.equal(h.dialog.sprites.size, 1);
	assert.ok(door.classList.contains("is-opening"));
	const slot = h.dialog.previews.get(1).children[0];
	h.reply(gift(1, {reward_quantities: ["999"]}));
	assert.equal(h.dialog.previews.get(1).children[0], slot);
	assert.equal(slot.children[0].children[1].textContent, "250");
});

test("invalid claim replies never start the opening animation", () => {
	for (const response of [gift(1, {status: "full"}), gift(1, {status: "inactive"}), gift(2), gift(1, {status: "claimed"})]) {
		const h = harness(); h.dialog.doors.get(1).onclick(); h.states[0] = "claimed"; h.states[1] = "claimed";
		h.reply(response);
		for (const door of h.dialog.doors.values()) assert.ok(!door.classList.contains("is-opening"));
	}
});

test("larger reward bundles remain accessible and can scroll inside their own door by keyboard", () => {
	const h = harness(); const door = h.dialog.doors.get(19); door.onclick(); h.states[18] = "claimed";
	h.reply(gift(19, {reward_names: ["A", "B", "C", "D", "E"], reward_classes: Array(5).fill("money"),
		reward_subclasses: Array(5).fill("gold"), reward_quantities: ["1", "2", "3", "4", "5"]}));
	const preview = h.dialog.previews.get(19);
	assert.equal(preview.children.length, 5); assert.equal(h.dialog.sprites.size, 5);
	assert.match(door.getAttribute("aria-label"), /A × 1, B × 2, C × 3, D × 4, E × 5/);
	assert.equal(door.title, "A × 1\nB × 2\nC × 3\nD × 4\nE × 5");
	preview.scrollHeight = 100; preview.clientHeight = 50;
	let prevented = false; const actions = h.actions.length;
	door.onkeydown({key: "ArrowDown", preventDefault() { prevented = true; }});
	assert.equal(prevented, true); assert.equal(preview.scrollTop, 28);
	door.onkeydown({key: "ArrowDown", preventDefault() {}});
	assert.equal(preview.scrollTop, 50); assert.equal(h.actions.length, actions);
	door.onkeydown({key: "ArrowUp", preventDefault() {}});
	assert.equal(preview.scrollTop, 22);
});

test("portrait and landscape placements each tile all30cells exactly once with24unique doors", () => {
	const h = harness();
	assert.equal(h.frames[0].responsiveWidth, true);
	for (const [prefix, columns, rows] of [["", 6, 5], ["landscape-", 10, 3]]) {
		const occupied = new Set();
		for (const [day, door] of h.dialog.doors) {
			const parse = value => value.match(/^(\d+) \/ span (\d+)$/).slice(1).map(Number);
			const [column, width] = parse(door.style[`--advent-${prefix}column`]);
			const [row, height] = parse(door.style[`--advent-${prefix}row`]);
			assert.equal(width, day === 12 || day === 20 || day === 24 ? 2 : 1);
			assert.equal(height, day === 9 || day === 24 ? 2 : 1);
			if (prefix && day === 24) assert.deepEqual([column, row], [9, 2]);
			for (let x = column; x < column + width; x++) for (let y = row; y < row + height; y++) {
				assert.ok(x >= 1 && x <= columns && y >= 1 && y <= rows);
				const cell = `${x},${y}`;
				assert.ok(!occupied.has(cell), `${prefix}${cell} overlaps at door${day}`);
				occupied.add(cell);
			}
		}
		assert.equal(h.dialog.doors.size, 24); assert.equal(occupied.size, 30);
	}
});

function browserFixture() {
	const root = path.resolve(__dirname, "../../../../..");
	const assets = pathToFileURL(path.join(root, "data") + path.sep).href;
	const css = readFileSync(path.join(root, "src/js/css/main.css"), "utf8").replaceAll("url(/data/", `url(${assets}`);
	const sources = {};
	for (const [key, filename] of [["slot", "../component/ItemSlotPresentation.ts"], ["calendar", "AdventCalendarDialog.ts"]]) {
		sources[key] = ts.transpileModule(readFileSync(path.join(__dirname, filename), "utf8"), {
			compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
		}).outputText;
	}
	return `<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>${css}\nbody{margin:8px;background:#211b14;color:#eee;} .advent-calendar-dialog{background:var(--background-url);border:2px solid #b48954;}</style>
<body><script>
const actions=[],frames=[];
const marauroa={me:{advent_calendar_available:1},clientFramework:{sendAction:action=>actions.push(action)}};
class DialogContentComponent {
	constructor(){this.componentElement=document.createElement('div');document.body.append(this.componentElement);}
	setFrame(frame){this.frame=frame;}
	addCloseButton(){const footer=document.createElement('div');footer.className='dialogbuttons';const button=document.createElement('button');button.className='dialogbutton';button.textContent='Zamknij';footer.append(button);this.componentElement.append(footer);}
}
class FloatingWindow{constructor(title,content){this.content=content;frames.push(this);}setId(){}setResponsiveWidth(){}}
const dependencies={marauroa:{marauroa},'../../data/Paths':{Paths:{sprites:${JSON.stringify(assets + "sprites")},gui:${JSON.stringify(assets + "gui")}}},
	'../../SingletonRepo':{singletons:{getSpriteStore:()=>({checkPath:p=>p,get:()=>({width:32})})}},
	'../../util/GameLanguage':{GameLanguage:{getText:s=>s,getLanguage:()=>"pl"}},
	'../toolkit/DialogContentComponent':{DialogContentComponent},'../toolkit/FloatingWindow':{FloatingWindow}};
const sources=${JSON.stringify(sources)};
function load(name){const exports={};new Function('exports','require',sources[name])(exports,name=>dependencies[name]);return exports;}
dependencies['../component/ItemSlotPresentation']=load('slot');
const Calendar=load('calendar').AdventCalendarDialog,states=Array(24).fill('available');
function reply(extra={}){Calendar.show({request_id:actions.at(-1).request_id,status:'ok',date:'2026-12-24',year:2026,today:24,states:[...states],layout:${JSON.stringify(portraitLayout.map(door => door.join(",")))},...extra});}
Calendar.open();reply();
const dialog=frames[0].content;
dialog.doors.get(1).click();states[0]='claimed';reply(${JSON.stringify(gift(1))});
dialog.doors.get(19).click();
window.fixture={Calendar,actions,dialog,states,reply};
</script>`;
}

const edge = process.platform === "win32" && [process.env["PROGRAMFILES(X86)"], process.env.PROGRAMFILES]
	.filter(Boolean).map(directory => path.join(directory, "Microsoft/Edge/Application/msedge.exe")).find(existsSync);

test("live CSS rotation preserves door identity, previews and an in-flight reply across portrait and landscape", {skip: !edge}, () => {
	const directory = mkdtempSync(path.join(tmpdir(), "polanie-advent-rotation-"));
	try {
		const filename = path.join(directory, "rotation.html");
		writeFileSync(filename, `<!doctype html><body><iframe id="calendar" style="border:0;width:390px;height:844px"></iframe><pre id="result">PENDING</pre><script>
const frame=document.getElementById('calendar'),result=document.getElementById('result');
const check=(condition,message)=>{if(!condition)throw new Error(message);};
const settle=()=>new Promise(resolve=>setTimeout(resolve,60));
frame.onload=async()=>{try{
	const w=frame.contentWindow,f=w.fixture,d=f.dialog,doors=[...d.doors.values()],slot=d.previews.get(1).firstChild;
	const request=d.requestId,count=f.actions.length;
	function verify(columns,height){
		const board=w.getComputedStyle(d.board),rect=d.board.getBoundingClientRect();
		check(board.gridTemplateColumns.split(' ').length===columns,'wrong column count');
		check(d.doors.size===24&&d.board.children.length===24,'door count changed');
		for(const [index,door]of [...d.doors.values()].entries()){
			check(door===doors[index],'door rebuilt');const box=door.getBoundingClientRect();
			check(box.left>=rect.left-.1&&box.right<=rect.right+.1&&box.top>=rect.top-.1&&box.bottom<=rect.bottom+.1,'door outside board');
			if(columns===10)check(box.width>=44&&box.height>=44,'touch door too small');
		}
		check(d.previews.get(1).firstChild===slot,'preview replaced');
		check(f.actions.length===count&&d.requestId===request,'rotation sent or replaced request');
		if(columns===10){check(d.componentElement.scrollHeight<=d.componentElement.clientHeight+1,'dialog scrolls');check(d.componentElement.getBoundingClientRect().bottom<=height,'dialog exceeds viewport');}
	}
	await settle();verify(6,844);
	frame.style.width='844px';frame.style.height='390px';await settle();verify(10,390);
	check(d.waiting,'pending claim lost');f.states[18]='claimed';f.reply(${JSON.stringify(gift(19))});
	check(d.previews.get(19).children.length===1&&!d.waiting,'pending reply rejected');
	frame.style.width='390px';frame.style.height='844px';await settle();verify(6,844);
	check(d.previews.get(19).children.length===1&&d.doors.get(19).classList.contains('is-claimed'),'claimed state lost');
	frame.style.width='640px';frame.style.height='360px';await settle();verify(10,360);
	frame.style.width='1024px';frame.style.height='768px';await settle();verify(6,768);
	check(d.componentElement.getBoundingClientRect().width===530,'desktop width changed');
	result.textContent='PASS portrait-landscape-portrait;844x390;640x360;desktop;identity;pending reply;previews';
}catch(error){result.textContent='FAIL '+error.stack;}};
frame.srcdoc=${JSON.stringify(browserFixture()).replaceAll("<", "\\u003c")};
</script>`);
		const output = execFileSync(edge, ["--headless=new", "--disable-gpu", "--no-first-run", "--allow-file-access-from-files",
			`--user-data-dir=${path.join(directory, "profile")}`, "--dump-dom", "--virtual-time-budget=2500", pathToFileURL(filename).href],
			{encoding: "utf8", windowsHide: true, timeout: 20000, stdio: ["ignore", "pipe", "ignore"]});
		assert.match(output.match(/<pre id="result">([^<]*)<\/pre>/)?.[1] || "missing browser result", /^PASS portrait-landscape-portrait;/);
	} finally {
		if (directory.startsWith(path.join(tmpdir(), "polanie-advent-rotation-"))) rmSync(directory, {recursive: true, force: true, maxRetries: 5, retryDelay: 100});
	}
});

test("live CSS opens before revealing rewards and keeps larger Christmas Eve icons accessible at every size", {skip: !edge}, () => {
	const directory = mkdtempSync(path.join(tmpdir(), "polanie-advent-opening-"));
	try {
		const filename = path.join(directory, "opening.html");
		writeFileSync(filename, `<!doctype html><body><iframe id="calendar" style="border:0;width:390px;height:844px"></iframe><pre id="result">PENDING</pre><script>
const frame=document.getElementById('calendar'),result=document.getElementById('result');
const check=(condition,message)=>{if(!condition)throw new Error(message);};
const settle=()=>new Promise(resolve=>setTimeout(resolve,60));
frame.onload=async()=>{try{
	const w=frame.contentWindow,f=w.fixture,d=f.dialog,door=d.doors.get(19),preview=d.previews.get(19);
	f.states[18]='claimed';f.reply(${JSON.stringify(gift(19))});
	const reduced=w.matchMedia('(prefers-reduced-motion: reduce)').matches,animations=door.getAnimations({subtree:true});
	if(reduced){
		check(animations.length===0&&w.getComputedStyle(preview).opacity==='1','reduced motion delays rewards');
	}else{
		check(animations.length>=2,'opening or reward animation missing');
		for(const animation of animations){animation.pause();animation.currentTime=0;}
		const leaf=door.querySelector('.advent-calendar-leaf-left'),closedWidth=leaf.getBoundingClientRect().width;
		check(closedWidth>=door.clientWidth-1,'opening does not begin with a closed leaf');
		for(const animation of animations){animation.pause();animation.currentTime=100;}
		check(w.getComputedStyle(door.querySelector('.advent-calendar-leaf-left')).transform.startsWith('matrix3d('),'door does not hinge');
		d.send('refresh');f.reply();
		check(door.getAnimations({subtree:true}).every((animation,index)=>animation===animations[index]&&animation.currentTime===100),'refresh restarted the opening animation');
		for(const animation of animations)animation.currentTime=199;
		check(w.getComputedStyle(preview).opacity==='0','rewards appear before the door opens');
		for(const animation of animations)animation.currentTime=201;
		check(Number(w.getComputedStyle(preview).opacity)>0,'rewards do not emerge after opening');
		for(const animation of animations)animation.currentTime=320;
		check(w.getComputedStyle(preview).opacity==='1','rewards remain faded');
		check(leaf.getBoundingClientRect().width<closedWidth/2,'opened leaf still covers the reward');
	}
	door.click();f.reply(${JSON.stringify(gift(19, {status: "claimed"}))});
	check(door.getAnimations({subtree:true}).length===0,'viewing rewards replays opening');
	d.doors.get(24).click();f.states[23]='claimed';f.reply(${JSON.stringify(gift(24, {
			reward_names: ["money", "eliksir", "jabłko", "A", "B", "C", "D", "E", "F"],
			reward_classes: ["money", "drink", "food", ...Array(6).fill("money")],
			reward_subclasses: ["gold", "potion", "apple_red", ...Array(6).fill("gold")],
			reward_quantities: ["25000", "3", "1", "2", "3", "4", "5", "6", "7"]
		}))});
	for(const animation of d.doors.get(24).getAnimations({subtree:true})){animation.pause();animation.currentTime=320;}
	f.states[1]='future';f.states[2]='expired';d.send('refresh');f.reply();
	check(w.getComputedStyle(d.doors.get(2).querySelector('.advent-calendar-number')).color!==w.getComputedStyle(d.doors.get(4).querySelector('.advent-calendar-number')).color,'unavailable number stays bright');
	check(w.getComputedStyle(d.doors.get(24)).outlineColor==='rgb(255, 255, 255)','today outline changed');
	for(const [width,height]of [[390,844],[320,568],[844,390],[640,360],[1024,768]]){
		frame.style.width=width+'px';frame.style.height=height+'px';await settle();
		const rewards=d.previews.get(24),reward=rewards.firstChild,slot=reward.firstChild,icon=slot.querySelector('.item-slot-icon');
		check(Math.abs(icon.getBoundingClientRect().width-32)<.1,'Christmas Eve icon is not native size at '+width);
		check(reward.getBoundingClientRect().width===38,'reward slot does not reserve full icon at '+width);
		check(rewards.scrollWidth<=rewards.clientWidth,'reward spills horizontally at '+width);
		if(width<1000)check(rewards.scrollHeight>rewards.clientHeight,'large bundle cannot scroll at '+width);
		check(d.doors.get(24).getAttribute('aria-label').includes('monety × 25000'),'exact accessible quantity missing');
		const label=slot.querySelector('.item-slot-quantity').getBoundingClientRect(),box=reward.getBoundingClientRect();
		check(label.left>=box.left&&label.right<=box.right+.1,'quantity label clipped at '+width);
		if(height<500)check(d.componentElement.querySelector('.dialogbutton').getBoundingClientRect().height===28,'landscape Close height changed');
	}
	result.textContent='PASS animation;reduced='+reduced+';native icons;quantity labels;scrolling;unavailable numbers;white outline;compact Close';
}catch(error){result.textContent='FAIL '+error.stack;}};
frame.srcdoc=${JSON.stringify(browserFixture()).replaceAll("<", "\\u003c")};
</script>`);
		for (const flags of [[], ["--force-prefers-reduced-motion"]]) {
			const output = execFileSync(edge, ["--headless=new", "--disable-gpu", "--no-first-run", "--allow-file-access-from-files", ...flags,
				`--user-data-dir=${path.join(directory, flags.length ? "reduced-profile" : "profile")}`, "--dump-dom", "--virtual-time-budget=2500", pathToFileURL(filename).href],
				{encoding: "utf8", windowsHide: true, timeout: 20000, stdio: ["ignore", "pipe", "ignore"]});
			assert.match(output.match(/<pre id="result">([^<]*)<\/pre>/)?.[1] || "missing browser result", /^PASS animation;/);
			assert.ok(output.includes('PASS animation;reduced=' + Boolean(flags.length)), "browser did not apply the requested motion preference");
		}
	} finally {
		if (directory.startsWith(path.join(tmpdir(), "polanie-advent-opening-"))) rmSync(directory, {recursive: true, force: true, maxRetries: 5, retryDelay: 100});
	}
});
