'use strict';
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawnSync} = require('node:child_process');

test('contributors build preserves Markdown and Unicode without emitting executable HTML', () => {
 const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'polanie-contributors-'));
 try {
  const input = path.join(directory, 'authors.md');
  const output = path.join(directory, 'authors.html');
  fs.writeFileSync(input, '# Autorzy\n\n- **Łukasz** — [Project](https://polanieonline.eu)\n\n<script>alert(1)</script>\n\n[unsafe](javascript:alert(1))\n\n| Name |\n| --- |\n| <svg onload=alert(1)> |\n');
  const result = spawnSync(process.execPath, [path.join(__dirname, 'render-contributors.cjs'), input, output], {encoding: 'utf8'});
  assert.equal(result.status, 0, result.stderr);
  const html = fs.readFileSync(output, 'utf8');
  assert.match(html, /<h1>Autorzy<\/h1>/);
  assert.match(html, /<strong>Łukasz<\/strong>/);
  assert.match(html, /href="https:\/\/polanieonline.eu"/);
  assert.match(html, /<table>/);
  assert.match(html, /&lt;script&gt;/);
  assert.doesNotMatch(html, /<script|<svg|href="javascript:/i);
 } finally {
  fs.rmSync(directory, {recursive: true, force: true});
 }
});

test('contributors build fails on missing input rather than silently publishing empty content', () => {
 const result = spawnSync(process.execPath, [path.join(__dirname, 'render-contributors.cjs'), path.join(__dirname, 'missing-contributors-fixture.md'), 'unused.html'], {encoding: 'utf8'});
 assert.notEqual(result.status, 0);
});
