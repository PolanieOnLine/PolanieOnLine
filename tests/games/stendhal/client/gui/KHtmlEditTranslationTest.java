package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.Translation;

public class KHtmlEditTranslationTest {
 private Field preferences;
 private Object previous;
 @Before public void isolatePreferences() throws Exception {
  preferences=WtWindowManager.class.getDeclaredField("properties");preferences.setAccessible(true);
  previous=preferences.get(WtWindowManager.getInstance());preferences.set(WtWindowManager.getInstance(),new Properties());
  WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY,"en");
 }
 @After public void restorePreferences() throws Exception {preferences.set(WtWindowManager.getInstance(),previous);}
 @Test public void translatedCaptionAndCanonicalTargetAreBoundBeforeLanguageChanges() {
  KHtmlEdit editor=new KHtmlEdit();
  String rendered=Translation.getText("en","Kontynuuj polowanie. Powiedz #załatwione, kiedy skończysz.");
  String html=editor.translateToHTML(rendered);
  assertTrue(html,html.contains("href='say:za%C5%82atwione'>done</a>"));
  WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY,"pl");
  assertTrue(html,html.contains("href='say:za%C5%82atwione'>done</a>"));
 }
 @Test public void quotedCaptionsPreserveAdjacentCanonicalKeywords() {
  KHtmlEdit editor=new KHtmlEdit();
  String source="Mogę pokazać Tobie #mapę lub pokazać gdzie jest #bank, #biblioteka, #oberża, #świątynia, #kowal, #piekarnia, #publiczna #skrzynia lub gdzie jest stara #wioska.";
  String html=editor.translateToHTML(Translation.getText("en",source));
  assertTrue(html,html.contains("href='say:publiczna+skrzynia'>public chest</a>"));
 }
 @Test public void unrelatedPlayerTextAndHttpLinksRetainTheirOriginalTargets() {
  KHtmlEdit editor=new KHtmlEdit();
  assertTrue(editor.translateToHTML("A player said #Cancel.").contains("href='say:Cancel'>Cancel</a>"));
  assertTrue(editor.translateToHTML("Visit #https://example.com.").contains("href='https://example.com'>https://example.com</a>"));
 }
}
