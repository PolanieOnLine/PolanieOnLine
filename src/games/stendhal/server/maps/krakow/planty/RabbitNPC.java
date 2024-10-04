/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.planty;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SilentNPC;
import games.stendhal.server.entity.npc.behaviour.impl.idle.WanderIdleBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Rabbits
 *
 * @author AntumDeluge
 */
public class RabbitNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {

	    // All rabbits
	    List<SilentNPC> rabbits = new LinkedList<SilentNPC>();

		final SilentNPC r1 = new SilentNPC();
        r1.setPosition(44, 103);
        rabbits.add(r1);

		final SilentNPC r2 = new SilentNPC();
		r2.setPosition(18, 36);
		rabbits.add(r2);

		final SilentNPC r3 = new SilentNPC();
		r3.setPosition(15, 118);
		rabbits.add(r3);

		// Add rabbits to zone
		for (SilentNPC mammal : rabbits) {
	        mammal.setDescription("Oto królik.");
	        mammal.setEntityClass("animal/rabbit");
	        mammal.setBaseSpeed(0.2);
	        mammal.setIdleBehaviour(new WanderIdleBehaviour());
	        mammal.setTitle("królik");
	        mammal.setPathCompletedPause(20);
	        zone.add(mammal);
		}
	}
}
