package games.stendhal.server.maps.quests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.dragon.YoungBoyNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SmoczyAmuletTest {
	private static SmoczyAmulet quest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		MockStendlRPWorld.get();
		final StendhalRPZone zone = new StendhalRPZone("test_robercik");
		new YoungBoyNPC().configureZone(zone, null);
		quest = new SmoczyAmulet();
		quest.addToWorld();
	}

	@Test
	public void historyNamesAllRequiredDragonClaws() {
		final Player player = PlayerTestHelper.createPlayer("claw_hunter");
		player.setQuest(SmoczyAmulet.QUEST_SLOT, "start");

		assertThat(quest.getHistory(player), hasItem(
				player.getGenderVerb("Zgodziłem")
				+ " się zebrać dla Robercika trzy różne pazury: pazur zielonego smoka, pazur czerwonego smoka i pazur niebieskiego smoka."));
	}
}
