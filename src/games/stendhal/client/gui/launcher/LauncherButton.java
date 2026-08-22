/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** Button with a lightweight dark-fantasy look painted entirely in Java2D. */
@SuppressWarnings("serial")
public class LauncherButton extends JButton {

	public enum Style {
		PRIMARY,
		NAVIGATION,
		SECONDARY
	}

	private final Style style;

	public LauncherButton(final Action action, final Style style) {
		super(action);
		this.style = style;
		setFocusPainted(false);
		setContentAreaFilled(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setForeground(LauncherTheme.TEXT);
		setFont(style == Style.PRIMARY
				? LauncherTheme.displayFont(26)
				: LauncherTheme.bodyFont(java.awt.Font.BOLD, 14));
		setPreferredSize(style == Style.PRIMARY ? new Dimension(250, 58) : new Dimension(180, 42));
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		final Graphics2D g2 = (Graphics2D) graphics.create();
		LauncherTheme.configureGraphics(g2);

		final boolean pressed = getModel().isPressed();
		final boolean rollover = getModel().isRollover();
		final int width = getWidth();
		final int height = getHeight();

		Color top;
		Color bottom;
		Color border;

		if (style == Style.PRIMARY) {
			top = pressed ? new Color(123, 80, 31) : rollover ? new Color(210, 158, 76) : new Color(174, 122, 52);
			bottom = pressed ? new Color(84, 53, 24) : rollover ? new Color(127, 81, 32) : new Color(105, 66, 29);
			border = rollover ? LauncherTheme.GOLD_BRIGHT : LauncherTheme.GOLD;
		} else if (style == Style.NAVIGATION) {
			top = rollover ? new Color(61, 49, 35, 235) : new Color(29, 31, 32, 220);
			bottom = rollover ? new Color(40, 31, 23, 240) : new Color(17, 19, 20, 225);
			border = rollover ? LauncherTheme.GOLD : new Color(93, 72, 45, 150);
		} else {
			top = rollover ? new Color(46, 46, 44, 235) : new Color(27, 29, 29, 225);
			bottom = rollover ? new Color(27, 27, 26, 240) : new Color(16, 18, 18, 230);
			border = rollover ? LauncherTheme.GOLD : new Color(88, 72, 49, 150);
		}

		g2.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
		g2.fillRoundRect(1, 1, width - 3, height - 3, 8, 8);
		g2.setColor(border);
		g2.drawRoundRect(1, 1, width - 3, height - 3, 8, 8);
		g2.setColor(new Color(255, 224, 166, rollover ? 80 : 35));
		g2.drawLine(8, 3, Math.max(8, width - 9), 3);
		g2.dispose();

		super.paintComponent(graphics);
	}
}
