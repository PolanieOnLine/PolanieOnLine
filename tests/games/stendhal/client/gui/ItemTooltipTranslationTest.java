package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemTooltipTranslationTest {
	private Field preferences;
	private Object previousPreferences;

	@Before public void useEnglishWithoutSavingPreferences() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		Properties values = new Properties();
		values.setProperty("config." + ClientLanguage.PROPERTY, "en");
		preferences.set(WtWindowManager.getInstance(), values);
	}

	@After public void restorePreferences() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}

	@Test public void translatesNestedWeaponLabelsAndKeepsUpgradeValues() {
		RPObject item = ItemTestHelper.createItem("test sword");
		item.put("class", "sword");
		stat(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		stat(item, ItemTooltip.DAMAGE_MIN, "13");
		stat(item, ItemTooltip.DAMAGE_MAX, "21");
		stat(item, ItemTooltip.ATTACKS_PER_SECOND, "1.5");
		stat(item, ItemTooltip.DAMAGE_TYPE, "light");
		stat(item, ItemTooltip.UPGRADE_LEVEL, "2");
		stat(item, ItemTooltip.MAX_UPGRADE_LEVEL, "4");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("Fast weapon"));
		assertTrue(html, html.contains("Damage type: Light"));
		assertTrue(html, html.contains("Upgrade: +2 / +4"));
		assertTrue(html, html.contains("[13–21] damage per hit"));
	}

	@Test public void translatesBonusLabelsWithTheirNumbersAndDiamonds() {
		RPObject item = ItemTestHelper.createItem("test ring");
		stat(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		stat(item, ItemTooltip.DEFENSE, "16");
		stat(item, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "2");
		stat(item, ItemTooltip.RESIST_BLEEDING, "0.12");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("&#9670; +16 armour"));
		assertTrue(html, html.contains("&#9670; +2 additional attack"));
		assertTrue(html, html.contains("&#9670; +12% bleeding resistance"));
	}

	@Test public void translatesMoneyBeforeItsEarlyReturn() {
		RPObject money = ItemTestHelper.createItem("money");
		money.put("quantity", 1234567);
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(money), null, false);
		assertEquals("Total: 1234567 coins", html);
		money.put("quantity", 1);
		assertEquals("Total: 1 coin", ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(money)));
	}

	@Test public void translatesElementalResistanceAsACompleteBonus() {
		RPObject item = ItemTestHelper.createItem("test ring");
		item.put("rarity_id", "epic");
		stat(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		stat(item, ItemTooltip.RESISTANCE_PREFIX + "light", "105");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("Epic"));
		assertTrue(html, html.contains("&#9670; +5% resistance to light"));
	}

	@Test public void translatesStandaloneAffixWeightAndPreservesItsValue() {
		RPObject item = ItemTestHelper.createItem("test sword");
		stat(item, ItemTooltip.RATE_INCREASE, "2");
		String html = LegendaryAffixPresentation.build(item);
		assertTrue(html, html.contains("-2 weapon weight"));
	}

	private void stat(RPObject item, String key, String value) {
		item.put(ItemTooltip.ATTRIBUTE, key, value);
	}

	@Test public void translatesInstantHealingUnitsInsideBoldMarkup() {
		RPObject item = ItemTestHelper.createItem("mega potion");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_INSTANT, "true");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_AMOUNT, "800");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("+800 HP"));
		assertTrue(html, html.contains("instant"));
		assertTrue(html, !html.contains("PW"));
	}

	@Test public void translatesPeriodicHealingUnitsAndFrequency() {
		RPObject item = ItemTestHelper.createItem("sandwich");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_REGEN, "10");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_FREQUENCY_SECONDS, "3");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_DURATION_SECONDS, "36");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("+10 HP every 3 s"));
		assertTrue(html, html.contains("Healing duration: 36 s"));
	}

	@Test public void translatesArmourAndRequiredLevelWithoutChangingValues() {
		RPObject item = ItemTestHelper.createItem("mithril cloak");
		stat(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		stat(item, ItemTooltip.DEFENSE, "35");
		stat(item, ItemTooltip.MIN_LEVEL, "300");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("35 armour points"));
		assertTrue(html, html.contains("Required level: 300"));
		RPObject equipped = ItemTestHelper.createItem("old cloak");
		stat(equipped, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		stat(equipped, ItemTooltip.DEFENSE, "30");
		String comparison = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), equipped, true);
		assertTrue(comparison, comparison.contains("35 armour points"));
		assertTrue(comparison, !comparison.contains("pkt. pancerza"));
	}

	@Test public void polishHealingAndArmourRemainPolish() throws Exception {
		((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "pl");
		RPObject item = ItemTestHelper.createItem("kanapka");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_REGEN, "10");
		stat(item, ItemTooltip.CONSUMABLE_HEALING_FREQUENCY_SECONDS, "3");
		String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null, false);
		assertTrue(html, html.contains("+10 PW co 3 s"));
		RPObject cloak = ItemTestHelper.createItem("płaszcz z mithrilu");
		stat(cloak, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		stat(cloak, ItemTooltip.DEFENSE, "35");
		stat(cloak, ItemTooltip.MIN_LEVEL, "300");
		String armour = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(cloak), null, false);
		assertTrue(armour, armour.contains("35 pkt. pancerza"));
		assertTrue(armour, armour.contains("Wymagany poziom: 300"));
	}
}
