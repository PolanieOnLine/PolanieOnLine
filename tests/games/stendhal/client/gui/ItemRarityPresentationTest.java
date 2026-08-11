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
	private static final String DIVIDER_MARKER =
			"&#9472;&#9472;&#9472;&#9472;&#9671;&#9671;&#9472;&#9472;&#9472;&#9472;";
	private static final String BETTER_COLOR_MARKER = "color='#62d26f'";
	private static final String WORSE_COLOR_MARKER = "color='#ef6a62'";

	@Test
	public void testEveryRarityHasColorAndTextLabel() {
		assertRarityToolTip("common", "#9e9e9e", "Zwykły");
		assertRarityToolTip("rare", "#4a90e2", "Rzadki");
		assertRarityToolTip("epic", "#9b59b6", "Epicki");
		assertRarityToolTip("legendary", "#ff8c00", "Legendarny");
	}

	@Test
	public void testRarityGlowMetadataUsesGraduatedStrength() {
		assertGlowMarker("common", "#9e9e9e", "0.05");
		assertGlowMarker("rare", "#4a90e2", "0.09");
		assertGlowMarker("epic", "#9b59b6", "0.12");
		assertGlowMarker("legendary", "#ff8c00", "0.14");
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
		putStat(object, ItemTooltip.PARRY_CHANCE, "0.15");
		putStat(object, ItemTooltip.ARMOR_PENETRATION, "0.25");
		putStat(object, ItemTooltip.UPGRADE_LEVEL, "0");
		putStat(object, ItemTooltip.MAX_UPGRADE_LEVEL, "3");
		putStat(object, ItemTooltip.VALUE, "11432");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("width='190'"));
		assertFalse(tooltip.contains("bgcolor="));
		assertFalse(tooltip.contains("size='+1'"));
		assertTrue(tooltip.contains(
				"<!--item-rarity-glow:#ff8c00:0.14-->"));
		assertTrue(tooltip.contains("53,3 pkt. obrażeń na sekundę"));
		assertTrue(tooltip.contains("[28–36] pkt. obrażeń za trafienie"));
		assertTrue(tooltip.contains("&#9500;&#9472;&#9670;"));
		assertTrue(tooltip.contains("&#9492;&#9472;&#9670;"));
		assertTrue(tooltip.contains(
				"1,67 ataku na sekundę (Szybka broń)"));
		assertFalse(tooltip.contains("53,3 DPS"));
		assertFalse(tooltip.contains("pkt. ataku"));
		assertFalse(tooltip.contains("Moc przedmiotu"));
		assertTrue(tooltip.contains("Typ obrażeń: Światło"));
		assertTrue(tooltip.contains("Pancerz: 10"));
		assertTrue(tooltip.contains("+12,4% kradzieży życia"));
		assertTrue(tooltip.contains("+15% szansy na parowanie"));
		assertTrue(tooltip.contains("&#9670; +15% szansy na parowanie"));
		assertTrue(tooltip.contains("+25% penetracji pancerza"));
		assertTrue(tooltip.contains("&#9670; +25% penetracji pancerza"));
		assertTrue(tooltip.contains("Ulepszenie: +0 / +3"));
		assertTrue(tooltip.indexOf("Ulepszenie: +0 / +3")
				< tooltip.indexOf("53,3 pkt. obrażeń na sekundę"));
		assertTrue(tooltip.indexOf("Ulepszenie: +0 / +3")
				== tooltip.lastIndexOf("Ulepszenie: +0 / +3"));
		assertTrue(tooltip.contains("Wartość: 11432"));
		assertTrue(tooltip.contains("text-align:right"));
	}

	@Test
	public void testSlowWeaponGetsReadableSpeedLabel() {
		final RPObject object = ItemTestHelper.createItem("powolny młot");
		object.put("class", "club");
		putCategory(object, ItemTooltip.CATEGORY_WEAPON);
		putStat(object, ItemTooltip.ATTACK, "36");
		putStat(object, ItemTooltip.DAMAGE_MIN, "32");
		putStat(object, ItemTooltip.DAMAGE_MAX, "40");
		putStat(object, ItemTooltip.ATTACKS_PER_SECOND, "0.9");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(
				"0,90 ataku na sekundę (Powolna broń)"));
	}

	@Test
	public void testArmourUsesPublishedCategoryAndPrimaryColor() {
		final RPObject object = ItemTestHelper.createItem("pancerz testowy");
		object.put("class", "custom_armour_class");
		object.put("rarity_id", "epic");
		putCategory(object, ItemTooltip.CATEGORY_ARMOUR);
		putStat(object, ItemTooltip.DEFENSE, "18");
		putStat(object, ItemTooltip.ATTACK, "4");
		putStat(object, ItemTooltip.RESISTANCE_PREFIX + "light", "105");
		putStat(object, ItemTooltip.RESISTANCE_PREFIX + "dark", "80");
		putStat(object, ItemTooltip.RESISTANCE_PREFIX + "fire", "100");
		putStat(object, ItemTooltip.LIFESTEAL, "0.10");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("18 pkt. pancerza"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
		assertTrue(tooltip.contains("+5% odporności na światło"));
		assertTrue(tooltip.contains("-20% odporności na mrok"));
		assertTrue(tooltip.contains("+10% kradzieży życia"));
		assertTrue(tooltip.contains("<!--item-rarity-glow:#9b59b6:0.12-->"));
		assertTrue(tooltip.contains("&#9670; +5% odporności na światło"));
		assertTrue(tooltip.contains("&#9670; -20% odporności na mrok"));
		assertFalse(tooltip.contains("odporności na ogień"));
		assertFalse(tooltip.contains("ŚWIATŁO: 105%"));
		assertFalse(tooltip.contains("MROK: 80%"));
		assertTrue(tooltip.contains("+4 ataku"));
		assertSectionDividerBetween(tooltip, "+4 ataku",
				"+5% odporności na światło");
		assertSectionDividerBetween(tooltip, "-20% odporności na mrok",
				"+10% kradzieży życia");
		assertFalse(tooltip.contains("obrażeń na sekundę"));
		assertFalse(tooltip.contains("obrażeń za trafienie"));
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

		assertTrue(tooltip.contains("175 pkt. pancerza"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
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

		assertTrue(tooltip.contains("21 pkt. pancerza"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
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
		putStat(ring, ItemTooltip.UPGRADE_LEVEL, "2");
		putStat(ring, ItemTooltip.MAX_UPGRADE_LEVEL, "4");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(ring));

		assertTrue(tooltip.contains("+7 ataku"));
		assertTrue(tooltip.contains("+17 pancerza"));
		assertTrue(tooltip.contains("PIERŚCIEŃ TESTOWY +2"));
		assertTrue(tooltip.contains("Ulepszenie: +2 / +4"));
		assertTrue(tooltip.indexOf("Rzadki")
				< tooltip.indexOf("Ulepszenie: +2 / +4"));
		assertTrue(countOccurrences(tooltip, DIVIDER_MARKER) == 1);
		assertFalse(tooltip.contains("17 pkt. pancerza"));
		assertFalse(tooltip.contains("Ochrona podstawowa"));
		assertFalse(tooltip.contains("obrażeń na sekundę"));
		assertFalse(tooltip.contains("Moc przedmiotu"));
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
		assertFalse(tooltip.contains("17 pkt. pancerza"));
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

		assertTrue(tooltip.contains("10,0 pkt. obrażeń na sekundę"));
		assertTrue(tooltip.contains("[15–15] pkt. obrażeń za trafienie"));
		assertTrue(tooltip.contains("0,67 ataku na sekundę (Powolna broń)"));
	}

	@Test
	public void testComparisonColorsBetterAndWorseDamageValues() {
		final RPObject candidate = ItemTestHelper.createItem("nowy miecz");
		candidate.put("class", "sword");
		putCategory(candidate, ItemTooltip.CATEGORY_WEAPON);
		putStat(candidate, ItemTooltip.DAMAGE_MIN, "11");
		putStat(candidate, ItemTooltip.DAMAGE_MAX, "19");
		putStat(candidate, ItemTooltip.ATTACKS_PER_SECOND, "1");
		final RPObject equipped = ItemTestHelper.createItem("założony miecz");
		equipped.put("class", "sword");
		putCategory(equipped, ItemTooltip.CATEGORY_WEAPON);
		putStat(equipped, ItemTooltip.DAMAGE_MIN, "10");
		putStat(equipped, ItemTooltip.DAMAGE_MAX, "20");
		putStat(equipped, ItemTooltip.ATTACKS_PER_SECOND, "1");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(candidate), equipped);

		assertTrue(tooltip.contains("Porównanie z: założony miecz"));
		assertTrue(tooltip.contains("<font color='#62d26f'>+1</font>"));
		assertTrue(tooltip.contains("<font color='#ef6a62'>-1</font>"));
	}

	@Test
	public void testComparisonCanBeDisabledWithoutHidingItemStatistics() {
		final RPObject candidate = ItemTestHelper.createItem("nowy miecz");
		candidate.put("class", "sword");
		putCategory(candidate, ItemTooltip.CATEGORY_WEAPON);
		putStat(candidate, ItemTooltip.DAMAGE_MIN, "11");
		putStat(candidate, ItemTooltip.DAMAGE_MAX, "19");
		putStat(candidate, ItemTooltip.ATTACKS_PER_SECOND, "1");
		final RPObject equipped = ItemTestHelper.createItem("założony miecz");
		equipped.put("class", "sword");
		putCategory(equipped, ItemTooltip.CATEGORY_WEAPON);
		putStat(equipped, ItemTooltip.DAMAGE_MIN, "10");
		putStat(equipped, ItemTooltip.DAMAGE_MAX, "20");
		putStat(equipped, ItemTooltip.ATTACKS_PER_SECOND, "1");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(candidate), equipped, false);

		assertTrue(tooltip.contains("[11–19] pkt. obrażeń za trafienie"));
		assertFalse(tooltip.contains("Porównanie z:"));
		assertFalse(tooltip.contains(BETTER_COLOR_MARKER));
		assertFalse(tooltip.contains(WORSE_COLOR_MARKER));
	}

	@Test
	public void testComparisonShowsStatsLostByCandidate() {
		final RPObject candidate = ItemTestHelper.createItem("nowy pierścień");
		putCategory(candidate, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(candidate, ItemTooltip.ATTACK, "5");
		final RPObject equipped = ItemTestHelper.createItem("założony pierścień");
		putCategory(equipped, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(equipped, ItemTooltip.ATTACK, "10");
		putStat(equipped, ItemTooltip.HEALTH, "8");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(candidate), equipped);

		assertTrue(tooltip.contains("+5 ataku"));
		assertTrue(tooltip.contains("+0 zdrowia"));
		assertTrue(tooltip.contains("<font color='#ef6a62'>-5</font>"));
		assertTrue(tooltip.contains("<font color='#ef6a62'>-8</font>"));
	}

	@Test
	public void testComparisonUsesPublishedFlatAffixKeys() {
		final RPObject candidate = ItemTestHelper.createItem("nowy amulet");
		putCategory(candidate, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(candidate, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "3");
		final RPObject equipped = ItemTestHelper.createItem("założony amulet");
		putCategory(equipped, ItemTooltip.CATEGORY_ACCESSORY);
		putStat(equipped, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "1");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(candidate), equipped);

		assertTrue(tooltip.contains("+3 dodatkowego ataku"));
		assertTrue(tooltip.contains("<font color='#62d26f'>+2</font>"));
	}

	@Test
	public void testEqualStatsDoNotAddComparisonNoise() {
		final RPObject candidate = ItemTestHelper.createItem("nowa zbroja");
		putCategory(candidate, ItemTooltip.CATEGORY_ARMOUR);
		putStat(candidate, ItemTooltip.DEFENSE, "25");
		final RPObject equipped = ItemTestHelper.createItem("założona zbroja");
		putCategory(equipped, ItemTooltip.CATEGORY_ARMOUR);
		putStat(equipped, ItemTooltip.DEFENSE, "25");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(candidate), equipped);

		assertFalse(tooltip.contains(BETTER_COLOR_MARKER));
		assertFalse(tooltip.contains(WORSE_COLOR_MARKER));
	}

	private void assertSectionDividerBetween(final String tooltip,
			final String before, final String after) {
		final int beforeIndex = tooltip.indexOf(before);
		final int afterIndex = tooltip.indexOf(after);
		assertTrue(beforeIndex >= 0);
		assertTrue(afterIndex > beforeIndex);
		assertTrue(tooltip.substring(beforeIndex, afterIndex)
				.contains(DIVIDER_MARKER));
	}

	private int countOccurrences(final String text, final String needle) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(needle, index)) >= 0) {
			count++;
			index += needle.length();
		}
		return count;
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

	private void assertGlowMarker(final String rarityId, final String color,
			final String opacity) {
		final RPObject object = ItemTestHelper.createItem("glow test");
		object.put("rarity_id", rarityId);
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("<!--item-rarity-glow:" + color + ":"
				+ opacity + "-->"));
	}
}
