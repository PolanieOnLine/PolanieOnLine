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
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.zakopane.cave.BartlomiejNPC;
import games.stendhal.server.util.TimeUtil;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ZlotyRogTest {
	private static final String QUEST_SLOT = "zloty_rog";

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
		final StendhalRPZone zone = new StendhalRPZone("zloty_rog_test");
		new BartlomiejNPC().configureZone(zone, null);
		npc = NPCList.get().get("Bartłomiej");
		new BuiltQuest(new ZlotyRog().story()).addToWorld();
		engine = npc.getEngine();
		player = PlayerTestHelper.createPlayer("zloty_rog_player");
	}

	@After
	public void tearDown() {
		NPCList.get().clear();
		PlayerTestHelper.removeAllPlayers();
	}

	@Test
	public void legacyMakeStateKeepsRemainingTimeAfterReturn() {
		player.setQuest(QUEST_SLOT, "make;" + System.currentTimeMillis());

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		final String reply = getReply(npc);
		assertTrue(reply, reply.contains("Mój brat wciąż pracuje nad twoim złotym rogiem"));
		assertEquals("make", player.getQuest(QUEST_SLOT, 0));
	}

	@Test
	public void completedLegacyMakeStateAwardsHornAndMigratesToDoneFormat() {
		player.setQuest(QUEST_SLOT, "make;"
				+ (System.currentTimeMillis() - 61 * TimeUtil.MILLISECONDS_IN_MINUTE));
		final int xpBefore = player.getXP();
		final double karmaBefore = player.getKarma();

		assertTrue(engine.step(player, "hi"));
		assertEquals("Mój brat skończył pracę. Oto twój złoty róg.", getReply(npc));

		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
		assertNotNull(player.getQuest(QUEST_SLOT, 1));
		assertEquals("1", player.getQuest(QUEST_SLOT, 2));
		assertEquals(xpBefore + 20000, player.getXP());
		assertEquals(karmaBefore + 100.0, player.getKarma(), 0.01);
		assertEquals(1, player.getNumberOfEquipped("złoty róg"));
		assertEquals(player.getName(), player.getFirstEquipped("złoty róg").getBoundTo());
	}

	@Test
	public void malformedLegacyTimestampCompletesSafely() {
		player.setQuest(QUEST_SLOT, "make;broken");

		assertTrue(engine.step(player, "hi"));
		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
		assertEquals(1, player.getNumberOfEquipped("złoty róg"));
	}

	@Test
	public void legacyDoneStateCanStartAnotherOrderAfterFourDays() {
		satisfyRequirementsExceptKill();
		player.setSoloKill("archanioł");
		player.setQuest(QUEST_SLOT, "done;"
				+ (System.currentTimeMillis() - (4 * 24 * 60 + 1) * TimeUtil.MILLISECONDS_IN_MINUTE));
		final double karmaBefore = player.getKarma();

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		assertEquals("Mój brat może już wykonać kolejny złoty róg. Chcesz zebrać dla niego nowe pióra?", getReply(npc));
		assertTrue(engine.step(player, "yes"));

		assertEquals("start", player.getQuest(QUEST_SLOT, 0));
		assertEquals(karmaBefore + 10.0, player.getKarma(), 0.01);
	}

	@Test
	public void legacyDoneStateStillChecksSoloKillBeforeAnotherOrder() {
		satisfyRequirementsExceptKill();
		player.setSharedKill("archanioł");
		player.setQuest(QUEST_SLOT, "done;"
				+ (System.currentTimeMillis() - (4 * 24 * 60 + 1) * TimeUtil.MILLISECONDS_IN_MINUTE));

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		assertEquals("Najpierw ukończ sprawę kolekcjonera broni, osiągnij poziom 200, zachowaj co najmniej 500 karmy i samodzielnie pokonaj archanioła.", getReply(npc));
		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
	}

	@Test
	public void sharedArchangelKillDoesNotSatisfyOriginalSoloRequirement() {
		satisfyRequirementsExceptKill();
		player.setSharedKill("archanioł");

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		assertEquals("Najpierw ukończ sprawę kolekcjonera broni, osiągnij poziom 200, zachowaj co najmniej 500 karmy i samodzielnie pokonaj archanioła.", getReply(npc));

		player.setSoloKill("archanioł");
		assertTrue(engine.step(player, "task"));
		assertEquals("Mój brat zna tajemnicę wyrabiania złotych rogów. Potrzebuje jednak rzadkich piór, zanim rozpocznie pracę. Chcesz zamówić jeden z nich?", getReply(npc));
	}

	@Test
	public void normalFlowConsumesFeathersAndFinishesAfterSavedTimestamp() {
		satisfyRequirementsExceptKill();
		player.setSoloKill("archanioł");

		assertTrue(engine.step(player, "hi"));
		assertTrue(engine.step(player, "task"));
		assertTrue(engine.step(player, "yes"));
		assertEquals("start", player.getQuest(QUEST_SLOT, 0));

		equip("piórko", 100);
		equip("pióro anioła", 20);
		equip("pióro archanioła", 10);
		equip("pióro mrocznego anioła", 8);
		equip("pióro upadłego anioła", 20);
		equip("pióro archanioła ciemności", 7);
		equip("pióro serafina", 2);

		assertTrue(engine.step(player, "task"));
		assertEquals("forging", player.getQuest(QUEST_SLOT, 0));
		assertEquals(0, player.getNumberOfEquipped("piórko"));
		assertEquals(0, player.getNumberOfEquipped("pióro anioła"));
		assertTrue(engine.step(player, "bye"));

		player.setQuest(QUEST_SLOT, 1,
				Long.toString(System.currentTimeMillis() - 61 * TimeUtil.MILLISECONDS_IN_MINUTE));
		assertTrue(engine.step(player, "hi"));
		assertEquals("done", player.getQuest(QUEST_SLOT, 0));
		assertEquals(1, player.getNumberOfEquipped("złoty róg"));
	}

	private void satisfyRequirementsExceptKill() {
		player.setQuest("weapons_collector", "done");
		player.setLevel(200);
		player.addKarma(500.0);
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
