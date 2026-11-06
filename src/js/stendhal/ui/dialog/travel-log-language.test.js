"use strict";
const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require("typescript");
const root = path.resolve(__dirname,"../../../../..");
const compile = file => ts.transpileModule(readFileSync(path.join(root,"src/js/stendhal",file),"utf8"), {
 compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}
}).outputText;
async function harness() {
 const exportsLanguage={};
 vm.runInNewContext(compile("util/GameLanguage.ts"),{exports:exportsLanguage,console,
  fetch:async url=>({ok:true,text:async()=>readFileSync(path.join(root,"data/languages",path.basename(url)),"utf8")})});
 const language=exportsLanguage.GameLanguage; await language.load("/data"); language.synchronize("en");
 class Element {
  constructor() {this.innerHTML="";this.children=[];this.style={setProperty(){}};}
  appendChild(child){this.children.push(child);}
  append(...children){this.children.push(...children);}
  dispatchEvent() {}
 }
 const list=new Element(),details=new Element();
 class DialogContentComponent {
  constructor(){this.componentElement=new Element();}
  child(selector){return selector===".travellogitems"?list:details;}
 }
 const escape=value=>value.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;");
 const modules={
  "../../util/GameLanguage":{GameLanguage:language},
  "../../data/Paths":{Paths:{gui:"/gui"}},
  "../toolkit/DialogContentComponent":{DialogContentComponent},
  "../UI":{ui:{registerComponent(){},unregisterComponent(){}}},
  "../UIComponentEnum":{UIComponentEnum:{TravelLogDialog:1}},
  marauroa:{marauroa:{clientFramework:{sendAction(){}}}},
  "../../stendhal":{stendhal:{config:{get:()=>"sans-serif"},session:{touchOnly:()=>true},ui:{html:{esc:escape}}}}
 };
 const timers = new Set();
 const tooltips = new Map();
 const rewardModules = {
  "../../entity/Item":{Item:class {}},
  "./ItemContainerImplementation":{ItemContainerImplementation:{bindToolTip(target,item){
   tooltips.set(target,item);return ()=>tooltips.delete(target);
  }}},
  "../../util/GameLanguage":modules["../../util/GameLanguage"],
  "../../data/Paths":{Paths:{gui:"/gui",sprites:"/sprites"}},
  "../../SingletonRepo":{singletons:{getSpriteStore:()=>({checkPath:path=>path,get:()=>({width:64})})}},
  "./ItemSlotPresentation":{getItemSlotLayers:slot=>{
   const layers={icon:new Element(),quantityLabel:new Element()};
   slot.append(layers.icon,layers.quantityLabel);return layers;
  }}
 };
 const rewardExports={};
 vm.runInNewContext(compile("ui/component/QuestRewardsView.ts"),{
  exports:rewardExports,document:{createElement:()=>new Element()},
  setInterval:fn=>{timers.add(fn);return fn;},clearInterval:fn=>timers.delete(fn),
  require:name=>{assert.ok(rewardModules[name],name);return rewardModules[name];}
 });
 modules["../component/QuestRewardsView"]=rewardExports;
 const exports={};
 vm.runInNewContext(compile("ui/dialog/TravelLogDialog.ts"),{exports,console,
  Event:class {},document:{createElement:()=>new Element()},require:name=>{assert.ok(modules[name],name);return modules[name];}});
 return {Dialog:exports.TravelLogDialog,language,list,details,timers,tooltips};
}
test("travel log translates loading and empty states without changing its option value",async()=>{
 const {Dialog,language,list}=await harness(); const dialog=new Dialog();
 assert.equal(list.innerHTML,'<option value="">(loading)</option>');
 dialog.progressTypeData("",[]);
 assert.equal(list.innerHTML,'<option value="">(none)</option>');
 language.synchronize("pl"); new Dialog();
 assert.equal(list.innerHTML,'<option value="">(ładowanie)</option>');
});
test("repeatable quest note is translated while canonical title and escaped server content stay intact",async()=>{
 const {Dialog,language,details}=await harness(); const dialog=new Dialog();
 dialog.setRepeatable(["Zamknij"]);
 const history=["<script>Witaj.</script>"];
 dialog.itemData("","Zamknij","Opis <script>",history);
 let view=details.children.at(-1);
 assert.ok(view.innerHTML.includes("I can do this quest again."));
 assert.ok(view.innerHTML.includes("<h3>Zamknij</h3>"));
 assert.equal(view.children[0].textContent,"Opis <script>");
 assert.equal(view.children[1].children[0].innerHTML,"&lt;script&gt;Witaj.&lt;/script&gt;");
 assert.deepEqual(history,["<script>Witaj.</script>"]);
 language.synchronize("pl"); dialog.itemData("","Zamknij","Opis",[]);
 view=details.children.at(-1);
 assert.ok(view.innerHTML.includes("Mogę wykonać to zadanie jeszcze raz."));
});


