package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;

import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class TrailNavigationTargetsTest {
	@Test
	public void aMovingNpcKeepsTheSameObjectiveIdentity() {
		Player player = PlayerTestHelper.createPlayer("compass_moving_npc");
		player.setQuest(Step.KEEP_RETURN.slot, "ready");
		StendhalRPZone zone = new StendhalRPZone("compass_julius", 20, 20);
		SpeakerNPC npc = new SpeakerNPC("Julius");
		npc.setPosition(5, 5);
		zone.add(npc);
		SingletonRepository.getNPCList().add(npc);
		try {
			NavigationGoal first = TrailNavigationTargets.targets(player).get(0);
			npc.setPosition(6, 5);
			NavigationGoal moved = TrailNavigationTargets.targets(player).get(0);
			assertEquals(first.identity, moved.identity);
			assertEquals("npc:Julius", moved.identity);
			assertEquals(6, moved.x);
		} finally {
			SingletonRepository.getNPCList().remove("Julius");
		}
	}

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void keepTracksUseAuthoredTileAndRemoveRecordedClue() {
		Player player = PlayerTestHelper.createPlayer("compass_keep");
		player.setQuest(Step.MOUNTAIN_RETURN.slot, "done");
		player.setQuest(Step.KEEP_TRAIL.slot, "clues;0");
		List<NavigationGoal> goals = TrailNavigationTargets.targets(player);
		assertTrue(
				goals.stream()
						.anyMatch(g -> g.x == 13 && g.y == 122 && AdosKeepProgress.OUTSIDE.equals(g.zone)));
		player.setQuest(Step.KEEP_TRAIL.slot, "clues;1");
		goals = TrailNavigationTargets.targets(player);
		assertFalse(goals.stream().anyMatch(g -> g.x == 13 && g.y == 122));
		assertEquals(1, goals.size());
	}

	@Test
	public void unacceptedRejectedAndCompletedQuestsDoNotNavigate() {
		Player player = PlayerTestHelper.createPlayer("compass_inactive");
		assertTrue(TrailNavigationTargets.targets(player).isEmpty());
		player.setQuest(Step.DELIVERY.slot, "rejected");
		assertTrue(TrailNavigationTargets.targets(player).isEmpty());
		player.setQuest(Step.DELIVERY.slot, "done");
		assertTrue(TrailNavigationTargets.targets(player).isEmpty());
	}

	@Test
	public void coastClueProgressRemainsPrivateToEachPlayer() {
		Player first = PlayerTestHelper.createPlayer("compass_coast1");
		Player second = PlayerTestHelper.createPlayer("compass_coast2");
		for (Player player : new Player[] {first, second}) {
			player.setQuest(Step.WARSAW_TRUCE.slot, "done");
			player.setQuest(Step.COAST_WRECK.slot, "traces;0");
		}
		first.setQuest(Step.COAST_WRECK.slot, "traces;1");
		assertEquals(1, TrailNavigationTargets.targets(first).size());
		assertEquals(2, TrailNavigationTargets.targets(second).size());
	}
}
