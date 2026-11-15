package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.client.util.UserInterfaceTestHelper;

public class CorpseLootWindowTest {
	@BeforeClass
	public static void init() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void longNamesDoNotStretchTheItemSlots() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			CorpseLootWindow window = new CorpseLootWindow(2, 2);
			window.setLootTitle("dwugłowy zielony smok bagienny");
			layout(window);
			checkSlotSizes(window);
			Dimension size = window.getSize();
			window.setLootTitle("pająk");
			assertEquals(size, window.getSize());
		});
	}

	@Test
	public void draggingTheNameMovesTheWindow() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			JLayeredPane host = new JLayeredPane();
			host.setSize(400, 300);
			CorpseLootWindow window = new CorpseLootWindow(2, 2);
			host.add(window);
			window.setLootTitle("dwugłowy zielony smok bagienny");
			window.setLocation(20, 20);
			layout(window);
			Component label = window.titleLabel;
			label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_PRESSED, 1, 0,
					5, 5, 1, false, MouseEvent.BUTTON1));
			label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_DRAGGED, 2,
					MouseEvent.BUTTON1_DOWN_MASK, 25, 15, 0, false, MouseEvent.NOBUTTON));
			label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_RELEASED, 3, 0,
					25, 15, 1, false, MouseEvent.BUTTON1));
			assertEquals(new Point(40, 30), window.getLocation());
		});
	}

	private static void layout(Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				layout((Container) child);
			}
		}
	}

	private static void checkSlotSizes(Container container) {
		for (Component child : container.getComponents()) {
			if (child instanceof ItemPanel) {
				assertEquals("Loot slots must retain their native dimensions",
						child.getPreferredSize(), child.getSize());
			} else if (child instanceof Container) {
				checkSlotSizes((Container) child);
			}
		}
	}
}
