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
		object.put("class", "axe");
		object.put("rarity_id", "legendary");
		putStat(object, ItemTooltip.ATTACK, "32");
		putStat(object, ItemTooltip.DAMAGE_MIN, "28");
		putStat(object, ItemTooltip.DAMAGE_MAX, "36");
		putStat(object, ItemTooltip.ATTACK_RATE, "2");
		putStat(object, ItemTooltip.DEFENSE, "10");
		putStat(object, ItemTooltip.DAMAGE_TYPE, "light");
		putStat(object, ItemTooltip.LIFESTEAL, "0.12402917");
		putStat(object, ItemTooltip.IMPROVE, "0");
		putStat(object, ItemTooltip.MAX_IMPROVES, "3");
		putStat(object, ItemTooltip.VALUE, "11432");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("width:195px"));
		assertTrue(tooltip.contains("53,3 DPS"));
		assertTrue(tooltip.contains("28–36 obrażeń"));
		assertTrue(tooltip.contains("1,67 ataku/s"));
		assertFalse(tooltip.contains("pkt. ataku"));
		assertTrue(tooltip.contains("Typ obrażeń: Światło"));
		assertTrue(tooltip.contains("Obrona: 10"));
		assertTrue(tooltip.contains("+12,4% kradzieży życia"));
		assertTrue(tooltip.contains("Ulepszenie: +0/3"));
		assertTrue(tooltip.contains("Wartość: 11432"));
	}

	@Test
	public void testArmourHighlightsDefenseAndDoesNotShowDps() {
		final RPObject object = ItemTestHelper.createItem("pancerz testowy");
		object.put("class", "armor");
		object.put("rarity_id", "epic");
		putStat(object, ItemTooltip.DEFENSE, "18");
		putStat(object, ItemTooltip.ATTACK, "4");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("18 OBRONY"));
		assertTrue(tooltip.contains("+4 ataku"));
		assertFalse(tooltip.contains("DPS"));
		assertFalse(tooltip.contains("obrażeń"));
	}

	@Test
	public void testRingAttackBonusDoesNotBecomeWeaponDps() {
		final RPObject object = ItemTestHelper.createItem("pierścień testowy");
		object.put("class", "ring");
		object.put("rarity_id", "rare");
		putStat(object, ItemTooltip.ATTACK, "7");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+7 ataku"));
		assertFalse(tooltip.contains("DPS"));
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
