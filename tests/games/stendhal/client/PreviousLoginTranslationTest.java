package games.stendhal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.NotificationType;

public class PreviousLoginTranslationTest {
	private final List<EventLine> received = new ArrayList<EventLine>();
	private Field preferences;
	private Object previousPreferences;
	private UserInterface previousInterface;
	private Queue<Runnable> tasks;
	private StendhalClient client;

	@Before @SuppressWarnings("unchecked") public void isolateClientDisplay() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		Properties values = new Properties();
		values.setProperty("config." + ClientLanguage.PROPERTY, "en");
		preferences.set(WtWindowManager.getInstance(), values);
		previousInterface = ClientSingletonRepository.getUserInterface();
		ClientSingletonRepository.setUserInterface((UserInterface) Proxy.newProxyInstance(
				UserInterface.class.getClassLoader(), new Class<?>[] { UserInterface.class },
				(proxy, method, arguments) -> {
					if ("addEventLine".equals(method.getName())) received.add((EventLine) arguments[0]);
					return null;
				}));
		Field queue = GameLoop.class.getDeclaredField("temporaryTasks");
		queue.setAccessible(true);
		tasks = (Queue<Runnable>) queue.get(GameLoop.get());
		assertTrue("The game loop must be idle during the callback test", tasks.isEmpty());
		// The callback uses no instance fields. Avoid starting network/cache services.
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field singleton = unsafeClass.getDeclaredField("theUnsafe");
		singleton.setAccessible(true);
		client = (StendhalClient) unsafeClass.getMethod("allocateInstance", Class.class)
				.invoke(singleton.get(null), StendhalClient.class);
	}

	@After public void restoreClientDisplay() throws Exception {
		ClientSingletonRepository.setUserInterface(previousInterface);
		if (preferences != null) preferences.set(WtWindowManager.getInstance(), previousPreferences);
	}

	@Test public void displaysTranslatedSuccessAfterTheGameLoopRuns() {
		client.onPreviousLogins(Arrays.asList("logowanie zakończyło się sukcesem dnia 2026-10-14 14:57 czasu serwera z 127.0.0.1"));
		assertTrue("The callback must stay queued", received.isEmpty());
		runCallback();
		assertEquals(1, received.size());
		assertEquals("Previous login succeeded on 2026-10-14 14:57 server time from 127.0.0.1", received.get(0).getText());
		assertEquals(NotificationType.SERVER, received.get(0).getType());
	}

	@Test public void warnsForPolishAndLegacyEnglishFailureMarkers() {
		client.onPreviousLogins(Arrays.asList(
				"logowanie zakończyło się NIEPOWODZENIEM dnia 2026-10-14 14:57 czasu serwera z 127.0.0.1",
				"login FAILED on 2026-10-13 09:30 from 192.0.2.5"));
		runCallback();
		assertEquals(2, received.size());
		assertEquals(NotificationType.WARNING, received.get(0).getType());
		assertEquals("Previous login FAILED on 2026-10-14 14:57 server time from 127.0.0.1", received.get(0).getText());
		assertEquals(NotificationType.WARNING, received.get(1).getType());
	}

	@Test public void keepsPolishOutputAndOtherServerMessagesUnchanged() {
		((Properties) getPreferences()).setProperty("config." + ClientLanguage.PROPERTY, "pl");
		String login = "logowanie zakończyło się sukcesem dnia 2026-10-14 14:57 czasu serwera z 127.0.0.1";
		client.onPreviousLogins(Arrays.asList(login));
		runCallback();
		assertEquals("Poprzednie " + login, received.get(0).getText());
		((Properties) getPreferences()).setProperty("config." + ClientLanguage.PROPERTY, "en");
		assertEquals("Wybierz postać", new EventLine("Someone", "Wybierz postać", NotificationType.SERVER).getText());
		assertEquals("Wybierz postać", new EventLine("Someone", "Wybierz postać", NotificationType.NORMAL).getText());
	}

	private Object getPreferences() {
		try { return preferences.get(WtWindowManager.getInstance()); }
		catch (IllegalAccessException error) { throw new AssertionError(error); }
	}

	private void runCallback() {
		Runnable callback = tasks.poll();
		assertNotNull("Previous login callback should enqueue one task", callback);
		callback.run();
		assertTrue(tasks.isEmpty());
	}
}
