package games.stendhal.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.factory.AbstractAchievementFactory;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.util.TimeUtil;

public class AchievementTranslationCatalogueTest {
 @BeforeClass public static void setUpWorld() { MockStendlRPWorld.get(); }

 @Test public void currentAchievementRequirementsHaveEnglishDisplayWithoutChangingSavedValues() {
  int checked = 0;
  for (AbstractAchievementFactory factory : AbstractAchievementFactory.createFactories()) {
   for (Achievement achievement : factory.createAchievements()) {
    String source = achievement.getDescription();
    String translated = Translation.getText("en", source);
    assertFalse(factory.getClass().getSimpleName() + ": " + source, source.equals(translated));
    assertEquals(source, achievement.getDescription());
    checked++;
   }
  }
  assertTrue("The audit must cover current factories, not a handful of example strings", checked > 200);
 }

 @Test public void currentAchievementTitlesAreTranslatedOrIntentionallyLanguageNeutral() {
  Set<String> neutral = new HashSet<String>(Arrays.asList("❤️", "Safari", "Major", "Farmer",
    "Aberratio Ictus", "Baron", "Baronet", "Gladiator"));
  for (AbstractAchievementFactory factory : AbstractAchievementFactory.createFactories()) {
   for (Achievement achievement : factory.createAchievements()) {
    String source = achievement.getTitle();
    if (!neutral.contains(source)) assertFalse(factory.getClass().getSimpleName() + ": " + source,
      source.equals(Translation.getText("en", source)));
    assertEquals(source, achievement.getTitle());
   }
  }
 }

 @Test public void actualApproximateTimeFormsTranslateInsideAFullNpcMessage() {
  for (int seconds : new int[]{0, 60, 95, 115, 180, 3600, 5500, 7100, 86400, 130000, 604800, 1209600}) {
   String duration = TimeUtil.approxTimeUntil(seconds);
   String result = Translation.getText("en", "Muzeum może Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za " + duration + ".");
   assertTrue(result, result.startsWith("The museum can ask you to bring an item only once a week. Return in "));
   assertFalse(duration + " -> " + result, result.matches("(?U).*\\b(?:minutę|minuty|minut|godzinę|godziny|godzin|tydzień|tygodni|dni|dzień|około|ponad|mniej niż|pół)\\b.*"));
  }
 }
}
