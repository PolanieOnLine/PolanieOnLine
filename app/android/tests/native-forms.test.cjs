const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const test = require('node:test');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '..');
const source = fs.readFileSync(path.join(root, 'client/src/main/java/eu/polanieonline/client/ClientView.java'), 'utf8');

const loadingSource=fs.readFileSync(path.join(root,'client/src/main/java/eu/polanieonline/client/GameLoadingProbe.java'),'utf8');
const loadingProbe=loadingSource.slice(loadingSource.indexOf('SCRIPT='),loadingSource.indexOf('private GameLoadingProbe'))
  .match(/"(?:\\.|[^"\\])*"/g).map(value=>JSON.parse(value)).join('');
function loadingState(elements) {
  return vm.runInNewContext(loadingProbe,{window:{getComputedStyle:e=>e.style},document:{
    getElementById:id=>elements[id],querySelector:()=>elements.form}});
}
const visible=()=>Object.freeze({style:Object.freeze({display:'flex',visibility:'visible'}),getClientRects:()=>[{}]});
test('loading surface yields to both the game and interactive login or character selection',()=>{
  assert.equal(loadingState({client:visible()}),1);
  assert.equal(loadingState({loginpopup:visible()}),2);
  assert.equal(loadingState({form:visible()}),2);
});
test('hidden game content and missing UI keep the native loading status',()=>{
  assert.equal(loadingState({}),0);
  assert.equal(loadingState({client:{style:{display:'none',visibility:'visible'},getClientRects:()=>[{}]}}),0);
});
test('native client logging never passes authentication URLs or secret fields to the logger',()=>{
  const logs=source.match(/LOG\.\w+\([^;]*\);/g)||[];
  assert.ok(logs.length>0);
  for(const statement of logs) {
    const args=statement.replace(/"(?:\\.|[^"\\])*"/g,'""');
    assert.doesNotMatch(args,/\b(url|failingUrl|initialPage|completeUrl|seed|stateId|loginseed|loginPass|registerPass|registerEmail)\b/);
  }
});

const probeSource = fs.readFileSync(path.join(root, 'client/src/main/java/eu/polanieonline/client/GameTransportProbe.java'), 'utf8');
const probe = probeSource.slice(probeSource.indexOf('SCRIPT='), probeSource.indexOf('private GameTransportProbe'))
  .match(/"(?:\\.|[^"\\])*"/g).map(value => JSON.parse(value)).join('');
test('transport probe reads all socket states without changing the socket or game globals', () => {
  for (const readyState of [0, 1, 2, 3]) {
    const socket = Object.freeze({ readyState });
    const framework = Object.freeze({ socket });
    const window = Object.freeze({ marauroa: Object.freeze({ clientFramework: framework }) });
    assert.equal(vm.runInNewContext(probe, { window }), readyState);
  }
});
test('transport probe supports the published minified names', () => {
  const window = { $v: { $clientFramework$: { $socket$: { readyState: 1 } } } };
  assert.equal(vm.runInNewContext(probe, { window }), 1);
});
test('transport probe finds a renamed namespace without reading window getters', () => {
  const window = { renamedAfterBuild: { $clientFramework$: { $socket$: { readyState: 1 } } } };
  Object.defineProperty(window, 'privateGetter', { get() { throw new Error('getter must not run'); } });
  assert.equal(vm.runInNewContext(probe, { window }), 1);
});
test('unknown transport is not reported as online', () => {
  assert.equal(vm.runInNewContext(probe, { window: {} }), -1);
  assert.equal(vm.runInNewContext(probe, { window: { marauroa: { clientFramework: {} } } }), -1);
});

