/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import utilities.RPClass.ItemTestHelper;

public class ExpandedLegendaryTooltipStateTest {
	private static final String[] MARKERS = {
		ItemTooltip.LEGENDARY_DUEL_MASTER,
		ItemTooltip.LEGENDARY_CRUSHING_BLOW,
		ItemTooltip.LEGENDARY_STUNNING_FORCE,
		ItemTooltip.LEGENDARY_BINDING_STRIKE,
		ItemTooltip.LEGENDARY_MERCILESS_REACH,
		ItemTooltip.LEGENDARY_FALCON_EYE,
		ItemTooltip.LEGENDARY_FIRST_SALVO,
		ItemTooltip.LEGENDARY_POWER_OVERLOAD,
		ItemTooltip.LEGENDARY_ARCANE_FOCUS,
		ItemTooltip.LEGENDARY_IRON_WILL,
		ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION,
		ItemTooltip.LEGENDARY_HERO_EYE,
		ItemTooltip.LEGENDARY_GUARDIAN_SEAL
	};

	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void allExpandedLegendaryMarkersArePublishedToStructuredTooltip() {
		final Item item = new Item("expanded legendary tooltip", "sword", "test", null);
		for (final String marker : MARKERS) {
			item.put(marker, 1.0);
		}

		ItemTooltipService.update(item);

		for (final String marker : MARKERS) {
			assertEquals("missing tooltip marker " + marker, "1.0",
					item.getMap(ItemTooltip.ATTRIBUTE).get(marker));
		}
	}

	@Test
	public void mercilessReachKeepsBonusOutOfDisplayedBaseRange() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "20");
		attributes.put("rate", "5");
		attributes.put("range", "2");
		final Item whip = new Item("legendary reach tooltip", "whip", "test",
				attributes);
		whip.put(WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE, 1.0);

		ItemTooltipService.update(whip);

		assertEquals(2, whip.getRange());
		assertEquals("1", whip.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.RANGE));
		assertEquals("1.0", whip.getMap(ItemTooltip.ATTRIBUTE).get(
				ItemTooltip.LEGENDARY_MERCILESS_REACH));
	}
}
