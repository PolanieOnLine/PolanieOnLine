"use strict";
const assert = require("node:assert/strict");
const {readFileSync,existsSync} = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {test} = require("node:test");
const ts = require("typescript");
const repoRoot = path.resolve(__dirname, "../../..");
const compile = file => ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
 compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
}).outputText;
async function harness() {
 const languageModule = {};
 vm.runInNewContext(compile("util/GameLanguage.ts"), {exports: languageModule, console,
  fetch: async url => ({ok:true,text:async()=>readFileSync(path.join(repoRoot,"data/languages",path.basename(url)),"utf8")})});
 const language = languageModule.GameLanguage;
 await language.load("/data"); language.synchronize("en");
 const logs=[], actions=[], values=new Map();
 const stendhal = {config:{getBoolean:key=>values.get(key)||false,set:(key,value)=>values.set(key,value)},
  sound:{toggleMute:()=>true,getLayerNames:()=>["music","Zamknij"],
   getVolume:name=>name==="master"?0.5:0.75,setVolume:()=>true}};
 const modules = {
  "marauroa":{marauroa:{clientFramework:{sendAction:action=>actions.push(action)},me:{}}},
  "./stendhal":{stendhal},
  "./util/Chat":{Chat:{log:(type,text)=>logs.push({type,text})}},
  "./util/GameLanguage":{GameLanguage:language},
  "./util/Debug":{Debug:{isActive:()=>false}}
 };
 const cache=new Map();
 function dependency(name,from=__dirname) {
  if(modules[name])return modules[name];
  if(name.startsWith(".")) {
   const filename=path.resolve(from,name)+".ts";
   if(filename.startsWith(path.join(__dirname,"action")+path.sep)&&existsSync(filename)) {
    if(cache.has(filename))return cache.get(filename);
    const result={};cache.set(filename,result);
    vm.runInNewContext(compile(path.relative(__dirname,filename)),{exports:result,console,require:n=>dependency(n,path.dirname(filename))});
    return result;
   }
  }
  return new Proxy({}, {get:()=>class {}});
 }
 const exports={};
 vm.runInNewContext(compile("SlashActionRepo.ts"), {exports,console,
  require:name=>dependency(name)});
 return {repo:exports.SlashActionRepo.get(),language,logs,actions,stendhal};
}
test("continuous movement keeps its action and colour while translating its local notice", async()=>{
 const {repo,language,logs,actions}=await harness();
 repo.movecont.execute("movecont",[],"");
 assert.equal(logs.at(-1).text,"Continuous movement enabled.");
 assert.equal(logs.at(-1).type,"info");
 assert.equal(actions[0].type,"move.continuous");
 assert.equal(actions[0]["move.continuous"],"");
 repo.movecont.execute("movecont",[],"");
 assert.equal(logs.at(-1).text,"Continuous movement disabled.");
 language.synchronize("pl");
 repo.movecont.execute("movecont",[],"");
 assert.equal(logs.at(-1).text,"Ciągły ruch włączony.");
});
test("the player help command translates descriptions before adding indentation",async()=>{
 const {repo,logs}=await harness();
 repo.help.execute("help",[],"");
 const missing=logs.map(line=>line.text).filter(text=>/[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/.test(text));
 assert.deepEqual(missing,[]);
 assert.ok(logs.some(line=>line.text==="&nbsp;&nbsp;Show the help message."));
});
test("the administrator help command also translates its inline descriptions",async()=>{
 const {repo,logs}=await harness();
 repo.gmhelp.execute("gmhelp",[],"");
 assert.deepEqual(logs.map(line=>line.text).filter(text=>/[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/.test(text)),[]);
});
test("detailed alter help translates prose without changing the command example",async()=>{
 const {repo,logs}=await harness();
 repo.gmhelp.execute("gmhelp",["alter"],"");
 assert.deepEqual(logs.map(line=>line.text).filter(text=>/[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/.test(text)),[]);
 assert.ok(logs.some(line=>line.text.includes("/alter testplayer outfit set 12109901")));
});
test("script help translates explanations while retaining file names and command tokens",async()=>{
 const {repo,logs}=await harness();
 repo.gmhelp.execute("gmhelp",["script"],"");
 assert.deepEqual(logs.map(line=>line.text).filter(text=>/[ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/.test(text)),[]);
 assert.ok(logs.some(line=>line.text.includes("#/script #ServerReset.class")));
});

test("volume help translates its instructions without renaming sound channels",async()=>{
 const {repo,language,logs,actions}=await harness();
 repo.volume.execute("volume",[],"");
 assert.ok(logs.some(line=>line.text==='<layer> is one of "master", "music", "Zamknij"'));
 assert.ok(logs.some(line=>line.text==='<value> is a number from 0 to 100.'));
 assert.ok(logs.some(line=>line.text==='Current volume levels:'));
 assert.ok(logs.some(line=>line.text==='&nbsp;&nbsp;- Zamknij -> 75'));
 assert.equal(actions.length,0);
 language.synchronize("pl"); logs.length=0;
 repo.volume.execute("volume",[],"");
 assert.ok(logs.some(line=>line.text==='Aktualne poziomy głośności:'));
});

test("volume confirmation preserves the channel token and numeric level",async()=>{
 const {repo,logs}=await harness();
 repo.volume.execute("volume",["Zamknij","75"],"");
 assert.equal(logs.at(-1).text,'Channel "Zamknij" set to 75.');
 assert.equal(logs.at(-1).type,"client");
});

test("group help contains English descriptions for the actual advertised parameter names",async()=>{
 const {repo,logs,language,actions}=await harness();
 repo.help.execute("help",[],"");
 for(const expected of ["Invite a player to your group.","Accept #player's group invitation.","Make #player the group leader.","Kick #player from the group."])
  assert.ok(logs.some(line=>line.text==="&nbsp;&nbsp;"+expected),expected);
 assert.ok(logs.some(line=>line.text.includes("group join <player>")));
 repo.group.execute("group",["invite"],"Zamknij");
 assert.equal(actions.at(-1).type,"group_management");assert.equal(actions.at(-1).action,"invite");assert.equal(actions.at(-1).params,"Zamknij");
 language.synchronize("pl");logs.length=0;repo.help.execute("help",[],"");
 assert.ok(logs.some(line=>line.text==="&nbsp;&nbsp;Zaproś gracza do swojej grupy."));
});
