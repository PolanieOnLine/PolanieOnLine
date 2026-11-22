/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity.npc.behaviour.impl;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.MockStendlRPWorld;

public class TeleporterBehaviourTest {

	private StendhalRPWorld world;
	private StendhalRPZone source;
	private StendhalRPZone target;
	private SpeakerNPC npc;
	private Field randomField;
	private Object previousRandom;
	private TeleporterBehaviour behaviour;
	private final Set<TurnListener> existingListeners = Collections.newSetFromMap(new IdentityHashMap<TurnListener, Boolean>());

	@Before public void setup() throws Exception {
		world = MockStendlRPWorld.get();
		for (final Set<TurnListener> listeners : TurnNotifier.get().getEventListForDebugging().values()) existingListeners.addAll(listeners);
		source = new StendhalRPZone("teleporter_source_test", 16, 16);
		world.addRPZone(source);
		npc = new SpeakerNPC("Testowy podróżnik");
		npc.setPosition(3, 3);
		source.add(npc);
		randomField = Rand.class.getDeclaredField("rand");
		randomField.setAccessible(true);
		previousRandom = randomField.get(null);
		randomField.set(null, new Random(2));
	}

	@After public void cleanup() throws Exception {
		randomField.set(null, previousRandom);
		final Set<TurnListener> registered = Collections.newSetFromMap(new IdentityHashMap<TurnListener, Boolean>());
		for (final Set<TurnListener> listeners : TurnNotifier.get().getEventListForDebugging().values()) registered.addAll(listeners);
		for (final TurnListener listener : registered) if (!existingListeners.contains(listener)) TurnNotifier.get().dontNotify(listener);
		if (npc.getZone() != null) npc.getZone().remove(npc);
		world.removeRPZone(source.getID());
		if (target != null) world.removeRPZone(target.getID());
	}

	private TeleporterBehaviour destination(final StendhalRPZone zone) {
		target = zone;
		world.addRPZone(zone);
		behaviour = new TeleporterBehaviour(npc, Collections.singletonList(zone.getName()), "0_", "");
		return behaviour;
	}

	@Test public void blockedDestinationHasBoundedWorkAndKeepsNpcInItsOldZone() {
		final StendhalRPZone blocked = new StendhalRPZone("teleporter_blocked_test", 16, 16) {
			private int checks;
			@Override public boolean collides(final int x, final int y) {
				assertTrue("Placement must stop instead of looping until a tile becomes free", ++checks <= 256);
				return true;
			}
		};
		destination(blocked).onTurnReached(0);
		assertSame(source, npc.getZone());
		assertTrue(source.has(npc.getID()));
	}

	@Test public void emptyDestinationListKeepsNpcInItsOldZone() {
		behaviour = new TeleporterBehaviour(npc, Collections.emptyList(), "0_", "");
		behaviour.onTurnReached(0);
		assertSame(source, npc.getZone());
	}

	@Test public void undersizedDestinationIsSkippedWithoutRemovingNpc() {
		destination(new StendhalRPZone("teleporter_tiny_test", 4, 5)).onTurnReached(0);
		assertSame(source, npc.getZone());
	}

	@Test public void teleportNeverReusesAWalkingPathFromThePreviousMap() {
		npc.setPath(new FixedPath(Arrays.asList(new Node(3, 3), new Node(4, 3)), true));
		destination(new StendhalRPZone("teleporter_single_tile_test", 5, 6)).onTurnReached(0);
		assertSame(target, npc.getZone());
		assertFalse("No new walking route exists on this map", npc.hasPath());
	}

	@Test public void walkingRouteStaysLocalEvenOnALargeMap() {
		destination(new StendhalRPZone("teleporter_large_test", 512, 512)).onTurnReached(0);
		assertSame(target, npc.getZone());
		assertNotNull(npc.getPath());
		assertTrue(npc.isPathLoop());
		assertTrue(npc.getPath().getNodeList().size() > 1);
		for (final Node node : npc.getPath().getNodeList()) {
			assertTrue("A strolling NPC must not search a route across the whole map",
					Math.abs(node.getX() - npc.getX()) + Math.abs(node.getY() - npc.getY()) <= 48);
		}
	}

	@Test public void protectedConversationDefersTeleportWithoutChangingTheNpc() {
		destination(new StendhalRPZone("teleporter_conversation_test", 16, 16));
		behaviour.setExitsConversation(false);
		npc.setCurrentState(games.stendhal.server.entity.npc.ConversationStates.ATTENDING);
		behaviour.onTurnReached(0);
		assertSame(source, npc.getZone());
		assertEquals(3, npc.getX());
		assertEquals(3, npc.getY());
		assertEquals(games.stendhal.server.entity.npc.ConversationStates.ATTENDING, npc.getEngine().getCurrentState());
	}
}
