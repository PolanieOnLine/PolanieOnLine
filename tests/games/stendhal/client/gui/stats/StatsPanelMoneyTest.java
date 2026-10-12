package games.stendhal.client.gui.stats;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.constants.CurrencyReform;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Checks money through the same slot events used by UserController. */
public class StatsPanelMoneyTest {

	private StatsPanelController controller;
	private PropertyChangeSupport changes;

	@Before
	public void setUp() throws Exception {
		controller = StatsPanelController.get();
		changes = new PropertyChangeSupport(this);
		controller.registerListeners(changes);
		changes.getPropertyChangeListeners("bag")[0].propertyChange(null);
	}

	private RPObject item(int id, String name, int quantity, int value) {
		RPObject item = delta(id);
		item.put("name", name);
		item.put("quantity", quantity);
		item.put("value", value);
		return item;
	}

	private RPObject delta(int id) {
		RPObject object = new RPObject();
		object.put("id", id);
		return object;
	}

	private void change(String slotName, boolean added, RPObject... items) {
		RPSlot slot = new RPSlot(slotName);
		new RPObject().addSlot(slot);
		for (RPObject item : items) {
			slot.addPreservingId(item);
		}
		changes.firePropertyChange(slotName, added ? null : slot, added ? slot : null);
	}

	private void assertMoney(long copper) throws Exception {
		SwingUtilities.invokeAndWait(() -> { });
		Field moneyField = StatsPanel.class.getDeclaredField("moneyPanel");
		moneyField.setAccessible(true);
		Object moneyPanel = moneyField.get(controller.getComponent());
		Field labelField = moneyPanel.getClass().getDeclaredField(
			CurrencyReform.useReformedCurrency() ? "copperLabel" : "legacyLabel");
		labelField.setAccessible(true);
		assertEquals(Long.toString(copper), ((JLabel) labelField.get(moneyPanel)).getText());
	}

	@Test
	public void itemPricesMustNotBecome1071PhantomCoins() throws Exception {
		change("bag", true, item(1, "eliksir", 20, 30), item(2, "polano", 71, 1),
			item(3, "szmaragd", 2, 200));
		assertMoney(0);
		for (RPObject update : new RPObject[] {
			item(1, "eliksir", 20, 30), item(2, "polano", 71, 1), item(3, "szmaragd", 2, 200) }) {
			update.remove("name");
			change("bag", true, update);
		}
		assertMoney(0);
	}

	@Test
	public void sellingLogAndEmeraldShowsTheActual201Coins() throws Exception {
		change("bag", true, item(1, "polano", 71, 1), item(2, "szmaragd", 3, 200));
		RPObject log = delta(1);
		log.put("quantity", 70);
		change("bag", true, log, item(3, "money", 1, 1));
		assertMoney(1);
		RPObject emerald = delta(2);
		emerald.put("quantity", 2);
		emerald.put("value", 200);
		RPObject coins = delta(3);
		coins.put("quantity", 201);
		change("bag", true, emerald, coins);
		assertMoney(201);
	}

