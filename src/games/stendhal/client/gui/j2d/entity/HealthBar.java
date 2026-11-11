/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import games.stendhal.client.gui.LinearScalingModel;
import games.stendhal.client.gui.LifeTideOverlay;

/**
 * Health bar for RPEntities.
 */
class HealthBar implements ChangeListener {
	private final int width, height;
	private BufferedImage image;
	private final LinearScalingModel model;
	private double armorRatio;
	private boolean hasArmor;
	private volatile double surplusRatio;
	/** A flag for signaling that the image needs updating. */
	private volatile boolean needsRedraw = true;

	/**
	 * Create a health bar.
	 *
	 * @param width width of the bar
	 * @param height height of the bar
	 */
	HealthBar(int width, int height) {
		this.width = width;
		this.height = height;
		model = new LinearScalingModel(1.0, width - 2);
		model.addChangeListener(this);
	}

	/**
	 * Get the bar height.
	 *
	 * @return height
	 */
	int getHeight() {
		return hasArmor ? Math.max(9, height) : height;
	}

	void setHasArmor(final boolean value) {
		if (hasArmor != value) {
			hasArmor = value;
			needsRedraw = true;
		}
	}

	/**
	 * Get the bar width.
	 *
	 * @return width
	 */
	int getWidth() {
		return width;
	}

	/**
	 * Set the HP ratio.
	 *
	 * @param hpRatio new HP ratio
	 */
	void setHPRatio(double hpRatio) {
		model.setValue(Double.isFinite(hpRatio) ? Math.max(0.0, Math.min(1.0, hpRatio)) : 0.0);
	}

	void setArmorRatio(final double ratio) {
		final double clamped = Double.isFinite(ratio) ? Math.max(0.0, Math.min(1.0, ratio)) : 0.0;
		if (armorRatio != clamped) {
			armorRatio = clamped;
			needsRedraw = true;
		}
	}

	void setSurplusRatio(final double ratio) {
		surplusRatio = Double.isFinite(ratio) ? Math.max(0.0, Math.min(1.0, ratio)) : 0.0;
	}

	/**
	 * Draw the health bar.
	 *
	 * @param g graphics
	 * @param x x coordinate of the top left corner
	 * @param y y coordinate of the top left corner
	 */
	void draw(Graphics g, int x, int y) {
		if (image == null || image.getHeight() != getHeight()) {
			image = new BufferedImage(width, getHeight(), BufferedImage.TYPE_INT_RGB);
			needsRedraw = true;
		}

		if (needsRedraw) {
			/*
			 * Toggle the flag first, so that if the game loop changes it to
			 * true while we are updating the image, it will result in a new
			 * update at the next screen redraw. The unlikely case that
			 * game loop sets it to true *again* between the check above and our
			 * reset of the flag is not a problem - we already have the correct
			 * data for the second draw request.
			 */
			needsRedraw = false;
			Graphics gr = image.createGraphics();
			drawImage(gr);
			gr.dispose();
		}
		g.drawImage(image, x, y, null);
		if (surplusRatio > 0) {
			LifeTideOverlay.draw(g, x + 1, y + healthTop(), width - 2, healthHeight(),
					surplusRatio, System.currentTimeMillis());
		}
	}

	private int healthHeight() {
		return hasArmor ? (getHeight() - 3) / 2 : height - 2;
	}

	private int healthTop() {
		return hasArmor ? getHeight() - 1 - healthHeight() : 1;
	}

	/**
	 * Draw the health bar image.
	 *
	 * @param g graphics
	 */
	private void drawImage(Graphics g) {
		// A dark divider keeps armor and health readable at native game scale.
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, getHeight());
		g.setColor(Color.GRAY);
		g.fillRect(1, healthTop(), width - 2, healthHeight());
		g.setColor(determineColor());
		g.fillRect(1, healthTop(), model.getRepresentation(), healthHeight());
		if (hasArmor) {
			int armorHeight = healthTop() - 2;
			g.setColor(new Color(55, 63, 72));
			g.fillRect(1, 1, width - 2, armorHeight);
			g.setColor(new Color(185, 196, 207));
			g.fillRect(1, 1, (int) Math.round((width - 2) * armorRatio), armorHeight);
		}
	}

	/**
	 * Determine the health bar color.
	 *
	 * @return bar color
	 */
	private Color determineColor() {
		float hpRatio = (float) model.getValue();

		float r = Math.min((1.0f - hpRatio) * 2.0f, 1.0f);
		float g = Math.min(hpRatio * 2.0f, 1.0f);
		return new Color(r, g, 0.0f);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		/*
		 * State changes are originated by the game loop thread. needsRedraw is
		 * volatile so the model will have correct data for the EDT.
		 */
		needsRedraw = true;
	}
}
