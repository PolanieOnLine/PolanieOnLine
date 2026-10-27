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
import javax.swing.Timer;
import javax.swing.JList;
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
import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

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
				final Field active = ItemUpgradeWindow.class.getDeclaredField("instance");
				active.setAccessible(true);
				if (active.get(null) == window) active.set(null, null);
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
	public void glyphWindowUsesRomanLevelsFractionalValuesAndGuaranteedUpgrade() throws Exception {
		onEdt(() -> {
			RPEvent open = empty(); open.put("phase", "open"); open.put("service", "glyph"); apply(open);
			RPEvent preview = selected(); preview.put("service", "glyph");
			preview.put("name", "glif żywej wody"); preview.put("class", "glyph");
			preview.put("subclass", "glyph_living_water");
			preview.put("upgrade_level", 1); preview.put("max_upgrade_level", 3); preview.put("next_upgrade_level", 2);
			preview.put("stat_names", Arrays.asList("regeneracja co 5 s po 10 s bez zadawania i otrzymywania obrażeń"));
			preview.put("current_stat_values", Arrays.asList("0,5%"));
			preview.put("upgraded_stat_values", Arrays.asList("0,75%")); apply(preview);
			assertTrue(window.titleLabel.getText().contains("Stół"));
			assertTrue(label("level").getText().contains("I ->"));
			assertTrue(label("chance").getText().contains("Gwarantowane"));
			assertTrue(allText((Container)field("stats")).contains("0,75%"));
			assertTrue(button("upgrade").isEnabled());
			RPEvent smith = selected(); smith.put("phase", "preview"); smith.put("message", "late smith response"); apply(smith);
			assertFalse(label("status").getText().contains("late smith response"));
		});
	}

    private Object nestedField(Object parent, String name) {
        try { Field value = parent.getClass().getDeclaredField(name); value.setAccessible(true); return value.get(parent); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    @Test
    public void glyphCatalogHasTwoCategoriesAndUnavailableRecipesStaySelectable() throws Exception {
        onEdt(() -> {
            RPEvent event = empty(); event.put("service", "glyph");
            event.put("recipe_ids", Arrays.asList("1:glif siły", "2:glif siły", "3:glif siły"));
            event.put("recipe_names", Arrays.asList("glif siły", "glif siły", "glif siły"));
            event.put("recipe_levels", Arrays.asList("1", "2", "3"));
            event.put("recipe_ready", Arrays.asList("0", "1", "0")); apply(event);
            Object create = field("creationCategory"), improve = field("upgradeCategory");
            JList<?> rows = (JList<?>) nestedField(create, "rows");
            assertEquals(1, rows.getModel().getSize());
            assertEquals(2, ((JList<?>)nestedField(improve, "rows")).getModel().getSize());
            @SuppressWarnings({"rawtypes", "unchecked"})
            Component row = ((javax.swing.ListCellRenderer)rows.getCellRenderer()).getListCellRendererComponent(rows, rows.getModel().getElementAt(0), 0, false, false);
            JButton header = (JButton) nestedField(create, "header");
            JComponent listScroll = (JComponent) nestedField(create, "scroll");
            header.doClick(); assertFalse(listScroll.isVisible()); header.doClick(); assertTrue(listScroll.isVisible());
            assertTrue(row.isEnabled()); assertTrue(row.getForeground().getAlpha() < 255);
            RPEvent preview = selected(); preview.put("service", "glyph"); preview.put("operation", "create");
            preview.put("selected_recipe", "1:glif siły"); preview.remove("selected_path"); apply(preview);
            assertEquals("Utwórz glif", button("upgrade").getText());
            assertNull(field("selectedPath")); assertTrue(button("upgrade").isEnabled());
        });
    }

    @Test
    public void glyphCategoriesUseTheSharedButtonFrameAndMaterialsUseAnimatedTiles() throws Exception {
        onEdt(() -> {
            RPEvent event = selected(); event.put("phase", "open"); event.put("service", "glyph");
            event.put("operation", "create"); event.put("selected_recipe", "1:glif siły");
            event.put("material_names", Arrays.asList("sztabka srebra"));
            event.put("material_classes", Arrays.asList("resource")); event.put("material_subclasses", Arrays.asList("silver_bar"));
            event.put("material_values", Arrays.asList("1")); event.put("owned_material_values", Arrays.asList("0")); apply(event);
            JButton header = (JButton) nestedField(field("creationCategory"), "header");
            assertSame(button("upgrade").getBorder(), header.getBorder());
            assertFalse(button("refresh").isVisible());
            Object material = findMaterialIcon((Container)field("materials"));
            Object sprite = nestedField(material, "sprite");
            assertTrue(sprite instanceof AnimatedSprite);
            assertEquals(32, ((Sprite)sprite).getWidth());
            assertEquals(32, ((Sprite)sprite).getHeight());
        });
    }

    @Test
    public void inventoryChangesInvalidateTheGlyphReceiptAndRefreshOnlyWhenContentsChange() throws Exception {
        RPObject player = new RPObject(); player.addSlot("bag");
        RPObject material = new RPObject(); material.put("id", 1); material.put("name", "fragment glifu"); material.put("quantity", 2);
        player.getSlot("bag").add(material);
        onEdt(() -> {
            Field active = ItemUpgradeWindow.class.getDeclaredField("instance"); active.setAccessible(true); active.set(null, null);
            Field opening = ItemUpgradeWindow.class.getDeclaredField("openingGlyphWindow"); opening.setAccessible(true); opening.setBoolean(null, true);
            SwingUtilities.invokeLater(() -> {
                try {
                    RPEvent event = selected(); event.put("service", "glyph"); event.put("operation", "create");
                    event.put("selected_recipe", "1:glif siły"); apply(event);
                    active.set(null, window); opening.setBoolean(null, false);
                } catch (Exception error) { throw new AssertionError(error); }
            });
            ItemUpgradeWindow.onInventoryChanged(player);
        });
        onEdt(() -> assertFalse(((Timer)field("inventoryRefresh")).isRunning()));
        material.put("quantity", 3); ItemUpgradeWindow.onInventoryChanged(player);
        onEdt(() -> {
            assertNull(field("requestToken")); assertFalse(button("upgrade").isEnabled());
            Timer timer = (Timer)field("inventoryRefresh"); assertTrue(timer.isRunning()); timer.stop();
        });
        ItemUpgradeWindow.onInventoryChanged(player);
        onEdt(() -> assertFalse(((Timer)field("inventoryRefresh")).isRunning()));
    }

    private RPEvent glyphSelectionPreview(String id, String name) {
        RPEvent event = selected(); event.put("phase", "preview"); event.put("service", "glyph");
        event.put("selected_recipe", id); event.put("operation", "create"); event.put("name", name);
        event.put("upgrade_level", 1); event.put("next_upgrade_level", 1); event.put("max_upgrade_level", 3);
        event.remove("selected_path");
        event.put("recipe_ids", Arrays.asList("1:glif siły", "1:glif tarczy"));
        event.put("recipe_names", Arrays.asList("glif siły", "glif tarczy"));
        event.put("recipe_levels", Arrays.asList("1", "1")); event.put("recipe_ready", Arrays.asList("0", "0"));
        return event;
    }

    @Test
    public void clickingAnotherGlyphKeepsItsSelectionWhileOlderResponsesArrive() throws Exception {
        onEdt(() -> {
            RPEvent open = empty(); open.put("service", "glyph"); apply(open);
            RPEvent previous = glyphSelectionPreview("1:glif siły", "glif siły"); apply(previous);
            JList<?> rows = (JList<?>)nestedField(field("creationCategory"), "rows");
            Field provider = ItemUpgradeWindow.class.getDeclaredField("npcId"); provider.setAccessible(true); provider.setInt(window, 0);
            rows.getSelectionModel().setValueIsAdjusting(true); rows.setSelectedIndex(1);
            assertEquals("Mouse press must record the new recipe before mouse release", "1:glif tarczy", field("selectedRecipe"));
            apply(previous);
            assertEquals(1, rows.getSelectedIndex()); assertEquals("1:glif tarczy", field("selectedRecipe"));
            RPEvent oldRefresh = empty(); oldRefresh.put("phase", "refresh"); oldRefresh.put("service", "glyph"); apply(oldRefresh);
            assertEquals(1, rows.getSelectedIndex()); assertEquals("1:glif tarczy", field("selectedRecipe"));
            rows.getSelectionModel().setValueIsAdjusting(false);
            apply(glyphSelectionPreview("1:glif tarczy", "glif tarczy"));
            assertTrue(label("itemName").getText().contains("glif tarczy")); assertEquals(1, rows.getSelectedIndex());
        });
    }

    @Test
    public void refreshingTheSameCatalogueDoesNotReplaceItsRowsDuringInput() throws Exception {
        onEdt(() -> {
            RPEvent open = empty(); open.put("service", "glyph"); apply(open);
            RPEvent preview = glyphSelectionPreview("1:glif siły", "glif siły"); apply(preview);
            JList<?> rows = (JList<?>)nestedField(field("creationCategory"), "rows");
            Object first = rows.getModel().getElementAt(0); apply(preview);
            assertSame("An unchanged server catalogue must preserve the rows being clicked", first, rows.getModel().getElementAt(0));
            assertEquals(0, rows.getSelectedIndex());
        });
    }

    @Test
    public void glyphRecipeAcceptsTheNextMatchingCopyAfterSuccessfulUpgrade() throws Exception {
        onEdt(() -> {
            RPEvent open = empty(); open.put("service", "glyph"); apply(open);
            RPEvent preview = selected(); preview.put("service", "glyph");
            preview.put("selected_recipe", "2:glif siły"); preview.put("operation", "upgrade");
            preview.put("upgrade_level", 1); preview.put("next_upgrade_level", 2); preview.put("max_upgrade_level", 3);
            apply(preview);
            RPEvent result = preview; result.put("phase", "result");
            result.put("status", "SUCCESS"); result.put("selected_path", "bag/2"); result.put("request_token", "next-copy");
            apply(result);
            assertEquals("bag/2", field("selectedPath")); assertEquals("next-copy", field("requestToken"));
            assertTrue(button("upgrade").isEnabled());
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
	public void changingMapClosesTheWindowAndIgnoresLatePreviews() throws Exception {
		onEdt(() -> {
			apply(selected());
			final Field active = ItemUpgradeWindow.class.getDeclaredField("instance");
			active.setAccessible(true);
			active.set(null, window);
			final Field npc = ItemUpgradeWindow.class.getDeclaredField("npcId");
			npc.setAccessible(true);
			npc.set(window, 0); // No live connection is needed for this UI test.
			final JPanel parent = new JPanel();
			parent.add(window);
			window.setVisible(true);
			assertTrue(ItemUpgradeWindow.isOpenForItemSelection());
			ItemUpgradeWindow.closeOnZoneChange();
		});
		onEdt(() -> {
			assertNull(window.getParent());
			assertFalse(ItemUpgradeWindow.isOpenForItemSelection());
		});
		final RPEvent latePreview = selected();
		latePreview.put("phase", "preview");
		ItemUpgradeWindow.show(latePreview);
		ItemUpgradeWindow.closeOnZoneChange(); // Closing twice is harmless.
		onEdt(() -> assertFalse(ItemUpgradeWindow.isOpenForItemSelection()));
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
