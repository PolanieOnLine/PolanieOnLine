package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ServerTranslationCatalogueTest {
 @Test public void blackjackTranslatesAllLinesWithoutChangingCardsTotalsOrPayout() {
  assertEquals("You have A♣.\nThe dealer drew K♦.\nYou have 21.\nThe dealer has 20.\nYou won. Congratulations! Here is your stake plus 50 gold.",
    Translation.getText("en","Masz A♣.\nBank dostał K♦.\nMasz 21.\nBank ma 20.\nWygrałeś. Gratulacje! Oto twoja stawka plus 50 złota."));
 }
 @Test public void beerUsesTheSameNameInDescriptionsQuantitiesAndNpcKeywords() {
  assertEquals("beer",Translation.getText("en","sok z chmielu"));
  assertEquals("beer",Translation.getText("en","soku z chmielu"));
  assertEquals("You see beer. Perfect for hot days.",Translation.getText("en","Oto sok z chmielu. Najlepszy na upalne dni."));
  assertEquals("3 bottles of beer",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnoun(3,"sok z chmielu")));
  assertEquals("sok z chmielu",Translation.getOriginalKeyword("en","beer"));
 }
 @Test public void singularCreatureGrammarDoesNotUsePluralDisplayNames() {
  assertEquals("Baba Dżaga",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnounCreature(1,"Baba Dżaga")));
  assertEquals("Baba Jaga",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnounCreature(1,"Baba Jaga")));
  assertEquals("Baba Laga",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnounCreature(1,"Baba Laga")));
  assertEquals("Chief Falatheen of the Risecia",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnounCreature(1,"Falatheen szef Risecia")));
  assertEquals("2 Baba Dżagas",Translation.getText("en",games.stendhal.common.grammar.Grammar.quantityplnounCreature(2,"Baba Dżaga")));
 }
 @Test public void statusEffectsLeaveAttackerNameUnchanged() {
  assertEquals("You were stunned by Zamknij.",Translation.getText("en","Zostałeś ogłuszony przez Zamknij."));
  assertEquals("You are no longer stunned.",Translation.getText("en","Nie jesteś już ogłuszony."));
 }
 @Test public void tradeMessagePreservesBothPlayerNames() {
  assertEquals("Zamknij wants to trade with you. Right-click polano and choose \"Trade\" to begin.",
    Translation.getText("en","Zamknij chcę pohandlować z tobą. Naciśnij prawy przycisk na polano i wybierz \"Handluj\", aby rozpocząć handlowanie."));
 }
 @Test public void punishmentPreservesReasonEnteredByAdministrator() {
  assertEquals("You were muted by Zamknij for 5 minutes. Reason: Nie możesz tego zrobić..",
    Translation.getText("en","Zostałeś uciszony przez Zamknij na 5 minutę. Powód: Nie możesz tego zrobić.."));
 }
 @Test public void explicitlyTranslatedTimeListsKeepAmounts() {
  assertEquals("The ring has not fully recovered its power yet. You think it will be ready in 2 hours, 12 minutes.",
    Translation.getText("en","Pierścień jeszcze nie odzyskał w pełni swojej mocy. Myślisz, że będzie gotowy w ciągu 2 godziny, 12 minut."));
 }
 @Test public void escapedHashSourcesAreNotTreatedAsComments() {
  assertEquals("#'Open quests': ",Translation.getText("en","#'Otwarte zadania': "));
 }
 @Test public void quantityFormsKeepExactAmountsInTradeOffers() {
  assertEquals("I will pay you 4000 coins for 8 amethysts. Will you sell it?",
    Translation.getText("en","Dam ci 4000 monet za 8 sztuk ametystu. Sprzedajesz?"));
  assertEquals("I will pay you 1 coin for amethyst. Will you sell it?",
    Translation.getText("en","Dam ci 1 monetę za ametyst. Sprzedajesz?"));
 }
 @Test public void markedIngredientsAndConjunctionsTranslateWithoutChangingAmounts() {
  assertEquals("I can make 3 #'leather cuirasses' if you bring me 8 #'black iron bars' and 5 #'pieces of wood'.",
    Translation.getText("en","Mogę zrobić 3 #'skórzany kirys' jeżeli przyniesiesz mi 8 #'sztabka czarnego żelaza' oraz 5 #polana."));
 }
 @Test public void unrecognisedPolishHtmlDoesNotTranslateConjunctions() {
  assertEquals("<html>jedna nazwa i druga nazwa</html>",Translation.getText("pl","<html>jedna nazwa i druga nazwa</html>"));
 }
 @Test public void fullWaitMessagesTranslateBothPrefixAndRemainingTime() {
  assertEquals("I haven't finished fixing your ring of life. Please check back in 2 hours, 12 minutes.",
    Translation.getText("en","Nie skończyłem jeszcze naprawy twego pierścienia życia. Powróć proszę za 2 godziny, 12 minut."));
  assertEquals("The museum can ask you to bring an item only once a week. Return in 3 days.",
    Translation.getText("en","Muzeum może Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za 3 dni."));
 }
 @Test public void achievementNamesAndRequirementsKeepThresholdsAndRegionNames() {
  assertEquals("Ados Helper",Translation.getText("en","Pomocnik Ados"));
  assertEquals("Highland Shepherd",Translation.getText("en","Juhas"));
  assertEquals("Completed the daily creature quest 1,000 times",Translation.getText("en","Ukończono codzienne zadanie na potwory 1,000 razy"));
  assertEquals("Collected 10,000,000 coins from creatures",Translation.getText("en","Zdobyto 10,000,000 monet na potworach"));
  assertEquals("Visited every underground area in the Warszawa region",Translation.getText("en","Odwiedzono wszystkie podziemne obszary w regionie Warszawa"));
 }
 @Test public void invasionCountersTranslateGeneratedVerbsAndKeepTheCorrectNumber() {
  assertEquals("There are still about 2 rats alive.",Translation.getText("en","Wciąż jest są około 2 żywych szczurów."));
  assertEquals("There is still about 1 rat alive.",Translation.getText("en","Wciąż jest jest około 1 żywych szczurów."));
 }
 @Test public void questRepliesTranslateTheWholeGeneratedRewardSentence() {
  assertEquals("Ah, it's ready at last! That is very good news indeed! Here, let me give you a little something for your help... Take these new leather legs! Let me know if you need anything.",
    Translation.getText("en","W końcu gotowe! To dobra wiadomość! Pozwól mi się odwdzięczyć za twoją pomoc... Weź te nowe skórzane spodnie! Daj mi znać jeżeli będziesz czegoś potrzebował."));
 }
 @Test public void sokobanTranslatesTheGeneratedOrdinalAndDurationButNotTheNickname() {
  assertEquals("Congratulations Zamknij, you completed the first level in 2 hours",Translation.getText("en","Gratulacje Zamknij ukończyłeś pierwszy level in 2 godziny"));
  assertEquals("Congratulations polano, you completed the 21st level in 3 days",Translation.getText("en","Gratulacje polano ukończyłeś 21st level in 3 dni"));
 }
}
