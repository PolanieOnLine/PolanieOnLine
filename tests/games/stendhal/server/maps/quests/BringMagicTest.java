/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.zakopane.tower.WizardNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class BringMagicTest {
	private static final String QUEST_SLOT = "bring_magic";

	private Player player;
	private SpeakerNPC npc;
	private Engine engine;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		NPCList.get().clear();
		final StendhalRPZone zone = new StendhalRPZone("bring_magic_test");
		new WizardNPC().configureZone(zone, null);
		npc = NPCList.get().get("Czarnoksiężnik");
		new BuiltQuest(new BringMagic().story()).addToWorld();
		engine = npc.getEngine();
		player = PlayerTestHelper.createPlayer("bring_magic_player");
	}

	@After
	public void tearDown() {
		NPCList.get().clear();
		PlayerTestHelper.removeAllPlayers();
	}

	@Test
	public void completesLegacyHelmetStateWithoutChargingMagicOrRepeatingFirstReward() {
		player.setQuest(QUEST_SLOT, "helmet");
		equip("hełm kolczy", 1);
		final int xpBefore = player.getXP();
		final double karmaBefore = player.getKarma();

		assertTrue(engine.step(player, "hi"));
		assertEquals("Masz hełm kolczy. Oddasz mi go na chwilę?", getReply(npc));
		assertTrue(engine.step(player, "yes"));

		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
		assertEquals("1", player.getQuest(QUEST_SLOT, 2));
		assertEquals(0, player.getNumberOfEquipped("hełm kolczy"));
		assertEquals(1, player.getNumberOfEquipped("magiczny hełm kolczy"));
		assertEquals(xpBefore + 5000, player.getXP());
		assertEquals(karmaBefore + 20.0, player.getKarma(), 0.01);
		assertEquals(player.getName(), player.getFirstEquipped("magiczny hełm kolczy").getBoundTo());
	}

	@Test
	public void legacyHelmetStateSurvivesReturnWithoutHelmet() {
		player.setQuest(QUEST_SLOT, "helmet");

		assertTrue(engine.step(player, "hi"));
		assertEquals("Nie masz przy sobie hełmu kolczego. Przynieś go, a dokończę ostatnie doświadczenie.", getReply(npc));
		assertEquals("helmet", player.getQuest(QUEST_SLOT));
	}

	@Test
	public void normalFlowKeepsBothHandInsAndAllOriginalRewards() {
		player.setQuest("kill_mountain_elves", "done");
		final int xpBefore = player.getXP();
		final double karmaBefore = player.getKarma();

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		assertEquals(ConversationStates.QUEST_3_OFFERED, engine.getCurrentState());
		assertTrue(engine.step(player, "yes"));
		assertEquals("start", player.getQuest(QUEST_SLOT, 0));
		assertEquals(karmaBefore + 10.0, player.getKarma(), 0.01);

		equip("magia ziemi", 100);
		equip("magia płomieni", 100);
		equip("magia deszczu", 100);
		equip("magia mroku", 100);
		equip("magia światła", 100);

		assertTrue(engine.step(player, "task"));
		assertEquals("forging", player.getQuest(QUEST_SLOT, 0));
		assertNotNull(player.getQuest(QUEST_SLOT, 1));
		assertEquals(xpBefore + 50000, player.getXP());
		assertEquals(karmaBefore + 20.0, player.getKarma(), 0.01);
		assertEquals(0, player.getNumberOfEquipped("magia ziemi"));
		assertEquals(0, player.getNumberOfEquipped("magia płomieni"));
		assertEquals(0, player.getNumberOfEquipped("magia deszczu"));
		assertEquals(0, player.getNumberOfEquipped("magia mroku"));
		assertEquals(0, player.getNumberOfEquipped("magia światła"));

		assertTrue(engine.step(player, "bye"));
		assertTrue(engine.step(player, "hi"));
		assertEquals("Nie masz przy sobie hełmu kolczego. Przynieś go, a dokończę ostatnie doświadczenie.", getReply(npc));
		assertTrue(engine.step(player, "bye"));

		equip("hełm kolczy", 1);
		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "yes"));
		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
		assertEquals(xpBefore + 55000, player.getXP());
		assertEquals(karmaBefore + 40.0, player.getKarma(), 0.01);
	}

	private void equip(String name, int quantity) {
		final Item item = SingletonRepository.getEntityManager().getItem(name);
		assertNotNull(name, item);
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(quantity);
		}
		player.equipToInventoryOnly(item);
	}
}
