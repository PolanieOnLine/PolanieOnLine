/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

/** Tests the final text emitted by NPCs, not just individual source fragments. */
public class RegionalNpcTranslationTest {
 @Test public void sheepdogFoodUsesTheExistingEnglishItemNames() {
  String foods = "Your sheepdog eats drumsticks, meat, ham, cottage sausage, steak, smoked sheep's cheese, or sheep's whey drink. Enjoy!";
  assertEquals("Here you are, a little Tatra sheepdog! " + foods, Translation.getText("en", "Proszę bardzo, mały owczarek podhalański! Twój owczarek żywi się udkiem, mięsem, szynką, kiełbasą swojską, stekiem, oscypek lub żyntycą. Ciesz się!"));
  assertEquals("Here you are, a little sheepdog! " + foods, Translation.getText("en", "Proszę bardzo oto mały owczarek! Twój owczarek żywi się udkiem, mięsem, szynką, kiełbasą swojską, stekiem, oscypek lub żyntycą. Ciesz się!"));
 }
 @Test public void currentArenaAndShopPricesArePreserved() {
  assertEquals("You chose a trial for 100000 gold. When you are ready, say #enter.", Translation.getText("en", "Wybrałeś próbę za 100000 sztuk złota. Gdy jesteś gotowy powiedz #wejście."));
  String sign = Translation.getText("en", " -- Skupuję -- \n buty żelazne\t 90\n złote buty\t 1500\n buty cieni\t 2000\n buty kamienne\t 2500\n buty chaosu\t 4000\n buty z zielonego potwora\t 6000\n buty xenocyjskie\t 8000");
  assertTrue(sign.contains("iron boots\t 90"));
  assertFalse(sign.contains("1000\n"));
 }
 @Test public void fullConcatenatedGreetingsAreTranslated() {
  String source = "Witaj, widzę, że już zdobyłeś wystarczającą ilość doświadczenia poprzez walki z potworami, a zatem jesteś godzien przyjąć me nauki!";
  assertEquals("Welcome, I see you have gained enough experience fighting monsters, so you are worthy of my teachings!", Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }
 @Test public void actualPouchRecipeMessageContainsTranslatedIngredients() {
  String source = "Dobra. Będę potrzebował 1 igła do skór, 2 skórzana nić, oraz 1 futro. Ponadto moja opłata wynosi 50000 money. Proszę, wróć, kiedy to będziesz miał.";
  String english = Translation.getText("en", source);
  assertTrue(english.startsWith("All right. I will need "));
  assertTrue(english.contains("50000 coins"));
  assertFalse(english.contains("igła"));
  assertFalse(english.contains("skórzana"));
  assertFalse(english, english.contains("futro"));
 }
 @Test public void psychicRepliesPreserveNicknamesAndHandleBothGenders() {
  assertEquals("Information about rat will cost 500. Would you like to pay?", Translation.getText("en", "Informacja o szczur będzie kosztować 500. Czy chciałabyś zapłacić?"));
  assertEquals("Zamknij, you killed a total of 3 rat. You defeated none alone and 3 in a group.", Translation.getText("en", "Zamknij, zabiłaś w sumie 3 szczur. Nie pokonałaś ani jednego w pojedynkę oraz 3 w grupie."));
 }
 @Test public void reboundOffersKeepNumbersAndSpecialGifts() {
  String source = "Mogę cofnąć twoją drogę po raz 3. Po tym odrodzeniu premia do każdego z limitów ATK, RATK i DEF wyniesie +24. Wyższe limity pozwalają wykorzystać wytrenowane umiejętności. Doświadczenie z potworów pozostanie na poziomie 100 procent wartości bazowej. Zachowam też dla ciebie dwa szczególne sztylety jako pamiątkę trzeciej podróży. Czy chcesz narodzić się ponownie?";
  String english = Translation.getText("en", source);
  assertTrue(english.contains("will be +24"));
  assertTrue(english.contains("100 percent of the base value"));
  assertTrue(english.contains("two special daggers"));
  assertTrue(english.endsWith("Would you like to be reborn?"));
 }
 @Test public void escapedOpeningKeywordsAreRealTranslationsNotComments() {
  assertEquals("We have already received enough food #offers. Thank you for helping.", Translation.getText("en", "#Zaoferowano nam już dość jedzenia, dziękuję za pomoc."));
  assertTrue(Translation.getText("en", "#Handluję narzędziami, ale nie mam szpulek. Są dla mnie zbyt skomplikowane do zrobienia. Spróbuj u krasnali.").startsWith("I #trade"));
 }
 @Test public void amountDependentNpcGrammarDoesNotLeakPolishWords() {
  assertEquals("Woof! Here is your angelica tincture!", Translation.getText("en", "Hau! Oto jest twoje nalewka litworowa!"));
  assertEquals("Woof! Here are your angelica tincture!", Translation.getText("en", "Hau! Oto są twoje nalewka litworowa!"));
 }
 @Test public void formattedNicknamesRemainOpaque() {
  assertEquals("Hey, Zamknij! You cannot run around my archery range for free.", Translation.getText("en", "Hej Zamknij! Nie możesz sobie biegać po mojej strzelnicy za darmo."));
 }
}
