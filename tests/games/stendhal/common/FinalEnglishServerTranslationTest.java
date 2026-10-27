package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.chest.Chest;
import games.stendhal.server.entity.mapstuff.sign.Clock;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class FinalEnglishServerTranslationTest {
 @Test public void arenaRewardsTranslatePluralFragmentsWithoutChangingRewardCounts() {
  for(int count:new int[]{1,2,3,4,6}) {
   String fragments=count==1?"1 fragment glifu":count+" fragmentów glifu";
   String source="Ukończyłeś Arenę Wyzwań. Otrzymujesz 500 punktów doświadczenia oraz "+fragments+".";
   String expected="You completed the Challenge Arena. You receive 500 experience points and "+count
    +(count==1?" glyph fragment.":" glyph fragments.");
   assertEquals(expected,Translation.getText("en",source));
   assertEquals(expected+" You also receive a chest containing random equipment.",Translation.getText("en",
    source+" Otrzymujesz również skrzynię z losowym wyposażeniem."));
   assertEquals(source,Translation.getText("pl",source));
  }
 }
 @Test public void fishingExamTranslatesTheActualPolishQuantityForms() {
  for(int count:new int[]{1,2,5,10}) {
   String source="Brakuje wciąż "+Grammar.quantityplnoun(count,"ryba")+Grammar.isare(count)
    +" to: pstrąg. Czy masz przy sobie którąś z tych ryb?";
   String expected=count==1?"One fish is still missing: trout. Do you have it with you?":
    count+" fish are still missing: trout. Do you have any of them with you?";
   assertEquals(expected,Translation.getText("en",source));
  }
 }
 @BeforeClass public static void world() { MockStendlRPWorld.get(); }
 private Player englishPlayer() {
  Player player=PlayerTestHelper.createPlayer("Zamknij");
  player.setLanguage("en"); player.setClientVersion("1.0"); return player;
 }
 @Test public void unreachableChestDeliversEnglishWithoutOpeningIt() {
  StendhalRPZone zone=new StendhalRPZone("english_final_chest",30,30);
  Chest chest=new Chest(); Player player=englishPlayer();
  zone.add(chest);zone.add(player);chest.setPosition(20,20);player.setPosition(1,1);
  assertFalse(chest.onUsed(player));
  assertFalse(chest.isOpen());
  assertEquals("You cannot reach the chest from here.",player.events().get(0).get("text"));
 }
 @Test public void genderedGroupMessagesPreserveTheInvitedPlayersName() {
  for(String gender:new String[]{"M","F"}) {
   Player player=englishPlayer();
   player.sendPrivateText("Zaprosiłeś wojownika Zamknij, aby "+Grammar.genderVerb(gender,"dołączył")+" do grupy.");
   assertEquals("You invited Zamknij to join the group.",player.events().get(0).get("text"));
  }
 }
 @Test public void eatingWarningTranslatesBothGendersAndKeepsDamageExact() {
  for(String gender:new String[]{"M","F"}) {
   String source=Grammar.genderVerb(gender,"Zjadłeś")+" tak dużo, że "+Grammar.genderVerb(gender,"zwymiotowałeś")
    +" na ziemię i "+Grammar.genderVerb(gender,"straciłeś")+" 17 punkt życia.";
   assertEquals("You ate so much that you threw up and lost 17 HP.",Translation.getText("en",source));
   assertEquals(source,Translation.getText("pl",source));
  }
 }
 @Test public void glyphSearchKeepsCoordinatesAndWeddingRingKeepsSpouseName() {
  assertEquals("Unfortunately, you did not find what you were looking for here. You sense that a glyph fragment may be nearby: (123, 87).",
   Translation.getText("en","Niestety, w tym miejscu nie znalazłaś tego czego szukasz. Czujesz, że fragment glifu może znajdować się w pobliżu: (123, 87)."));
  assertEquals("Sorry, but Zamknij has divorced you and is now engaged to someone else.",
   Translation.getText("en","Przepraszam, ale Zamknij rozwiodła się z tobą i jest teraz zaręczony z kimś innym."));
  assertEquals("Your item wooden bow broke!",Translation.getText("en","Twój przedmiot drewniany łuk się zepsuł!"));
 }
 @Test public void clockTranslatesItsLiveOutputAndEveryMinuteIncludingSourceTypos() throws Exception {
  Clock clock=new Clock();
  String live=clock.describe();
  assertTrue(Translation.getText("en",live).startsWith("It is "));
  assertEquals(live,Translation.getText("pl",live));
  java.lang.reflect.Method minute=Clock.class.getDeclaredMethod("describeMinute",int.class);
  minute.setAccessible(true);
  for(int hour=1;hour<=12;hour++)for(int min=0;min<60;min++) {
   String source="Jest godzina "+minute.invoke(clock,min)+Grammar.clockNumberString(hour)+".";
   String translated=Translation.getText("en",source);
   assertTrue(source,translated.startsWith("It is "));
   assertFalse(source,translated.matches(".*[ąćęłńóśźż].*"));
  }
  assertEquals("It is sixteen past four.",Translation.getText("en","Jest godzina szestanście po czwarta."));
  assertEquals("It is quarter to twelve.",Translation.getText("en","Jest godzina za kwadrans dwunasta."));
 }
}
