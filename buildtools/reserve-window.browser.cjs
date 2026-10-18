/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const esbuild = require('esbuild');
const {chromium} = require(process.env.STENDHAL_PLAYWRIGHT || 'playwright');
const root = path.resolve(__dirname, '..');
(async () => {
 const mocks = {
  '../toolkit/Component': 'export class Component {constructor(id){this.componentElement=document.getElementById(id)} isVisible(){return true}}',
  './ItemContainerImplementation': 'export class ItemContainerImplementation {markDirty(){} update(){} animate(){}}',
  './InventoryWindowController': 'export const InventoryWindowController={setTitle(){}};',
  'marauroa': 'export const marauroa={me:{features:{}},currentZoneName:"test",clientFramework:{sendAction:action=>window.actions.push(action)}};'
 };
 const script = (await esbuild.build({stdin:{contents:`import {PlayerEquipmentComponent} from './src/js/stendhal/ui/component/PlayerEquipmentComponent';window.actions=[];window.equipment=new PlayerEquipmentComponent();`,resolveDir:root,loader:'ts'},bundle:true,write:false,plugins:[{name:'offline-boundaries',setup(b){
  b.onResolve({filter:/.*/},a=>Object.hasOwn(mocks,a.path)?{path:a.path,namespace:'fixture'}:undefined);
  b.onLoad({filter:/.*/,namespace:'fixture'},a=>({contents:mocks[a.path]}));
 }}]})).outputFiles[0].text;
 const html=fs.readFileSync(path.join(root,'src/js/polanieonline.html'),'utf8')
  .replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi,'').replace(/<link\b[^>]*>/gi,'');
 const browser=await chromium.launch({executablePath:process.env.STENDHAL_BROWSER,headless:true});
 try {
  for (const [width,height,mobile] of [[1366,768,false],[915,412,true],[412,915,true]]) {
   const page=await browser.newPage({viewport:{width,height},isMobile:mobile,hasTouch:mobile});
   await page.setContent(html);
   await page.addStyleTag({content:fs.readFileSync(path.join(root,'src/js/css/main.css'),'utf8')});
   await page.addStyleTag({content:'#client{display:flex;width:100vw;height:100vh}#leftColumn{display:none}#middleColumn{flex:1;min-width:0}#loginpopup{display:none}'});
   await page.evaluate(mobile=>{document.getElementById('client').classList.toggle('mobile-floating-ui',mobile)},mobile);
   await page.addScriptTag({content:script});
   if (mobile) await page.locator('#reserve-toggle').tap();
   else await page.locator('#reserve-toggle').click();
   await page.waitForFunction(()=>getComputedStyle(document.getElementById('reserve-window')).opacity==='1');
   const visible=await page.evaluate(()=>{
    const panel=document.getElementById('reserve-window'),r=panel.getBoundingClientRect();
    const swap=document.getElementById('equipment-swap'),s=swap.getBoundingClientRect();
    const hit=document.elementFromPoint(s.left+s.width/2,s.top+s.height/2);
    return {left:r.left,right:r.right,top:r.top,bottom:r.bottom,clickable:hit===swap||swap.contains(hit),expanded:document.getElementById('reserve-toggle').getAttribute('aria-expanded')};
   });
   console.log({width,height,mobile,...visible});
   assert.ok(visible.left>=0&&visible.right<=width&&visible.top>=0&&visible.bottom<=height,'Reserve equipment must stay on screen');
   assert.ok(visible.clickable,'Reserve equipment must not be clipped by the mobile right panel');
   assert.equal(visible.expanded,'true');
   if (mobile) await page.locator('#equipment-swap').tap();
   else await page.locator('#equipment-swap').click();
   assert.equal(await page.evaluate(()=>window.actions[0]?.type),'setchange');
   if (mobile) {
    await page.evaluate(()=>document.getElementById('client').classList.add('right-panel-collapsed'));
    assert.equal(await page.evaluate(()=>document.getElementById('reserve-window').getClientRects().length),0,
     'Collapsing the right panel must hide its reserve window too');
    await page.evaluate(()=>document.getElementById('client').classList.remove('right-panel-collapsed'));
   }
   await page.locator('#reserve-toggle').click();
   assert.equal(await page.locator('#reserve-window').getAttribute('aria-hidden'),'true');
   await page.close();
  }
 } finally {await browser.close()}
})().catch(error=>{console.error(error);process.exitCode=1});
