/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.settings;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.MockUserInterface;
import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import utilities.MemoryClientPersistence;

public class SettingsPagesTest {
	private WtWindowManager manager;
	private Field propertiesField;
	private Object originalProperties;
	private Map<String, List<SettingChangeListener>> listeners;
	private Map<String, List<SettingChangeListener>> originalListeners;
	private UserInterface originalInterface;
	private List<Dimension> displaySizes;
	private List<Dimension> originalDisplaySizes;
	private MemoryClientPersistence persistence;

	@Before
	@SuppressWarnings("unchecked")
	public void isolatePreferences() throws Exception {
		persistence = new MemoryClientPersistence();
		manager = WtWindowManager.getInstance();
		propertiesField = WtWindowManager.class.getDeclaredField("properties");
		propertiesField.setAccessible(true);
		originalProperties = propertiesField.get(manager);
		propertiesField.set(manager, new Properties());
		Field listenersField = WtWindowManager.class.getDeclaredField("listeners");
		listenersField.setAccessible(true);
		listeners = (Map<String, List<SettingChangeListener>>) listenersField.get(manager);
		originalListeners = new HashMap<>(listeners);
		listeners.clear();
		originalInterface = ClientSingletonRepository.getUserInterface();
		ClientSingletonRepository.setUserInterface(new MockUserInterface());
		Field sizesField = stendhal.class.getDeclaredField("displaySizes");
		sizesField.setAccessible(true);
		displaySizes = (List<Dimension>) sizesField.get(null);
		originalDisplaySizes = new ArrayList<>(displaySizes);
		if (displaySizes.isEmpty()) {
			displaySizes.add(new Dimension(800, 600));
			displaySizes.add(new Dimension(928, 644));
			displaySizes.add(new Dimension(1056, 732));
		}
	}

	@After
	public void restorePreferences() throws Exception {
		propertiesField.set(manager, originalProperties);
		listeners.clear();
		listeners.putAll(originalListeners);
		ClientSingletonRepository.setUserInterface(originalInterface);
		displaySizes.clear();
		displaySizes.addAll(originalDisplaySizes);
		persistence.close();
	}

	private SettingsPages createPages() {
		SettingsPages pages = new SettingsPages();
		new GeneralSettings(pages);
		new VisualSettings(pages);
		new SoundSettings(pages);
		return pages;
	}

	@Test public void statLimitsAreOffByDefaultAndPersistWhenEnabled() throws Exception {
		SwingUtilities.invokeAndWait(() -> assertToggleSurvivesPreferenceReload(
				"Pokazuj limity statystyk", SettingsProperties.SHOW_STAT_LIMITS_PROPERTY));
	}

	@Test public void combatStatsAccessIsOffByDefaultAndPersistsWhenEnabled() throws Exception {
		SwingUtilities.invokeAndWait(() -> assertToggleSurvivesPreferenceReload(
				"Pokazuj statystyki", SettingsProperties.SHOW_COMBAT_STATS_PROPERTY));
	}

	private void assertToggleSurvivesPreferenceReload(String label, String parameter) {
		try {
			JCheckBox toggle = check(createPages().getPage(SettingsPages.Category.COMBAT), label);
			assertFalse(toggle.isSelected());
			assertNull(persistence.savedPreferences().getProperty("config." + parameter));
			Method reload = WtWindowManager.class.getDeclaredMethod("read");
			reload.setAccessible(true);
			for (boolean enabled : new boolean[] {true, false}) {
				toggle.doClick();
				assertEquals(Boolean.toString(enabled), manager.getProperty(parameter, ""));
				assertEquals(Boolean.toString(enabled), persistence.savedPreferences().getProperty("config." + parameter));
				reload.invoke(manager);
				toggle = check(createPages().getPage(SettingsPages.Category.COMBAT), label);
				assertEquals(enabled, toggle.isSelected());
			}
		} catch (ReflectiveOperationException | IOException error) {
			throw new AssertionError(error);
		}
	}

