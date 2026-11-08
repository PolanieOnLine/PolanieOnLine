/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.stats;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Timer;

import games.stendhal.client.gui.LinearScalingModel;
import games.stendhal.client.gui.LifeTideOverlay;
import games.stendhal.client.gui.StatusDisplayBar;


/**
 * A bar indicator component for HP.
 */
public class HPIndicator extends StatusDisplayBar {

	/** Default preferred height of the component. */
	private static final int DEFAULT_HEIGHT = 6;
	private double surplusRatio;
	private final Timer pulseTimer = new Timer(50, event -> repaint());


	public HPIndicator() {
		super(new LinearScalingModel());
		setBackground(Color.DARK_GRAY);
		setForeground(Color.WHITE);
		setPreferredSize(new Dimension(0, DEFAULT_HEIGHT));
		setMinimumSize(getPreferredSize());
		addHierarchyListener(event -> updatePulseTimer());
	}

	/**
	 * Set the ratio of HP/Maximum HP.
	 *
	 * @param ratio
	 * 		HP ratio.
	 */
	public void setRatio(float ratio) {
		surplusRatio = Float.isFinite(ratio) ? Math.max(0.0, ratio - 1.0) : 0.0;
		ratio = Float.isFinite(ratio) ? Math.max(0.0f, Math.min(1.0f, ratio)) : 0.0f;

		// Pick a color from red to green depending on the hp ratio.
		float r = Math.min((1.0f - ratio) * 2.0f, 1.0f);
		float g = Math.min(ratio * 2.0f, 1.0f);
		setBarColor(new Color(r, g, 0.0f));
		getModel().setValue(ratio);
		updatePulseTimer();
		repaint();
	}

	private void updatePulseTimer() {
		if (surplusRatio > 0 && isShowing()) {
			pulseTimer.start();
		} else {
			pulseTimer.stop();
		}
	}

	@Override
	public void removeNotify() {
		pulseTimer.stop();
		super.removeNotify();
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		if (surplusRatio <= 0) {
			return;
		}
		final Insets insets = getInsets();
		LifeTideOverlay.draw(graphics, insets.left + 1, insets.top + 1,
				getWidth() - insets.left - insets.right - 2,
				getHeight() - insets.top - insets.bottom - 2, surplusRatio, System.currentTimeMillis());
	}

	/**
	 *
	 * @param maxhp
	 * @param hp
	 */
	public void setHP(final int maxhp, final int hp) {
		final float ratio = maxhp > 0 ? (float) hp / maxhp : 0;
		setRatio(ratio);
	}
}
