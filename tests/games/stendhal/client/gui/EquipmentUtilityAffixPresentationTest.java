package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AffixRerollEvent;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

/** Utility rolls use the same human descriptions in tooltips and at Przemir. */
public class EquipmentUtilityAffixPresentationTest {
	private static final String[][] AFFIXES = {
		{"kill_recovery", "75", "Zawziętość: Po zabiciu potwora odzyskujesz 75 PZ; łącznie maksymalnie 200 PZ."},
		{"potion_conservation", "0.07", "Przezorność: 7% szansy, że użycie eliksiru leczącego go nie zużyje; łącznie maksymalnie 20%."},
		{"single_combat_bonus", "0.04", "Przewaga: Podczas walki z dokładnie jednym potworem zadajesz o 4% więcej obrażeń; łącznie maksymalnie 10%."},
		{"coin_bonus", "0.06", "Łupieżca: Zyskujesz o 6% więcej pieniędzy z pokonanych potworów; łącznie maksymalnie 15%."},
		{"durability_conservation", "0.15", "Dbałość: 15% szansy na uniknięcie zużycia wytrzymałości broni."}
	};
	private Field preferences;
	private Object previousPreferences;

	@Before public void setPolishWithoutSavingPreferences() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		final Properties values = new Properties();
		values.setProperty("config." + ClientLanguage.PROPERTY, "pl");
		preferences.set(WtWindowManager.getInstance(), values);
	}

	@After public void restorePreferences() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}

	@Test public void ordinaryUtilityRowsPrecedeLegendaryAndRetainTheirOwnMarker() {
		for (final String[] selected : AFFIXES) {
			final RPObject item = item();
			item.put(ItemTooltip.ATTRIBUTE, ItemTooltip.REROLLED_AFFIX, selected[0]);
			item.put(ItemTooltip.ATTRIBUTE, ItemTooltip.LEGENDARY_TENACITY, "1");
			final String html = LegendaryAffixPresentation.build(item);
			final String text = html.replaceAll("<[^>]*>", "");
			for (final String[] affix : AFFIXES) {
				assertTrue(text, text.contains(affix[2]));
				assertTrue(html, html.indexOf(affix[2].substring(0, affix[2].indexOf(':')))
						< html.indexOf("color='#f28c28'"));
			}
			assertEquals(1, html.split("affix_rerolled", -1).length - 1);
			assertTrue(html, html.substring(html.lastIndexOf("<tr>", html.indexOf("affix_rerolled")))
					.startsWith("<tr><td valign='top'><font size='-1'><img"));
			final String markedRow = html.substring(html.indexOf("affix_rerolled"),
					html.indexOf("</tr>", html.indexOf("affix_rerolled")));
			assertTrue(markedRow, markedRow.contains(selected[2].substring(0, selected[2].indexOf(':'))));
			assertFalse(markedRow, markedRow.contains("&#9670;"));
		}
	}

	@Test public void polishRerollLabelsMatchTooltipDescriptionsAndUnits() {
		final Player player = PlayerTestHelper.createPlayer("utility labels");
		final String html = LegendaryAffixPresentation.build(item());
		final String text = html.replaceAll("<[^>]*>", "");
		for (final String[] affix : AFFIXES) {
			final String label = AffixRerollEvent.label(player, affix[0], affix[1]);
			assertEquals(affix[2], label);
			assertTrue(text, text.contains(label));
			assertTrue(AffixRerollPresentation.listLabelHtml(label, false, 280, false),
					AffixRerollPresentation.listLabelHtml(label, false, 280, false)
						.replaceAll("<[^>]*>", "").contains(label));
			assertTrue(AffixRerollPresentation.offerLabelHtml(label, 268, false)
					.replaceAll("<[^>]*>", "").contains(label));
		}
	}

	@Test public void translatedRerollLabelsMatchTranslatedTooltips() {
		final Player player = PlayerTestHelper.createPlayer("translated utility labels");
		for (final String language : new String[] {"en", "es"}) {
			player.put("language", language);
			WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, language);
			final String text = LegendaryAffixPresentation.build(item()).replaceAll("<[^>]*>", "");
			for (final String[] affix : AFFIXES) {
				final String label = AffixRerollEvent.label(player, affix[0], affix[1]);
				assertTrue(text, text.contains(label));
				assertFalse(label, label.contains(affix[2].substring(0, affix[2].indexOf(':'))));
			}
			assertTrue(text, text.contains("75 " + ("en".equals(language) ? "HP" : "PV")));
			assertTrue(text, text.contains("7%") && text.contains("4%") && text.contains("6%") && text.contains("15%"));
		}
	}

	@Test public void invalidAndNonpositiveUtilityValuesRemainHidden() {
		final RPObject item = item();
		final String[] invalid = {"0", "NaN", "-0.04", "bad", "Infinity"};
		for (int index = 0; index < AFFIXES.length; index++) {
			item.put(ItemTooltip.ATTRIBUTE, AFFIXES[index][0], invalid[index]);
		}
		assertEquals("", LegendaryAffixPresentation.build(item));
	}

	private static RPObject item() {
		final RPObject item = ItemTestHelper.createItem("utility tooltip");
		for (final String[] affix : AFFIXES) item.put(ItemTooltip.ATTRIBUTE, affix[0], affix[1]);
		return item;
	}
}
