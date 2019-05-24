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

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a little girl NPC (Elisabeth) in Kirdneh city. 
 *
 * @author Vanessa Julius idea by miasma
 */
public class LittleGirlNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Elisabeth") {

			@Override
			protected void createPath() {
				setPath(null);
		
			}

			@Override
			protected void createDialog() {
				// greeting message in quest given (ChocolateForElisabeth)
				addJob("Praca? Lubię bawić się moimi #zabawkami :)");
				addReply(Arrays.asList("toys", "zabawkami"), "Jest młody chłopak, który mieszka w Semos i raz dał mi jednego ze swoich pluszowych misi :) Takie to słodkie!");
				addHelp("Zapytaj mojej #mamy. Może ona Tobie pomoże...");
				addReply(Arrays.asList("mommy", "mama", "mamy"), "Siedzi tam na ławce i cieszy się słońcem.");
				addOffer("Nic nie mogę ci zaoferować. Jestem tylko #dzieckiem.");
				addReply(Arrays.asList("child", "dziecko", "dzieckiem"), "Mam 5, latek!");
				addQuest("Chcę czekolady :( Mama nie ma czasu, musi mnie pilnować. Przynieś mi proszę tabliczkę czekolady.");
				addGoodbye("Boiboi :)");
			}
		};

		npc.setEntityClass("littlegirl2npc");
		npc.setPosition(92, 15);
		npc.initHP(100);
		npc.setDescription("Oto Elisabeth. Wygląda na głodną.");
		zone.add(npc);
	}
}
