package games.stendhal.client.gui.j2d.entity;

import java.awt.Graphics2D;

import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/** Keeps the ghost's visual flight separate from its ground position. */
class GhostPet2DView extends Pet2DView {

	@Override
	protected Sprite createWalkSprite(final Sprite tiles, final int y,
			final int width, final int height) {
		final SpriteStore store = SpriteStore.get();
		final Sprite first = store.getTile(tiles, 0, y, width, height);
		final Sprite neutral = store.getTile(tiles, width, y, width, height);
		final Sprite last = store.getTile(tiles, width * 2, y, width, height);
		return new AnimatedSprite(new Sprite[] {first, neutral, last, neutral}, 200, true);
	}

	static int hoverOffset(final long timeMillis) {
		return -3 + (int) Math.round(2 * Math.sin((timeMillis % 2400) * Math.PI * 2 / 2400));
	}

	@Override
	protected void drawEntity(final Graphics2D graphics, final int x, final int y,
			final int width, final int height) {
		super.drawEntity(graphics, x, y + hoverOffset(System.currentTimeMillis()), width, height);
	}
}
