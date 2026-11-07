/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.CoastTrailProgress.Encounter;

/**
 * Wreckers threaten only investigators on the active objective, never passing
 * travellers.
 */
final class CoastTrailGuard extends Creature {
	final Encounter role;

	CoastTrailGuard(Creature template, Encounter role) {
		super(template);
		this.role = role;
		setXP(0);
		clearDropItemList();
	}

	@Override
	public Creature getNewInstance() {
		return new CoastTrailGuard(this, role);
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {
		// Quest rewards replace all combat loot, including seasonal drops.
	}

	private boolean eligible(RPEntity target) {
		return target instanceof Player && target.getZone() == getZone()
				&& CoastTrailProgress.needs((Player) target, role)
				&& Math.abs(target.getX() - role.x) <= 8
				&& Math.abs(target.getY() - role.y) <= 8;
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
		if (getZone() != null)
			for (RPEntity entity : super.getEnemyList())
				if (eligible(entity) && (nextTo(entity, 4)
						|| getAttackingRPEntities().contains(entity)))
					result.add(entity);
		return result;
	}
}
