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
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class StyledToolTipUI extends BasicToolTipUI {
	private static final String RARITY_GLOW_MARKER =
			"<!--item-rarity-glow:";
	private static final float GLOW_STRENGTH_SCALE = 2.35f;
	private static final float MAX_GLOW_STRENGTH = 0.42f;
	private static final int BASE_SHADE_ALPHA = 72;
	private static final int BOTTOM_SHADE_ALPHA = 55;
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
			paintStyledBackground(g, tooltip);
			paint(g, tooltip);
		} else {
			super.update(g, tooltip);
		}
	}

	/**
	 * Render the skin, a readability shade and rarity glow into an off-screen
	 * image before drawing it on the Swing component. The client has rendering
	 * paths where alpha compositing directly on the component graphics is
	 * unreliable, while a BufferedImage consistently supports translucent
	 * overlays.
	 */
	private void paintStyledBackground(final Graphics graphics,
			final JComponent tooltip) {
		final int width = tooltip.getWidth();
		final int height = tooltip.getHeight();
		if (width <= 0 || height <= 0) {
			return;
		}

		final BufferedImage background = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D bufferGraphics = background.createGraphics();
		StyleUtil.fillBackground(style, bufferGraphics, 0, 0, width, height);
		paintSkinShade(bufferGraphics, width, height);
		paintRarityGlow(bufferGraphics, tooltip);
		bufferGraphics.dispose();
		graphics.drawImage(background, 0, 0, null);
	}

	/**
	 * Reduce the visual noise of bright or highly textured skins without
	 * replacing them. The lower part is slightly darker so footer and bonus
	 * text keep enough contrast while the header remains open for rarity glow.
	 */
	private void paintSkinShade(final Graphics2D graphics, final int width,
			final int height) {
		graphics.setColor(new Color(0, 0, 0, BASE_SHADE_ALPHA));
		graphics.fillRect(0, 0, width, height);
		graphics.setPaint(new GradientPaint(0.0f, 0.0f,
				new Color(0, 0, 0, 0), 0.0f, Math.max(1.0f, height),
				new Color(0, 0, 0, BOTTOM_SHADE_ALPHA)));
		graphics.fillRect(0, 0, width, height);
	}

	/**
	 * Paint a rarity tint over the selected skin. Structured item tooltips
	 * embed an invisible marker containing their rarity color and desired glow
	 * strength. Other client tooltips are unaffected.
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
			final float configuredStrength = Float.parseFloat(
					payload.substring(separator + 1));
			final float strength = Math.max(0.0f,
					Math.min(MAX_GLOW_STRENGTH,
							configuredStrength * GLOW_STRENGTH_SCALE));
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
		final int baseAlpha = Math.max(6,
				Math.round(255.0f * strength * 0.34f));
		final int topAlpha = Math.max(baseAlpha,
				Math.round(255.0f * strength));
		final int borderAlpha = Math.min(205,
				Math.round(255.0f * strength * 1.9f));

		/* Keep the chosen skin readable as a texture, while the persistent tint
		 * makes rarity visible even away from the header. */
		g.setColor(withAlpha(rarityColor, baseAlpha));
		g.fillRect(0, 0, width, height);

		/* The strongest glow sits behind the name and rarity lines, then fades
		 * through the main stat section instead of washing the whole tooltip. */
		g.setPaint(new GradientPaint(0.0f, 0.0f,
				withAlpha(rarityColor, topAlpha), 0.0f,
				Math.max(65.0f, height * 0.62f),
				withAlpha(rarityColor, Math.max(0, baseAlpha / 3))));
		g.fillRect(0, 0, width, height);

		/* A controlled inner edge makes the rarity legible on both bright and
		 * dark skins without replacing the border supplied by the selected skin. */
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
