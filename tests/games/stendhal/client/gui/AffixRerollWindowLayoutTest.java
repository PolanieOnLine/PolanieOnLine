/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
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
	public void idleStateIsCompactAndOnlyShowsTheRollAction() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(window.getWidth() <= 700);
			assertTrue(window.getHeight() <= 560);
			assertFalse(((JButton) field("keep")).isVisible());
			assertFalse(((JButton) field("accept")).isVisible());
			assertTrue(((JButton) field("roll")).isVisible());
			layout((Container) window);
			assertTrue(((JList<?>) field("itemList")).getWidth()
					< ((JList<?>) field("affixList")).getWidth());
		});
	}

	@Test
	public void affixRowsSeparateEntriesAndEmphasizeTheirNames() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			event.put("affix_ids", Arrays.asList("accuracy_bonus", "legendary_tenacity"));
			event.put("affix_labels", Arrays.asList("Celność: +7%", "Zaciętość: każde celne trafienie wzmacnia kolejne."));
			try {
				final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
				apply.setAccessible(true);
				apply.invoke(window, event);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			@SuppressWarnings("unchecked")
			final JList<Object> list = (JList<Object>) field("affixList");
			final javax.swing.JLabel first = (javax.swing.JLabel) list.getCellRenderer()
					.getListCellRendererComponent(list, list.getModel().getElementAt(0), 0, false, false);
			assertTrue(first.getText(), first.getText().contains("&#9671;&#9671;"));
			assertTrue(first.getText(), first.getText().contains("<b>Celność:</b>"));
			assertFalse(first.getFont().isBold());
			assertTrue("Short affix rows should remain compact: " + first.getPreferredSize(),
					first.getPreferredSize().height <= 34);
			final java.awt.Insets normalInsets = first.getBorder().getBorderInsets(first);
			final javax.swing.JLabel last = (javax.swing.JLabel) list.getCellRenderer()
					.getListCellRendererComponent(list, list.getModel().getElementAt(1), 1, true, false);
			assertFalse(last.getText(), last.getText().contains("&#9671;&#9671;"));
			assertEquals(normalInsets, last.getBorder().getBorderInsets(last));
			assertFalse(((InternalWindow) window).titleLabel.getText().contains("\u2014"));
		});
	}

	@Test
	public void catalogueRowsFitWhenInventoryRequiresAScrollbar() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			final List<String> paths = new ArrayList<>();
			for (int i = 0; i < 24; i++) paths.add("bag/" + i);
			event.put("candidate_paths", paths);
			event.put("candidate_names", Collections.nCopies(24, "płaszcz z hartowanego mithrilu"));
			event.put("candidate_classes", Collections.nCopies(24, "cloak"));
			event.put("candidate_subclasses", Collections.nCopies(24, "hardened_mithril_cloak"));
			event.put("candidate_rarities", Collections.nCopies(24, "epic"));
			event.put("candidate_levels", Collections.nCopies(24, "3"));
			try {
				final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
				apply.setAccessible(true);
				apply.invoke(window, event);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			layout((Container) window);
			@SuppressWarnings("unchecked")
			final JList<AffixRerollWindow.Candidate> list = (JList<AffixRerollWindow.Candidate>) field("itemList");
			final JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, list);
			assertTrue(scroll.getVerticalScrollBar().isVisible());
			final Component cell = list.getCellRenderer().getListCellRendererComponent(list,
					list.getModel().getElementAt(0), 0, true, false);
			assertTrue(cell.getPreferredSize() + " / " + scroll.getViewport().getExtentSize(),
					cell.getPreferredSize().width <= scroll.getViewport().getExtentSize().width);
		});
	}

	@Test
	public void paidOfferKeepsTheSelectedItemAndOnlyEnablesTheChoiceButtons() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final Dimension initialSize = window.getSize();
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
			event.put("affix_labels", Arrays.asList("Zaciętość: każde celne trafienie wzmacnia kolejne."));
			event.put("offer_token", "paid-offer");
			event.put("new_label", "Zaciętość: każde celne trafienie wzmacnia kolejne.");
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
			assertEquals(initialSize, window.getSize());
			assertFalse(((JButton) field("roll")).isVisible());
			assertTrue(((JButton) field("keep")).isVisible());
			assertTrue(((JButton) field("accept")).isVisible());
			assertTrue(((Component) field("comparisonSection")).isVisible());
			assertTrue(((javax.swing.JLabel) field("replacement")).getText().contains("<b>NOWY</b>"));
			assertTrue(((javax.swing.JLabel) field("itemName")).getText().contains("miecz chaosu"));
			assertNotNull(((javax.swing.JLabel) field("itemIcon")).getIcon());
			try {
				final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
				apply.setAccessible(true);
				apply.invoke(window, new RPEvent("affix_reroll_event"));
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			assertEquals(initialSize, window.getSize());
			assertTrue(((JButton) field("roll")).isVisible());
			assertFalse(((JButton) field("keep")).isVisible());
			assertFalse(((Component) field("comparisonSection")).isVisible());
			assertFalse(((javax.swing.JLabel) field("replacement")).getText().contains("NOWY"));
			assertNull(((javax.swing.JLabel) field("itemIcon")).getIcon());
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

	private static void layout(final Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container) layout((Container) child);
		}
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
