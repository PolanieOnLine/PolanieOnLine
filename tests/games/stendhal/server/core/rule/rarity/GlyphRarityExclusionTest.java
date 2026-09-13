/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Glyph;
import utilities.RPClass.ItemTestHelper;

public class GlyphRarityExclusionTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void glyphNeverReceivesRarityOrAffixesEvenWhenForced() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("critical_chance", "15.0");
		final Glyph glyph = new Glyph("glif testowy", "glyph", "glyph_test",
				attributes);
		glyph.setEquipableSlots(Arrays.asList("bag", "control_rune"));
		glyph.configureRarity(Boolean.TRUE, ItemRarityProfile.DEFAULT_ID, 10000);

		new ItemRarityService(new Random(7L)).initialize(glyph,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.LEGENDARY)
						.withAffixSeed(1234L)
						.build());

		assertNull(glyph.getRarity());
		assertFalse(ItemAffixState.hasAny(glyph));
		assertEquals(15.0, glyph.getDouble("critical_chance"), 0.0);
	}
}
