package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import games.stendhal.common.grammar.Grammar;

public class BulkDynamicQuestTranslationTest {
 @Test public void collectionJournalsTranslateActualGenderAndQuantityForms() {
  String amount = Grammar.quantityplnoun(3, "szynka");
  assertEquals("3 szynki", amount);
  for (String gender : new String[] {"M", "F"}) {
   String found = Grammar.genderVerb(gender, "Znalazłem");
   check(found + " " + amount + " do pomocy Ados i muszę je dostarczyć.",
    "I found 3 pieces of ham to help Ados and must deliver it.");
   check(found + " " + amount + " do pomocy Zakopanemu i muszę je dostarczyć.",
    "I found 3 pieces of ham to help Zakopane and must deliver it.");
   String asked = Grammar.genderVerb(gender, "Zostałem") + " " + Grammar.genderVerb(gender, "poproszony");
   check(asked + ", aby przynieść " + amount + " do muzeum w Kirdneh.",
    "I was asked to bring 3 pieces of ham to the museum in Kirdneh.");
   check(asked + ", aby przynieść " + amount + " do ratusza w Wieliczce.",
    "I was asked to bring 3 pieces of ham to the town hall in Wieliczka.");
   check(asked + ", aby przynieść " + amount + " dla zwierząt.",
    "I was asked to bring 3 pieces of ham for the animals.");
  }
 }

 @Test public void bestiaryQuestionsTranslateTheAssembledCreatureName() {
  check("Jaki jego poziom wynosi szczur?", "What is the level of rat?");
  check("Ile zdrowia posiada szczur?", "How much health does rat have?");
  check("Ile obrony posiada szczur?", "How much defense does rat have?");
  check("Jaki poziom ataku posiada szczur?", "What is the attack level of rat?");
 }

 @Test public void islandJournalUsesActualCombatSuffixAndGrammar() {
  String[] prefixes = {"Opuściłem wyspę.", "Aktualnie przebywam na wyspie"};
  String[] english = {"I left the island", "I am currently on the island"};
  for (int p = 0; p < prefixes.length; p++) {
   for (int count : new int[] {1, 3, 12}) {
    String source = prefixes[p] + " i tam " + Grammar.isare(count) + " "
     + Grammar.quantityplnoun(count, "potwór") + " pozostały do \u200b\u200bpokonania.";
    check(source, english[p] + " with " + (count == 1 ? "one monster" : count + " monsters") + " still to defeat.");
   }
  }
  check(prefixes[0] + " i pokonałem wszystkich wrogów, których tam spotkałem.",
   "I left the island after defeating all the enemies I encountered there.");
  check(prefixes[1] + " i pokonałem wszystkich wrogów, których tam spotkałem.",
   "I am currently on the island and have defeated all the enemies I encountered there.");
 }

 @Test public void completionJournalsIncludeNestedGenderVerbExpansion() {
  for (String gender : new String[] {"M", "F"}) {
   check(Grammar.genderVerb(gender, "Zabiłem") + " wszystkich żołnierzy blordroughs i za wsparcie "
    + Grammar.genderVerb(gender, "otrzymałem") + " nagrodę od Mrotho",
    "I killed all the blordrough soldiers and received a reward from Mrotho for my help.");
   check(Grammar.genderVerb(gender, "Wziąłem") + " wcześniej wodę do Xhiphin Zohos i "
    + Grammar.genderVerb(gender, Grammar.genderVerb(gender, "otrzymałem")) + " od niego trochę mikstur.",
    "I took water to Xhiphin Zohos earlier and received some potions from him.");
  }
 }

 @Test public void bankCountdownPreservesPlayerNameAndUsesOneComma() {
  String name = "Zamknij {0}";
  for (int minutes : new int[] {1, 3, 12}) {
   check(name + ", " + Grammar.plnoun(minutes, "zostało") + " Tobie 2 godziny.",
    name + ", you have 2 hours left.");
  }
 }

 private static void check(String source, String expected) {
  assertEquals(source, expected, Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }
}
