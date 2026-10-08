/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.transformer.PlayerTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Banks;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MineTownItemCleanupTest {
	private String previousProperty;
	private final List<StendhalRPZone> zones = new ArrayList<StendhalRPZone>();

	@BeforeClass
	public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }

	@Before
	public void setup() {
		previousProperty = System.getProperty("stendhal.minetown");
		System.setProperty("stendhal.minetown", "true");
	}

	@After
	public void cleanup() throws Exception {
		PlayerTestHelper.removeAllPlayers();
		for (final StendhalRPZone zone : zones) { SingletonRepository.getRPWorld().removeRPZone(zone.getID()); }
		if (previousProperty == null) { System.clearProperty("stendhal.minetown"); }
		else { System.setProperty("stendhal.minetown", previousProperty); }
	}

	private Item item(final String name) { return SingletonRepository.getEntityManager().getItem(name); }
	private Player player() { return PlayerTestHelper.createPlayer("event_cleanup_collector"); }

	@Test
	public void identifiesOnlyFestivalItemsIncludingLegacyLanterns() {
		assertTrue(MineTownItemCleanup.isEventItem(item("straszna dynia")));
		assertTrue(MineTownItemCleanup.isEventItem(item(MineTownLanternSpawns.LANTERN_NAME)));
		final Item legacy = item("latarenka");
		assertFalse(MineTownItemCleanup.isEventItem(legacy));
		legacy.setItemData("another_quest");
		assertFalse(MineTownItemCleanup.isEventItem(legacy));
		legacy.setItemData(MineTownLanternSpawns.itemData());
		assertTrue(MineTownItemCleanup.isEventItem(legacy));
		legacy.remove("itemdata");
		legacy.put("infostring", "minetown_lantern_26");
		assertTrue(MineTownItemCleanup.isEventItem(legacy));
		assertFalse(MineTownItemCleanup.isEventItem(item("dynia")));
		final RPObject unrelated = new RPObject();
		unrelated.put("name", "straszna dynia");
		assertFalse(MineTownItemCleanup.isEventItem(unrelated));
	}

	@Test
	public void removesWholeStacksFromBagAllBanksAndOtherSlotsButKeepsRewardsAndQuestState() {
		final Player player = player();
		final StackableItem pumpkins = (StackableItem) item("straszna dynia");
		pumpkins.setQuantity(100);
		player.getSlot("bag").add(pumpkins);
		for (final Banks bank : Banks.values()) {
			player.getSlot(bank.getSlotName()).add(item(MineTownLanternSpawns.LANTERN_NAME));
		}
		player.getSlot("magicbag").add(item("straszna dynia"));
		player.getSlot("rhand_set").add(item(MineTownLanternSpawns.LANTERN_NAME));
		final Item ordinary = item("latarenka");
		final Item pumpkin = item("dynia");
		final Item reward = item("złota skrzynia");
		player.getSlot("bag").add(ordinary);
		player.getSlot("bag").add(pumpkin);
		player.getSlot("bag").add(reward);
		player.setQuest("minetown_ritual_26", "done;123;1");
		assertEquals(Banks.values().length + 3, MineTownItemCleanup.cleanupPlayer(player, 1, false));
		assertSame(ordinary, player.getSlot("bag").get(ordinary.getID()));
		assertSame(pumpkin, player.getSlot("bag").get(pumpkin.getID()));
		assertSame(reward, player.getSlot("bag").get(reward.getID()));
		assertEquals("done;123;1", player.getQuest("minetown_ritual_26"));
		assertEquals(0, MineTownItemCleanup.cleanupPlayer(player, 1, false));
	}

	@Test
	public void recursivelyCleansContainersWithoutDeletingTheirOtherContents() {
		final Player player = player();
		final Item container = item("latarenka");
		container.setID(new RPObject.ID(902, "cleanup_container_test"));
		final RPSlot nested = new RPSlot("test_contents");
		nested.setCapacity(10);
		container.addSlot(nested);
		final Item kept = item("dynia");
		container.getSlot("test_contents").add(kept);
		container.getSlot("test_contents").add(item("straszna dynia"));
		player.getSlot("bag").add(container);
		assertEquals(1, MineTownItemCleanup.cleanupPlayer(player, 1, false));
		assertEquals(1, player.getSlot("bag").size());
		assertEquals(1, container.getSlot("test_contents").size());
		assertSame(kept, container.getSlot("test_contents").get(kept.getID()));
	}

	@Test
	public void activeEventPreservesCurrentInventory() {
		final Player player = player();
		player.setQuest(MineTownItemCleanup.PLAYER_MARKER, "3");
		player.getSlot("bag").add(item("straszna dynia"));
		player.getSlot("bag").add(item(MineTownLanternSpawns.LANTERN_NAME));
		assertEquals(0, MineTownItemCleanup.cleanupPlayer(player, 3, true));
		assertEquals(2, player.getSlot("bag").size());
	}

	@Test
	public void offlinePlayerCannotBringOldItemsIntoAReactivatedEvent() {
		final Player player = player();
		player.setQuest(MineTownItemCleanup.PLAYER_MARKER, "2");
		player.getSlot("bag").add(item("straszna dynia"));
		player.getSlot(Banks.values()[0].getSlotName()).add(item(MineTownLanternSpawns.LANTERN_NAME));
		assertEquals(2, MineTownItemCleanup.cleanupPlayer(player, 3, true));
		assertEquals("3", player.getQuest(MineTownItemCleanup.PLAYER_MARKER));
		player.getSlot("bag").add(item("straszna dynia"));
		assertEquals(0, MineTownItemCleanup.cleanupPlayer(player, 3, true));
	}

	@Test
	public void missingOrMalformedMarkersCannotBypassARecordedEventEnd() {
		for (final String marker : new String[] { null, "bad", "-1", "99" }) {
			final Player player = player();
			if (marker == null) { player.removeQuest(MineTownItemCleanup.PLAYER_MARKER); }
			else { player.setQuest(MineTownItemCleanup.PLAYER_MARKER, marker); }
			player.getSlot("bag").add(item("straszna dynia"));
			assertEquals(1, MineTownItemCleanup.cleanupPlayer(player, 3, true));
		}
	}

	@Test
	public void loginRestorationCleansOfflineInventoryWhenEventIsOff() throws Exception {
		final Player player = player();
		player.setID(new RPObject.ID(901, "cleanup_restore_test"));
		player.getSlot("bag").add(item("straszna dynia"));
		player.getSlot("bag").add(item("latarenka"));
		player.getSlot(Banks.values()[0].getSlotName()).add(item(MineTownLanternSpawns.LANTERN_NAME));
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		player.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject saved = new RPObject();
		saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		System.clearProperty("stendhal.minetown");
		final Player restored = new PlayerTransformer().create(saved);
		assertEquals(1, restored.getSlot("bag").size());
		assertEquals("latarenka", restored.getSlot("bag").getFirst().get("name"));
		assertEquals(0, restored.getSlot(Banks.values()[0].getSlotName()).size());
	}

	@Test
	public void restartWithInactiveEventCleansTheWorldAndInvalidatesOfflineInventory() throws Exception {
		final Player offline = player();
		offline.getSlot("bag").add(item("straszna dynia"));
		System.clearProperty("stendhal.minetown");
		MineTownItemCleanup.initialize();
		System.setProperty("stendhal.minetown", "true");
		MineTownItemCleanup.initialize();
		assertEquals(1, MineTownItemCleanup.onLogin(offline));
		offline.getSlot("bag").add(item("straszna dynia"));
		assertEquals(0, MineTownItemCleanup.onLogin(offline));
	}

	@Test
	public void endCleansOnlinePlayersGroundCorpsesAndHouseContainers() throws Exception {
		final StendhalRPZone zone = new StendhalRPZone("cleanup_world_test", 32, 32);
		SingletonRepository.getRPWorld().addRPZone(zone);
		zones.add(zone);
		final Player player = player();
		PlayerTestHelper.registerPlayer(player, zone);
		player.getSlot("bag").add(item("straszna dynia"));
		final Item ground = item(MineTownLanternSpawns.LANTERN_NAME);
		zone.add(ground, false);
		final Item chest = item("latarenka");
		chest.setID(new RPObject.ID(903, "cleanup_world_test"));
		final RPSlot content = new RPSlot("content");
		content.setCapacity(10);
		chest.addSlot(content);
		chest.getSlot("content").add(item("straszna dynia"));
		final Item kept = item("dynia");
		chest.getSlot("content").add(kept);
		zone.add(chest, false);
		final Corpse corpse = new Corpse("rat", 3, 3);
		corpse.add(item("straszna dynia"));
		corpse.add(item("dynia"));
		zone.add(corpse);
		System.clearProperty("stendhal.minetown");
		MineTownItemCleanup.finishEvent();
		assertEquals(0, player.getSlot("bag").size());
		assertFalse(zone.has(ground.getID()));
		assertTrue(zone.has(chest.getID()));
		assertEquals(1, chest.getSlot("content").size());
		assertSame(kept, chest.getSlot("content").get(kept.getID()));
		assertEquals(1, corpse.getSlot("content").size());
		assertEquals("dynia", corpse.getSlot("content").getFirst().get("name"));
		final String marker = player.getQuest(MineTownItemCleanup.PLAYER_MARKER);
		System.setProperty("stendhal.minetown", "true");
		MineTownItemCleanup.initialize(); // Reloads the durable marker like a server restart.
		assertEquals(marker, player.getQuest(MineTownItemCleanup.PLAYER_MARKER));
		player.getSlot("bag").add(item("straszna dynia"));
		assertEquals(0, MineTownItemCleanup.onLogin(player));
		final Player offline = player();
		offline.removeQuest(MineTownItemCleanup.PLAYER_MARKER);
		offline.getSlot("bag").add(item("straszna dynia"));
		assertEquals(1, MineTownItemCleanup.onLogin(offline));
	}
}
