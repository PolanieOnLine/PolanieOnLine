package games.stendhal.server.entity.item.behavior;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.navigation.CompassService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class WanderersCompassUseBehaviorTest {
	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void usingTheRewardHidesAndShowsTheBarWithoutConsumingTheCompass() {
		Player player = owner("compass_use");
		Item compass = compass(player);
		assertEquals("Pokaż / schowaj kompas|Użyj", compass.get("menu"));
		CompassService.update(player);
		assertFalse(player.has("compass_hidden"));
		assertTrue(compass.onUsed(player));
		assertEquals(1, player.getInt("compass_hidden"));
		CompassService.update(player);
		assertEquals("A periodic refresh must preserve the player's choice", 1, player.getInt("compass_hidden"));
		assertTrue(compass.onUsed(player));
		assertEquals(0, player.getInt("compass_hidden"));
		assertTrue(player.isEquipped("kompas wędrowca"));
		assertSame(player, compass.getBaseContainer());
	}

	@Test
	public void anUnfinishedQuestAndAnotherPlayersCompassCannotUnlockTheBar() {
		Player player = owner("compass_locked");
		Item compass = compass(player);
		player.setQuest("wanderers_compass", "return");
		assertFalse(compass.onUsed(player));
		assertFalse(player.has("compass_hidden"));
		player.setQuest("wanderers_compass", "done");
		compass.setBoundTo("Inny gracz");
		assertFalse(compass.onUsed(player));
		assertFalse(player.has("compass_hidden"));
	}

	private Player owner(String name) {
		Player player = PlayerTestHelper.createPlayer(name);
		player.setQuest("wanderers_compass", "done");
		StendhalRPZone zone = new StendhalRPZone(name + "_zone", 20, 20);
		zone.add(player);
		return player;
	}

	private Item compass(Player player) {
		Item compass = SingletonRepository.getEntityManager().getItem("kompas wędrowca");
		assertNotNull(compass);
		compass.setBoundTo(player.getName());
		assertTrue(player.equipToInventoryOnly(compass));
		return compass;
	}
}