function script(method) {
  let part = source.slice(source.indexOf('private void ' + method));
  part = part.slice(part.indexOf('final String js ='), part.indexOf('view.evaluateJavascript'));
  part = part.replace(/JSONObject\.quote\([a-zA-Z]+\)/g, () => JSON.stringify(JSON.stringify('synthetic')))
    .replace(/AUTO_LOGIN_MAX_ATTEMPTS/g, '"5"').replace(/AUTO_LOGIN_TIMEOUT_MS/g, '"20000"');
  return part.match(/"(?:\\.|[^"\\])*"/g).map(value => JSON.parse(value)).join('');
}

function setup(method) {
  let form = null;
  let observer;
  let timeout;
  let disconnected = false;
  let clicks = 0;
  let createClicks = 0;
  const fields = { '#username': {}, '#password': {}, '#passwordrepeat': {}, '#email': {},
    '#loginbutton': { click() { clicks++; } } };
  const sandbox = { window: { location: { hash: '' }, addEventListener() {} },
    document: { documentElement: {}, querySelector(selector) {
      if (selector.startsWith('form')) return form;
      if (selector === '#createlink') return { click() { createClicks++; } };
      return null;
    } }, MutationObserver: function(callback) { observer = callback; this.observe = () => {};
      this.disconnect = () => { disconnected = true; }; }, setTimeout(callback) { timeout = callback; } };
  const outcome = vm.runInNewContext(script(method), sandbox);
  return { outcome, sandbox, fields, showForm(register) {
    form = { querySelector(selector) { return !register && selector === '#passwordrepeat' ? null : fields[selector] || null; } };
  }, mutate() { observer?.(); }, timeout() { timeout?.(); },
    clicks: () => clicks, createClicks: () => createClicks, disconnected: () => disconnected };
}

test('registration waits for a late form without using up submission attempts', () => {
  const run = setup('attemptAutoRegister');
  for (let i = 0; i < 30; i++) run.mutate();
  assert.equal(run.outcome.finalStatus, 'pending');
  assert.equal(run.outcome.attempts, 0);
  run.showForm(true); run.mutate();
  assert.equal(run.outcome.finalStatus, 'submitted');
  assert.equal(run.clicks(), 1);
  assert.equal(run.fields['#email'].value, 'synthetic');
  assert.equal(run.disconnected(), true);
});
test('create-account link is clicked only once while switching forms', () => {
  const run = setup('attemptAutoRegister');
  run.showForm(false);
  for (let i = 0; i < 10; i++) run.mutate();
  assert.equal(run.createClicks(), 1);
  run.showForm(true); run.mutate(); run.mutate();
  assert.equal(run.clicks(), 1);
});
test('registration timeout disconnects observer and never submits a late form', () => {
  const run = setup('attemptAutoRegister');
  run.timeout(); run.showForm(true); run.mutate();
  assert.equal(run.outcome.finalStatus, 'timeout');
  assert.equal(run.disconnected(), true);
  assert.equal(run.clicks(), 0);
});
test('login waits for its form and submits exactly once', () => {
  const run = setup('attemptAutoLogin');
  run.showForm(false); run.mutate(); run.mutate();
  assert.equal(run.outcome.finalStatus, 'submitted');
  assert.equal(run.clicks(), 1);
});
test('published status contains no registration password or email values', () => {
  const run = setup('attemptAutoRegister');
  run.showForm(true); run.mutate();
  assert.equal(JSON.stringify(run.sandbox.window.__poAutoRegisterResult).includes('synthetic'), false);
});
test('both backup formats whitelist settings, not WebView sessions or credentials', () => {
  for (const file of ['backup_rules.xml', 'data_extraction_rules.xml']) {
    const xml = fs.readFileSync(path.join(root, 'client/src/main/res/xml', file), 'utf8');
    const includes = xml.match(/<include[^>]*>/g);
    assert.equal(includes.length, file === 'backup_rules.xml' ? 1 : 2);
    assert.ok(includes.every(value => /^<include domain="sharedpref" path="eu\.polanieonline\.client_preferences\.xml" \/>$/.test(value)));
    assert.equal(xml.includes('<include domain="root"'), false);
  }
});
