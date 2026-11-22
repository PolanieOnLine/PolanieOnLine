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
import games.stendhal.server.maps.tarnow.enchanter.EnchanterNPC;
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
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
		final StendhalRPZone zone = new StendhalRPZone("reroll_action_test", 128, 128);
		MockStendlRPWorld.get().addRPZone(zone);
		new EnchanterNPC().configureZone(zone, Collections.emptyMap());
		npc = SingletonRepository.getNPCList().get(EnchanterNPC.NAME);
		player = PlayerTestHelper.createPlayer("przemir_customer"); player.setLanguage("pl");
		player.setPosition(8, 7); zone.add(player);
		item = SingletonRepository.getEntityManager().getItem("ciupaga"); item.setRarity(ItemRarity.RARE);
		final ItemAffixDefinition affix = ItemAffixRegistry.getInstance().get("accuracy_bonus");
		assertTrue(affix.apply(item, new Random(4))); ItemAffixState.record(item, affix);
		assertTrue(player.equipToInventoryOnly(item));
		final StackableItem stone = (StackableItem) SingletonRepository.getEntityManager().getItem("kamień przemiany");
		stone.setQuantity(2); assertTrue(player.equipToInventoryOnly(stone));
	}

	@After public void cleanup() {
		SingletonRepository.getNPCList().remove(EnchanterNPC.NAME);
		SingletonRepository.getNPCList().remove("Zoryk Runiczny");
	}

	private RPEvent latest() {
		RPEvent result = null;
		for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) result = event;
		assertNotNull("Przemir must send a calendar-independent service event", result);
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

	@Test public void openingIncludesPrivateAffixPreviewsForImmediateClientSelection() {
		AffixRerollAction.open(player, npc);
		final RPEvent event = latest();
		assertTrue(event.has("preview_paths"));
		final int index = event.getList("preview_paths").indexOf(ItemUpgradeEvent.encodePath(item));
		assertTrue(index >= 0);
		assertEquals("accuracy_bonus", event.getList("preview_affix_ids").get(index));
		assertTrue(event.getList("preview_affix_labels").get(index).contains("Celność:"));
		assertEquals(event.getList("candidate_paths").size(), event.getList("candidate_locked_affixes").size());
	}

	@Test public void afterUnlockingTheServerPublishesTheMaterialPriceAndItsActualAvailability() {
		final RPEvent preview = quote();
		final RPAction roll = command(preview, "roll");
		roll.put("affix_id", "accuracy_bonus"); roll.put("request_token", preview.get("request_token"));
		action.onAction(player, roll);
		assertEquals("stones", latest().get("payment_mode"));
		assertEquals(Collections.singletonList("kamień przemiany"), latest().getList("cost_names"));
		final RPAction keep = command(latest(), "keep"); keep.put("offer_token", latest().get("offer_token"));
		action.onAction(player, keep);
		AffixRerollAction.open(player, npc);
		final RPAction select = command(latest(), "select"); select.put("affix_id", "accuracy_bonus");
		action.onAction(player, select);
		final RPEvent next = latest();
		assertEquals("MISSING_MATERIALS", next.get("status"));
		assertEquals("materials", next.get("payment_mode"));
		assertEquals(0, next.getInt("stone_cost"));
		assertEquals(0, next.getInt("can_roll"));
		assertEquals(java.util.Arrays.asList("sztabka srebra", "kieł tygrysi", "monety"), next.getList("cost_names"));
		assertEquals(java.util.Arrays.asList("5", "5", "20000"), next.getList("cost_amounts"));
		assertEquals(3, next.getList("cost_owned").size());
	}

	@Test public void refreshKeepsTheSelectedItemAndAffixAfterAnInventoryMove() {
		final RPEvent selected = quote();
		final RPAction refresh = command(selected, "refresh");
		refresh.put("affix_id", "accuracy_bonus");
		player.getSlot("bag").remove(item.getID());
		player.getSlot("rhand").add(item);
		action.onAction(player, refresh);
		assertEquals("READY", latest().get("status"));
		assertEquals(ItemUpgradeEvent.encodePath(item), latest().get("selected_path"));
		assertEquals("accuracy_bonus", latest().get("selected_affix"));
		assertFalse(latest().get("request_token").isEmpty());
	}

	@Test public void percentageLabelsDoNotExposeFloatingPointTails() {
		assertEquals("Odporność na zatrucie: +7%", AffixRerollEvent.label(player, "resist_poisoned", "0.07000000000000001"));
		assertTrue(AffixRerollEvent.label(player, "critical_damage_bonus", "0.10000000000000002").endsWith("+10%"));
		assertTrue(AffixRerollEvent.label(player, "resist_poisoned", "0.075").endsWith("+7.5%"));
		assertTrue(AffixRerollEvent.label(player, "accuracy_bonus", "7.5").endsWith("+7.5%"));
		assertFalse(AffixRerollEvent.label(player, "flat_attack_bonus", "3").contains("%"));
	}

	@Test public void refreshDropsTheSelectionWhenTheItemIsNoLongerCarried() {
		final RPAction refresh = command(quote(), "refresh");
		player.getSlot("bag").remove(item.getID());
		action.onAction(player, refresh);
		assertEquals("SELECT_ITEM", latest().get("status"));
		assertEquals("", latest().get("selected_path"));
		assertEquals("", latest().get("request_token"));
	}

	@Test public void refreshAfterAPaidRollResumesTheSameOfferWithoutChargingAgain() {
		final RPEvent selected = quote();
		final RPAction roll = command(selected, "roll");
		roll.put("affix_id", "accuracy_bonus");
		roll.put("request_token", selected.get("request_token"));
		action.onAction(player, roll);
		assertEquals("OFFER", latest().get("status"));
		final String offer = latest().get("offer_token");
		final int remaining = latest().getInt("stone_count");
		action.onAction(player, command(latest(), "refresh"));
		assertEquals("OFFER", latest().get("status"));
		assertEquals(offer, latest().get("offer_token"));
		assertEquals(remaining, latest().getInt("stone_count"));
	}

	@Test public void glyphMasterCannotOpenTheAffixService() {
		final StendhalRPZone zone = player.getZone();
		SingletonRepository.getNPCList().remove("Zoryk Runiczny");
		new RuneMasterNPC().configureZone(zone, Collections.emptyMap());
		final SpeakerNPC glyphMaster = SingletonRepository.getNPCList().get("Zoryk Runiczny");
		player.setPosition(60, 17);
		int before = 0;
		for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) before++;
		AffixRerollAction.open(player, glyphMaster);
		glyphMaster.getEngine().stepTest(player, "hi"); utilities.SpeakerNPCTestHelper.getReply(glyphMaster);
		glyphMaster.getEngine().stepTest(player, "przemiana");
		assertTrue(utilities.SpeakerNPCTestHelper.getReply(glyphMaster).contains("Przemir Zaklinacz"));
		int after = 0;
		for (final RPEvent event : player.events()) if (Events.AFFIX_REROLL.equals(event.getName())) after++;
		assertEquals(before, after);
	}

	@Test public void przemirUnderstandsTheVisibleDialogueKeywordAndItsInflection() {
		for (final String[] choice : new String[][] {{"pl", "przemiana"}, {"pl", "przemianę"}, {"en", "transformation"}, {"es", "transformación"}}) {
			player.setLanguage(choice[0]);
			npc.getEngine().stepTest(player, "hi"); npc.getEngine().stepTest(player, choice[1]);
			assertEquals("open", latest().get("phase"));
			assertEquals(npc.getID().getObjectID(), latest().getInt("npc_id"));
			npc.getEngine().stepTest(player, "bye");
		}
	}

	@Test public void enchanterExplainsPaymentAndThePermanentChoiceInPlainLanguage() {
		npc.getEngine().stepTest(player, "hi"); utilities.SpeakerNPCTestHelper.getReply(npc);
		npc.getEngine().stepTest(player, "kamień");
		final String stones = utilities.SpeakerNPCTestHelper.getReply(npc);
		assertTrue(stones, stones.contains("jeden kamień") && stones.contains("za trzy"));
		npc.getEngine().stepTest(player, "zasady");
		final String rules = utilities.SpeakerNPCTestHelper.getReply(npc);
		assertTrue(rules, rules.contains("tylko ta jedna właściwość"));
		assertTrue(rules, rules.contains("kamienie przepadną w obu przypadkach"));
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
			final String label = AffixRerollEvent.label(player, definition.getId(), "1");
			assertFalse(definition.getId(), label.startsWith("Właściwość przedmiotu"));
			assertFalse(definition.getId(), label.contains("\u2014") || label.contains("\u2013"));
		}
		assertTrue(AffixRerollEvent.label(player, "legendary_tenacity", "1").startsWith("Zaciętość"));
		assertFalse(AffixRerollEvent.label(player, "legendary_deep_wounds", "1").contains("100%"));
		assertTrue(AffixRerollEvent.label(player, "accuracy_bonus", "7").contains("+7%"));
	}

	@Test public void ordinaryCombatAffixesExplainTheirEffectAndKeepTheActualRolledValue() {
		for (final String[] effect : new String[][] {
			{"steady_hand", "minimalne obrażenia"}, {"poison_potency", "obrażenia trucizny"},
			{"bleed_potency", "obrażenia krwawienia"}, {"vigilance", "Pierwsze trafienie"},
			{"ammo_conservation", "nie zużyje amunicji"}, {"armor_crushing", "opancerzeniu przeciwnika"},
			{"critical_hardening", "otrzymywanych obrażeń krytycznych"}, {"spiked_plating", "ciosów wręcz"}
		}) {
			final String label = AffixRerollEvent.label(player, effect[0], "0.075");
			assertTrue(label, label.contains(effect[1]));
			assertTrue(label, label.contains("7.5%"));
			assertFalse(label, label.contains("{0}") || label.contains("{number"));
		}
		player.setLanguage("en");
		assertTrue(AffixRerollEvent.label(player, "steady_hand", "0.075").contains("minimum weapon damage by 7.5%"));
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
