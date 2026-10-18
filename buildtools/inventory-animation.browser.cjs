"use strict";
// Opt-in real-browser regression/benchmark. No game server or player account required.
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const {execFileSync} = require("node:child_process");
const esbuild = require("esbuild");
const {chromium} = require(process.env.STENDHAL_PLAYWRIGHT || "playwright");
const root = path.resolve(__dirname, "..");
const baseline = execFileSync("git", ["show", (process.env.STENDHAL_INVENTORY_BASELINE || "54cb308c3") + ":src/js/stendhal/ui/component/ItemContainerImplementation.ts"], {cwd: root, encoding: "utf8"});
const entry = `
import {ItemContainerImplementation} from './src/js/stendhal/ui/component/ItemContainerImplementation';
import {ItemRarity} from './src/js/stendhal/data/ItemRarity';
window.startScene = async () => {
 const frameCount=${Number(process.env.STENDHAL_INVENTORY_FRAMES || 8)};
 const source = document.createElement('canvas'); source.width = frameCount*32; source.height = 64;
 const ctx = source.getContext('2d');
 for (let state=0;state<2;state++) for (let f=0;f<frameCount;f++) {
  ctx.fillStyle=state?'#b08020':'#40c0e0'; ctx.fillRect(f*32+8,8+state*32,12,16);
  ctx.fillStyle='#ffffff'; ctx.fillRect(f*32+5+f,12+state*32,4,4);
 }
 const image = new Image(); image.src = source.toDataURL(); await image.decode();
 window.images.set('sprite.png', image);
 const parent = document.createElement('div'); parent.style.cssText='width:228px;display:flow-root'; document.body.appendChild(parent);
 const items=[];
 for(let i=0;i<72;i++) {
  const slot=document.createElement('div'); slot.id='bag'+i; slot.className='itemSlot'; parent.appendChild(slot);
  const filename='/sprites/items/sword/test_'+i+'.png';
  const distinctImage=new Image();distinctImage.src=image.src;await distinctImage.decode();window.images.set(filename,distinctImage);
  items.push({name:'Item '+i, class:'sword', subclass:'test_'+i, sprite:{filename}, frame:0, state:i%2,
   getXFrameIndex(){return this.frame},getYFrameIndex(){return this.state},isAnimated(){return true},
   stepAnimation(now){this.frame=Math.floor(now/100)%frameCount},formatQuantity(){return '12'},
   getRarity(){return [ItemRarity.RARE,ItemRarity.EPIC,ItemRarity.LEGENDARY][i%3]},getToolTip(){return this.name},getDisplayName(){return this.name},getCursor(){return 'auto'} });
 }
 const owner={bag:{count:()=>items.length,getByIndex:i=>items[i]}};
 const container=new ItemContainerImplementation(parent,'bag',72,owner,'',false);
 window.scene={container,items,parent,frameCount}; window.run=true;
 const tick=now=>{if(!window.run)return;container.animate(now);requestAnimationFrame(tick)}; requestAnimationFrame(tick);
};`;

async function bundle(old) {
 return (await esbuild.build({stdin:{contents:entry,resolveDir:root,loader:"ts"},bundle:true,write:false,format:"iife",plugins:[{
  name:"offline-game-boundaries",setup(build){
   build.onResolve({filter:/^(marauroa)$|SingletonRepo$|AdminDiagnostics$|\/stendhal$|\/UI$|\/dialog\/(ActionContextMenu|DropQuantitySelectorDialog|ItemUpgradeDialog)$|\/entity\/Item$|GameLanguage$|\/Paths$/},args=>({path:args.path,namespace:"fixture"}));
   build.onLoad({filter:/.*/,namespace:"fixture"},args=>({contents:
    /Paths$/.test(args.path)?`export const Paths={sprites:'/sprites',gui:'/data/gui'};`:
    /SingletonRepo$/.test(args.path)?`export const singletons={getSpriteStore:()=>({get:name=>window.images.get(name),checkPath:name=>window.images.get(name).src}),getConfigManager:()=>({getBoolean:()=>false})};`:
    /AdminDiagnostics$/.test(args.path)?`export const AdminDiagnostics={get:()=>false,getRevision:()=>0,inventory(){},removeInventory(){}};`:
    /GameLanguage$/.test(args.path)?`export const GameLanguage={getText:s=>s};`:
    /marauroa/.test(args.path)?`export const marauroa={};`:
    /\/stendhal$/.test(args.path)?`export const stendhal={config:{getBoolean:()=>false}};`:
    /\/UI$/.test(args.path)?`export const ui={};`:
    /\/Item$/.test(args.path)?`export class Item {}`:
    `export class ActionContextMenu {} export class DropQuantitySelectorDialog {} export class ItemUpgradeDialog {}`
   }));
   if(old) build.onLoad({filter:/ItemContainerImplementation\.ts$/},()=>({contents:baseline,loader:"ts"}));
  }
 }]})).outputFiles[0].text;
}

