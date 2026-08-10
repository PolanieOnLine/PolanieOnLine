/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature.impl.attack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import utilities.RPClass.CreatureTestHelper;

public class StunAwareAttackStrategyTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void stunnedCreatureCannotAttackOrReportAttackReady() {
		final RecordingStrategy delegate = new RecordingStrategy();
		final StunAwareAttackStrategy strategy = new StunAwareAttackStrategy(delegate);
		final Creature creature = new Creature();
		creature.put("status_stunned", 0);

		assertFalse(strategy.canAttackNow(creature));
		assertFalse(strategy.canAttackNow(creature, null));
		strategy.attack(creature);

		assertFalse(delegate.attackCalled);
		assertFalse(delegate.canAttackCalled);
		assertFalse(delegate.canAttackTargetCalled);
	}

	@Test
	public void stunDoesNotBlockPositioningOrTargetSelection() {
		final RecordingStrategy delegate = new RecordingStrategy();
		final StunAwareAttackStrategy strategy = new StunAwareAttackStrategy(delegate);
		final Creature creature = new Creature();
		creature.put("status_stunned", 0);

		strategy.getBetterAttackPosition(creature);
		strategy.findNewTarget(creature);
		assertTrue(strategy.hasValidTarget(creature));

		assertTrue(delegate.positionCalled);
		assertTrue(delegate.findTargetCalled);
		assertTrue(delegate.hasTargetCalled);
	}

	@Test
	public void normalCreatureStillUsesDelegateAttackFlow() {
		final RecordingStrategy delegate = new RecordingStrategy();
		final StunAwareAttackStrategy strategy = new StunAwareAttackStrategy(delegate);
		final Creature creature = new Creature();

		assertTrue(strategy.canAttackNow(creature));
		assertTrue(strategy.canAttackNow(creature, null));
		strategy.attack(creature);

		assertTrue(delegate.attackCalled);
		assertTrue(delegate.canAttackCalled);
		assertTrue(delegate.canAttackTargetCalled);
	}

	private static final class RecordingStrategy implements AttackStrategy {
		private boolean attackCalled;
		private boolean canAttackCalled;
		private boolean canAttackTargetCalled;
		private boolean positionCalled;
		private boolean findTargetCalled;
		private boolean hasTargetCalled;

		@Override
		public boolean canAttackNow(final Creature creature) {
			canAttackCalled = true;
			return true;
		}

		@Override
		public boolean canAttackNow(final Creature creature, final RPEntity target) {
			canAttackTargetCalled = true;
			return true;
		}

		@Override
		public void attack(final Creature creature) {
			attackCalled = true;
		}

		@Override
		public int getRange() {
			return 1;
		}

		@Override
		public void getBetterAttackPosition(final Creature creature) {
			positionCalled = true;
		}

		@Override
		public boolean hasValidTarget(final Creature creature) {
			hasTargetCalled = true;
			return true;
		}

		@Override
		public void findNewTarget(final Creature creature) {
			findTargetCalled = true;
		}
	}
}
