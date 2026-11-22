/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

/** The service must share the game window manager and its current theme. */
public class AffixRerollWindowLayoutTest {
	private Component window;
	private LookAndFeel previousLookAndFeel;
	private Object previousStyle;

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			try {
				previousLookAndFeel = UIManager.getLookAndFeel();
				previousStyle = UIManager.get("StendhalStyle");
				UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)")));
				final Constructor<?> constructor = AffixRerollWindow.class.getDeclaredConstructor(RPObject.class, RPEvent.class);
				constructor.setAccessible(true);
				final RPEvent event = new RPEvent("affix_reroll_event");
				event.put("window_id", "test-window");
				event.put("npc_id", "42");
				window = (Component) constructor.newInstance(new RPObject(), event);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		if (previousLookAndFeel == null) return;
		SwingUtilities.invokeAndWait(() -> {
			try {
				if (window instanceof InternalWindow) ((InternalWindow) window).close();
				UIManager.setLookAndFeel(previousLookAndFeel);
				UIManager.put("StendhalStyle", previousStyle);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		});
	}

	@Test
	public void serviceEmbedsAndClosesThroughTheGameWindowLifecycle() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			assertTrue("Affix service must be an in-game window", window instanceof InternalManagedWindow);
			final InternalManagedWindow internal = (InternalManagedWindow) window;
			final JLayeredPane game = new JLayeredPane();
			game.add(internal);
			assertSame(game, internal.getParent());
			assertNull(SwingUtilities.getWindowAncestor(internal));
			assertTrue(internal.closeButton.isVisible());
			assertFalse(internal.minimizeButton.isVisible());
			((Timer) field("timer")).start();
			internal.close();
			assertNull(internal.getParent());
			assertFalse(((Timer) field("timer")).isRunning());
			assertTrue((Boolean) field("closed"));
			internal.close();
		});
	}

	@Test
	public void paidOfferKeepsTheSelectedItemAndOnlyEnablesTheChoiceButtons() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			event.put("selected_path", "bag/7");
			event.put("selected_affix", "legendary_tenacity");
			event.put("rerolled_affix", "legendary_tenacity");
			event.put("candidate_paths", Arrays.asList("bag/7"));
			event.put("candidate_names", Arrays.asList("miecz chaosu"));
			event.put("candidate_classes", Arrays.asList("sword"));
			event.put("candidate_subclasses", Arrays.asList("chaos_sword"));
			event.put("candidate_rarities", Arrays.asList("legendary"));
			event.put("candidate_levels", Arrays.asList("3"));
			event.put("affix_ids", Arrays.asList("legendary_tenacity"));
			event.put("affix_labels", Arrays.asList("Zaciętość — każde celne trafienie wzmacnia kolejne."));
			event.put("offer_token", "paid-offer");
			event.put("stone_cost", "3");
			event.put("stone_count", "12");
			try {
				final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
				apply.setAccessible(true);
				apply.invoke(window, event);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			assertEquals("bag/7", field("selectedPath"));
			assertEquals("legendary_tenacity", field("selectedAffix"));
			assertFalse(((JList<?>) field("itemList")).isEnabled());
			assertFalse(((JList<?>) field("affixList")).isEnabled());
			assertFalse(((JButton) field("roll")).isEnabled());
			assertTrue(((JButton) field("keep")).isEnabled());
			assertTrue(((JButton) field("accept")).isEnabled());
		});
	}

	@Test
	public void sectionBordersAndButtonsMatchTheGameTheme() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(window instanceof InternalManagedWindow);
			assertEquals(4, countSections((Container) window));
			for (String name : new String[] {"roll", "keep", "accept", "refresh"}) {
				assertFalse(((JButton) field(name)).isFocusable());
			}
			assertFalse(((JButton) field("roll")).isEnabled());
			assertFalse(((JButton) field("accept")).isEnabled());
		});
	}

	private int countSections(final Container parent) {
		int count = 0;
		for (Component component : parent.getComponents()) {
			if (component instanceof javax.swing.JComponent) {
				final javax.swing.border.Border border = ((javax.swing.JComponent) component).getBorder();
				if (border instanceof CompoundBorder && ((CompoundBorder) border).getOutsideBorder() instanceof TitledBorder) {
					final TitledBorder title = (TitledBorder) ((CompoundBorder) border).getOutsideBorder();
					assertSame(StyleUtil.getStyle().getBorderDown(), title.getBorder());
					assertEquals(StyleUtil.getStyle().getForeground(), title.getTitleColor());
					count++;
				}
			}
			if (component instanceof Container) count += countSections((Container) component);
		}
		return count;
	}

	private Object field(final String name) {
		try {
			final Field field = AffixRerollWindow.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(window);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
