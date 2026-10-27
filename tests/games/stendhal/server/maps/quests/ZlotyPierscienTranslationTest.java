package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.Translation;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.wieliczka.blacksmith.KowalWincentyNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ZlotyPierscienTranslationTest {
 @BeforeClass public static void setUpClasses() throws Exception { QuestHelper.setUpBeforeClass(); }
 @After public void cleanup() { NPCList.get().clear(); PlayerTestHelper.removeAllPlayers(); }

 @Test public void englishRingAndPolishPierscienBothCollectTheCanonicalReward() {
  for (String word : new String[] {"ring", "pierścień"}) {
   NPCList.get().clear();
   new KowalWincentyNPC().configureZone(new StendhalRPZone("gold_ring_translation_test"), null);
   SpeakerNPC npc = NPCList.get().get("Kowal Wincenty");
   new ZlotyPierscien().addToWorld();
   Player player = PlayerTestHelper.createPlayer("GoldRing" + word.length());
   player.setLanguage(word.equals("ring") ? "en" : "pl");
   player.setQuest("zloty_pierscien", "forging;0");
   npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
   assertTrue(word, npc.getEngine().step(player, word));
   assertEquals(word, "done", player.getQuest("zloty_pierscien"));
   assertEquals(1, player.getNumberOfEquipped("złoty pierścień"));
   assertEquals(player.getName(), player.getFirstEquipped("złoty pierścień").getBoundTo());
  }
 }

 @Test public void englishInstructionsAndHistoryUseTheAcceptedEnglishKeyword() {
  String source = "Widzę, że masz wszystko o co cię prosiłem. Wróć za 1 godzinę, a złoty pierścień będzie gotowy. Przypomnij mi mówiąc #pierścień.";
  String english = "I see you have everything I asked for. Return in 1 hour, and the golden ring will be ready. Remind me by saying #ring.";
  assertEquals(english, Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
  assertEquals("pierścień", Translation.getOriginalKeyword("en", "ring", english));
  assertEquals("Apparently Wincenty the blacksmith finished his work. Password: ring.", Translation.getText("en", "Podobno Kowal Wincenty skończył swoją robotę. Hasło: pierścień."));
  assertEquals("I must collect the ring in 1 hour. Password: ring.", Translation.getText("en", "Po pierścień mam zgłosić się za 1 godzinę. Hasło: pierścień."));
 }
}
