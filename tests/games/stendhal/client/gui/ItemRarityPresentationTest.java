/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
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
		object.put("class", "custom_weapon_class");
		object.put("rarity_id", "legendary");
		putCategory(object, ItemTooltip.CATEGORY_WEAPON);
		putStat(object, ItemTooltip.ATTACK, "32");
		putStat(object, ItemTooltip.DAMAGE_MIN, "28");
		putStat(object, ItemTooltip.DAMAGE_MAX, "36");
		putStat(object, ItemTooltip.ATTACK_RATE, "2");
		putStat(object, ItemTooltip.ATTACK_INTERVAL_SECONDS, "0.6");
		putStat(object, ItemTooltip.ATTACKS_PER_SECOND, "1.6666666667");
		putStat(object, ItemTooltip.DEFENSE, "10");
		putStat(object, ItemTooltip.DAMAGE_TYPE, "light");
		putStat(object, ItemTooltip.LIFESTEAL, "0.12402917");
		putStat(object, ItemTooltip.IMPROVE, "0");
		putStat(object, ItemTooltip.MAX_IMPROVES, "3");
		putStat(object, ItemTooltip.VALUE, "11432");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("width='180'"));
		assertTrue(tooltip.contains("53,3 DPS"));
		assertTrue(tooltip.contains("#fffaf2"));
		assertTrue(tooltip.contains("28–36 obrażeń"));
		assertTrue(tooltip.contains("1,67 ataku/s"));
		assertFalse(tooltip.contains("pkt. ataku"));
		assertTrue(tooltip.contains("Typ obrażeń: Światło"));
		assertTrue(tooltip.contains("Pancerz: 10"));
		assertTrue(tooltip.contains("+12,4% kradzieży życia"));
		assertTrue(tooltip.contains("Ulepszenie: +0/3"));
		assertTrue(tooltip.contains("Wartość: 11432"));
	}

	@Test
	public void testArmourUsesPublishedCategoryAndPrimaryColor() {
		final RPObject object = ItemTestHelper.createItem("pancerz testowy");
		object.put("class", "custom_armour_class");
		object.put("rarity_id", "epic");
		putCategory(object, ItemTooltip.CATEGORY_ARMOUR);
		putStat(object, ItemTooltip.DEFENSE, "18");
		putStat(object, ItemTooltip.ATTACK, "4");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("18 PANCERZA"));
		assertTrue(tooltip.contains("Ochrona podstawowa"));
		assertTrue(tooltip.contains("#fffaf2"));
		assertTrue(tooltip.contains("+4 ataku"));
		assertFalse(tooltip.contains("DPS"));
		assertFalse(tooltip.contains("obrażeń"));
	}

	@Test
	public void testShieldHighlightsArmour() {
		final RPObject object = ItemTestHelper.createItem("tarcza testowa");
		object.put("class", "shield");
		object.put("rarity_id", "legendary");
		putCategory(object, ItemTooltip.CATEGORY_ARMOUR);
		putStat(object, ItemTooltip.DEFENSE, "175");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("175 PANCERZA"));
		assertTrue(tooltip.contains("Ochrona podstawowa"));
	}

	@Test
	public void testBeltsClassHighlightsArmour() {
		final RPObject object = ItemTestHelper.createItem("pas z mithrilu");
		object.put("class", "belts");
		object.put("rarity_id", "legendary");
		putCategory(object, ItemTooltip.CATEGORY_ARMOUR);
		putStat(object, ItemTooltip.DEFENSE, "21");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("21 PANCERZA"));
		assertTrue(tooltip.contains("Ochrona podstawowa"));
		assertFalse(tooltip.contains("+21 pancerza"));
	}

	@Test
	public void testAccessoryStatsStayInBonusList() {
		final RPObject ring = ItemTestHelper.createItem("pierścień testowy");
		ring.put("class", "custom_accessory");
		ring.put("rarity_id", "rare");
		putCategory(ring, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(ring, ItemTooltip.ATTACK, "7");
		putStat(ring, ItemTooltip.DEFENSE, "17");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(ring));

		assertTrue(tooltip.contains("+7 ataku"));
		assertTrue(tooltip.contains("+17 pancerza"));
		assertFalse(tooltip.contains("17 PANCERZA"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
		assertFalse(tooltip.contains("DPS"));
	}

	@Test
	public void testNecklaceStatsStayInBonusList() {
		final RPObject object = ItemTestHelper.createItem("amulet testowy");
		object.put("class", "necklace");
		object.put("rarity_id", "legendary");
		putCategory(object, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(object, ItemTooltip.DEFENSE, "17");
		putStat(object, ItemTooltip.HEALTH, "25");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+17 pancerza"));
		assertTrue(tooltip.contains("+25 zdrowia"));
		assertFalse(tooltip.contains("17 PANCERZA"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
	}

	@Test
	public void testLegacyDirectWeaponAttributesRemainSupported() {
		final RPObject object = ItemTestHelper.createItem("zwykły miecz");
		object.put("class", "sword");
		object.put("atk", 15);
		object.put("rate", 5);

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("10,0 DPS"));
		assertTrue(tooltip.contains("15–15 obrażeń"));
	}

	private void putCategory(final RPObject object, final String category) {
		putStat(object, ItemTooltip.CATEGORY, category);
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
