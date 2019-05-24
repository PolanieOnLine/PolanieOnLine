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
package games.stendhal.server.maps.fado.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Builds the church Nun NPC.
 *
 * @author kymara
 */
public class NunNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNunNPC(zone);
	}

	//
	// A Nun NPC outside church
	//
	private void buildNunNPC(final StendhalRPZone zone) {
		final SpeakerNPC nunnpc = new SpeakerNPC("Sister Benedicta") {

			@Override
			protected void createPath() {
				// does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w tym świętym miejscu.");
				addHelp("Nie wiem czego potrzebujesz drogie dziecko.");
				addJob("Jestem zakonnicą. To jest moje życie, a nie praca.");
				addGoodbye("Dowidzenia. Niech pokój będzie z tobą.");
			}

			/*
			 * (non-Javadoc)
			 * @see games.stendhal.server.entity.npc.SpeakerNPC#onGoodbye(games.stendhal.server.entity.RPEntity)
			 */
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
			
		};

		nunnpc.setDescription("Oto Sister Benedicta święta zakonnica.");
		nunnpc.setEntityClass("nunnpc");
		nunnpc.setDirection(Direction.RIGHT);
		nunnpc.setPosition(53, 54);
		nunnpc.initHP(100);
		zone.add(nunnpc);
	}
}
