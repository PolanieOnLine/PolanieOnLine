/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.common.NotificationType;

public class UiTranslationTest {
	private Field preferences;
	private Object previousPreferences;
	@Before public void isolatePreferences() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
	}
	@After public void restorePreferences() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}
	private void language(String value) throws Exception {
		SwingUtilities.invokeAndWait(() -> WtWindowManager.getInstance().setProperty(ClientLanguage.PROPERTY, value));
		SwingUtilities.invokeAndWait(() -> { });
	}

	@Test public void updatesVisibleControlsAndRestoresOriginals() throws Exception {
		JPanel root = new JPanel();
		JButton button = new JButton("Utwórz Konto");
		button.setToolTipText("Pokaż hasło");
		root.add(button);
		root.setBorder(new TitledBorder("Głośność"));
		UiTranslation.register(root);
		language("en");
		assertEquals("Create Account", button.getText());
		assertEquals("Show password", button.getToolTipText());
		assertEquals("Volume", ((TitledBorder) root.getBorder()).getTitle());
		language("pl");
		assertEquals("Utwórz Konto", button.getText());
		assertEquals("Pokaż hasło", button.getToolTipText());
		assertEquals("Głośność", ((TitledBorder) root.getBorder()).getTitle());
	}

	@Test public void screenshotMenuTabsAndMultilineSettingsHintsUseEnglish() throws Exception {
		language("en");
		JPanel root = new JPanel();
		String[][] captions = {{"Historia Logowania", "Login History"}, {"Zrzut ekranu", "Screenshot"},
			{"Wojownicy Online", "Players Online"}, {"Przewodnik", "Guide"}, {"Regulamin", "Rules"}};
		for (String[] caption : captions) {
			JMenuItem item = new JMenuItem(caption[0]); root.add(item);
		}
		JLabel note = new JLabel("<html>Tłumaczenia są jeszcze uzupełniane.<br>Brakujący tekst pozostaje po polsku.</html>");
		note.setToolTipText("Rozmiar czcionki w oknach klienta."); root.add(note);
		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Przyjaciele", new JPanel()); tabs.add("Prywatny", new JPanel()); tabs.add("Produkcja", new JPanel());
		root.add(tabs); UiTranslation.register(root);
		for (int i=0;i<captions.length;i++) assertEquals(captions[i][1],((JMenuItem) root.getComponent(i)).getText());
		assertEquals("<html>Translations are still being added.<br>Missing text stays in Polish.</html>",note.getText());
		assertEquals("Font size in client windows.",note.getToolTipText());
		assertEquals("Friends",tabs.getTitleAt(0)); assertEquals("Private",tabs.getTitleAt(1)); assertEquals("Production",tabs.getTitleAt(2));
		language("pl"); assertEquals("Przyjaciele",tabs.getTitleAt(0)); assertEquals("Prywatny",tabs.getTitleAt(1));
	}

	@Test public void preservesPlayerNamesInputAndOptionValues() throws Exception {
		language("en");
		JPanel root = new JPanel();
		JLabel character = new JLabel("Plecak");
		UiTranslation.preserve(character);
		JTextField input = new JTextField("Zamknij");
		JTextArea chat = new JTextArea("Utwórz Konto");
		JComboBox<String> profile = new JComboBox<>(new String[] {"Utwórz Konto"});
		JCheckBox toggle = new JCheckBox("Zamień zestawy", true);
		root.add(character); root.add(input); root.add(chat); root.add(profile); root.add(toggle);
		UiTranslation.register(root);
		assertEquals("Plecak", character.getText());
		assertEquals("Zamknij", input.getText());
		assertEquals("Utwórz Konto", chat.getText());
		assertEquals("Utwórz Konto", profile.getSelectedItem());
		assertEquals("Swap equipment sets", toggle.getText());
		assertTrue(toggle.isSelected());
	}

	@Test public void translatesDynamicallyAddedAndUpdatedControls() throws Exception {
		language("en");
		JPanel root = new JPanel(); UiTranslation.register(root);
		JLabel status = new JLabel("Wybierz postać"); root.add(status);
		assertEquals("Choose character", status.getText());
		status.setText("Obserwujesz: Plecak");
		assertEquals("Watching: Plecak", status.getText());
		language("pl");
		assertEquals("Obserwujesz: Plecak", status.getText());
	}

	@Test public void translatesOnlyClientNotificationsNotPlayerMessages() throws Exception {
		language("en");
		assertEquals("Choose character", new EventLine("", "Wybierz postać", NotificationType.CLIENT).getText());
		assertEquals("Wybierz postać", new EventLine("Plecak", "Wybierz postać", NotificationType.NORMAL).getText());
		assertEquals("Utwórz Konto", new EventLine("Plecak", "Utwórz Konto", NotificationType.PRIVMSG).getText());
	}

	@Test public void startupLanguageTranslatesWelcomeBeforeTheServerConfirmsIt() throws Exception {
		language("en");
		String welcome = "Witaj w #'PolanieOnLine'! Odwiedź stronę - #polanieonline.eu! "
				+ "Pamiętaj, aby nikomu ~'nie udostępniać' hasła do swojego konta! Jeżeli znalazłeś jakieś błędy w grze to zgłoś to nam do ~'supportu'.";
		assertTrue(new EventLine("",welcome,NotificationType.SERVER).getText().startsWith("Welcome to #'PolanieOnLine'!"));
		assertEquals(welcome,new EventLine("Witaj.",welcome,NotificationType.PRIVMSG).getText());
		assertEquals("Utwórz konto",new EventLine("","Utwórz konto",NotificationType.SERVER).getText());
	}

	@Test public void translatesTabCaptionsWithoutModifyingContentIdentity() throws Exception {
		JTabbedPane tabs = new JTabbedPane(); JPanel quests = new JPanel();
		quests.setName("Ukończone zadania");
		tabs.add("Ukończone zadania", quests); UiTranslation.register(tabs);
		language("en");
		assertEquals("Completed quests", tabs.getTitleAt(0));
		assertEquals("Ukończone zadania", tabs.getComponentAt(0).getName());
		language("pl"); assertEquals("Ukończone zadania", tabs.getTitleAt(0));
	}

	@Test public void itemDisplayNamesAndTooltipsNeverRenameTheCanonicalItem() throws Exception {
		marauroa.common.game.RPObject object = utilities.RPClass.ItemTestHelper.createItem("skórzany kirys");
		object.put("rarity_id", "rare");
		object.put(games.stendhal.common.constants.ItemTooltip.ATTRIBUTE, "category", "armour");
		object.put(games.stendhal.common.constants.ItemTooltip.ATTRIBUTE, "def", "85");
		games.stendhal.client.entity.IEntity item = games.stendhal.client.entity.factory.EntityFactory.createEntity(object);
		language("en");
		assertEquals("leather cuirass", item.getTitle());
		assertEquals("skórzany kirys", item.getName());
		String tooltip = ItemRarityPresentation.buildItemToolTip(item);
		assertTrue(tooltip.contains("Rare"));
		assertFalse(tooltip.contains("pancerza"));
		assertTrue(tooltip.contains("85"));
		assertEquals("85", object.get(games.stendhal.common.constants.ItemTooltip.ATTRIBUTE, "def"));
		language("pl"); assertEquals("skórzany kirys", item.getTitle());
	}
}
