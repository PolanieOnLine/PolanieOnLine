package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.*;
import java.awt.geom.Rectangle2D;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.util.Area;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ChallengeArenaEngineLootPeriodTest {
	@BeforeClass public static void setup() throws Exception { QuestHelper.setUpBeforeClass(); }
	@After public void cleanup() { ChallengeArenaManager.clearForTests(); }

	@Test public void winnerHasOneMinuteToLootAndGetsRewardOnlyOnce() {
		final long[] now = {System.currentTimeMillis() + 1000};
		final StendhalRPZone zone = new StendhalRPZone("arena-grace-test", 40, 40);
		MockStendlRPWorld.get().addRPZone(zone);
		final Player player = PlayerTestHelper.createPlayer("ArenaWinnerGrace");
		zone.add(player);
		player.setPosition(20, 20);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(new Area(zone,
				new Rectangle2D.Double(1, 1, 38, 25)), zone, zone.getName(), 20, 30, 20, 20);
		ChallengeArenaManager.reserve(player.getName());
		player.setQuest(ChallengeArenaState.QUEST_SLOT, ChallengeArenaState.start(ChallengeArenaTier.TRIAL)
				.withSpawnedCreatures(ChallengeArenaTier.TRIAL.getCreatureCount()).serialize());
		final ChallengeArenaEngine engine = new ChallengeArenaEngine(player, info, () -> now[0]);
		engine.onTurnReached(1);
		assertTrue(info.isInArena(player));
		assertTrue(ChallengeArenaManager.isReservedBy(player.getName()));
		assertEquals(1, ChallengeArenaRewardService.getWins(player));
		now[0] += 59000;
		engine.onTurnReached(2);
		assertTrue(info.isInArena(player));
		assertEquals(1, ChallengeArenaRewardService.getWins(player));
		now[0] += 1000;
		engine.onTurnReached(3);
		assertFalse(info.isInArena(player));
		assertFalse(ChallengeArenaManager.isReserved());
		assertEquals(ChallengeArenaState.Lifecycle.DONE,
				ChallengeArenaState.parse(player.getQuest(ChallengeArenaState.QUEST_SLOT)).getLifecycle());
		assertEquals(1, ChallengeArenaRewardService.getWins(player));
	}
}
