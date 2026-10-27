package games.stendhal.client.actions;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import games.stendhal.client.*;
import games.stendhal.client.gui.*;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.common.NotificationType;
import utilities.MemoryClientPersistence;

public class VolumeActionTranslationTest {
 private Field preferences;
 private Object previousPreferences;
 private UserInterface previousUi;
 private MemoryClientPersistence persistence;
 private final List<EventLine> events=new ArrayList<EventLine>();
 @Before public void isolatePreferencesAndAudioDevice() throws Exception {
  persistence=new MemoryClientPersistence();
  preferences=WtWindowManager.class.getDeclaredField("properties");preferences.setAccessible(true);
  previousPreferences=preferences.get(WtWindowManager.getInstance());
  Properties selected=new Properties();selected.setProperty("config."+ClientLanguage.PROPERTY,"en");
  preferences.set(WtWindowManager.getInstance(),selected);
  previousUi=ClientSingletonRepository.getUserInterface();
  final SoundSystemFacade sound=(SoundSystemFacade)Proxy.newProxyInstance(SoundSystemFacade.class.getClassLoader(),
   new Class<?>[]{SoundSystemFacade.class},(proxy,method,args)->{
    if(method.getName().equals("getGroupNames"))return Collections.emptyList();
    if(method.getName().equals("getVolume"))return 0.5f;
    throw new AssertionError("Audio device must not be changed by an invalid command: "+method.getName());
   });
  ClientSingletonRepository.setUserInterface(new MockUserInterface(){
   @Override public SoundSystemFacade getSoundSystemFacade(){return sound;}
   @Override public void addEventLine(EventLine event){events.add(event);}
  });
 }
 @After public void restoreClient() throws Exception {
  ClientSingletonRepository.setUserInterface(previousUi);
  preferences.set(WtWindowManager.getInstance(),previousPreferences);persistence.close();
 }
 @Test public void missingValueShowsEnglishHelpWithErrorColour() {
  assertTrue(new VolumeAction().execute(new String[]{"master",null},""));
  assertEquals("Use /volume for help.",events.get(0).getText());
  assertEquals(NotificationType.ERROR,events.get(0).getType());
 }
 @Test public void invalidNumberRemainsVerbatimInsideEnglishError() {
  new VolumeAction().execute(new String[]{"master","Witaj.{0}"},"");
  assertEquals("Witaj.{0} is not a valid number.",events.get(0).getText());
  assertEquals(NotificationType.ERROR,events.get(0).getType());
 }
 @Test public void unknownGroupTranslatesBothNoticesWithoutTranslatingItsName() {
  new VolumeAction().execute(new String[]{"Zamknij","75"},"");
  assertEquals("There is no sound group named \"Zamknij\".",events.get(0).getText());
  assertEquals("Enter \"/volume show\" to see the list of groups.",events.get(1).getText());
  assertEquals(NotificationType.NORMAL,events.get(0).getType());
 }
 @Test public void polishErrorsRemainUnchanged() throws Exception {
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");
  new VolumeAction().execute(new String[]{"master",null},"");
  assertEquals("Użyj /volume dla pomocy.",events.get(0).getText());
 }
 @Test public void volumeHelpUsesEnglishAndPreservesTheMasterValue() {
  new VolumeAction().execute(new String[]{null,null},"");
  assertEquals("Use /volume <name> <value> to adjust the volume.",events.get(0).getText());
  assertEquals("<name> is an entry from the list below. \"master\" controls the overall volume.",events.get(1).getText());
  assertEquals("<value> ranges from 0 to 100, but can be set higher.",events.get(2).getText());
  assertEquals("master -> 50",events.get(3).getText());
 }
}
