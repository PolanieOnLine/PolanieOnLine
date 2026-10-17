package games.stendhal.client.gui.settings;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.JLabel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;

public class SettingsTranslationTest {
 private Field preferences; private Object previous;
 @Before public void useEnglishWithoutSavingPreferences() throws Exception {
  preferences=WtWindowManager.class.getDeclaredField("properties"); preferences.setAccessible(true);
  previous=preferences.get(WtWindowManager.getInstance());
  Properties values=new Properties();values.setProperty("config."+ClientLanguage.PROPERTY,"en");
  preferences.set(WtWindowManager.getInstance(),values);
 }
 @After public void restorePreferences() throws Exception {preferences.set(WtWindowManager.getInstance(),previous);}
 @Test public void audioDeviceTooltipTranslatesAroundBoldTextAndLineBreaks() {
  JLabel device=new JLabel("Urządzenie:");
  device.setToolTipText("<html>Wyjściowe urządzenie dźwięku. <b>auto</b> powinno działać u większości osób,<br>ale wypróbuj inne jeżeli nie możesz otrzymać zadowalającego dźwięku</html>");
  SettingsTranslation.apply(device);
  assertEquals("<html>Audio output device. <b>auto</b> should work for most people,<br>but try another if the sound does not work properly</html>",device.getToolTipText());
 }
 @SuppressWarnings("unchecked")
 @Test public void transparencyDropdownTranslatesItsLiveTooltipWithoutChangingStoredOptions() throws Exception {
  String hint="Użyje prostej przezroczystości tam gdzie obraz jest w pełni przezroczysty lub całkowicie nieprzezroczysty. <P>Użyj tego ustawienia na starszych komputerach jeśli gra nie odpowiada.";
  String[][] data={{"Prosta przezroczystość","bitmask",hint}};
  Class<?> rendererType=Class.forName("games.stendhal.client.gui.settings.VisualSettings$TooltippedRenderer");
  java.lang.reflect.Constructor<?> constructor=rendererType.getDeclaredConstructor(String[][].class);constructor.setAccessible(true);
  javax.swing.ListCellRenderer<Object> renderer=(javax.swing.ListCellRenderer<Object>)constructor.newInstance((Object)data);
  javax.swing.JComboBox<Object> selector=new javax.swing.JComboBox<Object>(new Object[]{data[0][0]});selector.setRenderer(renderer);
  SettingsTranslation.apply(selector);javax.swing.JList<Object> list=new javax.swing.JList<Object>();
  selector.getRenderer().getListCellRendererComponent(list,data[0][0],0,false,false);
  assertEquals("<html>Uses simple transparency where pixels are either fully transparent or fully opaque. <P>Use this setting on older computers if the game stops responding.</html>",list.getToolTipText());
  assertEquals("Prosta przezroczystość",selector.getSelectedItem());assertEquals("bitmask",data[0][1]);
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");
  selector.getRenderer().getListCellRendererComponent(list,data[0][0],0,false,false);
  assertEquals("<html>"+hint+"</html>",list.getToolTipText());
 }
}
