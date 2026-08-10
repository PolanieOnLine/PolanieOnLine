/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.ItemTestHelper;

public class MissileItemSystemExclusionTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void missileClassCannotEnterRarityOrAffixSystemsEvenWhenForced() {
		final Weapon missile = missile();
		missile.configureRarity(Boolean.TRUE, ItemRarityProfile.DEFAULT_ID, 1000);
		final ItemRarityService service = new ItemRarityService(new Random(7L));

		assertFalse(service.isEligible(missile));
		service.initialize(missile, ItemCreationContext
				.builder(ItemCreationContext.Source.ADMIN)
				.withForcedRarity(ItemRarity.LEGENDARY)
				.withAffixSeed(12345L)
				.build());

		assertNull(missile.getRarity());
		assertEquals(20, missile.getInt("atk"));
		assertTrue(missile.getRarityModifiers().isEmpty());
		assertFalse(ItemAffixState.hasAny(missile));
		assertNull(ItemAffixState.getSeed(missile));
		assertFalse(missile.has("damage_min"));
		assertFalse(missile.has("damage_max"));
	}

	@Test
	public void missileClassIsNotPublishedAsWeaponByStructuredTooltip() {
		final Weapon missile = missile();
		ItemTooltipService.update(missile);

		assertEquals(ItemTooltip.CATEGORY_OTHER,
				missile.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.CATEGORY));
	}

	@Test
	public void missileClassHasNoLegendarySignatureEligibility() {
		final Weapon missile = missile();
		missile.setRarity(ItemRarity.LEGENDARY);

		assertTrue(LegendaryItemAffixRegistry.getInstance()
				.getEligible(missile).isEmpty());
	}

	private Weapon missile() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "20");
		attributes.put("rate", "5");
		attributes.put("range", "4");
		final Weapon missile = new Weapon("test missile", "missile", "test",
				attributes);
		missile.setEquipableSlots(Arrays.asList("rhand", "lhand", "bag"));
		return missile;
	}
}
