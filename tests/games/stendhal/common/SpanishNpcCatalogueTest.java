/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;

/** Keeps translated NPC text separate from canonical source and player names. */
public class SpanishNpcCatalogueTest {
 private Map<String,String> read(String file) throws IOException {
  Map<String,String> result=new LinkedHashMap<>();
  InputStream stream=getClass().getClassLoader().getResourceAsStream("data/languages/"+file);
  assertNotNull(file,stream);
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,"UTF-8"))) {
   String line;
   while((line=reader.readLine())!=null) {
    if(line.isEmpty()||line.startsWith("#"))continue;
    int split=-1;boolean escape=false;
    for(int i=0;i<line.length();i++) {
     char value=line.charAt(i);
     if(value=='='&&!escape){split=i;break;}
     escape=value=='\\'&&!escape;
    }
    assertTrue(file+": missing separator",split>0);
    result.put(decode(line.substring(0,split)),decode(line.substring(split+1)));
   }
  }
  return result;
 }
 private String decode(String source) {
  StringBuilder result=new StringBuilder();
  for(int i=0;i<source.length();i++) {
   char c=source.charAt(i);
   if(c=='\\'&&i+1<source.length()) {
    c=source.charAt(++i);
    if(c=='n')c='\n';else if(c=='r')c='\r';else if(c=='t')c='\t';
   }
   result.append(c);
  }
  return result.toString();
 }
 private Map<String,String> allSpanish() throws IOException {
  Map<String,String> result=read("es.txt");
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("data/languages/es-index.txt"),"UTF-8"))) {
   String line;
   while((line=reader.readLine())!=null)if(!line.trim().isEmpty()&&!line.startsWith("#"))result.putAll(read(line.trim()));
  }
  return result;
 }
 private Set<String> parameters(String text) {
  Set<String> result=new HashSet<>();Matcher matcher=Pattern.compile("\\{(?:(?:item|number):)?(\\d+)\\}").matcher(text);
  while(matcher.find())result.add(matcher.group(1));return result;
 }
 @Test public void allCompletedRegionalNpcSourcesHaveSpanishTranslations() throws Exception {
  Map<String,String> spanish=allSpanish();
  for(String file:Arrays.asList("en-npcs-ados.txt","en-npcs-regions.txt","en-npcs-composed.txt"))for(String source:read(file).keySet())assertTrue(source,spanish.containsKey(source));
 }
 @Test public void savedNpcSourcesRemainExactAndParametersRemainIntact() throws Exception {
  Set<String> verified=new HashSet<>();
  for(String file:Arrays.asList("en-npcs-ados.txt","en-npcs-regions.txt","en-npcs-composed.txt","en-keywords.txt"))verified.addAll(read(file).keySet());
  for(String file:Arrays.asList("es-npcs-ados.txt","es-npcs-regions.txt","es-npcs-composed.txt"))for(Map.Entry<String,String> entry:read(file).entrySet()) {
   assertTrue(entry.getKey(),verified.contains(entry.getKey()));
   assertFalse(entry.getValue().isEmpty());
   assertFalse(entry.getKey().contains("tokens truncated"));
   assertFalse(entry.getValue().contains("tokens truncated"));
   assertEquals(entry.getKey(),parameters(entry.getKey()),parameters(entry.getValue()));
  }
 }
 @Test public void rawNicknamesAreNotTranslatedAsItems() throws Exception {
  Map<String,String> composed=read("es-npcs-composed.txt");
  assertEquals("¡Eh, {0}! No puedes corretear gratis por mi campo de tiro.",composed.get("Hej {0}! Nie możesz sobie biegać po mojej strzelnicy za darmo."));
  String source="{0}, zabiłaś w sumie {1} {2}. Nie pokonałaś ani jednego w pojedynkę oraz {4} w grupie.";
  String text=composed.get(source);
  assertNotNull(text);assertTrue(text.startsWith("{0},"));assertTrue(text.contains("{item:2}"));assertFalse(text.contains("{item:0}"));
 }
 @Test public void executableObservationCommandIsNotLocalized() throws Exception {
  Map<String,String> regional=read("es-npcs-regions.txt");
  String source="Na arenie walczą: {0}. Wybierz gracza w oknie obserwacji albo wpisz: obserwuj {1}.";
  assertTrue(regional.get(source).endsWith("obserwuj {1}."));
 }
 @Test public void explicitKeywordAliasesMatchSpanishWordOrder() throws Exception {
  Map<String,String> ados=read("es-npcs-ados.txt");
  assertEquals("pergaminos",ados.get("zwoje"));
  assertEquals("mágicos",ados.get("magiczne"));
  assertEquals("personas amables",ados.get("fajnych ludzi"));
  assertEquals("comida deliciosa",ados.get("przepyszne jedzenie"));
 }
}
