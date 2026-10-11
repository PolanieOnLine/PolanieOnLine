package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
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

/** Checks the themed upgrade layout and the existing selection safeguards. */
public class ItemUpgradeWindowTest {
	private ItemUpgradeWindow window;
	private LookAndFeel previousLookAndFeel;
	private Object previousStyle;

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		onEdt(() -> {
			previousLookAndFeel = UIManager.getLookAndFeel();
			previousStyle = UIManager.get("StendhalStyle");
			UIManager.setLookAndFeel(new StyledLookAndFeel(
					StyleFactory.createStyle("Drewno (default)")));
			final Constructor<ItemUpgradeWindow> constructor =
					ItemUpgradeWindow.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			window = constructor.newInstance();
		});
	}

	@After
	public void tearDown() throws Exception {
		if (previousLookAndFeel != null) {
			onEdt(() -> {
				UIManager.setLookAndFeel(previousLookAndFeel);
				UIManager.put("StendhalStyle", previousStyle);
			});
		}
	}

	@Test
	public void emptyWindowFitsItsContentsAndDoesNotEnableUpgrade() throws Exception {
		onEdt(() -> {
			apply(empty());
			assertTrue(window.getHeight() < 450);
			assertEquals(window.getPreferredSize(), window.getSize());
			assertFalse(button("upgrade").isEnabled());
			assertFalse(button("upgrade").isFocusable());
			assertFalse(button("refresh").isFocusable());
			assertTrue(label("chance").getText().contains("brak danych"));
		});
	}

	@Test
	public void sectionFramesUseTheSharedTheme() throws Exception {
		onEdt(() -> {
			final Container content = (Container) ((BorderLayout) window.getLayout())
					.getLayoutComponent(BorderLayout.CENTER);
			assertEquals(4, checkSectionFrames(content));
			assertEquals(new Dimension(48, 48),
					((JComponent) field("icon")).getPreferredSize());
		});
	}

	@Test
	public void materialRowsGrowAndShrinkWithoutChangingSlotSize() throws Exception {
		onEdt(() -> {
			apply(empty());
			final int emptyHeight = window.getHeight();
			apply(selected());
			final JPanel materials = (JPanel) field("materials");
			assertEquals(4, materials.getComponentCount());
			assertEquals(3, ((GridLayout) materials.getLayout()).getColumns());
			final int rowHeight = materials.getComponent(0).getPreferredSize().height;
			assertTrue(materials.getPreferredSize().height >= 2 * rowHeight);
			assertTrue(window.getHeight() > emptyHeight);
			layout(window);
			for (final Component card : materials.getComponents()) {
				assertTrue(card.getY() + card.getHeight() <= materials.getHeight());
				assertEquals(new Dimension(40, 40), findMaterialIcon((Container) card).getPreferredSize());
			}
			assertTrue(allText(materials).contains("5 / 5"));
			assertTrue(allText(materials).contains("sztabka ciemnomithrilu"));
			apply(empty());
			assertEquals(emptyHeight, window.getHeight());
		});
	}

	@Test
	public void upgradeStillRequiresPermissionAndARequestToken() throws Exception {
		onEdt(() -> {
			final RPEvent event = selected();
			apply(event);
			assertTrue(button("upgrade").isEnabled());
			event.remove("request_token");
			apply(event);
			assertFalse(button("upgrade").isEnabled());
			event.put("request_token", "fixture");
			event.put("can_upgrade", 0);
			apply(event);
			assertFalse(button("upgrade").isEnabled());
		});
	}

	@Test
	public void outdatedResponsesCannotReplaceTheChosenItem() throws Exception {
		onEdt(() -> {
			apply(selected());
			final String name = label("itemName").getText();
			final RPEvent outdated = selected();
			outdated.put("phase", "preview");
			outdated.put("selected_path", "bag/2");
			outdated.put("name", "other item");
			apply(outdated);
			assertEquals(name, label("itemName").getText());
			outdated.put("phase", "refresh");
			outdated.put("selected_path", "bag/1");
			apply(outdated);
			assertEquals(name, label("itemName").getText());
		});
	}

	@Test
	public void walkingAwayPreservesThePreviewButDisablesUpgrade() throws Exception {
		onEdt(() -> {
			apply(selected());
			final String name = label("itemName").getText();
			final RPEvent event = new RPEvent("upgrade_layout_fixture");
			event.put("phase", "preview");
			event.put("status", "NPC_TOO_FAR");
			apply(event);
			assertEquals(name, label("itemName").getText());
			assertEquals(4, ((JPanel) field("materials")).getComponentCount());
			assertFalse(button("upgrade").isEnabled());
			assertTrue(label("status").getText().contains("w pobliżu kowala"));
		});
	}

	@Test
	public void longMessagesWrapAndNamesCannotInjectMarkup() throws Exception {
		onEdt(() -> {
			final RPEvent event = selected();
			event.put("message", "Brakuje materiałów. Wróć do kowala, gdy zbierzesz wszystkie potrzebne przedmioty i monety. <test> & koniec.");
			event.put("name", "Przedmiot <test> & koniec");
			apply(event);
			assertTrue(label("status").getPreferredSize().width <= 390);
			assertTrue(label("status").getText().contains("&lt;test&gt; &amp;"));
			assertTrue(label("itemName").getText().contains("&lt;test&gt; &amp;"));
		});
	}

	@Test
	public void visibleLabelsUsePlainPolishWords() throws Exception {
		onEdt(() -> {
			apply(selected());
			assertTrue(allText((Container) field("stats")).contains("Obrona"));
			assertTrue(allText((Container) field("stats")).contains("-&gt;"));
			final String text = allText(window);
			for (final String unwanted : new String[] {"DEF", "ATK", "→", "—", "–", "✓", "✗"}) {
				assertFalse(unwanted, text.contains(unwanted));
			}
		});
	}

	private static RPEvent empty() {
		final RPEvent event = new RPEvent("upgrade_layout_fixture");
		event.put("phase", "open");
		event.put("npc_id", 1);
		event.put("status", "SELECT_ITEM");
		return event;
	}

	private static RPEvent selected() {
		final RPEvent event = empty();
		event.put("status", "READY");
		event.put("can_upgrade", 1);
		event.put("request_token", "fixture");
		event.put("selected_path", "bag/1");
		event.put("name", "Zwykłe spodnie kolcze");
		event.put("class", "legs");
		event.put("subclass", "chain_legs");
		event.put("upgrade_level", 0);
		event.put("max_upgrade_level", 1);
		event.put("success_percent", 100);
		event.put("fee_text", "69600 monet");
		event.put("stat_names", Arrays.asList("def"));
		event.put("current_stat_values", Arrays.asList("4"));
		event.put("upgraded_stat_values", Arrays.asList("5"));
		event.put("material_names", Arrays.asList("polano", "szafir", "sztabka ciemnomithrilu", "ruda żelaza"));
		event.put("material_values", Arrays.asList("5", "1", "12", "100"));
		event.put("owned_material_values", Arrays.asList("5", "0", "0", "123"));
		return event;
	}

	private Object field(final String name) throws Exception {
		final Field field = ItemUpgradeWindow.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(window);
	}

	private JLabel label(final String name) throws Exception {
		return (JLabel) field(name);
	}

	private JButton button(final String name) throws Exception {
		return (JButton) field(name);
	}

	private void apply(final RPEvent event) throws Exception {
		final Method apply = ItemUpgradeWindow.class.getDeclaredMethod("apply", RPEvent.class);
		apply.setAccessible(true);
		apply.invoke(window, event);
	}

	private static int checkSectionFrames(final Container container) {
		int count = 0;
		for (final Component child : container.getComponents()) {
			if (child instanceof JPanel) {
				final JPanel panel = (JPanel) child;
				assertFalse(panel.isOpaque());
				if (panel.getBorder() instanceof CompoundBorder) {
					final CompoundBorder border = (CompoundBorder) panel.getBorder();
					if (border.getOutsideBorder() instanceof TitledBorder) {
						final TitledBorder title = (TitledBorder) border.getOutsideBorder();
						assertSame(StyleUtil.getStyle().getBorderDown(), title.getBorder());
						assertEquals(StyleUtil.getStyle().getForeground(), title.getTitleColor());
						count++;
					}
				}
			}
			if (child instanceof Container) {
				count += checkSectionFrames((Container) child);
			}
		}
		return count;
	}

	private static Component findMaterialIcon(final Container container) {
		for (final Component child : container.getComponents()) {
			if (child.getClass().getSimpleName().equals("MaterialIcon")) {
				return child;
			}
			if (child instanceof Container) {
				final Component found = findMaterialIcon((Container) child);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static void layout(final Container container) {
		container.doLayout();
		for (final Component child : container.getComponents()) {
			if (child instanceof Container) layout((Container) child);
		}
	}

	private static String allText(final Container container) {
		final StringBuilder result = new StringBuilder();
		for (final Component child : container.getComponents()) {
			if (child instanceof JLabel) result.append(((JLabel) child).getText());
			if (child instanceof Container) result.append(allText((Container) child));
		}
		return result.toString();
	}

	private interface CheckedRunnable {
		void run() throws Exception;
	}

	private static void onEdt(final CheckedRunnable runnable) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			try {
				runnable.run();
			} catch (final Exception exception) {
				throw new RuntimeException(exception);
			}
		});
	}
}
