/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.sukiennice;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class ChengNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Cheng") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj tubylcze.");
				addJob("Poszukuję rzadkich kamieni, ładnych wyrobów jubilerskich. Możesz coś #zaoferować.");
				addHelp("Skupuję rzadkie kamienie i wyroby jubilerskie, jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Zaglądnij do księgi, tam znajdziesz moją ofertę.");
				addQuest("Hm... opowiedz mi o waszych zwyczajach (regulaminie PolanieOnLine).");
				addGoodbye("Do widzenia.");
			}
		};

		npc.setDescription("Oto Cheng, kupiec z Chin.");
		npc.setEntityClass("npcwikary");
		npc.setGender("M");
		npc.setPosition(26, 30);
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}
}
