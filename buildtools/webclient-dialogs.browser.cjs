const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const esbuild = require('esbuild');
const {chromium} = require(process.env.STENDHAL_PLAYWRIGHT || 'playwright');

(async () => {
 const browser = await chromium.launch({executablePath: process.env.STENDHAL_BROWSER, headless:true});
 try {
  const page = await browser.newPage();
  const errors=[];page.on('pageerror',error=>errors.push(error.message));
  const html = fs.readFileSync('src/js/polanieonline.html','utf8');
  await page.setContent(html.replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi,'').replace(/<link\b[^>]*>/gi,''));
  const bundle = await esbuild.build({stdin:{contents:'export * from "./src/js/stendhal/ui/dialog/settings/SettingsCategoryLayout"; export * from "./src/js/stendhal/ui/dialog/CharacterCard";',resolveDir:process.cwd()},bundle:true,write:false,format:'iife',globalName:'Dialogs'});
  await page.addScriptTag({content:bundle.outputFiles[0].text});
  await page.addStyleTag({content:fs.readFileSync('src/js/css/main.css','utf8')});
  await page.addStyleTag({content:`:root{--background-url:url(data:image/jpeg;base64,${fs.readFileSync('data/gui/panel_wood.jpg').toString('base64')})}body{margin:8px}.settings-dialog,.character-selection{padding:8px;box-sizing:border-box}`});
  await page.evaluate(()=>document.head.append(...document.querySelectorAll('template')));
  const result = await page.evaluate(() => {
   const root = document.querySelector('#settingsdialog-template').content.firstElementChild.cloneNode(true);
   document.body.replaceChildren(root);
   const input = root.querySelector('#txtchatopts'); input.value='saved';
   let changes=0; input.addEventListener('change',()=>changes++);
   const pages = Dialogs.organizeSettings(root);
   root.append(...pages.map(p=>p.element));
   input.dispatchEvent(new Event('change'));
   const attrs={name:'<Mithril>',level:'260',atk:'79',def:'118',outfit:123};
   let selected=''; const preview=document.createElement('canvas');
   const card=Dialogs.createCharacterCard(attrs,preview,name=>selected=name,text=>text);
   document.body.append(card);card.click();
   const fallback=Dialogs.createCharacterCard({name:'New'},document.createElement('canvas'),()=>{},text=>text);
   return {labels:pages.map(p=>p.label),value:input.value,changes,chatPage:input.closest('.settings-category').dataset.category,
    unique:root.querySelectorAll('#txtchatopts').length,card:card.textContent,selected,unsafe:card.querySelector('mithril')!==null,
    canvas:card.querySelectorAll('canvas').length,fallback:fallback.textContent,unchanged:attrs.outfit};
  });
  assert.deepEqual(result.labels,['Sterowanie','Obraz','Wygląd','Tekst i powiadomienia','Walka i przedmioty','Dźwięk']);
  assert.equal(result.value,'saved');assert.equal(result.changes,1);assert.equal(result.unique,1);assert.equal(result.chatPage,'text');
  assert.match(result.card,/260/);assert.match(result.card,/79\/118/);assert.equal(result.selected,'<Mithril>');assert.equal(result.unsafe,false);assert.equal(result.canvas,1);assert.match(result.fallback,/0\/0/);assert.equal(result.unchanged,123);
  console.log('Settings preserve bound controls; character cards preserve names, stats and selection.');
  const stubs = {
   'stendhal.ts': 'export const stendhal=window.fixture.stendhal;',
   'SingletonRepo.ts': 'export const singletons=window.fixture.singletons;',
   'ConfigManager.ts': 'export class ConfigManager {static get(){return window.fixture.config}}',
   'UI.ts': 'export const ui={get:()=>window.fixture.ui,onMenuUpdate(){}};',
   'Globals.ts': 'export const Globals={getMenuStyle:()=>"traditional"};',
   'Debug.ts': 'export const Debug={isActive:()=>false};',
   'StandardMessages.ts': 'export const StandardMessages={changeNeedsRefresh(){}};',
   'AdminDiagnostics.ts': 'export const AdminDiagnostics={isAdministrator:()=>window.fixture.admin};',
   'AdminDiagnosticsTab.ts': 'import {Component} from "../../toolkit/Component"; export class AdminDiagnosticsTab extends Component {constructor(){super(document.createElement("div"))}}',
   'InventoryWindowOrderManager.ts': 'export const InventoryWindowOrderManager={get:()=>({isPersonalizationEnabled:()=>false,setPersonalizationEnabled(){},reset(){}})};',
   'Client.ts': 'export const Client={get:()=>({chooseCharacter:name=>window.fixture.selected=name})};',
   'SlashActionRepo.ts': 'export const SlashActionRepo={get:()=>({execute:action=>window.fixture.action=action})};'
  };
  const integration = await esbuild.build({stdin:{contents:'export {SettingsDialog} from "./src/js/stendhal/ui/dialog/SettingsDialog"; export {ChooseCharacterDialog} from "./src/js/stendhal/ui/dialog/ChooseCharacterDialog"; export {GameLanguage} from "./src/js/stendhal/util/GameLanguage";',resolveDir:process.cwd()},bundle:true,write:false,format:'iife',globalName:'RealDialogs',plugins:[{name:'boundary-fixtures',setup(build){
   build.onResolve({filter:/^marauroa$/},()=>({path:'marauroa',namespace:'fixture'}));
   build.onLoad({filter:/.*/,namespace:'fixture'},()=>({contents:'export const marauroa={clientFramework:{sendAction:action=>window.fixture.sent=action}};'}));
   build.onLoad({filter:/\.ts$/},args=>{const contents=stubs[path.basename(args.path)];return contents?{contents,loader:'ts',resolveDir:path.dirname(args.path)}:undefined;});
  }}]});
  await page.setContent(html.replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi,'').replace(/<link\b[^>]*>/gi,''));
  await page.addStyleTag({content:fs.readFileSync('src/js/css/main.css','utf8')});
  await page.addStyleTag({content:`:root{--background-url:url(data:image/jpeg;base64,${fs.readFileSync('data/gui/panel_wood.jpg').toString('base64')})}body{margin:8px}.settings-dialog,.character-selection{padding:8px;box-sizing:border-box}`});
  await page.evaluate(()=>document.head.append(...document.querySelectorAll('template')));
  await page.evaluate(() => {
   const values={'joystick.center.x':'264','joystick.center.y':'384','joystick.autoposition':'true','viewport.tileScale.mobile':'0.9','chat-opts.custom':'hello','font.body':'Carlito','joystick.style':'joystick','menu.style':'traditional'};
   const config={get:key=>values[key]??'false',set:(key,value)=>values[key]=String(value),getBoolean:key=>values[key]==='true',getInt:key=>Number(values[key])||0,
    fonts:{Carlito:'Carlito'},getThemesMap:()=>({wood:''}),getTheme:()=> 'wood',getFontsMap:()=>({Carlito:''}),getOpts:key=>key==='joystick.style'?{joystick:'Joystick'}:{traditional:'Tradycyjny'},getList:()=>[]};
   const noops=new Proxy({}, {get:()=>()=>{}});
   const controller={update(){},joystickEnabled:()=>false,attackButtonEnabled:()=>false,lootButtonEnabled:()=>config.getBoolean('loot.button'),getVolume:()=>0.5};
   window.fixture={config,values,admin:false,ui:noops,stendhal:{config,data:{outfit:{getLayerNames:()=>['body','dress','head','hair'],detailHasRearLayer:()=>false,isSkinLayer:()=>false}}}};
   window.fixture.singletons=new Proxy({getConfigManager:()=>config},{get:(target,key)=>target[key]||(()=>controller)});
  });
  await page.addScriptTag({content:integration.outputFiles[0].text});
  await page.evaluate(() => {window.settings=new RealDialogs.SettingsDialog();document.body.replaceChildren(settings.componentElement);});
  assert.equal(await page.locator('.tabpanel-tabs button').count(),6);
  for (let i=0;i<6;i++) {await page.locator('.tabpanel-tabs button').nth(i).click();assert.equal(await page.locator('.settings-category:visible').count(),1);}
  assert.equal(await page.locator('#chk_loot_button').count(),1,'Loot button setting must be available');
  assert.equal(await page.locator('#chk_joystick_auto').count(),1,'Joystick auto-position setting must be available');
  await page.locator('.tabpanel-tabs button').nth(4).click();
  await page.locator('#chk_loot_button').click();
  assert.equal(await page.evaluate(()=>fixture.values['loot.button']),'true');
  assert.equal(await page.locator('#chk_loot_button').evaluate(el=>el.closest('.settings-category').dataset.category),'combat');
  await page.locator('.tabpanel-tabs button').nth(0).click();
  assert.equal(await page.locator('#txtjoystickx').isDisabled(),true);
  assert.equal(await page.locator('#txtjoysticky').isDisabled(),true);
  await page.locator('#chk_joystick_auto').click();
  assert.equal(await page.evaluate(()=>fixture.values['joystick.autoposition']),'false');
  assert.equal(await page.locator('#txtjoystickx').isEnabled(),true);
  assert.equal(await page.locator('#txtjoysticky').isEnabled(),true);
  await page.locator('#txtjoystickx').fill('123');
  assert.equal(await page.evaluate(()=>fixture.values['joystick.center.x']),'123');
  await page.locator('#chk_joystick_auto').click();
  assert.equal(await page.locator('#txtjoystickx').isDisabled(),true);
  await page.locator('.tabpanel-tabs button').nth(3).click();
  await page.locator('#txtchatopts').fill('remember me');await page.locator('#txtchatopts').dispatchEvent('change');
  assert.equal(await page.evaluate(()=>fixture.values['chat-opts.custom']),'remember me');
  await page.locator('.tabpanel-tabs button').nth(1).click();
  await page.locator('#chk_light').click();
  assert.equal(await page.evaluate(()=>fixture.values['effect.lighting']),'true');
  assert.equal(await page.locator('#button-reload').isEnabled(),true);
  await page.evaluate(()=>{fixture.admin=true;document.body.replaceChildren(new RealDialogs.SettingsDialog().componentElement)});
  assert.equal(await page.locator('.tabpanel-tabs button').count(),7);
  assert.equal(await page.locator('#txtchatopts').inputValue(),'remember me');
  assert.equal(await page.locator('#chk_loot_button').getAttribute('aria-checked'),'true');
  assert.equal(await page.locator('#chk_joystick_auto').getAttribute('aria-checked'),'true');
  assert.equal(await page.locator('#txtjoystickx').inputValue(),'123');
  assert.equal(await page.locator('#txtjoystickx').isDisabled(),true);
  await page.setViewportSize({width:390,height:844});
  for(let i=0;i<7;i++) {
   await page.locator('.tabpanel-tabs button').nth(i).click();
   assert.ok(await page.evaluate(()=>{const el=document.querySelector('.settings-dialog');return el.getBoundingClientRect().right<=innerWidth && el.scrollWidth<=el.clientWidth+1;}),'Settings fit phone width');
  }
  fs.mkdirSync('build/qa',{recursive:true});
  await page.locator('.tabpanel-tabs button').nth(0).click();
  await page.screenshot({path:'build/qa/webclient-settings-mobile.png'});
  await page.setViewportSize({width:1000,height:800});
  await page.screenshot({path:'build/qa/webclient-settings-desktop.png'});
  await page.route('https://fixture.invalid/data/languages/*',route=>{
   const name=new URL(route.request().url()).pathname.split('/').pop();
   route.fulfill({status:200,headers:{'Access-Control-Allow-Origin':'*'},body:fs.readFileSync(path.join('data/languages',name),'utf8')});
  });
  await page.evaluate(async()=>{await RealDialogs.GameLanguage.load('https://fixture.invalid/data');RealDialogs.GameLanguage.synchronize('en')});
  assert.equal(await page.locator('.tabpanel-tabs button').nth(0).innerText(),'Controls');
  assert.equal(await page.locator('.tabpanel-tabs button').nth(3).innerText(),'Text and notifications');
  assert.match(await page.locator('#chk_loot_button').evaluate(el=>el.closest('label').textContent),/Loot button/);
  assert.match(await page.locator('#chk_joystick_auto').evaluate(el=>el.closest('label').textContent),/Automatic joystick positioning/);
  console.log('Real settings dialog: all pages, admin gating, saved config callback and phone width passed.');
  const sprites=Object.fromEntries(['body/001','dress/015','head/001','hair/007'].map(name=>[name+'.png','data:image/png;base64,'+fs.readFileSync('data/sprites/outfit/'+name+'.png').toString('base64')]));
  await page.evaluate(sprites=>{
   const cache=new Map();
   const images=new Map();
   const store={getCached:key=>cache.get(key),cache:(key,value)=>cache.set(key,value),get:path=>{
    if(!images.has(path)){const image=new Image();image.src=sprites[path.split('/outfit/')[1]];images.set(path,image);}return images.get(path);
   }};
   fixture.singletons.getSpriteStore=()=>store;
   const characters={one:{a:{name:'karajuss',level:'260',atk:'79',def:'118',outfit_ext:'body=1,dress=15,head=1,hair=7'}},two:{a:{name:'new_character',outfit:7011501}}};
   document.body.replaceChildren(new RealDialogs.ChooseCharacterDialog(characters).componentElement);
  },sprites);
  await page.waitForFunction(()=>Array.from(document.querySelectorAll('.character-card canvas')).every(canvas=>canvas.getContext('2d').getImageData(0,0,48,64).data.some((v,i)=>i%4===3&&v>0)));
  assert.match(await page.locator('.character-card').first().innerText(),/Level: 260/);
  assert.match(await page.locator('.character-card').first().innerText(),/Skills: 79\/118/);
  await page.screenshot({path:'build/qa/webclient-characters-desktop.png'});
  await page.setViewportSize({width:390,height:844});
  const bounds=await page.locator('.character-card').evaluateAll(cards=>cards.map(el=>{const b=el.getBoundingClientRect();return {x:b.x,y:b.y,right:b.right}}));
  assert.equal(bounds[0].x,bounds[1].x);assert.ok(bounds[1].y>bounds[0].y);assert.ok(bounds[0].right<=390);
  await page.screenshot({path:'build/qa/webclient-characters-mobile.png'});
  await page.locator('.character-card').first().click();assert.equal(await page.evaluate(()=>fixture.selected),'karajuss');
  await page.locator('#logout').click();await page.waitForFunction(()=>fixture.action==='/logout');
  console.log('Real character dialog: asynchronously loaded sprite pixels, selection, logout and phone stacking passed.');
  assert.deepEqual(errors,[],'No uncaught browser errors');
 } finally {await browser.close();}
})().catch(error=>{console.error(error);process.exitCode=1;});
