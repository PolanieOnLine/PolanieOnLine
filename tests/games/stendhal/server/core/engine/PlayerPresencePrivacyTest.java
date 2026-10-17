package games.stendhal.server.core.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class PlayerPresencePrivacyTest {
    @BeforeClass public static void initialize() {
        MockStendlRPWorld.get();
        PlayerTestHelper.generatePlayerRPClasses();
    }

    @After public void clearPlayers() { MockStendhalRPRuleProcessor.get().clearPlayers(); }

    private Player observer(String name, boolean ghost) {
        Player player = PlayerTestHelper.createPlayer(name);
        player.setGhost(ghost);
        MockStendhalRPRuleProcessor.get().addPlayer(player);
        return player;
    }

    private void presence(String method, Player subject) throws Exception {
        Method handler = StendhalRPRuleProcessor.class.getDeclaredMethod(method, Player.class);
        handler.setAccessible(true);
        handler.invoke(MockStendhalRPRuleProcessor.get(), subject);
    }

    @Test public void hiddenLogoutIsNotSentToVisiblePlayersEvenIfTheyAreFriends() throws Exception {
        Player viewer = observer("Viewer", false);
        viewer.put("buddies", "HiddenAdmin", true);
        Player hidden = observer("HiddenAdmin", true);
        presence("updatePlayerNameListForPlayersOnLogout", hidden);
        assertTrue("Hidden logout must not leave the server for visible players", viewer.events().isEmpty());
    }

    @Test public void hiddenLoginIsNotSentToVisiblePlayers() throws Exception {
        Player viewer = observer("Viewer", false);
        Player hidden = observer("HiddenAdmin", true);
        presence("updatePlayerNameListForPlayersOnLogin", hidden);
        assertTrue(viewer.events().isEmpty());
    }

    @Test public void visibleLogoutStillUpdatesOtherPlayers() throws Exception {
        Player viewer = observer("Viewer", false);
        Player subject = observer("Friend", false);
        presence("updatePlayerNameListForPlayersOnLogout", subject);
        assertEquals(1, viewer.events().size());
        assertEquals("Friend", viewer.events().get(0).get("name"));
    }

    @Test public void hiddenObserversRetainTheirExistingVisibilityOfOtherGhosts() throws Exception {
        Player viewer = observer("HiddenViewer", true);
        Player hidden = observer("HiddenAdmin", true);
        presence("updatePlayerNameListForPlayersOnLogout", hidden);
        assertFalse(viewer.events().isEmpty());
        assertEquals("HiddenAdmin", viewer.events().get(0).get("name"));
    }
}
