package games.stendhal.client.gui;

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
		assertTrue(html, html.contains("Coins in this stack: <b>1 234 567</b>"));
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
}
