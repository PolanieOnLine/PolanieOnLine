package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class BulkNpcTranslationTest {
 @Test public void potionShopTranslatesItsComposedPriceListAndPreservesAmounts() {
  assertEquals("I buy mandrake for 300, and deadly poison for 1000\n\nI also sell small potion for 150, and mega potion for 1650",
   Translation.getText("en", "Skupuję mandragora za 300, oraz śmiertelna trucizna za 1000\n\nSprzedaję również mały eliksir za 150, oraz wielki eliksir za 1650"));
  assertEquals("greater potion for 250", Translation.getText("en", "duży eliksir za 250"));
 }

 @Test public void followupTranslatesItsAuthoredPrefixButNotThePlayersNameInAWarning() {
  assertEquals("Who, me? I do not have much to say about myself. Who else would you like to know about?",
   Translation.getText("en", "Kto ja? Nie mam wiele do powiedzenia o sobie. Kto jeszcze chciałbyś wiedzieć?"));
  assertEquals("Hey Zamknij {0}! Do not push others!", Translation.getText("en", "Hej Zamknij {0}! Nie pchaj innych!"));
 }

 @Test public void anchoredFerryCountdownsKeepTheSecondsExact() {
  String[][] cases = {
   {"na wyspie", "at the island"}, {"na pirackiej wyspie", "at the pirate island"},
   {"w Krakowie", "in Krakow"}, {"w Warszawie", "in Warsaw"}
  };
  for (String[] row : cases) {
   assertEquals("The ferry is anchored " + row[1] + ". This will take 120 seconds.",
    Translation.getText("en", "Prom jest zakotwiczony " + row[0] + ". Zajmie to 120."));
  }
 }
}
