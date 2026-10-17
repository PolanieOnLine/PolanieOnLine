/* Validate current Polish-to-Spanish display catalogues, excluding legacy EN-to-ES. */
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '..');
const directory = path.join(root, 'data/languages');
function read(name) {
  const result = new Map();
  const text = fs.readFileSync(path.join(directory, name), 'utf8');
  assert(!text.includes('\uFFFD'), `${name}: invalid Unicode replacement character`);
  for (const [offset, line] of text.split(/\r?\n/).entries()) {
    if (!line.trim() || line.startsWith('#')) continue;
    let separator = -1;
    for (let i = 0; i < line.length; ++i) {
      if (line[i] === '\\') { ++i; continue; }
      if (line[i] === '=') { separator = i; break; }
    }
    const location = `${name}:${offset + 1}`;
    assert(separator > 0, `${location}: missing source or separator`);
    const source = line.slice(0, separator);
    const target = line.slice(separator + 1);
    assert(target.trim(), `${location}: empty translation`);
    assert(!result.has(source), `${location}: duplicate source`);
    result.set(source, target);
  }
  return result;
}
function placeholders(value) {
  return [...new Set([...value.matchAll(/\{(?:(?:item|number):)?(\d+)\}/g)].map(match => match[1]))].sort();
}
function formatting(value) {
  return [...value.matchAll(/%(?:\d+\$)?[-+#0]*(?:\d+)?(?:\.\d+)?[a-zA-Z%]/g)].map(match => match[0]);
}
function numbers(value) {
  const literal = value.replace(/\{(?:(?:item|number):)?\d+\}/g, '').replace(/%(?:\d+\$)?[-+#0]*(?:\d+)?(?:\.\d+)?[a-zA-Z%]/g, '');
  return [...literal.matchAll(/\d+(?:[.,]\d+)?/g)].map(match => match[0].replace(',', '.')).sort();
}
const files = fs.readdirSync(directory).filter(name => /^es-(?!index\.)[a-z0-9-]+\.txt$/.test(name));
const all = new Map();
const englishSources = new Set(fs.readdirSync(directory)
  .filter(name => /^en(?:-[a-z0-9-]+)?\.txt$/.test(name) && name !== 'en-index.txt')
  .flatMap(name => [...read(name).keys()]));
for (const name of files) {
  const entries = read(name);
  for (const [source, target] of entries) {
    assert(englishSources.has(source), `${name}: source does not match any current catalogue: ${source}`);
    assert.deepEqual(placeholders(target), placeholders(source), `${name}: placeholder mismatch: ${source}`);
    assert.deepEqual(formatting(target), formatting(source), `${name}: numeric format mismatch: ${source}`);
    assert.deepEqual(numbers(target), numbers(source), `${name}: numeric value changed: ${source}`);
    assert.deepEqual([...target.matchAll(/(?:href|src)="([^"]*)"/g)].map(x => x[1]),
      [...source.matchAll(/(?:href|src)="([^"]*)"/g)].map(x => x[1]), `${name}: link target changed: ${source}`);
    assert(!all.has(source), `${name}: duplicate source across catalogues: ${source}`);
    all.set(source, target);
  }
  console.log(`${name}: ${entries.size} entries`);
}
for (const reference of ['en.txt', 'en-ui.txt', 'en-items.txt', 'en-creatures.txt', 'en-server.txt']) {
  const entries = read(reference);
  const missing = [...entries.keys()].filter(key => !all.has(key));
  console.log(`${reference}: Spanish coverage ${entries.size - missing.length}/${entries.size}`);
  assert.equal(missing.length, 0, `${reference}: previously completed Spanish coverage regressed`);
  if (process.argv.includes('--missing-ui') && ['en.txt', 'en-ui.txt'].includes(reference)) {
    missing.forEach(key => console.log(`MISSING: ${key}`));
  }
}
assert.equal(all.get('Porozmawiaj'), 'Hablar');
assert.equal(all.get('skórzany kirys'), 'coraza de cuero');
assert.equal(all.get('Dhohr Nuggetcutter'), 'Dhohr Nuggetcutter');
assert.equal(all.get('Monety w stosie: {0}'), 'Monedas en esta pila: {0}');
assert.equal(all.get('Kolor {0}'), 'Color de {item:0}');
for (const reference of ['en-npcs-semos.txt', 'en-quests-hunting.txt', 'en-quests-houses.txt',
  'en-server-items.txt', 'en-server-events.txt', 'en-server-admin.txt',
  'en-server-npc-services.txt', 'en-server-time.txt', 'en-achievements.txt',
  'en-quests.txt', 'en-quests-info.txt', 'en-quests-social.txt', 'en-quests-dialogue.txt',
  'en-quests-dialogue-2.txt', 'en-quests-dialogue-3.txt',
  'en-quests-coal.txt', 'en-quests-coded-message.txt', 'en-quests-crystals.txt',
  'en-quests-antivenom.txt', 'en-quests-annie.txt', 'en-item-forms.txt', 'en-creature-forms.txt',
  'en-quests-dialogue-4.txt', 'en-quests-barbarian.txt', 'en-quests-yeti.txt',
  'en-release-client.txt', 'en-release-server.txt']) {
  const entries = read(reference);
  const covered = [...entries.keys()].filter(key => all.has(key)).length;
  console.log(`${reference}: Spanish coverage ${covered}/${entries.size}`);
  assert.equal(covered, entries.size, `${reference}: previously completed Spanish coverage regressed`);
}
console.log('Spanish catalogues: format, numeric values, placeholders and links verified.');
