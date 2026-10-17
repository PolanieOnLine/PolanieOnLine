/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;
import static org.junit.Assert.*;
import org.junit.Test;

public class AdosTranslationTest {
	@Test public void translatedHintsFollowCurrentGameRecipesNotOldUpstreamDialogue() {
		String polish = "Jeśli potrzebujesz #olejku, przynieś mi dwa dorsze na każdą buteleczkę. Powiedz na przykład #'zrób olejek' albo #'przygotuj olejek'. Gotowe zlecenie odbierzesz słowem #przypomnij.";
		String english = Translation.getText("en", polish);
		assertTrue(english.contains("two cod"));
		assertTrue(english.contains("#'make oil'"));
		assertTrue(english.contains("#remind"));
		assertFalse(english.contains("signposts"));
		assertEquals(polish, Translation.getText("pl", polish));
	}
	@Test public void directionsAndLocationsReflectPolanieOnLineChanges() {
		assertTrue(Translation.getText("en", "Olbrzymy mieszkają w jaskiniach na wschód stąd. Powodzenia w zabijaniu tych bestii ...").contains("east"));
		assertTrue(Translation.getText("en", "W dzisiejszych czasach samorodki można znaleźć tylko w górach Ados oraz kopalni w Zakopanem. Nie mam pojęcia czy ten obszar jest ucywilizowany...").contains("mine in Zakopane"));
		assertEquals("Tatra scrolls take you to Zakopane instantly. A clever way out of a dangerous situation!", Translation.getText("en", "Tatrzańskie zwoje zabierają natychmiast do Zakopanego. Pomysłowe wyjście z niebezpiecznej sytuacji!"));
	}
	@Test public void dialogueTemplatesPreserveNamesPricesAndCanonicalDescriptions() {
		assertEquals("You see Zamknij, a dedicated zoologist.", Translation.getText("en", "Oto Zamknij, oddany zoolog"));
		assertEquals("In order to board the ferry, you have to pay 5000 gold. Do you want to pay?", Translation.getText("en", "Aby wejść na prom musisz zapłacić 5000 złota. Czy chcesz zapłacić?"));
		assertEquals("make oil", Translation.getText("en", "zrób olejek"));
	}
}
