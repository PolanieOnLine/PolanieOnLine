/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixStateTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void restoresMapAndMaterializedParryValue() {
		final Item saved = sword();
		saved.put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.12);
		saved.put(ItemAffixState.ATTRIBUTE,
				ParryService.PARRY_CHANCE_ATTRIBUTE, "0.12");
		final Item restored = sword();

		ItemAffixState.restore(restored, saved);

		assertTrue(ItemAffixState.has(restored,
				ParryService.PARRY_CHANCE_ATTRIBUTE));
		assertEquals("0.12", ItemAffixState.getValues(restored).get(
				ParryService.PARRY_CHANCE_ATTRIBUTE));
		assertEquals(0.12,
				restored.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0000001);
	}

	@Test
	public void restoresLegendarySignatureMarker() {
		final Item saved = sword();
		saved.put(WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 1.0);
		saved.put(ItemAffixState.ATTRIBUTE,
				WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, "1.0");
		final Item restored = sword();

		ItemAffixState.restore(restored, saved);

		assertTrue(ItemAffixState.has(restored,
				WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE));
		assertEquals(1.0, restored.getDouble(
				WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE), 0.0);
	}

	@Test
	public void everyLegendaryMaterializedAttributeIsDeclaredOnItemRPClass() {
		final Set<String> names = new HashSet<String>();
		for (final Definition definition
				: RPClass.getRPClass("item").getDefinitions()) {
			names.add(definition.getName());
		}

		for (final ItemAffixDefinition definition
				: LegendaryItemAffixRegistry.getInstance().getDefinitions()) {
			assertTrue("legendary attribute missing from item RPClass: "
					+ definition.getAttribute(),
					names.contains(definition.getAttribute()));
		}
	}

	@Test
	public void legacyMaterializedValueSurvivesWithoutBecomingRolledAffix() {
		final Item saved = sword();
		saved.put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.09);
		final Item restored = sword();

		ItemAffixState.restore(restored, saved);

		assertEquals(0.09,
				restored.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0000001);
		assertFalse(ItemAffixState.hasAny(restored));
	}

	@Test
	public void unknownAffixIdIsPreservedButNotMaterialized() {
		final Item saved = sword();
		saved.put(ItemAffixState.ATTRIBUTE, "future_affix", "42");
		final Item restored = sword();

		ItemAffixState.restore(restored, saved);

		assertEquals("42", ItemAffixState.getValues(restored).get("future_affix"));
		assertFalse(restored.has("future_affix"));
	}

	private Item sword() {
		return new Item("affix state sword", "sword", "test", null);
	}
}
