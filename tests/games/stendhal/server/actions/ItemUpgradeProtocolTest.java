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
import games.stendhal.server.events.ItemUpgradeEventCompatibility;
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
		ItemUpgradeEventCompatibility.generateRPClasses();
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
		final ItemUpgradeService service = ItemUpgradeService.getInstance();
		PlayerTestHelper.equipWithMoney(player,
				service.calculateUpgradeFee(player, item));
		for (final Map.Entry<String, Integer> material
				: service.getMaterialRequirements(1).entrySet()) {
			PlayerTestHelper.equipWithStackableItem(player, material.getKey(),
					material.getValue());
		}

		final ItemUpgradeNPC npc = new ItemUpgradeNPC("context smith");
		npc.setPosition(5, 8);
		npc.addGreeting();
		new ItemUpgradeAdder().add(npc);
		zone.add(npc);

		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.OPEN);
		action.put(ItemUpgradeAction.NPC_ID, npc.getID().getObjectID());
		new ItemUpgradeAction().onAction(player, action);

		assertFalse(player.nextTo(npc));
		assertSame(player, npc.getAttending());
		assertTrue(npc.has("job_item_upgrader"));
		final RPEvent openEvent = findUpgradeEvent(player);
		assertEquals("open", openEvent.get("phase"));
		assertEquals("SELECT_ITEM", openEvent.get("status"));
		assertFalse(openEvent.has("selected_path"));

		npc.endConversation();
		assertNull(npc.getAttending());
		player.clearEvents();
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.PREVIEW);
		action.put(ItemUpgradeAction.TARGET_PATH,
				ItemUpgradeEvent.decodePath(ItemUpgradeEvent.encodePath(item)));
		new ItemUpgradeAction().onAction(player, action);
		final RPEvent previewEvent = findUpgradeEvent(player);
		assertEquals(ItemUpgradeEvent.PHASE_PREVIEW, previewEvent.get("phase"));
		assertEquals(ItemUpgradeEvent.encodePath(item),
				previewEvent.get("selected_path"));
		assertEquals(1, previewEvent.getInt("can_upgrade"));

		player.clearEvents();
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.UPGRADE);
		action.put(ItemUpgradeAction.REQUEST_TOKEN,
				previewEvent.get("request_token"));
		new ItemUpgradeAction().onAction(player, action);
		final RPEvent resultEvent = findUpgradeEvent(player);
		assertEquals(ItemUpgradeEvent.PHASE_RESULT, resultEvent.get("phase"));
		assertEquals(ItemUpgradeResult.Status.SUCCESS.name(),
				resultEvent.get("status"));
		assertEquals(1, item.getUpgradeLevel());
	}

	@Test
	public void rejectedDistantContextOpenDoesNotCreateAnEmptyWindow() {
		final StendhalRPZone zone = new StendhalRPZone(
				"item_upgrade_protocol_distant_context");
		final Player player = PlayerTestHelper.createPlayer("protocol_distant");
		player.setPosition(2, 2);
		zone.add(player);
		final ItemUpgradeNPC npc = new ItemUpgradeNPC("distant smith");
		npc.setPosition(2, 10);
		npc.addGreeting();
		new ItemUpgradeAdder().add(npc);
		zone.add(npc);

		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.OPEN);
		action.put(ItemUpgradeAction.NPC_ID, npc.getID().getObjectID());
		new ItemUpgradeAction().onAction(player, action);

		assertNull(npc.getAttending());
		for (final RPEvent event : player.events()) {
			assertNotEquals(Events.ITEM_UPGRADE, event.getName());
		}
	}

	@Test
	public void refreshWithoutSelectionUpdatesExactCandidatePaths() {
		final StendhalRPZone zone = new StendhalRPZone(
				"item_upgrade_protocol_refresh");
		final Player player = PlayerTestHelper.createPlayer("protocol_refresh");
		player.setPosition(5, 5);
		zone.add(player);
		final Item item = item("new candidate", 12);
		player.equipToInventoryOnly(item);
		final ItemUpgradeNPC npc = new ItemUpgradeNPC("refresh smith");
		npc.setPosition(5, 8);
		new ItemUpgradeAdder().add(npc);
		zone.add(npc);

		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
		action.put(ItemUpgradeAction.COMMAND, ItemUpgradeAction.REFRESH);
		action.put(ItemUpgradeAction.NPC_ID, npc.getID().getObjectID());
		new ItemUpgradeAction().onAction(player, action);

		final RPEvent event = findUpgradeEvent(player);
		assertEquals(ItemUpgradeEvent.PHASE_REFRESH, event.get("phase"));
		assertEquals(ItemUpgradeResult.Status.SELECT_ITEM.name(),
				event.get("status"));
		assertFalse(event.has("selected_path"));
		assertEquals(Arrays.asList(ItemUpgradeEvent.encodePath(item)),
				event.getList("candidate_paths"));
		assertNull(npc.getAttending());
	}

	@Test
	public void clearAndCloseCommandsInvalidatePreviewWithoutUiResponse() {
		final Player player = PlayerTestHelper.createPlayer("protocol_lifecycle");
		final Item item = item("lifecycle item", 14);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = ItemUpgradeService.getInstance();
		provideRequirements(player, item, service);

		for (final String command : Arrays.asList(ItemUpgradeAction.CLEAR,
				ItemUpgradeAction.CLOSE)) {
			final ItemUpgradePreview preview = service.createPreview(player, item);
			player.clearEvents();
			final RPAction action = new RPAction();
			action.put(Actions.TYPE, Actions.ITEM_UPGRADE);
			action.put(ItemUpgradeAction.COMMAND, command);
			new ItemUpgradeAction().onAction(player, action);

			assertFalse(player.events().iterator().hasNext());
			assertSame(ItemUpgradeResult.Status.STALE_PREVIEW,
					service.performUpgrade(player, item,
							preview.getRequestToken()).getStatus());
		}
		assertEquals(0, item.getUpgradeLevel());
	}

	@Test
	public void clientRequestDoesNotUseTheServerEventRpClass()
			throws IOException {
		assertNotEquals(Actions.ITEM_UPGRADE, Events.ITEM_UPGRADE);
		assertNotEquals(Actions.ITEM_UPGRADE,
				ItemUpgradeEventCompatibility.LEGACY_EVENT);
		assertFalse(RPClass.hasRPClass(Actions.ITEM_UPGRADE));

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
		assertEquals(event.getList("material_names").size(),
				event.getList("material_classes").size());
		assertEquals(event.getList("material_names").size(),
				event.getList("material_subclasses").size());
		assertEquals("resource", event.getList("material_classes").get(0));
		assertEquals("wood", event.getList("material_subclasses").get(0));
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

	private static RPEvent findUpgradeEvent(final Player player) {
		for (final RPEvent event : player.events()) {
			if (Events.ITEM_UPGRADE.equals(event.getName())) {
				return event;
			}
		}
		throw new AssertionError("Missing item-upgrade event");
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

	private static void provideRequirements(final Player player,
			final Item item, final ItemUpgradeService service) {
		PlayerTestHelper.equipWithMoney(player,
				service.calculateUpgradeFee(player, item));
		for (final Map.Entry<String, Integer> material
				: service.getMaterialRequirements(1).entrySet()) {
			PlayerTestHelper.equipWithStackableItem(player, material.getKey(),
					material.getValue());
		}
	}
}
