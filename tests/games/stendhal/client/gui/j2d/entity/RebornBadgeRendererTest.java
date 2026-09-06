/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static games.stendhal.client.gui.settings.SettingsProperties.REBORN_BADGES_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.Test;

import games.stendhal.client.entity.Player;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;

public class RebornBadgeRendererTest {
	private static final String ZONE = "reborn_badge_test";

	@Test
	public void readsExactPublicRebornCount() {
		final Player player = createPlayer(12);
		assertEquals(12, RebornBadgeRenderer.getDisplayedRebornCount(player));
	}

	@Test
	public void centersShieldAndNicknameAsSingleNameplate() {
		final Player player = createPlayer(6);
		final int x = 30;
		final int width = 32;
		final Rectangle nameplate = RebornBadgeRenderer.getNameplateBounds(
				player, x, 30, width, 6);
		final Rectangle badge = RebornBadgeRenderer.getBadgeBounds(
				player, x, 30, width, 6);

		assertEquals(nameplate.x, badge.x);
		assertEquals(18, badge.width);
		assertEquals(18, badge.height);

		final int entityCenterTwice = 2 * x + width;
		final int nameplateCenterTwice = 2 * nameplate.x + nameplate.width;
		assertTrue(Math.abs(entityCenterTwice - nameplateCenterTwice) <= 1);
	}

	@Test
	public void drawsLargeReadableShieldOnTitleLine() {
		final Player player = createPlayer(6);
		final int x = 70;
		final int y = 30;
		final int width = 32;
		final int titleOffset = 6;
		final BufferedImage image = new BufferedImage(192, 64,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			assertTrue(RebornBadgeRenderer.drawNameplate(player, graphics,
					x, y, width, titleOffset));
		} finally {
			graphics.dispose();
		}

		final Rectangle badge = RebornBadgeRenderer.getBadgeBounds(player,
				x, y, width, titleOffset);
		final int shieldAlpha = image.getRGB(
				badge.x + badge.width / 2,
				badge.y + badge.height / 2) >>> 24;
		assertTrue(shieldAlpha > 0);

		int brightPixels = 0;
		for (int py = badge.y; py < badge.y + badge.height; py++) {
			for (int px = badge.x; px < badge.x + badge.width; px++) {
				final int rgb = image.getRGB(px, py);
				final int red = rgb >> 16 & 0xff;
				final int green = rgb >> 8 & 0xff;
				final int blue = rgb & 0xff;
				if (red > 230 && green > 225 && blue > 180) {
					brightPixels++;
				}
			}
		}
		assertTrue(brightPixels >= 4);

		// The previous implementation rendered the badge to the right and below
		// the player name. That area must stay unused by the badge.
		final int oldFloatingAlpha = image.getRGB(x + width + 10,
				y + titleOffset + 10) >>> 24;
		assertEquals(0, oldFloatingAlpha);
	}

	@Test
	public void globalSettingHidesBadgesForAllPlayers() {
		final WtWindowManager windowManager = WtWindowManager.getInstance();
		final String previous = windowManager.getProperty(REBORN_BADGES_PROPERTY,
				"true");
		final Player player = createPlayer(6);
		final BufferedImage image = new BufferedImage(64, 64,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			windowManager.setProperty(REBORN_BADGES_PROPERTY, "false");
			assertFalse(RebornBadgeRenderer.isBadgeDisplayEnabled());
			assertFalse(RebornBadgeRenderer.drawNameplate(player, graphics,
					10, 30, 16, 6));
		} finally {
			windowManager.setProperty(REBORN_BADGES_PROPERTY, previous);
			graphics.dispose();
		}
	}

	@Test
	public void leavesStandardTitleRenderingForPlayersWithoutReborns() {
		final Player player = createPlayer(0);
		final BufferedImage image = new BufferedImage(64, 64,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			assertFalse(RebornBadgeRenderer.drawNameplate(player, graphics,
					10, 30, 16, 6));
		} finally {
			graphics.dispose();
		}
	}

	private Player createPlayer(final int reborns) {
		final RPObject object = new RPObject();
		object.put("type", "player");
		object.put("name", "badge-player");
		if (reborns > 0) {
			object.put("reborn_badge", reborns);
		}
		object.setID(new ID(1001, ZONE));

		final Player player = new Player();
		player.initialize(object);
		return player;
	}
}
