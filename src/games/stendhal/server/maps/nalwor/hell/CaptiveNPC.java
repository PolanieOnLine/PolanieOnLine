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
package games.stendhal.server.maps.nalwor.hell;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Inside Nalwor Hell - level -1 .
 */
public class CaptiveNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTomi(zone);
	}

	private void buildTomi(final StendhalRPZone zone) {
		final SpeakerNPC tomi = new SpeakerNPC("tomi") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("pomocy!");
				addJob("pomóż mi");
				addHelp("gdzie jest mój miecz lodowy?");
				addOffer("proszę miecz lodowy");
				addGoodbye("dowidzenia");
			}
		};

		tomi.setEntityClass("transparentnpc");
		tomi.setAlternativeImage("tomi");
		tomi.setPosition(119, 13);
		tomi.setBaseHP(100); 
		tomi.setHP(50);
		tomi.setDescription("Oto Tomi. Jest cały spocony i na pewno potrzebuje coś do ochłodzenia.");
		zone.add(tomi);
	}
}
