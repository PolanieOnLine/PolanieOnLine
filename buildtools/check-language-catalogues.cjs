#!/usr/bin/env node
/* Validates the bundled display catalogues without modifying game data. */
'use strict';
const fs=require('node:fs');
const path=require('node:path');
const directory=path.resolve(__dirname,'../data/languages');
const decode=text=>text.replace(/\\([nrt\\=#])/g,(_,value)=>({n:'\n',r:'\r',t:'\t'}[value]??value));
const parameters=text=>[...new Set([...text.matchAll(/\{(?:(?:item|number):)?(\d+)\}/g)].map(match=>match[1]))].sort().join(',');
function readCatalogue(file) {
 const entries=new Map(),errors=[];
 fs.readFileSync(path.join(directory,file),'utf8').replace(/^\uFEFF/,'').split(/\r?\n/).forEach((line,index)=>{
  if(!line.trim()||line.trimStart().startsWith('#'))return;
  let separator=-1,escaped=false;
  for(let i=0;i<line.length;i++){if(line[i]==='='&&!escaped){separator=i;break;}escaped=line[i]==='\\'&&!escaped;}
  const location=`${file}:${index+1}`;
  if(separator<1){errors.push(`${location}: missing key/value separator`);return;}
  const source=decode(line.slice(0,separator)),value=decode(line.slice(separator+1));
  if(/tokens truncated|Warning: truncated output/.test(source+'\n'+value))errors.push(`${location}: truncated tool output is not game text`);
  if(source.includes('\uFFFD'))errors.push(`${location}: damaged source encoding`);
  if(!value.trim()){errors.push(`${location}: empty translation`);return;}
  if(parameters(source)!==parameters(value))errors.push(`${location}: changed parameters`);
  if(/\uFFFD|\b(?:TRANSLATE_ME|TODO_TRANSLATE|undefined)\b/.test(value))errors.push(`${location}: incomplete text`);
  if(entries.has(source)&&entries.get(source)!==value)errors.push(`${location}: conflicting duplicate ${source.slice(0,90)}`);
  entries.set(source,value);
 });
 return {entries,errors};
}
function loadLanguage(language) {
 const files=[`${language}.txt`],index=path.join(directory,`${language}-index.txt`);
 if(fs.existsSync(index))for(const line of fs.readFileSync(index,'utf8').split(/\r?\n/)){
  const file=line.trim();if(!file||file.startsWith('#'))continue;
  if(!new RegExp(`^${language}-[a-zA-Z0-9_-]+\\.txt$`).test(file)||file===`${language}-index.txt`)throw new Error(`Unsafe catalogue ${file}`);
  if(files.includes(file))throw new Error(`Duplicate index entry ${file}`);
  files.push(file);
 }
 const entries=new Map(),errors=[],origin=new Map();
 for(const file of files){
  const catalogue=readCatalogue(file);errors.push(...catalogue.errors);
  for(const [source,value] of catalogue.entries){
   if(entries.has(source)&&entries.get(source)!==value)errors.push(`${file}: conflicts with ${origin.get(source)}: ${source.slice(0,90)}`);
   if(!entries.has(source)){entries.set(source,value);origin.set(source,file);}
  }
 }
 return {entries,errors,files};
}
module.exports={readCatalogue,loadLanguage,parameters};
if(require.main===module){
 let errors=[];
 for(const language of process.argv.slice(2).length?process.argv.slice(2):['pl','en']){
  const catalogue=loadLanguage(language);errors.push(...catalogue.errors);
  console.log(`${language}: ${catalogue.entries.size} unique entries in ${catalogue.files.length} files`);
 }
 for(const error of errors)console.error(error);
 if(errors.length)process.exitCode=1;
 else console.log('Catalogue files, parameters and duplicate checks passed.');
}
