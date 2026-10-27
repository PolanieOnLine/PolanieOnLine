/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class GlyphTooltipPresentationTest {

    @Test
    public void levelFractionAndRegenerationCapAppearWithoutDuplicatingSpeed() {
        final RPObject object = ItemTestHelper.createItem("glif żywej wody");
        object.put("class", "glyph");
        object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_OTHER);
        object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.GLYPH_LEVEL, "2");
        object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.RATE_INCREASE, "0.7");
        object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.OUT_OF_COMBAT_REGEN, "0.75");
        object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.OUT_OF_COMBAT_REGEN_CAP, "14");
        final String tooltip = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(object));
        assertTrue(tooltip.contains("GLIF ŻYWEJ WODY II"));
        assertTrue(tooltip.contains("0,7 zmniejszenia wagi broni"));
        assertTrue(tooltip.contains("0,75% maksymalnych PW"));
        assertTrue(tooltip.contains("limit 14 PW"));
        assertFalse(tooltip.contains("do wagi broni"));
        final RPObject equipped = ItemTestHelper.createItem("glif żywej wody");
        equipped.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_OTHER);
        equipped.put(ItemTooltip.ATTRIBUTE, ItemTooltip.OUT_OF_COMBAT_REGEN, "0.5");
        final String comparison = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(object), equipped);
        assertTrue(comparison, comparison.contains("+0,25"));
    }

	@Test
	public void knowledgeShowsPermanentCreatureExperienceAsAGlyphProperty() {
		final RPObject object = ItemTestHelper.createItem("glif wiedzy");
		object.put("class", "glyph");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_OTHER);
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.COMBAT_EXPERIENCE_BONUS, "10");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(object));
		assertTrue(tooltip.contains("+10% doświadczenia z potworów"));
	}
	@Test
	public void strzybogGlyphExplainsItsAttackRateReductionInFullTooltip() {
		final RPObject object = ItemTestHelper.createItem("glif Strzyboga");
		object.put("class", "glyph");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY,
				ItemTooltip.CATEGORY_OTHER);
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.RATE_INCREASE, "1");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(
				"&#9670; -1 do wagi broni"));
	}
}
