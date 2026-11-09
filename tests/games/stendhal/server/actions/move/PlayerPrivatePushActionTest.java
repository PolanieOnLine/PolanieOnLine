/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.actions.move;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class PlayerPrivatePushActionTest {

	private Player owner;
	private Player outsider;
	private Creature actor;
	private RPAction push;

	@BeforeClass
	public static void world() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Before
	public void scene() {
		MockStendhalRPRuleProcessor.get().setTurn(11);
		StendhalRPZone zone = new StendhalRPZone("private-push", 10, 10);
		owner = PlayerTestHelper.createPlayer("push owner");
		outsider = PlayerTestHelper.createPlayer("push outsider");
		owner.setPosition(1, 1);
		outsider.setPosition(3, 4);
		zone.add(owner);
		zone.add(outsider);
		actor = new Creature();
		actor.put("name", "private push actor");
		actor.setPosition(4, 4);
		actor.setSize(1, 1);
		actor.setPath(new FixedPath(Arrays.asList(new Node(4, 4),
				new Node(4, 5), new Node(4, 6)), false));
		PlayerPrivateScene.assign(actor, owner);
		zone.add(actor);
		push = new RPAction();
		push.put("type", "push");
		push.put(Actions.TARGET, "#" + actor.getID().getObjectID());
	}

	@After
	public void resetTurn() {
		MockStendhalRPRuleProcessor.get().setTurn(0);
	}

	@Test
	public void outsiderForgingPrivateActorIdCannotMoveItOrClearItsPath() {
		new PushAction().onAction(outsider, push);
		assertEquals(4, actor.getX());
		assertEquals(4, actor.getY());
		assertTrue(actor.hasPath());
		assertEquals(0, outsider.events().size());
	}

	@Test
	public void outsiderIsRejectedBeforePushThrottleFeedback() {
		MockStendhalRPRuleProcessor.get().setTurn(0);
		new PushAction().onAction(outsider, push);
		assertEquals(0, outsider.events().size());
		assertTrue(actor.hasPath());
	}

	@Test
	public void ownerCanStillPushPrivateActorAndClearItsOldPath() {
		owner.setPosition(3, 4);
		outsider.setPosition(1, 1);
		new PushAction().onAction(owner, push);
		assertEquals(5, actor.getX());
		assertEquals(4, actor.getY());
		assertFalse(actor.hasPath());
	}
}
