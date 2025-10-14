package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

public class Glyph extends Item {
	public Glyph(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public Glyph(final Glyph glyph) {
		super(glyph);
	}

	private int getAttackBonus() {
		return has("skill_atk") ? getInt("skill_atk") : 0;
	}

	private int getHealthBonus() {
		return has("health") ? getInt("health") : 0;
	}

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.setAtk(player.getAtk() + getAttackBonus());
			player.setBaseHP(player.getBaseHP() + getHealthBonus());
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

	private void reduceHP(Player player) {
		int healthBonus = getHealthBonus();
		if (healthBonus == 0) {
			return;
		}
		int reduceHP = Math.max(player.getBaseHP() - healthBonus, 0);
		if (player.getHP() == player.getBaseHP() || player.getHP() > reduceHP) {
			player.setHP(reduceHP);
		}
		player.setBaseHP(reduceHP);
	}

	private void reduceAtk(Player player) {
		player.setAtk(player.getAtk() - getAttackBonus());
	}
}
