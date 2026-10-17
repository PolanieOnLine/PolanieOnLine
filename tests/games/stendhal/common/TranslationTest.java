package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class TranslationTest {
	@Test public void repeatedQuestCountsTranslateNumbersAndMayorJournalEntries() {
		assertEquals("2 times", Translation.getText("en", "2 razy"));
		assertEquals("12 times", Translation.getText("en", "12 razy"));
		assertEquals("1 time", Translation.getText("en", "1 raz"));
		assertEquals("I have helped Semos 2 times so far.", Translation.getText("en", "Pomogłem Semos 2 razy do tej pory."));
		assertEquals("I have helped Kuźnice 12 times so far.", Translation.getText("en", "Pomogłam Kuźnicom 12 razy do tej pory."));
		assertEquals("2 razy", Translation.getText("pl", "2 razy"));
	}
	@Test public void translatedWindowTitlesKeepTheirNonBreakingSpaces() {
		assertEquals("<html>two-headed&nbsp;black&nbsp;dragon</html>",
				Translation.getText("en", "<html>dwugłowy&nbsp;czarny&nbsp;smok</html>"));
		assertEquals("<html>dwugłowy&nbsp;czarny&nbsp;smok</html>",
				Translation.getText("pl", "<html>dwugłowy&nbsp;czarny&nbsp;smok</html>"));
	}
	@Test public void cottageSausageNameIsConsistentAcrossFormsAndDescriptions() {
		assertEquals("cottage sausage", Translation.getText("en", "kiełbasa wiejska"));
		assertEquals("2 cottage sausages", Translation.getText("en", "2 sztuki kiełbasy wiejskiej"));
		assertEquals("You see a cottage sausage.", Translation.getText("en", "Oto kiełbasa wiejska."));
	}
	@Test public void defaultsToPolishAndNormalizesOnlySupportedLanguages() {
		assertEquals("pl", Translation.normalize(null));
		assertEquals("pl", Translation.normalize("es"));
		assertEquals("en", Translation.normalize(" EN "));
		assertFalse(Translation.isSupported("../../en"));
		assertFalse(Translation.isSupported(""));
	}
	@Test public void usesExistingPolishAndNewEnglishDictionaries() {
		assertEquals("Rzadkość", Translation.getText(null, "Rarity"));
		assertEquals("Close", Translation.getText("en", "Zamknij"));
		assertEquals("strzała", Translation.getText("pl", "wooden arrow"));
	}
	@Test public void neverLosesMissingOrEmptyText() {
		assertEquals("Brak tłumaczenia", Translation.getText("en", "Brak tłumaczenia"));
		assertEquals("", Translation.getText("en", ""));
		assertNull(Translation.getText("en", null));
	}
	@Test public void templatesPreserveNamesNumbersAndTranslateOnlyExplicitItemArguments() {
		assertEquals("Hello, Zamknij!", Translation.getText("en", "Witaj, Zamknij!"));
		assertEquals("Money: 992550648", Translation.getText("en", "Pieniądze: 992550648"));
		assertEquals("I will pay you 4000 coins for leather cuirass. Will you sell it?",
				Translation.getText("en", "Dam ci 4000 monet za skórzany kirys. Sprzedajesz?"));
	}
	@Test public void htmlTranslationChangesDisplayTextButNeverAttributes() {
		assertEquals("<html><b title=\"Zamknij\">Close</b></html>",
				Translation.getText("en", "<html><b title=\"Zamknij\">Zamknij</b></html>"));
	}
	@Test public void escapedKeysAndJournalLinesRemainReadable() {
		assertEquals("a=b", Translation.unescape("a\\=b"));
		assertEquals("first\nsecond", Translation.unescape("first\\nsecond"));
		assertEquals(4, Translation.separator("a\\=b=value"));
		assertEquals("\t * Money: 15\r\n\t * Level: 2", Translation.getText("en", "\t * Pieniądze: 15\r\n\t * Poziom: 2"));
	}
	@Test public void polishTranslatedNpcLinksReturnTheirCanonicalKeyword() {
		assertEquals("pamięć", Translation.getKeywordLabel("pl", "memory"));
		assertEquals("memory", Translation.getOriginalKeyword("pl", "pamięć"));
		assertEquals("nieznane hasło", Translation.getOriginalKeyword("pl", "nieznane hasło"));
	}
	@Test public void explicitNpcAliasesHavePriorityOverWordsPairedFromDialogue() {
		assertEquals("sword", Translation.getKeywordLabel("en", "Miecz"));
		assertEquals("miecz", Translation.getOriginalKeyword("en", "Sword"));
		assertEquals("of Demons", Translation.getKeywordLabel("en", "Demonów"));
		assertEquals("magic", Translation.getKeywordLabel("en", "magiczne"));
		assertEquals("scrolls", Translation.getKeywordLabel("en", "zwoje"));
	}
	@Test public void sentenceContextPreservesCanonicalNpcTargetsDespiteUnrelatedCaptionCollisions() {
		String[][] cases = {
			{"Kontynuuj polowanie. Powiedz #załatwione, kiedy skończysz.", "done", "załatwione"},
			{"Najpierw zakończ obecne polowanie albo powiedz #rezygnuję. Przerwane zadanie nie daje nagrody.", "cancel", "rezygnuję"},
			{"Masz aktywne polowanie: wilk, 8 sztuk. Powiedz #załatwione lub #rezygnuję.", "cancel", "rezygnuję"},
			{"Słowo już padło. Teraz nie mnie masz przekonywać. Zakonnik czeka na murze w północnej części zamku od strony wschodniej, by przyjąć twoją #wartę. To on ma zobaczyć, czy wytrwasz przy obowiązku.", "watch", "wartę"},
			{"Mogę pokazać Tobie #mapę lub pokazać gdzie jest #bank, #biblioteka, #oberża, #świątynia, #kowal, #piekarnia, #publiczna #skrzynia lub gdzie jest stara #wioska.", "public chest", "publiczna skrzynia"}
		};
		for (String[] row : cases) {
			String rendered=Translation.getText("en",row[0]);
			assertEquals(row[0],row[2],Translation.getOriginalKeyword("en",row[1],rendered));
			assertEquals(row[2],Translation.getOriginalKeyword("en",row[1],"  "+rendered+"  "));
			assertEquals(row[2],Translation.getOriginalKeyword("en",row[1],"Journal\n"+rendered+"\n"));
		}
		assertEquals("Cancel", Translation.getOriginalKeyword("en", "Cancel", "A player said #Cancel."));
	}
}
