package games.stendhal.server.actions.query;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.Translate;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

public class LanguageActionTest {
	@BeforeClass public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		Translate.init();
	}
	private RPAction action(String language) {
		RPAction action = new RPAction();
		action.put("type", "language");
		if (language != null) action.put("target", language);
		return action;
	}
	@Test public void languageSurvivesRecreatingThePlayerFromStoredAttributes() {
		Player player = PlayerTestHelper.createPlayer("Language persistence");
		assertEquals("pl", player.getLanguage());
		new LanguageAction().onAction(player, action(" EN "));
		assertEquals("en", player.get("language"));
		Player restored = new Player(new RPObject(player));
		assertEquals("en", restored.getLanguage());
	}
	@Test public void malformedAndUnsupportedActionsCannotChangeLanguage() {
		Player player = PlayerTestHelper.createPlayer("Language validation");
		player.setLanguage("en");
		for (String language : new String[] {null, "", "es", "../../pl"}) {
			new LanguageAction().onAction(player, action(language));
			assertEquals("en", player.getLanguage());
		}
	}
	@Test public void failedActionCannotLeakLanguageToTheNextPlayer() {
		Player player = PlayerTestHelper.createPlayer("Language context");
		player.setLanguage("en");
		CommandCenter.register("translation_test_failure", (caller, request) -> {
			assertEquals("Close", Translate.getText("Zamknij"));
			throw new IllegalStateException("Intentional translation context test");
		});
		RPAction request = new RPAction();
		request.put("type", "translation_test_failure");
		assertFalse(CommandCenter.execute(player, request));
		assertEquals("Rzadkość", Translate.getText("Rarity"));
	}
	@Test public void systemTextUsesRecipientLanguageButPrivateChatStaysOriginal() {
		Player player = PlayerTestHelper.createPlayer("Language messages");
		player.setLanguage("en");
		player.sendPrivateText(NotificationType.SERVER, "Podejdź bliżej, aby porozmawiać.");
		player.sendPrivateText(NotificationType.PRIVMSG, "Zamknij");
		assertEquals("Move closer to talk.", player.events().get(0).get("text"));
		assertEquals("Zamknij", player.events().get(1).get("text"));
	}
}
