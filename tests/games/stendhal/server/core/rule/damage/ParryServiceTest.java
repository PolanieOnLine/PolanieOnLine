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
		final Player player = createMock(Player.class);
		expect(player.getWeapons()).andReturn(Collections.<Item>emptyList());
		replay(player);

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
		replay(player);
		return player;
	}

	private Item parryWeapon(final String itemClass, final double chance) {
		final Item weapon = new Item("parry test weapon", itemClass, "test", null);
		weapon.put(ParryService.PARRY_CHANCE_ATTRIBUTE, chance);
		return weapon;
	}
}
