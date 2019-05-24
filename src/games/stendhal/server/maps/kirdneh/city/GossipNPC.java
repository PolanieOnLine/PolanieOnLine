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
package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a information giving NPC in Kirdneh city. 
 *
 * @author kymara
 */
public class GossipNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jef") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć!");
				addJob("Hm, nie wiem co masz na myśli. Teraz czekam na moją mamę, aż wróci ze #sklepów.");
				addHelp("Mam nowiny o bazarze.");
				addOffer("Nie sprzedaję, ja czekam tylko na mamę. Mam #nowiny jeżeli chcesz wiedzieć.");
				addQuest("Co? Nie rozumiem Cię.");
				addReply(Arrays.asList("news", "nowiny"), "Paru sprzedawców będzie wkrótce na bazarze! Będzie fajnie, a na razie jest tutaj pusto.");
				addReply(Arrays.asList("shops", "sklepów"), "Tak, ona wyjechała z miasteczka. Jedynie co tutaj mamy to kwiaciarkę! Są #nowiny, że będziemy mieli własny bazar ...");
				addGoodbye("Żegnaj.");
			}
		};

		npc.setEntityClass("kid6npc");
		npc.setPosition(114, 67);
		npc.initHP(100);
		npc.setDescription("Oto Jef. Wygląda jak by czekał na kogoś.");
		zone.add(npc);
	}
}
