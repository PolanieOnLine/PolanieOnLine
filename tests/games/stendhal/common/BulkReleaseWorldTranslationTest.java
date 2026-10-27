package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.util.TimeUtil;

/** Regressions for complete runtime messages, after server substitutions. */
public class BulkReleaseWorldTranslationTest {
 @Test public void requiredItemsTranslateAfterBracketSubstitution() {
  String item = Grammar.quantityplnoun(10, "ser");
  String[][] messages = {
   {"Jeszcze nie przyniosłeś " + item + ". Idź i zdobądź, a wtedy wróć i powiedz #załatwione jak skończysz.", "You have not brought 10 pieces of cheese yet. Go and get it, then return and say #done when you finish."},
   {"Jeszcze nie masz " + item + ". Idź i zdobądź, a wtedy wróć i powiedz #załatwione jak skończysz.", "You do not have 10 pieces of cheese yet. Go and get it, then return and say #done when you finish."},
   {"Nie posiadasz " + item + " Co za szkoda.", "You do not have 10 pieces of cheese. What a pity."},
   {"Hm, nie masz " + item + ", nie próbuj mnie oszukać!", "Hmm, you do not have 10 pieces of cheese. Do not try to trick me!"},
   {"Bardzo dobrze, wróć kiedy będziesz mieć the " + Grammar.plnoun(10, "ser") + " z sobą.", "Very well, return when you have the pieces of cheese with you."},
   {"Jesteś na misji, aby znaleźć " + item + ". Nie możesz jeszcze poprosić o nowy przedmiot.", "You are on a quest to find 10 pieces of cheese. You cannot ask for a new item yet."},
   {"Jesteś na misji, aby znaleźć " + item + ". Czy chcesz poszukać innego przedmiotu?", "You are on a quest to find 10 pieces of cheese. Would you like to look for another item?"},
   {"Zatem przynieś mi " + item + ".", "Then bring me 10 pieces of cheese."},
   {"Już masz zadanie przyniesienia do muzeum " + item + ". Powiedz #zakończone jeżeli będziesz miał ze sobą.", "You already have a quest to bring 10 pieces of cheese to the museum. Say #done when you have it with you."},
   {"Już masz zadanie przyniesienia do muzeum " + item + ". Powiedz #zakończone jeżeli będziesz miał ze sobą. Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej.", "You already have a quest to bring 10 pieces of cheese to the museum. Say #done when you have it with you. This item may be rare at the moment. I can give you #another quest, or you can return with the item I requested earlier."},
   {"Już masz zadanie, aby przynieść " + item + ". Powiedz #zakończone jeśli zdobędziesz już ten przedmiot.", "You already have a quest to bring 10 pieces of cheese. Say #done when you obtain the item."},
   {"Już masz zadanie, aby przynieść " + item + ". Powiedz #zakończone jeśli zdobędziesz " + item + ". Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej.", "You already have a quest to bring 10 pieces of cheese. Say #done when you obtain 10 pieces of cheese. This item may be rare at the moment. I can give you #another quest, or you can return with the item I requested earlier."},
   {"Bardzo dobrze, wróć kiedy będziesz mieć the " + Grammar.plnoun(10, "ser") + " ze sobą.", "Very well, return when you have the pieces of cheese with you."},
   {"Już jesteś w trakcie zbierania " + item + ".", "You are already collecting 10 pieces of cheese."},
   {"*westchnienie* SPECCJALNIE powiedziałam, że potrzebujemy " + item + "!", "*sigh* I SPECIFICALLY said we need 10 pieces of cheese!"}
  };
  for (String[] message : messages) check(message[0], message[1]);
 }

 @Test public void countdownTranslatesTheAssembledTime() {
  check("Hau! Dziękuję mój magazyn z zapasami do zrobienia dobrej #nalewki wystarczy na " + TimeUtil.approxTimeUntil(300) + ".",
   "Woof! Thank you. My supplies for making good #liqueur will last for 5 minutes.");
 }

 @Test public void itemAmountsAndOpaqueNamesSurviveRuntimeMatching() {
  for (int count : new int[] {1, 2, 5, 25}) {
   String amount = Grammar.quantityplnoun(count, "ser");
   String english = count == 1 ? "cheese" : count + " pieces of cheese";
   check("Już jesteś w trakcie zbierania " + amount + ".", "You are already collecting " + english + ".");
  }
  check("Oto Zamknij {0}. Zamknij {0}, która wpatruje się w Otwórz {1}.",
   "You see Zamknij {0}. Zamknij {0} is gazing at Otwórz {1}.");
 }

 @Test public void luaShopRepliesAndSignsTranslate() {
  String[][] messages = {
   {"Proszę spójrz na tablicę, by sprawdzić przedmioty jakie mam na sprzedaż oraz ich cenę.", "Please check the board for the items I sell and their prices."},
   {"Wybacz, ale nie potrzebuję niczego w czym mógłbyś mi pomóc na tę chwilę.", "Sorry, there is nothing you can help me with at the moment."},
   {"Możesz zakupić te przedmioty.", "You can buy these items."},
   {"Sprawdź na tablicy listę przedmiotów, które skupuję.", "Check the board for the list of items I buy."},
   {"Nie, dziękuję. Nie potrzebuję pomocy.", "No, thank you. I do not need help."},
   {"Barter Materiałów Pochodzenia Zwierzęcego", "Animal Materials Exchange"},
   {"Cóż... Nie mam teraz nic, w czym mógłbyś mi pomóc.", "Well... There is nothing you can help me with right now."},
   {"W drzewie zostało coś jest wyryte.", "Something has been carved into the tree."},
   {"W niczym nie potrzebuję pomocy poza zbieraniem materiałów, aby zarabiać na sklepach w Faimouni.", "I only need help collecting materials to make money from the shops in Faimouni."},
   {"Zamknij {0} skupuje następujące przedmioty", "Zamknij {0} buys the following items"},
   {"Oto Emma. Emma, która wpatruje się w Ari.", "You see Emma. Emma is gazing at Ari."},
   {"Oto Ari. Ari, który wpatruje się w Emma.", "You see Ari. Ari is gazing at Emma."},
   {"Oto Zamknij {0}. Wygląda na właściciela lombardu, który aktualnie prowadzi.", "You see Zamknij {0}. He appears to own the pawn shop he is currently running."},
   {"Oto twój nowy wzmocniony pierścień cesarski.", "Here is your new enhanced imperial ring."},
   {"Dobrze, że przypominasz. Oto twój nowy wzmocniony pierścień zdrowia!", "Thank you for reminding me. Here is your new reinforced health ring!"}
  };
  for (String[] message : messages) check(message[0], message[1]);
 }

 private static void check(String source, String expected) {
  assertEquals(source, expected, Translation.getText("en", source));
  assertEquals(source, Translation.getText("pl", source));
 }
}
