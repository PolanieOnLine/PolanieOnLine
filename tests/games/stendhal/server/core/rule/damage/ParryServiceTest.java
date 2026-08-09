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
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import utilities.RPClass.ItemTestHelper;

public class ParryServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void noWeaponsMeansNoParryChance() {
		final Player player = playerWith();

		assertEquals(0.0, ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void oneWeaponUsesItsExactParryChanceBelowCap() {
		final Item weapon = parryWeapon("sword", 0.10);
		final Player player = playerWith(weapon);

		assertEquals(0.10, ParryService.getParryChance(player), 0.0000001);
		verify(player);
	}

	@Test
	public void pairedWeaponsCombineIndependentParryChancesBelowCap() {
		final Item first = parryWeapon("sword", 0.05);
		final Item second = parryWeapon("sword", 0.05);
		final Player player = playerWith(first, second);

		assertEquals(0.0975, ParryService.getParryChance(player), 0.0000001);
		verify(player);
	}

	@Test
	public void pairedWeaponsCannotExceedFinalFifteenPercentCap() {
		final Item first = parryWeapon("sword", 0.10);
		final Item second = parryWeapon("sword", 0.10);
		final Player player = playerWith(first, second);

		assertEquals(ParryService.MAX_PARRY_CHANCE,
				ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void singleOversizedParryValueIsCapped() {
		final Item weapon = parryWeapon("sword", 0.80);
		final Player player = playerWith(weapon);

		assertEquals(ParryService.MAX_PARRY_CHANCE,
				ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void rangedWeaponsDoNotProvideParry() {
		final Item weapon = parryWeapon("ranged", 0.15);
		final Player player = playerWith(weapon);

		assertEquals(0.0, ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void duelMasterAddsFivePointsWithoutRegularParryAffix() {
		final Item sword = new Item("duel master sword", "sword", "test", null);
		sword.put(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE, 1.0);
		final Player player = playerWith(sword);

		assertEquals(0.05, ParryService.getParryChance(player), 0.0000001);
		verify(player);
	}

	@Test
	public void duelMasterAndRegularParryShareFinalCap() {
		final Item sword = parryWeapon("sword", 0.12);
		sword.put(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE, 1.0);
		final Player player = playerWith(sword);

		assertEquals(ParryService.MAX_PARRY_CHANCE,
				ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void duelMasterSuccessfulParryArmsExactlyOneRiposte() {
		final Item sword = duelMasterSword("first duel master");
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Arrays.asList(sword));
		replay(player);

		ParryService.markDuelMasterRiposte(player);

		verify(player);
		assertTrue(ParryService.consumeDuelMasterRiposte(Arrays.asList(sword)));
		assertFalse(ParryService.consumeDuelMasterRiposte(Arrays.asList(sword)));
	}

	@Test
	public void oneParryDoesNotArmTwoRipostesWhenDualWielding() {
		final Item first = duelMasterSword("first duel master");
		final Item second = duelMasterSword("second duel master");
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Arrays.asList(first, second));
		replay(player);

		ParryService.markDuelMasterRiposte(player);

		verify(player);
		assertTrue(ParryService.consumeDuelMasterRiposte(
				Arrays.asList(first, second)));
		assertFalse(ParryService.consumeDuelMasterRiposte(
				Arrays.asList(first, second)));
	}

	@Test
	public void unyieldingProtectionAddsEmergencyParryBelowThirtyPercent() {
		final Item armour = new Item("unyielding armor", "armor", "test", null);
		armour.put(LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE,
				1.0);
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Collections.<Item>emptyList());
		expect(player.getBaseHP()).andReturn(100);
		expect(player.getHP()).andReturn(29);
		expect(player.getDefenseItems()).andReturn(Arrays.asList(armour));
		replay(player);

		assertEquals(0.10, ParryService.getParryChance(player), 0.0000001);
		verify(player);
	}

	@Test
	public void unyieldingProtectionIsInactiveAtThirtyPercent() {
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Collections.<Item>emptyList());
		expect(player.getBaseHP()).andReturn(100);
		expect(player.getHP()).andReturn(30);
		replay(player);

		assertEquals(0.0, ParryService.getParryChance(player), 0.0);
		verify(player);
	}

	@Test
	public void zeroPercentNeverParriesAndChanceIsCappedAtFifteenPercent() {
		assertFalse(ParryService.isParrySuccessful(0.0, 0.0));
		assertFalse(ParryService.isParrySuccessful(0.0, 0.999999));
		assertTrue(ParryService.isParrySuccessful(1.0, 0.149999));
		assertFalse(ParryService.isParrySuccessful(1.0, 0.15));
	}

	@Test
	public void tenPercentUsesDirectPercentageSemantics() {
		assertTrue(ParryService.isParrySuccessful(0.10, 0.099999));
		assertFalse(ParryService.isParrySuccessful(0.10, 0.10));
	}

	private Player playerWith(final Item... weapons) {
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Arrays.asList(weapons));
		expect(player.getBaseHP()).andReturn(100);
		expect(player.getHP()).andReturn(100);
		replay(player);
		return player;
	}

	private Item duelMasterSword(final String name) {
		final Item sword = new Item(name, "sword", "test", null);
		sword.put(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE, 1.0);
		return sword;
	}

	private Item parryWeapon(final String itemClass, final double chance) {
		final Item weapon = new Item("parry test weapon", itemClass, "test", null);
		weapon.put(ParryService.PARRY_CHANCE_ATTRIBUTE, chance);
		return weapon;
	}
}
