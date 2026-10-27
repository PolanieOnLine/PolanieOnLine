package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Properties;
import org.junit.*;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import utilities.MemoryClientPersistence;

/** Tests the message formatter without launching a browser or sending a request. */
public class BrowserFailureTranslationTest {
 private Field preferences;private Object previous;private MemoryClientPersistence persistence;
 @Before public void useIsolatedEnglishPreferences() throws Exception {
  persistence=new MemoryClientPersistence();preferences=WtWindowManager.class.getDeclaredField("properties");preferences.setAccessible(true);
  previous=preferences.get(WtWindowManager.getInstance());Properties selected=new Properties();
  selected.setProperty("config."+ClientLanguage.PROPERTY,"en");preferences.set(WtWindowManager.getInstance(),selected);
 }
 @After public void restorePreferences() throws Exception {preferences.set(WtWindowManager.getInstance(),previous);persistence.close();}
 @Test public void browserFailureTranslatesOnlyItsPrefixAndKeepsTheExactUrl() {
  String url="https://example.invalid/Zamknij?q=Witaj.%20{0}&value=$FEE#Plecak";
  assertEquals("Error opening the browser:\n "+url,BareBonesBrowserLaunch.failureMessage(url));
 }
 @Test public void polishFailureKeepsItsExistingTextAndWhitespace() throws Exception {
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");
  assertEquals("Błąd podczas uruchamiania przeglądarki:\n https://example.invalid",BareBonesBrowserLaunch.failureMessage("https://example.invalid"));
 }
}
