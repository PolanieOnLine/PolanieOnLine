/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

/**
 * The old guardian targets only its current investigators, close to its
 * chamber.
 */
final class AdosKeepGuardian extends Creature {
	AdosKeepGuardian(Creature template) {
		super(template);
		setXP(0);
		clearDropItemList();
	}

	@Override
	public Creature getNewInstance() {
		return new AdosKeepGuardian(this);
	}

	private boolean eligible(RPEntity target) {
		return target instanceof Player && target.getZone() == getZone()
				&& AdosKeepProgress.needsGuardian((Player) target)
				&& Math.abs(target.getX() - AdosKeepProgress.GUARDIAN_X) <= 8
				&& Math.abs(target.getY() - AdosKeepProgress.GUARDIAN_Y) <= 8;
	}

	@Override
	public void logic() {
		if (getAttackTarget() != null && !eligible(getAttackTarget())) {
			setIdle();
		}
		super.logic();
		if (getAttackTarget() == null) {
			setIdle();
		}
	}

	@Override
	public void setIdle() {
		TrailGuardReturn.idle(this);
	}

	@Override
	public List<RPEntity> getEnemyList() {
		List<RPEntity> result = new ArrayList<>();
		if (getZone() == null)
			return result;
		for (RPEntity target : super.getEnemyList()) {
			if (eligible(target) && (nextTo(target, 4)
					|| getAttackingRPEntities().contains(target)))
				result.add(target);
		}
		return result;
	}
}
