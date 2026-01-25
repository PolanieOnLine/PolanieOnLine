/***************************************************************************
 *                 (C) Copyright 2003-2015 - Faiumoni e.V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Graphics;

/**
 * An effect that draws a semi-transparent fog overlay.
 */
public class FogScreenEffect extends EffectLayer {
	private final int maxAlpha;

	public FogScreenEffect(int duration, int strength) {
		super(duration);
		int resolvedStrength = strength > 0 ? strength : 40;
		maxAlpha = alpha((int) (255 * (resolvedStrength / 100.0)));
	}

	@Override
	public void drawScreen(Graphics g, int x, int y, int w, int h) {
		long time = System.currentTimeMillis();
		double intensity = 1.0;
		if (duration > 0) {
			double progress = Math.min(1.0, Math.max(0.0, (time - timestamp) / (double) duration));
			double fadePortion = 0.2;
			double fadeIn = Math.min(1.0, progress / fadePortion);
			double fadeOut = Math.min(1.0, (1.0 - progress) / fadePortion);
			intensity = Math.min(1.0, Math.min(fadeIn, fadeOut));
		}
		int alphaValue = (int) Math.round(maxAlpha * intensity);
		Color c = new Color(90, 90, 90, alphaValue);
		g.setColor(c);
		g.fillRect(x, y, w, h);
	}
}
