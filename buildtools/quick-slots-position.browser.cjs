/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const esbuild = require('esbuild');
const {chromium} = require(process.env.STENDHAL_PLAYWRIGHT || 'playwright');
const root = path.resolve(__dirname, '..');

(async () => {
 const mocks = {
  '../toolkit/Component': 'export class Component { constructor(element) { this.componentElement=element; } }',
  '../../stendhal': 'export const stendhal={ui:{equip:{getInventory:()=>[]}}};',
  'marauroa': 'export const marauroa={};',
  '../dialog/ActionContextMenu': 'export class ActionContextMenu {}',
  '../UI': 'export const ui={};',
  '../../entity/Item': 'export class Item {}',
  '../component/ItemContainerImplementation': 'export const ItemContainerImplementation={updateCursorFor(){},updateToolTipFor(){}};',
  '../../SingletonRepo': 'export const singletons={};',
  '../../data/Paths': 'export const Paths={gui:"/data/gui"};',
  '../../util/Chat': 'export const Chat={};',
  '../../util/GameLanguage': 'export const GameLanguage={};',
  '../../util/ConfigManager': 'export const ConfigManager={get:()=>({get:()=>"[null,null,null]",set(){}})};'
 };
 const script = (await esbuild.build({stdin:{contents:`
  import {QuickSlots} from './src/js/stendhal/ui/interaction/QuickSlots';
  window.quickSlots=new QuickSlots(); window.quickSlots.mount();
 `,resolveDir:root,loader:'ts'},bundle:true,write:false,plugins:[{name:'offline-boundaries',setup(b){
  b.onResolve({filter:/.*/},a=>Object.hasOwn(mocks,a.path)?{path:a.path,namespace:'fixture'}:undefined);
  b.onLoad({filter:/.*/,namespace:'fixture'},a=>({contents:mocks[a.path]}));
 }}]})).outputFiles[0].text;
 const browser=await chromium.launch({executablePath:process.env.STENDHAL_BROWSER,headless:true});
 try {
  const page=await browser.newPage({viewport:{width:915,height:412},isMobile:true,hasTouch:true});
  await page.setContent('<meta name="viewport" content="width=device-width,initial-scale=1"><body style="margin:0"><div id="client"><canvas id="viewport" width="650" height="310"></canvas></div><div id="interaction-button-container"></div>');
  await page.addStyleTag({content:fs.readFileSync(path.join(root,'src/js/css/main.css'),'utf8')});
  await page.addStyleTag({content:'#client {display:block !important} #viewport {width:650px;height:310px;max-width:none;max-height:none}'});
  await page.addScriptTag({content:script});
  const settle=()=>page.evaluate(()=>new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r))));
  const alignment=()=>page.evaluate(()=>{
   const slot=document.querySelector('.quick-slot'),button=document.getElementById('attack-button');
   const r=button.getBoundingClientRect(),s=slot.getBoundingClientRect();
   const expectedX=r.left+r.width/2-12+Math.cos(-135*Math.PI/180)*(s.width+44)-s.width/2;
   const expectedY=r.top+r.height/2-s.height-2+Math.sin(-135*Math.PI/180)*(s.width+44)-s.height/2;
   return {dx:Math.abs(s.left-expectedX),dy:Math.abs(s.top-expectedY),slotX:s.left,buttonX:r.left};
  });
  await settle();
  // Login can mount quick slots before the attack controller creates its button.
  await page.evaluate(()=>{
   const button=document.createElement('button');button.id='attack-button';
   button.style.cssText='position:absolute;left:540px;top:225px';
   document.getElementById('interaction-button-container').appendChild(button);
  });
  await settle();
  let actual=await alignment();console.log('late attack mount',actual);
  assert.ok(actual.dx<1&&actual.dy<1,'Quick slots must anchor to an attack button mounted later, without a server packet');
  // A layout observer may move the attack button after the quick-slot observer.
  await page.evaluate(()=>{document.getElementById('attack-button').style.left='420px'});
  await settle();actual=await alignment();console.log('attack relocation',actual);
  assert.ok(actual.dx<1&&actual.dy<1,'Quick slots must follow attack relocation without a server packet');
  await page.evaluate(()=>{
   const old=document.getElementById('attack-button');const next=old.cloneNode(true);
   next.style.left='500px';old.replaceWith(next);
  });
  await settle();actual=await alignment();console.log('attack replacement',actual);
  assert.ok(actual.dx<1&&actual.dy<1,'Quick slots must rebind after attack-button replacement');
  await page.evaluate(()=>window.quickSlots.unmount());
  assert.equal(await page.locator('#quick-slots').count(),0);
  await page.evaluate(()=>{document.getElementById('attack-button').style.left='400px'});
  await settle();
  assert.equal(await page.locator('#quick-slots').count(),0,'Unmounted slots must stay detached');
 } finally {await browser.close()}
})().catch(error=>{console.error(error);process.exitCode=1});
