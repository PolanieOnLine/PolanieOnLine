package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class SupportAnswerTranslationTest {
    @BeforeClass public static void setup() { MockStendlRPWorld.get(); }
    @After public void cleanup() { MockStendhalRPRuleProcessor.get().clearPlayers(); }

    @Test public void onlineSupportAnswerUsesRecipientLanguageAndPreservesTheHumanReply() {
        final Player admin = PlayerTestHelper.createPlayer("polano");
        final Player english = PlayerTestHelper.createPlayer("Zamknij");
        final Player polish = PlayerTestHelper.createPlayer("Witaj.");
        english.setLanguage("en");
        MockStendhalRPRuleProcessor.get().addPlayer(english);
        MockStendhalRPRuleProcessor.get().addPlayer(polish);
        final SupportAnswerAction handler = new SupportAnswerAction();
        final String reply = "Witaj.\nZamknij $5 \\opis {0} {1}";
        final RPAction action = new RPAction();
        action.put(Actions.TYPE, Actions.SUPPORTANSWER);
        action.put(Actions.TARGET, english.getName());
        action.put(Actions.TEXT, reply);
        handler.onAction(admin, action);
        assertEquals("Administrator admin1 tells you: " + reply + " \nUse /support to reply.",
                english.events().get(0).get("text"));
        assertEquals("SUPPORT", english.events().get(0).get("texttype"));
        action.put(Actions.TARGET, polish.getName());
        handler.onAction(admin, action);
        assertEquals("Administrator admin1 powiedział Tobie: " + reply + " \nJeżeli chcesz odpowiedzieć to użyj /support.",
                polish.events().get(0).get("text"));
        assertEquals(reply, action.get(Actions.TEXT));
    }
}
