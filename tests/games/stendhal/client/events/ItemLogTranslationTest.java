package games.stendhal.client.events;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.util.*;
import javax.swing.*;
import org.junit.*;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.Events;
import marauroa.common.game.*;
import utilities.MemoryClientPersistence;

public class ItemLogTranslationTest {
 private Field preferences;private Object previous;private MemoryClientPersistence persistence;
 private RPEvent event;private Object view;
 private static final String ROW="drewniany łuk,true,987654321,ranged,wooden_bow";
 @Before public void isolatedEnglishLog() throws Exception {
  persistence=new MemoryClientPersistence();preferences=WtWindowManager.class.getDeclaredField("properties");preferences.setAccessible(true);
  previous=preferences.get(WtWindowManager.getInstance());Properties values=new Properties();
  values.setProperty("config."+ClientLanguage.PROPERTY,"en");preferences.set(WtWindowManager.getInstance(),values);
  if(!RPClass.hasRPClass(Events.DROPPEDLIST))games.stendhal.server.events.ItemLogEvent.generateRPClass();
  event=new RPEvent(Events.DROPPEDLIST);event.put("dropped_items",ROW);
  ItemLogEvent log=new ItemLogEvent();log.init(null,event);view=log.createView();
 }
 @After public void restorePreferences() throws Exception {preferences.set(WtWindowManager.getInstance(),previous);persistence.close();}
 private Object invoke(String name,Class<?> parameter,Object argument) throws Exception {
  Method method=view.getClass().getDeclaredMethod(name,parameter);method.setAccessible(true);return method.invoke(view,argument);
 }
 @Test public void logTableTranslatesHeadersAndItemNamesButKeepsExactCountersAndCanonicalLinks() throws Exception {
  JTable table=(JTable)invoke("createTableForCategory",List.class,Arrays.asList(ROW));
  assertEquals("Item name",table.getColumnName(1));assertEquals("Quantity",table.getColumnName(2));assertEquals("Obtained",table.getColumnName(3));
  assertEquals("wooden bow",table.getValueAt(0,1));assertEquals("987654321",table.getValueAt(0,2));assertEquals("✔",table.getValueAt(0,3));
  assertEquals(ROW,event.get("dropped_items"));
  String url=ItemLogEvent.itemCatalogUrl("https://example.invalid","ranged","wooden_bow","drewniany łuk");
  assertEquals("ranged:wooden_bow:drewniany łuk",URLDecoder.decode(url.split("&item=",2)[1],"UTF-8"));
 }
 @Test public void everyCategoryHasAnEnglishTabAndCanSwitchBackWithoutChangingFilteringLabels() throws Exception {
  String[][] categories={{"belts","Belts"},{"ring","Rings"},{"ranged","Ranged weapons"},{"necklace","Necklaces"},
   {"missile","Missiles"},{"magia","Magic"},{"money","Coins"},{"misc","Miscellaneous"},{"wand","Wands"},
   {"resource","Resources"},{"boots","Boots"},{"club","Hammers"},{"sword","Swords"},{"cloak","Cloaks"},
   {"armor","Armor"},{"shield","Shields"},{"glove","Gloves"},{"legs","Legwear"},{"helmet","Helmets"},
   {"jewellery","Gems"},{"drink","Drinks"},{"dagger","Daggers"},{"herb","Herbs"},{"scroll","Scrolls"},
   {"food","Food"},{"axe","Axes"},{"ammunition","Ammunition"}};
  JTabbedPane tabs=new JTabbedPane();List<String> canonical=new ArrayList<String>();
  for(String[] category:categories) {String label=(String)invoke("getTranslatedClass",String.class,category[0]);canonical.add(label);tabs.addTab(label,new JPanel());}
  UiTranslation.apply(tabs);
  for(int i=0;i<categories.length;i++)assertEquals(categories[i][0],categories[i][1],tabs.getTitleAt(i));
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");UiTranslation.apply(tabs);
  for(int i=0;i<categories.length;i++)assertEquals(canonical.get(i),tabs.getTitleAt(i));
 }
 @Test public void polishLogAndUndiscoveredItemKeepTheirOriginalPresentation() throws Exception {
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");
  String undiscovered="drewniany łuk,false,0,ranged,wooden_bow";
  JTable table=(JTable)invoke("createTableForCategory",List.class,Arrays.asList(undiscovered));
  assertEquals("Nazwa przedmiotu",table.getColumnName(1));assertEquals("drewniany łuk",table.getValueAt(0,1));
  assertEquals("",table.getValueAt(0,2));assertEquals("",table.getValueAt(0,3));
 }
 @Test public void changingLanguageRefreshesTheOpenTableWithoutTouchingCountsOrEventData() throws Exception {
  JTable table=(JTable)invoke("createTableForCategory",List.class,Arrays.asList(ROW));
  table.moveColumn(1,3);
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"pl");UiTranslation.apply(table);
  assertEquals("Nazwa przedmiotu",table.getColumnModel().getColumn(3).getHeaderValue());assertEquals("drewniany łuk",table.getValueAt(0,3));
  ((Properties)preferences.get(WtWindowManager.getInstance())).setProperty("config."+ClientLanguage.PROPERTY,"en");UiTranslation.apply(table);
  assertEquals("Item name",table.getColumnModel().getColumn(3).getHeaderValue());assertEquals("wooden bow",table.getValueAt(0,3));
  assertEquals("987654321",table.getModel().getValueAt(0,2));assertEquals(ROW,event.get("dropped_items"));
 }
}
