/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.npc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import utilities.PlayerTestHelper;

public class PlayerPrivateSpeakerNPCTest {

	@BeforeClass
	public static void beforeClass() {
		PlayerTestHelper.generatePlayerRPClasses();
		PlayerTestHelper.generateNPCRPClasses();
		MockStendlRPWorld.get();
	}

	@Test
	public void npcRpClassDeclaresPrivatePerceptionAttributes() {
		final Set<String> names = new HashSet<String>();
		for (final Definition definition : RPClass.getRPClass("npc").getDefinitions()) {
			names.add(definition.getName());
		}

		assertTrue(names.contains(PlayerPrivateSpeakerNPC.PERCEPTION_KEY_ATTRIBUTE));
		assertTrue(names.contains(PlayerPrivateSpeakerNPC.PERCEPTION_VALUE_ATTRIBUTE));
		assertTrue(names.contains(PlayerPrivateSpeakerNPC.OWNER_COLLISION_ONLY_ATTRIBUTE));
	}

	@Test
	public void privateNpcKeepsVisibleNameButHasUniqueInternalName() {
		final Player alice = PlayerTestHelper.createPlayer("Alice");
		final Player bob = PlayerTestHelper.createPlayer("Bob");

		final PlayerPrivateSpeakerNPC aliceNpc = new PlayerPrivateSpeakerNPC(alice, "Witomir");
		final PlayerPrivateSpeakerNPC bobNpc = new PlayerPrivateSpeakerNPC(bob, "Witomir");

		assertEquals("Witomir", aliceNpc.getTitle());
		assertEquals("Witomir", bobNpc.getTitle());
		assertNotEquals(aliceNpc.getName(), bobNpc.getName());
		assertEquals("Alice", aliceNpc.getOwnerName());
		assertEquals("Bob", bobNpc.getOwnerName());
		assertTrue(aliceNpc.has(PlayerPrivateSpeakerNPC.OWNER_COLLISION_ONLY_ATTRIBUTE));
	}

	@Test
	public void privateNpcOnlyBlocksItsOwner() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateSpeakerNPC npc = new PlayerPrivateSpeakerNPC(owner, "Witomir");

		assertTrue(npc.isOwnedBy(owner));
		assertFalse(npc.isOwnedBy(stranger));
		assertTrue(npc.isObstacle(owner));
		assertFalse(npc.isObstacle(stranger));
	}

	@Test
	public void zoneCollisionIgnoresPrivateNpcForOtherPlayer() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateSpeakerNPC npc = new PlayerPrivateSpeakerNPC(owner, "Witomir");
		final StendhalRPZone zone = new StendhalRPZone("test_private_npc_collision", 10, 10);

		owner.setPosition(1, 1);
		stranger.setPosition(2, 1);
		npc.setPosition(4, 4);
		zone.add(owner);
		zone.add(stranger);
		zone.add(npc);

		assertTrue(zone.collidesObjects(owner, owner.getArea(4, 4)));
		assertFalse(zone.collidesObjects(stranger, stranger.getArea(4, 4)));
	}

	@Test
	public void travellingPrivateNpcIgnoresGreetingUntilPathIsFinished() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final PlayerPrivateSpeakerNPC npc = new PlayerPrivateSpeakerNPC(owner, "Witomir");
		final StendhalRPZone zone = new StendhalRPZone("test_private_npc_travel", 10, 10);

		owner.setPosition(2, 2);
		npc.setPosition(1, 1);
		npc.setBaseSpeed(1.0);
		npc.addGreeting("Witaj.");
		zone.add(owner);
		zone.add(npc);

		npc.setPath(new FixedPath(Arrays.asList(
				new Node(1, 1), new Node(2, 1), new Node(3, 1)), false));
		owner.put("text", "hi");
		npc.preLogic();

		assertFalse(npc.isTalking());
		assertNull(npc.getAttending());
		assertTrue(npc.hasPath());
		owner.remove("text");

		while (npc.hasPath()) {
			npc.preLogic();
		}

		owner.put("text", "hi");
		npc.preLogic();
		assertTrue(npc.isTalking());
		assertEquals(owner, npc.getAttending());
	}
}
