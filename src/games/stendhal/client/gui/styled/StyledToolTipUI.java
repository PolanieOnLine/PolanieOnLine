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

import java.awt.Color;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class StyledToolTipUI extends BasicToolTipUI {
	private static final String RARITY_GLOW_MARKER =
			"<!--item-rarity-glow:";
	private final Style style;
	private final Border border;

	/**
	 * Create a StyledToolTipUI. This method is used by the UIManager.
	 *
	 * @param tooltip
	 * @return tooltip UI using the currently selected Stendhal style
	 */
	public static ComponentUI createUI(JComponent tooltip) {
		/* Do not cache a single UI instance. The player can switch skins while
		 * the client is running, so each UI refresh must capture the current
		 * StendhalStyle instead of keeping the previous texture and font. */
		return new StyledToolTipUI(StyleUtil.getStyle());
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
			paintRarityGlow(g, tooltip);
			paint(g, tooltip);
		} else {
			super.update(g, tooltip);
		}
	}

	/**
	 * Paint a subtle rarity tint over the selected skin. Structured item
	 * tooltips embed an invisible marker containing their rarity color and
	 * desired glow strength. Other client tooltips are unaffected.
	 */
	private void paintRarityGlow(final Graphics graphics,
			final JComponent tooltip) {
		if (!(tooltip instanceof JToolTip)) {
			return;
		}
		final String text = ((JToolTip) tooltip).getTipText();
		if (text == null) {
			return;
		}

		final int markerStart = text.indexOf(RARITY_GLOW_MARKER);
		if (markerStart < 0) {
			return;
		}
		final int payloadStart = markerStart + RARITY_GLOW_MARKER.length();
		final int markerEnd = text.indexOf("-->", payloadStart);
		if (markerEnd < 0) {
			return;
		}

		final String payload = text.substring(payloadStart, markerEnd);
		final int separator = payload.lastIndexOf(':');
		if (separator <= 0 || separator >= payload.length() - 1) {
			return;
		}

		try {
			final Color rarityColor = Color.decode(payload.substring(0, separator));
			final float strength = Math.max(0.0f, Math.min(0.25f,
					Float.parseFloat(payload.substring(separator + 1))));
			paintRarityGlow(graphics, tooltip, rarityColor, strength);
		} catch (NumberFormatException e) {
			// Presentation metadata must never prevent a tooltip from rendering.
		}
	}

	private void paintRarityGlow(final Graphics graphics,
			final JComponent tooltip, final Color rarityColor,
			final float strength) {
		if (strength <= 0.0f) {
			return;
		}

		final Graphics2D g = (Graphics2D) graphics.create();
		final int width = tooltip.getWidth();
		final int height = tooltip.getHeight();
		final int baseAlpha = Math.max(2,
				Math.round(255.0f * strength * 0.22f));
		final int topAlpha = Math.max(baseAlpha,
				Math.round(255.0f * strength));
		final int borderAlpha = Math.min(150,
				Math.round(255.0f * strength * 1.65f));

		/* A faint tint keeps the chosen skin visible while the stronger top
		 * gradient makes rarity immediately readable around the item header. */
		g.setColor(withAlpha(rarityColor, baseAlpha));
		g.fillRect(0, 0, width, height);
		g.setPaint(new GradientPaint(0.0f, 0.0f,
				withAlpha(rarityColor, topAlpha), 0.0f,
				Math.max(40.0f, height * 0.58f),
				withAlpha(rarityColor, 0)));
		g.fillRect(0, 0, width, height);

		/* The inner edge gives the glow a controlled boundary without replacing
		 * the actual border supplied by the current skin. */
		if (width > 3 && height > 3) {
			g.setColor(withAlpha(rarityColor, borderAlpha));
			g.drawRect(1, 1, width - 3, height - 3);
		}
		g.dispose();
	}

	private Color withAlpha(final Color color, final int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				Math.max(0, Math.min(255, alpha)));
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
