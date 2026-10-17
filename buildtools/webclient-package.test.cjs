const assert = require('node:assert/strict');
const { test } = require('node:test');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const root = path.resolve(__dirname, '..');

test('webclient package contains its referenced script and cache-versioned inventory CSS', () => {
 const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'polanie-web-package-'));
 try {
  const source = path.join(tmp, 'source'), output = path.join(tmp, 'output');
  fs.mkdirSync(path.join(source, 'css'), { recursive: true });
  const html = fs.readFileSync(path.join(root, 'src/js/polanieonline.html'), 'utf8');
  fs.writeFileSync(path.join(source, 'polanieonline.html'), html);
  fs.copyFileSync(path.join(root, 'src/js/css/main.css'), path.join(source, 'css/main.css'));
  fs.writeFileSync(path.join(source, 'stendhal-compiled.js'), '/* matching build fixture */');
  const build = path.join(tmp, 'stage.xml');
  fs.writeFileSync(build, `<project basedir="${root.replaceAll('\\', '/')}">
   <import file="${root.replaceAll('\\', '/')}/build.xml"/>
   <target name="compilejs"/><target name="clean_client_data"/><target name="build_client_data"/>
  </project>`);
  const args = ['-noinput', '-f', build, `-Dsrcjs=${source}`, `-Dbuild_webclient=${output}`, 'webclient_build'];
  const launcher = process.env.STENDHAL_TEST_ANT_JAR;
  const result = spawnSync(launcher ? 'java' : 'ant', launcher ? ['-jar', launcher, ...args] : args, {
   encoding: 'utf8', windowsHide: true
  });
  assert.equal(result.status, 0, `${result.stdout}\n${result.stderr}`);
  const packaged = fs.readFileSync(path.join(output, 'polanieonline.html'), 'utf8');
  for (const match of packaged.matchAll(/<script[^>]+src="([^"]+)"/g)) {
   assert.ok(fs.existsSync(path.join(output, match[1].split('?')[0])), `Missing packaged script: ${match[1]}`);
   assert.notEqual(new URL(match[1], 'https://game.invalid/client/').search, '', 'The matching inventory script must not use an older cached build');
  }
  const cssUrl = new URL(packaged.match(/href="(css\/main\.css[^\"]*)"/)[1], 'https://game.invalid/client/');
  assert.notEqual(cssUrl.search, '', 'Old cached CSS must not be reused after the inventory layer change');
  assert.equal(fs.readFileSync(path.join(output, cssUrl.pathname.replace('/client/', '')), 'utf8'), fs.readFileSync(path.join(source, 'css/main.css'), 'utf8'));
 } finally { fs.rmSync(tmp, { recursive: true, force: true }); }
});