(async()=>{
 const browser=await chromium.launch({executablePath:process.env.STENDHAL_BROWSER,headless:true});
 try {
  const results=[]; const screenshots=[];
  for(const old of [true,false]) {
   const page=await browser.newPage({viewport:{width:412,height:915},deviceScaleFactor:1,isMobile:true,hasTouch:true});
   page.on('pageerror',error=>console.error('Browser:',error.message));
   const cdp=await page.context().newCDPSession(page);
   await cdp.send('Emulation.setCPUThrottlingRate',{rate:4});
   const texture=fs.readFileSync(path.join(root,'data/gui/slot.png')).toString('base64');
   await page.route('http://inventory.test/**',route=>route.fulfill(route.request().url().endsWith('/slot.png')
    ?{contentType:'image/png',body:Buffer.from(texture,'base64')}
    :{contentType:'text/html',body:'<html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head><body style="margin:0;background:#302014"></body></html>'}));
   await page.goto('http://inventory.test/');
   await page.addStyleTag({content:fs.readFileSync(process.env.STENDHAL_INVENTORY_CSS || path.join(root,'src/js/css/main.css'),'utf8').replaceAll('/data/gui/slot.png','data:image/png;base64,'+texture)});
   await page.evaluate(()=>window.images=new Map());
   await page.addScriptTag({content:await bundle(old)});
   await page.evaluate(()=>window.startScene());
   await page.waitForTimeout(1500); // finite warmup; this is a benchmark, not app synchronization
   const events=[];
   cdp.on('Tracing.dataCollected',data=>events.push(...data.value));
   await cdp.send('Tracing.start',{categories:'devtools.timeline',transferMode:'ReportEvents'});
   const frames=await page.evaluate(()=>new Promise(resolve=>{
    const intervals=[];let last;const start=performance.now();
    function tick(now){if(last)intervals.push(now-last);last=now;if(now-start<2500)requestAnimationFrame(tick);else resolve(intervals)}requestAnimationFrame(tick);
   }));
   const done=new Promise(resolve=>cdp.once('Tracing.tracingComplete',resolve));
   await cdp.send('Tracing.end');await done;
   const paints=events.filter(e=>e.name==='Paint');
   results.push({mode:old?'baseline':'candidate',paintEvents:paints.length,paintMs:paints.reduce((n,e)=>n+(e.dur||0)/1000,0),fps:1000/(frames.reduce((a,b)=>a+b,0)/frames.length)});
   await page.evaluate(()=>{window.run=false;for(const item of window.scene.items)item.frame=Math.min(3,window.scene.frameCount-1);window.scene.container.render()});
   await page.waitForTimeout(200);
   screenshots.push(await page.screenshot());
   if (!old) {
    const checks=await page.evaluate(()=>{
     const slot=document.getElementById('bag0');
     const rect=slot.getBoundingClientRect();
     const hit=document.elementFromPoint(rect.left+19,rect.top+19)===slot;
     slot.dispatchEvent(new MouseEvent('mouseenter',{clientX:20,clientY:20}));
     const tooltip=document.getElementById('item-rarity-tooltip')?.textContent.includes('ITEM 0');
     slot.dispatchEvent(new MouseEvent('mouseleave'));
     const layers=document.querySelectorAll('.item-slot-animation').length;
     window.scene.items.splice(0);window.scene.container.render();
     const cleared=!document.querySelector('.item-slot-animation')&&!slot.querySelector('.item-slot-icon').style.visibility;
     window.scene.container.dispose();
     return {hit,tooltip,layers,cleared};
    });
    assert.deepEqual(checks,{hit:true,tooltip:true,layers:72,cleared:true});
   }
   await page.close();
  }
  console.log(JSON.stringify(results,null,2));
  fs.mkdirSync(path.join(root,'build/qa'),{recursive:true});
  fs.writeFileSync(path.join(root,'build/qa/inventory-animation-results.json'),JSON.stringify(results,null,2));
  screenshots.forEach((data,i)=>fs.writeFileSync(path.join(root,'build/qa/inventory-'+(i?'candidate':'baseline')+'.png'),data));
  const pixels=await browser.newPage();
  const visual=await pixels.evaluate(async urls=>{
   const data=[];
   for(const url of urls){const img=new Image();img.src=url;await img.decode();const canvas=document.createElement('canvas');canvas.width=img.width;canvas.height=img.height;const ctx=canvas.getContext('2d');ctx.drawImage(img,0,0);data.push(ctx.getImageData(0,0,img.width,img.height).data)}
   let different=0;for(let i=0;i<data[0].length;i+=4){if(data[0].subarray(i,i+4).some((v,n)=>v!==data[1][i+n]))different++}
   const center=(22*412+15)*4;
   return {differentPixels:different,iconPixels:data.map(buffer=>Array.from(buffer.slice(center,center+4)))};
  },screenshots.map(data=>'data:image/png;base64,'+data.toString('base64')));
  console.log(visual);await pixels.close();
  // Identical screenshots are insufficient: both renderers could be blank.
  for(const color of visual.iconPixels) assert.deepEqual(color,[64,192,224,255],'The actual icon pixels must be visible, not only slot outlines/quantities');
  assert.equal(visual.differentPixels,0,'Icon state, outlines, slot geometry and quantity must remain pixel-identical');
  assert.ok(results[0].paintEvents>50,'Baseline must exercise actual repainting');
  assert.ok(results[1].paintEvents<results[0].paintEvents/4,'Animation should eliminate repeated slot paints after warmup');
 } finally {await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
