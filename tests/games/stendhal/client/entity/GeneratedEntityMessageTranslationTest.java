package games.stendhal.client.entity;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.gui.MockUserInterface;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;
import marauroa.common.game.RPObject;

public class GeneratedEntityMessageTranslationTest {
	private Field preferences, userProxy;
	private Object previousPreferences, previousUser;
	private UserInterface previousInterface;
	private final List<EventLine> events = new ArrayList<EventLine>();
	private TestUser user;

	@Before public void isolateClient() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties"); preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		Properties properties = new Properties(); properties.setProperty("config." + ClientLanguage.PROPERTY, "en");
		preferences.set(WtWindowManager.getInstance(), properties);
		userProxy = User.class.getDeclaredField("userProxy"); userProxy.setAccessible(true); previousUser = userProxy.get(null);
		previousInterface = ClientSingletonRepository.getUserInterface();
		ClientSingletonRepository.setUserInterface(new MockUserInterface() {
			@Override public void addEventLine(EventLine event) { events.add(event); }
		});
		user = new TestUser();
	}

	@After public void restoreClient() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previousPreferences); userProxy.set(null, previousUser);
		ClientSingletonRepository.setUserInterface(previousInterface);
	}

	@Test public void generatedPoisonAndHealingKeepNamesAndAmounts() throws Exception {
		invoke(RPEntity.class, user, "onPoisoned", new Class<?>[] {int.class}, 3);
		assertEquals("Zamknij was poisoned. Loses 3 health points.", events.get(0).getText());
		user.onHealed(5);
		assertEquals("Zamknij recovered 5 health points.", events.get(1).getText());
		assertEquals(NotificationType.HEAL, events.get(1).getType());
	}

	@Test public void joinsAndLeavesDoNotTranslatePlayerNames() throws Exception {
		RPObject changes = new RPObject(); changes.put("online", "Zamknij,Plecak");
		invoke(User.class, user, "notifyUserAboutPlayerOnlineChanges", new Class<?>[] {RPObject.class}, changes);
		assertEquals("Zamknij has joined PolanieOnLine.", events.get(0).getText());
		assertEquals("Plecak has joined PolanieOnLine.", events.get(1).getText());
	}

	@Test public void awayLabelIsTranslatedButAuthoredAwayTextStaysUnchanged() {
		TestPlayer player = new TestPlayer(); player.onAway("Zamknij");
		assertEquals("Away", player.label);
		assertEquals("Zamknij", player.getAway());
		player.onAway(null); assertEquals("Back", player.label);
	}

	private void invoke(Class<?> type, Object target, String name, Class<?>[] parameters, Object... values) throws Exception {
		Method method = type.getDeclaredMethod(name, parameters); method.setAccessible(true); method.invoke(target, values);
	}

	private static class TestUser extends User {
		@Override public String getTitle() { return "Zamknij"; }
		@Override public String getGender() { return "M"; }
		@Override public boolean isInHearingRange() { return true; }
		@Override protected void playRandomSoundFromCategory(String group, String category) { }
	}
	private static class TestPlayer extends Player {
		private String label;
		@Override public String getGender() { return "M"; }
		@Override protected void addTextIndicator(String text, NotificationType type) { label = text; }
	}
}
