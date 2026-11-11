package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Duszek;
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class CompanionSummonActionTest {
	private Player player;
	private StendhalRPZone zone;

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("companion-summon-test", 20, 20);
		MockStendlRPWorld.get().addRPZone(zone);
		player = PlayerTestHelper.createPlayer("companion-admin");
		zone.add(player);
		player.setPosition(2, 2);
	}

	@After
	public void tearDown() {
		MockStendlRPWorld.get().removeZone(zone);
	}

	private RPAction summon(String type) {
		RPAction action = new RPAction();
		action.put("creature", type);
		action.put("x", 5);
		action.put("y", 5);
		return action;
	}

	@Test
	public void summonedGhostBecomesThePlayersCompanion() {
		new SummonAction().perform(player, summon("duszek"));
		assertTrue(player.getPet() instanceof Duszek);
		assertSame(player, player.getPet().getOwner());
		assertSame(player.getPet(), zone.getEntityAt(5, 5));
	}

	@Test
	public void summoningAnotherGhostPreservesTheExistingCompanion() {
		new SummonAction().perform(player, summon("duszek"));
		Duszek ghost = (Duszek) player.getPet();
		assertNotNull(ghost);
		RPAction another = summon("duszek");
		another.put("x", 8);
		new SummonAction().perform(player, another);
		assertSame(ghost, player.getPet());
		assertNull(zone.getEntityAt(8, 5));
	}

	@Test
	public void itemAndEliteOptionsDoNotCreateAGhostOrReserveItsSlot() {
		RPAction action = summon("duszek");
		action.put("rarity", "legendary");
		new SummonAction().perform(player, action);
		assertFalse(player.hasPet());
		assertNull(zone.getEntityAt(5, 5));
		action.remove("rarity");
		action.put("elite", "true");
		new SummonAction().perform(player, action);
		assertFalse(player.hasPet());
		assertNull(zone.getEntityAt(5, 5));
	}

	@Test
	public void unresolvedPetDoesNotBreakSummoningOrRemovingAGoat() {
		player.put("pet", 123456);
		new SummonAction().perform(player, summon("goat"));
		Goat goat = player.getGoat();
		assertNotNull(goat);
		RPAction destroy = new RPAction();
		destroy.put("target", "#" + goat.getID().getObjectID());
		new DestroyAction().perform(player, destroy);
		assertFalse(player.hasGoat());
		assertNull(player.getGoat());
		assertTrue(player.hasPet());
	}
}
