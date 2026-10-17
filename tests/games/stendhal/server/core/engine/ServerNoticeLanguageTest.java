package games.stendhal.server.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.actions.query.WhoAction;
import games.stendhal.server.entity.mapstuff.useable.WellSource;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class ServerNoticeLanguageTest {
    @BeforeClass public static void setUpClasses() {
        MockStendlRPWorld.get();
        PlayerTestHelper.generatePlayerRPClasses();
    }

    @After public void clearPlayers() { MockStendhalRPRuleProcessor.get().clearPlayers(); }

    @Test public void whoLocalizesTheNoticeAndPreservesDictionaryLikeNicknames() {
        final Player player = PlayerTestHelper.createPlayer("Witaj.");
        player.setLanguage("en");
        player.setClientVersion("1.0");
        MockStendhalRPRuleProcessor.get().addPlayer(player);
        final RPAction action = new RPAction();
        action.put(Actions.TYPE, Actions.WHO);
        new WhoAction().onAction(player, action);
        assertEquals("1 online now: Witaj.(0) ", player.events().get(0).get("text"));
    }

    @Test public void welcomeUsesThePlayersSelectedLanguage() {
        final Player player = PlayerTestHelper.createPlayer("Zamknij");
        player.setLanguage("en");
        StendhalRPRuleProcessor.welcome(player);
        final String text = player.events().get(0).get("text");
        assertTrue(text, text.startsWith("Welcome to #'PolanieOnLine'!"));
        assertTrue(text, text.contains("#'polanieonline.eu'"));
    }

    @Test public void configuredSingleLineWelcomeTranslatesItsWholeSecurityNotice() throws Exception {
        final java.lang.reflect.Field saved = StendhalRPRuleProcessor.class.getDeclaredField("cachedWelcomeMessage");
        saved.setAccessible(true);
        final Object previous = saved.get(null);
        final String message = "Witaj w #'PolanieOnLine'! Odwiedź stronę - #polanieonline.eu! "
                + "Pamiętaj, aby nikomu ~'nie udostępniać' hasła do swojego konta! Jeżeli znalazłeś jakieś błędy w grze to zgłoś to nam do ~'supportu'.";
        try {
            saved.set(null, message);
            final Player player = PlayerTestHelper.createPlayer("Zamknij"); player.setLanguage("en");
            StendhalRPRuleProcessor.welcome(player);
            assertEquals("Welcome to #'PolanieOnLine'! Visit our website - #polanieonline.eu! "
                    + "Remember ~'never to share' your account password with anyone! If you find any bugs in the game, please report them to ~'support'.",
                    player.events().get(0).get("text"));
        } finally { saved.set(null,previous); }
    }

    @Test public void wishingWellStartUsesThePlayersSelectedLanguage() {
        final Player player = PlayerTestHelper.createPlayer("Zamknij");
        player.setLanguage("en");
        player.setClientVersion("1.0");
        new WellSource() {
            void startWish() { onStarted(player); }
        }.startWish();
        assertEquals("You threw 30 coins into the well and made a wish.", player.events().get(0).get("text"));
    }
}
