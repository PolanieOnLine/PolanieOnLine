package games.stendhal.server.entity.npc.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class PlayerHasHarvestedNumberOfItemsGreaterThanConditionTest {

	@Before
	public void setUp() throws Exception {
		MockStendlRPWorld.get();
	}

	@After
	public void tearDown() throws Exception {
		MockStendlRPWorld.reset();
	}

	@Test
	public void testEqualsHashCode() {
		PlayerHasHarvestedNumberOfItemsCondition actual = new PlayerHasHarvestedNumberOfItemsCondition(1, "topór");
		assertThat(actual.toString(), is("player has harvested <1 of [topór]>"));
		assertThat(actual, is(actual));
		assertThat(actual.hashCode(), is(actual.hashCode()));
		assertThat(actual, is(new PlayerHasHarvestedNumberOfItemsCondition(1, "topór")));
		assertThat(actual.hashCode(), is(new PlayerHasHarvestedNumberOfItemsCondition(1, "topór").hashCode()));
		assertThat(actual, not(is(new PlayerHasHarvestedNumberOfItemsCondition(1, "sztylecik"))));
	}

	@Test
	public void testFire() {
		PlayerHasHarvestedNumberOfItemsCondition condition = new PlayerHasHarvestedNumberOfItemsCondition(5, "topór");
		Player player = PlayerTestHelper.createPlayer("looter");
		assertThat(condition.fire(player, null, null), is(false));
		player.incHarvestedForItem("sztylecik", 12);
		assertThat(condition.fire(player, null, null), is(false));
		player.incHarvestedForItem("topór", 4);
		assertThat(condition.fire(player, null, null), is(false));
		player.incHarvestedForItem("topór", 2);
		assertThat(condition.fire(player, null, null), is(true));
	}

}
