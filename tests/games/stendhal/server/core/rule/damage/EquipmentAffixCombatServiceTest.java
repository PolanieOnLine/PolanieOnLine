/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.EquipmentAffixService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class EquipmentAffixCombatServiceTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
		CreatureTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Before
	public void clearMarks() {
		EquipmentAffixCombatService.clearMarksForTests();
	}

	@Test
	public void giantSlayerUsesFiftyLevelStepsAndTenPercentCap() {
		assertEquals(0.0, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 399), 0.0);
		assertEquals(0.01, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 400), 0.0000001);
		assertEquals(0.03, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 500), 0.0000001);
		assertEquals(0.05, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 600), 0.0000001);
		assertEquals(0.10, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 850), 0.0000001);
		assertEquals(0.10, EquipmentAffixCombatService.giantSlayerPenetration(
				350, 1000), 0.0000001);
	}

	@Test
	public void spikedPlatingReflectionUsesActualDamageAndTenPercentCap() {
		assertEquals(20, EquipmentAffixCombatService.reflectedDamage(1000, 0.02));
		assertEquals(80, EquipmentAffixCombatService.reflectedDamage(1000, 0.08));
		assertEquals(100, EquipmentAffixCombatService.reflectedDamage(1000, 0.15));
		assertEquals(0, EquipmentAffixCombatService.reflectedDamage(0, 0.10));
		assertEquals(0, EquipmentAffixCombatService.reflectedDamage(1000, -0.10));
	}

	@Test
	public void hunterMarkIsTargetSpecificAndExpires() {
		final Player player = player("hunter");
		final Creature marked = creature(500);
		final Creature other = creature(500);
		final long now = 1000L;

		EquipmentAffixCombatService.mark(player, marked, now);
		assertEquals(true, EquipmentAffixCombatService.isMarked(
				player, marked, now + 1L));
		assertEquals(false, EquipmentAffixCombatService.isMarked(
				player, other, now + 1L));
		assertEquals(true, EquipmentAffixCombatService.isMarked(
				player, marked, now + 5999L));
		assertEquals(false, EquipmentAffixCombatService.isMarked(
				player, marked, now + 6001L));
	}

	@Test
	public void equippedWeaponKeepsPlayerContainerForTacticalPenetration() {
		final Player player = player("giant-hunter");
		player.setLevel(350);
		final Weapon dagger = weapon("dagger");
		player.getSlot("rhand").add(dagger);
		final Item armour = item("armor", 20);
		armour.put(EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE, 1.0);
		player.getSlot("armor").add(armour);

		final Creature defender = creature(500);
		defender.setArmorType("heavy");

		assertSame(player, dagger.getContainer());
		// Dagger vs heavy is 0.40. Three percent Giant Slayer penetration moves
		// it to 0.418, so a 100 point resolved hit becomes 42 after rounding.
		assertEquals(42, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(dagger), defender));
	}

	@Test
	public void hunterMarkAndGiantSlayerDoNotStackAcrossDuplicatePieces() {
		final Player player = player("marked-hunter");
		player.setLevel(350);
		final Weapon dagger = weapon("dagger");
		player.getSlot("rhand").add(dagger);

		final Item armour = item("armor", 20);
		armour.put(EquipmentAffixService.HUNTER_MARK_ATTRIBUTE, 1.0);
		armour.put(EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE, 1.0);
		player.getSlot("armor").add(armour);
		final Item shield = item("shield", 20);
		shield.put(EquipmentAffixService.HUNTER_MARK_ATTRIBUTE, 1.0);
		shield.put(EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE, 1.0);
		player.getSlot("lhand").add(shield);

		final Creature defender = creature(500);
		defender.setArmorType("heavy");
		EquipmentAffixCombatService.mark(player, defender, System.currentTimeMillis());

		// Hunter's Mark 5% and Giant Slayer 3% combine independently to 7.85%.
		// 0.40 + 0.60 * 0.0785 = 0.4471 -> 45 damage after rounding.
		assertEquals(45, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(dagger), defender));
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		PlayerTestHelper.addEmptySlots(player);
		return player;
	}

	private Creature creature(final int level) {
		final Creature creature = new Creature();
		creature.setBaseHP(1000);
		creature.setHP(1000);
		creature.setLevel(level);
		return creature;
	}

	private Item item(final String itemClass, final int defense) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", Integer.toString(defense));
		return new Item("tactical armour", itemClass, "test", attributes);
	}

	private Weapon weapon(final String itemClass) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		return new Weapon("tactical weapon", itemClass, "test", attributes);
	}
}
