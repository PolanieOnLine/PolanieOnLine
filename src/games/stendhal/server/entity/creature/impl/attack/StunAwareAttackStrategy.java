/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature.impl.attack;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;

/**
 * Decorates every creature attack strategy with the common stun attack lock.
 * Targeting and positioning stay active so stun does not become a root effect.
 */
final class StunAwareAttackStrategy implements AttackStrategy {
	private static final String STUNNED_ATTRIBUTE = "status_stunned";

	private final AttackStrategy delegate;

	StunAwareAttackStrategy(final AttackStrategy delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("Attack strategy is required");
		}
		this.delegate = delegate;
	}

	AttackStrategy getDelegate() {
		return delegate;
	}

	@Override
	public boolean canAttackNow(final Creature creature) {
		return !isStunned(creature) && delegate.canAttackNow(creature);
	}

	@Override
	public boolean canAttackNow(final Creature creature, final RPEntity target) {
		return !isStunned(creature) && delegate.canAttackNow(creature, target);
	}

	@Override
	public void attack(final Creature creature) {
		if (!isStunned(creature)) {
			delegate.attack(creature);
		}
	}

	@Override
	public int getRange() {
		return delegate.getRange();
	}

	@Override
	public void getBetterAttackPosition(final Creature creature) {
		delegate.getBetterAttackPosition(creature);
	}

	@Override
	public boolean hasValidTarget(final Creature creature) {
		return delegate.hasValidTarget(creature);
	}

	@Override
	public void findNewTarget(final Creature creature) {
		delegate.findNewTarget(creature);
	}

	private boolean isStunned(final Creature creature) {
		return creature != null && creature.has(STUNNED_ATTRIBUTE);
	}
}
