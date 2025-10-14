package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

public class Glyph extends Item {
	private static final String ATTRIBUTE_ATTACK = "skill_atk";
	private static final String ATTRIBUTE_ATTACK_ALIAS = "atk";
	private static final String ATTRIBUTE_DEFENSE = "def";
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
			applyDefenseBonus(player);
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
			reduceDefense(player);
			reduceHP(player);
		}
		return super.onUnequipped();
	}

	private int getIntAttribute(final String attributeName) {
		return has(attributeName) ? getInt(attributeName) : 0;
	}

	private void applyAttackBonus(final Player player) {
		int skillAttackBonus = getIntAttribute(ATTRIBUTE_ATTACK);
		if (skillAttackBonus != 0) {
			player.setAtk(player.getAtk() + skillAttackBonus);
		}

		int attackBonus = getIntAttribute(ATTRIBUTE_ATTACK_ALIAS);
		if (attackBonus != 0) {
			player.setAtk(player.getAtk() + attackBonus);
		}
	}

	private void applyDefenseBonus(final Player player) {
		int defenseBonus = getIntAttribute(ATTRIBUTE_DEFENSE);
		if (defenseBonus == 0) {
			return;
		}
		player.setDef(player.getDef() + defenseBonus);
	}

	private void applyHealthBonus(final Player player) {
		int healthBonus = getIntAttribute(ATTRIBUTE_HEALTH);
		if (healthBonus == 0) {
			return;
		}
		player.setBaseHP(player.getBaseHP() + healthBonus);
	}

	private void reduceAtk(final Player player) {
		int skillAttackBonus = getIntAttribute(ATTRIBUTE_ATTACK);
		if (skillAttackBonus != 0) {
			player.setAtk(player.getAtk() - skillAttackBonus);
		}

		int attackBonus = getIntAttribute(ATTRIBUTE_ATTACK_ALIAS);
		if (attackBonus != 0) {
			player.setAtk(player.getAtk() - attackBonus);
		}
	}

	private void reduceDefense(final Player player) {
		int defenseBonus = getIntAttribute(ATTRIBUTE_DEFENSE);
		if (defenseBonus == 0) {
			return;
		}
		int newDefense = player.getDef() - defenseBonus;
		player.setDef(Math.max(newDefense, 0));
	}

	private void reduceHP(final Player player) {
		int healthBonus = getIntAttribute(ATTRIBUTE_HEALTH);
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
