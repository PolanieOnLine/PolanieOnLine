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
// Based on ../games/stendhal/server/maps/nalwor/tunnel/ThingCreature.java
package games.stendhal.server.maps.dragon.cave;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.ItemGuardCreature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

import java.util.Map;

/**
 * Configure Cave to include a Dragon Creature who carries a golden blade. 
 * Then it should give a golden blade that is bound to the player.
 */
public class Dragon implements ZoneConfigurator {


	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildDragonArea(zone);
	}

	private void buildDragonArea(final StendhalRPZone zone) {
		final EntityManager manager = SingletonRepository.getEntityManager();
		final Creature creature = new ItemGuardCreature(manager.getCreature("dwugłowy czarny smok"), "złota klinga", "kill_dragon", "start");
		final CreatureRespawnPoint point = new CreatureRespawnPoint(zone, 135, 118, creature, 1);
		zone.add(point);
	}
}
