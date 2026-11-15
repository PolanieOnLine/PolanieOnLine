/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.common.NavigationTarget;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Arrays;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import org.junit.Test;

public class CompassOverlayTest {
	@Test
	public void usingTheCompassCanHideAndRestoreAnAlreadyUnlockedBar() {
		PLAYER_CLASS.addAttribute("compass_hidden", Type.INT);
		RPObject user = player(true);
		BufferedImage image = new BufferedImage(640, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		CompassOverlay overlay = new CompassOverlay(new ImageSprite(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB)));
		user.put("compass_hidden", 1);
		overlay.drawSnapshot(g, 640, 50, 50, user, "zone");
		assertEquals("A hidden compass must not cover the game", 0, image.getRGB(320, 27));
		user.put("compass_hidden", 0);
		overlay.drawSnapshot(g, 640, 50, 50, user, "zone");
		assertNotEquals(0, image.getRGB(320, 27));
		g.dispose();
	}

	@Test
	public void keepsThePrimaryGoalWhenAnotherObjectiveBecomesCloser() {
		CompassOverlay overlay = new CompassOverlay();
		RPObject owner = playerFixture();
		NavigationTarget first = NavigationTarget.parse("main:a", "10\t0\tmain\tPierwszy");
		NavigationTarget second = NavigationTarget.parse("main:b", "1\t0\tmain\tDrugi");
		NavigationTarget side = NavigationTarget.parse("side", "0\t0\tside\tPoboczny");
		assertEquals("main:a", overlay.selectPrimary(Arrays.asList(side, second, first), owner).id);
		first = NavigationTarget.parse("main:a", "100\t0\tmain\tPierwszy");
		assertEquals("main:a", overlay.selectPrimary(Arrays.asList(side, second, first), owner).id);
		assertEquals("main:b", overlay.selectPrimary(Arrays.asList(side, second), owner).id);
		assertEquals("main:a", overlay.selectPrimary(Arrays.asList(side, second, first), playerFixture()).id);
	}

	private static RPObject playerFixture() {
		RPObject player = new RPObject();
		player.setRPClass(PLAYER_CLASS);
		return player;
	}
	private static final RPClass PLAYER_CLASS = new RPClass("compass_overlay_fixture");

	static {
		PLAYER_CLASS.addAttribute(NavigationTarget.UNLOCKED_ATTRIBUTE, Type.INT);
		PLAYER_CLASS.addAttribute(NavigationTarget.TARGETS_ATTRIBUTE, Type.MAP);
	}

	@Test
	public void narrowAndWideLayoutKeepMargins() {
		assertEquals(296, CompassOverlay.widthForViewport(320));
		assertEquals(560, CompassOverlay.widthForViewport(1920));
		assertEquals(0, CompassOverlay.widthForViewport(12));
	}

	@Test
	public void actualRendererPreservesGraphicsAndBackgroundOpacity() throws Exception {
		BufferedImage wood = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		Graphics2D texture = wood.createGraphics();
		texture.setColor(new Color(0x745033));
		texture.fillRect(0, 0, 8, 8);
		texture.dispose();
		BufferedImage output = new BufferedImage(640, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		g.setColor(Color.MAGENTA);
		new CompassOverlay(new ImageSprite(wood)).drawSnapshot(g, 640, 50, 50, player(false), "zone");
		assertEquals(Color.MAGENTA, g.getColor());
		assertEquals(166, output.getRGB(60, 45) >>> 24);
		assertEquals(255, output.getRGB(320, 27) >>> 24);
		assertEquals(0, output.getRGB(20, 45));
		g.dispose();
	}

	@Test
	public void renderGameTexturePreviewsWhenRequested() throws Exception {
		String assets = System.getProperty("compass.assets");
		String destination = System.getProperty("compass.preview");
		if (assets == null || destination == null) {
			return;
		}
		BufferedImage wood = ImageIO.read(new File(assets, "data/gui/panel_wood.jpg"));
		BufferedImage grass = ImageIO.read(new File(assets, "data/maps/tileset/ground/grass_2.png"));
		for (int width : new int[] {640, 320}) {
			BufferedImage image = new BufferedImage(width, 180, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			for (int y = 0; y < image.getHeight(); y += 32) {
				for (int x = 0; x < width; x += 32) {
					g.drawImage(grass, x, y, x + 32, y + 32, 32, 64, 64, 96, null);
				}
			}
			new CompassOverlay(new ImageSprite(wood))
					.drawSnapshot(g, width, 50.25, 50.5, player(true), "zone");
			g.dispose();
			String suffix = width == 640 ? "" : "-320";
			ImageIO.write(image, "png", new File(destination, "java-compass-preview" + suffix + ".png"));
		}
	}

	private RPObject player(boolean targets) {
		RPObject player = new RPObject();
		player.setRPClass(PLAYER_CLASS);
		player.put(NavigationTarget.UNLOCKED_ATTRIBUTE, 1);
		player.put(NavigationTarget.TARGETS_ATTRIBUTE, "_zone", "zone");
		if (targets) {
			player.put(NavigationTarget.TARGETS_ATTRIBUTE, "main", "65\t50\tmain\tCiężkie ślady\t145");
			player.put(
					NavigationTarget.TARGETS_ATTRIBUTE, "side", "21\t48\tside\tOdłamek kamiennej płyty");
		}
		return player;
	}
}
