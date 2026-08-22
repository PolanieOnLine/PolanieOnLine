/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

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
		setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setRolloverEnabled(true);
		setForeground(LauncherTheme.TEXT);
		setFont(style == Style.PRIMARY
				? LauncherTheme.displayFont(27)
				: LauncherTheme.bodyFont(java.awt.Font.BOLD, 14));
		final Dimension size = style == Style.PRIMARY
				? new Dimension(280, 64)
				: new Dimension(170, 42);
		setPreferredSize(size);
		if (style == Style.PRIMARY) {
			setMinimumSize(size);
			setMaximumSize(size);
		}
	}

	@Override
	public void updateUI() {
		setUI(new BasicButtonUI());
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		final Graphics2D g2 = (Graphics2D) graphics.create();
		LauncherTheme.configureGraphics(g2);

		final boolean pressed = getModel().isPressed();
		final boolean rollover = getModel().isRollover();
		final boolean enabled = isEnabled();
		final int width = getWidth();
		final int height = getHeight();
		if (width <= 3 || height <= 3) {
			g2.dispose();
			return;
		}

		Color top;
		Color bottom;
		Color border;

		if (!enabled) {
			top = new Color(37, 38, 38, 205);
			bottom = new Color(20, 21, 22, 215);
			border = new Color(91, 77, 57, 125);
		} else if (style == Style.PRIMARY) {
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

		if (isFocusOwner()) {
			g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 1.0f, new float[] {3.0f, 3.0f}, 0.0f));
			g2.setColor(new Color(245, 211, 151, 155));
			g2.drawRoundRect(5, 5, width - 11, height - 11, 5, 5);
		}

		paintText(g2, enabled);
		g2.dispose();
	}

	private void paintText(final Graphics2D graphics, final boolean enabled) {
		final String text = getText();
		if (text == null || text.length() == 0) {
			return;
		}

		graphics.setFont(getFont());
		final FontMetrics metrics = graphics.getFontMetrics();
		final Insets insets = getInsets();
		final int textWidth = metrics.stringWidth(text);
		final int x;
		if (getHorizontalAlignment() == SwingConstants.LEFT) {
			x = insets.left;
		} else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
			x = getWidth() - insets.right - textWidth;
		} else {
			x = (getWidth() - textWidth) / 2;
		}
		final int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

		if (style == Style.PRIMARY && enabled) {
			graphics.setColor(LauncherTheme.SHADOW);
			BasicGraphicsUtils.drawStringUnderlineCharAt(graphics, text,
					getDisplayedMnemonicIndex(), x + 1, y + 2);
		}
		graphics.setColor(enabled ? LauncherTheme.TEXT : LauncherTheme.TEXT_MUTED);
		BasicGraphicsUtils.drawStringUnderlineCharAt(graphics, text,
				getDisplayedMnemonicIndex(), x, y);
	}
}
