/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import org.junit.*;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.Level;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.zakopane.hospital.GazdzinaJadzkaNPC;
import games.stendhal.server.maps.zakopane.hospital.JadzkaAntidoteProduction;
import games.stendhal.server.util.ItemCollection;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class JadzkaHerbalOrdersTest {
	private Player player;
	private SpeakerNPC npc;
	private Engine engine;
	private HerbalOrdersForJadzka quest;
	private StendhalRPZone zone;
	private long time = 2000000000000L;

	@BeforeClass public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		SingletonRepository.getNPCList().remove("Gaździna Jadźka");
		zone = new StendhalRPZone("int_zakopane_hospital", 64, 64);
		MockStendlRPWorld.get().addRPZone(zone);
		new GazdzinaJadzkaNPC().configureZone(zone, null);
		npc = SingletonRepository.getNPCList().get("Gaździna Jadźka"); engine = npc.getEngine();
		new HerbsForJadzka().addToWorld();
		quest = new HerbalOrdersForJadzka() { @Override protected long now() { return time; } };
		quest.addToWorld();
		player = PlayerTestHelper.createPlayer("herbal_order_visitor"); player.setXP(Level.getXP(10)); player.setLevel(10);
		zone.add(player); player.setPosition(19,10);
		say("hi");
	}
	@After public void cleanup() {
		SingletonRepository.getNPCList().remove("Gaździna Jadźka");
		MockStendlRPWorld.get().removeZone(zone);
	}
	private String say(String words) { engine.stepTest(player, words); return getReply(npc); }
	private void accept() {
		say("zamówienie"); assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		say("tak"); assertTrue(player.hasQuest(HerbalOrdersForJadzka.SLOT));
	}
	private void equipOrder() {
		ItemCollection items = new ItemCollection(); items.addFromQuestStateString(player.getQuest(HerbalOrdersForJadzka.SLOT));
		for (String name : items.keySet()) PlayerTestHelper.equipWithStackableItem(player,name,items.get(name));
	}
	@Test public void freshPlayersReceiveHerbsInsteadOfTheDuplicateFruitQuest() {
		say("zadanie"); say("nie");
		assertFalse(player.hasQuest(HerbsForJadzka.QUEST_SLOT));
		assertFalse(player.hasQuest(HerbalOrdersForJadzka.SLOT));
		accept();
		String order=player.getQuest(HerbalOrdersForJadzka.SLOT);
		assertTrue(order,order.contains("mięta=")); assertTrue(order,order.contains("babka lekarska="));
		assertFalse(order,order.contains("jabłko")); assertFalse(order,order.contains("borowik"));
		assertFalse(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK));
	}
	@Test public void partialDeliveriesPersistAndFinalTurnInRewardsAndUnlocksOnce() {
		accept(); PlayerTestHelper.equipWithStackableItem(player,"mięta",1);
		String before=player.getQuest(HerbalOrdersForJadzka.SLOT);
		say("mięta");
		assertEquals(0,player.getNumberOfEquipped("mięta"));
		assertNotEquals(before,player.getQuest(HerbalOrdersForJadzka.SLOT));
		String progress=player.getQuest(HerbalOrdersForJadzka.SLOT); say("bye"); say("hi");
		assertEquals(progress,player.getQuest(HerbalOrdersForJadzka.SLOT));
		assertEquals(QuestMarker.IN_PROGRESS,quest.getNPCQuestMarker(player,npc));
		equipOrder(); assertEquals(QuestMarker.READY,quest.getNPCQuestMarker(player,npc));
		int xp=player.getXP(); say("gotowe");
		assertTrue(quest.isCompleted(player)); assertEquals(xp+500,player.getXP());
		assertEquals(300,player.getNumberOfEquipped("money"));
		assertTrue(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK));
		say("gotowe"); say("tak"); say("mięta");
		assertEquals(xp+500,player.getXP()); assertEquals(300,player.getNumberOfEquipped("money"));
	}
	@Test public void dailyCooldownDoesNotExpireAcceptedOrdersOrRelockProduction() {
		accept(); equipOrder(); say("gotowe");
		assertFalse(quest.isRepeatable(player)); assertEquals(QuestMarker.NONE,quest.getNPCQuestMarker(player,npc));
		time+=24L*60*60*1000-1; say("zamówienie");
		assertTrue(quest.isCompleted(player)); time++;
		assertTrue(quest.isRepeatable(player)); assertEquals(QuestMarker.AVAILABLE,quest.getNPCQuestMarker(player,npc));
		accept(); String order=player.getQuest(HerbalOrdersForJadzka.SLOT);
		time+=7L*24*60*60*1000; say("bye"); say("hi"); say("zamówienie");
		assertEquals(order,player.getQuest(HerbalOrdersForJadzka.SLOT));
		assertTrue(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK));
	}
	@Test public void oldActiveListsCanBeFinishedWithoutResettingPartialProgress() {
		player.setQuest(HerbsForJadzka.QUEST_SLOT,"arandula=2");
		say("zamówieniu"); say("tak");
		assertFalse(player.hasQuest(HerbalOrdersForJadzka.SLOT));
		PlayerTestHelper.equipWithStackableItem(player,"arandula",2); say("arandula");
		assertTrue(player.isQuestCompleted(HerbsForJadzka.QUEST_SLOT));
		assertEquals(7,player.getNumberOfEquipped("antidotum"));
		assertFalse(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK));
		accept();
	}
	@Test public void oldCompletionDoesNotSkipTheNewHerbalOrder() {
		player.setQuest(HerbsForJadzka.QUEST_SLOT,"done");
		say("uwarz 1 antidotum"); say("tak");
		assertFalse(player.hasQuest(JadzkaAntidoteProduction.SLOT));
		accept(); equipOrder(); say("gotowe");
		assertTrue(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK));
	}
	@Test public void journalCompassAndTwoPlayersDoNotMutateOrShareOrders() {
		assertEquals(300,quest.getQuestInfo(player).getRewards().coins);
		accept(); String state=player.getQuest(HerbalOrdersForJadzka.SLOT);
		assertTrue(quest.getHistory(player).toString().contains("mięt"));
		assertNull(quest.getCompassTarget(player));
		assertEquals(state,player.getQuest(HerbalOrdersForJadzka.SLOT));
		Player other=PlayerTestHelper.createPlayer("second_herbalist"); other.setLevel(10);
		engine.setCurrentState(ConversationStates.IDLE); engine.stepTest(other,"hi"); engine.stepTest(other,"zamówienie"); engine.stepTest(other,"tak");
		PlayerTestHelper.equipWithStackableItem(player,"mięta",2); engine.setCurrentState(ConversationStates.ATTENDING); say("mięta");
		assertEquals(state,other.getQuest(HerbalOrdersForJadzka.SLOT));
		assertNotEquals(state,player.getQuest(HerbalOrdersForJadzka.SLOT));
	}
	@Test public void productionIsLockedThenUsesExactBatchIngredientsAndSurvivesReturn() {
		say("uwarz 2 antidotum"); say("tak"); assertFalse(player.hasQuest(JadzkaAntidoteProduction.SLOT));
		accept(); equipOrder(); say("gotowe");
		PlayerTestHelper.equipWithStackableItem(player,"mięta",2);
		PlayerTestHelper.equipWithStackableItem(player,"babka lekarska",2);
		PlayerTestHelper.equipWithStackableItem(player,"buteleczka wody",2);
		say("uwarz 2 antidotum"); assertEquals(ConversationStates.PRODUCTION_OFFERED,engine.getCurrentState());
		say("tak"); assertEquals(0,player.getNumberOfEquipped("mięta")); assertEquals(0,player.getNumberOfEquipped("babka lekarska"));
		String order=player.getQuest(JadzkaAntidoteProduction.SLOT); assertTrue(order,order.startsWith("2;antidotum;"));
		say("bye"); say("hi"); assertEquals(order,player.getQuest(JadzkaAntidoteProduction.SLOT));
		player.setQuest(JadzkaAntidoteProduction.SLOT,"2;antidotum;"+(System.currentTimeMillis()-61000));
		say("bye"); say("hi"); assertEquals(2,player.getNumberOfEquipped("antidotum"));
		assertTrue(player.isQuestCompleted(JadzkaAntidoteProduction.SLOT)); say("bye"); say("hi"); assertEquals(2,player.getNumberOfEquipped("antidotum"));
	}
	@Test public void missingProductionIngredientConsumesNothingAndUiRecipeIsLocked() {
		games.stendhal.server.entity.npc.behaviour.ProductionRecipe recipe = games.stendhal.server.entity.npc.behaviour.ProductionRecipe.forNPC(npc).get(0);
		assertFalse(recipe.unavailableReason(player).isEmpty());
		player.setQuest(JadzkaAntidoteProduction.UNLOCK,"done");
		assertEquals("",recipe.unavailableReason(player)); assertEquals(60,recipe.getProductionTime(2));
		PlayerTestHelper.equipWithStackableItem(player,"mięta",2);
		PlayerTestHelper.equipWithStackableItem(player,"babka lekarska",2);
		say("uwarz 2 antidotum"); say("tak");
		assertFalse(player.hasQuest(JadzkaAntidoteProduction.SLOT));
		assertEquals(2,player.getNumberOfEquipped("mięta")); assertEquals(2,player.getNumberOfEquipped("babka lekarska"));
	}
	@Test public void finishedProductionWaitsForInventorySpace() {
		player.setQuest(JadzkaAntidoteProduction.UNLOCK,"done");
		String order="2;antidotum;"+(System.currentTimeMillis()-61000);
		player.setQuest(JadzkaAntidoteProduction.SLOT,order);
		for (String slot : new String[] {"bag","lhand","rhand"}) {
			while (!player.getSlot(slot).isFull()) player.getSlot(slot).add(SingletonRepository.getEntityManager().getItem("ciupaga"));
		}
		say("bye"); String message=say("hi");
		assertEquals(order,player.getQuest(JadzkaAntidoteProduction.SLOT));
		assertEquals(0,player.getNumberOfEquipped("antidotum"));
		assertTrue(message,message.contains("miejsce"));
		player.drop("ciupaga",1); say("bye"); say("hi");
		assertTrue(player.isQuestCompleted(JadzkaAntidoteProduction.SLOT));
		assertEquals(2,player.getNumberOfEquipped("antidotum"));
	}
	@Test public void newDialogueAndProgressTranslateWithoutChangingTheOrder() {
		assertEquals("Herbal Order",games.stendhal.common.Translation.getText("en",quest.getName()));
		assertEquals("Encargo de hierbas",games.stendhal.common.Translation.getText("es",quest.getName()));
		accept();
		String state=player.getQuest(HerbalOrdersForJadzka.SLOT);
		for (String line : quest.getHistory(player)) {
			assertNotEquals(line,games.stendhal.common.Translation.getText("en",line));
			assertNotEquals(line,games.stendhal.common.Translation.getText("es",line));
		}
		assertEquals(state,player.getQuest(HerbalOrdersForJadzka.SLOT));
	}

}
