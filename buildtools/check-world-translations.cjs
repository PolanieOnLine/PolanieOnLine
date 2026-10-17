#!/usr/bin/env node
/* Coverage check for display text only. Never changes entity definitions or IDs. */
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '..');
const decode = value => value.replace(/&#x([0-9a-f]+);/gi, (_, hex) => String.fromCodePoint(parseInt(hex, 16)))
  .replace(/&#([0-9]+);/g, (_, number) => String.fromCodePoint(Number(number)))
  .replace(/&quot;/g, '"').replace(/&apos;/g, "'").replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
function xmlFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const target = path.join(dir, entry.name);
    return entry.isDirectory() ? xmlFiles(target) : target.endsWith('.xml') ? [target] : [];
  });
}
function readCatalogue(file) {
  const catalogue = new Map();
  fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '').split(/\r?\n/).forEach((line, index) => {
    if (!line.trim() || line.trimStart().startsWith('#')) return;
    const separator = line.indexOf('=');
    assert.ok(separator > 0, `${file}:${index + 1}: invalid entry`);
    const source = line.slice(0, separator), translated = line.slice(separator + 1);
    assert.ok(translated.trim(), `${file}:${index + 1}: empty translation`);
    assert.ok(!catalogue.has(source), `${file}:${index + 1}: duplicate source`);
    assert.ok(!/\uFFFD|\bundefined\b|\bTODO\b|\bTRANSLATE_ME\b/.test(translated), `${file}:${index + 1}: unfinished translation`);
    assert.deepEqual(placeholders(translated), placeholders(source), `${file}:${index + 1}: changed substitution placeholders`);
    assert.deepEqual(numbers(translated), numbers(source), `${file}:${index + 1}: changed numbers in world display text`);
    catalogue.set(source, translated);
  });
  return catalogue;
}
function placeholders(text) {
  return [...text.matchAll(/\$\{[^}]+\}|\{\d+(?:,[^}]+)?\}|%(?:\d+\$)?[sd]/g)].map(match => match[0]).sort();
}
function numbers(text) {
  return [...text.replace(/\$\{[^}]+\}|\{\d+(?:,[^}]+)?\}|%(?:\d+\$)?[sd]/g, '').matchAll(/\d+(?:[.,]\d+)*/g)]
    .map(match => match[0].replace(/,/g, '.')).sort();
}
for (const kind of ['items', 'creatures']) {
  const tag = kind === 'items' ? 'item' : 'creature';
  const catalogue = readCatalogue(path.join(root, 'data/languages', `en-${kind}.txt`));
  const names = new Set(), descriptions = new Set(), noises = new Set();
  let definitionCount = 0, descriptionCount = 0;
  for (const file of xmlFiles(path.join(root, 'data/conf', kind))) {
    const xml = fs.readFileSync(file, 'utf8').replace(/<!--[\s\S]*?-->/g, '');
    const entities = xml.matchAll(new RegExp(`<${tag} name="([^"]+)"[^>]*>([\\s\\S]*?)</${tag}>`, 'g'));
    for (const match of entities) {
      definitionCount++;
      names.add(decode(match[1]));
      const description = decode((match[2].match(/<description>([\s\S]*?)<\/description>/) || [])[1] || '').trim();
      if (description) { descriptions.add(description); descriptionCount++; }
      for (const noise of match[2].matchAll(/<noise\b[^>]*value="([^"]*)"/g)) if (noise[1]) noises.add(decode(noise[1]));
    }
  }
  for (const [label, sources] of [['name', names], ['description', descriptions], ['noise', noises]]) {
    const missing = [...sources].filter(source => !catalogue.has(source));
    assert.deepEqual(missing, [], `${kind}: missing ${label} translations:\n${missing.join('\n')}`);
  }
  console.log(`${kind}: ${definitionCount} definitions, ${names.size} names, ${descriptionCount} nonempty descriptions (${descriptions.size} unique), ${noises.size} unique noises: complete`);
}
readCatalogue(path.join(root, 'data/languages/pl-world.txt'));
console.log('World catalogue format and placeholder checks passed.');
