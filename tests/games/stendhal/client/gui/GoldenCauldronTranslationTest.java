package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.Properties;
import javax.swing.*;
import org.junit.*;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import utilities.MemoryClientPersistence;

public class GoldenCauldronTranslationTest {
 private Field preferences;
 private Object previous;
 private MemoryClientPersistence persistence;
 private GoldenCauldronWindow window;

 @Before public void createEnglishWindow() throws Exception {
  persistence = new MemoryClientPersistence();
  preferences = WtWindowManager.class.getDeclaredField("properties");
  preferences.setAccessible(true);
  previous = preferences.get(WtWindowManager.getInstance());
  Properties values = new Properties();
  values.setProperty("config." + ClientLanguage.PROPERTY, "en");
  preferences.set(WtWindowManager.getInstance(), values);
  SwingUtilities.invokeAndWait(() -> window = new GoldenCauldronWindow("Kocioł Draconii"));
 }

 @After public void restorePreferences() throws Exception {
  if (window != null) {
   Method stop = GoldenCauldronWindow.class.getDeclaredMethod("stopCountdown");
   stop.setAccessible(true);
   SwingUtilities.invokeAndWait(() -> { try { stop.invoke(window); } catch (Exception e) { throw new AssertionError(e); } });
  }
  preferences.set(WtWindowManager.getInstance(), previous);
  persistence.close();
 }

 private Object field(String name) throws Exception {
  Field field = GoldenCauldronWindow.class.getDeclaredField(name);
  field.setAccessible(true);
  return field.get(window);
 }

 private String status() throws Exception {
  final String[] result = new String[1];
  SwingUtilities.invokeAndWait(() -> { try { result[0] = ((JLabel) field("statusLabel")).getText(); } catch (Exception e) { throw new AssertionError(e); } });
  return result[0];
 }

 @Test public void completedBrewAndPickupInstructionsAreEnglish() throws Exception {
  window.updateStatus("Wywar gotowy.", 0);
  assertEquals("The concoction is ready.", status());
  window.updateStatus("Wyjmij przygotowany wywar z kotła.", 0);
  assertEquals("Take the prepared concoction out of the cauldron.", status());
 }

 @Test public void countdownTranslatesSeparatelyAndKeepsCanonicalStateWhenLanguageChanges() throws Exception {
  long deadline = System.currentTimeMillis() + 125000;
  window.updateStatus("Kocioł nie pracuje.", deadline);
  assertTrue(status(), status().matches("The cauldron is idle\\. Time remaining: 02:\\d{2}\\."));
  SwingUtilities.invokeAndWait(() -> {
   try { ((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "pl"); }
   catch (Exception e) { throw new AssertionError(e); }
   UiTranslation.apply(window);
  });
  assertTrue(status(), status().matches("Kocioł nie pracuje\\. Pozostało 02:\\d{2}\\."));
  SwingUtilities.invokeAndWait(() -> {
   try { ((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "en"); }
   catch (Exception e) { throw new AssertionError(e); }
   UiTranslation.apply(window);
  });
  assertTrue(status(), status().matches("The cauldron is idle\\. Time remaining: 02:\\d{2}\\."));
  assertEquals("Kocioł nie pracuje.", field("baseStatus"));
  assertEquals(deadline, field("readyAt"));
 }

 @Test public void secondsAndEmptyStatusAreTranslatedWithoutChangingMixAction() throws Exception {
  window.updateStatus(null, System.currentTimeMillis() + 45000);
  assertTrue(status(), status().matches("Time remaining: \\d+ s\\."));
  final int[] clicks = {0};
  SwingUtilities.invokeAndWait(() -> {
   window.setMixAction(e -> clicks[0]++);
   try { JButton button = (JButton) field("mixButton"); assertEquals("Stir", button.getText()); button.doClick(); }
   catch (Exception e) { throw new AssertionError(e); }
  });
  assertEquals(1, clicks[0]);
  window.setMixEnabled(false);
  SwingUtilities.invokeAndWait(() -> {
   try { ((JButton) field("mixButton")).doClick(); }
   catch (Exception e) { throw new AssertionError(e); }
  });
  assertEquals(1, clicks[0]);
  window.updateStatus("Wywar gotowy.", System.currentTimeMillis() - 1);
  assertEquals("The concoction is ready.", status());
  assertNull(field("countdownTimer"));
 }

 @Test public void accessErrorsTranslateWithoutTranslatingTheBrewersName() {
  String[][] messages = {
   {"Tylko alchemicy mogą sięgać do kotła.", "Only alchemists can reach into the cauldron."},
   {"Najpierw otwórz kocioł, aby sięgnąć do środka.", "First open the cauldron to reach inside."},
   {"Poproś Draconię o pozwolenie na mieszanie składników.", "Ask Draconia for permission to mix the ingredients."},
   {"Kocioł obsługuje teraz Zamknij {0}.", "The cauldron is currently being used by Zamknij {0}."},
   {"Najpierw otwórz kocioł, aby wrzucić składniki.", "First open the cauldron to add ingredients."}
  };
  for (String[] message : messages) assertEquals(message[1], ClientLanguage.getText(message[0]));
 }
}
