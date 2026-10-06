/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.ZoneMapUpdater;
import games.stendhal.server.core.events.seasonal.MineTownLanternSpawns;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.QuestUtils;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.script.Guslarz;
import java.util.Collections;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class MineTownQuestsTest {
	private Player player;
	private SpeakerNPC npc;
	private Engine engine;
	private String previousProperty;
	private static final long TIME = 1800000000000L;

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		previousProperty = System.getProperty("stendhal.minetown");
		System.setProperty("stendhal.minetown", "true");
		player = PlayerTestHelper.createPlayer("festival_player");
		npc = SpeakerNPCTestHelper.createSpeakerNPC("festival_test_npc");
		npc.addGreeting("Witaj!");
		engine = npc.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
	}

	@After
	public void cleanup() {
		if (previousProperty == null) { System.clearProperty("stendhal.minetown"); }
		else { System.setProperty("stendhal.minetown", previousProperty); }
		NPCList.get().remove(npc.getName());
		PlayerTestHelper.removeAllPlayers();
	}

	private void accept() {
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		assertTrue(engine.step(player, "tak"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
	}

	private void deliver() {
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.QUEST_ITEM_BROUGHT, engine.getCurrentState());
		assertTrue(engine.step(player, "tak"));
	}

	private Item equip(final String name, final int quantity) {
		final Item item = SingletonRepository.getEntityManager().getItem(name);
		assertNotNull(name, item);
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(quantity);
		}
		assertTrue(player.equipToInventoryOnly(item));
		return item;
	}

	private void food(final int steaks) {
		equip("mięso", 100);
		equip("szynka", 50);
		equip("ser", 100);
		equip("stek", steaks);
	}

	private static final class TimedFeast extends MineTownFeast {
		long time = TIME;
		@Override protected long now() { return time; }
	}

	private static final class TimedLanterns extends MineTownLanterns {
		long time = TIME;
		@Override protected long now() { return time; }
	}

	@Test
	public void feastConsumesExactSuppliesAndRepeatsAfterSixHours() {
		final TimedFeast quest = new TimedFeast();
		quest.attachDialog(npc);
		food(41);
		accept();
		deliver();
		assertTrue(quest.isCompleted(player));
		assertEquals(1, quest.completionCount(player));
		assertEquals(0, player.getNumberOfEquipped("mięso"));
		assertEquals(0, player.getNumberOfEquipped("szynka"));
		assertEquals(0, player.getNumberOfEquipped("ser"));
		assertEquals(1, player.getNumberOfEquipped("stek"));
		assertEquals(player.getName(), player.getFirstEquipped("skrzynka").getBoundTo());
		quest.time += 6 * 3600000L - 1;
		assertFalse(quest.isRepeatable(player));
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		quest.time++;
		assertTrue(quest.isRepeatable(player));
		food(40);
		accept();
		assertEquals(1, quest.completionCount(player));
		deliver();
		assertEquals(2, quest.completionCount(player));
		assertEquals(2, player.getNumberOfEquipped("skrzynka"));
	}

	@Test
	public void missingFoodDoesNotConsumePartialSupplies() {
		final MineTownFeast quest = new MineTownFeast();
		quest.attachDialog(npc);
		food(39);
		accept();
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertEquals(100, player.getNumberOfEquipped("mięso"));
		assertEquals(0, player.getNumberOfEquipped("skrzynka"));
		assertFalse(quest.isCompleted(player));
	}

	@Test
	public void feastMarkerTracksSuppliesCooldownAndEventAvailability() {
		final TimedFeast quest = new TimedFeast();
		quest.attachDialog(npc);
		assertEquals(QuestMarker.REPEATABLE, quest.getNPCQuestMarker(player, npc));
		accept();
		assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, npc));
		food(40);
		assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, npc));
		System.clearProperty("stendhal.minetown");
		assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, npc));
		System.setProperty("stendhal.minetown", "true");
		deliver();
		assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, npc));
		quest.time += 6 * 3600000L;
		assertEquals(QuestMarker.REPEATABLE, quest.getNPCQuestMarker(player, npc));
	}

	@Test
	public void lanternMarkerOnlyCountsCurrentFestivalItems() {
		final TimedLanterns quest = new TimedLanterns();
		quest.attachDialog(npc);
		accept();
		for (int i = 0; i < MineTownLanterns.REQUIRED_LANTERNS; i++) { equip("latarenka", 1); }
		assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, npc));
		final StackableItem lanterns = (StackableItem) equip(
				MineTownLanternSpawns.LANTERN_NAME, MineTownLanterns.REQUIRED_LANTERNS);
		lanterns.setItemData("minetown_lantern_old_edition");
		assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, npc));
		lanterns.setItemData(MineTownLanternSpawns.itemData());
		assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, npc));
		deliver();
		assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, npc));
		quest.time += 12 * 3600000L;
		assertEquals(QuestMarker.REPEATABLE, quest.getNPCQuestMarker(player, npc));
	}

	@Test
	public void rechecksSuppliesAfterConfirmationOffer() {
		final MineTownFeast quest = new MineTownFeast();
		quest.attachDialog(npc);
		food(40);
		accept();
		assertTrue(engine.step(player, "zadanie"));
		assertTrue(player.drop("stek", 1));
		assertTrue(engine.step(player, "tak"));
		assertEquals(100, player.getNumberOfEquipped("mięso"));
		assertEquals(0, player.getNumberOfEquipped("skrzynka"));
		assertFalse(quest.isCompleted(player));
	}

	@Test
	public void eventOffBlocksAcceptanceAndDelivery() {
		final MineTownFeast quest = new MineTownFeast();
		quest.attachDialog(npc);
		food(40);
		System.clearProperty("stendhal.minetown");
		assertTrue(engine.step(player, "zadanie"));
		assertFalse(player.hasQuest(MineTownFeast.QUEST_SLOT));
		System.setProperty("stendhal.minetown", "true");
		accept();
		assertTrue(engine.step(player, "zadanie"));
		System.clearProperty("stendhal.minetown");
		assertTrue(engine.step(player, "tak"));
		assertEquals(100, player.getNumberOfEquipped("mięso"));
		assertEquals(0, player.getNumberOfEquipped("skrzynka"));
	}

	@Test
	public void onlyCurrentEventLanternsCountAndRepeatAfterTwelveHours() {
		final TimedLanterns quest = new TimedLanterns();
		quest.attachDialog(npc);
		for (int i = 0; i < 5; i++) { equip("latarenka", 1); }
		accept();
		assertFalse(quest.hasRequiredItems(player));
		final Item oldEdition = SingletonRepository.getEntityManager().getItem(MineTownLanternSpawns.LANTERN_NAME);
		oldEdition.setItemData("minetown_lantern_old_edition");
		((StackableItem) oldEdition).setQuantity(5);
		assertTrue(player.equipToInventoryOnly(oldEdition));
		assertFalse(quest.hasRequiredItems(player));
		for (int i = 0; i < 7; i++) {
			final Item lantern = SingletonRepository.getEntityManager().getItem(MineTownLanternSpawns.LANTERN_NAME);
			lantern.setItemData(MineTownLanternSpawns.itemData());
			assertTrue(player.equipToInventoryOnly(lantern));
		}
		assertEquals(2, player.getAllEquipped(MineTownLanternSpawns.LANTERN_NAME).size());
		deliver();
		assertEquals(5, player.getNumberOfEquipped("latarenka"));
		assertEquals(7, player.getNumberOfEquipped(MineTownLanternSpawns.LANTERN_NAME));
		assertEquals(5, oldEdition.getQuantity());
		assertFalse(player.isEquippedWithItemdata(MineTownLanternSpawns.LANTERN_NAME,
				MineTownLanternSpawns.itemData(), 3));
		assertTrue(player.isEquippedWithItemdata(MineTownLanternSpawns.LANTERN_NAME,
				MineTownLanternSpawns.itemData(), 2));
		assertEquals(1, player.getNumberOfEquipped("srebrna skrzynia"));
		assertEquals(player.getName(), player.getFirstEquipped("srebrna skrzynia").getBoundTo());
		quest.time += 12 * 3600000L - 1;
		assertFalse(quest.isRepeatable(player));
		quest.time++;
		assertTrue(quest.isRepeatable(player));
	}

	@Test
	public void ritualNeedsBothCurrentEditionQuestsAndRewardsOnlyOnce() {
		final MineTownRitual quest = new MineTownRitual();
		quest.attachDialog(npc);
		equip("straszna dynia", 20);
		// A previous edition is not enough.
		assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, npc));
		player.setQuest(QuestUtils.evaluateQuestSlotName(MineTownFeast.QUEST_SLOT) + "_old", "done;1;1");
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		player.setQuest(MineTownFeast.QUEST_SLOT, "done;1;1");
		assertFalse(quest.prerequisitesMet(player));
		player.setQuest(MineTownLanterns.QUEST_SLOT, "start;1;1");
		assertTrue(quest.prerequisitesMet(player));
		assertEquals(QuestMarker.AVAILABLE, quest.getNPCQuestMarker(player, npc));
		accept();
		assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, npc));
		deliver();
		assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, npc));
		assertEquals(0, player.getNumberOfEquipped("straszna dynia"));
		assertEquals(1, player.getNumberOfEquipped("złota skrzynia"));
		assertEquals(player.getName(), player.getFirstEquipped("złota skrzynia").getBoundTo());
		assertTrue(engine.step(player, "zadanie"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertFalse(quest.isRepeatable(player));
		System.clearProperty("stendhal.minetown");
		System.setProperty("stendhal.minetown", "true");
		// Recreating/re-enabling the event does not reset persisted completions.
		assertEquals(1, new MineTownRitual().completionCount(player));
		assertEquals(1, player.getNumberOfEquipped("złota skrzynia"));
	}

	@Test
	public void festivalNPCsArePlacedAndRemovedWithoutDuplicates() throws Exception {
		final StendhalRPZone zone = new StendhalRPZone(MineTownCollectionQuest.FESTIVAL_ZONE, 128, 128);
		final StendhalMapStructure map = ZoneMapUpdater.prepare("Level 0/zakopane/s_halloween.tmx");
		zone.collisionMap.setCollisionData(map.getLayer("collision"));
		zone.setPosition(0, 0, 0);
		SingletonRepository.getRPWorld().addRPZone(zone);
		final MineTownFeast feast = new MineTownFeast();
		final MineTownLanterns lanterns = new MineTownLanterns();
		try {
			feast.addToWorld();
			lanterns.addToWorld();
			assertNotNull(NPCList.get().get("Boguchwał"));
			assertNotNull(NPCList.get().get("Wolrad"));
			assertEquals(zone, NPCList.get().get("Boguchwał").getZone());
			assertEquals(zone, NPCList.get().get("Wolrad").getZone());
			assertFalse(zone.collides(NPCList.get().get("Boguchwał").getX(), NPCList.get().get("Boguchwał").getY()));
			assertFalse(zone.collides(NPCList.get().get("Wolrad").getX(), NPCList.get().get("Wolrad").getY()));
			assertTrue(feast.removeFromWorld());
			assertTrue(lanterns.removeFromWorld());
			assertNull(NPCList.get().get("Boguchwał"));
			assertNull(NPCList.get().get("Wolrad"));
			feast.addToWorld();
			assertNotNull(NPCList.get().get("Boguchwał"));
		} finally {
			feast.removeFromWorld();
			lanterns.removeFromWorld();
			SingletonRepository.getRPWorld().removeRPZone(zone.getID());
		}
	}

	@Test
	public void runtimeGuslarzStartsFromInactiveRegisteredQuestAndKeepsBothRewards() throws Exception {
		final String oldGuslarz = System.getProperty("stendhal.guslarz");
		final StendhalQuestSystem system = StendhalQuestSystem.get();
		// The full suite shares the quest registry between test classes.
		if (system.getQuest(MeetGuslarz.QUEST_NAME) != null) {
			assertTrue(system.unloadQuest(MeetGuslarz.QUEST_NAME));
		}
		assertNull(system.getQuest(MeetGuslarz.QUEST_NAME));
		final StendhalRPZone existing = SingletonRepository.getRPWorld().getZone("int_admin_playground");
		final StendhalRPZone playground = existing == null
				? new StendhalRPZone("int_admin_playground", 32, 32) : existing;
		if (existing == null) { SingletonRepository.getRPWorld().addRPZone(playground); }
		final Guslarz script = new Guslarz();
		try {
			System.clearProperty("stendhal.guslarz");
			system.loadQuest(new MeetGuslarz());
			assertNull(NPCList.get().get("Guślarz"));
			script.execute(player, Collections.singletonList("true"));
			final SpeakerNPC guslarz = NPCList.get().get("Guślarz");
			assertNotNull(guslarz);
			final Engine dialog = guslarz.getEngine();
			assertTrue(dialog.step(player, "hi"));
			assertEquals(ConversationStates.ATTENDING, dialog.getCurrentState());
			assertEquals(1, player.getNumberOfEquipped("skrzynka"));
			player.setQuest(MineTownFeast.QUEST_SLOT, "done;1;1");
			player.setQuest(MineTownLanterns.QUEST_SLOT, "done;1;1");
			equip("straszna dynia", 20);
			assertTrue(dialog.step(player, "zadanie"));
			assertEquals(ConversationStates.QUEST_OFFERED, dialog.getCurrentState());
			assertTrue(dialog.step(player, "tak"));
			assertTrue(dialog.step(player, "zadanie"));
			assertEquals(ConversationStates.QUEST_ITEM_BROUGHT, dialog.getCurrentState());
			assertTrue(dialog.step(player, "tak"));
			assertEquals(1, player.getNumberOfEquipped("złota skrzynia"));
			assertTrue(dialog.step(player, "bye"));
			assertTrue(dialog.step(player, "hi"));
			assertEquals(1, player.getNumberOfEquipped("skrzynka"));
			script.execute(player, Collections.singletonList("false"));
			assertNull(NPCList.get().get("Guślarz"));
		} finally {
			if (system.getQuest(MeetGuslarz.QUEST_NAME) != null) {
				system.unloadQuest(MeetGuslarz.QUEST_NAME);
			}
			if (oldGuslarz == null) { System.clearProperty("stendhal.guslarz"); }
			else { System.setProperty("stendhal.guslarz", oldGuslarz); }
			if (existing == null) { SingletonRepository.getRPWorld().removeRPZone(playground.getID()); }
		}
	}

	@Test
	public void collectionDialogsUseNaturalSpeechAndCorrectRewardNames() {
		food(40);
		for (int i = 0; i < MineTownLanterns.REQUIRED_LANTERNS; i++) {
			final Item lantern = SingletonRepository.getEntityManager().getItem(MineTownLanternSpawns.LANTERN_NAME);
			lantern.setItemData(MineTownLanternSpawns.itemData());
			assertTrue(player.equipToInventoryOnly(lantern));
		}
		equip("straszna dynia", 20);
		final MineTownCollectionQuest[] quests = {
				new MineTownFeast(), new MineTownLanterns(), new MineTownRitual()
		};
		final String[] offeredRewards = { "skrzynkę", "srebrną skrzynię", "złotą skrzynię" };
		for (int i = 0; i < quests.length; i++) {
			final SpeakerNPC speaker = SpeakerNPCTestHelper.createSpeakerNPC("festival_dialog_" + i);
			try {
				quests[i].attachDialog(speaker);
				final Engine dialog = speaker.getEngine();
				dialog.setCurrentState(ConversationStates.ATTENDING);
				assertTrue(dialog.step(player, "nagroda"));
				final String promise = getReply(speaker);
				assertNaturalSpeech(promise);
				assertTrue(promise, promise.contains(offeredRewards[i]));
				assertTrue(dialog.step(player, "zadanie"));
				assertEquals(ConversationStates.QUEST_OFFERED, dialog.getCurrentState());
				assertNaturalSpeech(getReply(speaker));
				assertTrue(dialog.step(player, "tak"));
				assertNaturalSpeech(getReply(speaker));
				assertTrue(dialog.step(player, "zadanie"));
				assertEquals(ConversationStates.QUEST_ITEM_BROUGHT, dialog.getCurrentState());
				assertNaturalSpeech(getReply(speaker));
				assertTrue(dialog.step(player, "tak"));
				final String thanks = getReply(speaker);
				assertNaturalSpeech(thanks);
				assertTrue(thanks, thanks.contains(offeredRewards[i]));
				assertTrue(quests[i].isCompleted(player));
				for (final String entry : quests[i].getHistory(player)) {
					assertNaturalSpeech(entry);
				}
			} finally {
				NPCList.get().remove(speaker.getName());
			}
		}
	}

	@Test
	public void waitingDialogUsesFullPolishMinuteForms() {
		final TimedFeast quest = new TimedFeast();
		quest.attachDialog(npc);
		food(40);
		accept();
		deliver();
		final int[] minutes = { 1, 2, 12, 22, 60, 122, 300 };
		final String[] phrases = {
				"1 minutę", "2 minuty", "12 minut", "22 minuty",
				"1 godzinę", "2 godziny i 2 minuty", "5 godzin"
		};
		for (int i = 0; i < minutes.length; i++) {
			quest.time = TIME + 6 * 3600000L - minutes[i] * 60000L;
			assertTrue(engine.step(player, "zadanie"));
			final String reply = getReply(npc);
			assertNaturalSpeech(reply);
			assertTrue(reply, reply.contains(phrases[i]));
		}
	}

	private static void assertNaturalSpeech(final String reply) {
		assertNotNull(reply);
		assertFalse(reply, reply.matches("(?s).*[:;\\-\u2013\u2014].*"));
		for (final String technical : new String[] {
				"edycji", "mapach", "Sklepowe", "dostępne", "Nagroda:", "przedmioty", " min."
		}) {
			assertFalse(reply, reply.contains(technical));
		}
	}

	@Test
	public void declinedQuestDoesNotStartOrTakeItems() {
		new MineTownFeast().attachDialog(npc);
		food(40);
		assertTrue(engine.step(player, "zadanie"));
		assertTrue(engine.step(player, "nie"));
		assertFalse(player.hasQuest(MineTownFeast.QUEST_SLOT));
		assertEquals(100, player.getNumberOfEquipped("mięso"));
		assertEquals("Może innym razem.", getReply(npc));
	}
}
