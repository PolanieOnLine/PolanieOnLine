/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityPresentationTest {
	@Test
	public void testEveryRarityHasColorAndTextLabel() {
		assertRarityToolTip("common", "#9e9e9e", "Zwykły");
		assertRarityToolTip("rare", "#4a90e2", "Rzadki");
		assertRarityToolTip("epic", "#9b59b6", "Epicki");
		assertRarityToolTip("legendary", "#ff8c00", "Legendarny");
	}

	@Test
	public void testMissingAndUnknownRarityAreSafe() {
		RPObject object = ItemTestHelper.createItem("legacy item");
		IEntity item = EntityFactory.createEntity(object);
		assertNull(ItemRarityPresentation.buildItemToolTip(item));

		object = ItemTestHelper.createItem("unknown item");
		object.put("rarity_id", "mythical");
		item = EntityFactory.createEntity(object);
		assertNull(ItemRarityPresentation.buildItemToolTip(item));
	}

	@Test
	public void testRarityToolTipEscapesItemName() {
		final RPObject object = ItemTestHelper.createItem("sword <prototype>");
		object.put("rarity_id", "rare");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("SWORD &lt;PROTOTYPE&gt;"));
	}

	@Test
	public void testWeaponToolTipShowsServerPublishedStatistics() {
		final RPObject object = ItemTestHelper.createItem(
				"złota ciupaga z trzema wąsami");
		object.put("rarity_id", "legendary");
		putStat(object, ItemTooltip.ATTACK, "32");
		putStat(object, ItemTooltip.ATTACK_RATE, "2");
		putStat(object, ItemTooltip.DEFENSE, "10");
		putStat(object, ItemTooltip.DAMAGE_TYPE, "light");
		putStat(object, ItemTooltip.LIFESTEAL, "0.12402917");
		putStat(object, ItemTooltip.IMPROVE, "0");
		putStat(object, ItemTooltip.MAX_IMPROVES, "3");
		putStat(object, ItemTooltip.VALUE, "11432");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("Bazowy DPS"));
		assertTrue(tooltip.contains("53,3"));
		assertTrue(tooltip.contains("32 pkt. ataku"));
		assertTrue(tooltip.contains("1,67 ataku na sekundę"));
		assertTrue(tooltip.contains("0,60 s między atakami"));
		assertTrue(tooltip.contains("Typ obrażeń: Światło"));
		assertTrue(tooltip.contains("Obrona: 10"));
		assertTrue(tooltip.contains("+12,4% kradzieży życia"));
		assertTrue(tooltip.contains("Ulepszenie: +0/3"));
		assertTrue(tooltip.contains("Wartość: 11432"));
	}

	@Test
	public void testLegacyDirectAttributesRemainSupported() {
		final RPObject object = ItemTestHelper.createItem("zwykły miecz");
		object.put("atk", 15);
		object.put("rate", 5);

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("Bazowy DPS"));
		assertTrue(tooltip.contains("10,0"));
	}

	private void putStat(final RPObject object, final String key,
			final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}

	private void assertRarityToolTip(final String rarityId, final String color,
			final String displayName) {
		final RPObject object = ItemTestHelper.createItem("test sword");
		object.put("rarity_id", rarityId);
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(color));
		assertTrue(tooltip.contains("TEST SWORD"));
		assertTrue(tooltip.contains(displayName));
	}
}
