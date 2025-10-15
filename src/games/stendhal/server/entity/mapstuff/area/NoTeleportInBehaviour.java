/***************************************************************************
 *                  (C) Copyright 2013 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.area;


import games.stendhal.server.core.engine.StendhalRPZone;

/**
 * prevents teleporting into the specified area
 *
 * @author hendrik
 */
public class NoTeleportInBehaviour implements AreaBehaviour {

	private AreaEntity area;

	@Override
	public void addToWorld(AreaEntity area) {
		this.area = area;
		area.getZone().disallowIn(area.getX(), area.getY(), (int) area.getWidth(), (int) area.getHeight());
	}

	@Override
	public void removeFromWorld() {
		if (area == null) {
			return;
		}
		StendhalRPZone zone = area.getZone();
		if (zone != null) {
			zone.allowIn(area.getX(), area.getY(), (int) area.getWidth(), (int) area.getHeight());
		}
		area = null;
	}
}
