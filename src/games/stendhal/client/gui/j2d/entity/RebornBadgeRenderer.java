/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import static games.stendhal.client.gui.settings.SettingsProperties.REBORN_BADGES_PROPERTY;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.client.entity.Player;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.TextSprite;
import marauroa.common.game.RPObject;

/** Draws the reborn counter as part of the player nameplate. */
final class RebornBadgeRenderer {
	private static final String ATTR_REBORN_BADGE = "reborn_badge";
	private static final int BADGE_WIDTH = 18;
	private static final int BADGE_HEIGHT = 18;
	private static final int NAME_GAP = 3;

	private static final Color SHIELD_OUTLINE = new Color(29, 24, 20);
	private static final Color INNER = new Color(47, 43, 39);
	private static final Color INNER_HIGHLIGHT = new Color(104, 91, 72);
	private static final Color INNER_SHADOW = new Color(20, 18, 17);
	private static final Color TEXT = new Color(255, 252, 224);
	private static final Color TEXT_SHADOW = new Color(8, 7, 6);
	private static final Color BRONZE = new Color(151, 104, 62);
	private static final Color SILVER = new Color(188, 190, 184);
	private static final Color GOLD = new Color(211, 164, 48);
	private static final Color AMETHYST = new Color(147, 101, 172);
	private static final Color LEGENDARY = new Color(235, 190, 62);

	private static final Map<Player, CachedTitle> TITLE_CACHE =
			new WeakHashMap<Player, CachedTitle>();

	private RebornBadgeRenderer() {
	}

	static boolean isBadgeDisplayEnabled() {
		return WtWindowManager.getInstance().getPropertyBoolean(
				REBORN_BADGES_PROPERTY, true);
	}

	static int getDisplayedRebornCount(final Player player) {
		if (player == null) {
			return 0;
		}
		final RPObject object = player.getRPObject();
		if (object == null || !object.has(ATTR_REBORN_BADGE)) {
			return 0;
		}
		return Math.max(0, object.getInt(ATTR_REBORN_BADGE));
	}

	/**
	 * Draws one centered nameplate containing the shield and player title.
	 *
	 * @return true when the reborn nameplate was drawn
	 */
	static boolean drawNameplate(final Player player, final Graphics2D g2d,
			final int x, final int statusY, final int width,
			final int titleDrawYOffset) {
		if (!canDrawNameplate(player)) {
			return false;
		}

		final int reborns = getDisplayedRebornCount(player);
		final Sprite titleSprite = getTitleSprite(player);
		final Rectangle nameplate = getNameplateBounds(player, x, statusY, width,
				titleDrawYOffset);
		final Rectangle badge = getBadgeBounds(player, x, statusY, width,
				titleDrawYOffset);
		final int titleX = nameplate.x + BADGE_WIDTH + NAME_GAP;
		final int titleY = nameplate.y;

		drawShield(g2d, reborns, badge.x, badge.y);
		titleSprite.draw(g2d, titleX, titleY);
		return true;
	}

	static Rectangle getNameplateBounds(final Player player, final int x,
			final int statusY, final int width, final int titleDrawYOffset) {
		if (!canDrawNameplate(player)) {
			return new Rectangle();
		}

		final Sprite titleSprite = getTitleSprite(player);
		final int totalWidth = BADGE_WIDTH + NAME_GAP + titleSprite.getWidth();
		final int startX = x + (width - totalWidth) / 2;
		final int titleY = statusY - (3 + titleSprite.getHeight())
				+ titleDrawYOffset;
		return new Rectangle(startX, titleY, totalWidth, titleSprite.getHeight());
	}

	static Rectangle getBadgeBounds(final Player player, final int x,
			final int statusY, final int width, final int titleDrawYOffset) {
		final Rectangle nameplate = getNameplateBounds(player, x, statusY, width,
				titleDrawYOffset);
		if (nameplate.width == 0) {
			return new Rectangle();
		}

		final int badgeY = nameplate.y
				+ (nameplate.height - BADGE_HEIGHT) / 2;
		return new Rectangle(nameplate.x, badgeY, BADGE_WIDTH, BADGE_HEIGHT);
	}

	private static boolean canDrawNameplate(final Player player) {
		return isBadgeDisplayEnabled() && player != null && player.showTitle()
				&& player.getTitle() != null && getDisplayedRebornCount(player) > 0;
	}

	private static Sprite getTitleSprite(final Player player) {
		final String title = player.getTitle();
		final Color color = getNameColor(player);
		synchronized (TITLE_CACHE) {
			final CachedTitle cached = TITLE_CACHE.get(player);
			if (cached != null && cached.matches(title, color)) {
				return cached.sprite;
			}

			final Sprite sprite = TextSprite.createTextSprite(title, color);
			TITLE_CACHE.put(player, new CachedTitle(title, color.getRGB(), sprite));
			return sprite;
		}
	}

	private static Color getNameColor(final Player player) {
		final String titleType = player.getTitleType();
		final int adminlevel = player.getAdminLevel();

		if ("npc".equals(titleType)) {
			return new Color(200, 200, 255);
		}
		if ("enemy".equals(titleType)) {
			return new Color(255, 200, 200);
		}
		if (adminlevel >= 1000) {
			return new Color(150, 149, 34);
		}
		if (adminlevel >= 20) {
			return new Color(200, 200, 0);
		}
		if (adminlevel >= 7) {
			return new Color(255, 255, 172);
		}
		if (adminlevel >= 3) {
			return new Color(185, 255, 185);
		}
		if (adminlevel > 0) {
			return new Color(205, 255, 205);
		}
		return Color.white;
	}

