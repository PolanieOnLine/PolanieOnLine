/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A little girl needing some eggs
 * @see games.stendhal.server.maps.quests.EggsForMarianne
 */
public class LittleGirlNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Marianne") {
			@Override
			public void createDialog() {
				addGreeting("Witaj nieznajomy!");
				addHelp("Może nie mogę Tobie pomóc, ale ty... możesz pomóc mi!");
				addJob("Potrzebuję kurze jajeczka");
				addOffer("Potrzebuję kilka kurzych jajek");
				addGoodbye("Żegnaj, nieznajomy!");
				// All further behaviours are defined in appropriate quest class.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
		};

		npc.setPosition(89, 54);
		npc.setEntityClass("kid5npc");
		npc.setDescription("Widzisz małą dziewczynkę, może potrzebuje pomocy...");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}
}