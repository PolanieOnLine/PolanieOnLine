package games.stendhal.server.maps.ados.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.bakery.ChefNPC;
import games.stendhal.server.maps.semos.bakery.ShopAssistantNPC;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class HolidayingWomanNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "testzone";
	private static final String TASTED_EVERYTHING_REPLY = "Sądzę, że spróbowałam już wszystkiego";
	private static final String BREAD_DESCRIPTION = "Erna pracuje nad chleby, który potrzebuje 2 mąki.";
	private static final String SANDWICH_DESCRIPTION = "Leander pracuje nad kanapki, który potrzebuje 2 sery, chleb, oraz szynka.";

	private Player player;
	private SpeakerNPC aliceNpc;
	private Engine aliceEngine;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME, new HolidayingWomanNPC(), new ChefNPC(), new ShopAssistantNPC());
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		player = createPlayer("player");
		aliceNpc = SingletonRepository.getNPCList().get("Alice Farmer");
		aliceEngine = aliceNpc.getEngine();

		final Map<String, Integer> chleb = new TreeMap<String, Integer>();
		chleb.put("mąka", 2);
		final Map<String, Integer> kanapka = new TreeMap<String, Integer>();
		kanapka.put("ser", 2);
		kanapka.put("chleb", 1);
		kanapka.put("szynka", 1);

		SingletonRepository.getProducerRegister().configureNPC(
				"Leander", new ProducerBehaviour("leander_test", Arrays.asList("make"), "kanapka", kanapka, 0), "");
		SingletonRepository.getProducerRegister().configureNPC(
				"Erna", new ProducerBehaviour("erna_test", Arrays.asList("make"), "chleb", chleb, 0), "");
	}

	public HolidayingWomanNPCTest() {
		super(ZONE_NAME, "Alice Farmer", "Leander", "Erna");
	}

	@Test
	public void testDialogue() {
		startConversation();

		askForFoodList();
		checkReply("chleb", BREAD_DESCRIPTION);
		checkReply("kanapka", SANDWICH_DESCRIPTION);
		checkReply("kanapka.", SANDWICH_DESCRIPTION);

		endConversation();
	}

	private void startConversation() {
		aliceEngine.step(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertTrue(aliceNpc.isTalking());
		assertEquals("Cześć.", getReply(aliceNpc));
	}

	private void askForFoodList() {
		aliceEngine.step(player, "food");
		assertTrue(aliceNpc.isTalking());
		String listOfFoodReply = getReply(aliceNpc);
		assertTrue(listOfFoodReply.startsWith(TASTED_EVERYTHING_REPLY));
		assertTrue(listOfFoodReply.contains("#chleb"));
		assertTrue(listOfFoodReply.contains("#kanapka"));
	}

	private void endConversation() {
		aliceEngine.step(player, ConversationPhrases.GOODBYE_MESSAGES.get(0));
		assertFalse(aliceNpc.isTalking());
		assertEquals("Do widzenia.", getReply(aliceNpc));
	}

	private void checkReply(String question, String expectedReply) {
		aliceEngine.step(player, question);
		assertTrue(aliceNpc.isTalking());
		assertEquals(expectedReply, getReply(aliceNpc));
	}
}
