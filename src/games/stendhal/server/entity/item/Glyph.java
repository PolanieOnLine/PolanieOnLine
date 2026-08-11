package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.core.rule.glyph.GlyphEffectService;
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

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if (entity instanceof Player && isGlyphSlot(slot)) {
			/*
			 * Maximum HP is still stateful because base_hp is a persisted player
			 * attribute. skill_atk is intentionally not mutated here: DressedEntity
			 * resolves it dynamically from the currently equipped glyphs.
			 */
			GlyphEffectService.applyHealthOnEquipped((Player) entity, this);
		}

		return super.onEquipped(entity, slot);
	}

	@Override
	public boolean onUnequipped() {
		final RPObject entity = getBaseContainer();
		if (entity instanceof Player && isInGlyphSlot()) {
			GlyphEffectService.applyHealthOnUnequipped((Player) entity, this);
		}
		return super.onUnequipped();
	}

	private boolean isGlyphSlot(final String slot) {
		return slot != null && Slots.GLYPHS.getNames().contains(slot);
	}

	private boolean isInGlyphSlot() {
		final RPSlot slot = getContainerSlot();
		return slot != null && isGlyphSlot(slot.getName());
	}
}
