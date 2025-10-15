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
package games.stendhal.server.core.config.zone;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TeleportationRules {
	/** Areas where teleporting out is blocked */
	private final List<Shape> leavingBarriers = new LinkedList<Shape>();
	/** Areas where teleporting in is blocked */
	private final List<Shape> arrivingBarriers = new LinkedList<Shape>();

	/**
	 * Block teleporting to a rectangular area.
	 *
	 * @param x x coordinate of the blocked area
	 * @param y y coordinate of the blocked area
	 * @param width width of the blocked area
	 * @param height height of the blocked area
	 */
	public void disallowIn(int x, int y, int width, int height) {
		Rectangle r = new Rectangle(x, y, width, height);
		arrivingBarriers.add(r);
	}

	/**
	 * Block teleporting in.
	 */
	public void disallowIn() {
		// Make a rectangle large enough to cover the zone, even if we some
		// day start allowing changeable sizes
		Rectangle r = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		arrivingBarriers.add(r);
	}

	/**
	 * Allow teleporting to specified area.
	 *
	 * @param x x coordinate of the area
	 * @param y y coordinate of the area
	 * @param width width of the area
	 * @param height height of the area
	 */
	public void allowIn(int x, int y, int width, int height) {
		Rectangle target = new Rectangle(x, y, width, height);
		Iterator<Shape> iterator = arrivingBarriers.iterator();
		while (iterator.hasNext()) {
			Shape barrier = iterator.next();
			if (barrier.getBounds().equals(target)) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * Allow teleporting to the entire zone using a scroll.
	 */
	public void allowIn() {
		allowIn(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Check if teleporting to a location is allowed.
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return <code>true</code> if teleporting to the point is allowed, <code>false</code> otherwise
	 */
	public boolean isInAllowed(int x, int y) {
		for (Shape r : arrivingBarriers) {
			if (r.contains(x, y)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Block teleporting from a rectangular area.
	 *
	 * @param x x coordinate of the blocked area
	 * @param y y coordinate of the blocked area
	 * @param width width of the blocked area
	 * @param height height of the blocked area
	 */
	public void disallowOut(int x, int y, int width, int height) {
		Rectangle r = new Rectangle(x, y, width, height);
		leavingBarriers.add(r);
	}

	/**
	 * Allow teleporting from specified area.
	 *
	 * @param x x coordinate of the area
	 * @param y y coordinate of the area
	 * @param width width of the area
	 * @param height height of the area
	 */
	public void allowOut(int x, int y, int width, int height) {
		Rectangle target = new Rectangle(x, y, width, height);
		Iterator<Shape> iterator = leavingBarriers.iterator();
		while (iterator.hasNext()) {
			Shape barrier = iterator.next();
			if (barrier.getBounds().equals(target)) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * Allow teleporting from the entire zone using a scroll.
	 */
	public void allowOut() {
		allowOut(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Block teleporting out.
	 */
	public void disallowOut() {
		// Make a rectangle large enough to cover the zone, even if we some
		// day start allowing changeable sizes
		Rectangle r = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		leavingBarriers.add(r);
	}

	/**
	 * Check if teleporting from a location is allowed.
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return <code>true</code> if teleporting to the point is allowed, <code>false</code> otherwise
	 */
	public boolean isOutAllowed(int x, int y) {
		for (Shape r : leavingBarriers) {
			if (r.contains(x, y)) {
				return false;
			}
		}

		return true;
	}
}
