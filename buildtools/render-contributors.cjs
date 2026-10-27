#!/usr/bin/env node
'use strict';
const fs = require('node:fs');
const MarkdownIt = require('markdown-it');

// Only generate a fragment for AboutDialog; never interpret embedded HTML.
const [input, output] = process.argv.slice(2);
if (!input || !output) {
 console.error('Usage: node render-contributors.cjs input.md output.html');
 process.exitCode = 1;
} else {
 const markdown = new MarkdownIt({html: false, linkify: false});
 fs.writeFileSync(output, markdown.render(fs.readFileSync(input, 'utf8')), 'utf8');
}
