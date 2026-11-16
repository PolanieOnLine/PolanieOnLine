/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.actions;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Events;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.ItemAffixDefinition;
import games.stendhal.server.core.rule.rarity.ItemAffixRegistry;
import games.stendhal.server.core.rule.rarity.ItemAffixRerollService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.LegendaryItemAffixRegistry;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AffixRerollEvent;
import games.stendhal.server.events.ItemUpgradeEvent;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.tarnow.city.RuneMasterNPC;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class AffixRerollActionTest {
	private Player player;
	private SpeakerNPC npc;
	private Item item;
	private final AffixRerollAction action = new AffixRerollAction();
	private int request;

	@BeforeClass public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
		if (!RPClass.hasRPClass(Events.AFFIX_REROLL)) games.stendhal.server.events.AffixRerollEvent.generateRPClass();
	}

	@Before public void setup() {
		SingletonRepository.getNPCList().remove("Zoryk Runiczny");
		final StendhalRPZone zone = new StendhalRPZone("reroll_action_test", 128, 128);
		MockStendlRPWorld.get().addRPZone(zone);
		new RuneMasterNPC().configureZone(zone, Collections.emptyMap());
		npc = SingletonRepository.getNPCList().get("Zoryk Runiczny");
		player = PlayerTestHelper.createPlayer("zoryk_customer"); player.setLanguage("pl");
		player.setPosition(60, 17); zone.add(player);
		item = SingletonRepository.getEntityManager().getItem("ciupaga"); item.setRarity(ItemRarity.RARE);
		final ItemAffixDefinition affix = ItemAffixRegistry.getInstance().get("accuracy_bonus");
		assertTrue(affix.apply(item, new Random(4))); ItemAffixState.record(item, affix);
		assertTrue(player.equipToInventoryOnly(item));
		final StackableItem stone = (StackableItem) SingletonRepository.getEntityManager().getItem("kamień przemiany");
		stone.setQuantity(2); assertTrue(player.equipToInventoryOnly(stone));
	}

	@After public void cleanup() { SingletonRepository.getNPCList().remove("Zoryk Runiczny"); }

	private RPEvent latest() {
		RPEvent result = null;
		for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) result = event;
		assertNotNull("Zoryk must send a calendar-independent service event", result);
		return result;
	}

	private RPAction command(final RPEvent window, final String operation) {
		final RPAction result = new RPAction();
		result.put("window_id", window.get("window_id")); result.put("npc_id", window.getInt("npc_id"));
		result.put("request_id", "test:" + ++request); result.put("command", operation);
		result.put("target_path", ItemUpgradeEvent.decodePath(ItemUpgradeEvent.encodePath(item)));
		return result;
	}

	private RPEvent quote() {
		AffixRerollAction.open(player, npc);
		final RPAction select = command(latest(), "select"); select.put("affix_id", "accuracy_bonus");
		action.onAction(player, select); assertEquals("READY", latest().get("status"));
		return latest();
	}

	@Test public void zorykUnderstandsTheVisibleDialogueKeywordAndItsInflection() {
		for (final String keyword : new String[] {"przemiana", "przemianę"}) {
			npc.getEngine().stepTest(player, "hi"); npc.getEngine().stepTest(player, keyword);
			assertEquals("open", latest().get("phase"));
			assertEquals(npc.getID().getObjectID(), latest().getInt("npc_id"));
			npc.getEngine().stepTest(player, "bye");
		}
	}

	@Test public void legendaryQuotesAndOffersPublishTheThreeStonePrice() {
		item.setRarity(ItemRarity.LEGENDARY);
		final ItemAffixDefinition affix = LegendaryItemAffixRegistry.getInstance().get("legendary_deep_wounds");
		assertTrue(affix.apply(item, new Random(3))); ItemAffixState.record(item, affix);
		AffixRerollAction.open(player, npc);
		RPAction select = command(latest(), "select"); select.put("affix_id", affix.getId());
		action.onAction(player, select);
		assertEquals("MISSING_STONE", latest().get("status"));
		assertEquals(3, latest().getInt("stone_cost")); assertEquals(0, latest().getInt("can_roll"));
		for (final marauroa.common.game.RPObject object : player.getSlot("bag")) {
			if (object instanceof StackableItem) ((StackableItem) object).setQuantity(3);
		}
		select = command(latest(), "select"); select.put("affix_id", affix.getId()); action.onAction(player, select);
		assertEquals("READY", latest().get("status")); assertEquals(1, latest().getInt("can_roll"));
		final RPAction roll = command(latest(), "roll"); roll.put("affix_id", affix.getId());
		roll.put("request_token", latest().get("request_token")); action.onAction(player, roll);
		assertEquals("OFFER", latest().get("status")); assertEquals(3, latest().getInt("stone_cost"));
		assertEquals(0, latest().getInt("stone_count")); assertEquals(affix.getId(), latest().get("rerolled_affix"));
	}

	@Test public void paidChoiceResumesAfterCloseAndAnOldWindowCannotChargeAgain() {
		final RPEvent preview = quote(); final RPAction roll = command(preview, "roll");
		roll.put("affix_id", "accuracy_bonus"); roll.put("request_token", preview.get("request_token"));
		action.onAction(player, roll); final RPEvent offer = latest();
		assertEquals("OFFER", offer.get("status")); assertEquals(1, offer.getInt("stone_count"));
		assertEquals(1, offer.getInt("can_accept")); final String token = offer.get("offer_token");
		action.onAction(player, command(offer, "close"));
		AffixRerollAction.open(player, npc); final RPEvent resumed = latest();
		assertEquals("open", resumed.get("phase")); assertEquals(token, resumed.get("offer_token"));
		assertNotEquals(offer.get("window_id"), resumed.get("window_id"));
		action.onAction(player, roll); assertEquals(1, ItemAffixRerollService.getInstance().stones(player));
		final RPAction accept = command(resumed, "accept"); accept.put("offer_token", token);
		action.onAction(player, accept); assertEquals("ACCEPTED", latest().get("status"));
		action.onAction(player, accept); assertEquals("INVALID_REQUEST", latest().get("status"));
		assertEquals(1, ItemAffixRerollService.getInstance().stones(player));
	}

	@Test public void remoteAndForgedNpcRequestsCannotConsumeAStone() {
		final RPEvent preview = quote(); final RPAction roll = command(preview, "roll");
		roll.put("affix_id", "accuracy_bonus"); roll.put("request_token", preview.get("request_token"));
		roll.put("npc_id", player.getID().getObjectID()); action.onAction(player, roll);
		assertEquals("NPC_TOO_FAR", latest().get("status"));
		assertEquals(2, ItemAffixRerollService.getInstance().stones(player));
		final RPEvent fresh = quote(); final RPAction remote = command(fresh, "roll");
		remote.put("affix_id", "accuracy_bonus"); remote.put("request_token", fresh.get("request_token"));
		player.setPosition(80, 80); action.onAction(player, remote);
		assertEquals("NPC_TOO_FAR", latest().get("status"));
		assertEquals(2, ItemAffixRerollService.getInstance().stones(player));
	}

	@Test public void everyRegisteredAffixHasAHumanLabelAndFixedPowersDoNotShowMarkerPercentages() {
		final List<ItemAffixDefinition> definitions = new ArrayList<>(ItemAffixRegistry.getInstance().getDefinitions());
		definitions.addAll(LegendaryItemAffixRegistry.getInstance().getDefinitions());
		for (final ItemAffixDefinition definition : definitions) {
			assertFalse(definition.getId(), AffixRerollEvent.label(player, definition.getId(), "1").startsWith("Właściwość przedmiotu"));
		}
		assertTrue(AffixRerollEvent.label(player, "legendary_tenacity", "1").startsWith("Zaciętość"));
		assertFalse(AffixRerollEvent.label(player, "legendary_deep_wounds", "1").contains("100%"));
		assertTrue(AffixRerollEvent.label(player, "accuracy_bonus", "7").contains("+7%"));
	}

	@Test public void privateServiceEventIsExcludedFromOtherPlayersPerceptions() throws Exception {
		quote();
		for (final DetailLevel level : new DetailLevel[] {DetailLevel.PRIVATE, DetailLevel.NORMAL}) {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			player.writeObject(new OutputSerializer(bytes), level);
			final RPObject restored = new RPObject();
			restored.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
			boolean found = false;
			for (final RPEvent event : restored.events()) if (Events.AFFIX_REROLL.equals(event.getName())) found = true;
			assertEquals(level == DetailLevel.PRIVATE, found);
		}
	}
}
