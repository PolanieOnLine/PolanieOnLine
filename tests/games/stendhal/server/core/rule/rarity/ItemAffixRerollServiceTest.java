/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.StatusResistantItem;
import games.stendhal.server.entity.status.StatusType;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixRerollServiceTest {
	@BeforeClass public static void setup() { MockStendlRPWorld.get(); ItemTestHelper.generateRPClasses(); }
	private ItemAffixRerollService service() { return new ItemAffixRerollService(new Random(42)); }
	private Player player() { return PlayerTestHelper.createPlayer("reroller"); }
	private Item sword() {
		final Item item = new Item("test reroll", "sword", "test", Collections.singletonMap("atk", "12"));
		item.setEquipableSlots(Arrays.asList("bag", "rhand")); item.setRarity(ItemRarity.LEGENDARY);
		item.put("max_improves", 3); item.setUpgradeLevel(2);
		item.put("damage_min", 10); item.put("damage_max", 14); item.put("rate", 5);
		item.put("bound", "reroller"); item.setRarityModifier("atk", 1.25);
		apply(item, "accuracy_bonus"); apply(item, "critical_chance"); apply(item, "legendary_deep_wounds");
		return item;
	}
	private void apply(final Item item, final String id) {
		final ItemAffixDefinition def = ItemAffixRerollService.definition(id);
		assertTrue(def.apply(item, new Random(2))); ItemAffixState.record(item, def);
	}
	private void stones(final Player player) {
		final StackableItem stone = new StackableItem(ItemAffixRerollService.STONE, "resource", "transformation_stone", null);
		stone.setEquipableSlots(Collections.singletonList("bag")); stone.setQuantity(3); player.equipToInventoryOnly(stone);
	}
	@Test public void legendaryRollRequiresThreeStonesAndChargesOnlyOnce() {
		final Player p = player(); final Item item = sword(); p.equipToInventoryOnly(item); stones(p);
		StackableItem stack = null;
		for (final marauroa.common.game.RPObject object : p.getSlot("bag")) {
			if (object instanceof StackableItem) stack = (StackableItem) object;
		}
		assertNotNull(stack); stack.setQuantity(2);
		final ItemAffixRerollService s = service(); final String quote = s.select(p, item, "legendary_deep_wounds");
		assertEquals("MISSING_STONE", s.roll(p, item, "legendary_deep_wounds", quote));
		assertEquals(2, s.stones(p)); assertFalse(s.hasPending(item));
		stack.setQuantity(3);
		assertEquals("OFFER", s.roll(p, item, "legendary_deep_wounds", quote));
		assertEquals(0, s.stones(p));
		assertEquals("OFFER", s.roll(p, item, "legendary_deep_wounds", quote));
		assertEquals(0, s.stones(p));
		assertEquals("legendary_deep_wounds", item.getMap("tooltip_stats").get("rerolled_affix"));
		assertEquals("KEPT", s.finish(p, item, ItemAffixRerollService.state(item).get("token"), false));
		assertEquals("legendary_deep_wounds", item.getMap("tooltip_stats").get("rerolled_affix"));
	}

	@Test public void legendaryPaymentCanUseSeveralStoneStacks() {
		final Player p = player(); final Item item = sword(); p.equipToInventoryOnly(item);
		for (int i = 0; i < 3; i++) {
			final StackableItem stone = new StackableItem(ItemAffixRerollService.STONE, "resource", "transformation_stone", null);
			stone.setQuantity(1); p.getSlot("bag").add(stone);
		}
		final ItemAffixRerollService s = service();
		assertEquals("OFFER", s.roll(p, item, "legendary_deep_wounds", s.select(p, item, "legendary_deep_wounds")));
		assertEquals(0, s.stones(p));
	}

	@Test public void onlyTheBoundOwnerCanSpendTransformationStones() {
		final Player player = player();
		final Item item = sword();
		player.equipToInventoryOnly(item);
		final StackableItem foreign = new StackableItem(ItemAffixRerollService.STONE, "resource", "transformation_stone", null);
		foreign.setQuantity(3);
		foreign.setBoundTo("other_character");
		player.getSlot("bag").add(foreign);
		final ItemAffixRerollService service = service();
		final String quote = service.select(player, item, "legendary_deep_wounds");
		assertEquals(0, service.stones(player));
		assertEquals("MISSING_STONE", service.roll(player, item, "legendary_deep_wounds", quote));
		final StackableItem own = new StackableItem(ItemAffixRerollService.STONE, "resource", "transformation_stone", null);
		own.setQuantity(3);
		own.setBoundTo(player.getName());
		player.getSlot("bag").add(own);
		assertEquals(3, service.stones(player));
		assertEquals("OFFER", service.roll(player, item, "legendary_deep_wounds", quote));
		assertEquals(0, service.stones(player));
		assertEquals(3, foreign.getQuantity());
	}

	@Test public void paidOfferKeepsOldStatsUntilAcceptedAndCannotBeReplayed() {
		final Player player = player(); final Item item = sword(); player.equipToInventoryOnly(item); stones(player);
		final ItemAffixRerollService service = service(); final Map<String,String> before = ItemAffixState.getValues(item);
		final String token = service.select(player, item, "accuracy_bonus"); assertFalse(token.isEmpty());
		assertEquals("OFFER", service.roll(player,item,"accuracy_bonus",token)); assertEquals(2,service.stones(player));
		assertEquals(before,ItemAffixState.getValues(item)); assertTrue(service.validPending(player,item));
		final String offer = ItemAffixRerollService.state(item).get("token");
		assertEquals("OFFER",service.roll(player,item,"accuracy_bonus",token)); assertEquals(2,service.stones(player));
		assertEquals("ACCEPTED",service.finish(player,item,offer,true));
		assertEquals("INVALID_REQUEST",service.finish(player,item,offer,true)); assertEquals(2,service.stones(player));
		assertEquals(before.get("critical_chance"),ItemAffixState.getValues(item).get("critical_chance"));
		assertEquals(before.get("legendary_deep_wounds"),ItemAffixState.getValues(item).get("legendary_deep_wounds"));
		assertEquals(12,item.getInt("atk")); assertEquals(2,item.getUpgradeLevel()); assertEquals("reroller",item.get("bound"));
		assertEquals(ItemRarity.LEGENDARY,item.getRarity()); assertEquals(Double.valueOf(1.25),item.getRarityModifiers().get("atk"));
		assertEquals(ItemAffixRerollService.state(item).get("locked"), item.getMap("tooltip_stats").get("rerolled_affix"));
		assertEquals(3,ItemAffixState.getValues(item).size()); assertEquals(1,service.affixes(item).size());
	}
	@Test public void wrongTokenOrChangedItemCostsNothing() {
		final Player player=player(); final Item item=sword(); player.equipToInventoryOnly(item); stones(player);
		final ItemAffixRerollService s=service(); String token=s.select(player,item,"accuracy_bonus");
		assertEquals("INVALID_REQUEST",s.roll(player,item,"accuracy_bonus","wrong")); assertEquals(3,s.stones(player));
		item.put("atk",99); assertEquals("INVALID_REQUEST",s.roll(player,item,"accuracy_bonus",token)); assertEquals(3,s.stones(player));
	}
	@Test public void keepChargesOnceAndLocksTheOriginalSlot() {
		final Player p=player(); final Item item=sword(); p.equipToInventoryOnly(item); stones(p); final ItemAffixRerollService s=service();
		String token=s.select(p,item,"accuracy_bonus"); s.roll(p,item,"accuracy_bonus",token);
		assertEquals("KEPT",s.finish(p,item,ItemAffixRerollService.state(item).get("token"),false));
		assertEquals(Collections.singletonList("accuracy_bonus"),s.affixes(item)); assertEquals(2,s.stones(p));
		assertEquals("",s.select(p,item,"critical_chance"));
	}
	@Test public void savedOfferSurvivesCopyAndServiceRestart() {
		final Player p=player(); final Item item=sword(); p.equipToInventoryOnly(item); stones(p); final ItemAffixRerollService s=service();
		s.roll(p,item,"accuracy_bonus",s.select(p,item,"accuracy_bonus"));
		final String token=ItemAffixRerollService.state(item).get("token");
		final Item restored=new Item(item); item.getContainerSlot().remove(item.getID()); p.equipToInventoryOnly(restored);
		final ItemAffixRerollService restarted=service(); assertTrue(restarted.validPending(p,restored));
		assertEquals("ACCEPTED",restarted.finish(p,restored,token,true)); assertEquals(2,restarted.stones(p));
	}
	@Test public void changedPaidOfferCannotApplyButCanBeKept() {
		final Player p=player(); final Item item=sword(); p.equipToInventoryOnly(item); stones(p); final ItemAffixRerollService s=service();
		s.roll(p,item,"accuracy_bonus",s.select(p,item,"accuracy_bonus")); final String token=ItemAffixRerollService.state(item).get("token");
		item.put("atk",55); assertEquals("CHANGED_ITEM",s.finish(p,item,token,true)); assertEquals(55,item.getInt("atk"));
		assertEquals("KEPT",s.finish(p,item,token,false));
	}
	@Test public void removingFlatAttackRestoresOwnAttackAndLifestealRestoresOwnValue() {
		final Item ring=new Item("ring","ring","test",Collections.singletonMap("atk","4")); apply(ring,"flat_attack_bonus");
		ItemAffixRerollService.remove(ring,"flat_attack_bonus"); assertEquals(4,ring.getInt("atk")); assertFalse(ring.has("flat_attack_bonus"));
		final Item life=new Item("life","sword","test",Collections.singletonMap("lifesteal","0.03"));
		life.put("lifesteal",0.08); life.put(ItemAffixState.ATTRIBUTE,"lifesteal","0.05");
		ItemAffixRerollService.remove(life,"lifesteal"); assertEquals(0.03,life.getDouble("lifesteal"),0.000001);
	}
	@Test public void signatureAndMapOnlySlotsStayInTheirOwnPool() {
		final Item item=sword(); final ItemAffixRerollService s=service();
		for(int i=0;i<100;i++) {
			final Item rerolled=s.replacement(item,"legendary_deep_wounds");
			assertEquals(""+i,3,ItemAffixState.getValues(rerolled).size());
			int signatures=0; for(String id:ItemAffixState.getValues(rerolled).keySet()) if(LegendaryItemAffixRegistry.getInstance().get(id)!=null) signatures++;
			assertEquals(1,signatures);
		}
		apply(item,"steady_hand"); final Item replaced=s.replacement(item,"steady_hand"); assertEquals(4,ItemAffixState.getValues(replaced).size());
		assertFalse(replaced.has("steady_hand"));
	}
	@Test public void boundEquipmentCannotBeChangedByAnotherCharacter() {
		final Player other = PlayerTestHelper.createPlayer("other_reroller");
		final Item item = sword(); other.equipToInventoryOnly(item); stones(other);
		final ItemAffixRerollService s = service();
		assertEquals("", s.select(other, item, "accuracy_bonus"));
		assertEquals("INVALID_ITEM", s.roll(other, item, "accuracy_bonus", "anything"));
		assertEquals(3, s.stones(other));
	}
	@Test public void transferredPendingOfferCanOnlyBeDiscardedByItsNewOwner() {
		final Player first = player(); final Item item = sword(); item.remove("bound");
		first.equipToInventoryOnly(item); stones(first); final ItemAffixRerollService s = service();
		s.roll(first, item, "accuracy_bonus", s.select(first, item, "accuracy_bonus"));
		final String token = ItemAffixRerollService.state(item).get("token");
		final Map<String, String> original = ItemAffixState.getValues(item);
		final Player recipient = PlayerTestHelper.createPlayer("recipient_reroller");
		item.getContainerSlot().remove(item.getID()); recipient.equipToInventoryOnly(item);
		assertSame(item, s.pending(recipient));
		assertFalse(s.validPending(recipient, item));
		assertEquals("CHANGED_ITEM", s.finish(recipient, item, token, true));
		assertEquals("KEPT", s.finish(recipient, item, token, false));
		assertEquals(original, ItemAffixState.getValues(item));
		assertFalse(s.hasPending(item));
		assertEquals(Collections.singletonList("accuracy_bonus"), s.affixes(item));
	}
	@Test public void nativeStatusResistanceRemainsIneligibleDuringRerolls() {
		final StatusResistantItem ring = new StatusResistantItem("native resistance ring", "ring", "test", Collections.singletonMap("atk", "2"));
		ring.initializeStatusResistancesList(Collections.singletonMap(StatusType.POISONED, 0.5));
		apply(ring, "flat_attack_bonus"); final ItemAffixRerollService s = service();
		for (int attempt = 0; attempt < 150; attempt++) {
			final Item replacement = s.replacement(ring, "flat_attack_bonus");
			assertFalse("Authored resistance must stay outside the random pool", ItemAffixState.has(replacement, "resist_poisoned"));
		}
	}
	@Test public void lockAndPaidMetadataSurviveSnapshotAndTransferCancelsPending() {
		final Player p=player(); final Item item=sword(); p.equipToInventoryOnly(item); stones(p); final ItemAffixRerollService s=service();
		s.roll(p,item,"accuracy_bonus",s.select(p,item,"accuracy_bonus"));
		final Item saved=sword(); ItemRaritySnapshot.restore(saved,ItemRaritySnapshot.encode(item));
		assertEquals(ItemAffixRerollService.state(item),ItemAffixRerollService.state(saved));
		final Item target=new Item("new weapon","sword","test",Collections.singletonMap("atk","20"));
		target.setEquipableSlots(Arrays.asList("bag","rhand"));
		ItemRarityTransferSnapshot.apply(target,ItemRarityTransferSnapshot.encode(item));
		assertEquals("accuracy_bonus",ItemAffixRerollService.state(target).get("locked")); assertFalse(s.hasPending(target));
	}
	@Test public void oldTransferAndQuestSnapshotsStillRestoreWithoutRerollMetadata() {
		final Item source = sword();
		final byte[] transfer = java.util.Base64.getUrlDecoder().decode(ItemRarityTransferSnapshot.encode(source));
		transfer[0] = 1;
		final String legacyTransfer = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(transfer, transfer.length - 2));
		final Item target = new Item("legacy upgraded sword", "sword", "test", Collections.singletonMap("atk", "20"));
		ItemRarityTransferSnapshot.apply(target, legacyTransfer);
		assertEquals(ItemAffixState.getValues(source), ItemAffixState.getValues(target));
		assertTrue(ItemAffixRerollService.state(target).isEmpty());
		final byte[] quest = java.util.Base64.getUrlDecoder().decode(ItemRaritySnapshot.encode(source));
		quest[0] = 3;
		final String legacyQuest = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(quest, quest.length - 4));
		final Item restored = sword();
		ItemRaritySnapshot.restore(restored, legacyQuest);
		assertEquals(ItemAffixState.getValues(source), ItemAffixState.getValues(restored));
		assertTrue(ItemAffixRerollService.state(restored).isEmpty());
	}
}
