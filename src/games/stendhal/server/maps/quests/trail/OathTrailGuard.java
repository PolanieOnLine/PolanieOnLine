/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.OathTrailProgress.Encounter;

/** Duels belong to the current investigator, never to passing travellers. */
final class OathTrailGuard extends Creature {
	final Encounter role;

	OathTrailGuard(Creature template, Encounter role) {
		super(template);
		this.role = role;
		setXP(0);
		clearDropItemList();
	}

	@Override
	public Creature getNewInstance() {
		return new OathTrailGuard(this, role);
	}

	private boolean eligible(RPEntity target) {
		return target instanceof Player && target.getZone() == getZone()
				&& OathTrailProgress.needs((Player) target, role)
				&& Math.abs(target.getX() - role.x) <= 8
				&& Math.abs(target.getY() - role.y) <= 8;
	}

	@Override
	public void logic() {
		if (getAttackTarget() != null && !eligible(getAttackTarget())) {
			stopAttack();
			clearPath();
			stop();
		}
		super.logic();
	}

	@Override
	public void setIdle() {
		clearPath();
		stopAttack();
		stop();
	}

	@Override
	public List<RPEntity> getEnemyList() {
		List<RPEntity> enemies = new ArrayList<>();
		if (getZone() == null)
			return enemies;
		for (RPEntity target : super.getEnemyList())
			if (eligible(target) && (nextTo(target, 4)
					|| getAttackingRPEntities().contains(target)))
				enemies.add(target);
		return enemies;
	}
}
