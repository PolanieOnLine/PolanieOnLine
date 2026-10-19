package games.stendhal.server.entity;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.deathmatch.DeathmatchInfo;
import games.stendhal.server.maps.deathmatch.DeathmatchState;
import games.stendhal.server.util.Area;
import games.stendhal.server.util.KillsForQuestCounter;
import utilities.PlayerTestHelper;

/** Run the actual reward hooks; unrelated lifetime/quest kill counters stay intact. */
public class HuntingArenaCreditTest {
    private Player player;
    private StendhalRPZone zone;
    @BeforeClass public static void init() { MockStendlRPWorld.get(); }
    @Before public void setup() {
        player = PlayerTestHelper.createPlayer("arena-hunter");
        player.setLevel(10);
        player.setQuest("hunting", "szczur,0,2,0,0;0;3;MEDIUM;1500");
        player.setQuest("other_task", "szczur,0,2,0,0");
        zone = new StendhalRPZone("hunting-world", 32, 32);
        MockStendlRPWorld.get().addRPZone(zone);
        PlayerTestHelper.registerPlayer(player);
        zone.add(player);
    }
    @After public void cleanup() {
        PlayerTestHelper.removePlayer(player);
        MockStendlRPWorld.get().removeZone(zone);
    }
    private Creature rat() { return SingletonRepository.getEntityManager().getCreature("szczur"); }
    private void kill(RPEntity victim, boolean solo) {
        zone.add(victim);
        victim.damageReceived.add(player, solo ? 2 : 1);
        victim.totalDamageReceived = 2;
        victim.rewardKillers(100);
    }
    private int remaining() {
        return new KillsForQuestCounter(player.getQuest("hunting", 0)).remainingKills(player, "szczur");
    }
    @Test public void ordinaryWorldKillStillCounts() {
        kill(rat(), true);
        assertEquals(1, remaining());
        assertEquals(1, player.getSoloKill("szczur"));
    }
    @Test public void arenaSoloKillPreservesExistingWorldProgressAndLifetimeStats() {
        kill(rat(), true);
        kill(new ArenaCreature(rat(), new Rectangle2D.Double(0, 0, 32, 32)), true);
        assertEquals(2, player.getSoloKill("szczur"));
        assertEquals(1, remaining());
        assertEquals(0, new KillsForQuestCounter(player.getQuest("other_task")).remainingKills(player, "szczur"));
        kill(rat(), true);
        assertEquals(0, remaining());
        assertEquals("1500", player.getQuest("hunting", 4));
    }
    @Test public void arenaSharedKillDoesNotAdvanceHunting() {
        kill(new ArenaCreature(rat(), new Rectangle2D.Double(0, 0, 32, 32)), false);
        assertEquals(1, player.getSharedKill("szczur"));
        assertEquals(2, remaining());
    }
    @Test public void deathmatchKillStillAwardsArenaPointsButNotHuntingCredit() {
        player.setQuest("deathmatch", DeathmatchState.createStartState(10).toQuestString());
        DeathmatchInfo info = new DeathmatchInfo(new Area(zone, new Rectangle2D.Double(0, 0, 32, 32)),
                zone, new Spot(zone, 1, 1));
        DeathMatchCreature victim = new DeathMatchCreature(rat(), info);
        victim.setPlayerToReward(player);
        int xp = player.getXP();
        kill(victim, true);
        assertEquals(10, victim.getDMPoints());
        assertEquals(xp, player.getXP());
        assertEquals(1, player.getSoloKill("szczur"));
        assertEquals(2, remaining());
    }
    @Test public void ordinaryCreatureInFadoBattlePitDoesNotCountEither() {
        zone.remove(player);
        MockStendlRPWorld.get().removeZone(zone);
        zone = new StendhalRPZone("int_fado_battle_pit", 32, 32);
        MockStendlRPWorld.get().addRPZone(zone);
        zone.add(player);
        kill(rat(), true);
        assertEquals(1, player.getSoloKill("szczur"));
        assertEquals(2, remaining());
    }
    @Test public void completedHuntAndDifferentTargetsAreNotChanged() {
        player.setQuest("hunting", "done;0;3;MEDIUM;1500");
        kill(new ArenaCreature(rat(), new Rectangle2D.Double(0, 0, 32, 32)), true);
        assertEquals("done;0;3;MEDIUM;1500", player.getQuest("hunting"));
        player.setQuest("hunting", "dzik,0,2,0,0;0;3;MEDIUM;1500");
        kill(new ArenaCreature(rat(), new Rectangle2D.Double(0, 0, 32, 32)), true);
        assertEquals("dzik,0,2,0,0;0;3;MEDIUM;1500", player.getQuest("hunting"));
    }
}
