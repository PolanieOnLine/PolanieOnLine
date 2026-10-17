/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/** Shared reader for the game's existing UTF-8 language dictionaries. */
public final class Translation {
 public static final String DEFAULT_LANGUAGE = "pl";
 private static final Logger logger = Logger.getLogger(Translation.class);
 private static final Pattern PARAMETER = Pattern.compile("\\{(?:(item|number):)?(\\d+)\\}");
 private static final Pattern NUMERIC_SUFFIX = Pattern.compile("^\\{number:\\d+\\}([^0-9{}][^{}]*)$");
 private static final Pattern TAG = Pattern.compile("<[^>]+>");
 private static final Map<String, Catalogue> dictionaries = loadDictionaries();
 private Translation() { }

 public static boolean isSupported(String language) {
  return language != null && ("pl".equals(language.trim().toLowerCase(Locale.ROOT))
    || "en".equals(language.trim().toLowerCase(Locale.ROOT)));
 }
 public static String normalize(String language) {
  return isSupported(language) ? language.trim().toLowerCase(Locale.ROOT) : DEFAULT_LANGUAGE;
 }
 public static String getText(String language, String text) {
  if (text == null) return null;
  return translate(dictionaries.get(normalize(language)), text, 0);
 }
 public static String getOriginalKeyword(String language,String word) {
  if(word==null)return null;
  String original=dictionaries.get(normalize(language)).originalKeywords.get(word.trim().toLowerCase(Locale.ROOT));
  return original==null?word:original;
 }
 /** Resolve a displayed link only against the sentence that produced it.
  * The returned target must be bound when rendering, before a language change.
  */
 public static String getOriginalKeyword(String language,String word,String renderedContext) {
  if(word==null||renderedContext==null)return word;
  Catalogue catalogue=dictionaries.get(normalize(language));
  String original=catalogue.contextKeyword(word,renderedContext);
  if(original==null&&!renderedContext.equals(renderedContext.trim()))original=catalogue.contextKeyword(word,renderedContext.trim());
  if(original==null&&(renderedContext.contains("\n")||renderedContext.contains("\r"))) {
   Set<String> targets=new HashSet<>();
   for(String line:renderedContext.split("[\\r\\n]+")) {String target=catalogue.contextKeyword(word,line.trim());if(target!=null)targets.add(target);}
   if(targets.size()==1)original=targets.iterator().next();
  }
  return original==null?word:original;
 }
 public static String getKeywordLabel(String language,String word) {
  Catalogue catalogue=dictionaries.get(normalize(language));
  String label=catalogue.keywordLabels.get(word.trim().toLowerCase(Locale.ROOT));
  return label==null?getText(language,word):label;
 }
 private static String translate(Catalogue catalogue, String text, int depth) {
  if (depth > 8) return text;
  String exact = catalogue.texts.get(text);
  if (exact != null) return exact;
  if(depth>0) {
   Matcher marked=Pattern.compile("^([#§])(?:'([^']+)'|([^\\s]+))$").matcher(text);
   if(marked.matches()) {
    String label=marked.group(2)==null?marked.group(3):marked.group(2);
    String translated=translate(catalogue,label,depth+1);
    if(!translated.equals(label))return marked.group(1)+(translated.contains(" ")?"'"+translated+"'":translated);
   }
   Matcher amount=Pattern.compile("^([0-9]+) ([#§])(?:'([^']+)'|([^\\s]+))$").matcher(text);
   if(amount.matches()) {
    String label=amount.group(3)==null?amount.group(4):amount.group(3);
    String plain=amount.group(1)+" "+label,translated=translate(catalogue,plain,depth+1);
    if(!translated.equals(plain)&&translated.startsWith(amount.group(1)+" ")) {
     String noun=translated.substring(amount.group(1).length()+1);
     return amount.group(1)+" "+amount.group(2)+(noun.contains(" ")?"'"+noun+"'":noun);
    }
   }
  }
  if(depth>0 && text.contains("&nbsp;")) {
   String decoded=text.replace("&nbsp;"," ");
   String translated=translate(catalogue,decoded,depth+1);
   if(!translated.equals(decoded))return translated;
  }
  String matched = catalogue.match(text, depth);
  if (matched != null) return matched;
  // Translate display fragments, never HTML attributes or captured player names.
  if (text.startsWith("<html>") || text.startsWith("<div") || text.startsWith("<span")) {
   Matcher tags = TAG.matcher(text); StringBuilder out = new StringBuilder(); int offset=0;
   while(tags.find()) {out.append(translate(catalogue,text.substring(offset,tags.start()),depth+1));out.append(tags.group());offset=tags.end();}
   return out.append(translate(catalogue,text.substring(offset),depth+1)).toString();
  }
  if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
   Matcher lines=Pattern.compile("([^\\r\\n]*)(\\r\\n|\\r|\\n|$)").matcher(text);StringBuilder out=new StringBuilder();
   while(lines.find()) {if(lines.group().isEmpty())break;out.append(translate(catalogue,lines.group(1),depth+1)).append(lines.group(2));}
   return out.toString();
  }
  String trimmed=text.trim();
  if(!trimmed.equals(text) && !trimmed.isEmpty()) {
   String translated=translate(catalogue,trimmed,depth+1);
   if(!translated.equals(trimmed)) {int start=text.indexOf(trimmed);return text.substring(0,start)+translated+text.substring(start+trimmed.length());}
  }
  // The journal adds bullet markers to already-written quest sentences.
  Matcher bullet=Pattern.compile("^(\\s*[*-]\\s+)(.+)$").matcher(text);
  if(bullet.matches())return bullet.group(1)+translate(catalogue,bullet.group(2),depth+1);
  // Only explicitly translated template captures may contain display lists.
  if (depth > 0 && text.contains(", ")) {
   String[] parts=text.split(", ",-1);StringBuilder out=new StringBuilder();boolean changed=false;
   for(String part:parts) {String translated=translate(catalogue,part,depth+1);changed|=!translated.equals(part);if(out.length()>0)out.append(", ");out.append(translated);}
   if(changed)return out.toString();
  }
  if(depth>0&&"en".equals(catalogue.language)) {
   Matcher joined=Pattern.compile("^(.+?) (i|oraz|lub) (.+)$").matcher(text);
   if(joined.matches())return translate(catalogue,joined.group(1),depth+1)+("lub".equals(joined.group(2))?" or ":" and ")+translate(catalogue,joined.group(3),depth+1);
   if(text.startsWith("oraz "))return "and "+translate(catalogue,text.substring(5),depth+1);
  }
  return text;
 }

 private static Map<String,Catalogue> loadDictionaries() {
  Map<String,Catalogue> result=new HashMap<>();
  for(String language:new String[]{"pl","en"}) {
   Map<String,String> entries=new LinkedHashMap<>();
   readFile(language+".txt",entries);
   {
    try(BufferedReader index=reader(language+"-index.txt")) {
     if(index!=null) {String line;while((line=index.readLine())!=null) {
      String file=line.trim();if(file.isEmpty()||file.startsWith("#"))continue;
      if(file.matches(language+"-[a-zA-Z0-9_-]+\\.txt")&&!file.equals(language+"-index.txt"))readFile(file,entries);
      else logger.warn("Invalid language catalogue name: "+file);
     }}
    } catch(IOException error){logger.error("Cannot read language index",error);}
   }
   result.put(language,new Catalogue(language,entries));
  }
  return Collections.unmodifiableMap(result);
 }
 private static BufferedReader reader(String file)throws IOException {
  InputStream stream=Translation.class.getClassLoader().getResourceAsStream("data/languages/"+file);
  return stream==null?null:new BufferedReader(new InputStreamReader(stream,"UTF-8"));
 }
 private static void readFile(String file,Map<String,String> entries) {
  try(BufferedReader reader=reader(file)) {
   if(reader==null){logger.warn("Missing language dictionary: "+file);return;}
   String line;while((line=reader.readLine())!=null) {
    if(line.startsWith("\uFEFF"))line=line.substring(1);
    if(line.trim().isEmpty()||line.trim().startsWith("#"))continue;
    int separator=separator(line);if(separator<1){logger.warn("Malformed translation in "+file+": "+line);continue;}
    String source=unescape(line.substring(0,separator)),value=unescape(line.substring(separator+1));
    if(value.trim().isEmpty())continue;
    if(!parameters(source).equals(parameters(value))){logger.warn("Translation parameters differ in "+file+": "+source);continue;}
    if(!entries.containsKey(source))entries.put(source,value);
    else if(!entries.get(source).equals(value))logger.warn("Conflicting translation in "+file+": "+source);
   }
  } catch(IOException error){logger.error("Cannot read language dictionary "+file,error);}
 }
 static int separator(String line) {
  boolean escaped=false;for(int i=0;i<line.length();i++) {
   char c=line.charAt(i);if(c=='='&&!escaped)return i;
   escaped=c=='\\'&&!escaped;
  }return -1;
 }
 static String unescape(String text) {
  StringBuilder out=new StringBuilder();for(int i=0;i<text.length();i++) {
   char c=text.charAt(i);if(c=='\\'&&i+1<text.length()) {
    char next=text.charAt(++i);switch(next){case 'n':out.append('\n');break;case 'r':out.append('\r');break;case 't':out.append('\t');break;case '\\':out.append('\\');break;case '#':out.append('#');break;case '=':out.append('=');break;default:out.append('\\').append(next);}
   }else out.append(c);
  }return out.toString();
 }
 private static Set<String> parameters(String text) {
  Set<String> values=new HashSet<>();Matcher matcher=PARAMETER.matcher(text);
  while(matcher.find())values.add(matcher.group(2));return values;
 }
 private static final class Catalogue {
  final String language;
  final Map<String,String> texts;
  final Map<String,List<Rule>> rules=new HashMap<>();
  final Map<String,List<Rule>> numericSuffixRules=new HashMap<>();
  final Map<String,String> originalKeywords=new HashMap<>(),keywordLabels=new HashMap<>();
  final Map<String,List<KeywordContext>> keywordContexts=new HashMap<>(),keywordTemplates=new HashMap<>();
  final Map<String,Set<String>> topicAliases=new HashMap<>();
  Catalogue(String language,Map<String,String> entries) {
   this.language=language;
   texts=Collections.unmodifiableMap(entries);
   // Explicit aliases take precedence over positional links in translated dialogue.
   for(Map.Entry<String,String> entry:entries.entrySet()) {
    if(entry.getKey().length()<=80&&!entry.getKey().contains("\n")&&!entry.getKey().contains("<")&&!PARAMETER.matcher(entry.getKey()).find()) {
     originalKeywords.putIfAbsent(entry.getValue().toLowerCase(Locale.ROOT),entry.getKey());
     keywordLabels.putIfAbsent(entry.getKey().toLowerCase(Locale.ROOT),entry.getValue());
    }
   }
   for(Map.Entry<String,String> entry:entries.entrySet()) {
    List<String> original=keywords(entry.getKey()),translated=keywords(entry.getValue());
    if(original.size()==translated.size())for(int i=0;i<original.size();i++)addTopicAlias(translated.get(i),original.get(i));
    List<String> sourceLinks=linkKeywords(entry.getKey()),outputLinks=linkKeywords(entry.getValue());
    if(!outputLinks.isEmpty()) {
     boolean paired=sourceLinks.size()==outputLinks.size();
     KeywordContext context=new KeywordContext(entry.getValue(),paired?sourceLinks:Collections.emptyList(),paired?outputLinks:Collections.emptyList());
     if(paired)for(int i=0;i<sourceLinks.size();i++)addTopicAlias(outputLinks.get(i),sourceLinks.get(i));
     Map<String,List<KeywordContext>> target=context.rule==null?keywordContexts:keywordTemplates;
     String key=context.rule==null?entry.getValue():context.rule.prefix;
     target.computeIfAbsent(key,ignored->new ArrayList<>()).add(context);
    }
    if(original.size()==translated.size())for(int i=0;i<original.size();i++) {
     if(PARAMETER.matcher(original.get(i)).find()||PARAMETER.matcher(translated.get(i)).find())continue;
     originalKeywords.putIfAbsent(translated.get(i).toLowerCase(Locale.ROOT),original.get(i));
     keywordLabels.putIfAbsent(original.get(i).toLowerCase(Locale.ROOT),translated.get(i));
    }
    if(PARAMETER.matcher(entry.getKey()).find()) {
     Rule rule=new Rule(entry.getKey(),entry.getValue());
     if(rule.score<(entry.getKey().startsWith("{number:")?1:5))continue;
     Map<String,List<Rule>> target=rule.numericSuffix==null?rules:numericSuffixRules;
     String key=rule.numericSuffix==null?rule.prefix:rule.numericSuffix;
     List<Rule> bucket=target.get(key);if(bucket==null){bucket=new ArrayList<>();target.put(key,bucket);}bucket.add(rule);
    }
   }
   for(List<Rule> bucket:rules.values())bucket.sort((a,b)->Integer.compare(b.score,a.score));
   for(List<Rule> bucket:numericSuffixRules.values())bucket.sort((a,b)->Integer.compare(b.score,a.score));
  }
  private static List<String> keywords(String text) {
   List<String> words=new ArrayList<>();
   Matcher marked=Pattern.compile("(?<!#)#(?!#)(?:'([^']+)'|([^\\s,.;:!?]+))").matcher(text);
   while(marked.find())words.add(marked.group(1)==null?marked.group(2):marked.group(1));return words;
  }
  private static List<String> linkKeywords(String text) {
   List<String> words=new ArrayList<>();
   Matcher marked=Pattern.compile("(?<!#)#(?!#)(?:'([^']+)'|([^\\s,.;:!?]+))").matcher(text);
   int end=-1;boolean quoted=true;
   while(marked.find()) {
    String label=marked.group(1)==null?marked.group(2):marked.group(1);
    if(!quoted&&marked.group(1)==null&&text.substring(end,marked.start()).equals(" ")) {
     words.set(words.size()-1,words.get(words.size()-1)+" "+label);
    }else words.add(label);
    end=marked.end();quoted=marked.group(1)!=null;
   }
   return words;
  }
  String contextKeyword(String word,String text) {
   Set<String> targets=new HashSet<>();
   List<KeywordContext> exact=keywordContexts.get(text);
   boolean known=exact!=null;
   if(exact!=null)for(KeywordContext context:exact)context.collect(word,targets);
   else {
    int best=-1;
    for(int length=-1;length<=Math.min(16,text.length());length++) {
     List<KeywordContext> bucket=keywordTemplates.get(length==-1?"\u0000number":text.substring(0,length));
     if(bucket==null)continue;
     for(KeywordContext context:bucket) {
      if(context.rule.score<best||!context.rule.pattern.matcher(text).matches())continue;
      known=true;
      if(context.rule.score>best){targets.clear();best=context.rule.score;}
      context.collect(word,targets);
     }
    }
   }
   return targets.size()==1?targets.iterator().next():targets.isEmpty()&&known?topicKeyword(word):null;
  }
  private void addTopicAlias(String label,String target) {
   if(PARAMETER.matcher(label+target).find())return;
   topicAliases.computeIfAbsent(label.toLowerCase(Locale.ROOT),ignored->new HashSet<>()).add(target);
  }
  String topicKeyword(String word) {
   Set<String> targets=topicAliases.get(word.trim().toLowerCase(Locale.ROOT));
   return targets!=null&&targets.size()==1?targets.iterator().next():null;
  }
  String match(String text, int depth) {
   Rule best=null;Matcher match=null;
   int digits=0;
   while(digits<text.length()&&text.charAt(digits)>='0'&&text.charAt(digits)<='9')digits++;
   if(digits>0) {
    List<Rule> quantities=numericSuffixRules.get(text.substring(digits));
    if(quantities!=null)for(Rule rule:quantities) {
     Matcher candidate=rule.pattern.matcher(text);
     if(candidate.matches()){best=rule;match=candidate;break;}
    }
   }
   for(int length=-1;length<=Math.min(16,text.length());length++) {
    if(length==-1&&(text.isEmpty()||text.charAt(0)<'0'||text.charAt(0)>'9'))continue;
    List<Rule> bucket=rules.get(length==-1?"\u0000number":text.substring(0,length));if(bucket==null)continue;
    for(Rule rule:bucket) {
     if(best!=null&&rule.score<=best.score)break;
     Matcher candidate=rule.pattern.matcher(text);
     if(candidate.matches()){best=rule;match=candidate;break;}
    }
   }
   return best==null?null:best.render(match,this,depth);
  }
 }
 private static final class KeywordContext {
  final Rule rule;
  final List<String> original,translated;
  KeywordContext(String output,List<String> original,List<String> translated) {
   this.original=original;this.translated=translated;
   rule=PARAMETER.matcher(output).find()?new Rule(output,output):null;
  }
  void collect(String word,Set<String> targets) {
   for(int start=0;start<translated.size();start++) {
    String label="",target="";
    for(int end=start;end<translated.size();end++) {
     if(PARAMETER.matcher(original.get(end)).find()||PARAMETER.matcher(translated.get(end)).find())break;
     label+=(end==start?"":" ")+translated.get(end);
     target+=(end==start?"":" ")+original.get(end);
     if(label.equalsIgnoreCase(word.trim()))targets.add(target);
     if(label.length()>=word.trim().length())break;
    }
   }
  }
 }
 private static final class Rule {
  final Pattern pattern;final String output,prefix,numericSuffix;final int score;
  final Map<String,Integer> groups=new HashMap<>();
  Rule(String source,String output) {
   this.output=output;Matcher parameters=PARAMETER.matcher(source);StringBuilder regex=new StringBuilder();int offset=0,literal=0;
   String initial=null;
   while(parameters.find()) {
    String part=source.substring(offset,parameters.start());if(initial==null)initial=part;
    literal+=part.length();regex.append(Pattern.quote(part));
    String id=parameters.group(2);Integer group=groups.get(id);
    if(group==null){group=groups.size()+1;groups.put(id,group);regex.append("number".equals(parameters.group(1))?"([0-9]+)":"(.*?)");}else regex.append("\\").append(group);
    offset=parameters.end();
   }
   String tail=source.substring(offset);literal+=tail.length();regex.append(Pattern.quote(tail));
   this.pattern=Pattern.compile(regex.toString(),Pattern.DOTALL);this.score=literal;
   String head=initial==null?source:initial;this.prefix=source.startsWith("{number:")?"\u0000number":head.substring(0,Math.min(16,head.length()));
   Matcher suffix=NUMERIC_SUFFIX.matcher(source);this.numericSuffix=suffix.matches()?suffix.group(1):null;
  }
  String render(Matcher match,Catalogue catalogue,int depth) {
   Matcher slots=PARAMETER.matcher(output);StringBuilder result=new StringBuilder();int offset=0;
   while(slots.find()) {
    result.append(output,offset,slots.start());String captured=match.group(groups.get(slots.group(2)));
    result.append("item".equals(slots.group(1))?translate(catalogue,captured,depth+1):captured);offset=slots.end();
   }
   return result.append(output.substring(offset)).toString();
  }
 }
}
