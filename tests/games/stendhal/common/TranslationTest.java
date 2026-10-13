package games.stendhal.common;

import static org.junit.Assert.*;
import org.junit.Test;

public class TranslationTest {
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
}
