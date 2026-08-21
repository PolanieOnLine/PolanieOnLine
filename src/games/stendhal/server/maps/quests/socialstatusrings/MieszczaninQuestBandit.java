/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

/** One non-respawning attacker inside the private Mieszczanin hideout. */
final class MieszczaninQuestBandit extends Creature {
	private static final int SOURCE_LEVEL = 130;
	private static final int LEVEL_BOOST = 35;
	private static final int BASE_ATK = 1200;
	private static final int ATK_PER_SOURCE_LEVEL = 45;
	private static final int DEF_BOOST = 55;
	private static final int BASE_DEF = 85;
	private static final int HP_MULTIPLIER = 3;
	private static final int BASE_HP = 2800;
	private static final int HP_PER_SOURCE_LEVEL = 240;
	private static final double MIN_SPEED = 0.9;

	private final String ownerName;

	MieszczaninQuestBandit(final Creature template, final Player owner,
			final String visibleName, final String description,
			final int level, final int atk, final int def, final int hp) {
		super(template);
		ownerName = owner.getName();
		setName(visibleName);
		setDescription(description);
		setLevel(challengeLevel(level));
		setAtk(challengeAtk(level, atk));
		setRatk(0);
		setDef(challengeDef(level, def));
		initHP(challengeHP(level, hp));
		setSpeed(Math.max(getSpeed(), MIN_SPEED));
		setXP(0);
		clearDropItemList();
	}

	static int challengeLevel(final int sourceLevel) {
		return sourceLevel + LEVEL_BOOST;
	}

	static int challengeAtk(final int sourceLevel, final int sourceAtk) {
		final int offset = Math.max(0, sourceLevel - SOURCE_LEVEL);
		return Math.max(sourceAtk * 2, BASE_ATK + offset * ATK_PER_SOURCE_LEVEL);
	}

	static int challengeDef(final int sourceLevel, final int sourceDef) {
		final int offset = Math.max(0, sourceLevel - SOURCE_LEVEL);
		return Math.max(sourceDef + DEF_BOOST, BASE_DEF + offset * 5 / 2);
	}

	static int challengeHP(final int sourceLevel, final int sourceHP) {
		final int offset = Math.max(0, sourceLevel - SOURCE_LEVEL);
		return Math.max(sourceHP * HP_MULTIPLIER, BASE_HP + offset * HP_PER_SOURCE_LEVEL);
	}

	static double minimumSpeed() {
		return MIN_SPEED;
	}

	@Override
	public void onDead(final Killer killer, final boolean remove) {
		final StendhalRPZone zone = getZone();
		if (zone != null && !hasOtherLivingBandit(zone)) {
			final Player owner = findOwner(zone);
			if (owner != null && !MieszczaninHideoutProgress.isCleared(owner)) {
				MieszczaninHideoutProgress.markCleared(owner);
				owner.sendPrivateText("W kryjówce zapada cisza. Nikt z napastników nie stoi już między tobą a uwięzionym posłańcem.");
			}
		}
		super.onDead(killer, remove);
	}

	private boolean hasOtherLivingBandit(final StendhalRPZone zone) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninQuestBandit.class)) {
			if (entity != this && ((MieszczaninQuestBandit) entity).getHP() > 0) {
				return true;
			}
		}
		return false;
	}

	private Player findOwner(final StendhalRPZone zone) {
		for (final Player player : zone.getPlayers()) {
			if (ownerName.equals(player.getName())) {
				return player;
			}
		}
		return null;
	}
}
