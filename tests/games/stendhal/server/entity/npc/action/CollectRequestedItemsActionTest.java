package games.stendhal.server.entity.npc.action;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.parser.ConversationParser;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class CollectRequestedItemsActionTest {
	private Player player;
	private SpeakerNPC npc;
	private int completions;

	@BeforeClass public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		player = PlayerTestHelper.createPlayer("ingredient_collector_test");
		npc = SpeakerNPCTestHelper.createSpeakerNPC("Ingredient collector");
		new StendhalRPZone("ingredient_collector_zone", 30, 30).add(npc);
	}
	@After public void cleanup() {
		SpeakerNPCTestHelper.removeSpeakerNPC(npc);
		PlayerTestHelper.removeAllPlayers();
	}

	private void collect(final int position, final String message) {
		new CollectRequestedItemsAction("arandula", "collection_test", position, message, "Already delivered.",
				(p, sentence, raiser) -> completions++, ConversationStates.ATTENDING)
				.fire(player, ConversationParser.parse("arandula"), new EventRaiser(npc));
	}

	@Test public void oldQuestMessagesStayUnchanged() {
		player.setQuest("collection_test", "arandula=5;polano=2");
		PlayerTestHelper.equipWithStackableItem(player, "arandula", 2);
		collect(0, "Dobrze, a masz coś jeszcze?");
		assertEquals("Dobrze, a masz coś jeszcze?", getReply(npc));
		assertEquals(0, completions);
	}

	@Test public void remainingItemsRespectAnIndexedQuestAndPreserveItsEarlierFields() {
		player.setQuest("collection_test", "start;owner;stamp;arandula=5;polano=2");
		PlayerTestHelper.equipWithStackableItem(player, "arandula", 2);
		collect(3, "Brakuje mi jeszcze [items].");
		assertEquals("start;owner;stamp;arandula=3;polano=2", player.getQuest("collection_test"));
		String reply = getReply(npc);
		assertTrue(reply, reply.contains("3 #arandul"));
		assertTrue(reply, reply.contains("2 #polan"));
		assertFalse(reply, reply.contains("[items]"));
		assertEquals(0, completions);
	}
}
