package games.stendhal.client.scripting;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.*;
import games.stendhal.client.*;
import games.stendhal.client.actions.*;
import games.stendhal.client.gui.*;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;
import utilities.MemoryClientPersistence;

public class CommandErrorTranslationTest {
 private Field preferences,actions;private Object oldPreferences,oldActions;
 private UserInterface oldUi;private MemoryClientPersistence persistence;
 private final List<EventLine> events=new ArrayList<EventLine>();
 @Before public void isolatedClient() throws Exception {
  persistence=new MemoryClientPersistence();preferences=WtWindowManager.class.getDeclaredField("properties");preferences.setAccessible(true);
  oldPreferences=preferences.get(WtWindowManager.getInstance());Properties selected=new Properties();selected.setProperty("config."+ClientLanguage.PROPERTY,"en");preferences.set(WtWindowManager.getInstance(),selected);
  oldUi=ClientSingletonRepository.getUserInterface();ClientSingletonRepository.setUserInterface(new MockUserInterface(){
   @Override public void addEventLine(EventLine event){events.add(event);}
  });
  actions=SlashActionRepository.class.getDeclaredField("actions");actions.setAccessible(true);oldActions=actions.get(null);
  HashMap<String,SlashAction> registry=new HashMap<String,SlashAction>();registry.put("zamknij",new SlashAction(){
   public int getMinimumParameters(){return 1;}public int getMaximumParameters(){return 1;}
   public boolean execute(String[] parameters,String remainder){throw new AssertionError("Incomplete command must not execute");}
  });actions.set(null,registry);
 }
 @After public void restoreClient() throws Exception {
  actions.set(null,oldActions);preferences.set(WtWindowManager.getInstance(),oldPreferences);
  ClientSingletonRepository.setUserInterface(oldUi);persistence.close();
 }
 @Test public void incompleteCommandShowsEnglishErrorWithoutTranslatingItsNameOrExecuting() {
  assertFalse(ChatLineParser.parseAndHandle("/Zamknij"));
  assertEquals("Missing parameter for command 'Zamknij'.",events.get(0).getText());
  assertEquals(NotificationType.ERROR,events.get(0).getType());
 }
 @Test public void polishCommandErrorRemainsPolish() throws Exception {
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");
  assertFalse(ChatLineParser.parseAndHandle("/Zamknij"));
  assertEquals("Zabrakło parametru dla komendy 'Zamknij'.",events.get(0).getText());
 }
}