	@Test
	public void changingDisplayLanguagePreservesSettingsAndOriginalLabels() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			JCheckBox blood = check(pages.getPage(SettingsPages.Category.APPEARANCE), "Pokaż krew i zwłoki");
			boolean selected = blood.isSelected();
			List<JSlider> sliders = find(pages, JSlider.class);
			int volume = sliders.get(0).getValue();
			manager.setProperty("ui.language", "en");
			SettingsTranslation.apply(pages);
			assertEquals("Show blood and corpses", blood.getText());
			assertEquals("Close", ClientLanguage.getText("Zamknij"));
			assertEquals("Talk", ActionType.TALK.getRepresentation());
			assertSame(ActionType.TALK, ActionType.getbyRep("Talk"));
			assertSame(ActionType.TALK, ActionType.getbyRep("Porozmawiaj"));
			CompoundBorder languageBorder = (CompoundBorder)
					pages.addGroup(SettingsPages.Category.TEXT, "Język").getBorder();
			assertEquals("Language", ((TitledBorder) languageBorder.getOutsideBorder()).getTitle());
			manager.setProperty("ui.language", "pl");
			SettingsTranslation.apply(pages);
			assertEquals("Pokaż krew i zwłoki", blood.getText());
			assertEquals(selected, blood.isSelected());
			assertEquals(volume, sliders.get(0).getValue());
			assertEquals("Język", ((TitledBorder) languageBorder.getOutsideBorder()).getTitle());
		});
	}

	@Test
	public void allExistingOptionsArePresentOnceAndInTheRightCategories() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			assertEquals(27, find(pages, JCheckBox.class).size());
			assertEquals(11, find(pages, JComboBox.class).size());
			assertEquals(6, find(pages, JSlider.class).size());
			assertNotNull(check(pages.getPage(SettingsPages.Category.CONTROLS), "Ciągły ruch"));
			assertNotNull(check(pages.getPage(SettingsPages.Category.DISPLAY), "Zapamiętaj rozmiar"));
			assertNotNull(check(pages.getPage(SettingsPages.Category.APPEARANCE), "Pokaż cienie"));
			assertNotNull(check(pages.getPage(SettingsPages.Category.TEXT), "Powiadomienie dźwiękowe"));
			assertNotNull(check(pages.getPage(SettingsPages.Category.COMBAT), "Porównuj statystyki przedmiotów"));
			assertNotNull(check(pages.getPage(SettingsPages.Category.AUDIO), "Włącz dźwięk"));
		});
	}

	@Test
	public void navigationKeepsControlsAndSavedChoicesWithoutChangingPreferences() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			manager.setProperty(SettingsProperties.SCALE_SCREEN_PROPERTY, "true");
			manager.setProperty(SettingsProperties.ITEM_TOOLTIP_COMPARISON_PROPERTY, "false");
			SettingsPages pages = createPages();
			Properties before = new Properties();
			try { before.putAll((Properties) propertiesField.get(manager)); }
			catch (IllegalAccessException error) { throw new AssertionError(error); }
			JCheckBox scale = check(pages.getPage(SettingsPages.Category.DISPLAY), "Dopasuj widok gry do okna");
			assertTrue(scale.isSelected());
			assertFalse(check(pages.getPage(SettingsPages.Category.COMBAT), "Porównuj statystyki przedmiotów").isSelected());
			for (SettingsPages.Category category : SettingsPages.Category.values()) {
				button(pages, category.getLabel()).doClick();
				assertEquals(category, pages.getSelectedCategory());
				assertTrue(button(pages, category.getLabel()).isSelected());
			}
			try { assertEquals(before, propertiesField.get(manager)); }
			catch (IllegalAccessException error) { throw new AssertionError(error); }
			assertSame(scale, check(pages.getPage(SettingsPages.Category.DISPLAY), "Dopasuj widok gry do okna"));
			scale.doClick();
			assertEquals("false", manager.getProperty(SettingsProperties.SCALE_SCREEN_PROPERTY, ""));
		});
	}

	@Test
	public void arrowKeysSwitchCategoriesAndWrapAtTheEnd() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			JButton controls = button(pages, "Sterowanie");
			Object up = controls.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
			controls.getActionMap().get(up).actionPerformed(null);
			assertEquals(SettingsPages.Category.AUDIO, pages.getSelectedCategory());
			JButton audio = button(pages, "Dźwięk");
			Object down = audio.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
			audio.getActionMap().get(down).actionPerformed(null);
			assertEquals(SettingsPages.Category.CONTROLS, pages.getSelectedCategory());
		});
	}

	@Test
	public void advancedControlsStartCollapsedAndCanBeExpanded() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			JButton advanced = button(pages, "Zaawansowane +");
			JComponent content = (JComponent) advanced.getParent().getComponent(1);
			assertFalse(content.isVisible());
			advanced.doClick();
			assertTrue(content.isVisible());
			assertEquals("Zaawansowane -", advanced.getText());
			advanced.doClick();
			assertFalse(content.isVisible());
		});
	}

	@Test
	public void mutedSoundDisablesSlidersAndValuesFollowChanges() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			manager.setProperty("sound.play", "false");
			SettingsPages pages = createPages();
			Container audio = pages.getPage(SettingsPages.Category.AUDIO);
			List<JSlider> sliders = find(audio, JSlider.class);
			for (JSlider slider : sliders) assertFalse(slider.isEnabled());
			check(audio, "Włącz dźwięk").doClick();
			for (JSlider slider : sliders) assertTrue(slider.isEnabled());
			sliders.get(0).setValue(37);
			assertEquals("37", manager.getProperty("sound.volume.master", ""));
			assertTrue(find(sliders.get(0).getParent(), JLabel.class).stream().anyMatch(label -> "37%".equals(label.getText())));
		});
	}

	@Test
	public void volumeTracksStayAlignedWhenPercentagesAndFontSizeChange() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			pages.showCategory(SettingsPages.Category.AUDIO);
			List<JSlider> sliders = find(pages.getPage(SettingsPages.Category.AUDIO), JSlider.class);
			for (int size : new int[] {12, 16, 20}) {
				setFonts(pages, size);
				pages.setSize(pages.getPreferredSize());
				Point original = null;
				int originalWidth = 0;
				for (int offset = 0; offset < 7; offset++) {
					int[] values = {0, 1, 9, 10, 37, 99, 100};
					for (int i = 0; i < sliders.size(); i++) sliders.get(i).setValue(values[(i + offset) % values.length]);
					layout(pages);
					for (JSlider slider : sliders) {
						Point position = SwingUtilities.convertPoint(slider, 0, 0, pages);
						if (original == null) { original = position; originalWidth = slider.getWidth(); }
						assertEquals("All volume tracks start in the same column (font " + size + ", offset " + offset
								+ ", value " + slider.getValue() + ", row " + slider.getParent().getBounds() + ")", original.x, position.x);
						assertEquals("Changing 0% to 100% must not move a track", originalWidth, slider.getWidth());
					}
				}
			}
		});
	}

	@Test
	public void fontSizesAndExpandedSettingsFitWithoutHorizontalClipping() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SettingsPages pages = createPages();
			button(pages, "Zaawansowane +").doClick();
			for (int fontSize : new int[] {12, 16, 20}) {
				setFonts(pages, fontSize);
				for (SettingsPages.Category category : SettingsPages.Category.values()) {
					pages.showCategory(category);
					Dimension preferred = pages.getPreferredSize();
					pages.setSize(preferred);
					layout(pages);
					assertFits(pages);
				}
			}
		});
	}

	private static void setFonts(Component component, int size) {
		component.setFont(component.getFont().deriveFont((float) size));
		if (component instanceof Container) for (Component child : ((Container) component).getComponents()) setFonts(child, size);
	}
	private static void layout(Container parent) {
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container && child.isVisible()) layout((Container) child);
	}
	private static void assertFits(Container parent) {
		for (Component child : parent.getComponents()) {
			if (!child.isVisible()) continue;
			// A viewport may vertically scroll; horizontal scrolling must never be needed.
			assertTrue(child.getClass().getName() + " extends beyond its parent", child.getX() + child.getWidth() <= parent.getWidth());
			if (child instanceof Container) assertFits((Container) child);
		}
	}
	private static JCheckBox check(Container root, String label) {
		return find(root, JCheckBox.class).stream().filter(check -> label.equals(check.getText())).findFirst().orElseThrow(() -> new AssertionError(label));
	}
	private static JButton button(Container root, String label) {
		return find(root, JButton.class).stream().filter(button -> label.equals(button.getText())).findFirst().orElseThrow(() -> new AssertionError(label));
	}
	private static <T extends Component> List<T> find(Container root, Class<T> type) {
		List<T> result = new ArrayList<>();
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) result.add(type.cast(child));
			if (child instanceof Container) result.addAll(find((Container) child, type));
		}
		return result;
	}
}
