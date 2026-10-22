/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.Position;
import javax.swing.text.LabelView;
import javax.swing.text.View;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixPresentationLayoutTest {
	@Test
	public void flatAttackAndDefenseAffixesAppearOnceAfterResistancesNotAmongCoreStats() {
		final RPObject object = ItemTestHelper.createItem("amulet z mithrilu");
		object.put("class", "necklace");
		object.put("rarity_id", "epic");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(object, ItemTooltip.ATTACK, "3");
		put(object, ItemTooltip.DEFENSE, "16");
		put(object, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "2");
		put(object, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS, "4");
		put(object, ItemTooltip.RESISTANCE_PREFIX + "light", "120");
		put(object, ItemTooltip.RESIST_BLEEDING, "0.12");
		final RPObject equipped = ItemTestHelper.createItem("amulet");
		put(equipped, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "1");
		put(equipped, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS, "2");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object), equipped);
		assertTrue(tooltip.contains("+3 ataku"));
		assertTrue(tooltip.contains("+16 pancerza"));
		for (final String line : new String[] {"+2 dodatkowego ataku", "+4 dodatkowego pancerza"}) {
			final int index = tooltip.indexOf(line);
			assertTrue(tooltip, index >= 0);
			org.junit.Assert.assertEquals("An affix must only be shown once", index, tooltip.lastIndexOf(line));
			assertTrue("Flat affixes must stay below resistance affixes", index > tooltip.indexOf("odporności na krwawienie"));
			final String row = tooltip.substring(index, tooltip.indexOf("</tr>", index));
			assertTrue("The affix must retain its right-hand comparison", row.contains("align='right'"));
			assertTrue(row.contains("color='#62d26f'"));
		}
	}

	@Test
	public void comparisonTableRowsRetainTheTooltipTextColor() throws Exception {
		final RPObject object = ItemTestHelper.createItem("pierścień");
		object.put("class", "ring");
		put(object, ItemTooltip.ATTACK, "3");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object), ItemTestHelper.createItem("założony pierścień"));
		SwingUtilities.invokeAndWait(() -> {
			try {
				final JLabel label = new JLabel(tooltip);
				label.setForeground(Color.BLACK);
				final View view = (View) label.getClientProperty(BasicHTML.propertyKey);
				final String text = view.getDocument().getText(0, view.getDocument().getLength());
				org.junit.Assert.assertEquals("Table cells must inherit the tooltip's readable color",
						textColor(view, text.indexOf("PIERŚCIEŃ")), textColor(view, text.indexOf("+3 ataku")));
			} catch (final javax.swing.text.BadLocationException e) {
				throw new AssertionError(e);
			}
		});
	}

	private static Color textColor(final View view, final int offset) {
		if (view instanceof LabelView && view.getStartOffset() <= offset && view.getEndOffset() > offset) {
			return ((LabelView) view).getForeground();
		}
		for (int i = 0; i < view.getViewCount(); i++) {
			final Color color = textColor(view.getView(i), offset);
			if (color != null) return color;
		}
		return null;
	}

	@Test
	public void comparisonValuesRenderInARightHandColumnOnTheStatLine() throws Exception {
		final RPObject object = ItemTestHelper.createItem("pierścień");
		object.put("class", "ring");
		put(object, ItemTooltip.ATTACK, "3");
		put(object, ItemTooltip.DEFENSE, "7");
		final RPObject equipped = ItemTestHelper.createItem("pierścień");
		equipped.put("class", "ring");
		put(equipped, ItemTooltip.DEFENSE, "2");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object), equipped);
		SwingUtilities.invokeAndWait(() -> {
			try {
				final JLabel label = new JLabel(tooltip);
				final View view = (View) label.getClientProperty(BasicHTML.propertyKey);
				final Rectangle allocation = new Rectangle(0, 0,
						(int) Math.ceil(view.getPreferredSpan(View.X_AXIS)),
						(int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)));
				view.setSize(allocation.width, allocation.height);
				assertTrue("Comparisons must not force a wide tooltip", allocation.width <= 245);
				final String text = view.getDocument().getText(0, view.getDocument().getLength());
				final Rectangle attack = view.modelToView(text.indexOf("+3 ataku"), allocation, Position.Bias.Forward).getBounds();
				final Rectangle attackDelta = view.modelToView(text.lastIndexOf("+3"), allocation, Position.Bias.Forward).getBounds();
				final Rectangle defense = view.modelToView(text.indexOf("+7 pancerza"), allocation, Position.Bias.Forward).getBounds();
				final Rectangle defenseDelta = view.modelToView(text.lastIndexOf("+5"), allocation, Position.Bias.Forward).getBounds();
				assertTrue("Attack comparison must stay on the stat line", Math.abs(attack.y - attackDelta.y) <= 1);
				assertTrue("Defense comparison must stay on the stat line", Math.abs(defense.y - defenseDelta.y) <= 1);
				assertTrue("Comparisons must align at the right edge", Math.abs(attackDelta.x - defenseDelta.x) <= 1);
				assertTrue("Comparisons must not remain inline after the label", attackDelta.x > allocation.width * 0.7);
			} catch (final javax.swing.text.BadLocationException e) {
				throw new AssertionError(e);
			}
		});
	}

	@Test
	public void weaponSpeedDescriptionStartsBelowTheComparedAttackRate() throws Exception {
		final RPObject object = ItemTestHelper.createItem("ciupaga");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		put(object, ItemTooltip.DAMAGE_MIN, "26");
		put(object, ItemTooltip.DAMAGE_MAX, "37");
		put(object, ItemTooltip.ATTACKS_PER_SECOND, "1.67");
		final RPObject equipped = ItemTestHelper.createItem("ciupaga");
		put(equipped, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		put(equipped, ItemTooltip.DAMAGE_MIN, "25");
		put(equipped, ItemTooltip.DAMAGE_MAX, "39");
		put(equipped, ItemTooltip.ATTACKS_PER_SECOND, "1.5");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object), equipped);
		SwingUtilities.invokeAndWait(() -> {
			try {
				final JLabel label = new JLabel(tooltip);
				final View view = (View) label.getClientProperty(BasicHTML.propertyKey);
				final Rectangle allocation = new Rectangle(0, 0,
						(int) Math.ceil(view.getPreferredSpan(View.X_AXIS)),
						(int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)));
				view.setSize(allocation.width, allocation.height);
				final String text = view.getDocument().getText(0, view.getDocument().getLength());
				final Rectangle rate = view.modelToView(text.indexOf("1,67 ataku"), allocation, Position.Bias.Forward).getBounds();
				final Rectangle description = view.modelToView(text.indexOf("(Szybka broń)"), allocation, Position.Bias.Forward).getBounds();
				final Rectangle delta = view.modelToView(text.indexOf("+0,17"), allocation, Position.Bias.Forward).getBounds();
				assertTrue("The speed description must start on the next line", description.y >= rate.y + rate.height);
				assertTrue("The speed description must align with the rate", Math.abs(description.x - rate.x) <= 1);
				assertTrue("The comparison must stay beside the rate, not its description", Math.abs(delta.y - rate.y) <= 1);
			} catch (final javax.swing.text.BadLocationException e) {
				throw new AssertionError(e);
			}
		});
	}
	@Test
	public void accessoryShowsCoreAttackSeparatelyFromRegularAndLegendaryAffixes() {
		final RPObject object = ItemTestHelper.createItem("pierścień z mithrilu");
		object.put("class", "ring");
		object.put("rarity_id", "legendary");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(object, ItemTooltip.ATTACK, "3");
		put(object, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "1");
		put(object, ItemTooltip.LEGENDARY_RELIC_POWER, "5");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+3 ataku"));
		assertTrue(tooltip.contains("+1 dodatkowego ataku"));
		assertTrue(tooltip.contains("Relikt Mocy:"));
		assertTrue(tooltip.contains("pkt. dodatkowego ataku."));
		assertFalse(tooltip.contains("ataku z affixu"));
		assertFalse(tooltip.contains("+9 ataku"));
		assertTrue(tooltip.indexOf("+3 ataku")
				< tooltip.indexOf("+1 dodatkowego ataku"));
		assertTrue(tooltip.indexOf("+1 dodatkowego ataku")
				< tooltip.indexOf("Relikt Mocy:"));
	}

	@Test
	public void flatDefenseAndBastionUseAdditionalDefenseWording() {
		final RPObject object = ItemTestHelper.createItem("pancerz legendarny");
		object.put("class", "armor");
		object.put("rarity_id", "legendary");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		put(object, ItemTooltip.DEFENSE, "28");
		put(object, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS, "4");
		put(object, ItemTooltip.LEGENDARY_BASTION_BONUS, "10");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("28 pkt. pancerza"));
		assertTrue(tooltip.contains("+4 dodatkowego pancerza"));
		assertTrue(tooltip.contains("Niezłomny Bastion:"));
		assertTrue(tooltip.contains("pkt. dodatkowego pancerza."));
		assertFalse(tooltip.contains("pancerza z affixu"));
	}

	private void put(final RPObject object, final String key, final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
