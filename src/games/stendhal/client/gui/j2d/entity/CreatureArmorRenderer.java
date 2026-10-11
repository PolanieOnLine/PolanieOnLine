/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.j2d.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.CreatureArmor;

/** Static armor icons shared with the web client, loaded once per armor type. */
final class CreatureArmorRenderer {
	static final int SIZE = 16;
	private static final Color FRAME = new Color(104, 116, 131);
	private static final Map<CreatureArmor, Sprite> ICONS = new EnumMap<>(CreatureArmor.class);

	private CreatureArmorRenderer() {
	}

	static void draw(final CreatureArmor armor, final Graphics2D graphics,
			final int barX, final int barY, final int barWidth, final int barHeight) {
		if (armor == null || armor == CreatureArmor.NONE) {
			return;
		}
		Sprite icon;
		synchronized (ICONS) {
			icon = ICONS.get(armor);
			if (icon == null) {
				icon = SpriteStore.get().getSprite(armor.getSpritePath());
				ICONS.put(armor, icon);
			}
		}
		icon.draw(graphics, barX - SIZE - 2, barY);
		final Graphics2D copy = (Graphics2D) graphics.create();
		try {
			copy.setColor(FRAME);
			copy.drawRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1);
		} finally {
			copy.dispose();
		}
	}
}
