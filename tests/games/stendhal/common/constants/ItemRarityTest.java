/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.common.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ItemRarityTest {
	@Test
	public void exposesStableWireIdsColorsAndDisplayNames() {
		assertRarity(ItemRarity.COMMON, "common", "#9e9e9e", "Zwykły", "Common");
		assertRarity(ItemRarity.RARE, "rare", "#4a90e2", "Rzadki", "Rare");
		assertRarity(ItemRarity.EPIC, "epic", "#9b59b6", "Epicki", "Epic");
		assertRarity(ItemRarity.LEGENDARY, "legendary", "#ff8c00",
				"Legendarny", "Legendary");
	}

	@Test
	public void parsingIsSafeForLegacyAndUnknownValues() {
		assertSame(ItemRarity.EPIC, ItemRarity.fromId("EPIC"));
		assertNull(ItemRarity.fromId(null));
		assertNull(ItemRarity.fromId("mythic"));
		assertSame(ItemRarity.COMMON, ItemRarity.fromIdOrCommon(null));
	}

	private void assertRarity(final ItemRarity rarity, final String id,
			final String color, final String polish, final String english) {
		assertEquals(id, rarity.getId());
		assertEquals(color, rarity.getColorHex());
		assertEquals(polish, rarity.getPolishDisplayName());
		assertEquals(english, rarity.getEnglishDisplayName());
		assertSame(rarity, ItemRarity.fromId(id));
	}
}
