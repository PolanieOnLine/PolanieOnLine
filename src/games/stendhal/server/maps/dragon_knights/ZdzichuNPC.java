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
// Base on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
package games.stendhal.server.maps.dragon_knights;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class ZdzichuNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Jubiler Zdzichu") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj dzielny rycerzu!");
				addJob("Jestem jubilerem na tych włościach. #Zadanie mam dla ciebie jeżeli okarze się, że jesteś godny zaufania.");
				addHelp("Nie jesteś w stanie mi pomóc, więc odejdź.");
				addGoodbye("Żegnam.");
			}
		};

		npc.setDescription("Oto Jubiler Zdzichu.");
		npc.setEntityClass("blacksmithnpc");
		npc.setPosition(10, 6);
		npc.initHP(1000);
		zone.add(npc);
	}
}
