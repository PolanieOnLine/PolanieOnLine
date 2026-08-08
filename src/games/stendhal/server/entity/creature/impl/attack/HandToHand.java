/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.creature.impl.attack;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AttackEvent;

class HandToHand implements AttackStrategy {

	private static final int FOLLOW_RADIUS = 12;

	@Override
	public void attack(final Creature creature) {
		if (creature.isAttackTurn(SingletonRepository.getRuleProcessor().getTurn())) {
			final RPEntity defender = creature.getAttackTarget();
			if (defender instanceof Player
					&& rollParry((Player) defender)) {
				defender.rememberAttacker(creature);

				final Item attackWeapon = creature.getWeapon();
				final String weaponClass = attackWeapon == null
						? null : attackWeapon.getWeaponType();
				creature.addEvent(new AttackEvent(true, 0, Nature.CUT,
						weaponClass, false, true));
				creature.notifyWorldAboutChanges();
				return;
			}
			creature.attack();
		}
	}

	/**
	 * Resolves parry separately from attack orchestration so the integration
	 * behavior can be tested deterministically without changing combat RNG.
	 */
	boolean rollParry(final Player player) {
		return ParryService.rollParry(player);
	}

	@Override
	public boolean canAttackNow(final Creature creature) {
		return canAttackNow(creature, creature.getAttackTarget());
	}

	@Override
	public boolean canAttackNow(Creature attacker, RPEntity target) {
		if (target != null) {
			return attacker.squaredDistance(target) < 1;
		}
		return false;
	}

	@Override
	public void findNewTarget(final Creature creature) {
		final RPEntity enemy = creature.getNearestEnemy(creature.getPerceptionRange()+2);
		if (enemy != null) {
			creature.setTarget(enemy);
		}
	}

	@Override
	public void getBetterAttackPosition(final Creature creature) {
		final games.stendhal.server.entity.Entity target = creature.getAttackTarget();
		if (creature.hasTargetMoved()) {
			creature.setMovement(target, 0, 1, creature.getMovementRange());
		}
		if (!creature.hasPath()) {
			if ((int) creature.squaredDistance(target) >= 1) {
				creature.stopAttack();
				return;
			}
		}
		creature.faceToward(creature.getAttackTarget());
	}

	@Override
	public boolean hasValidTarget(final Creature creature) {
		if (!creature.isAttacking()) {
			return false;
		}

		final RPEntity victim = creature.getAttackTarget();
		if (victim.isInvisibleToCreatures()) {
			return false;
		}
		if (!victim.getZone().equals(creature.getZone())) {
			return false;
		}

		if (!creature.getZone().has(victim.getID())) {
			return false;
		}
		return creature.squaredDistance(victim) < (FOLLOW_RADIUS * FOLLOW_RADIUS);
	}

	@Override
	public int getRange() {
		return 0;
	}
}