	private static void drawShield(final Graphics2D source,
			final int reborns, final int x, final int y) {
		final Graphics2D g2d = (Graphics2D) source.create();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			fillOuterShield(g2d, SHIELD_OUTLINE, x, y);
			fillShieldRim(g2d, getBorderColor(reborns), x, y);
			fillInnerShield(g2d, x, y);

			g2d.setColor(INNER_HIGHLIGHT);
			g2d.fillRect(x + 4, y + 4, 10, 1);
			g2d.setColor(INNER_SHADOW);
			g2d.fillRect(x + 6, y + 12, 6, 1);

			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			drawCount(g2d, reborns, x, y);
		} finally {
			g2d.dispose();
		}
	}

	private static void fillOuterShield(final Graphics2D g2d,
			final Color color, final int x, final int y) {
		g2d.setColor(color);
		g2d.fillRect(x + 4, y, 10, 1);
		g2d.fillRect(x + 2, y + 1, 14, 1);
		g2d.fillRect(x + 1, y + 2, 16, 1);
		g2d.fillRect(x, y + 3, 18, 6);
		g2d.fillRect(x + 1, y + 9, 16, 3);
		g2d.fillRect(x + 2, y + 12, 14, 1);
		g2d.fillRect(x + 3, y + 13, 12, 1);
		g2d.fillRect(x + 4, y + 14, 10, 1);
		g2d.fillRect(x + 5, y + 15, 8, 1);
		g2d.fillRect(x + 6, y + 16, 6, 1);
		g2d.fillRect(x + 8, y + 17, 2, 1);
	}

	private static void fillShieldRim(final Graphics2D g2d,
			final Color color, final int x, final int y) {
		g2d.setColor(color);
		g2d.fillRect(x + 4, y + 1, 10, 1);
		g2d.fillRect(x + 2, y + 2, 14, 1);
		g2d.fillRect(x + 1, y + 3, 16, 5);
		g2d.fillRect(x + 2, y + 8, 14, 3);
		g2d.fillRect(x + 3, y + 11, 12, 1);
		g2d.fillRect(x + 4, y + 12, 10, 1);
		g2d.fillRect(x + 5, y + 13, 8, 1);
		g2d.fillRect(x + 6, y + 14, 6, 1);
		g2d.fillRect(x + 7, y + 15, 4, 1);
		g2d.fillRect(x + 8, y + 16, 2, 1);
	}

	private static void fillInnerShield(final Graphics2D g2d,
			final int x, final int y) {
		g2d.setColor(INNER);
		g2d.fillRect(x + 3, y + 3, 12, 5);
		g2d.fillRect(x + 3, y + 8, 12, 2);
		g2d.fillRect(x + 4, y + 10, 10, 2);
		g2d.fillRect(x + 5, y + 12, 8, 1);
		g2d.fillRect(x + 6, y + 13, 6, 1);
		g2d.fillRect(x + 7, y + 14, 4, 1);
	}

	private static Color getBorderColor(final int reborns) {
		switch (Math.min(reborns, 5)) {
		case 1:
			return BRONZE;
		case 2:
			return SILVER;
		case 3:
			return GOLD;
		case 4:
			return AMETHYST;
		default:
			return LEGENDARY;
		}
	}

	private static void drawCount(final Graphics2D g2d, final int reborns,
			final int badgeX, final int badgeY) {
		final String text = Integer.toString(reborns);
		final int fontSize;
		if (text.length() == 1) {
			fontSize = 13;
		} else if (text.length() == 2) {
			fontSize = 11;
		} else if (text.length() == 3) {
			fontSize = 9;
		} else {
			fontSize = 7;
		}

		g2d.setFont(new Font(Font.DIALOG, Font.BOLD, fontSize));
		final FontMetrics metrics = g2d.getFontMetrics();
		final int textX = badgeX + (BADGE_WIDTH - metrics.stringWidth(text)) / 2;
		final int textY = badgeY + (BADGE_HEIGHT - metrics.getHeight()) / 2
				+ metrics.getAscent() - 1;

		g2d.setColor(TEXT_SHADOW);
		g2d.drawString(text, textX - 1, textY - 1);
		g2d.drawString(text, textX, textY - 1);
		g2d.drawString(text, textX + 1, textY - 1);
		g2d.drawString(text, textX - 1, textY);
		g2d.drawString(text, textX + 1, textY);
		g2d.drawString(text, textX - 1, textY + 1);
		g2d.drawString(text, textX, textY + 1);
		g2d.drawString(text, textX + 1, textY + 1);

		g2d.setColor(TEXT);
		g2d.drawString(text, textX, textY);
	}

	private static final class CachedTitle {
		private final String title;
		private final int color;
		private final Sprite sprite;

		private CachedTitle(final String title, final int color,
				final Sprite sprite) {
			this.title = title;
			this.color = color;
			this.sprite = sprite;
		}

		private boolean matches(final String newTitle, final Color newColor) {
			return title.equals(newTitle) && color == newColor.getRGB();
		}
	}
}
