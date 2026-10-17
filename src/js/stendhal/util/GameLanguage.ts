/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
type SavedText = {original: string, rendered: string};
type TemplateRule = {pattern: RegExp, output: string, prefix: string, score: number, groups: Map<string, number>};
type KeywordContext = {pattern?: RegExp, score: number, original: string[], translated: string[]};

/** Uses the game's bundled dictionaries, without any translation service. */
export class GameLanguage {
 private static language = "pl";
 private static dictionaries: Record<string, Map<string, string>> = {pl: new Map(), en: new Map()};
 private static rules: Record<string, Map<string, TemplateRule[]>> = {pl: new Map(), en: new Map()};
 private static numericSuffixRules: Record<string, Map<string, TemplateRule[]>> = {pl: new Map(), en: new Map()};
 private static loading?: Promise<void>;
 private static originals = new WeakMap<Node, SavedText>();
 private static attributes = new WeakMap<Element, Map<string, SavedText>>();
 private static roots = new Map<HTMLElement, MutationObserver | undefined>();
 private static scheduled = new Set<HTMLElement>();
 private static keywordLabels:Record<string,Map<string,string>>={pl:new Map(),en:new Map()};
 private static originalKeywords:Record<string,Map<string,string>>={pl:new Map(),en:new Map()};
 private static revision=0;
 private static keywordContexts:Record<string,Map<string,KeywordContext[]>>={pl:new Map(),en:new Map()};
 private static keywordTemplates:Record<string,Map<string,KeywordContext[]>>={pl:new Map(),en:new Map()};
 private static topicAliases:Record<string,Map<string,Set<string>>>={pl:new Map(),en:new Map()};

