package games.stendhal.common.grammar;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import games.stendhal.common.parser.ConversationParser;

public class TranslatedItemParserTest {
 @Test public void englishWaresResolveToCanonicalPolishName() {
  ItemParser parser=new ItemParser(new HashSet<String>(Arrays.asList("skórzany kirys","polano")));
  ItemParserResult result=parser.parse(ConversationParser.parse("buy leather cuirass"));
  assertTrue(result.wasFound());
  assertEquals("skórzany kirys",result.getChosenItemName());
  assertEquals(1,result.getAmount());
 }
 @Test public void englishAmountIsPreserved() {
  ItemParserResult result=new ItemParser("polano").parse(ConversationParser.parse("buy 8 wood"));
  assertTrue(result.wasFound());
  assertEquals("polano",result.getChosenItemName());
  assertEquals(8,result.getAmount());
 }
 @Test public void displayedPluralAndMassNounWaresResolveWithoutRewritingChat() {
  ItemParser parser=new ItemParser(new HashSet<String>(Arrays.asList("skórzany kirys","polano")));
  for(String message:new String[]{"buy 8 pieces of wood","buy 3 leather cuirasses"}) {
   games.stendhal.common.parser.Sentence sentence=ConversationParser.parse(message);
   ItemParserResult result=parser.parse(sentence);
   assertTrue(message,result.wasFound());
   assertEquals(message.startsWith("buy 8")?"polano":"skórzany kirys",result.getChosenItemName());
   assertEquals(message.startsWith("buy 8")?8:3,result.getAmount());
   assertEquals(message,sentence.getOriginalText());
  }
 }
 @Test public void unavailableEnglishItemCannotBecomeShopWare() {
  ItemParserResult result=new ItemParser("polano").parse(ConversationParser.parse("buy leather cuirass"));
  assertFalse(result.wasFound());
 }
 @Test public void polishWaresStillResolveWithoutChangingSentence() {
  games.stendhal.common.parser.Sentence sentence=ConversationParser.parse("buy 8 polano");
  ItemParserResult result=new ItemParser("polano").parse(sentence);
  assertTrue(result.wasFound());
  assertEquals("polano",result.getChosenItemName());
  assertEquals(8,result.getAmount());
  assertEquals("buy 8 polano",sentence.getOriginalText());
 }
}
