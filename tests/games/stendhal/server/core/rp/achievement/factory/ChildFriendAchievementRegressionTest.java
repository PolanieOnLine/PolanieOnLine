package games.stendhal.server.core.rp.achievement.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

public class ChildFriendAchievementRegressionTest {
    private static final String[][] REPEATED = {
        {"campfire", "done;done;5"},
        {"icecream_for_annie", "eating;1793199013375"},
        {"chocolate_for_elisabeth", "eating;1793199297473"},
        {"fishsoup_for_hughie", "1793051306906"},
        {"eggs_for_marianne", "done;done;3"}
    };
    @BeforeClass public static void initialize() {
        MockStendlRPWorld.get(); PlayerTestHelper.generatePlayerRPClasses();
    }
    private Achievement achievement() {
        for (Achievement candidate : new FriendAchievementFactory().createAchievements()) {
            if (FriendAchievementFactory.ID_CHILD_FRIEND.equals(candidate.getIdentifier())) { return candidate; }
        }
        throw new AssertionError("Missing child friend achievement");
    }
    private void oneTimeQuests(Player player) {
        for (String name : new String[]{"introduce_players", "plink_toy", "toys_collector", "find_jefs_mom", "coded_message"}) {
            player.setKeyedSlot("!quests", name, "done");
        }
        player.setKeyedSlot("!quests", "susi", "2026");
    }
    @Test public void recognizesRealPlinkSlotAndExistingCompletionStates() {
        Player player=PlayerTestHelper.createPlayer("child-friend-existing"); oneTimeQuests(player);
        for (String[] quest : REPEATED) { player.setKeyedSlot("!quests", quest[0], quest[1]); }
        assertTrue(achievement().isFulfilled(player));
        player.setKeyedSlot("!quests", "plink_toy", null);
        assertFalse(achievement().isFulfilled(player));
        player.setKeyedSlot("!quests", "plinks_toy", "done");
        assertTrue(achievement().isFulfilled(player));
    }
    @Test public void remembersEachCompletionBeforeOtherChildrenAreHelpedAndAfterRestore() {
        Player player=PlayerTestHelper.createPlayer("child-friend-repeat");
        for (String[] quest : REPEATED) {
            player.setQuest(quest[0], quest[1]); player.setQuest(quest[0], "start");
        }
        assertFalse(achievement().isFulfilled(player)); oneTimeQuests(player);
        Player restored=new Player(new RPObject(player));
        assertTrue(achievement().isFulfilled(restored));
    }
    @Test public void firstAcceptanceRejectionAndUnknownStatesCannotCompleteTheAchievement() {
        for (String[] missing : REPEATED) {
            for (String invalid : new String[]{"start", "rejected", "unexpected"}) {
                Player player=PlayerTestHelper.createPlayer("child-friend-incomplete"); oneTimeQuests(player);
                for (String[] quest : REPEATED) {
                    player.setKeyedSlot("!quests", quest[0], quest == missing ? invalid : quest[1]);
                }
                assertFalse(missing[0] + " " + invalid, achievement().isFulfilled(player));
            }
        }
    }
    @Test public void existingCompletionCountsSurviveAnAlreadyStartedRepeat() {
        Player player=PlayerTestHelper.createPlayer("child-friend-counts"); oneTimeQuests(player);
        for (String[] quest : REPEATED) { player.setKeyedSlot("!quests", quest[0], quest[1]); }
        player.setKeyedSlot("!quests", "campfire", "start;done;5");
        player.setKeyedSlot("!quests", "eggs_for_marianne", "start;done;3");
        assertTrue(achievement().isFulfilled(player));
    }
}
