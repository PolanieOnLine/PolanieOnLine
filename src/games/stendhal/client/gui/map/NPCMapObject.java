package games.stendhal.client.gui.map;

import java.awt.Color;
import java.awt.Graphics;

import games.stendhal.client.entity.IEntity;

public class NPCMapObject extends RPEntityMapObject {
    NPCMapObject(final IEntity entity) {
		super(entity);
	}

    @Override
	void draw(final Graphics g, final int scale) {
		super.draw(g, scale);
	}

	@Override
	void draw(final Graphics g, final int scale, final Color color) {
		int mapX = worldToCanvas(x, scale);
		int mapY = worldToCanvas(y, scale);
		final int scale_2 = scale / 2;
		final int size = scale_2 + 2;

		mapX += scale_2;
		mapY += scale_2;

		g.setColor(Color.YELLOW);
		g.drawRect(mapX - 1, mapY - 1, size, size);
	}
}