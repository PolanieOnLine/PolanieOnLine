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
package games.stendhal.server.maps.gdansk.goldsmith;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

import java.util.Map;

/**
 * Creates the items on the table in the museum.
 *
 * @author kymara
 */
public class ItemsOnTable implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBasementArea(zone);
	}

	private void buildBasementArea(final StendhalRPZone zone) {

		final PassiveEntityRespawnPoint plantGrower = new PassiveEntityRespawnPoint("bryłka złota", 40);
		plantGrower.setPosition(18, 12);
		plantGrower.setDescription("Miejsce na kamień.");
		zone.add(plantGrower);

		plantGrower.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower2 = new PassiveEntityRespawnPoint("sztabka złota", 40);
		plantGrower2.setPosition(18, 14);
		plantGrower2.setDescription("Miejsce na kamień.");
		zone.add(plantGrower2);

		plantGrower2.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower3 = new PassiveEntityRespawnPoint("sztabka mithrilu", 40);
		plantGrower3.setPosition(18, 16);
		plantGrower3.setDescription("Miejsce na kamień.");
		zone.add(plantGrower3);

		plantGrower3.setToFullGrowth();
	}

}