test("main quests preserve the server's current-step order without mutating data",async()=>{
 const {Dialog,list,details}=await harness();const dialog=new Dialog();dialog.currentProgressType="Zadania główne";
 const names=["Spotkanie Pietrka","Kosa Fryderyka"];
 dialog.progressTypeData("Zadania główne",names);
 assert.ok(list.innerHTML.indexOf("Spotkanie Pietrka")<list.innerHTML.indexOf("Kosa Fryderyka"));
 assert.deepEqual(names,["Spotkanie Pietrka","Kosa Fryderyka"]);
 const history=["Aktualny cel: Porozmawiaj z Pietrkiem.","Historia zadania."];
 dialog.itemData("Zadania główne","Spotkanie Pietrka","Opis zadania",history);
 const view=details.children.at(-1);
 assert.ok(view.innerHTML.includes("Aktualny cel:"));
 assert.equal(view.children[0].textContent,"Opis zadania");
 assert.equal(view.children[1].children.length,1);
 assert.deepEqual(history,["Aktualny cel: Porozmawiaj z Pietrkiem.","Historia zadania."]);
});

test("quest rewards precede history, preserve quantity and guaranteed rarity, and stop animation on change",async()=>{
 const {Dialog,language,details,timers,tooltips}=await harness();
 language.synchronize("pl");
 const dialog=new Dialog();dialog.currentProgressType="Zadania główne";
 const rewards={reward_xp:10000,reward_coins:200,reward_completed:0,
  reward_names:["duży eliksir"],reward_classes:["drink"],reward_subclasses:["greater_potion"],
  reward_amounts:["5"],reward_rarities:["rare"],reward_descriptions:["Opis <script>"],
  reward_bindings:["1"],reward_notes:[],reward_stats:[JSON.stringify({heal:"300"})]};
 dialog.itemData("Zadania główne","Tytuł","Historia",["Aktualny cel: Wróć."],rewards);
 let view=details.children.at(-1);
 const section=view.children[0];
 assert.equal(section.className,"quest-rewards");
 assert.equal(section.children[0].textContent,"Nagrody");
 const card=section.children[2].children[0];
 assert.equal(card.children[0].children[1].textContent,"5");
 assert.equal(card.children[1].children[1].textContent,"Rzadki");
 assert.equal(card.title,undefined);
 assert.equal(tooltips.get(card).name,"duży eliksir");
 assert.equal(tooltips.get(card).rarity_id,"rare");
 assert.equal(tooltips.get(card).tooltip_stats.heal,"300");
 assert.equal(timers.size,1);
 dialog.itemData("Zadania główne","Inne zadanie","Historia",[]);
 assert.equal(timers.size,0);
 assert.equal(tooltips.size,0);
 assert.notEqual(details.children.at(-1).children[0].className,"quest-rewards");
 const wire=Object.fromEntries(Object.entries(rewards).map(([key,value])=>
  [key,Array.isArray(value)?"["+value.join("\t")+"]":String(value)]));
 dialog.itemData("Zadania główne","Ukończone","Historia",[],{...wire,reward_completed:"1"});
 assert.equal(details.children.at(-1).children[0].children[0].textContent,"Otrzymane nagrody");
 assert.equal(details.children.at(-1).children[0].children[2].children[0].children[0].children[1].textContent,"5");
 dialog.onParentClose();
 assert.equal(timers.size,0);
});

test("a quest with only XP and coins does not create an empty item slot",async()=>{
 const {Dialog,language,details,timers}=await harness();
 language.synchronize("pl");
 const dialog=new Dialog();dialog.currentProgressType="Zadania główne";
 dialog.itemData("Zadania główne","Dostawa, której nikt nie zamawiał","Opis",[],{
  reward_xp:"1200",reward_coins:"25",reward_completed:"0",reward_names:"[]",
  reward_classes:"[]",reward_subclasses:"[]",reward_amounts:"[]",reward_rarities:"[]",
  reward_descriptions:"[]",reward_bindings:"[]",reward_notes:"[]"
 });
 const section=details.children.at(-1).children[0];
 assert.equal(section.children[0].textContent,"Nagrody");
 assert.equal(section.children[1].children.length,2);
 assert.equal(section.children.length,2);
 assert.equal(timers.size,0);
});
