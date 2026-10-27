package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class NpcServiceContinuationTranslationTest {
 @Test public void healerRepairerAndSellerRepliesTranslate() {
  check(new String[][] {
   {"Mogę #wyleczyć poniesione rany w walce.", "I can #heal the wounds you suffered in battle."},
   {"Mogę Ci #naprawić ten przedmiot.", "I can #repair this item for you."},
   {"Dobrze w czym jeszcze mogę pomóc?", "All right, what else can I help you with?"},
   {"Dobrze. W czym jeszcze mogę tobie pomóc?", "All right. What else can I help you with?"}
  });
  assertEquals("ulecz", Translation.getOriginalKeyword("en", "heal"));
 }

 @Test public void outfitWarningsAndCancellationRepliesTranslate() {
  check(new String[][] {
   {"Jesteś pewien? Stracisz swój specjalny strój.", "Are you sure? You will lose your special outfit."},
   {"Ok, jak jeszcze mogę Ci pomóc?", "OK, how else can I help you?"},
   {"Dobrze w czym mogę jeszcze pomóc?", "All right, how else can I help you?"}
  });
 }

 @Test public void sharedConstructionRepliesTranslateAndKeepTopicLinks() {
  check(new String[][] {
   {"Jeszcze raz dziękuję za pomoc. Robimy #progres. Miejmy nadzieję, że skończymy na czas.", "Thank you again for your help. We are making #progress. Let us hope we finish on time."},
   {"Przepraszam, jestem w tej chwili bardzo zajęty, próbując zakończyć ten #projekt na czas. Prosiłbym cię o pomoc, ale wydajesz się być bardzo #niedoświadczony.", "Sorry, I am very busy trying to finish this #project on time. I would ask for your help, but you seem very #inexperienced."},
   {"Dziękuję za pomoc! W tej chwili już nie będę ci przeszkadzać. Sprawdź ponownie jutro.", "Thank you for your help! I will not trouble you any further for now. Check again tomorrow."},
   {"Dzięki twojej pomocy otrzymaliśmy cały potrzebny materiał. W krótkim czasie zakończymy budowę. Sprawdź ponownie wkrótce.", "Thanks to your help, we have received all the materials we need. We will finish construction soon. Check again shortly."},
   {"Och dziękuje. Jakie przedmioty możesz mi dostarczyć?", "Oh, thank you. Which items can you bring me?"},
   {"Dziękuję za pomoc. Ale nie potrzebujemy tego przedmiotu. Czy jest jeszcze coś z listy potrzebnych pozycji, które chciałbyś przynieść?", "Thank you for your help, but we do not need that item. Is there anything else on the list of required items that you would like to bring?"}
  });
  assertEquals("progres", Translation.getOriginalKeyword("en", "progress"));
 }

 private void check(String[][] rows) {
  for (String[] row : rows) {
   assertEquals(row[0], row[1], Translation.getText("en", row[0]));
   assertEquals(row[0], Translation.getText("pl", row[0]));
  }
 }
}
