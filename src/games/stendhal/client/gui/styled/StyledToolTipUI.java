/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.styled;

import java.awt.Container;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class StyledToolTipUI extends BasicToolTipUI {
	private static StyledToolTipUI instance;

	private final Style style;
	private final Border border;

	/**
	 * Create a StyledToolTipUI. This method is used by the UIManager.
	 *
	 * @param tooltip
	 * @return A shared StyledToolTipUI instance
	 */
	public static synchronized ComponentUI createUI(JComponent tooltip) {
		if (instance == null) {
			instance = new StyledToolTipUI(StyleUtil.getStyle());
		}

		return instance;
	}

	/**
	 * Create a new StyledToolTipUI.
	 *
	 * @param style pixmap style
	 */
	public StyledToolTipUI(Style style) {
		this.style = style;
		if (style != null && style.getBorder() != null) {
			border = style.getBorder();
		} else if (style != null) {
			border = BorderFactory.createLineBorder(style.getShadowColor());
		} else {
			border = BorderFactory.createEtchedBorder();
		}
	}

	@Override
	public void update(Graphics g, JComponent tooltip) {
		if (style != null && style.getBackground() != null) {
			StyleUtil.fillBackground(style, g, 0, 0,
					tooltip.getWidth(), tooltip.getHeight());
			paint(g, tooltip);
		} else {
			super.update(g, tooltip);
		}
	}

	@Override
	public void paint(Graphics g, JComponent tooltip) {
		// Get rid of popup borders, if it has any (Heavy weight popups tend to
		// pack the tooltip in a JPanel
		Container parent = tooltip.getParent();
		if (parent instanceof JComponent) {
			JComponent popup = (JComponent) parent;
			if (popup.getBorder() != null) {
				popup.setBorder(null);
			}
		}
		super.paint(g, tooltip);
	}

	@Override
	public void installUI(JComponent tooltip) {
		super.installUI(tooltip);
		if (style != null) {
			tooltip.setBackground(style.getPlainColor());
			tooltip.setForeground(style.getForeground());
			tooltip.setFont(style.getFont());
		}
		tooltip.setBorder(border);
	}
}
