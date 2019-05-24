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
package games.stendhal.server.maps.gdansk.museum;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class MieczysławNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneRatuszArea(zone);
	}

	private void buildZakopaneRatuszArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mieczysław") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addJob("Jestem Mieczysław. Opiekuje się miastem Gdańsk!");
				addHelp("Nasze miasto potrzebuje różnych rzeczy. Jeżeli potrzebujesz banku to znajdziesz go na południe od muzeum.");
				addOffer("Już nie sprzedaję zwojów. Tym zajmuje się teraz Juhas. Może chciałbyś się zasłużyć dla Gdańska podejmując się #zadania. Nagroda Cię nie minie.");
				addGoodbye("Życzę powodzenia!");
			}
		};

		npc.setEntityClass("beggarnpc");
		npc.setPosition(24, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
