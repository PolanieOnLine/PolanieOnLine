package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.swing.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.buddies.BuddyPanelController;

public class LeftColumnResizeTest {
 private LookAndFeel previous;
 private Object previousStyle;
 @Before public void installStyle() throws Exception {
  previous = UIManager.getLookAndFeel(); previousStyle = UIManager.get("StendhalStyle");
  UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)")));
 }
 @After public void restoreStyle() throws Exception { UIManager.setLookAndFeel(previous); UIManager.put("StendhalStyle", previousStyle); }
 private static JComponent create(String type) throws Exception {
  Constructor<?> constructor = Class.forName(type).getDeclaredConstructor();
  constructor.setAccessible(true);
  return (JComponent) constructor.newInstance();
 }
 private static void layout(Container parent) {
  parent.invalidate(); parent.doLayout();
  for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
  parent.invalidate();
 }
 @Test public void openingBuddySearchFitsTheDockWithoutHorizontalScrolling() throws Exception {
  SwingUtilities.invokeAndWait(() -> {
   try {
    Constructor<BuddyPanelController> constructor = BuddyPanelController.class.getDeclaredConstructor();
    constructor.setAccessible(true); BuddyPanelController controller = constructor.newInstance();
    JComponent buddies = (JComponent)controller.getComponent();
    JComponent filter = (JComponent)buddies.getComponent(0);
    JToggleButton toggle = (JToggleButton)filter.getComponent(0); JTextField field = (JTextField)filter.getComponent(1);
    JList<?> list = (JList<?>)buddies.getComponent(1);
    Method add = list.getModel().getClass().getDeclaredMethod("setOnline", String.class, boolean.class); add.setAccessible(true);
    for (int i=0; i<60; i++) add.invoke(list.getModel(), "Przyjaciel" + i, i<2);
    JScrollPane scroll = (JScrollPane)new ScrolledViewport(buddies).getComponent(); scroll.setBorder(null);
    JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM); tabs.add("Przyjaciele", scroll); tabs.add("Grupa", new JPanel());
    for (int width : new int[] {180, 240, 320, 180}) {
     tabs.setSize(width, 500); for (int pass=0; pass<3; pass++) layout(tabs);
     toggle.doClick(); for (int pass=0; pass<3; pass++) layout(tabs);
     assertTrue(field.isVisible()); assertTrue(scroll.getVerticalScrollBar().isVisible());
     assertFalse("Opening search must not create a horizontal scrollbar at dock width " + width, scroll.getHorizontalScrollBar().isVisible());
     assertTrue(field.getWidth() > 0); assertTrue(field.getX() + field.getWidth() <= filter.getWidth());
     assertTrue(buddies.getWidth() <= scroll.getViewport().getExtentSize().width);
     field.setText("Przyjaciel59"); assertEquals(1, list.getModel().getSize());
     field.getActionMap().get("buddyFilterHide").actionPerformed(new ActionEvent(field, 0, "escape"));
     assertFalse(field.isVisible()); assertFalse(toggle.isSelected()); assertEquals(60, list.getModel().getSize());
    }
   } catch (Exception error) { throw new AssertionError(error); }
  });
 }
 @Test public void expandedDockCanReturnToItsOriginalMinimumWidth() throws Exception {
  SwingUtilities.invokeAndWait(() -> {
   try {
    JComponent dock = SwingClientGUI.createLeftColumn();
    JComponent info = create("games.stendhal.client.gui.map.InformationPanel");
    Method setZone = info.getClass().getDeclaredMethod("setZoneName", String.class);
    setZone.setAccessible(true); setZone.invoke(info, "Semos tavern 1, budynek");
    dock.add(info, SLayout.EXPAND_X);
    JComponent stats = create("games.stendhal.client.gui.stats.StatsPanel$StatLabel");
    ((JTextArea) stats).setText("ATK: 90 × 38 (151072)");
    dock.add(stats, SLayout.EXPAND_X);
    int minimum = dock.getMinimumSize().width;
    JPanel game = new JPanel(); game.setMinimumSize(new java.awt.Dimension(0, 0));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dock, game);
    split.setBorder(null);
    split.setSize(1300, 700); split.setDividerLocation(minimum); layout(split);
    split.setDividerLocation(600); layout(split); layout(split);
    assertEquals("Widening the dock must not raise its minimum width", minimum, dock.getMinimumSize().width);
    split.setDividerLocation(minimum); layout(split);
    assertEquals(minimum, split.getDividerLocation());
    assertEquals(minimum, dock.getWidth());
    assertTrue("The game must regain space when the dock is narrowed", game.getWidth() > 1000);
    // Repeat the larger/smaller allocations produced by maximizing and restoring.
    for (int expandedWidth : new int[] {2000, 1600, 2100}) {
     split.setSize(expandedWidth, 950); layout(split);
     split.setDividerLocation(700); layout(split);
     split.setSize(1300, 700); layout(split);
     split.setDividerLocation(minimum); layout(split);
     assertEquals(minimum, dock.getMinimumSize().width);
     assertEquals(minimum, dock.getWidth());
     assertTrue(game.getWidth() > 1000);
    }
   } catch (Exception e) { throw new AssertionError(e); }
  });
 }
}
