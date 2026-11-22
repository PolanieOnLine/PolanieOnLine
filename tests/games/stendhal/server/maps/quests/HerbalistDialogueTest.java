package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.city.HealerNPC;
import games.stendhal.server.maps.zakopane.hospital.GazdzinaJadzkaNPC;
import games.stendhal.server.util.ItemCollection;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

@RunWith(Parameterized.class)
public class HerbalistDialogueTest {
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> healers() {
		return Arrays.asList(new Object[][] {{"Carmen"}, {"Gaździna Jadźka"}});
	}

	private final String name;
	private Player player;
	private SpeakerNPC npc;
	private Engine engine;
	private String slot;
	private AbstractQuest quest;

	public HerbalistDialogueTest(final String name) {
		this.name = name;
	}

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		StendhalRPZone zone = new StendhalRPZone("herbalist_dialogue_test", 80, 80);
		if ("Carmen".equals(name)) {
			new HealerNPC().configureZone(zone, null);
			quest = new HerbsForCarmen() {
				@Override public boolean removeFromWorld() { return true; }
			};
		} else {
			new GazdzinaJadzkaNPC().configureZone(zone, null);
			quest = new HerbsForJadzka() {
				@Override public boolean removeFromWorld() { return true; }
			};
		}
		npc = SingletonRepository.getNPCList().get(name);
		engine = npc.getEngine();
		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
		assertTrue(SingletonRepository.getStendhalQuestSystem().isLoaded(quest));
		slot = quest.getSlotName();
		player = PlayerTestHelper.createPlayer("herbalist_visitor");
		player.setLevel(10);
		zone.add(player);
	}

	@After
	public void cleanup() {
		SingletonRepository.getStendhalQuestSystem().unloadQuest(quest);
		SingletonRepository.getNPCList().remove(name);
		PlayerTestHelper.removeAllPlayers();
	}

	private String say(final String text) {
		engine.step(player, text);
		return getReply(npc);
	}

	private void startActiveQuest() {
		player.setQuest(slot, HerbsForCarmen.NEEDED_ITEMS);
		assertNotNull(say("hi"));
	}

	@Test
	public void advertisedIngredientKeywordsAndDoneExplainHowToHandItemsIn() {
		startActiveQuest();
		for (String keyword : Arrays.asList("składniki", "skladniki", "zapasy", "ingredients", "done")) {
			engine.setCurrentState(ConversationStates.ATTENDING);
			String reply = say(keyword);
			assertNotNull(keyword + " must be understood", reply);
			assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
			assertTrue(reply, reply.contains("arandul"));
			assertTrue("The player needs an explicit instruction: " + reply, reply.contains("nazw"));
		}
		assertEquals(HerbsForCarmen.NEEDED_ITEMS, player.getQuest(slot));
	}

	@Test
	public void namesWorkImmediatelyAfterGreetingAndFinishExactlyOnce() {
		startActiveQuest();
		ItemCollection ingredients = new ItemCollection();
		ingredients.addFromQuestStateString(HerbsForCarmen.NEEDED_ITEMS);
		for (String item : ingredients.keySet()) {
			PlayerTestHelper.equipWithStackableItem(player, item, ingredients.get(item));
		}
		int xp = player.getXP();
		String reply = say("arandula");
		assertEquals(0, player.getNumberOfEquipped("arandula"));
		assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
		assertTrue(reply, reply.contains("Brakuje mi jeszcze"));
		assertFalse(reply, reply.contains("arandul"));
		for (String item : Arrays.asList("borowik", "jabłko", "pieczarka", "polano")) {
			reply = say(item);
		}
		assertEquals("done", player.getQuest(slot));
		assertEquals(xp + 500, player.getXP());
		assertTrue(reply, reply.contains("Dziękuję"));
		String reward = "Carmen".equals(name) ? "mały eliksir" : "antidotum";
		int quantity = "Carmen".equals(name) ? 5 : 7;
		assertEquals(quantity, player.getNumberOfEquipped(reward));
		for (String keyword : Arrays.asList("składniki", "done", "tak", "arandula")) {
			say(keyword);
			assertEquals("done", player.getQuest(slot));
			assertEquals(xp + 500, player.getXP());
			assertEquals(quantity, player.getNumberOfEquipped(reward));
			assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		}
	}

	@Test
	public void partialDeliveriesSurviveLeavingAndOnlyTakeTheNamedIngredient() {
		startActiveQuest();
		PlayerTestHelper.equipWithStackableItem(player, "arandula", 2);
		PlayerTestHelper.equipWithStackableItem(player, "polano", 4);
		say("arandula");
		assertEquals(0, player.getNumberOfEquipped("arandula"));
		assertEquals(4, player.getNumberOfEquipped("polano"));
		ItemCollection remaining = new ItemCollection();
		remaining.addFromQuestStateString(player.getQuest(slot));
		assertEquals(Integer.valueOf(3), remaining.get("arandula"));
		String progress = player.getQuest(slot);
		assertTrue(say("arandula").contains("Nie masz"));
		assertEquals(progress, player.getQuest(slot));
		say("bye");
		say("hi");
		assertTrue(say("składniki").contains("arandul"));
		say("polano");
		assertEquals(2, player.getNumberOfEquipped("polano"));
		progress = player.getQuest(slot);
		assertNotNull(say("polano"));
		assertEquals(progress, player.getQuest(slot));
		assertEquals(2, player.getNumberOfEquipped("polano"));
	}

	@Test
	public void ingredientNamesShownInTheListCanBeUsedForDelivery() {
		startActiveQuest();
		ItemCollection ingredients = new ItemCollection();
		ingredients.addFromQuestStateString(HerbsForCarmen.NEEDED_ITEMS);
		for (String item : ingredients.keySet()) {
			PlayerTestHelper.equipWithStackableItem(player, item, ingredients.get(item));
		}
		say("składniki");
		for (String word : Arrays.asList("arandule", "borowik", "jabłka", "pieczarka", "polana")) {
			assertNotNull(word, say(word));
		}
		assertEquals("done", player.getQuest(slot));
	}

	@Test
	public void yesCannotEnterTheCollectionBeforeAcceptingTheQuest() {
		say("hi");
		say("tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertFalse(player.hasQuest(slot));
		if ("Gaździna Jadźka".equals(name)) {
			say("składniki");
			assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
			assertFalse("Retired ingredient quest must not be offered to new players", player.hasQuest(slot));
			return;
		}
		assertNotNull(say("składniki"));
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		String reply = say("tak");
		assertEquals(HerbsForCarmen.NEEDED_ITEMS, player.getQuest(slot));
		assertTrue(reply, reply.contains("nazw"));
	}
}
