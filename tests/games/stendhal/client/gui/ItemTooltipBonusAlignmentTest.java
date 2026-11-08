/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.Position;
import javax.swing.text.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.client.gui.styled.StyledToolTipUI;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

/** Check actual Swing text positions, not merely matching HTML prefixes. */
public class ItemTooltipBonusAlignmentTest {
	private Field preferences;
	private Object previousPreferences;

	@Before public void isolatePolishLanguage() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		Properties values = new Properties();
		values.setProperty("config." + ClientLanguage.PROPERTY, "pl");
		preferences.set(WtWindowManager.getInstance(), values);
	}

	@After public void restoreLanguage() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}

	@Test
	public void bonusSignsAlignAcrossCoreLifeStealResistanceAndAffixSections() throws Exception {
		assertBonusAlignment(false);
	}

	@Test
	public void bonusSignsStayAlignedWithComparisonValuesOnTheRight() throws Exception {
		assertBonusAlignment(true);
	}

	@Test
	public void descriptiveAffixesAlignWithStandardBonusRows() throws Exception {
		final RPObject item = ItemTestHelper.createItem("descriptive alignment ring");
		put(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(item, ItemTooltip.DEFENSE, "10");
		put(item, ItemTooltip.ATTACK, "5");
		put(item, ItemTooltip.RATE_INCREASE, "1");
		put(item, ItemTooltip.STEADY_HAND, "0.1");
		put(item, ItemTooltip.POISON_POTENCY, "0.2");
		put(item, ItemTooltip.ARMOR_CRUSHING, "0.15");
		put(item, ItemTooltip.CRITICAL_HARDENING, "0.08");
		put(item, ItemTooltip.LEGENDARY_IRON_WILL, "1");
		put(item, ItemTooltip.LEGENDARY_LIFE_TIDE, "1");
		final String html = ItemRarityPresentation.buildItemToolTip(EntityFactory.createEntity(item), null);
		SwingUtilities.invokeAndWait(() -> {
			final JToolTip tooltip = new JToolTip();
			final StyledToolTipUI ui = new StyledToolTipUI(null);
			try {
				tooltip.setTipText(html);
				ui.installUI(tooltip);
				tooltip.setSize(tooltip.getPreferredSize());
				final View view = (View) tooltip.getClientProperty(BasicHTML.propertyKey);
				final String text = view.getDocument().getText(0, view.getDocument().getLength());
				final Rectangle bounds = new Rectangle(0, 0, tooltip.getWidth(), tooltip.getHeight());
				final int expectedX = view.modelToView(text.indexOf("◆"), bounds, Position.Bias.Forward).getBounds().x;
				final int attackY = view.modelToView(text.indexOf("+5 ataku"), bounds, Position.Bias.Forward).getBounds().y;
				final int defenseY = view.modelToView(text.indexOf("+10 pancerza"), bounds, Position.Bias.Forward).getBounds().y;
				final int rateY = view.modelToView(text.indexOf("-1 do wagi"), bounds, Position.Bias.Forward).getBounds().y;
				final int steadyY = view.modelToView(text.indexOf("Pewna ręka"), bounds, Position.Bias.Forward).getBounds().y;
				assertEquals("Regular descriptions must keep standard bonus line spacing", defenseY - attackY, steadyY - rateY);

				for (final String title : new String[] {"Pewna ręka", "Zjadliwość", "Kruszenie", "Hartowanie"}) {
					final int diamond = text.lastIndexOf("◆", text.indexOf(title));
					assertEquals(title, expectedX, view.modelToView(diamond, bounds, Position.Bias.Forward).getBounds().x);
				}
				for (final String title : new String[] {"Żelazna Wola", "Przypływ życia"}) {
					assertEquals(title, expectedX, view.modelToView(text.indexOf(title), bounds, Position.Bias.Forward).getBounds().x);
				}
			} catch (Exception exception) {
				throw new AssertionError(exception);
			} finally {
				ui.uninstallUI(tooltip);
			}
		});
	}

	private void assertBonusAlignment(boolean comparing) throws Exception {
		final RPObject item = ItemTestHelper.createItem("alignment ring");
		item.put("rarity_id", "epic");
		put(item, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		put(item, ItemTooltip.SKILL_ATTACK, "7");
		put(item, ItemTooltip.DEFENSE, "10");
		put(item, ItemTooltip.BASE_LIFESTEAL, "0.1");
		put(item, ItemTooltip.EFFECTIVE_LIFESTEAL, "0.1");
		put(item, ItemTooltip.RESISTANCE_PREFIX + "light", "120");
		put(item, ItemTooltip.PARRY_CHANCE, "0.5");
		final RPObject equipped = ItemTestHelper.createItem("equipped alignment ring");
		put(equipped, ItemTooltip.DEFENSE, "5");
		put(equipped, ItemTooltip.SKILL_ATTACK, "3");
		put(equipped, ItemTooltip.BASE_LIFESTEAL, "0.05");
		put(equipped, ItemTooltip.EFFECTIVE_LIFESTEAL, "0.05");
		final String html = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(item), comparing ? equipped : null);
		SwingUtilities.invokeAndWait(() -> {
			final JToolTip tooltip = new JToolTip();
			final StyledToolTipUI ui = new StyledToolTipUI(null);
			try {
				tooltip.setTipText(html);
				ui.installUI(tooltip);
				tooltip.setSize(tooltip.getPreferredSize());
				final View view = (View) tooltip.getClientProperty(BasicHTML.propertyKey);
				final String text = view.getDocument().getText(0, view.getDocument().getLength());
				final Rectangle bounds = new Rectangle(0, 0, tooltip.getWidth(), tooltip.getHeight());
				Integer expectedX = null;
				int count = 0;
				final Matcher signs = Pattern.compile("◆[ \\u00a0]+([+-])").matcher(text);
				while (signs.find()) {
					final int index = signs.start(1);
					final Shape position = view.modelToView(index, bounds, Position.Bias.Forward);
					final int x = position.getBounds().x;
					if (expectedX == null) expectedX = x;
					assertEquals("Bonus sign in " + text.substring(index), expectedX.intValue(), x);
					count++;
				}
				assertEquals("All five bonus rows must be rendered", 5, count);
			} catch (Exception exception) {
				throw new AssertionError(exception);
			} finally {
				ui.uninstallUI(tooltip);
			}
		});
	}

	private static void put(RPObject item, String key, String value) {
		item.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
