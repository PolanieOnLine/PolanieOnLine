/* $Id: CommonChest.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.chest.Chest;

import java.util.Map;

public class CommonChest implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneCityAreaChest(zone);
	}

	private void buildZakopaneCityAreaChest(final StendhalRPZone zone) {

		final Chest chest = new Chest();
		chest.setPosition(110, 45);
		chest.add(SingletonRepository.getEntityManager().getItem("mieczyk"));
		chest.add(SingletonRepository.getEntityManager().getItem("drewniana tarcza"));
		chest.add(SingletonRepository.getEntityManager().getItem("skórzana zbroja"));
		chest.add(SingletonRepository.getEntityManager().getItem("money"));
		zone.add(chest);
	}
}
