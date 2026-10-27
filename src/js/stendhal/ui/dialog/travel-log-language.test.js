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
 const exports={};
 vm.runInNewContext(compile("ui/dialog/TravelLogDialog.ts"),{exports,console,
  document:{createElement:()=>new Element()},require:name=>{assert.ok(modules[name],name);return modules[name];}});
 return {Dialog:exports.TravelLogDialog,language,list,details};
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
 assert.ok(view.innerHTML.includes("Opis &lt;script&gt;"));
 assert.equal(view.children[0].children[0].innerHTML,"&lt;script&gt;Witaj.&lt;/script&gt;");
 assert.deepEqual(history,["<script>Witaj.</script>"]);
 language.synchronize("pl"); dialog.itemData("","Zamknij","Opis",[]);
 view=details.children.at(-1);
 assert.ok(view.innerHTML.includes("Mogę wykonać to zadanie jeszcze raz."));
});
