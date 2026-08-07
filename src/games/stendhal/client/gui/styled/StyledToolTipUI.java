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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class StyledToolTipUI extends BasicToolTipUI {
	private static final String RARITY_GLOW_MARKER =
			"<!--item-rarity-glow:";
	private static final String STRUCTURED_LIST_MARKER =
			"<!--item-tooltip-list-v2-->";
	private static final float GLOW_STRENGTH_SCALE = 2.35f;
	private static final float MAX_GLOW_STRENGTH = 0.42f;
	private static final float EPIC_GLOW_FACTOR = 0.82f;
	private static final float LEGENDARY_GLOW_FACTOR = 0.78f;
	private static final int EPIC_GLOW_RGB = Color.decode("#9b59b6").getRGB();
	private static final int LEGENDARY_GLOW_RGB = Color.decode("#ff8c00").getRGB();
	private static final int BASE_SHADE_ALPHA = 72;
	private static final int BOTTOM_SHADE_ALPHA = 55;
	private static final Pattern ARMOUR_STAT_PATTERN =
			Pattern.compile("Pancerz: ([0-9]+)");
	private static final Pattern SKILL_ATTACK_STAT_PATTERN =
			Pattern.compile("Siła ataku: ([0-9]+)");
	private final Style style;
	private final Border border;
	private final PropertyChangeListener tooltipNormalizer =
			new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					if (event.getSource() instanceof JComponent) {
						normalizeStructuredItemMarkup(
								(JComponent) event.getSource());
					}
				}
			};

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
			final float tunedStrength = tuneRarityGlow(rarityColor,
					configuredStrength);
			final float strength = Math.max(0.0f,
					Math.min(MAX_GLOW_STRENGTH,
							tunedStrength * GLOW_STRENGTH_SCALE));
			paintRarityGlow(graphics, tooltip, rarityColor, strength);
		} catch (NumberFormatException e) {
			// Presentation metadata must never prevent a tooltip from rendering.
		}
	}

	/**
	 * Epic and legendary cards keep a clear rarity identity without washing
	 * out highly textured client skins. Rare and common retain their current
	 * strength because they already read well on the wooden theme.
	 */
	private float tuneRarityGlow(final Color rarityColor,
			final float configuredStrength) {
		if (rarityColor.getRGB() == EPIC_GLOW_RGB) {
			return configuredStrength * EPIC_GLOW_FACTOR;
		}
		if (rarityColor.getRGB() == LEGENDARY_GLOW_RGB) {
			return configuredStrength * LEGENDARY_GLOW_FACTOR;
		}
		return configuredStrength;
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

	/**
	 * Normalize secondary structured item properties into one visual list.
	 * The generator keeps raw semantic labels while this skin-aware layer adds
	 * the shared diamond treatment used by bonuses. Main armour cards are not
	 * affected because they use the separate "pkt. pancerza" headline.
	 */
	private void normalizeStructuredItemMarkup(final JComponent component) {
		if (!(component instanceof JToolTip)) {
			return;
		}
		final JToolTip tooltip = (JToolTip) component;
		final String text = tooltip.getTipText();
		if (text == null || text.indexOf(RARITY_GLOW_MARKER) < 0
				|| text.indexOf(STRUCTURED_LIST_MARKER) >= 0) {
			return;
		}

		String normalized = text.replace("<html>",
				"<html>" + STRUCTURED_LIST_MARKER);
		normalized = normalized.replace("Typ obrażeń: ",
				"&#9670;&nbsp; Typ obrażeń: ");
		normalized = normalized.replace("Zasięg: ",
				"&#9670;&nbsp; Zasięg: ");
		normalized = normalized.replace("Efekty trafienia: ",
				"&#9670;&nbsp; Efekty trafienia: ");
		normalized = ARMOUR_STAT_PATTERN.matcher(normalized).replaceAll(
				"&#9670;&nbsp; +$1 pancerza");
		normalized = SKILL_ATTACK_STAT_PATTERN.matcher(normalized).replaceAll(
				"&#9670;&nbsp; +$1 siły ataku");
		tooltip.setTipText(normalized);
	}

	private Color withAlpha(final Color color, final int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				Math.max(0, Math.min(255, alpha)));
	}

	@Override
	public void paint(Graphics g, JComponent tooltip) {
		normalizeStructuredItemMarkup(tooltip);
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
		tooltip.addPropertyChangeListener(tooltipNormalizer);
		normalizeStructuredItemMarkup(tooltip);
	}

	@Override
	public void uninstallUI(final JComponent tooltip) {
		tooltip.removePropertyChangeListener(tooltipNormalizer);
		super.uninstallUI(tooltip);
	}
}
