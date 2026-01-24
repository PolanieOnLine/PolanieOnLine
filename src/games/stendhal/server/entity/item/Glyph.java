package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class Glyph extends Item {
	public Glyph(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public Glyph(final Glyph glyph) {
		super(glyph);
	}

	private int getAttackBonus(final Item glyph) {
		return glyph.has("skill_atk") ? glyph.getInt("skill_atk") : 0;
	}

	private int getHealthBonus(final Item glyph) {
		return glyph.has("health") ? glyph.getInt("health") : 0;
	}

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if (entity instanceof Player && isGlyphSlot(slot)) {
			entity.setAtk(entity.getAtk() + getAttackBonus(this));
			entity.setBaseHP(entity.getBaseHP() + getHealthBonus(this));
		}

		return super.onEquipped(entity, slot);
	}

	@Override
	public boolean onUnequipped() {
		RPObject entity = this.getBaseContainer();
		if (entity instanceof Player && isInGlyphSlot()) {
			Player player = (Player) entity;
			reduceAtk(player);
			reduceHP(player);
		}
		return super.onUnequipped();
	}

	private boolean isGlyphSlot(String slot) {
		return slot != null && Slots.GLYPHS.getNames().contains(slot);
	}

	private boolean isInGlyphSlot() {
		RPSlot slot = getContainerSlot();
		return slot != null && isGlyphSlot(slot.getName());
	}

	private void reduceHP(Player player) {
		int reduceHP = player.getBaseHP() - getHealthBonus(this);
		if (player.getHP() == player.getBaseHP() || player.getHP() > reduceHP) {
			player.setHP(reduceHP);
		}
		player.setBaseHP(reduceHP);
	}

	private void reduceAtk(Player player) {
		player.setAtk(player.getAtk() - getAttackBonus(this));
	}
}
