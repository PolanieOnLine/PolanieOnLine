/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.Graphics2D;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.StatusID;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/** Draws the rotating stars above stunned combat entities. */
final class StunnedStarsRenderer {
	private static final int FRAME_DELAY_MS = 140;
	private static final Sprite STARS = SpriteStore.get().getAnimatedSprite(
			SpriteStore.get().getStatusSprite("stunned.png"), FRAME_DELAY_MS);

	private StunnedStarsRenderer() {
		// utility class
	}

	static void draw(final RPEntity entity, final Graphics2D g2d,
			final int x, final int y, final int width) {
		if (entity == null || !entity.hasStatus(StatusID.STUNNED)) {
			return;
		}

		final int sx = x + (width - STARS.getWidth()) / 2;
		// Keep the orbit half over the top of the body sprite: it reads as stars
		// circling the head and does not compete with the title/HP bar above it.
		final int sy = y - STARS.getHeight() / 2;
		STARS.draw(g2d, sx, sy);
	}
}
