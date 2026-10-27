/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class MzQuestTranslationTest {
 @Test public void historiesUseTheSameEnglishForBothGenders() {
  String male="Obiecałem Tomaszowi, że przyniosę wystarczająco dużo drewna, aby mógł naprawić swoją łódź.";
  assertEquals("I promised Tomasz to bring enough wood to repair his boat.", Translation.getText("en",male));
  assertEquals(Translation.getText("en",male),Translation.getText("en",male.replace("Obiecałem","Obiecałam")));
  assertEquals(male,Translation.getText("pl",male));
 }
 @Test public void dynamicQuestionsTranslateTheirEmbeddedTextNotPlayerNames() {
  assertEquals("Welcome back! I believe you have studied and can answer the question. Are you ready for the test?",Translation.getText("en","Wróciłaś! Wierzę, że studiowałaś i możesz odpowiedzieć na pytanie. Jesteś gotowy na test?"));
  assertEquals("You are already married to Zamknij! You cannot marry again.",Translation.getText("en","Jesteś już żonaty z Zamknij! Nie możesz jeszcze raz wziąć ślubu."));
 }
 @Test public void housePricesTaxesAndNumbersArePreserved() {
  String source="Nowy dom w Zakopanym kosztuje 1500000 money. Ponadto trzeba zapłacić podatek 7000 money, co miesiąc. Jeśli masz jakiś dom na oku powiedz jego numer, sprawdzę czy jest wolny. Domy w Zakopanym mają numery od 1 do 9.";
  String english=Translation.getText("en",source);
  assertTrue(english.contains("1500000 coins"));assertTrue(english.contains("7000 coins"));assertTrue(english.contains("1 to 9"));
 }
 @Test public void currentGoldenHornRecipeIsNotAnOldUpstreamRecipe() {
  String source="Przynieś mi wszystkie pióra naraz\n#'100 piórek gołębich'\n#'20 piór anioła'\n#'10 piór archanioła'\n#'8 piór mrocznego anioła'\n#'20 piór upadłego anioła'\n#'7 piór archanioła ciemności'\n#'2 pióra serafina'";
  String english=Translation.getText("en",source);
  assertTrue(english.contains("#'100 pigeon feathers'"));assertTrue(english.contains("#'8 dark angel feathers'"));assertTrue(english.contains("#'2 seraph feathers'"));
  assertFalse(english.contains("piór"));
 }
 @Test public void substitutedItemNameIsTranslatedAfterTheQuestReplacesIt() {
  String source="Genialnie! Powoli wezmę się do pracy i twoja #'złota ciupaga' będzie gotowa za około 6 godzin. Wróć gdy skończę!";
  assertEquals("Brilliant! I will start working slowly, and your #'golden highland axe' will be ready in about 6 hours. Return when I finish!",Translation.getText("en",source));
 }
 @Test public void skillTestOrdinalsAndCooldownsRemainReadable() {
  assertEquals("That was your third and final move. Let me check your work.",Translation.getText("en","To był twój trzeci i ostatni ruch. Pozwól mi sprawdzić twoją pracę."));
  assertEquals("I am still working on your ring. Come back in 5 minutes.",Translation.getText("en","Wciąż pracuje nad twoim pierścieniem. Wróć za 5 minut."));
 }
}
