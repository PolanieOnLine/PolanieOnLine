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
package games.stendhal.server.maps.ados.bar;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

/**
 * Creates bottles in Ados bar 1st floor (Upstairs)
 */
public class ItemsOnTableUpstairs implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBarUpstairsArea(zone);
	}

	private void buildBarUpstairsArea(final StendhalRPZone zone) {

		// grower for an empty slim bottle (1h)
		final PassiveEntityRespawnPoint bottleGrower1 = new PassiveEntityRespawnPoint("wąska butelka", 12000);
		bottleGrower1.setPosition(7, 4);
		bottleGrower1.setDescription("Wygląda na miejsce, gdzie można znaleść butelkę.");
		zone.add(bottleGrower1);

		bottleGrower1.setToFullGrowth();

		// grower for an empty slim bottle (1h)
		final PassiveEntityRespawnPoint bottleGrower2 = new PassiveEntityRespawnPoint("wąska butelka", 12000);
		bottleGrower2.setPosition(8, 25);
		bottleGrower2.setDescription("Wygląda na miejsce, gdzie można znaleść butelkę.");
		zone.add(bottleGrower2);

		bottleGrower2.setToFullGrowth();

		// grower for an empty eared bottle (1h)
		final PassiveEntityRespawnPoint bottleGrower3 = new PassiveEntityRespawnPoint("butla czwórniaczka", 12000);
		bottleGrower3.setPosition(19, 9);
		bottleGrower3.setDescription("Wygląda na miejsce, gdzie można znaleść butelkę.");
		zone.add(bottleGrower3);

		bottleGrower3.setToFullGrowth();
	}
}
