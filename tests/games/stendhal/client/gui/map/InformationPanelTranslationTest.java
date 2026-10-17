package games.stendhal.client.gui.map;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.junit.*;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.Translation;
import utilities.MemoryClientPersistence;

public class InformationPanelTranslationTest {
 private Field preferences;
 private Object previous;
 private MemoryClientPersistence persistence;
 private Properties values;
 private InformationPanel panel;
 private JTextPane name;

 @Before public void setup() throws Exception {
  persistence = new MemoryClientPersistence();
  preferences = WtWindowManager.class.getDeclaredField("properties");
  preferences.setAccessible(true);
  previous = preferences.get(WtWindowManager.getInstance());
  values = new Properties(); values.setProperty("config." + ClientLanguage.PROPERTY, "en");
  preferences.set(WtWindowManager.getInstance(), values);
  SwingUtilities.invokeAndWait(() -> panel = new InformationPanel());
  Field field = InformationPanel.class.getDeclaredField("nameField"); field.setAccessible(true);
  name = (JTextPane) field.get(panel);
 }
 @After public void teardown() throws Exception {
  preferences.set(WtWindowManager.getInstance(), previous); persistence.close();
 }
 private void display(String source, String expected) throws Exception {
  SwingUtilities.invokeAndWait(() -> panel.setZoneName(source));
  assertEquals(expected, name.getText());
 }
 @Test public void minimapDisplaysEnglishBuildingAndSignedLevels() throws Exception {
  display("Semos tavern 0, budynek", "Semos tavern 0, building");
  display("Wieliczka salt mines W, poziom -1", "Wieliczka salt mines W, level -1");
  display("Kikareukin clouds, poziom 7", "Kikareukin clouds, level 7");
  display("Koscielisko NE, świat alternatywny", "Koscielisko NE, alternate world");
 }
 @Test public void languageRefreshRestoresOriginalWithoutAnotherZoneChange() throws Exception {
  display("Semos tavern 0, budynek", "Semos tavern 0, building");
  SwingUtilities.invokeAndWait(() -> {
   values.setProperty("config." + ClientLanguage.PROPERTY, "pl"); UiTranslation.apply(panel);
  });
  assertEquals("Semos tavern 0, budynek", name.getText());
  SwingUtilities.invokeAndWait(() -> {
   values.setProperty("config." + ClientLanguage.PROPERTY, "en"); UiTranslation.apply(panel);
  });
  assertEquals("Semos tavern 0, building", name.getText());
 }
 @Test public void zonePrefixRemainsOpaqueEvenWhenItMatchesAUiTranslation() throws Exception {
  display("Plecak, budynek", "Plecak, building");
  assertEquals("Plecak, level -12", Translation.getText("en", "Plecak, poziom -12"));
  assertEquals("Plecak, poziom -12", Translation.getText("pl", "Plecak, poziom -12"));
 }
}
