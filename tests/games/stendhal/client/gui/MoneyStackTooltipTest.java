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
		RPObject object = ItemTestHelper.createItem(name, quantity);
		object.put("class", "money".equals(name) ? "money" : "resource");
		object.put("subclass", "money".equals(name) ? "gold" : "wood");
		return (Item) EntityFactory.createEntity(object);
	}

	@Test
	public void moneyShowsAllDigitsWithoutRequiringRarityOrTooltipStats() {
		assertEquals("<html>Monety w stosie: <b>992 550 648</b></html>",
				ItemRarityPresentation.buildItemToolTip(item("money", 992550648)));
		assertEquals("<html>Monety w stosie: <b>2 147 483 647</b></html>",
				ItemRarityPresentation.buildItemToolTip(item("money", Integer.MAX_VALUE)));
	}

	@Test
	public void smallAmountsAreAlsoExactAndOtherStacksRemainUnchanged() {
		assertEquals("<html>Monety w stosie: <b>1</b></html>",
				ItemRarityPresentation.buildItemToolTip(item("money", 1)));
		assertNull(ItemRarityPresentation.buildMoneyToolTip(item("polano", 992550648)));
		assertNull(ItemRarityPresentation.buildItemToolTip(item("polano", 992550648)));
		assertNull(ItemRarityPresentation.buildMoneyToolTip(null));
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
				assertTrue(panel.getToolTipText(hover).contains("992 550 648"));
				RPObject quantity = new RPObject();
				quantity.put("quantity", 992550348);
				coins.onChangedAdded(coins.getRPObject(), quantity);
				coins.getRPObject().applyDifferences(quantity, null);
				assertTrue(panel.getToolTipText(hover).contains("992 550 348"));
				assertFalse(panel.getToolTipText(hover).contains("992 550 648"));
				panel.setEntity(null);
				assertNull(panel.getToolTipText(hover));
			}
		});
	}
}
