package games.stendhal.client.actions;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.*;
import games.stendhal.client.*;
import games.stendhal.client.gui.*;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;
import utilities.MemoryClientPersistence;

public class HelpActionTranslationTest {
 private Field preferences;
 private Object previousPreferences;
 private UserInterface previousUi;
 private MemoryClientPersistence persistence;
 private final List<EventLine> events = new ArrayList<EventLine>();

 @Before public void captureHelpWithoutConnectingToServer() throws Exception {
  persistence = new MemoryClientPersistence();
  preferences = WtWindowManager.class.getDeclaredField("properties");
  preferences.setAccessible(true);
  previousPreferences = preferences.get(WtWindowManager.getInstance());
  Properties selected = new Properties();
  selected.setProperty("config." + ClientLanguage.PROPERTY, "en");
  preferences.set(WtWindowManager.getInstance(), selected);
  previousUi = ClientSingletonRepository.getUserInterface();
  ClientSingletonRepository.setUserInterface(new MockUserInterface() {
   @Override public void addEventLine(EventLine event) { events.add(event); }
  });
 }

 @After public void restoreClient() throws Exception {
  ClientSingletonRepository.setUserInterface(previousUi);
  preferences.set(WtWindowManager.getInstance(), previousPreferences);
  persistence.close();
 }

 @Test public void javaHelpTranslatesEveryLineAndRetainsExecutableCommandTokens() throws Exception {
  assertTrue(new HelpAction().execute(new String[0], ""));
  List<String> english = new ArrayList<String>();
  for (EventLine event : events) {
   assertEquals(NotificationType.CLIENT, event.getType());
   english.add(event.getText());
  }
  assertEquals("For more information, visit #https://polanieonline.eu", english.get(0));
  assertEquals("These are the most commonly used commands:", english.get(1));
  assertTrue(english.contains("- /tell <player> <message> \tSend a private message to <player>"));
  ((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "pl");
  for (int i = 0; i < events.size(); i++) {
   String polish = events.get(i).getText();
   if (!polish.equals("* SUPPORT:") && !polish.equals("* STATUS:")) {
    assertFalse("Untranslated help: " + polish, polish.equals(english.get(i)));
   }
   if (polish.startsWith("- /")) {
    assertEquals(polish.split(" ")[1], english.get(i).split(" ")[1]);
   }
  }
  assertEquals("Oto najczęściej używane komendy:", events.get(1).getText());
 }

 @Test public void helpTranslationDoesNotRewritePlayerChat() {
  EventLine chat = new EventLine("Zamknij", "Oto najczęściej używane komendy:", NotificationType.NORMAL);
  assertEquals("Oto najczęściej używane komendy:", chat.getText());
  assertEquals("Zamknij", chat.getHeader());
 }

 private List<String> translatedGmHelp(String section) throws Exception {
  assertTrue(new GMHelpAction().execute(new String[]{section}, ""));
  List<String> english = new ArrayList<String>();
  for (EventLine event : events) {
   english.add(event.getText());
   assertFalse("Polish text in help: " + event.getText(), event.getText().matches(".*[ąćęłńóśźż].*"));
  }
  ((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "pl");
  for (int i = 0; i < events.size(); i++) {
   String polish = events.get(i).getText();
   if (polish.startsWith("- /")) assertEquals(polish.split(" ")[1], english.get(i).split(" ")[1]);
   java.util.regex.Matcher script = java.util.regex.Pattern.compile("#[A-Za-z]+\\.class").matcher(polish);
   while (script.find()) assertTrue(english.get(i), english.get(i).contains(script.group()));
  }
  return english;
 }

 @Test public void gmOverviewExplainsAdministrationInEnglishWithoutRenamingCommands() throws Exception {
  List<String> lines = translatedGmHelp(null);
  assertEquals("These are the most commonly used commands for administrators and GMs:", lines.get(1));
  assertTrue(lines.contains("- /jail <player> <minutes> <reason>"));
  assertTrue(lines.contains("\t\tSummon the specified item or creature. Eligible equipment receives a random rarity unless rarity is specified."));
 }

 @Test public void alterHelpTranslatesInstructionsButPreservesTheLiteralExample() throws Exception {
  List<String> lines = translatedGmHelp("alter");
  assertEquals("/alter <player> <attribute> <mode> <value>", lines.get(0));
  assertTrue(lines.contains("\t\t- For example: #'/alter testplayer outfit set 151010301'"));
 }

 @Test public void scriptHelpTranslatesLegacyJavaWordingAndPreservesScriptNames() throws Exception {
  List<String> lines = translatedGmHelp("script");
  assertEquals("Usage: /script [-list|-load|-unload|-execute] [parameters]", lines.get(0));
  assertTrue(lines.contains("#/script #LogoutPlayer.class #player : Log a player out of the game."));
 }
}
