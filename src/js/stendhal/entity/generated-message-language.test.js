"use strict";
const assert=require("node:assert/strict"),{readFileSync}=require("node:fs"),path=require("node:path"),vm=require("node:vm");
const {test}=require("node:test"),ts=require("typescript");
const root=path.resolve(__dirname,"../../../..");
function load(file,deps={},globals={}) {
 const exports={};
 const compiled=ts.transpileModule(readFileSync(path.join(__dirname,file),"utf8"),{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText;
 vm.runInNewContext(compiled,{exports,console,require:name=>deps[name]||new Proxy({}, {get:()=>class {}}),...globals});
 return exports;
}
async function harness() {
 const language=load("../util/GameLanguage.ts",{}, {fetch:async url=>({ok:true,text:async()=>readFileSync(path.join(root,"data/languages",path.basename(url)),"utf8")})}).GameLanguage;
 await language.load("/data"); language.synchronize("en");
 const logs=[],floaters=[],marauroa={me:{_name:"Local",isInHearingRange:()=>true},clientFramework:{}};
 const stendhal={players:[],playerInGame:true,data:{group:{members:{}}}};
 const deps={"marauroa":{marauroa,RPEvent:class {}},"../stendhal":{stendhal},"stendhal":{stendhal},
  "../util/GameLanguage":{GameLanguage:language},"../../util/GameLanguage":{GameLanguage:language},"./util/GameLanguage":{GameLanguage:language},
  "../util/Chat":{Chat:{log:(type,text,orator)=>logs.push({type,text,orator}),logH:(type,text)=>logs.push({type,text})}},
  "../SingletonRepo":{singletons:{getEmojiStore:()=>({create:()=>null})}},
  "./ActiveEntity":{ActiveEntity:class {set(key,value){this[key]=value;}unset(key){delete this[key];}}}};
 deps["../../util/Chat"]=deps["../util/Chat"];
 const entity=new (load("RPEntity.ts",deps).RPEntity)();
 Object.assign(entity,{title:"Zamknij",gender:"f"});
 entity.addFloater=(text,color)=>floaters.push({text,color}); entity.addSpeechBubble=()=>{};
 return {language,logs,floaters,marauroa,stendhal,deps,entity};
}
test("experience and level notices translate without changing names, amounts or message colours",async()=>{
 const {entity,logs,floaters}=await harness();
 entity.onXPChanged(22);
 assert.equal(logs.at(-1).text,"Zamknij gained 22 experience points.");
 assert.equal(logs.at(-1).type,"significant_positive");
 assert.equal(floaters.at(-1).text,"+22");
 entity.onLevelChanged("atk",11,10);
 assert.equal(logs.at(-1).text,"Zamknij reached attack level 11");
 entity.onLevelChanged("level",9,10);
 assert.equal(logs.at(-1).text,"Zamknij dropped to level 9");
});

test("NPC invitations keep a separate message type and colour from experience",async()=>{
 const {deps,logs,stendhal,marauroa}=await harness();
 stendhal.config={get:()=>false};
 marauroa.clientFramework.lastPrivateChatter="Mroczny";
 const chat=deps["../util/Chat"];
 deps["util/Chat"]=chat;
 const PrivateTextEvent=load("../event/PrivateTextEvent.ts",deps).PrivateTextEvent;
 const event=new PrivateTextEvent();
 event.texttype="QUEST_INVITATION";
 event.text="Wieści od Drilenuna: Zajrzyj do ratusza w Semos.";
 event.execute();
 assert.equal(logs.at(-1).type,"quest_invitation");
 assert.equal(logs.at(-1).text,event.text);
 assert.equal(marauroa.clientFramework.lastPrivateChatter,"Mroczny");
 const {Color}=load("../data/color/Color.ts");
 const {NotificationType}=load("../util/NotificationType.ts",{"../data/color/Color":{Color}});
 assert.notEqual(NotificationType.quest_invitation,NotificationType.significant_positive);
 assert.notEqual(NotificationType.quest_invitation,NotificationType.privmsg);
 const css=readFileSync(path.join(root,"src/js/css/main.css"),"utf8");
 assert.match(css,/div\.logquest_invitation\s*\{[^}]*color:\s*rgb\(112,\s*217,\s*232\)/);
 assert.match(css,/span\.logh\s*\{[^}]*color:\s*rgb\(90,\s*170,\s*255\)/);
});
test("death translates its wrapper but never the attacker names or their internal conjunctions",async()=>{
 const {entity,logs}=await harness();
 entity.attackers.set("1",{getTitle:()=>"Witaj."});entity.attackers.set("2",{getTitle:()=>"Zamknij i Plecak"});
 entity.onDeath();
 assert.equal(logs.at(-1).text,"Zamknij was killed by Witaj. and Zamknij i Plecak");
});

test("creature death sentences start with a capital while player names retain their case", async () => {
	const {entity, logs} = await harness();
	entity.title = "giant eagle";
	entity.type = "creature";
	entity.attackers.set("1", {getTitle: () => "Mroczny"});
	entity.onDeath();
	assert.equal(logs.at(-1).text, "Giant eagle was killed by Mroczny");
	entity.title = "mroczny";
	entity.type = "player";
	entity.onDeath();
	assert.equal(logs.at(-1).text, "mroczny was killed by Mroczny");
});

test("only server-marked item emotes are translated, leaving player-authored text alone", async () => {
	const {deps} = await harness();
	const {TextEvent} = load("../event/TextEvent.ts", {...deps,
		"util/GameLanguage": deps["../util/GameLanguage"]});
	const heard = [];
	const player = {type: "player", say: text => heard.push(text)};
	const event = new TextEvent();
	event.text = "!me przytulił pluszowego misia.";
	event.execute(player);
	assert.equal(heard.at(-1), event.text);
	event.translate = "";
	event.execute(player);
	assert.equal(heard.at(-1), "!me hugged their teddy bear.");
});

test("bestiary sorts displayed English names and preserves kill flags and rarity groups", async () => {
	const {deps, stendhal} = await harness();
	class Node {
		constructor() {this.children = []; this.classList = {add() {}};}
		appendChild(node) {this.children.push(node);}
	}
	class Content {
		constructor() {this.componentElement = new Node();}
		setConfigId() {}
	}
	let shown;
	stendhal.ui = {globalInternalWindow: {set() {}}};
	const {BestiaryEvent} = load("../event/BestiaryEvent.ts", {...deps,
		"../ui/UI": {ui: {createSingletonFloatingWindow: (_title, content) => {shown = content;}}},
		"../ui/toolkit/DialogContentComponent": {DialogContentComponent: Content}},
		{document: {createElement: () => new Node()}});
	const event = new BestiaryEvent();
	event.enemies = "dzik,true,false;niedźwiedź,false,true;dzik (rzadki),true,false;niedźwiedź (rzadki),false,true";
	event.execute();
	const columns = shown.componentElement.children[0].children;
	assert.deepEqual(columns[0].children.slice(1).map(node => node.textContent),
		["bear", "boar", "bear (rare)", "boar (rare)"]);
	assert.deepEqual(columns[1].children.slice(1).map(node => node.textContent), ["", "✔", "", "✔"]);
	assert.deepEqual(columns[2].children.slice(1).map(node => node.textContent), ["✔", "", "✔", ""]);
});
test("away floaters translate but player chat remains exactly as written",async()=>{
 const {entity,logs,floaters}=await harness();
 entity.set("away","Zamknij");
 assert.equal(floaters.at(-1).text,"Away");assert.equal(entity.away,"Zamknij");
 entity.unset("away");assert.equal(floaters.at(-1).text,"Back");
 entity.say("Witaj.");assert.equal(logs.at(-1).text,"Witaj.");assert.equal(logs.at(-1).orator,"Zamknij");
});
test("online events translate notices, preserve canonical names and suppress self notifications",async()=>{
 const {logs,deps,marauroa,stendhal}=await harness();
 marauroa.me.buddies={"Zamknij":"false"};
 const on=new (load("../event/PlayerLoggedOnEvent.ts",deps).PlayerLoggedOnEvent)();on.name="Zamknij";on.execute();
 assert.equal(logs.at(-1).text,"Zamknij has joined PolanieOnLine.");assert.equal(stendhal.players[0],"Zamknij");
 const off=new (load("../event/PlayerLoggedOutEvent.ts",deps).PlayerLoggedOutEvent)();off.name="Zamknij";off.execute();
 assert.equal(logs.at(-1).text,"Zamknij has left PolanieOnLine.");assert.equal(stendhal.players.length,0);
 on.name=marauroa.me._name;on.execute();assert.equal(logs.length,2);
});
test("login callbacks translate account notices without translating a raw server detail",async()=>{
 const {deps,marauroa}=await harness(); const alerts=[];
 const Client=load("../Client.ts",deps,{document:{getElementById:()=>null},alert:text=>alerts.push(text),window:{location:{reload:()=>{}}}}).Client;
 const client=Client.get();client.registerMarauroaEventHandlers();
 marauroa.clientFramework.onCreateAccountAck("Zamknij");
 assert.equal(alerts[0],"Your account was created successfully. Please log in.");
 marauroa.clientFramework.onLoginFailed("test","Witaj.");
 assert.equal(alerts[1],"Login failed. Witaj.");
});
test("group errors translate without sending an action or changing group membership",async()=>{
 const {deps,logs,stendhal}=await harness();
 const Type=load("../ui/component/GroupPanelComponent.ts",deps).GroupPanelComponent;
 const group=Object.create(Type.prototype);
 group.onGroupPartButtonClick();
 assert.equal(logs.at(-1).text,"You cannot leave the group because you are not a member.");
 assert.equal(Object.keys(stendhal.data.group.members).length,0);
});
