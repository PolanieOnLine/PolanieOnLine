package games.stendhal.client;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.gui.wt.core.WtWindowManager;
import marauroa.client.ClientFramework;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import utilities.MemoryClientPersistence;

public class ClientLanguageStartupChoiceTest {
	private Field preferences;
	private Object previousPreferences;
	private Field playerField;
	private Object previousPlayer;
	private ClientFramework previousFramework;
	private final List<RPAction> sent = new ArrayList<RPAction>();
	private MemoryClientPersistence persistence;
	private Object previousPendingLanguage;
	private Object previouslySubmitted;

	@Before public void isolateSession() throws Exception {
		persistence = new MemoryClientPersistence();
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
		playerField = UserContext.class.getDeclaredField("player");
		playerField.setAccessible(true);
		previousPlayer = playerField.get(UserContext.get());
		playerField.set(UserContext.get(), null);
		previousFramework = ClientSingletonRepository.getClientFramework();
		ClientSingletonRepository.setClientFramework(new RecordingFramework());
		previousPendingLanguage = pendingField().get(null);
		previouslySubmitted = submittedField().get(null);
		clearPendingChoice();
	}

	@After public void restoreSession() throws Exception {
		pendingField().set(null, previousPendingLanguage);
		submittedField().set(null, previouslySubmitted);
		ClientSingletonRepository.setClientFramework(previousFramework);
		playerField.set(UserContext.get(), previousPlayer);
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
		persistence.close();
	}

	@Test public void persistsExplicitChoiceBeforeAnyLoginOrClientQuit() throws Exception {
		ClientLanguage.choose("en");
		assertEquals("en", persistence.savedPreferences().getProperty("config." + ClientLanguage.PROPERTY));
		assertTrue(sent.isEmpty());
	}

	@Test public void sendsExplicitStartupChoiceOnceAndKeepsItUntilAcknowledged() {
		ClientLanguage.choose("en");
		assertEquals("en", ClientLanguage.getLanguage());
		assertTrue(sent.isEmpty());
		RPObject profile = profile("pl");
		ClientLanguage.synchronize(profile);
		assertEquals("en", ClientLanguage.getLanguage());
		assertEquals(1, sent.size());
		assertEquals("language", sent.get(0).get("type"));
		assertEquals("en", sent.get(0).get("target"));
		ClientLanguage.synchronize(profile);
		assertEquals("en", ClientLanguage.getLanguage());
		assertEquals(1, sent.size());
		profile.put("language", "en");
		ClientLanguage.synchronize(profile);
		assertEquals(1, sent.size());
		profile.put("language", "pl");
		ClientLanguage.synchronize(profile);
		assertEquals("pl", ClientLanguage.getLanguage());
	}

	@Test public void followsServerProfileWithoutAnExplicitStartupChoice() throws Exception {
		((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "en");
		ClientLanguage.synchronize(profile("pl"));
		assertEquals("pl", ClientLanguage.getLanguage());
		assertTrue(sent.isEmpty());
		ClientLanguage.synchronize(profile("en"));
		assertEquals("en", ClientLanguage.getLanguage());
	}

	@Test public void mostRecentStartupChoiceWins() {
		ClientLanguage.choose("en");
		ClientLanguage.choose("pl");
		ClientLanguage.synchronize(profile("en"));
		assertEquals("pl", ClientLanguage.getLanguage());
		assertEquals(1, sent.size());
		assertEquals("pl", sent.get(0).get("target"));
	}

	@Test public void keepsExistingInGameActionAndServerAcknowledgmentFlow() throws Exception {
		playerField.set(UserContext.get(), profile("pl"));
		ClientLanguage.choose("en");
		assertEquals(1, sent.size());
		assertEquals("en", sent.get(0).get("target"));
		assertEquals("pl", ClientLanguage.getLanguage());
		ClientLanguage.synchronize(profile("en"));
		assertEquals("en", ClientLanguage.getLanguage());
		assertEquals(1, sent.size());
	}

	@Test public void retriesOnceForANewPlayerSessionWhenThePriorLoginNeverAcknowledged() {
		ClientLanguage.choose("en");
		ClientLanguage.synchronize(profile("pl"));
		assertEquals(1, sent.size());
		RPObject nextSession = profile("pl");
		ClientLanguage.synchronize(nextSession);
		assertEquals(2, sent.size());
		assertEquals("en", sent.get(1).get("target"));
		ClientLanguage.synchronize(nextSession);
		assertEquals(2, sent.size());
		assertEquals("en", ClientLanguage.getLanguage());
	}

	private RPObject profile(String language) {
		RPObject player = new RPObject();
		player.put("language", language);
		return player;
	}

	private void clearPendingChoice() throws Exception {
		// Keep session state isolated when this class runs with other client tests.
		pendingField().set(null, null);
		submittedField().set(null, null);
	}

	private Field pendingField() throws Exception {
		Field field = ClientLanguage.class.getDeclaredField("pendingStartupLanguage");
		field.setAccessible(true);
		return field;
	}

	private Field submittedField() throws Exception {
		Field field = ClientLanguage.class.getDeclaredField("startupLanguageSubmittedTo");
		field.setAccessible(true);
		return field;
	}

	private class RecordingFramework extends ClientFramework {
		@Override public void send(RPAction action) { sent.add(action); }
		@Override protected void onPerception(MessageS2CPerception message) { }
		@Override protected List<TransferContent> onTransferREQ(List<TransferContent> content) { return content; }
		@Override protected void onTransfer(List<TransferContent> content) { }
		@Override protected void onAvailableCharacters(String[] characters) { }
		@Override protected void onServerInfo(String[] info) { }
		@Override protected void onPreviousLogins(List<String> logins) { }
		@Override protected String getGameName() { return "test"; }
		@Override protected String getVersionNumber() { return "test"; }
	}
}
