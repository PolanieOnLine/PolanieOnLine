/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.junit.Test;
import games.stendhal.client.entity.Item;
import games.stendhal.client.entity.factory.EntityFactory;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class MoneyStackTooltipTest {
	private Item item(String name, int quantity) {
		RPObject object = ItemTestHelper.createItem(name, Math.max(1, quantity));
		object.put("quantity", quantity);
		object.put("class", "money".equals(name) ? "money" : "resource");
		object.put("subclass", "money".equals(name) ? "gold" : "wood");
		return (Item) EntityFactory.createEntity(object);
	}

	@Test
	public void moneyShowsAllDigitsWithoutRequiringRarityOrTooltipStats() {
		assertEquals("Łącznie: 992550648 monet",
				ItemRarityPresentation.buildItemToolTip(item("money", 992550648)));
		assertEquals("Łącznie: 2147483647 monet",
				ItemRarityPresentation.buildItemToolTip(item("money", Integer.MAX_VALUE)));
	}

	@Test
	public void smallAmountsAreAlsoExactAndOtherStacksRemainUnchanged() {
		assertEquals("Łącznie: 1 moneta",
				ItemRarityPresentation.buildItemToolTip(item("money", 1)));
		assertNull(ItemRarityPresentation.buildMoneyToolTip(item("polano", 992550648)));
		assertNull(ItemRarityPresentation.buildItemToolTip(item("polano", 992550648)));
		assertNull(ItemRarityPresentation.buildMoneyToolTip(null));
	}

	@Test
	public void moneyDescriptionUsesThePanelWordingAndPolishCoinForms() {
		for (String[] entry : new String[][] {{"0", "monet"}, {"1", "moneta"},
				{"2", "monety"}, {"5", "monet"}, {"12", "monet"}, {"22", "monety"}, {"101", "monet"}}) {
			assertEquals("Łącznie: " + entry[0] + " " + entry[1],
					ItemRarityPresentation.buildMoneyToolTip(item("money", Integer.parseInt(entry[0]))));
		}
	}

	@Test
	public void bagAndPouchReadQuantityAgainWhenTheAbbreviatedIconDoesNotChange() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			for (String slot : new String[] {"bag", "pouch"}) {
				Item coins = item("money", 992550648);
				ItemPanel panel = new ItemPanel(slot, null);
				panel.setParent(coins);
				panel.setEntity(coins);
				MouseEvent hover = new MouseEvent(panel, MouseEvent.MOUSE_ENTERED, 0, 0, 5, 5, 0, false);
				for (java.awt.event.MouseListener listener : panel.getMouseListeners()) listener.mouseEntered(hover);
				assertEquals("Łącznie: 992550648 monet", panel.getToolTipText(hover));
				RPObject quantity = new RPObject();
				quantity.put("quantity", 992550348);
				coins.onChangedAdded(coins.getRPObject(), quantity);
				coins.getRPObject().applyDifferences(quantity, null);
				assertEquals("Łącznie: 992550348 monet", panel.getToolTipText(hover));
				panel.setEntity(null);
				assertNull(panel.getToolTipText(hover));
			}
		});
	}
}
