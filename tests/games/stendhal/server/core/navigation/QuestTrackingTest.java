package games.stendhal.server.core.navigation;

import static org.junit.Assert.*;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.npc.quest.DeliverItemQuestBuilder;
import games.stendhal.server.maps.quests.CoalForHaunchy;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.actions.query.ProgressStatusQueryAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import java.util.Arrays;
import java.util.List;
import org.junit.*;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class QuestTrackingTest {
	private Player player;
	private Item compass;
	private Fixture first, second, main;

	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		player = PlayerTestHelper.createPlayer("tracking-owner");
		new StendhalRPZone("tracking_zone", 30, 30).add(player);
		player.setQuest("wanderers_compass", "done");
		compass = SingletonRepository.getEntityManager().getItem("kompas wędrowca");
		compass.setBoundTo(player.getName());
		assertTrue(player.equipToInventoryOnly(compass));
		first = load("tracking_first", false);
		second = load("tracking_second", false);
		main = load("tracking_main", true);
	}
	private Fixture load(String slot, boolean isMain) {
		Fixture q = new Fixture(slot, isMain);
		SingletonRepository.getStendhalQuestSystem().loadQuest(q);
		player.setQuest(slot, "start");
		return q;
	}
	@After public void cleanup() {
		for (Fixture q : Arrays.asList(first, second, main))
			SingletonRepository.getStendhalQuestSystem().unloadQuest(q);
	}
	@Test public void onlyOneActiveSideQuestCanBeSelected() {
		assertTrue(QuestTracking.select(player, first.getSlotName()));
		assertTrue(QuestTracking.select(player, second.getSlotName()));
		assertSame(second, QuestTracking.current(player));
		assertEquals(second.getSlotName(), player.get("compass_tracked"));
		Player restored = new Player(new RPObject(player));
		assertSame(second, QuestTracking.current(restored));
		assertTrue(QuestTracking.select(player, ""));
		assertNull(QuestTracking.current(player));
	}
	@Test public void journalActionsValidateTheSlotAndReturnTrackingMetadata() {
		ProgressStatusQueryAction handler =
				new ProgressStatusQueryAction();
		RPAction action = new RPAction();
		action.put("progress_type", "Zadania poboczne"); action.put("item", first.getName());
		action.put("track_quest", first.getSlotName()); handler.onAction(player, action);
		assertSame(first, QuestTracking.current(player));
		boolean found = false;
		for (RPEvent event : player.events()) {
			if (event.has("tracking_slot")) { assertEquals(first.getSlotName(), event.get("tracking_slot")); found = true; }
		}
		assertTrue(found);
		action.put("track_quest", main.getSlotName()); handler.onAction(player, action);
		assertSame(first, QuestTracking.current(player));
		player.getSlot("bag").remove(compass.getID());
		action.put("track_quest", second.getSlotName()); handler.onAction(player, action);
		assertSame(first, QuestTracking.current(player));
	}
	@Test public void missingBankedForeignAndUnrepairedCompassesCannotSelectAQuest() {
		player.getSlot("bag").remove(compass.getID());
		player.getSlot("bank").add(compass);
		assertFalse(QuestTracking.select(player, first.getSlotName()));
		player.getSlot("bank").remove(compass.getID());
		assertTrue(player.equipToInventoryOnly(compass));
		compass.setBoundTo("someone-else");
		assertFalse(QuestTracking.available(player));
		compass.setBoundTo(player.getName());
		player.setQuest("wanderers_compass", "return");
		assertFalse(QuestTracking.available(player));
	}
	@Test public void rejectedUnstartedCompletedMainAndUnknownRequestsPreserveTheSelection() {
		assertTrue(QuestTracking.select(player, first.getSlotName()));
		assertFalse(QuestTracking.select(player, main.getSlotName()));
		assertFalse(QuestTracking.select(player, "missing"));
		for (String state : new String[] {"rejected", "done"}) {
			player.setQuest(second.getSlotName(), state);
			assertFalse(QuestTracking.select(player, second.getSlotName()));
		}
		player.removeQuest(second.getSlotName());
		assertFalse(QuestTracking.select(player, second.getSlotName()));
		assertSame(first, QuestTracking.current(player));
	}
	@Test public void bankingPausesTrackingButCompletionClearsIt() {
		assertTrue(QuestTracking.select(player, first.getSlotName()));
		player.getSlot("bag").remove(compass.getID());
		CompassService.update(player);
		assertEquals(0, player.getInt("compass_available"));
		assertSame(first, QuestTracking.current(player));
		assertFalse(player.has("compass_targets", "_tracked"));
		assertTrue(player.equipToInventoryOnly(compass));
		CompassService.update(player);
		assertEquals(first.getSlotName(), player.get("compass_targets", "_tracked"));
		player.setQuest(first.getSlotName(), "done");
		CompassService.update(player);
		assertFalse(player.has("compass_tracked"));
	}
	@Test public void unsupportedStagesPublishTextWithoutInventingAnArrowToTheQuestGiver() {
		assertTrue(QuestTracking.select(player, first.getSlotName()));
		CompassService.update(player);
		assertEquals(first.getSlotName(), player.get("compass_targets", "_tracked"));
		assertEquals("Zbierz potrzebne przedmioty.", player.get("compass_targets", "_objective"));
		assertEquals(4, player.getMap("compass_targets").size());
	}
	@Test public void bankingDoesNotReplaceThePausedQuestWithAnAutomaticMainGoal() {
		SpeakerNPC npc = new SpeakerNPC("tracking-npc");
		npc.setPosition(12, 12); player.getZone().add(npc);
		try {
			assertTrue(QuestTracking.select(player, first.getSlotName()));
			player.getSlot("bag").remove(compass.getID());
			CompassService.update(player);
			assertEquals("Banked compass must not calculate unrelated routes", 1, player.getMap("compass_targets").size());
		} finally { SingletonRepository.getNPCList().remove(npc.getName()); }
	}
	@Test public void gatheringChangesToReturnOnlyWhenTheRequestedItemsAreCarried() {
		SpeakerNPC npc = new SpeakerNPC("Haunchy Meatoch");
		npc.setPosition(12, 12);
		player.getZone().add(npc);
		try {
			BuiltQuest quest = new BuiltQuest(
					new CoalForHaunchy().story());
			player.setQuest(quest.getSlotName(), "start");
			assertNull(quest.getCompassTarget(player));
			assertTrue(quest.getCompassHint(player).contains("0/25"));
			StackableItem coal = (StackableItem)
					SingletonRepository.getEntityManager().getItem("węgiel");
			coal.setQuantity(25);
			assertTrue(player.equipToInventoryOnly(coal));
			assertEquals(npc.getName(), quest.getCompassTarget(player).label);
			coal.setQuantity(24);
			assertNull(quest.getCompassTarget(player));
		} finally { SingletonRepository.getNPCList().remove(npc.getName()); }
	}
	@Test public void deliveriesLeadToTheNamedCustomerAndMissingParcelsBackToTheSender() {
		DeliverItemQuestBuilder builder =
				new DeliverItemQuestBuilder();
		builder.info().internalName("tracking_delivery").questGiverNpc("tracking-sender");
		builder.task().itemName("pizza").order().customerNpc("tracking-customer").itemDescription("Pizza testowa");
		BuiltQuest quest = new BuiltQuest(builder);
		SpeakerNPC sender = new SpeakerNPC("tracking-sender");
		SpeakerNPC customer = new SpeakerNPC("tracking-customer");
		for (SpeakerNPC npc : Arrays.asList(sender, customer)) {
			player.getZone().add(npc);
		}
		try {
			player.setQuest(quest.getSlotName(), customer.getName() + ";1");
			assertEquals(sender.getName(), quest.getCompassTarget(player).label);
			Item parcel = SingletonRepository.getEntityManager().getItem("pizza");
			parcel.setBoundTo(player.getName()); assertTrue(player.equipToInventoryOnly(parcel));
			assertEquals("An ordinary pizza cannot replace the delivery order", sender.getName(), quest.getCompassTarget(player).label);
			parcel.setItemData("Pizza testowa");
			assertEquals(customer.getName(), quest.getCompassTarget(player).label);
			player.setQuest(quest.getSlotName(), "missing-customer;1");
			assertNull(quest.getCompassTarget(player));
		} finally {
			SingletonRepository.getNPCList().remove(sender.getName());
			SingletonRepository.getNPCList().remove(customer.getName());
		}
	}
	private static class Fixture extends AbstractQuest {
		private final String slot;
		Fixture(String slot, boolean main) {
			this.slot = slot;
			fillQuestInfo(slot, "Opis zadania.", false);
			if (main) questInfo.setMainQuest("Rozdział", 1, "Rozpocznij.");
		}
		@Override public String getSlotName() { return slot; }
		@Override public String getName() { return slot; }
		@Override public String getNPCName() { return "tracking-npc"; }
		@Override public void addToWorld() { }
		@Override public boolean removeFromWorld() { return true; }
		@Override public List<String> getHistory(Player p) { return Arrays.asList("Przyjęto zadanie."); }
		@Override public String getCompassHint(Player p) { return "Zbierz potrzebne przedmioty."; }
	}
}
