/***************************************************************************
 *                   Copyright (C) 2026 - PolanieOnLine                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.User;
import games.stendhal.common.QuestMarker;
import marauroa.common.game.RPObject;

/** Small static pixel glyphs. Images are made once, not on every frame. */
final class QuestMarkerRenderer {
	static final int WIDTH = 12;
	static final int HEIGHT = 16;
	private static final String[] EXCLAMATION =
			{"00100", "01110", "01110", "00100", "00100", "00000", "00100"};
	private static final String[] QUESTION =
			{"01110", "10001", "00001", "00110", "00100", "00000", "00100"};
	private static final Map<QuestMarker, BufferedImage> IMAGES = new EnumMap<>(QuestMarker.class);

	static {
		IMAGES.put(QuestMarker.AVAILABLE, create(EXCLAMATION, new Color(0xf4d06f)));
		IMAGES.put(QuestMarker.REPEATABLE, create(EXCLAMATION, new Color(0x79b9eb)));
		IMAGES.put(QuestMarker.READY, create(QUESTION, new Color(0x82d878)));
		IMAGES.put(QuestMarker.IN_PROGRESS, create(QUESTION, new Color(0xa4a49b)));
	}

	private QuestMarkerRenderer() {
	}

	static QuestMarker getMarker(final NPC npc) {
		if (User.isNull() || npc.getID() == null || !npc.showTitle()) {
			return QuestMarker.NONE;
		}
		return readMarker(User.get().getRPObject(), npc.getID().getZoneID(),
				String.valueOf(npc.getID().getObjectID()));
	}

	static QuestMarker readMarker(final RPObject user, final String zone, final String id) {
		if (user == null || zone == null || id == null
				|| !user.has(QuestMarker.ATTRIBUTE, QuestMarker.ZONE_KEY)
				|| !zone.equals(user.get(QuestMarker.ATTRIBUTE, QuestMarker.ZONE_KEY))
				|| !user.has(QuestMarker.ATTRIBUTE, id)) {
			return QuestMarker.NONE;
		}
		return QuestMarker.fromCode(user.get(QuestMarker.ATTRIBUTE, id));
	}

	static void draw(final Graphics2D graphics, final QuestMarker marker, final int x, final int y) {
		final BufferedImage image = IMAGES.get(marker);
		if (image != null) {
			graphics.drawImage(image, x, y, null);
		}
	}

	private static BufferedImage create(final String[] rows, final Color color) {
		final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();
		try {
			g.setColor(new Color(0x211b16));
			for (int y = 0; y < rows.length; y++) {
				for (int x = 0; x < rows[y].length(); x++) {
					if (rows[y].charAt(x) == '1') {
						g.fillRect(2 * x, 2 * y, 4, 4);
					}
				}
			}
			g.setColor(color);
			for (int y = 0; y < rows.length; y++) {
				for (int x = 0; x < rows[y].length(); x++) {
					if (rows[y].charAt(x) == '1') {
						g.fillRect(1 + 2 * x, 1 + 2 * y, 2, 2);
					}
				}
			}
		} finally {
			g.dispose();
		}
		return image;
	}
}
