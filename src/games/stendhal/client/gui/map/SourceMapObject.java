/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.map;

import java.awt.Color;
import java.awt.Graphics;

import games.stendhal.client.entity.IEntity;

class SourceMapObject extends StaticMapObject {
	private static final Color COLOR = new Color(200, 255, 200);

	SourceMapObject(final IEntity entity) {
		super(entity);
	}

	@Override
	void draw(final Graphics g, final int scale) {
		draw(g, scale, COLOR);
	}

	void draw(final Graphics g, final int scale, final Color color) {
		final int rx = worldToCanvas(x, scale);
		final int ry = worldToCanvas(y, scale);
		final int rwidth = width * scale;
		final int rheight = height * scale;

		g.setColor(color);
		g.fillRect(rx, ry, rwidth, rheight);
	}
}
