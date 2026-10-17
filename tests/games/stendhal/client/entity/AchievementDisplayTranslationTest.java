package games.stendhal.client.entity;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.MockUserInterface;
import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.gui.achievementlog.AchievementLogRenderers;
import games.stendhal.client.gui.wt.core.WtWindowManager;

public class AchievementDisplayTranslationTest {
 private Field preferences;
 private Object previousPreferences;
 private UserInterface previousInterface;
 private final Properties isolated = new Properties();

 @Before public void isolateClientState() throws Exception {
  preferences=WtWindowManager.class.getDeclaredField("properties");
  preferences.setAccessible(true);
  previousPreferences=preferences.get(WtWindowManager.getInstance());
  preferences.set(WtWindowManager.getInstance(),isolated);
  previousInterface=ClientSingletonRepository.getUserInterface();
  isolated.setProperty("config."+ClientLanguage.PROPERTY,"en");
  assertEquals("en",ClientLanguage.getLanguage());
 }
 @After public void restoreClientState() throws Exception {
  preferences.set(WtWindowManager.getInstance(),previousPreferences);
  ClientSingletonRepository.setUserInterface(previousInterface);
 }
 @Test public void achievedTitleAndRequirementAreTranslatedAtTheDisplayBoundary() {
  final String[] rendered = new String[3];
  ClientSingletonRepository.setUserInterface(new MockUserInterface() {
   @Override public void addAchievementBox(String title,String description,String category) {
    rendered[0]=title;rendered[1]=description;rendered[2]=category;
   }
  });
  RPEntity entity=new RPEntity() {};
  entity.onReachAchievement("Pomocnik Ados","Ukończono dzienne zadanie na przedmiot 10 razy","quest.special.daily_item");
  assertEquals("Ados Helper",rendered[0]);
  assertEquals("Completed the daily item quest 10 times",rendered[1]);
  assertEquals("quest.special.daily_item",rendered[2]);
  isolated.setProperty("config."+ClientLanguage.PROPERTY,"pl");
  entity.onReachAchievement("Pomocnik Ados","Ukończono dzienne zadanie na przedmiot 10 razy","quest.special.daily_item");
  assertEquals("Pomocnik Ados",rendered[0]);
 }
 @Test public void achievementListRendererTranslatesTextWithoutRenamingTheTableValue() {
  JTable table=new JTable(new Object[][]{{"Pomocnik Ados"}},new Object[]{"Osiągnięcie"});
  AchievementLogRenderers.DescriptionCellRenderer renderer=AchievementLogRenderers.get().new DescriptionCellRenderer();
  JLabel label=(JLabel)renderer.getTableCellRendererComponent(table,table.getValueAt(0,0),false,false,0,0);
  assertEquals("Ados Helper",label.getText());
  assertEquals("Pomocnik Ados",table.getValueAt(0,0));
  isolated.setProperty("config."+ClientLanguage.PROPERTY,"pl");
  label=(JLabel)renderer.getTableCellRendererComponent(table,table.getValueAt(0,0),false,false,0,0);
  assertEquals("Pomocnik Ados",label.getText());
 }
}
