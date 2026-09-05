/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.RaidCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class EliteSummonActionTest {
	private StendhalRPZone zone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("elite-summon-test") {
			@Override
			public synchronized boolean collides(final Entity entity,
					final double x, final double y) {
				return false;
			}
		};
	}

	@After
	public void tearDown() {
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@Test
	public void adminCanForceEligibleCreatureToEliteVariant() {
		final Player player = createAdmin();
		final RPAction action = summonAction("kamienny golem");
		action.put("elite", "true");

		CommandCenter.execute(player, action);

		final Creature creature = (Creature) zone.getEntityAt(0, 0);
		assertNotNull(creature);
		assertTrue(creature instanceof RaidCreature);
		assertEquals("elite", creature.get("title_type"));
		assertEquals("Elitarny kamienny golem", creature.get("title"));
		assertEquals("kamienny golem", creature.getName());
	}

	@Test
	public void eliteOptionCannotBeAppliedToItem() {
		final Player player = createAdmin();
		final RPAction action = summonAction("sztylecik");
		action.put("elite", "true");

		CommandCenter.execute(player, action);

		assertNull(zone.getEntityAt(0, 0));
	}

	@Test
	public void invalidEliteValueDoesNotSummonAnything() {
		final Player player = createAdmin();
		final RPAction action = summonAction("kamienny golem");
		action.put("elite", "sometimes");

		CommandCenter.execute(player, action);

		assertNull(zone.getEntityAt(0, 0));
	}

	private Player createAdmin() {
		final Player player = PlayerTestHelper.createPlayer("elite-admin");
		MockStendhalRPRuleProcessor.get().addPlayer(player);
		zone.add(player);
		player.setPosition(1, 1);
		player.put("adminlevel", 5000);
		return player;
	}

	private RPAction summonAction(final String name) {
		final RPAction action = new RPAction();
		action.put("type", "summon");
		action.put("creature", name);
		action.put("x", 0);
		action.put("y", 0);
		return action;
	}
}
