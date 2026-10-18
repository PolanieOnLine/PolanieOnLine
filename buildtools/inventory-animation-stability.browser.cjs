const fs=require('node:fs');
const path=require('node:path');
const assert=require('node:assert/strict');
const esbuild=require('esbuild');
const {chromium}=require(process.env.STENDHAL_PLAYWRIGHT||'playwright');
const root=path.resolve(__dirname,'..');
const frameCount=Number(process.env.STENDHAL_INVENTORY_FRAMES||18);
const entry=`
import {ItemSlotAnimation} from './src/js/stendhal/ui/component/ItemSlotAnimation';
window.setup=async()=>{
 const source=document.createElement('canvas');source.width=${frameCount}*32;source.height=32;
 const ctx=source.getContext('2d');
 for(let f=0;f<${frameCount};f++){ctx.fillStyle='#40c0e0';ctx.fillRect(f*32+7,5,17,22);ctx.fillStyle='#fff';ctx.fillRect(f*32+7,5,1,22);ctx.fillRect(f*32+10,10,6,6)}
 const image=new Image();image.src=source.toDataURL();await image.decode();window.source=image;
 const grid=document.createElement('div');grid.style.cssText='width:228px;position:absolute;left:37.25px;top:5.5px';document.body.appendChild(grid);
 const entries=Array.from({length:24},()=>{
  const slot=document.createElement('div');slot.className='itemSlot';slot.style.setProperty('--item-slot-texture','none');grid.appendChild(slot);
  const icon=document.createElement('span');icon.className='item-slot-icon';slot.appendChild(icon);
  const item={sprite:{filename:'source'},frame:0,getXFrameIndex(){return this.frame},getYFrameIndex(){return 0},getRarity(){return undefined}};
  return {slot,icon,item};
 });
 window.slotRects=()=>entries.map(({slot})=>{const r=slot.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height}});
 window.setFrame=async frame=>{for(const {slot,icon,item} of entries){item.frame=frame;let ready=false;for(let attempt=0;attempt<20;attempt++){if(ItemSlotAnimation.update(slot,icon,item)){ready=true;break}await new Promise(r=>setTimeout(r,20))}if(!ready)throw new Error('Atlas not ready')}};
 await window.setFrame(0);
};`;
(async()=>{
 const script=(await esbuild.build({stdin:{contents:entry,resolveDir:root,loader:'ts'},write:false,bundle:true,plugins:[{name:'offline-boundaries',setup(b){
  b.onResolve({filter:/SingletonRepo$|AdminDiagnostics$/},a=>({path:a.path,namespace:'fixture'}));
  b.onLoad({filter:/.*/,namespace:'fixture'},a=>({contents:a.path.endsWith('SingletonRepo')?'export const singletons={getSpriteStore:()=>({get:()=>window.source})};':'export const AdminDiagnostics={get:()=>false};'}));
 }}]})).outputFiles[0].text;
 const browser=await chromium.launch({executablePath:process.env.STENDHAL_BROWSER,headless:true});
 try {
  const failures=[];
  for(const dpr of [1,1.25,1.5,2.625,3.5]) {
   const page=await browser.newPage({viewport:{width:915,height:412},deviceScaleFactor:dpr,isMobile:true,hasTouch:true});
   await page.setContent('<meta name="viewport" content="width=device-width,initial-scale=1"><body style="margin:0;background:#302014;transform:scale(1.37);transform-origin:0 0"></body>');
   const cdp=await page.context().newCDPSession(page);await cdp.send('Emulation.setPageScaleFactor',{pageScaleFactor:1.37});
   await page.addStyleTag({content:fs.readFileSync(path.join(root,'src/js/css/main.css'),'utf8')});
   await page.addScriptTag({content:script});await page.evaluate(()=>window.setup());
   const shots=[],rects=[];
   for(const frame of [0,frameCount-1]) {await page.evaluate(f=>window.setFrame(f),frame);rects.push(await page.evaluate(()=>window.slotRects()));shots.push((await page.screenshot()).toString('base64'));}
   assert.deepEqual(rects[0],rects[1],'Animation must not move any slot in the six-column grid');
   const difference=await page.evaluate(async shots=>{
    const data=[];
    for(const shot of shots){const img=new Image();img.src='data:image/png;base64,'+shot;await img.decode();const c=document.createElement('canvas');c.width=img.width;c.height=img.height;const ctx=c.getContext('2d');ctx.drawImage(img,0,0);data.push(ctx.getImageData(0,0,c.width,c.height).data)}
    return data[0].reduce((n,v,i)=>n+(v!==data[1][i]?1:0),0);
   },shots);
   console.log({dpr,columns:6,rows:4,slotRectsStable:true,differentChannels:difference});if(difference)failures.push(dpr);
   await page.close();
  }
  assert.deepEqual(failures,[],'Identical sprite frames must not jitter at fractional mobile pixel ratios');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
