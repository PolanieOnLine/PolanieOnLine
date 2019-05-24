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
package games.stendhal.server.maps.zakopane.bank;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

import java.util.Map;

/**
 * Creates the items on the table zakopane bank -1.
 *
 * @author KarajuSs // based on kalavan.castle ItemsOnTable
 */
public class ItemsOnTable implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBasementArea(zone);
	}

	private void buildBasementArea(final StendhalRPZone zone) {
		final PassiveEntityRespawnPoint plantGrower = new PassiveEntityRespawnPoint("szczerbiec", 36000);
		plantGrower.setPosition(26, 33);
		plantGrower.setDescription("Tutaj leżą skarby.");
		zone.add(plantGrower);
		plantGrower.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower2 = new PassiveEntityRespawnPoint("złoty hełm", 36000);
		plantGrower2.setPosition(26, 34);
		plantGrower2.setDescription("Tutaj leżą skarby");
		zone.add(plantGrower2);
		plantGrower2.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower3 = new PassiveEntityRespawnPoint("złota zbroja", 36000);
		plantGrower3.setPosition(23, 34);
		plantGrower3.setDescription("Tutaj leżą skarby.");
		zone.add(plantGrower3);
		plantGrower3.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower4 = new PassiveEntityRespawnPoint("złota tarcza", 36000);
		plantGrower4.setPosition(23, 33);
		plantGrower4.setDescription("Tutaj leżą skarby.");
		zone.add(plantGrower4);
		plantGrower4.setToFullGrowth();

		final PassiveEntityRespawnPoint plantGrower5 = new PassiveEntityRespawnPoint("złote buty", 36000);
		plantGrower5.setPosition(37, 34);
		plantGrower5.setDescription("Tutaj leżą skarby.");
		zone.add(plantGrower5);
		plantGrower5.setToFullGrowth();
	}
}
