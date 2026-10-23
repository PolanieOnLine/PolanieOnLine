/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.listener.RPObjectChangeListener;
import games.stendhal.common.constants.CurrencyReform;
import marauroa.common.game.RPObject;

/** Exercises the real perception handler, including the ordering of private slot updates. */
public class CarriedMoneyPerceptionTest {
	@Test public void combatStatsUseMergedPrivatePerceptionsAndClearOnLogout() throws Exception {
		marauroa.common.game.RPClass schema = new marauroa.common.game.RPClass("combat_stats_perception_test");
		schema.addAttribute("combat_stats", marauroa.common.game.Definition.Type.MAP);
		player.setRPClass(schema);
		player.put("combat_stats", "resist_bleeding", "35");
		player.put("combat_stats", "resist_bleeding.limit", "60");
		perception.onPerceptionEnd((byte) 0, 1);
		SwingUtilities.invokeAndWait(() -> {});
		assertEquals("Odporność na krwawienie: 35%", combatText());
		RPObject changes = new RPObject();
		changes.setRPClass(schema);
		changes.setID(player.getID());
		changes.put("combat_stats", "resist_bleeding", "0");
		perception.onMyRPObject(changes, null);
		perception.onPerceptionEnd((byte) 0, 2);
		SwingUtilities.invokeAndWait(() -> {});
		assertEquals("Odporność na krwawienie: 0%", combatText());
		perception.onClear();
		SwingUtilities.invokeAndWait(() -> {});
		assertEquals("", combatText());
	}

	private String combatText() throws Exception {
		Object root = stats.getComponent();
		Field combat = root.getClass().getDeclaredField("combatStatsPanel");
		combat.setAccessible(true);
		java.awt.Container view = (java.awt.Container) combat.get(root);
		for (java.awt.Component child : view.getComponents()) {
			if (child instanceof javax.swing.JTextArea) return ((javax.swing.JTextArea) child).getText();
		}
		throw new AssertionError("Missing combat text");
	}
	private StatsPanelController stats;
	private UserContext context;
	private Map<RPObject.ID, RPObject> world;
	private StendhalPerceptionListener perception;
	private RPObject player;

	@Before
	public void setUp() throws Exception {
		Constructor<UserContext> constructor = UserContext.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		context = constructor.newInstance();
		world = new HashMap<>();
		RPObjectChangeListener noOp = new NoOpListener();
		perception = new StendhalPerceptionListener(new PerceptionDispatcher(),
				new RPObjectChangeDispatcher(noOp, noOp), context, world);
		stats = StatsPanelController.get();
		stats.updateCarriedMoney(null);
		player = new RPObject();
		player.setID(new RPObject.ID(1, "money-test"));
		player.put("name", "test-player");
		player.addSlot("bag");
		player.addSlot("pouch");
		world.put(player.getID(), player);
		context.setPlayer(player);
	}

	private RPObject coins(int id, int quantity) {
		RPObject coins = new RPObject();
		coins.put("id", id);
		coins.put("name", "money");
		coins.put("quantity", quantity);
		return coins;
	}

	private RPObject update(String slot, RPObject item) {
		RPObject changes = new RPObject();
		changes.setID(player.getID());
		changes.addSlot(slot);
		changes.getSlot(slot).addPreservingId(item);
		return changes;
	}

	private void finishAndAssert(long amount) throws Exception {
		perception.onPerceptionEnd((byte) 0, 1);
		SwingUtilities.invokeAndWait(() -> {});
		Object statsPanel = stats.getComponent();
		Field money = statsPanel.getClass().getDeclaredField("moneyPanel");
		money.setAccessible(true);
		Object panel = money.get(statsPanel);
		Field label = panel.getClass().getDeclaredField(CurrencyReform.useReformedCurrency() ? "copperLabel" : "legacyLabel");
		label.setAccessible(true);
		assertEquals(Long.toString(amount), ((JLabel) label.get(panel)).getText());
	}

	@Test
	public void purchaseAndSaleReadTheMergedQuantityInsteadOfThePreviousPacket() throws Exception {
		player.getSlot("bag").addPreservingId(coins(7, 992550648));
		finishAndAssert(992550648);
		RPObject quantity = new RPObject();
		quantity.put("id", 7);
		quantity.put("quantity", 992550348);
		perception.onMyRPObject(update("bag", quantity), null);
		assertEquals(992550348, player.getSlot("bag").iterator().next().getInt("quantity"));
		finishAndAssert(992550348);
		quantity = new RPObject();
		quantity.put("id", 7);
		quantity.put("quantity", 992550548);
		perception.onMyRPObject(update("bag", quantity), null);
		finishAndAssert(992550548);
	}

	@Test
	public void movementToPouchUsesTheCompletedInventoryAndDroppingItShowsZero() throws Exception {
		player.getSlot("bag").addPreservingId(coins(7, 201));
		finishAndAssert(201);
		RPObject removed = new RPObject();
		removed.put("id", 7);
		perception.onMyRPObject(update("pouch", coins(3, 201)), update("bag", removed));
		assertEquals(0, player.getSlot("bag").size());
		finishAndAssert(201);
		removed = new RPObject();
		removed.put("id", 3);
		perception.onModifiedDeleted(player, update("pouch", removed));
		finishAndAssert(0);
	}

	@Test
	public void mapReplacementAndPlayerRemovalCannotLeaveThePreviousStacks() throws Exception {
		player.getSlot("bag").addPreservingId(coins(7, 992550648));
		finishAndAssert(992550648);
		perception.onClear();
		RPObject replacement = new RPObject();
		replacement.setID(player.getID());
		replacement.put("name", "test-player");
		replacement.addSlot("bag");
		replacement.getSlot("bag").addPreservingId(coins(25, 992550648));
		world.put(replacement.getID(), replacement);
		context.setPlayer(replacement);
		player = replacement;
		finishAndAssert(992550648);
		world.clear();
		finishAndAssert(0);
	}

	private static class NoOpListener implements RPObjectChangeListener {
		@Override public void onAdded(RPObject object) {}
		@Override public void onChangedAdded(RPObject object, RPObject changes) {}
		@Override public void onChangedRemoved(RPObject object, RPObject changes) {}
		@Override public void onRemoved(RPObject object) {}
		@Override public void onSlotAdded(RPObject object, String slot, RPObject item) {}
		@Override public void onSlotChangedAdded(RPObject object, String slot, RPObject item, RPObject changes) {}
		@Override public void onSlotChangedRemoved(RPObject object, String slot, RPObject item, RPObject changes) {}
		@Override public void onSlotRemoved(RPObject object, String slot, RPObject item) {}
	}
}
