/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.Events;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradePreview;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeService;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.npc.behaviour.adder.ItemUpgradeAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ItemUpgradeAdder.ItemUpgradeNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ItemUpgradeEvent;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ItemUpgradeProtocolTest {
	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		ItemTestHelper.generateRPClasses();
		if (!RPClass.hasRPClass(Events.ITEM_UPGRADE)) {
			ItemUpgradeEvent.generateRPClass();
		}
	}

	@Test
	public void contextMenuActionStartsConversationAndOpensWindow() {
		final StendhalRPZone zone = new StendhalRPZone(
				"item_upgrade_protocol_context");
		final Player player = PlayerTestHelper.createPlayer("protocol_context");
		player.setPosition(5, 5);
		zone.add(player);
		final Item item = item("context item", 10);
		player.equipToInventoryOnly(item);

		final ItemUpgradeNPC npc = new ItemUpgradeNPC("context smith");
		npc.setPosition(5, 6);
		npc.addGreeting();
		new ItemUpgradeAdder().add(npc);
		zone.add(npc);

		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.OPEN);
		action.put(ItemUpgradeAction.NPC_ID, npc.getID().getObjectID());
		new ItemUpgradeAction().onAction(player, action);

		assertSame(player, npc.getAttending());
		assertTrue(npc.has("job_item_upgrader"));
		boolean foundUpgradeEvent = false;
		for (final RPEvent event : player.events()) {
			if (Events.ITEM_UPGRADE.equals(event.getName())) {
				foundUpgradeEvent = true;
				assertEquals("preview", event.get("phase"));
				assertEquals(ItemUpgradeEvent.encodePath(item),
						event.get("selected_path"));
			}
		}
		assertTrue(foundUpgradeEvent);
	}

	@Test
	public void clientRequestDoesNotUseTheServerEventRpClass()
			throws IOException {
		assertNotEquals(Actions.ITEM_UPGRADE, Events.ITEM_UPGRADE);

		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.PREVIEW);
		action.put(ItemUpgradeAction.NPC_ID, 77);
		action.put(ItemUpgradeAction.TARGET_PATH,
				Arrays.asList("1", "bag", "2"));

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		action.writeObject(new OutputSerializer(bytes));
		assertFalse(bytes.size() == 0);
	}

	@Test
	public void previewSerializesExactPathsAndParallelServerValues() {
		final Player player = PlayerTestHelper.createPlayer("protocol_preview");
		new StendhalRPZone("item_upgrade_protocol_preview").add(player);
		final Item first = item("identyczny miecz", 10);
		final Item second = item("identyczny miecz", 30);
		player.equipToInventoryOnly(first);
		player.equipToInventoryOnly(second);
		final ItemUpgradeService service = new ItemUpgradeService(new Random(1));
		final ItemUpgradePreview preview = service.createPreview(player, second);

		final ItemUpgradeEvent event = new ItemUpgradeEvent(77,
				Arrays.asList(first, second), preview, null);
		assertEquals("preview", event.get("phase"));
		assertEquals(77, event.getInt("npc_id"));
		assertEquals(2, event.getList("candidate_paths").size());
		assertNotEquals(event.getList("candidate_paths").get(0),
				event.getList("candidate_paths").get(1));
		assertEquals(ItemUpgradeEvent.encodePath(second), event.get("selected_path"));
		assertEquals(event.getList("stat_names").size(),
				event.getList("current_stat_values").size());
		assertEquals(event.getList("stat_names").size(),
				event.getList("upgraded_stat_values").size());
	}

	@Test
	public void invalidAndMovedPathsCannotResolveAStaleItem() {
		final Player player = PlayerTestHelper.createPlayer("protocol_moved");
		new StendhalRPZone("item_upgrade_protocol_moved").add(player);
		final Item item = item("moved item", 10);
		player.equipToInventoryOnly(item);
		final String oldPath = ItemUpgradeEvent.encodePath(item);
		final RPAction action = action(oldPath);
		assertSame(item, ItemUpgradeAction.resolveOwnedItem(player, action));

		item.getContainerSlot().remove(item.getID());
		player.equip("lhand", item);
		assertNull(ItemUpgradeAction.resolveOwnedItem(player, action));
		action.put(ItemUpgradeAction.TARGET_PATH,
				ItemUpgradeEvent.decodePath(ItemUpgradeEvent.encodePath(item)));
		assertSame(item, ItemUpgradeAction.resolveOwnedItem(player, action));

		final RPAction invalid = new RPAction();
		invalid.put(ItemUpgradeAction.TARGET_PATH, Arrays.asList("not-an-id"));
		assertNull(ItemUpgradeAction.resolveOwnedItem(player, invalid));
	}

	@Test
	public void resultAndMaxLevelRefreshAreReportedByServer() {
		final Player player = PlayerTestHelper.createPlayer("protocol_max");
		new StendhalRPZone("item_upgrade_protocol_max").add(player);
		final Item item = item("max refresh", 10);
		item.setUpgradeLevel(2);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = new ItemUpgradeService(new Random(1));
		final ItemUpgradePreview preview = service.createPreview(player, item);
		assertSame(ItemUpgradeResult.Status.MAX_LEVEL,
				preview.getBlockingStatus());

		final ItemUpgradeResult result = service.resultForStatus(
				ItemUpgradeResult.Status.MAX_LEVEL);
		final ItemUpgradeEvent event = new ItemUpgradeEvent(8,
				Arrays.asList(item), preview, result);
		assertEquals("result", event.get("phase"));
		assertEquals("MAX_LEVEL", event.get("status"));
		assertEquals(0, event.getInt("can_upgrade"));
	}

	@Test
	public void invalidRequestResultContainsNoClientGameplayValues() {
		final ItemUpgradeService service = new ItemUpgradeService(new Random(1));
		final ItemUpgradeEvent event = new ItemUpgradeEvent(0,
				Arrays.<Item>asList(), null, service.resultForStatus(
						ItemUpgradeResult.Status.INVALID_REQUEST));
		assertEquals("INVALID_REQUEST", event.get("status"));
		assertEquals("result", event.get("phase"));
		assertFalse(event.has("candidate_paths"));
	}

	private static RPAction action(final String path) {
		final RPAction action = new RPAction();
		action.put(ItemUpgradeAction.TARGET_PATH,
				ItemUpgradeEvent.decodePath(path));
		return action;
	}

	private static Weapon item(final String name, final int attack) {
		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		attributes.put("damage_min", Integer.toString(attack - 2));
		attributes.put("damage_max", Integer.toString(attack + 2));
		attributes.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, "2");
		final Weapon item = new Weapon(name, "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("bag", "lhand"));
		return item;
	}
}
