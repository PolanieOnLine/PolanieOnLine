package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.MockStendhalClient;
import games.stendhal.client.entity.Chest;
import games.stendhal.client.entity.Item;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class ChestQuickTransferTest {
	private RecordingClient client;
	private User user;
	private final List<SlotWindow> windows = new ArrayList<>();
	private final Map<Integer, Chest> chests = new HashMap<>();

	@BeforeClass public static void initialize() {
		MockStendlRPWorld.get();
		UserInterfaceTestHelper.initUserInterface();
	}
	@Before public void setup() {
		client = new RecordingClient();
		user = new User();
		RPObject object = object("player", 1);
		object.put("name", "transfer_player");
		object.addSlot(new RPSlot("bag"));
		object.addSlot(new RPSlot("rhand"));
		user.initialize(object);
		WtWindowManager.getInstance().setProperty("ui.doubleclick", "false");
	}
	@After public void cleanup() {
		for (SlotWindow window : windows) { window.close(); }
	}
	private RPObject object(String type, int id) {
		RPObject object = new RPObject();
		object.setRPClass(type);
		object.put("type", type);
		object.put("id", id);
		object.put("zoneid", "transfer_zone");
		object.put("x", 10); object.put("y", 10);
		return object;
	}
	private SlotWindow chest(int id) {
		RPObject object = object("chest", id);
		object.put("open", "");
		object.addSlot(new RPSlot("content"));
		Chest chest = new Chest(); chest.initialize(object);
		chests.put(id, chest);
		SlotWindow window = new SlotWindow("chest", 2, 2);
		window.setSlot(chest, "content");
		window.setVisible(true);
		windows.add(window);
		return window;
	}
	private ItemPanel itemPanel(String slot) {
		return itemPanel(user, slot);
	}
	private ItemPanel itemPanel(games.stendhal.client.entity.IEntity owner, String slot) {
		games.stendhal.server.entity.item.Item object = utilities.RPClass.ItemTestHelper.createItem("polano", 25);
		object.put("id", 17);
		owner.getRPObject().getSlot(slot).add(object);
		Item item = (Item) EntityFactory.createEntity(object);
		ItemPanel panel = new ItemPanel(slot, null);
		panel.setParent(owner); panel.setEntity(item);
		return panel;
	}
	private void click(ItemPanel panel, int modifiers) {
		for (MouseListener listener : panel.getMouseListeners()) {
			listener.mousePressed(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
					modifiers | InputEvent.BUTTON1_DOWN_MASK, 8, 8, 1, false, MouseEvent.BUTTON1));
			listener.mouseReleased(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
					modifiers, 8, 8, 1, false, MouseEvent.BUTTON1));
		}
	}
	@Test public void clickingMovesTheFullStackIntoTheOpenChest() {
		chest(40);
		click(itemPanel("bag"), 0);
		assertEquals(1, client.actions.size());
		RPAction action = client.actions.get(0);
		assertEquals("equip", action.get("type"));
		assertEquals("[1\tbag\t0]", action.get("source_path"));
		assertEquals("[40\tcontent]", action.get("target_path"));
		assertFalse(action.has("clicked"));
		assertFalse(action.has("quantity"));
	}
	@Test public void anEquippedSlotAlsoDepositsAndDoubleClickPreferenceDoesNotBlockIt() {
		chest(40);
		WtWindowManager.getInstance().setProperty("ui.doubleclick", "true");
		click(itemPanel("rhand"), 0);
		assertEquals(1, client.actions.size());
		assertEquals("[1\trhand\t0]", client.actions.get(0).get("source_path"));
	}
	@Test public void hiddenAndClosedChestsDoNotReceiveItems() {
		SlotWindow window = chest(40);
		window.setVisible(false);
		click(itemPanel("bag"), 0);
		assertTrue(client.actions.isEmpty());
	}
	@Test public void theMostRecentlyOpenedChestIsUsedAndClosingItRestoresThePreviousOne() {
		chest(40);
		SlotWindow second = chest(41);
		ItemPanel panel = itemPanel("bag");
		click(panel, 0);
		assertEquals("[41\tcontent]", client.actions.get(0).get("target_path"));
		second.close();
		click(panel, 0);
		assertEquals("[40\tcontent]", client.actions.get(1).get("target_path"));
	}
	@Test public void otherContainerContentsDepositButTheDestinationContentsArePickedUp() {
		chest(40);
		chest(41);
		click(itemPanel(chests.get(40), "content"), 0);
		assertEquals("[41\tcontent]", client.actions.get(0).get("target_path"));
		click(itemPanel(chests.get(41), "content"), 0);
		assertEquals("[1\tbag]", client.actions.get(1).get("target_path"));
		assertTrue(client.actions.get(1).has("clicked"));
	}
	@Test public void modifiersPreserveNormalInventoryClicks() {
		chest(40);
		ItemPanel panel = itemPanel("bag");
		for (int modifier : new int[] {InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
				InputEvent.ALT_DOWN_MASK, InputEvent.META_DOWN_MASK}) {
			click(panel, modifier);
		}
		assertTrue(client.actions.isEmpty());
	}
	@Test public void groundAndStaleSourcesAreNeverDeposited() {
		chest(40);
		ItemPanel panel = itemPanel("bag");
		Item item = (Item) panel.getEntity();
		user.getRPObject().getSlot("bag").remove(item.getRPObject().getID());
		item.getRPObject().put("zoneid", "transfer_zone");
		assertNull(ChestQuickTransfer.createAction(item, user));
	}
	@Test public void chestsFromAnotherZoneCharacterOrOutsideReachAreIgnored() {
		chest(40);
		ItemPanel panel = itemPanel("bag");
		Chest chest = chests.get(40);
		chest.getRPObject().put("zoneid", "elsewhere");
		assertNull(ChestQuickTransfer.createAction(panel.getEntity(), user));
		chest.getRPObject().put("zoneid", "transfer_zone");
		chest.getRPObject().put("x", 90); chest.initialize(chest.getRPObject());
		assertNull(ChestQuickTransfer.createAction(panel.getEntity(), user));
		chest.getRPObject().put("x", 10); chest.initialize(chest.getRPObject());
		assertNotNull(ChestQuickTransfer.createAction(panel.getEntity(), user));
		User other = new User(); other.initialize(user.getRPObject());
		assertNull(ChestQuickTransfer.createAction(panel.getEntity(), other));
	}
	@Test public void closingTheChestDuringThePressDoesNotUseTheItem() {
		SlotWindow window = chest(40);
		ItemPanel panel = itemPanel("bag");
		for (MouseListener listener : panel.getMouseListeners()) {
			listener.mousePressed(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
					InputEvent.BUTTON1_DOWN_MASK, 8, 8, 1, false, MouseEvent.BUTTON1));
		}
		window.close();
		for (MouseListener listener : panel.getMouseListeners()) {
			listener.mouseReleased(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
					0, 8, 8, 1, false, MouseEvent.BUTTON1));
		}
		assertTrue(client.actions.isEmpty());
	}
	@Test public void addingAModifierBeforeReleasingCancelsQuickDeposit() {
		chest(40);
		ItemPanel panel = itemPanel("bag");
		for (int modifier : new int[] {InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
				InputEvent.ALT_DOWN_MASK, InputEvent.META_DOWN_MASK}) {
			for (MouseListener listener : panel.getMouseListeners()) {
				listener.mousePressed(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
						InputEvent.BUTTON1_DOWN_MASK, 8, 8, 1, false, MouseEvent.BUTTON1));
				listener.mouseReleased(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
						modifier, 8, 8, 1, false, MouseEvent.BUTTON1));
			}
		}
		assertTrue(client.actions.isEmpty());
	}
	private static class RecordingClient extends MockStendhalClient {
		final List<RPAction> actions = new ArrayList<>();
		@Override public void send(RPAction action) { actions.add(action); }
	}
}
