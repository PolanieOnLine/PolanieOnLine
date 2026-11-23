package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.Test;

import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;

/** Selected inventory rows share the merchant's subtle background highlight. */
public class ServiceItemSelectionTest {
	@Test
	public void selectedGlyphAndAffixItemsPaintTheSameHighlightAsTheShop() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final LookAndFeel previous = UIManager.getLookAndFeel();
			final Object previousStyle = UIManager.get("StendhalStyle");
			try {
				UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)")));
				final Object shopEntry = construct("NpcShopWindow$Entry", "miecz", "10", "sword", "sword", false, "", 10L);
				final Object model = new DefaultListModel<>();
				final JList<?> shop = (JList<?>) construct("NpcShopWindow$ShopList", model);
				final BufferedImage reference = render(shop.getCellRenderer(), shop, shopEntry, true);
				assertEquals("Merchant selection is a white overlay with alpha 31", new Color(66, 57, 49).getRGB(), reference.getRGB(238, 12));
				final Object upgrade = construct("ItemUpgradeWindow");
				final ListCellRenderer<?> glyphRenderer = (ListCellRenderer<?>) construct("ItemUpgradeWindow$RecipeRenderer", upgrade);
				final Object glyph = construct("ItemUpgradeWindow$RecipeRow", "glyph", "glif siły", 1, false, "glyph_strength");
				assertHighlight(reference, render(glyphRenderer, new JList<>(), glyph, true));
				final ListCellRenderer<?> affixRenderer = (ListCellRenderer<?>) construct("AffixRerollWindow$CandidateRenderer");
				final Object item = new AffixRerollWindow.Candidate("bag/1", "miecz", "sword", "sword", "epic", "3");
				assertHighlight(reference, render(affixRenderer, new JList<>(), item, true));
				final JList<?> lockedItems = new JList<>();
				lockedItems.setEnabled(false);
				assertHighlight(reference, render(affixRenderer, lockedItems, item, true));
				assertEquals("Unselected glyph rows must leave the window background visible", new Color(40, 30, 20).getRGB(), render(glyphRenderer, new JList<>(), glyph, false).getRGB(238, 12));
				final JLabel glyphLabel = (JLabel) glyphRenderer;
				assertEquals("Unavailable unselected glyph text stays dimmed", 120, glyphLabel.getForeground().getAlpha());
				final long dimmedIcon = iconAlpha(glyphLabel);
				render(glyphRenderer, new JList<>(), glyph, true);
				assertEquals("Selected glyph text stays readable", 255, glyphLabel.getForeground().getAlpha());
				final long selectedIcon = iconAlpha(glyphLabel);
				assertTrue("Unavailable unselected glyph icons retain their 45% opacity", dimmedIcon > selectedIcon * 0.40 && dimmedIcon < selectedIcon * 0.50);
			} catch (Exception error) {
				throw new AssertionError(error);
			} finally {
				try { UIManager.setLookAndFeel(previous); } catch (Exception error) { throw new AssertionError(error); }
				UIManager.put("StendhalStyle", previousStyle);
			}
		});
	}

	private static long iconAlpha(final JLabel label) {
		final BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		label.getIcon().paintIcon(label, graphics, 0, 0);
		graphics.dispose();
		long alpha = 0;
		for (int y = 0; y < 18; y++) for (int x = 0; x < 18; x++) alpha += image.getRGB(x, y) >>> 24;
		return alpha;
	}

	private static void assertHighlight(final BufferedImage reference, final BufferedImage actual) {
		for (int y = 0; y < actual.getHeight(); y++) {
			assertEquals("Selected row background at y=" + y, reference.getRGB(238, y), actual.getRGB(238, y));
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static BufferedImage render(final ListCellRenderer renderer, final JList<?> list, final Object value, final boolean selected) {
		final Component row = renderer.getListCellRendererComponent(list, value, 0, selected, false);
		row.setSize(240, 46);
		layout((Container) row);
		final BufferedImage image = new BufferedImage(240, 46, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(40, 30, 20));
		graphics.fillRect(0, 0, 240, 46);
		row.paint(graphics);
		graphics.dispose();
		return image;
	}

	private static void layout(final Container parent) {
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
	}

	static Object construct(final String name, final Object... arguments) throws Exception {
		for (Constructor<?> constructor : Class.forName("games.stendhal.client.gui." + name).getDeclaredConstructors()) {
			if (constructor.getParameterCount() == arguments.length) {
				constructor.setAccessible(true);
				return constructor.newInstance(arguments);
			}
		}
		throw new AssertionError("No constructor for " + name);
	}
}
