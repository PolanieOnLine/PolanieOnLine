/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import games.stendhal.client.entity.User;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.NavigationTarget;
import games.stendhal.client.ClientLanguage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import marauroa.common.game.RPObject;

/** Screen-space compass. Reads only the local player's private snapshot. */
public final class CompassOverlay {
	private static final Color INK = new Color(0xfff3d2);
	private static final Color MAIN = new Color(0xf27766);
	private static final Color SIDE = new Color(0xd9c697);
	private Sprite wood;
	private RPObject owner;
	private String selectedTarget;

	public CompassOverlay() {}

	CompassOverlay(Sprite wood) {
		this.wood = wood;
	}

	public static int widthForViewport(int viewportWidth) {
		return Math.max(0, Math.min(560, viewportWidth - 24));
	}

	public void draw(Graphics2D source, int viewportWidth, double playerX, double playerY) {
		if (User.isNull() || !Double.isFinite(playerX) || !Double.isFinite(playerY)) {
			owner = null;
			selectedTarget = null;
			return;
		}
		RPObject user = User.get().getRPObject();
		drawSnapshot(source, viewportWidth, playerX, playerY, user, User.get().getZoneName());
	}

	void drawSnapshot(
			Graphics2D source,
			int viewportWidth,
			double playerX,
			double playerY,
			RPObject user,
			String zone) {
		if (!Double.isFinite(playerX) || !Double.isFinite(playerY)) {
			return;
		}
		if (user == null
				|| !user.has(NavigationTarget.UNLOCKED_ATTRIBUTE)
				|| !"1".equals(user.get(NavigationTarget.UNLOCKED_ATTRIBUTE))) {
			owner = null;
			selectedTarget = null;
			return;
		}
		if (user.has(NavigationTarget.HIDDEN_ATTRIBUTE) && "1".equals(user.get(NavigationTarget.HIDDEN_ATTRIBUTE))) {
			return;
		}
		int width = widthForViewport(viewportWidth);
		if (width < 80) {
			return;
		}
		int left = (viewportWidth - width) / 2;
		Graphics2D g = (Graphics2D) source.create();
		try {
			g.translate(left, 12);
			g.clipRect(0, 0, width, 38);
			g.setComposite(AlphaComposite.SrcOver.derive(0.65f));
			if (wood == null) {
				wood = SpriteStore.get().getSprite("data/gui/panel_wood.jpg");
			}
			if (wood != null && wood.getWidth() > 0 && wood.getHeight() > 0) {
				for (int x = 0; x < width; x += wood.getWidth()) {
					for (int y = 0; y < 38; y += wood.getHeight()) {
						wood.draw(g, x, y);
					}
				}
			} else {
				g.setColor(new Color(0x382818));
				g.fillRect(0, 0, width, 38);
			}
			g.setColor(new Color(0xae8a5b));
			g.drawRect(0, 0, width - 1, 37);
			g.setComposite(AlphaComposite.SrcOver);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
			String[] directions = {"S", "W", "N", "E", "S"};
			for (int tick = 0; tick <= 16; tick++) {
				int x = 8 + (int) Math.round((width - 16) * tick / 16.0);
				boolean cardinal = tick % 4 == 0;
				g.setColor(INK);
				g.fillRect(x - (cardinal ? 1 : 0), 14, cardinal ? 2 : 1, cardinal ? 7 : 4);
				if (cardinal) {
					text(g, directions[tick / 4], x, 12);
				}
			}
			List<NavigationTarget> targets = new ArrayList<>();
			if (user.has(NavigationTarget.TARGETS_ATTRIBUTE, "_zone")
					&& zone != null
					&& zone.equals(user.get(NavigationTarget.TARGETS_ATTRIBUTE, "_zone"))) {
				for (Map.Entry<String, String> entry :
						user.getMap(NavigationTarget.TARGETS_ATTRIBUTE).entrySet()) {
					NavigationTarget target = NavigationTarget.parse(entry.getKey(), entry.getValue());
					if (target != null && Double.isFinite(target.distance(playerX, playerY))) {
						targets.add(target);
					}
				}
			}
			NavigationTarget primary = selectPrimary(targets, user);
			targets.sort(Comparator.comparing((NavigationTarget t) -> t != primary).thenComparing(t -> t.id));
			List<Integer> occupied = new ArrayList<>();
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			for (NavigationTarget target : targets) {
				int x = 8 + (int) Math.round((width - 16) * target.position(playerX, playerY));
				if (occupied.stream().anyMatch(previous -> Math.abs(previous - x) < 45)) {
					continue;
				}
				occupied.add(x);
				if (target == primary) {
					int[] xs = {x, x + 5, x, x - 5};
					int[] ys = {17, 22, 27, 22};
					g.setColor(target.main ? MAIN : SIDE);
					g.fillPolygon(xs, ys, 4);
					g.setColor(Color.WHITE);
					g.drawPolygon(xs, ys, 4);
					g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
					String distance = Math.round(target.distance(playerX, playerY)) + " " + ClientLanguage.getText("kr.");
					int tx = Math.min(width - g.getFontMetrics().stringWidth(distance) - 3, x + 7);
					g.setColor(INK);
					g.drawString(distance, tx, 26);
				} else {
					g.setColor(new Color(0xaaa28e));
					g.fillOval(x - 2, 20, 4, 4);
				}
				if (occupied.size() == 4) {
					break;
				}
			}
			if (primary != null) {
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
				String caption = ClientLanguage.getText(primary.instruction(playerX, playerY)) + " · " + primary.label;
				boolean shortened = false;
				while (!caption.isEmpty() && g.getFontMetrics().stringWidth(caption + (shortened ? "..." : "")) > width - 20) {
					caption = caption.substring(0, caption.length() - 1);
					shortened = true;
				}
				g.setColor(INK);
				text(g, caption + (shortened ? "..." : ""), width / 2, 36);
			}
		} finally {
			g.dispose();
		}
	}

	NavigationTarget selectPrimary(List<NavigationTarget> targets, RPObject user) {
		if (owner != user) {
			owner = user;
			selectedTarget = null;
		}
		for (NavigationTarget target : targets) if (target.id.equals(selectedTarget)) return target;
		NavigationTarget primary = targets.stream()
				.min(Comparator.comparing((NavigationTarget target) -> !target.main).thenComparing(target -> target.id))
				.orElse(null);
		selectedTarget = primary == null ? null : primary.id;
		return primary;
	}

	private static void text(Graphics2D g, String value, int centerX, int baseline) {
		int x = centerX - g.getFontMetrics().stringWidth(value) / 2;
		Color color = g.getColor();
		g.setColor(Color.BLACK);
		g.drawString(value, x + 1, baseline + 1);
		g.setColor(color);
		g.drawString(value, x, baseline);
	}
}