 public static normalize(language: unknown): string {
  return typeof language === "string" && language.trim().toLowerCase() === "en" ? "en" : "pl";
 }
 public static getLanguage(): string { return this.language; }
 public static getRevision(): number { return this.revision; }
 public static getKeywordLabel(word:string):string {
  return this.keywordLabels[this.language].get(word.trim().toLowerCase())||this.getText(word);
 }
 public static getOriginalKeyword(word:string,renderedContext?:string):string {
  if(renderedContext!==undefined) {
   const targets=new Set<string>(),key=word.trim().toLowerCase();
   const collect=(context:KeywordContext)=>{
    for(let start=0;start<context.translated.length;start++) {
     let label="",target="";
     for(let end=start;end<context.translated.length;end++) {
      if(/[{}]/.test(context.original[end]+context.translated[end]))break;
      label+=(end===start?"":" ")+context.translated[end];
      target+=(end===start?"":" ")+context.original[end];
      if(label.toLowerCase()===key)targets.add(target);
      if(label.length>=key.length)break;
     }
    }
   };
   const exact=this.keywordContexts[this.language].get(renderedContext);
   let known=exact!==undefined;
   if(exact)exact.forEach(collect);
   else {
    let best=-1;
    for(let length=-1;length<=Math.min(16,renderedContext.length);length++) {
     const prefix:string=length===-1?"\u0000number":renderedContext.substring(0,length);
     const contexts:KeywordContext[]=this.keywordTemplates[this.language].get(prefix)||[];
     for(const context of contexts) {
      if(context.score<best||!context.pattern!.test(renderedContext))continue;
      known=true;
      if(context.score>best){targets.clear();best=context.score;}
      collect(context);
     }
    }
   }
   if(targets.size===1)return Array.from(targets)[0];
   const aliases=this.topicAliases[this.language].get(key);
   if(known&&targets.size===0&&aliases?.size===1)return Array.from(aliases)[0];
   if(renderedContext.trim()!==renderedContext)return this.getOriginalKeyword(word,renderedContext.trim());
   if(/[\r\n]/.test(renderedContext)) {
    const lines=new Set(renderedContext.split(/[\r\n]+/).map(line=>this.getOriginalKeyword(word,line.trim())).filter(target=>target!==word));
    if(lines.size===1)return Array.from(lines)[0];
   }
   return word;
  }
  return this.originalKeywords[this.language].get(word.trim().toLowerCase())||word;
 }
 public static getText(text: string): string { return this.translate(text, 0); }
 private static translate(text: string, depth: number): string {
  if(depth>8)return text;
  const exact = this.dictionaries[this.language].get(text);
  if (exact !== undefined) return exact;
  if(depth>0) {
   const marked=/^([#§])(?:'([^']+)'|([^\s]+))$/.exec(text);
   if(marked) {
    const label=marked[2]??marked[3],translated=this.translate(label,depth+1);
    if(translated!==label)return marked[1]+(translated.includes(" ")?"'"+translated+"'":translated);
   }
   const amount=/^([0-9]+) ([#§])(?:'([^']+)'|([^\s]+))$/.exec(text);
   if(amount) {
    const plain=amount[1]+" "+(amount[3]??amount[4]),translated=this.translate(plain,depth+1);
    if(translated!==plain&&translated.startsWith(amount[1]+" ")) {
     const noun=translated.substring(amount[1].length+1);
     return amount[1]+" "+amount[2]+(noun.includes(" ")?"'"+noun+"'":noun);
    }
   }
  }
  if(depth>0 && text.includes("&nbsp;")) {
   const decoded=text.replace(/&nbsp;/g," ");
   const translated=this.translate(decoded,depth+1);
   if(translated!==decoded)return translated;
  }
  let best: TemplateRule | undefined, matched: RegExpExecArray | undefined;
  const digits=/^[0-9]+/.exec(text);
  if(digits)for(const rule of this.numericSuffixRules[this.language].get(text.substring(digits[0].length))||[]) {
   const match=rule.pattern.exec(text);
   if(match){best=rule;matched=match;break;}
  }
  for(let length=-1;length<=Math.min(16,text.length);length++) {
   if(length===-1&&!/^[0-9]/.test(text))continue;
   for(const rule of this.rules[this.language].get(length===-1?"\u0000number":text.substring(0,length)) || []) {
    if(best && rule.score<=best.score)break;
    const match=rule.pattern.exec(text);
    if(match){best=rule;matched=match;break;}
   }
  }
  if(best && matched) {
   const captures=matched, rule=best;
   return rule.output.replace(/\{(?:(item|number):)?(\d+)\}/g, (_token, kind, id) => {
    const value=captures[rule.groups.get(id)!];
    return kind==="item"?this.translate(value,depth+1):value;
   });
  }
  if(/^<(html>|div\b|span\b)/.test(text)) {
   return text.split(/(<[^>]+>)/).map((part,index)=>index%2?part:this.translate(part,depth+1)).join("");
  }
  if(/[\r\n]/.test(text))return text.split(/(\r\n|\r|\n)/).map((part,index)=>index%2?part:this.translate(part,depth+1)).join("");
  const trimmed=text.trim();
  if(trimmed && trimmed!==text) {
   const translated=this.translate(trimmed,depth+1);
   if(translated!==trimmed){const start=text.indexOf(trimmed);return text.substring(0,start)+translated+text.substring(start+trimmed.length);}
  }
  const bullet=/^(\s*[*-]\s+)(.+)$/.exec(text);
  if(bullet)return bullet[1]+this.translate(bullet[2],depth+1);
  if(depth>0 && text.includes(", "))return text.split(", ").map(part=>this.translate(part,depth+1)).join(", ");
  if(depth>0&&this.language==="en") {
   const joined=/^(.+?) (i|oraz|lub) (.+)$/.exec(text);
   if(joined)return this.translate(joined[1],depth+1)+(joined[2]==="lub"?" or ":" and ")+this.translate(joined[3],depth+1);
   if(text.startsWith("oraz "))return "and "+this.translate(text.substring(5),depth+1);
  }
  return text;
 }
 public static unescape(text: string): string {
  return text.replace(/\\([nrt\\=#])/g, (_match, value) => ({n:"\n",r:"\r",t:"\t"}[value as "n"] ?? value));
 }
 public static parse(content: string): Map<string, string> {
  const entries=new Map<string,string>();
  for(const raw of content.replace(/^\uFEFF/,"").split(/\r?\n/)) {
   if(!raw.trim() || raw.trim().startsWith("#"))continue;
   let separator=-1, escaped=false;
   for(let index=0;index<raw.length;index++) {
    const char=raw[index];if(char==="="&&!escaped){separator=index;break;}
    escaped=char==="\\"&&!escaped;
   }
   if(separator<1)continue;
   const source=this.unescape(raw.substring(0,separator)),value=this.unescape(raw.substring(separator+1));
   if(!value.trim())continue;
   const placeholders=(text:string)=>Array.from(new Set(Array.from(text.matchAll(/\{(?:(?:item|number):)?(\d+)\}/g),m=>m[1]))).sort().join(",");
   if(placeholders(source)!==placeholders(value)){console.warn("Translation parameters differ",source);continue;}
   if(!entries.has(source))entries.set(source,value);
  }
  return entries;
 }
 private static buildRules(language: string): void {
  const buckets=new Map<string,TemplateRule[]>();
  const numericSuffixes=new Map<string,TemplateRule[]>();
  const quote=(text:string)=>text.replace(/[.*+?^$\{\}()|[\]\\]/g,"\\$&");
  for(const [source,output] of Array.from(this.dictionaries[language])) {
   const slots=Array.from(source.matchAll(/\{(?:(item|number):)?(\d+)\}/g));
   if(!slots.length)continue;
   const groups=new Map<string,number>();let regex="",offset=0,score=0;
   for(const slot of slots) {
    const literal=source.substring(offset,slot.index!);regex+=quote(literal);score+=literal.length;
    const id=slot[2],group=groups.get(id);
    if(group===undefined){groups.set(id,groups.size+1);regex+=slot[1]==="number"?"([0-9]+)":"([\\s\\S]*?)";}else regex+="\\"+group;
    offset=slot.index!+slot[0].length;
   }
   const tail=source.substring(offset);regex+=quote(tail);score+=tail.length;
   if(score<(source.startsWith("{number:")?1:5))continue;
   const head=source.substring(0,slots[0].index);
   const rule={pattern:new RegExp("^"+regex+"$"),output,prefix:source.startsWith("{number:")?"\u0000number":head.substring(0,16),score,groups};
   const suffix=/^\{number:\d+\}([^0-9{}][^{}]*)$/.exec(source);
   const target=suffix?numericSuffixes:buckets,key=suffix?suffix[1]:rule.prefix;
   const bucket=target.get(key)||[];bucket.push(rule);target.set(key,bucket);
  }
  for(const bucket of Array.from(buckets.values()))bucket.sort((a,b)=>b.score-a.score);
  for(const bucket of Array.from(numericSuffixes.values()))bucket.sort((a,b)=>b.score-a.score);
  this.rules[language]=buckets;
  this.numericSuffixRules[language]=numericSuffixes;
  {
   const labels=this.keywordLabels[language],originals=this.originalKeywords[language];
   const marked=(text:string)=>Array.from(text.matchAll(/(?<!#)#(?!#)(?:'([^']+)'|([^\s,.;:!?]+))/g),m=>m[1]||m[2]);
   const links=(text:string)=>{
    const words:string[]=[];let end=-1,quoted=true;
    for(const match of text.matchAll(/(?<!#)#(?!#)(?:'([^']+)'|([^\s,.;:!?]+))/g)) {
     const label=match[1]??match[2];
     if(!quoted&&match[1]===undefined&&text.substring(end,match.index)===" ")words[words.length-1]+=" "+label;
     else words.push(label);
     end=match.index!+match[0].length;quoted=match[1]!==undefined;
    }
    return words;
   };
   for(const [source,value] of Array.from(this.dictionaries[language])) {
    if(source.length<=80&&!/[\n<{]/.test(source)) {
     if(!originals.has(value.toLowerCase()))originals.set(value.toLowerCase(),source);
     if(!labels.has(source.toLowerCase()))labels.set(source.toLowerCase(),value);
    }
   }
   for(const [source,value] of Array.from(this.dictionaries[language])) {
    const original=marked(source),translated=marked(value);
    const addAlias=(label:string,target:string)=>{
     if(/[{}]/.test(label+target))return;
     const key=label.toLowerCase(),aliases=this.topicAliases[language].get(key)||new Set<string>();
     aliases.add(target);this.topicAliases[language].set(key,aliases);
    };
    if(original.length===translated.length)for(let i=0;i<original.length;i++)addAlias(translated[i],original[i]);
    const sourceLinks=links(source),outputLinks=links(value);
    if(outputLinks.length) {
     const paired=sourceLinks.length===outputLinks.length;
     if(paired)for(let i=0;i<sourceLinks.length;i++)addAlias(outputLinks[i],sourceLinks[i]);
     const slots=Array.from(value.matchAll(/\{(?:(item|number):)?(\d+)\}/g));
     const context:KeywordContext={score:value.length,original:paired?sourceLinks:[],translated:paired?outputLinks:[]};
     let key=value;
     if(slots.length) {
      let regex="",offset=0,score=0;const groups=new Map<string,number>();
      for(const slot of slots) {
       const literal=value.substring(offset,slot.index!);regex+=quote(literal);score+=literal.length;
       const id=slot[2],group=groups.get(id);
       if(group===undefined){groups.set(id,groups.size+1);regex+=slot[1]==="number"?"([0-9]+)":"([\\s\\S]*?)";}else regex+="\\"+group;
       offset=slot.index!+slot[0].length;
      }
      const tail=value.substring(offset);regex+=quote(tail);score+=tail.length;
      context.pattern=new RegExp("^"+regex+"$");context.score=score;
      key=value.startsWith("{number:")?"\u0000number":value.substring(0,slots[0].index).substring(0,16);
     }
     const target=slots.length?this.keywordTemplates[language]:this.keywordContexts[language];
     const bucket=target.get(key)||[];bucket.push(context);target.set(key,bucket);
    }
    if(original.length===translated.length)for(let i=0;i<original.length;i++) {
     if(/[{}]/.test(original[i]+translated[i]))continue;
     if(!originals.has(translated[i].toLowerCase()))originals.set(translated[i].toLowerCase(),original[i]);
     if(!labels.has(original[i].toLowerCase()))labels.set(original[i].toLowerCase(),translated[i]);
    }
   }
  }
 }
 public static load(dataPath: string): Promise<void> {
  if(!this.loading) {
   this.loading=(async () => {
    const files=["pl.txt","en.txt"];
    for(const language of ["pl","en"])try {
     const response=await fetch(dataPath+"/languages/"+language+"-index.txt");
     if(response.ok)for(const raw of (await response.text()).split(/\r?\n/)) {
      const file=raw.trim();
      if(new RegExp("^"+language+"-[a-zA-Z0-9_-]+\\.txt$").test(file)&&file!==language+"-index.txt")files.push(file);
     }
    }catch(error){console.warn("Cannot load game language index",error);}
    const contents=await Promise.all(files.map(async file=>{
     try {const response=await fetch(dataPath+"/languages/"+file);if(!response.ok)throw new Error("HTTP "+response.status);return this.parse(await response.text());}
     catch(error){console.warn("Cannot load game language "+file,error);return new Map<string,string>();}
    }));
    for(let index=0;index<files.length;index++) {
     const language=files[index].startsWith("pl")?"pl":"en";
     for(const [source,value] of Array.from(contents[index]))if(!this.dictionaries[language].has(source))this.dictionaries[language].set(source,value);
    }
    this.buildRules("pl");this.buildRules("en");this.revision++;this.refreshUI();
   })();
  }
  return this.loading;
 }
 public static synchronize(language: unknown): void {
  const selected=this.normalize(language);if(selected===this.language)return;
  this.language=selected;this.revision++;this.refreshUI();
 }
 public static registerUI(root: HTMLElement): void {
  if(this.roots.has(root)){this.translateUI(root);return;}
  let observer:MutationObserver|undefined;
  if(typeof MutationObserver!=="undefined") {
   observer=new MutationObserver(()=>{
    if(this.scheduled.has(root))return;
    this.scheduled.add(root);
    Promise.resolve().then(()=>{this.scheduled.delete(root);if(this.roots.has(root))this.translateUI(root);});
   });
   observer.observe(root,{childList:true,subtree:true,characterData:true,attributes:true,attributeFilter:["title","aria-label","placeholder","data-no-translate"]});
  }
  this.roots.set(root,observer);this.translateUI(root);
 }
 public static unregisterUI(root: HTMLElement): void {
  this.roots.get(root)?.disconnect();this.roots.delete(root);this.scheduled.delete(root);
 }
 private static refreshUI(): void {
  if(typeof document==="undefined")return;
  for(const root of Array.from(this.roots.keys()))this.translateUI(root);
  for(const root of Array.from(document.querySelectorAll<HTMLElement>(".settings-dialog")))this.translateSettings(root);
 }
 public static translateSettings(root: HTMLElement): void {
  const caption=root.closest(".windowcontent")?.parentElement?.querySelector(".windowtitle");
  if(caption)caption.textContent=this.getText("Ustawienia");
  this.translateUI(root);
  const selector=root.querySelector<HTMLSelectElement>("#selectlanguage");
  if(selector)selector.value=this.language;
 }
 public static translateUI(root: HTMLElement): void {
  if(root.closest("[data-no-translate]"))return;
  const excluded="textarea, input, script, style, [data-no-translate], [data-translation-keyword], #chatlog, #chatinput, #buddyListUL";
  const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);let node:Node|null;
  while((node=walker.nextNode())) {
   const parent=node.parentElement;if(!parent||parent.closest(excluded))continue;
   const current=node.textContent||"";let saved=this.originals.get(node);
   if(!saved||saved.rendered!==current)saved={original:current,rendered:current};
   saved.rendered=saved.original.replace(/\S[\s\S]*\S|\S/,value=>this.getText(value.trim().replace(/\s+/g," ")));
   if(current!==saved.rendered)node.textContent=saved.rendered;
   this.originals.set(node,saved);
  }
  const attributeSelector="[title], [aria-label], [placeholder]";
  const attributeElements=Array.from(root.querySelectorAll(attributeSelector));
  if(root.matches?.(attributeSelector))attributeElements.unshift(root);
  for(const element of attributeElements) {
   if(element.closest("[data-no-translate], #chatlog, #chatinput, #buddyListUL"))continue;
   let saved=this.attributes.get(element);if(!saved){saved=new Map();this.attributes.set(element,saved);}
   for(const attribute of ["title","aria-label","placeholder"]) {
    const current=element.getAttribute(attribute);if(current===null)continue;
    let text=saved.get(attribute);if(!text||text.rendered!==current)text={original:current,rendered:current};
    text.rendered=this.getText(text.original);if(text.rendered!==current)element.setAttribute(attribute,text.rendered);
    saved.set(attribute,text);
   }
  }
  for(const button of Array.from(root.querySelectorAll<HTMLElement>("[data-translation-keyword]"))) {
   if(button.closest("[data-no-translate]"))continue;
   const caption=this.getKeywordLabel(button.getAttribute("data-translation-keyword")||"");
   if(button.textContent!==caption)button.textContent=caption;
  }
  for(const input of Array.from(root.querySelectorAll<HTMLInputElement>("input[type=button], input[type=submit], input[type=reset]"))) {
   if(input.closest("[data-no-translate]"))continue;
   let values=this.attributes.get(input);if(!values){values=new Map();this.attributes.set(input,values);}
   const current=input.value;let saved=values.get("value");
   if(!saved||saved.rendered!==current)saved={original:current,rendered:current};
   saved.rendered=this.getText(saved.original);if(input.value!==saved.rendered)input.value=saved.rendered;
   values.set("value",saved);
  }
 }
}
