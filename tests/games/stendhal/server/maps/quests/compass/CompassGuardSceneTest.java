/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import static org.junit.Assert.*;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;
import marauroa.common.game.RPEvent;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** The staged rescue animates real actors without inflicting scripted damage. */
public class CompassGuardSceneTest {
	private StendhalRPZone forest;
	private Player owner, outsider;
	private SpeakerNPC wounded, previous;
	private CompassGuard wolf;

	@BeforeClass
	public static void world() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void scene() {
		previous = NPCList.get().remove(CompassProgress.WANDERER);
		forest = new StendhalRPZone(CompassProgress.FOREST, 128, 128);
		forest.protectionMap.init(128, 128);
		MockStendlRPWorld.get().addRPZone(forest);
		CompassScenes.register(forest);
		owner = PlayerTestHelper.createPlayer("rescue owner");
		outsider = PlayerTestHelper.createPlayer("rescue outsider");
		forest.add(owner);
		forest.add(outsider);
		MockStendhalRPRuleProcessor.get().addPlayer(owner);
		MockStendhalRPRuleProcessor.get().addPlayer(outsider);
		CompassProgress.enter(owner, Stage.WOLVES);
		refresh(owner);
		for (Entity entity : forest.getEntitiesOfClass(SpeakerNPC.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, owner)
					&& CompassProgress.WANDERER.equals(TrailSceneRegistry.speakerName(entity))) {
				wounded = (SpeakerNPC) entity;
			}
		}
		for (Entity entity : forest.getEntitiesOfClass(CompassGuard.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, owner) && ((CompassGuard) entity).role() == 0) {
				wolf = (CompassGuard) entity;
			}
		}
		assertNotNull(wounded);
		assertNotNull(wolf);
		owner.setPosition(1, 1);
		outsider.setPosition(2, 1);
	}

	private void refresh(Player player) {
		TrailSceneRegistry.refreshLater(player);
		int start = Math.max(0, TurnNotifier.get().getCurrentTurnForDebugging());
		for (int turn = start + 1; turn <= start + 3; turn++) TurnNotifier.get().logic(turn);
	}

	@After
	public void cleanup() {
		MockStendhalRPRuleProcessor.get().clearPlayers();
		MockStendlRPWorld.get().removeZone(forest);
		NPCList.get().remove(CompassProgress.WANDERER);
		if (previous != null) NPCList.get().add(previous);
	}

	@Test
	public void wolvesApproachCircleAndTradeHarmlessNativeAttacks() {
		int initialX = wolf.getX(), initialY = wolf.getY();
		int wolfHP = wolf.getHP(), woundedHP = wounded.getHP();
		for (int tick = 0; tick < 160; tick++) {
			wolf.logic();
			assertFalse("Lunges must not put the wolf on Dobromir's tile",
					wolf.getX() == wounded.getX() && wolf.getY() == wounded.getY());
		}
		assertTrue("Wolf must walk in from the forest", wolf.getX() != initialX || wolf.getY() != initialY);
		assertTrue("Wolf must reach the wounded traveller", wolf.nextTo(wounded, 3));
		assertHarmlessAttacks(wolf, wounded);
		assertHarmlessAttacks(wounded, wolf);
		assertEquals(wolfHP, wolf.getHP());
		assertEquals(woundedHP, wounded.getHP());
		assertNull("The staged attack must never engage server combat", wolf.getAttackTarget());
		assertNull(wounded.getAttackTarget());
		assertFalse(wounded.isAttackable());
		assertEquals("The player still has to kill both wolves", 0, CompassProgress.detail(owner));
		Set<String> positions = new HashSet<>();
		for (int tick = 0; tick < 80; tick++) {
			wolf.logic();
			positions.add(wolf.getX() + "," + wolf.getY());
		}
		assertTrue("The wolf must keep circling after arriving", positions.size() > 4);
	}

	private void assertHarmlessAttacks(Entity attacker, Entity defender) {
		boolean found = false;
		for (RPEvent event : attacker.events()) {
			if (Events.ATTACK.equals(event.getName())) {
				assertEquals(0, event.getInt("damage"));
				found = true;
			}
		}
		assertTrue("Native attack animation missing", found);
		assertEquals(defender.getID().getObjectID(), attacker.getInt("target"));
	}

	@Test
	public void ownerAttackStartsRealCombatAndOnlyActualDeathsAdvanceTheQuest() {
		wolf.setPosition(wounded.getX() - 1, wounded.getY());
		for (int tick = 0; tick < 12; tick++) wolf.logic();
		owner.setPosition(wolf.getX(), wolf.getY() + 1);
		StendhalRPAction.startAttack(owner, wolf);
		assertSame(wolf, owner.getAttackTarget());
		wolf.logic();
		assertSame(owner, wolf.getAttackTarget());
		assertFalse("Dobromir must stop the staged duel", wounded.has("target"));
		int before = wolf.getHP();
		assertEquals(1, wolf.receiveCombatDamage(owner, 1).getHealthDamage());
		assertEquals(before - 1, wolf.getHP());
		assertEquals(0, CompassProgress.detail(owner));
		int xp = owner.getXP();
		wolf.onDead(owner);
		assertEquals(1, CompassProgress.detail(owner));
		assertEquals(xp, owner.getXP());
		for (Entity corpse : forest.getEntitiesOfClass(Corpse.class)) {
			assertEquals(0, corpse.getSlot("content").size());
		}
	}

	@Test
	public void foreignRescueCannotSupplyAnOpponentOrStartRealCombat() {
		CompassProgress.enter(outsider, Stage.WOLVES);
		refresh(outsider);
		SpeakerNPC foreign = null;
		for (Entity entity : forest.getEntitiesOfClass(SpeakerNPC.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, outsider)
					&& CompassProgress.WANDERER.equals(TrailSceneRegistry.speakerName(entity))) {
				foreign = (SpeakerNPC) entity;
			}
		}
		assertNotNull(foreign);
		foreign.setPosition(wounded.getX() - 8, wounded.getY());
		wolf.setPosition(foreign.getX() - 1, foreign.getY());
		outsider.setPosition(wolf.getX(), wolf.getY() + 1);
		StendhalRPAction.startAttack(outsider, wolf);
		assertNull(outsider.getAttackTarget());
		int hp = wolf.getHP();
		assertEquals(0, wolf.receiveCombatDamage(outsider, 1000).getTotalDamage());
		for (int tick = 0; tick < 160; tick++) wolf.logic();
		assertTrue(wolf.nextTo(wounded, 3));
		assertHarmlessAttacks(wolf, wounded);
		assertTrue(foreign.events().isEmpty());
		assertNull(wolf.getAttackTarget());
		assertEquals(hp, wolf.getHP());
		assertEquals(0, CompassProgress.detail(owner));
	}

	@Test
	public void attackingOneWolfEngagesTheOwnersPackWithoutHarmingDobromir() {
		CompassGuard second = null;
		for (Entity entity : forest.getEntitiesOfClass(CompassGuard.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, owner) && ((CompassGuard) entity).role() == 1) {
				second = (CompassGuard) entity;
			}
		}
		assertNotNull(second);
		wolf.setPosition(wounded.getX() - 1, wounded.getY());
		second.setPosition(wounded.getX() + 1, wounded.getY());
		for (int tick = 0; tick < 12; tick++) {
			wolf.logic();
			second.logic();
		}
		int woundedHP = wounded.getHP();
		owner.setPosition(wounded.getX(), wounded.getY() + 2);
		StendhalRPAction.startAttack(owner, wolf);
		wolf.logic();
		second.logic();
		assertSame(owner, wolf.getAttackTarget());
		assertSame(owner, second.getAttackTarget());
		assertFalse(wounded.has("target"));
		assertEquals(woundedHP, wounded.getHP());
		assertEquals(0, CompassProgress.detail(owner));
	}
}
