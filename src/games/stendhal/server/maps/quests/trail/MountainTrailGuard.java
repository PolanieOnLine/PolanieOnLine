/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.MountainTrailProgress.Encounter;

/** Shared encounter actors leave ordinary travellers alone. */
final class MountainTrailGuard extends Creature {
	final Encounter role;

	MountainTrailGuard(Creature template, Encounter role) {
		super(template);
		this.role = role;
		setXP(0);
		clearDropItemList();
	}

	@Override
	public Creature getNewInstance() {
		return new MountainTrailGuard(this, role);
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {
		// Quest rewards replace all combat loot, including seasonal drops.
	}

	private boolean eligible(RPEntity enemy) {
		return enemy instanceof Player && enemy.getZone() == getZone()
				&& MountainTrailProgress.needs((Player) enemy, role)
				&& Math.abs(enemy.getX() - role.x) <= 8
				&& Math.abs(enemy.getY() - role.y) <= 8;
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
		for (RPEntity enemy : super.getEnemyList()) {
			if (eligible(enemy) && (nextTo(enemy, 4)
					|| getAttackingRPEntities().contains(enemy)))
				result.add(enemy);
		}
		return result;
	}
}
