/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class StunnedStatusHandlerTest {
	private StendhalRPZone zone;
	private StunnedStatusHandler handler;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("stunned-focus-test", 20, 20);
		MockStendlRPWorld.get().addRPZone(zone);
		handler = new StunnedStatusHandler();
	}

	@After
	public void tearDown() {
		MockStendlRPWorld.get().removeZone(zone);
	}

	@Test
	public void defaultDurationDependsOnStunnedTargetType() {
		final Player player = PlayerTestHelper.createPlayer("stun-duration-player");
		final Creature creature = SingletonRepository.getEntityManager()
				.getCreature("mysz domowa");
		final StunnedStatus status = new StunnedStatus();

		assertEquals(4, status.getDurationSeconds(player));
		assertEquals(3, status.getDurationSeconds(creature));
	}

	@Test
	public void playerKeepsClientFocusAndResumesSameTarget() {
		final Player player = PlayerTestHelper.createPlayer("focused-player");
		final Creature target = SingletonRepository.getEntityManager()
				.getCreature("mysz domowa");
		zone.add(player);
		zone.add(target);
		player.setTarget(target);

		handler.pausePlayerAttackKeepingFocus(player);

		assertNull("internal attack must be paused", player.getAttackTarget());
		assertTrue("client focus must remain", player.has("target"));
		assertEquals(target.getID().getObjectID(), player.getInt("target"));

		handler.resumePlayerAttackIfStillFocused(player);

		assertSame("same target should resume automatically", target,
				player.getAttackTarget());
		assertEquals(target.getID().getObjectID(), player.getInt("target"));
	}

	@Test
	public void explicitFocusCancellationPreventsAutomaticResume() {
		final Player player = PlayerTestHelper.createPlayer("cancelled-focus-player");
		final Creature target = SingletonRepository.getEntityManager()
				.getCreature("mysz domowa");
		zone.add(player);
		zone.add(target);
		player.setTarget(target);

		handler.pausePlayerAttackKeepingFocus(player);
		player.stopAttack();
		handler.resumePlayerAttackIfStillFocused(player);

		assertNull(player.getAttackTarget());
		assertFalse(player.has("target"));
	}

	@Test
	public void creatureKeepsItsRealAttackTargetWhileStunned() {
		final Creature creature = SingletonRepository.getEntityManager()
				.getCreature("mysz domowa");
		final Player target = PlayerTestHelper.createPlayer("creature-focus-target");
		zone.add(creature);
		zone.add(target);
		creature.setTarget(target);

		handler.pausePlayerAttackKeepingFocus(creature);

		assertSame("creature attack strategy pauses hits without dropping target",
				target, creature.getAttackTarget());
		assertTrue(creature.has("target"));
	}
}
