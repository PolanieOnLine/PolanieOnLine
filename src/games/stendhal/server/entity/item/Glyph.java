package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

public class Glyph extends Item {
	private static final String ATTRIBUTE_ATTACK = "skill_atk";
	private static final String ATTRIBUTE_ATTACK_ALIAS = "atk";
	private static final String ATTRIBUTE_HEALTH = "health";
	private static final String ATTRIBUTE_APPLIED_ATTACK = "applied_glyph_attack_bonus";
	private static final String ATTRIBUTE_APPLIED_HEALTH = "applied_glyph_health_bonus";

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
			forgetAppliedAttackBonus();
			return;
		}
		player.setAtk(player.getAtk() + attackBonus);
		rememberAppliedAttackBonus(attackBonus);
	}

	private void applyHealthBonus(final Player player) {
		int healthBonus = getHealthBonus();
		if (healthBonus == 0) {
			forgetAppliedHealthBonus();
			return;
		}
		player.setBaseHP(player.getBaseHP() + healthBonus);
		rememberAppliedHealthBonus(healthBonus);
	}

	private void reduceAtk(final Player player) {
		int appliedAttackBonus = getAppliedAttackBonus();
		if (appliedAttackBonus == 0) {
			return;
		}
		player.setAtk(player.getAtk() - appliedAttackBonus);
		forgetAppliedAttackBonus();
	}

	private void reduceHP(final Player player) {
		int appliedHealthBonus = getAppliedHealthBonus();
		if (appliedHealthBonus == 0) {
			return;
		}
		int newBaseHP = Math.max(player.getBaseHP() - appliedHealthBonus, 0);
		player.setBaseHP(newBaseHP);
		if (player.getHP() > newBaseHP) {
			player.setHP(newBaseHP);
		}
		forgetAppliedHealthBonus();
	}

	private void rememberAppliedAttackBonus(final int bonus) {
		put(ATTRIBUTE_APPLIED_ATTACK, bonus);
	}

	private void rememberAppliedHealthBonus(final int bonus) {
		put(ATTRIBUTE_APPLIED_HEALTH, bonus);
	}

	private void forgetAppliedAttackBonus() {
		if (has(ATTRIBUTE_APPLIED_ATTACK)) {
			remove(ATTRIBUTE_APPLIED_ATTACK);
		}
	}

	private void forgetAppliedHealthBonus() {
		if (has(ATTRIBUTE_APPLIED_HEALTH)) {
			remove(ATTRIBUTE_APPLIED_HEALTH);
		}
	}

	private int getAppliedAttackBonus() {
		return has(ATTRIBUTE_APPLIED_ATTACK) ? getInt(ATTRIBUTE_APPLIED_ATTACK) : 0;
	}

	private int getAppliedHealthBonus() {
		return has(ATTRIBUTE_APPLIED_HEALTH) ? getInt(ATTRIBUTE_APPLIED_HEALTH) : 0;
	}
}
