package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class GameAnnouncementTranslationTest {
 @BeforeClass public static void setUpClasses() {
  MockStendlRPWorld.get();
  PlayerTestHelper.generatePlayerRPClasses();
 }
 @After public void clearPlayers() { MockStendhalRPRuleProcessor.get().clearPlayers(); }
 @Test public void npcAnnouncementsUseEachViewersLanguageAndKeepTheirNotificationType() {
  MockStendhalRPRuleProcessor processor=MockStendhalRPRuleProcessor.get();
  Player english=PlayerTestHelper.createPlayer("Zamknij"),polish=PlayerTestHelper.createPlayer("polano");
  english.setLanguage("en");
  processor.addPlayer(english);
  processor.addPlayer(polish);
  processor.tellAllPlayersTranslated(NotificationType.PRIVMSG,"Witaj.");
  assertEquals("Hello.",english.events().get(0).get("text"));
  assertEquals("Witaj.",polish.events().get(0).get("text"));
  assertEquals(english.events().get(0).get("texttype"),polish.events().get(0).get("texttype"));
  english.clearEvents();polish.clearEvents();
  processor.tellAllPlayers(NotificationType.PRIVMSG,"Zamknij");
  assertEquals("Zamknij",english.events().get(0).get("text"));
  assertEquals("Zamknij",polish.events().get(0).get("text"));
 }
}
