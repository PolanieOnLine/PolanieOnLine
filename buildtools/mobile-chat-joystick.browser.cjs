/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const esbuild = require('esbuild');
const {chromium} = require(process.env.STENDHAL_PLAYWRIGHT || 'playwright');
const root = path.resolve(__dirname, '..');

(async () => {
	const mocks = {
		marauroa: 'export const marauroa={me:{setDirection(){},stop(){}}};',
		stendhal: 'export const stendhal=window.game;',
		SingletonRepo: 'export const singletons={getViewPort:()=>window.viewport};',
		ConfigManager: 'export const ConfigManager={get:()=>window.game.config};',
		SessionManager: 'export const SessionManager={get:()=>({joystickEnabled:()=>true})};',
		UiStateStore: 'export const UiStateStore={get:()=>({subscribe:()=>()=>{}})};',
		QuickMenu: 'export const QuickMenu={refresh(){}};',
		QuickMenuButton: 'export class QuickMenuButton {}',
		Tooltip: 'export class Tooltip {}',
		ApplicationMenuDialog: 'export class ApplicationMenuDialog {}',
		SingletonFloatingWindow: 'export class SingletonFloatingWindow {}',
		Paths: 'export const Paths={gui:"/data/gui"};'
	};
	const script = (await esbuild.build({stdin: {contents: `
		import {ui} from './src/js/stendhal/ui/UI';
		import {UIComponentEnum} from './src/js/stendhal/ui/UIComponentEnum';
		import {UIUpdateObserver} from './src/js/stendhal/ui/UIUpdateObserver';
		import {SoftwareJoystickController} from './src/js/stendhal/ui/SoftwareJoystickController';
		import {ChatLogComponent} from './src/js/stendhal/ui/component/ChatLogComponent';
		import {GameLanguage} from './src/js/stendhal/util/GameLanguage';
		GameLanguage.dictionaries.en=new Map([['Główny','Main'],['Prywatny','Private'],['Kanał czatu','Chat channel']]);
		GameLanguage.dictionaries.es=new Map([['Główny','General'],['Prywatny','Privado'],['Kanał czatu','Canal de chat']]);
		window.language=GameLanguage;
		ui.constructor.displayReady=true;
		ui.registerComponent(UIComponentEnum.BottomPanel,{refresh(){}});
		window.log=new ChatLogComponent();
		for(let i=0;i<30;i++) log.addLine('normal','Wiadomość '+i,'Gracz');
		window.controller=SoftwareJoystickController.get(); controller.update();
		UIUpdateObserver.get().init();
		window.ready=true;
	`, resolveDir: root, loader: 'ts'}, bundle: true, write: false, plugins: [{name: 'offline-services', setup(build) {
		build.onResolve({filter: /.*/}, args => {
			const key = args.path.split('/').pop();
			if (Object.hasOwn(mocks, key)) return {path: key, namespace: 'fixture'};
		});
		build.onLoad({filter: /.*/, namespace: 'fixture'}, args => ({contents: mocks[args.path]}));
	}}]})).outputFiles[0].text;
	const sourceHtml = fs.readFileSync(path.join(root, 'src/js/polanieonline.html'), 'utf8');
	const template = sourceHtml.match(/<template id="tabpanel-template">[\s\S]*?<\/template>/)[0];
	const input = sourceHtml.match(/<div id="bottomPanel"[\s\S]*?<div id="chat"[^>]*><\/div>\s*<\/div>/)[0];
	const html = `<!doctype html><meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="/src/js/css/main.css"><style>
		body{font:14px Arial}#client{display:flex;height:100dvh}#middleColumn{height:100%;min-width:0}
		#viewport{display:block;flex:0 0 75%;height:75%;background:#264626}
		#bottomPanel{flex:1 1 25%;min-height:0}#joystick-container{position:absolute;top:0;left:0}
		</style><div id="client" class="mobile-floating-ui left-panel-collapsed right-panel-collapsed">
		<div id="middleColumn" class="mainColumn"><canvas id="viewport"></canvas>${input}</div></div>
		<div id="joystick-container"></div>${template}<script>
		window.settings={'joystick.style':'joystick','joystick.autoposition':true};
		window.game={config:{get:key=>settings[key]||'Arial',getBoolean:key=>settings[key],getInt:(key,fallback)=>settings[key]??fallback}};
		window.viewport={updateCanvasSize(){},scheduleResize(){}};
		</script><script>${script}</script>`;
	const browser = await chromium.launch({executablePath: process.env.STENDHAL_BROWSER, headless: true});
	try {
		const page = await browser.newPage({viewport: {width: 390, height: 844}, hasTouch: true});
		const errors = [];
		page.on('pageerror', error => errors.push(String(error)));
		await page.route('http://game.test/**', async route => {
			const pathname = new URL(route.request().url()).pathname;
			if (pathname === '/') return route.fulfill({contentType: 'text/html', body: html});
			const file = path.join(root, pathname);
			return fs.existsSync(file) ? route.fulfill({path: file}) : route.fulfill({status: 404});
		});
		await page.goto('http://game.test/');
		await page.waitForFunction(() => window.ready && document.querySelectorAll('#joystick-container img:not(.hidden)').length === 2);
		const settle = () => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
		const metrics = () => page.evaluate(() => {
			const rect = selector => document.querySelector(selector).getBoundingClientRect();
			const joystick=rect('#joystick-container img'), viewport=rect('#viewport'), chat=rect('#bottomPanel');
			const select=rect('.chat-channel-select'), input=rect('#chatinput'), pane=rect('#chat-channel-0');
			return {joystickBottom:joystick.bottom, viewportBottom:viewport.bottom, chatTop:chat.top,
				selectorY:select.y,inputY:input.y,inputWidth:input.width,paneHeight:pane.height,
				overflow:document.documentElement.scrollWidth>innerWidth};
		});
		for (const [width, height] of [[390,844],[844,390],[390,844],[667,375],[320,568]]) {
			await page.setViewportSize({width,height}); await settle();
			const actual = await metrics();
			assert.ok(Math.abs(actual.joystickBottom-actual.viewportBottom)<2, JSON.stringify(actual));
			assert.ok(actual.joystickBottom<=actual.chatTop+1, 'joystick must stay above the docked chat');
			assert.ok(Math.abs(actual.selectorY-actual.inputY)<3, 'channel selector belongs beside the input');
			assert.ok(actual.inputWidth>45 && actual.paneHeight>20 && !actual.overflow, JSON.stringify(actual));
			assert.equal(await page.locator('#chat [role=tablist]').isVisible(), false);
			console.log(`${width}x${height}`, actual);
			if (process.env.STENDHAL_SCREENSHOT_DIR) {
				fs.mkdirSync(process.env.STENDHAL_SCREENSHOT_DIR, {recursive: true});
				await page.screenshot({path: path.join(process.env.STENDHAL_SCREENSHOT_DIR, `${width}x${height}.png`)});
			}
		}
		await page.evaluate(() => log.addLine('privmsg','Test prywatnej wiadomości','Gracz'));
		assert.equal(await page.locator('.chat-channel-select option').nth(1).textContent(), 'Prywatny •');
		await page.evaluate(() => language.synchronize('en'));
		assert.equal(await page.locator('.chat-channel-select option').nth(1).textContent(), 'Private •');
		assert.equal(await page.locator('.chat-channel-select').getAttribute('aria-label'), 'Chat channel');
		await page.evaluate(() => language.synchronize('es'));
		assert.equal(await page.locator('.chat-channel-select option').nth(1).textContent(), 'Privado •');
		await page.evaluate(() => language.synchronize('pl'));
		assert.equal(await page.locator('.chat-channel-select option').nth(1).textContent(), 'Prywatny •');
		await page.selectOption('.chat-channel-select','1');
		assert.equal(await page.locator('#chat-channel-1').isVisible(), true);
		assert.equal(await page.locator('.chat-channel-select option').nth(1).textContent(), 'Prywatny');
		await page.setViewportSize({width:844,height:390}); await settle();
		assert.equal(await page.locator('.chat-channel-select').inputValue(), '1');
		assert.equal(await page.locator('#chat-channel-1 .logrow').count(), 1);
		await page.evaluate(() => {
			window.bubbledKeys=0;
			for(const type of ['keydown','keyup']) document.addEventListener(type,()=>window.bubbledKeys++);
		});
		await page.locator('.chat-channel-select').focus();
		await page.keyboard.press('ArrowUp');
		assert.equal(await page.evaluate(() => window.bubbledKeys), 0, 'selector keys must not reach game controls');
		await page.evaluate(() => document.getElementById('client').classList.remove('mobile-floating-ui'));
		assert.equal(await page.locator('.chat-channel-select').isVisible(), false);
		await page.getByRole('tab',{name:'Główny'}).click();
		assert.equal(await page.locator('#chat-channel-0 .logrow').count(), 31);
		assert.equal(await page.locator('.chat-channel-select').inputValue(), '0');
		// A manually positioned joystick must still use the user's chosen coordinates.
		await page.evaluate(() => {Object.assign(settings,{'joystick.autoposition':false,'joystick.center.x':140,'joystick.center.y':180});controller.update();});
		const center = await page.locator('#joystick-container img').first().boundingBox();
		assert.equal(center.x+center.width/2,140); assert.equal(center.y+center.height/2,180);
		assert.deepEqual(errors, []);
		console.log('Rotation, compact chat, unread markers, desktop tabs and manual positioning passed.');
	} finally {await browser.close();}
})().catch(error => {console.error(error); process.exit(1);});
