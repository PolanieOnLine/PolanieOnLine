/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

import javax.swing.ImageIcon;

import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.update.ClientGameConfiguration;

/** Center artwork based on the existing Polanie Online splash image. */
@SuppressWarnings("serial")
public class LauncherArtworkPanel extends LauncherTransparentPanel {
	private final Image image;
	private final int imageWidth;
	private final int imageHeight;

	public LauncherArtworkPanel() {
		final URL url = DataLoader.getResource(
				ClientGameConfiguration.get("GAME_SPLASH_BACKGROUND"));
		final ImageIcon icon = url == null ? null : new ImageIcon(url);
		image = icon == null ? null : icon.getImage();
		imageWidth = icon == null ? 0 : icon.getIconWidth();
		imageHeight = icon == null ? 0 : icon.getIconHeight();
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		final Graphics2D g2 = (Graphics2D) graphics.create();
		LauncherTheme.configureGraphics(g2);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		final Shape oldClip = g2.getClip();
		final RoundRectangle2D clip = new RoundRectangle2D.Float(
				0, 0, getWidth(), getHeight(), 9, 9);
		g2.clip(clip);
		g2.setColor(LauncherTheme.PANEL_INNER);
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (image != null && imageWidth > 0 && imageHeight > 0) {
			final double scale = Math.max((double) getWidth() / imageWidth,
					(double) getHeight() / imageHeight);
			final int width = (int) Math.ceil(imageWidth * scale);
			final int height = (int) Math.ceil(imageHeight * scale);
			final int x = (getWidth() - width) / 2;
			final int y = (getHeight() - height) / 2;
			g2.drawImage(image, x, y, width, height, this);
		}

		g2.setPaint(new GradientPaint(0, 0, new Color(5, 8, 12, 70),
				getWidth(), 0, new Color(4, 7, 10, 28)));
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setPaint(new GradientPaint(0, getHeight() / 2,
				new Color(4, 6, 8, 0), 0, getHeight(), new Color(3, 5, 7, 238)));
		g2.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2 + 1);

		g2.setClip(oldClip);
		LauncherTheme.paintBorder(g2, 0, 0, getWidth(), getHeight(), 9);
		g2.dispose();
	}
}
