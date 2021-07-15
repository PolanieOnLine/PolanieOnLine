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
package games.pol.server.maps.krakow.saddler;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class KrawiecNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("rzemieślnik Wojciech") {

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem krawcem. Potrzebujesz coś zszyć?");
				addHelp("Nie potrzebuję pomocy.");
				addOffer("Nie mam nic Tobie do zaoferowania.");
				addGoodbye("Do widzenia.");
			}
		};

		npc.setOutfit(3, 49, 3, 1, 9, 11, 14, 21, null);
		npc.setDescription("Oto krawiec rzemieślnik zwany Wojciech. Nie wygląda jakoś na zapracowanego.");
		npc.setGender("M");
		npc.setPosition(13, 10);
		zone.add(npc);
	}
}
