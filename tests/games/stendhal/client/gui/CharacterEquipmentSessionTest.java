/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects;
import games.stendhal.client.entity.CharacterSessionEntityReset;
import games.stendhal.client.entity.ContentChangeListener;
import games.stendhal.client.entity.User;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class CharacterEquipmentSessionTest {
	private Character window;

	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		UserInterfaceTestHelper.initUserInterface();
	}

	@Before
	public void setup() throws Exception {
		GameObjects.createInstance(null);
		SwingUtilities.invokeAndWait(() -> window = new Character());
	}

	@After
	public void cleanup() throws Exception {
		window.resetSession();
		flush();
		CharacterSessionEntityReset.reset();
	}

	@Test
	public void queuedOldRefreshAndResetCannotOverwriteNextCharacter() throws Exception {
		final TestUser first = new TestUser("first", true);
		final TestUser next = new TestUser("next", false);
		final CountDownLatch blocked = new CountDownLatch(1);
		final CountDownLatch release = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> {
			blocked.countDown();
			try { release.await(5, TimeUnit.SECONDS); }
			catch (final InterruptedException e) { Thread.currentThread().interrupt(); }
		});
		assertTrue(blocked.await(5, TimeUnit.SECONDS));
		try {
			window.setPlayer(first);
			window.resetSession();
			window.setPlayer(next);
		} finally { release.countDown(); }
		flush();
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(hasTitle(window, "<html>next</html>"));
			assertFalse(window.isReserveWindowAvailable());
		});
		assertEquals(1, first.removedListeners);
		assertEquals(1, next.addedListeners);
		for (final ItemPanel panel : panels().values()) {
			assertSame(next, field(ItemPanel.class, panel, "parent"));
		}
	}

	@Test
	public void resetClearsEquipmentAndOldCallbacksAreIgnored() throws Exception {
		final TestUser first = new TestUser("first", true);
		final RPObject item = new RPObject();
		item.setRPClass("item");
		item.setID(new RPObject.ID(701, "equipment_test"));
		item.put("class", "misc");
		item.put("subclass", "seed");
		item.put("name", "seed");
		first.object.addSlot(new RPSlot("armor"));
		first.object.getSlot("armor").add(item);
		GameObjects.getInstance().onAdded(item);
		window.setPlayer(first);
		flush();
		assertNotNull(panels().get("armor").getEntity());
		final ContentChangeListener oldSubscription = first.listener;
		window.resetSession();
		flush();
		assertTrue(hasTitle(window, "<html>Ekwipunek</html>"));
		for (final ItemPanel panel : panels().values()) {
			assertNull(panel.getEntity());
			assertNull(field(ItemPanel.class, panel, "parent"));
		}
		final TestUser next = new TestUser("next", false);
		window.setPlayer(next);
		oldSubscription.contentAdded(first.object.getSlot("armor"));
		flush();
		assertNull(panels().get("armor").getEntity());
		assertTrue(hasTitle(window, "<html>next</html>"));
	}

	@Test
	public void reserveAvailabilityDoesNotMoveOrResizeEquipment() throws Exception {
		final TestUser first = new TestUser("first", true);
		window.setPlayer(first);
		flush();
		final Dimension[] originalSize = new Dimension[1];
		final Point[] originalHead = new Point[1];
		SwingUtilities.invokeAndWait(() -> {
			layout();
			originalSize[0] = window.getPreferredSize();
			originalHead[0] = SwingUtilities.convertPoint(panelsUnchecked().get("head"), 0, 0, window);
		});
		for (int i = 0; i < 5; i++) {
			window.resetSession();
			flush();
			assertLayout(originalSize[0], originalHead[0]);
			window.setPlayer(new TestUser("next" + i, i % 2 == 0));
			flush();
			assertLayout(originalSize[0], originalHead[0]);
		}
	}

	private void assertLayout(final Dimension size, final Point head) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			layout();
			assertEquals(size, window.getPreferredSize());
			assertEquals(head, SwingUtilities.convertPoint(panelsUnchecked().get("head"), 0, 0, window));
		});
	}

	private void layout() {
		invalidateTree(window);
		window.setSize(window.getPreferredSize());
		layoutTree(window);
	}

	private static void invalidateTree(final Component component) {
		component.invalidate();
		if (component instanceof Container) {
			for (final Component child : ((Container) component).getComponents()) { invalidateTree(child); }
		}
	}

	private static void layoutTree(final Component component) {
		if (component instanceof Container) {
			((Container) component).doLayout();
			for (final Component child : ((Container) component).getComponents()) { layoutTree(child); }
		}
	}

	private static boolean hasTitle(final Component component, final String title) {
		if (component instanceof JLabel && title.equals(((JLabel) component).getText())) { return true; }
		if (component instanceof Container) {
			for (final Component child : ((Container) component).getComponents()) {
				if (hasTitle(child, title)) { return true; }
			}
		}
		return false;
	}

	private static Object field(final Class<?> type, final Object instance, final String name) throws Exception {
		final Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(instance);
	}

	@SuppressWarnings("unchecked")
	private Map<String, ItemPanel> panels() throws Exception {
		return (Map<String, ItemPanel>) field(Character.class, window, "slotPanels");
	}

	private Map<String, ItemPanel> panelsUnchecked() {
		try { return panels(); }
		catch (final Exception e) { throw new AssertionError(e); }
	}

	private static void flush() throws Exception { SwingUtilities.invokeAndWait(() -> { }); }

	private static final class TestUser extends User {
		private final RPObject object = new RPObject();
		private ContentChangeListener listener;
		private int addedListeners;
		private int removedListeners;

		TestUser(final String name, final boolean reserve) {
			object.setRPClass("player");
			object.setID(new RPObject.ID(700, "equipment_test"));
			object.put("name", name);
			if (reserve) { object.addSlot(new RPSlot("armor_set")); }
		}
		@Override public RPObject getRPObject() { return object; }
		@Override public String getName() { return object.get("name"); }
		@Override public RPSlot getSlot(final String name) { return object.hasSlot(name) ? object.getSlot(name) : null; }
		@Override public void addContentChangeListener(final ContentChangeListener value) { listener = value; addedListeners++; }
		@Override public void removeContentChangeListener(final ContentChangeListener value) { assertSame(listener, value); removedListeners++; }
	}
}
