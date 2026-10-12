package games.stendhal.common.grammar;

import static org.junit.Assert.*;
import org.junit.Test;

public class PolishItemGrammarTest {
	@Test public void mineralsUsePiecesWithTheGenitiveSingular() {
		assertEquals("ametyst", Grammar.itemQuantityAccusative(1, "ametyst"));
		for (int n : new int[] {0, 2, 4, 5, 8, 11, 12, 13, 14, 21, 22, 24, 25, 112, 114, 122}) {
			boolean few = (n % 10 >= 2 && n % 10 <= 4) && (n % 100 < 12 || n % 100 > 14);
			assertEquals(n + (few ? " sztuki ametystu" : " sztuk ametystu"), Grammar.itemQuantityAccusative(n, "ametyst"));
		}
		assertEquals("8 sztuk rubinu", Grammar.itemQuantityAccusative(8, "rubin"));
	}
	@Test public void pairedEquipmentUsesPairsAndDeclinesEveryAdjective() {
		assertEquals("parę spodni kolczych", Grammar.itemQuantityAccusative(1, "spodnie kolcze"));
		assertEquals("2 pary skórzanych wzmocnionych rękawic", Grammar.itemQuantityAccusative(2, "skórzane wzmocnione rękawice"));
		assertEquals("12 par magicznych butów płytowych", Grammar.itemQuantityAccusative(12, "magiczne buty płytowe"));
		assertEquals("22 pary spodni z mithrilu", Grammar.itemQuantityAccusative(22, "spodnie z mithrilu"));
		assertEquals("parę zaczarowanych nożyczek", Grammar.itemQuantityAccusative(1, "zaczarowane nożyczki"));
		assertEquals("parę portek bukowych", Grammar.itemQuantityAccusative(1, "portki bukowe"));
	}
	@Test public void equipmentUsesAccusativeAndTheRightPluralCases() {
		assertEquals("polską tarczę ciężką", Grammar.itemQuantityAccusative(1, "polska tarcza ciężka"));
		assertEquals("złotą ciupagę z trzema wąsami", Grammar.itemQuantityAccusative(1, "złota ciupaga z trzema wąsami"));
		assertEquals("2 polskie tarcze ciężkie", Grammar.itemQuantityAccusative(2, "polska tarcza ciężka"));
		assertEquals("5 polskich tarcz ciężkich", Grammar.itemQuantityAccusative(5, "polska tarcza ciężka"));
		assertEquals("12 krótkich mieczy", Grammar.itemQuantityAccusative(12, "krótki miecz"));
		assertEquals("2 lodowe zwoje", Grammar.itemQuantityAccusative(2, "lodowy zwój"));
	}
	@Test public void prepositionsOwnersAndMaterialsAreNotDeclinedAgain() {
		assertEquals("5 sztabek złota", Grammar.itemQuantityAccusative(5, "sztabka złota"));
		assertEquals("złotą klingę orków", Grammar.itemQuantityAccusative(1, "złota klinga orków"));
		assertEquals("8 sztuk glifu daru Mokoszy", Grammar.itemQuantityAccusative(8, "glif daru Mokoszy"));
		assertEquals("2 pary butów z zielonego potwora", Grammar.itemQuantityAccusative(2, "buty z zielonego potwora"));
	}
	@Test public void suppliesCanUsePortionsAndSetsInsteadOfPairs() {
		assertEquals("porcję lodów", Grammar.itemQuantityAccusative(1, "lody"));
		assertEquals("3 porcje gotowanych ziemniaków", Grammar.itemQuantityAccusative(3, "gotowane ziemniaki"));
		assertEquals("zestaw kart do gry", Grammar.itemQuantityAccusative(1, "karty do gry"));
		assertEquals("polano", Grammar.itemQuantityAccusative(1, "polano"));
		assertEquals("22 polana", Grammar.itemQuantityAccusative(22, "polano"));
		assertEquals("8 polan", Grammar.itemQuantityAccusative(8, "polano"));
		assertEquals("3 sztuki przędzy jedwabnej", Grammar.itemQuantityAccusative(3, "przędza jedwabna"));
		assertEquals("8 porcji zupy grzybowej", Grammar.itemQuantityAccusative(8, "zupa grzybowa"));
	}
	@Test public void unknownNamesAndUpgradeSuffixesArePreserved() {
		assertEquals("TestowyArtefakt", Grammar.itemQuantityAccusative(1, "TestowyArtefakt"));
		assertEquals("parę spodni kolczych +3", Grammar.itemQuantityAccusative(1, "spodnie kolcze +3"));
		assertEquals("przedmiot", Grammar.itemQuantityAccusative(1, null));
	}
}
