/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.HeavyStatus;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class LegendaryWeaponSignatureCombatTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void firstSalvoAddsThirtyPercentAtEightyPercentHealthOrHigher() {
		final Item weapon = weapon("ranged", 20);
		weapon.put(WeaponAffixCombatService.LEGENDARY_FIRST_SALVO_ATTRIBUTE, 1.0);
		final Player target = target(100, 80);

		assertEquals(130, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, true));
		target.setHP(79);
		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, true));
	}

	@Test
	public void firstSalvoRequiresActualRangedAttack() {
		final Item weapon = weapon("ranged", 20);
		weapon.put(WeaponAffixCombatService.LEGENDARY_FIRST_SALVO_ATTRIBUTE, 1.0);
		final Player target = target(100, 100);

		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, false));
	}

	@Test
	public void crushingBlowRecognizesMediumAndHeavySemanticArmor() {
		final Creature medium = creatureWithArmor("medium");
		final Creature heavy = creatureWithArmor("heavy");
		final Creature light = creatureWithArmor("light");

		assertTrue(WeaponAffixCombatService.isCrushingBlowActive(medium));
		assertTrue(WeaponAffixCombatService.isCrushingBlowActive(heavy));
		assertFalse(WeaponAffixCombatService.isCrushingBlowActive(light));
		verify(medium, heavy, light);
	}

	@Test
	public void powerOverloadUsesExactFifteenPercentBoundary() {
		final Item wand = weapon("wand", 20);
		wand.put(WeaponAffixCombatService.LEGENDARY_POWER_OVERLOAD_ATTRIBUTE, 1.0);

		assertTrue(WeaponAffixCombatService.isPowerOverloadSuccessful(
				Arrays.asList(wand), true, 1500));
		assertFalse(WeaponAffixCombatService.isPowerOverloadSuccessful(
				Arrays.asList(wand), true, 1501));
		assertFalse(WeaponAffixCombatService.isPowerOverloadSuccessful(
				Arrays.asList(wand), false, 1));
	}

	@Test
	public void arcaneFocusAddsTwentyFivePercentAgainstActiveCombatStatus() {
		final Item wand = weapon("wand", 20);
		wand.put(WeaponAffixCombatService.LEGENDARY_ARCANE_FOCUS_ATTRIBUTE, 1.0);
		final Player target = target(100, 100);
		target.getStatusList().getStatuses().add(new HeavyStatus(10));

		assertTrue(WeaponAffixCombatService.hasCombatStatus(target));
		assertEquals(125, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(wand), target, true));
		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(wand), target, false));
	}

	@Test
	public void stunningAndBindingHeavySourcesShareGlobalProcCap() {
		final Item club = weapon("club", 20);
		final Item whip = weapon("whip", 20);
		club.put(WeaponAffixCombatService.LEGENDARY_STUNNING_FORCE_ATTRIBUTE, 1.0);
		whip.put(WeaponAffixCombatService.LEGENDARY_BINDING_STRIKE_ATTRIBUTE, 1.0);

		final double stunning = WeaponAffixCombatService.combinedFixedProcChance(
				Arrays.asList(club, whip),
				WeaponAffixCombatService.LEGENDARY_STUNNING_FORCE_ATTRIBUTE, 0.15);
		final double binding = WeaponAffixCombatService.combinedFixedProcChance(
				Arrays.asList(club, whip),
				WeaponAffixCombatService.LEGENDARY_BINDING_STRIKE_ATTRIBUTE, 0.15);
		final double combined = Math.min(0.25,
				1.0 - ((1.0 - stunning) * (1.0 - binding)));

		assertEquals(0.25, combined, 0.0000001);
	}

	private Creature creatureWithArmor(final String armorType) {
		final Creature creature = createMock(Creature.class);
		expect(creature.getArmorType()).andReturn(armorType);
		replay(creature);
		return creature;
	}

	private Player target(final int baseHp, final int hp) {
		final Player target = PlayerTestHelper.createPlayer("legendary target");
		target.setBaseHP(baseHp);
		target.setHP(hp);
		return target;
	}

	private Item weapon(final String itemClass, final int attack) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		return new Item("legendary combat weapon", itemClass, "test", attributes);
	}
}
