/*
 * Copyright (C) 2026 - PolanieOnLine
 * Licensed under the GNU General Public License, version 2 or later.
 */
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.common.QuestMarker;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

public class QuestMarkerRendererTest {
	@Test
	public void missingUnknownAndPreviousZoneMarkersAreHidden() {
		final RPObject user = new RPObject();
		final RPClass rpClass = new RPClass("quest_marker_renderer_test");
		rpClass.addAttribute(QuestMarker.ATTRIBUTE, Type.MAP);
		user.setRPClass(rpClass);
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(null, "zone", "7"));
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(user, "zone", "7"));
		user.put(QuestMarker.ATTRIBUTE, QuestMarker.ZONE_KEY, "zone");
		user.put(QuestMarker.ATTRIBUTE, "7", "ready");
		assertEquals(QuestMarker.READY, QuestMarkerRenderer.readMarker(user, "zone", "7"));
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(user, "other_zone", "7"));
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(user, "zone", "8"));
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(user, null, "7"));
		user.put(QuestMarker.ATTRIBUTE, "7", "unknown");
		assertEquals(QuestMarker.NONE, QuestMarkerRenderer.readMarker(user, "zone", "7"));
	}

	@Test
	public void allFourGlyphsHaveSmallStablePixelGeometryAndCorrectColors() {
		final QuestMarker[] markers = {QuestMarker.AVAILABLE, QuestMarker.REPEATABLE,
				QuestMarker.READY, QuestMarker.IN_PROGRESS};
		final int[] colors = {0xfff4d06f, 0xff79b9eb, 0xff82d878, 0xffa4a49b};
		for (int i = 0; i < markers.length; i++) {
			final BufferedImage image = render(markers[i]);
			assertEquals(colors[i], image.getRGB(7, 4));
			assertEquals(0xff211b16, image.getRGB(6, 3));
			assertEquals(0, image.getRGB(0, 0));
			final BufferedImage again = render(markers[i]);
			assertArrayEquals(image.getRGB(0, 0, 16, 20, null, 0, 16),
					again.getRGB(0, 0, 16, 20, null, 0, 16));
		}
		final BufferedImage hidden = render(QuestMarker.NONE);
		assertEquals(0, hidden.getRGB(6, 4));
	}

	private BufferedImage render(final QuestMarker marker) {
		final BufferedImage image = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			QuestMarkerRenderer.draw(graphics, marker, 2, 3);
		} finally {
			graphics.dispose();
		}
		return image;
	}
}
