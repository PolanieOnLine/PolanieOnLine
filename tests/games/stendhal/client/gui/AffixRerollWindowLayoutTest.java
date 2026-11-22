/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.BorderLayout;
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
import games.stendhal.client.MockStendhalClient;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPAction;

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

	@Test public void paymentDetailsAppearOnlyAfterChoosingAnAffix() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			assertFalse(((Component) field("cost")).isVisible());
			final RPEvent event = new RPEvent("affix_reroll_event");
			event.put("selected_path", "bag/7");
			event.put("stone_cost", 3);
			apply(event);
			assertFalse(((Component) field("cost")).isVisible());
			event.put("selected_affix", "legendary_tenacity");
			apply(event);
			assertTrue(((Component) field("cost")).isVisible());
			assertTrue(((Component) field("paymentHint")).isVisible());
			event.put("selected_affix", "");
			apply(event);
			assertFalse(((Component) field("cost")).isVisible());
			assertFalse(((Component) field("paymentHint")).isVisible());
		});
	}

	@Test public void aLaterItemClickIsQueuedInsteadOfRevertedByThePreviousReply() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final List<RPAction> actions = new ArrayList<>();
			final RPObject owner = (RPObject) field("owner");
			new MockStendhalClient() {
				@Override public RPObject getPlayer() { return owner; }
				@Override public void send(final RPAction action) { actions.add(action); }
			};
			try {
				final RPEvent event = new RPEvent("affix_reroll_event");
				event.put("candidate_paths", Arrays.asList("bag/1", "bag/2", "bag/3"));
				event.put("candidate_names", Collections.nCopies(3, "miecz chaosu"));
				event.put("candidate_classes", Collections.nCopies(3, "sword"));
				event.put("candidate_subclasses", Collections.nCopies(3, "chaos_sword"));
				event.put("candidate_rarities", Collections.nCopies(3, "rare"));
				event.put("candidate_levels", Collections.nCopies(3, "0"));
				apply(event);
				final JList<?> list = (JList<?>) field("itemList");
				list.setSelectedIndex(1);
				assertEquals(1, actions.size());
				list.setSelectedIndex(2);
				assertEquals("Do not issue parallel selections", 1, actions.size());
				event.put("selected_path", "bag/2");
				apply(event);
				assertEquals("Old replies must not undo the latest click", 2, list.getSelectedIndex());
				assertTrue("A pending selection must not block the next click", list.isEnabled());
				assertEquals(2, actions.size());
				assertEquals(Arrays.asList("bag", "3"), actions.get(1).getList("target_path"));
				event.put("selected_path", "bag/3");
				event.put("affix_ids", Arrays.asList("accuracy_bonus", "critical_chance"));
				event.put("affix_labels", Arrays.asList("Celność: +7%", "Szansa na trafienie krytyczne: +3%"));
				apply(event);
				assertEquals(2, list.getSelectedIndex());
				final JList<?> properties = (JList<?>) field("affixList");
				properties.setSelectedIndex(0);
				properties.setSelectedIndex(1);
				event.put("selected_affix", "accuracy_bonus");
				apply(event);
				assertEquals("Old replies must not undo the latest affix click", 1, properties.getSelectedIndex());
				assertEquals("critical_chance", actions.get(actions.size() - 1).get("affix_id"));
				event.put("selected_affix", "critical_chance");
				apply(event);
				assertEquals(1, properties.getSelectedIndex());
				properties.setSelectedIndex(0);
				properties.setSelectedIndex(1);
				final int requests = actions.size();
				event.put("offer_token", "existing-paid-offer");
				event.put("selected_affix", "accuracy_bonus");
				apply(event);
				assertEquals("An existing paid choice takes precedence over queued selection", 0, properties.getSelectedIndex());
				assertEquals(requests, actions.size());
				assertFalse(properties.isEnabled());
			} finally { StendhalClient.resetClient(); }
		});
	}

	@Test
	public void affixRowsSeparateEntriesAndEmphasizeTheirNames() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			event.put("affix_ids", Arrays.asList("accuracy_bonus", "legendary_tenacity"));
			event.put("affix_labels", Arrays.asList("Celność: +7%", "Zaciętość: każde trafienie zwiększa obrażenia o 3% przez 5 s."));
			try {
				final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
				apply.setAccessible(true);
				apply.invoke(window, event);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
			@SuppressWarnings("unchecked")
			final JList<Object> list = (JList<Object>) field("affixList");
			final Component row = list.getCellRenderer()
					.getListCellRendererComponent(list, list.getModel().getElementAt(0), 0, false, false);
			assertTrue("Separators must be outside the selected label", row instanceof javax.swing.JPanel);
			final javax.swing.JPanel panel = (javax.swing.JPanel) row;
			final BorderLayout rowLayout = (BorderLayout) panel.getLayout();
			final javax.swing.JLabel first = (javax.swing.JLabel) rowLayout.getLayoutComponent(BorderLayout.CENTER);
			final javax.swing.JLabel separator = (javax.swing.JLabel) rowLayout.getLayoutComponent(BorderLayout.SOUTH);
			assertTrue(separator.getText(), separator.getText().contains("&#9671;&#9671;"));
			assertTrue(separator.isVisible());
			assertFalse(separator.isOpaque());
			assertFalse(panel.isOpaque());
			assertTrue(first.getText(), first.getText().contains("<b>Celność:</b>"));
			assertFalse(first.getFont().isBold());
			assertTrue("Short affix rows should remain compact: " + row.getPreferredSize(),
					row.getPreferredSize().height <= 34);
			final java.awt.Insets normalInsets = first.getBorder().getBorderInsets(first);
			list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(1), 1, true, false);
			final javax.swing.JLabel last = first;
			assertFalse(separator.isVisible());
			assertTrue(last.isOpaque());
			assertFalse(separator.isOpaque());
			assertTrue(last.getText(), last.getText().contains("color='#f28c28'"));
			assertTrue(last.getText(), last.getText().contains("color='#f3e2b8'>3%</font>"));
			assertEquals(normalInsets, last.getBorder().getBorderInsets(last));
			assertFalse(((InternalWindow) window).titleLabel.getText().contains("\u2014"));
		});
	}

	@Test public void emptyWireAffixListsNeverCreateAnEmptySelectedRow() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			event.put("affix_ids", Collections.singletonList(""));
			event.put("affix_labels", Collections.singletonList(""));
			apply(event);
			assertEquals(0, ((JList<?>) field("affixList")).getModel().getSize());
		});
	}

	@Test public void unchangedRepliesPreserveWindowLocationAndBothScrollPositions() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final RPEvent event = new RPEvent("affix_reroll_event");
			final List<String> paths = new ArrayList<>();
			final List<String> ids = new ArrayList<>();
			for (int i = 0; i < 24; i++) {
				paths.add("bag/" + i);
				ids.add("affix_" + i);
			}
			event.put("candidate_paths", paths);
			event.put("candidate_names", Collections.nCopies(24, "płaszcz z hartowanego mithrilu"));
			event.put("candidate_classes", Collections.nCopies(24, "cloak"));
			event.put("candidate_subclasses", Collections.nCopies(24, "hardened_mithril_cloak"));
			event.put("candidate_rarities", Collections.nCopies(24, "epic"));
			event.put("candidate_levels", Collections.nCopies(24, "3"));
			event.put("selected_path", paths.get(0));
			event.put("affix_ids", ids);
			event.put("affix_labels", Collections.nCopies(24, "Celność: +7%"));
			event.put("selected_affix", ids.get(0));
			apply(event);
			layout((Container) window);
			window.setLocation(100, 80);
			final JScrollPane items = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, (Component) field("itemList"));
			final JScrollPane affixes = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, (Component) field("affixList"));
			items.getViewport().setViewPosition(new Point(0, 100));
			affixes.getViewport().setViewPosition(new Point(0, 100));
			apply(event);
			layout((Container) window);
			assertEquals(new Point(100, 80), window.getLocation());
			assertEquals(new Point(0, 100), items.getViewport().getViewPosition());
			assertEquals(new Point(0, 100), affixes.getViewport().getViewPosition());
		});
	}

	private void apply(final RPEvent event) {
		try {
			final Method apply = AffixRerollWindow.class.getDeclaredMethod("apply", RPEvent.class);
			apply.setAccessible(true);
			apply.invoke(window, event);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
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
			event.put("current_label", "Zaciętość: każde trafienie zwiększa obrażenia o 3% przez 5 s.");
			event.put("new_label", "Przełamanie: zadajesz o 15% więcej obrażeń przez 6 s.");
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
			final javax.swing.JLabel currentLabel = (javax.swing.JLabel) field("current");
			assertFalse(currentLabel.getFont().isBold());
			assertTrue(currentLabel.getText(), currentLabel.getText().contains("color='#f28c28'"));
			assertTrue(currentLabel.getText(), currentLabel.getText().contains("color='#f3e2b8'>3%</font>"));
			final String offered = ((javax.swing.JLabel) field("replacement")).getText();
			assertTrue(offered, offered.contains("color='#f28c28'"));
			assertTrue(offered, offered.contains("color='#f3e2b8'>15%</font>"));
			assertTrue(((javax.swing.JLabel) field("itemName")).getText().contains("miecz chaosu"));
			assertNotNull(((javax.swing.JLabel) field("itemIcon")).getIcon());
			event.remove("offer_token");
			event.put("selected_affix", "");
			apply(event);
			assertEquals(1, ((JList<?>) field("affixList")).getModel().getSize());
			assertEquals("An unquoted affix must be clickable again after resolving an offer",
					-1, ((JList<?>) field("affixList")).getSelectedIndex());
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
