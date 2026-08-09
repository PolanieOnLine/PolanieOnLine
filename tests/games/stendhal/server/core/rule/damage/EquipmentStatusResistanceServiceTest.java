/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusType;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class EquipmentStatusResistanceServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void resistanceAttributeUsesExistingStatusConvention() {
		assertEquals("resist_poisoned",
				EquipmentStatusResistanceService.getResistanceAttribute(
						StatusType.POISONED));
		assertEquals("resist_bleeding",
				EquipmentStatusResistanceService.getResistanceAttribute(
						StatusType.BLEEDING));
	}

	@Test
	public void twoTwentyPercentSourcesCombineToThirtySixPercent() {
		final Item first = resistanceItem("resist_poisoned", 0.20);
		final Item second = resistanceItem("resist_poisoned", 0.20);

		assertEquals(0.36, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(first, second), "resist_poisoned"), 0.0000001);
	}

	@Test
	public void ironWillAddsTwentyPercentToSupportedNegativeStatuses() {
		final Item armour = legendaryResistanceItem("armor",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);

		assertEquals(0.20, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(armour), "resist_poisoned"), 0.0000001);
		assertEquals(0.20, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(armour), "resist_bleeding"), 0.0000001);
		assertEquals(0.20, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(armour), "resist_heavy"), 0.0000001);
	}

	@Test
	public void guardianSealAndRegularResistanceCombineIndependently() {
		final Item ring = legendaryResistanceItem("ring",
				LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE);
		ring.put("resist_poisoned", 0.20);

		assertEquals(0.36, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(ring), "resist_poisoned"), 0.0000001);
	}

	@Test
	public void equippedJewelleryParticipatesInRealResistanceResolution() {
		final Player player = PlayerTestHelper.createPlayer("resistance jewellery tester");
		final Item ring = legendaryResistanceItem("ring",
				LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE);
		ring.put("resist_poisoned", 0.20);
		assertTrue(player.equip("finger", ring));

		assertEquals(0.36, EquipmentStatusResistanceService.getResistance(player,
				StatusType.POISONED), 0.0000001);
	}

	@Test
	public void universalLegendaryResistanceDoesNotApplyToUnlistedStatuses() {
		final Item ring = legendaryResistanceItem("ring",
				LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE);

		assertEquals(0.0, EquipmentStatusResistanceService.getEquipmentResistance(
				Arrays.asList(ring), "resist_drunk"), 0.0);
	}

	@Test
	public void missingEquipmentResistanceIsNeutral() {
		assertEquals(0.0, EquipmentStatusResistanceService.getEquipmentResistance(
				Collections.<Item>emptyList(), "resist_poisoned"), 0.0);
	}

	@Test
	public void finalRandomEquipmentResistanceIsCappedAtSixtyPercent() {
		final Item first = resistanceItem("resist_poisoned", 0.90);
		final TestEntity target = new TestEntity(Arrays.asList(first));

		assertEquals(0.60, EquipmentStatusResistanceService.getResistance(target,
				StatusType.POISONED), 0.0000001);
	}

	@Test
	public void legendaryResistanceStillSharesSixtyPercentEquipmentCap() {
		final Item first = legendaryResistanceItem("armor",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);
		final Item second = legendaryResistanceItem("helmet",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);
		final Item third = legendaryResistanceItem("cloak",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);
		final Item fourth = legendaryResistanceItem("boots",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);
		final Item fifth = legendaryResistanceItem("legs",
				LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE);
		final TestEntity target = new TestEntity(
				Arrays.asList(first, second, third, fourth, fifth));

		assertEquals(0.60, EquipmentStatusResistanceService.getResistance(target,
				StatusType.POISONED), 0.0000001);
	}

	@Test
	public void intrinsicAndEquipmentResistanceCombineIndependently() {
		final Item equipment = resistanceItem("resist_poisoned", 0.20);
		final TestEntity target = new TestEntity(Arrays.asList(equipment));
		target.put("resist_poisoned", 0.25);

		assertEquals(0.40, EquipmentStatusResistanceService.getResistance(target,
				StatusType.POISONED), 0.0000001);
	}

	@Test
	public void authoredFullIntrinsicResistanceRemainsFullImmunity() {
		final TestEntity target = new TestEntity(Collections.<Item>emptyList());
		target.put("resist_poisoned", 1.0);

		assertEquals(1.0, EquipmentStatusResistanceService.getResistance(target,
				StatusType.POISONED), 0.0);
	}

	private Item resistanceItem(final String attribute, final double value) {
		final Item item = new Item("resistance test", "ring", "test", null);
		item.put(attribute, value);
		return item;
	}

	private Item legendaryResistanceItem(final String itemClass,
			final String marker) {
		final Item item = new Item("legendary resistance test", itemClass,
				"test", null);
		item.put(marker, 1.0);
		return item;
	}

	private static final class TestEntity extends RPEntity {
		private final List<Item> equipment;

		TestEntity(final List<Item> equipment) {
			this.equipment = equipment;
		}

		@Override
		public List<Item> getDefenseItems() {
			return equipment;
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// no-op
		}

		@Override
		public void logic() {
			// no-op
		}
	}
}
