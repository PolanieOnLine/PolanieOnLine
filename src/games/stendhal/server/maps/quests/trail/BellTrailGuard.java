/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.BellTrailProgress.Encounter;

/** Quest guardians only engage players undertaking their current encounter. */
final class BellTrailGuard extends Creature {
	final Encounter role;

	BellTrailGuard(Creature template, Encounter role) {
		super(template);
		this.role = role;
		setName(role.name);
		setXP(0);
		clearDropItemList();
	}

	@Override
	public Creature getNewInstance() {
		return new BellTrailGuard(this, role);
	}

	@Override
	protected void rewardKillers(final int oldXP) {
		// Story progress replaces the generic minimum-one-XP combat award.
		for (Map.Entry<Entity, Integer> entry : damageReceived.entrySet()) {
			Player player = entityAsOnlinePlayer(entry.getKey());
			if (player != null && entry.getValue() > 0) {
				BellTrailProgress.recordKill(player, this);
			}
		}
	}

	@Override
	protected void rewardKillerAnimals(final int oldXP) {
		// Companions do not gain repeatable experience from story guardians.
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {
		// Story encounters never award loot, including live seasonal drops and
		// carried items.
	}

	private boolean eligible(RPEntity target) {
		return target instanceof Player && !((Player) target).isDisconnected()
				&& target.getZone() == getZone() && getZone() != null
				&& getZone().has(target.getID())
				&& BellTrailProgress.needs((Player) target, role)
				&& Math.abs(target.getX() - role.x) <= 24
				&& Math.abs(target.getY() - role.y) <= 24;
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
		clearPath();
		stopAttack();
		stop();
		if (getRespawnPoint() != null && getZone() != null
				&& getZone().has(getID()) && (Math.abs(getX() - role.x) > 2
						|| Math.abs(getY() - role.y) > 2)) {
			StendhalRPAction.placeat(getZone(), this, role.x, role.y);
		}
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
