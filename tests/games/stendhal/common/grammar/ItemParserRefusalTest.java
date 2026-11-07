package games.stendhal.common.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import games.stendhal.common.Translation;
import games.stendhal.common.parser.ConversationParser;

public class ItemParserRefusalTest {

	@Test
	public void unavailableWaresAreRefusedWithoutGuessingTheirDeclension() {
		ItemParser parser = new ItemParser(new HashSet<String>(Arrays.asList("polano", "ser")));
		for (String command : Arrays.asList("sell", "buy")) {
			for (String name : Arrays.asList("węgiel", "zbroja z mithrilu", "spodnie kolcze",
					"wengeil", "leather cuirass")) {
				ItemParserResult result = parser.parse(ConversationParser.parse(command + " " + name));
				assertFalse(command + " " + name, result.wasFound());
				String npcAction = command.equals("sell") ? "buy" : "sell";
				String expected = command.equals("sell")
						? "Nie skupuję takich przedmiotów." : "Nie sprzedaję takich przedmiotów.";
				assertEquals(command + " " + name, expected,
						parser.getErrormessage(result, Collections.singletonList(command), npcAction));
			}
		}
	}

	@Test
	public void tradeRefusalsRemainTranslated() {
		assertEquals("I do not buy items like that.",
				Translation.getText("en", "Nie skupuję takich przedmiotów."));
		assertEquals("I do not sell items like that.",
				Translation.getText("en", "Nie sprzedaję takich przedmiotów."));
		assertEquals("No compro ese tipo de objetos.",
				Translation.getText("es", "Nie skupuję takich przedmiotów."));
		assertEquals("No vendo ese tipo de objetos.",
				Translation.getText("es", "Nie sprzedaję takich przedmiotów."));
	}
}
