package games.stendhal.server.entity.npc.condition;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.Translation;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class TriggerExactlyTranslationTest {
 @BeforeClass public static void setUpClasses() {
  MockStendlRPWorld.get();
  PlayerTestHelper.generatePlayerRPClasses();
 }
 @Test public void displayedPhraseIsAcceptedWithoutChangingThePlayersOriginalText() {
  Player player=PlayerTestHelper.createPlayer("Zamknij");
  player.setLanguage("en");
  TriggerExactlyInListCondition condition=new TriggerExactlyInListCondition("Witaj.");
  Sentence input=ConversationParser.parse("Hello.");
  assertTrue(condition.fire(player,input,null));
  assertEquals("Hello.",input.getOriginalText());
  assertTrue(condition.fire(player,ConversationParser.parse("Witaj."),null));
  assertFalse(condition.fire(player,ConversationParser.parse("Hello and goodbye."),null));
 }
 @Test public void canonicalTriggersStillWorkForPlayersWithoutALocalizedPhrase() {
  TriggerExactlyInListCondition condition=new TriggerExactlyInListCondition("Bez słownikowej frazy.");
  assertTrue(condition.fire(null,ConversationParser.parse("Bez słownikowej frazy."),null));
  Player player=PlayerTestHelper.createPlayer("polano");
  assertFalse(condition.fire(player,ConversationParser.parse("An unrelated sentence."),null));
 }
 @Test public void everyDisplayedFoundGirlRhymeCanBeRepeatedInEnglish() {
  Player player=PlayerTestHelper.createPlayer("RhymeTester");
  player.setLanguage("en");
  String[] phrases={"Kółko jest okrągłe,","nie ma końca.","To jak długie,","Będę twoim przyjacielem.",
    "Zdobywajmy przyjaciół,","ale utrzymujmy kontakty ze starymi.","Jeden jest srebrny,","a inny złoty."};
  for(String source:phrases) {
   String displayed=Translation.getText("en",source);
   assertNotEquals(source,displayed);
   Sentence input=ConversationParser.parse(displayed);
   TriggerExactlyInListCondition condition=new TriggerExactlyInListCondition(source,source.substring(0,source.length()-1));
   assertTrue(source+" -> "+displayed,condition.fire(player,input,null));
   assertEquals(displayed,input.getOriginalText());
  }
 }
}
