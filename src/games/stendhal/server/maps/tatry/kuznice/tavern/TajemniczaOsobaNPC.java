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
package games.stendhal.server.maps.tatry.kuznice.tavern;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class TajemniczaOsobaNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildnpc(zone);
	}

	private void buildnpc(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Tajemnicza osoba") {

			@Override
			protected void createDialog() {
				addGreeting("Hmm?");
				addJob("Niczym się zajmuję.");
				addHelp("Może będziesz mógł mi w czymś pomóc.");
				addGoodbye();
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("npctajemniczaosoba");
		npc.setPosition(9, 34);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
