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

	private int getAttackBonus(final Player player) {
		int atk = 0;
		for (final Item equip: player.getAllEquippedGlyphs()) {
			atk += equip.has("skill_atk") ? equip.getInt("skill_atk") : 0;
		}
		return atk;
	}

	private int getHealthBonus(final Player player) {
		int hp = 0;
		for (final Item equip: player.getAllEquippedGlyphs()) {
			hp += equip.has("health") ? equip.getInt("health") : 0;
		}
		return hp;
	}

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			entity.setAtk(entity.getAtk() + getAttackBonus(player));
			entity.setBaseHP(entity.getBaseHP() + getHealthBonus(player));
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
		int reduceHP = player.getBaseHP() - getHealthBonus(player);
		if (player.getHP() == player.getBaseHP() || player.getHP() > reduceHP) {
			player.setHP(reduceHP);
		}
		player.setBaseHP(reduceHP);
	}
	
	private void reduceAtk(Player player) {
		player.setAtk(player.getAtk() - getAttackBonus(player));
	}
}