	@Test
	public void changingCoinSaleValueDoesNotChangeItsDenomination() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		RPObject update = delta(1);
		update.put("value", 100);
		change("bag", true, update);
		assertMoney(201);
	}

	@Test
	public void removingAnUnrelatedCoinAttributeDoesNotRemoveTheStack() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		RPObject update = delta(1);
		update.put("value", 1);
		change("bag", false, update);
		assertMoney(201);
	}

	@Test
	public void droppingTheLastCoinsClearsTheCounter() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		change("bag", false, delta(1));
		assertMoney(0);
	}

	@Test
	public void splittingAndMovingMoneyBetweenSlotsDoesNotDuplicateIt() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		RPObject update = delta(1);
		update.put("quantity", 101);
		change("bag", true, update);
		change("pouch", true, item(1, "money", 100, 1));
		assertMoney(201);
		change("bag", false, delta(1));
		update = delta(1);
		update.put("quantity", 201);
		change("pouch", true, update);
		assertMoney(201);
	}

	@Test
	public void reusedObjectIdDoesNotTurnEquipmentIntoMoney() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		change("bag", false, delta(1));
		change("bag", true, item(1, "tarcza", 1, 10000));
		RPObject update = delta(1);
		update.put("quantity", 1);
		update.put("value", 10000);
		change("bag", true, update);
		assertMoney(0);
	}

	@Test
	public void onlyCarriedMoneyCounts() throws Exception {
		for (String slot : new String[] { "ground", "bank", "reserve", "trade" }) {
			change(slot, true, item(1, "money", 2000, 1));
		}
		for (String slot : new String[] { "bag", "pouch", "lhand", "rhand" }) {
			change(slot, true, item(1, "money", 10, 1));
		}
		assertMoney(40);
	}

	@Test
	public void playerRemovalResetsAllSlots() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		for (java.beans.PropertyChangeListener listener : changes.getPropertyChangeListeners("bag")) {
			listener.propertyChange(null);
		}
		assertMoney(0);
		change("bag", true, item(1, "polano", 1, 1));
		assertMoney(0);
	}

	@Test
	public void zeroAndNegativeQuantitiesDoNotCount() throws Exception {
		change("bag", true, item(1, "money", 0, 1), item(2, "money", -5, 1));
		assertMoney(0);
	}

	@Test
	public void multipleLargeStacksDoNotOverflow() throws Exception {
		change("bag", true, item(1, "money", Integer.MAX_VALUE, 1));
		change("pouch", true, item(1, "money", Integer.MAX_VALUE, 1));
		assertMoney(2L * Integer.MAX_VALUE);
	}

	@Test
	public void denominationConversionDoesNotOverflow() throws Exception {
		change("bag", true, item(1, "dukat", Integer.MAX_VALUE, 10000));
		long total = 10000L * Integer.MAX_VALUE;
		assertMoney(CurrencyReform.useReformedCurrency() ? 0 : total);
		Field moneyField = StatsPanel.class.getDeclaredField("moneyPanel");
		moneyField.setAccessible(true);
		javax.swing.JComponent panel = (javax.swing.JComponent) moneyField.get(controller.getComponent());
		org.junit.Assert.assertTrue(panel.getToolTipText().contains(Long.toString(total)));
	}

	@Test
	public void inventoryEventsAreNotMutatedByTheCounter() throws Exception {
		RPObject carried = item(1, "money", 201, 1);
		change("bag", true, carried);
		RPSlot originalSlot = carried.getContainerSlot();
		RPObject update = delta(1);
		update.put("quantity", 20);
		change("bag", true, update);
		assertMoney(20);
		assertEquals(201, carried.getInt("quantity"));
		org.junit.Assert.assertSame(originalSlot, carried.getContainerSlot());
		org.junit.Assert.assertFalse(update.has("name"));
	}

	@Test
	public void coinsOfDifferentDenominationsUseTheirIdentityNotItemPrices() throws Exception {
		change("bag", true, item(1, "dukat", 3, 1), item(2, "talar", 4, 1),
			item(3, "miedziak", 5, 10000), item(4, "money", 6, 100));
		assertMoney(CurrencyReform.useReformedCurrency() ? 11 : 30411);
		Field moneyField = StatsPanel.class.getDeclaredField("moneyPanel");
		moneyField.setAccessible(true);
		javax.swing.JComponent panel = (javax.swing.JComponent) moneyField.get(controller.getComponent());
		org.junit.Assert.assertTrue(panel.getToolTipText().contains("30411"));
	}

	@Test
	public void unknownPriceOnlyUpdatesCannotCreateCoins() throws Exception {
		RPObject update = delta(1);
		update.put("value", 10000);
		update.put("quantity", 2);
		change("bag", true, update);
		assertMoney(0);
	}

	@Test
	public void removingQuantityOnlyKeepsTheRemainingIdentity() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		RPObject update = delta(1);
		update.put("quantity", 201);
		change("bag", false, update);
		assertMoney(0);
		update = delta(1);
		update.put("quantity", 20);
		change("bag", true, update);
		assertMoney(20);
	}

	@Test
	public void fullInventoryAfterMapChangeDoesNotKeepOldMoney() throws Exception {
		change("bag", true, item(1, "money", 201, 1));
		changes.getPropertyChangeListeners("bag")[0].propertyChange(null);
		change("bag", true, item(1, "polano", 70, 1), item(2, "szmaragd", 2, 200));
		assertMoney(0);
		change("pouch", true, item(1, "money", 50, 1));
		assertMoney(50);
	}
}
