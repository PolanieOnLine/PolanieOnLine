package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusType;
import utilities.PlayerTestHelper;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;

public class CombatStatsServiceTest {
	private Player player;
	@Before public void createPlayer() {
		player = PlayerTestHelper.createPlayer("combat stats");
		player.setBaseHP(1000);
		player.setHP(1000);
	}
	@Test public void snapshotContainsEffectiveValuesAndCapsWithoutChangingCombat() {
		Item ring = equip("finger", "ring", "resist_bleeding", 0.35);
		ring.put("critical_chance", 80.0);
		player.updateItemAtkDef();
		assertTrue(player.hasMap("combat_stats"));
		assertNumber("resist_bleeding", 35);
		assertNumber("resist_bleeding.limit", 60);
		assertNumber("critical_chance", 50);
		assertNumber("critical_chance.limit", 50);
		assertEquals(0.35, EquipmentStatusResistanceService.getResistance(player,
				StatusType.BLEEDING), 0.000001);
	}
	@Test public void gearRemovalRefreshesInsteadOfLeavingOldBonuses() {
		equip("finger", "ring", "resist_bleeding", 0.35);
		player.updateItemAtkDef();
		assertNumber("resist_bleeding", 35);
		player.getSlot("finger").clear();
		player.updateItemAtkDef();
		assertNumber("resist_bleeding", 0);
		assertNumber("critical_chance", 10);
	}
	@Test public void resistanceCapReflectsIntrinsicResistanceToo() {
		player.put("resist_bleeding", 0.4);
		equip("finger", "ring", "resist_bleeding", 0.9);
		player.updateItemAtkDef();
		assertNumber("resist_bleeding", 76);
		assertNumber("resist_bleeding.limit", 76);
	}
	@Test public void legendaryResistanceIsIncludedButDoesNotBypassCurrentCap() {
		Item ring = equip("finger", "ring", "resist_bleeding", 0.5);
		ring.put(LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE, 1);
		equip("fingerb", "ring", "resist_bleeding", 0.5);
		player.updateItemAtkDef();
		assertNumber("resist_bleeding", 60);
		assertNumber("resist_bleeding.limit", 60);
	}
	@Test public void unchangedSnapshotsAreNotRepublishedAndConditionalParryRefreshes() {
		Item armor = equip("armor", "armor", LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE, 1.0);
		armor.put("def", 10);
		player.updateItemAtkDef();
		assertNumber("parry_chance", 0);
		assertFalse(CombatStatsService.update(player));
		player.setHP(200);
		assertTrue(CombatStatsService.update(player));
		assertNumber("parry_chance", 10);
		assertNumber("parry_chance.limit", 15);
		assertFalse(CombatStatsService.update(player));
	}
	@Test public void snapshotIsPrivateAndNeverSavedAsCharacterState() {
		Definition definition = player.getRPClass().getDefinition(DefinitionClass.ATTRIBUTE, "combat_stats");
		assertTrue(definition.isPrivate());
		assertFalse(definition.isVisible());
		assertFalse(definition.isStorable());
	}
	private Item equip(String slot, String itemClass, String attribute, double value) {
		Item item = new Item("stats " + slot, itemClass, "test", null);
		item.put(attribute, value);
		player.getSlot(slot).add(item);
		return item;
	}
	private void assertNumber(String key, double expected) {
		assertTrue("Missing stats map", player.hasMap("combat_stats"));
		assertEquals(key, expected, Double.parseDouble(player.get("combat_stats", key)), 0.000001);
	}
}
