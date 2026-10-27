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
test("death translates its wrapper but never the attacker names or their internal conjunctions",async()=>{
 const {entity,logs}=await harness();
 entity.attackers.set("1",{getTitle:()=>"Witaj."});entity.attackers.set("2",{getTitle:()=>"Zamknij i Plecak"});
 entity.onDeath();
 assert.equal(logs.at(-1).text,"Zamknij was killed by Witaj. and Zamknij i Plecak");
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
