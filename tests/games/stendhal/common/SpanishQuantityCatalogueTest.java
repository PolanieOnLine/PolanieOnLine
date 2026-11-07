package games.stendhal.common;

import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import games.stendhal.common.grammar.Grammar;
import org.junit.Test;

/** Tests the catalogues without requiring Spanish runtime/index wiring. */
public class SpanishQuantityCatalogueTest {
 private static final Map<String,String> TEXTS=load();
 private static final int[] COUNTS={0,1,2,3,4,5,11,12,21,22,25,101,112};
 private static Map<String,String> load() {
  Map<String,String> result=new LinkedHashMap<>();
  List<String> catalogues=new ArrayList<>();catalogues.add("es.txt");
  try {catalogues.addAll(Files.readAllLines(Paths.get("data/languages/es-index.txt"),StandardCharsets.UTF_8));}
  catch(IOException error){throw new IllegalStateException(error);}
  for(String file:catalogues) {
   if(file.trim().isEmpty()||file.trim().startsWith("#"))continue;
   try(BufferedReader reader=new BufferedReader(new InputStreamReader(SpanishQuantityCatalogueTest.class.getClassLoader().getResourceAsStream("data/languages/"+file),StandardCharsets.UTF_8))) {
    String line;while((line=reader.readLine())!=null){
     if(line.trim().isEmpty()||line.trim().startsWith("#"))continue;
     int split=-1;boolean escaped=false;
     for(int i=0;i<line.length();i++){if(line.charAt(i)=='='&&!escaped){split=i;break;}escaped=line.charAt(i)=='\\'&&!escaped;}
     if(split<1)throw new IllegalStateException(file+": malformed entry "+line);
     String key=decode(line.substring(0,split)),value=decode(line.substring(split+1));
     if(result.containsKey(key)&&!result.get(key).equals(value))throw new IllegalStateException("Conflicting form "+key);
     result.put(key,value);
    }
   }catch(IOException error){throw new IllegalStateException(error);}
  }
  return result;
 }
 private static String decode(String value){return value.replace("\\=","=").replace("\\#","#").replace("\\n","\n").replace("\\r","\r").replace("\\t","\t").replace("\\\\","\\");}
 private static String display(String source) {
  String exact=TEXTS.get(source);if(exact!=null)return exact;
  Matcher amount=Pattern.compile("^([0-9]+)( .+)$").matcher(source);
  if(amount.matches()) {
   String template=TEXTS.get("{number:0}"+amount.group(2));
   if(template!=null)return template.replace("{0}",amount.group(1));
  }
  return null;
 }
 private static Set<String> definitions(String kind,String tag)throws IOException {
  Set<String> names=new TreeSet<>();
  try(java.util.stream.Stream<Path> files=Files.walk(Paths.get("data/conf/"+kind))) {
   for(Path file:(Iterable<Path>)files.filter(p->p.toString().endsWith(".xml"))::iterator) {
    String xml=new String(Files.readAllBytes(file),StandardCharsets.UTF_8).replaceAll("(?s)<!--.*?-->","");
    Matcher matches=Pattern.compile("<"+tag+" name=\"([^\"]+)\"").matcher(xml);
    while(matches.find())names.add(matches.group(1).replace("&quot;","\"").replace("&amp;","&"));
   }
  }
  return names;
 }
 private static void quantityExists(String canonical,int count,String source) {
  String translated=display(source);
  assertNotNull("Missing form for "+canonical+", count "+count+": "+source,translated);
  if(source.startsWith(count+" "))assertTrue("Changed amount: "+source+" => "+translated,translated.startsWith(count+" "));
 }
 @Test public void everyCurrentItemHasAllActualQuantityAndTradeForms()throws Exception {
  Set<String> names=definitions("items","item");assertEquals(889,names.size());
  for(String name:names)for(int count:COUNTS){
   quantityExists(name,count,Grammar.quantityplnoun(count,name));
   quantityExists(name,count,Grammar.itemQuantityAccusative(count,name));
  }
 }
 @Test public void everyCurrentCreatureHasAllActualQuantityForms()throws Exception {
  Set<String> names=definitions("creatures","creature");assertEquals(641,names.size());
  for(String name:names)for(int count:COUNTS)quantityExists(name,count,Grammar.quantityplnounCreature(count,name));
 }
 @Test public void oneDoesNotAcquireAnIncorrectPlural() {
  assertEquals("cerveza",display("sok z chmielu"));
  assertEquals("3 botellas de cerveza",display(Grammar.quantityplnoun(3,"sok z chmielu")));
  assertEquals("1 manzana",display("1 jabłko"));
  assertEquals("1 moneda",display("1 money"));
  assertEquals("1 par de botas negras",display("1 czarne buty"));
  assertEquals("1 par de pantalones de vampiro",display("1 spodnie wampirze"));
  assertEquals("1 trozo de mineral de hierro",display("1 ruda żelaza"));
  assertEquals("serafín",display(Grammar.quantityplnounCreature(1,"serafin")));
  assertEquals("Baba Dżaga",display(Grammar.quantityplnounCreature(1,"Baba Dżaga")));
 }
 @Test public void quantitiesAgreeAcrossCompoundNamesAndAdjectives() {
  assertEquals("2 dragones negros de dos cabezas",display(Grammar.quantityplnounCreature(2,"dwugłowy czarny smok")));
  assertEquals("5 gigantes de hielo ancianos",display(Grammar.quantityplnounCreature(5,"lodowy starszy olbrzym")));
  assertEquals("22 grandes lobos feroces",display(Grammar.quantityplnounCreature(22,"wielki zły wilk")));
  assertEquals("3 capitanes elfos oscuros",display(Grammar.quantityplnounCreature(3,"elf ciemności kapitan")));
  assertEquals("12 armaduras de placas de acero mágicas",display(Grammar.quantityplnoun(12,"magiczna zbroja płytowa")));
  assertEquals("2 cascos de cota de malla mágicos",display(Grammar.quantityplnoun(2,"magiczny hełm kolczy")));
  assertEquals("5 huevos de pascua pequeños",display(Grammar.quantityplnoun(5,"małe jajo wielkanocne")));
 }
 @Test public void pairedAndMassNounsUseNaturalUnitsWithoutChangingTheirAmount() {
  assertEquals("2 pares de botas negras",display(Grammar.quantityplnoun(2,"czarne buty")));
  assertEquals("5 trozos de mineral de hierro negro",display(Grammar.quantityplnoun(5,"ruda czarnego żelaza")));
  assertEquals("3 madejas de hilo de mithril",display(Grammar.quantityplnoun(3,"przędza z mithrilu")));
  assertEquals("2 raciones de sopa de pescado",display(Grammar.quantityplnoun(2,"zupa rybna")));
  assertEquals("2 unidades de infantería blordrough",display(Grammar.quantityplnounCreature(2,"piechota blordrough")));
  assertEquals("5 serafines",display(Grammar.quantityplnounCreature(5,"serafin")));
 }
}
