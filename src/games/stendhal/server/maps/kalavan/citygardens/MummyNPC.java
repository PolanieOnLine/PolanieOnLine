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
package games.stendhal.server.maps.kalavan.citygardens;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the mother of Annie Jones.
 *
 * @author kymara
 */
public class MummyNPC implements ZoneConfigurator {
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
		final SpeakerNPC mummyNPC = new SpeakerNPC("Mrs. Jones") {
			@Override
			protected void createDialog() {
				// greeting in maps/quests/IcecreamForAnnie.java
				addOffer("Nie mogę. Jestem zajęta pilnowaniem mojej córki.");
				addQuest("Nic, dziękuję.");
				addJob("Jestem matką.");
				addHelp("Pomogę jeżeli będę mogła, ale muszę pilnować swojej córki.");
				addGoodbye("Do widzenia.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
		};

		mummyNPC.setDescription("Oto Mrs. Jones, kobieta odpoczywająca na ławce i obserwująca zabawę córki.");
		mummyNPC.setEntityClass("woman_000_npc");
		mummyNPC.setGender("F");
		mummyNPC.setPosition(53, 88);
		zone.add(mummyNPC);
	}
}
