/***************************************************************************
 *                      (C) Copyright 2010 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.styled.cursor;

import java.awt.Point;

/**
 * An enumeration of cursors.
 *
 * @author hendrik
 */
public enum StendhalCursor {
	ACTIVITY("activity", new Point(1, 1)),
// Gate2DView, PlantGrower
	ATTACK("attack", new Point(1, 1)),
	BAG("bag", new Point(1, 1)),
	EMPTY_BAG("emptybag", new Point(1, 1)),
	LOCKED_BAG("lockedbag", new Point(1, 1)),
	HARVEST("harvest", new Point(1, 1)),
	LOOK("look", new Point(1, 1)),
	NORMAL("normal", new Point(1, 1)),
	ITEM_EQUIP("itemequip", new Point(1, 1)),
	ITEM_PICK_UP_FROM_SLOT("itempickupfromslot", new Point(1, 1)),
	ITEM_USE("itemuse", new Point(1, 1)),
	PORTAL("portal", new Point(1, 1)),
	SPELLCASTING("usemagic", new Point(1,1)),
	STOP("stop", new Point(1, 1)),
	UNKNOWN("unknown", new Point(1, 1)),
	WALK("walk", new Point(1, 1)),
	WALK_BORDER("walkborder", new Point(1, 1));

	private String imageName;
	private Point hotSpot;

	/**
	 * creates a StendhalCursor enum entry
	 *
	 * @param imageName name of the image file
	 * @param hotSpot point that marks the click target of the cursor image
	 */
	private StendhalCursor(String imageName, Point hotSpot) {
		this.imageName = imageName;
		this.hotSpot = hotSpot;
	}

	/**
	 * gets the name of the image
	 *
	 * @return name of image
	 */
	String getImageName() {
		return imageName;
	}

	/**
	 * gets the hot spot
	 *
	 * @return point that marks the click target of the cursor image
	 */
	Point getHotSpot() {
		return hotSpot;
	}

	/**
	 * gets a cursor based on its name
	 *
	 * @param cursorName    name of cursor, may be <code>null</code>
	 * @param defaultCursor default cursor to use in case the cursorName does not exist or is <code>null</code>
	 * @return StendhalCursor
	 */
	public static StendhalCursor valueOf(String cursorName, StendhalCursor defaultCursor) {
		try {
			return valueOf(cursorName);
		} catch (RuntimeException e) {
			return defaultCursor;
		}
	}

}
