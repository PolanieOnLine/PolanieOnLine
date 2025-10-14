package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

public class Glyph extends Item {
	private static final String ATTRIBUTE_ATTACK = "skill_atk";
	private static final String ATTRIBUTE_ATTACK_ALIAS = "atk";
	private static final String ATTRIBUTE_HEALTH = "health";

	public Glyph(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public Glyph(final Glyph glyph) {
		super(glyph);
	}

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			applyAttackBonus(player);
			applyHealthBonus(player);
		}

		return super.onEquipped(entity, slot);
	}

	@Override
	public boolean onUnequipped() {
		RPObject entity = this.getBaseContainer();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			reduceAtk(player);
			reduceHP(player);
		}
		return super.onUnequipped();
	}

	private int getAttackBonus() {
		if (has(ATTRIBUTE_ATTACK)) {
			return getInt(ATTRIBUTE_ATTACK);
		}
		if (has(ATTRIBUTE_ATTACK_ALIAS)) {
			return getInt(ATTRIBUTE_ATTACK_ALIAS);
		}
		return 0;
	}

	private int getHealthBonus() {
		return has(ATTRIBUTE_HEALTH) ? getInt(ATTRIBUTE_HEALTH) : 0;
	}

	private void applyAttackBonus(final Player player) {
		int attackBonus = getAttackBonus();
		if (attackBonus == 0) {
			return;
		}
		player.setAtk(player.getAtk() + attackBonus);
	}

	private void applyHealthBonus(final Player player) {
		int healthBonus = getHealthBonus();
		if (healthBonus == 0) {
			return;
		}
		player.setBaseHP(player.getBaseHP() + healthBonus);
	}

	private void reduceAtk(final Player player) {
		int attackBonus = getAttackBonus();
		if (attackBonus == 0) {
			return;
		}
		player.setAtk(player.getAtk() - attackBonus);
	}

	private void reduceHP(final Player player) {
		int healthBonus = getHealthBonus();
		if (healthBonus == 0) {
			return;
		}
		int newBaseHP = Math.max(player.getBaseHP() - healthBonus, 0);
		player.setBaseHP(newBaseHP);
		if (player.getHP() > newBaseHP) {
			player.setHP(newBaseHP);
		}
	}
}
