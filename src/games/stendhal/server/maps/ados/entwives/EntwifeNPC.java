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
package games.stendhal.server.maps.ados.entwives;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * entwife located in 0_ados_mountain_n2_w2.
 */
public class EntwifeNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildentwife(zone);
	}

	private void buildentwife(final StendhalRPZone zone) {
		final SpeakerNPC entwife = new SpeakerNPC("Tendertwig") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Prowadzę spokojne życie. Pilnuje wszystkiego dookoła.");
				addHelp("Tutaj jest sporo rzeczy do zobaczenia i zebrania. Rozglądnij się dookoła.");
				addOffer("Nie mam nic do zaoferowania poza świeżym powietrzem i słońcem.");
				addGoodbye("Niech twoje podróże będą przyjemne mój dobry przyjacielu.");
				addQuest("Jest coś co chciałabym, ale nie mam teraz czasu, aby o tym rozmawiać. Wróć później.");
			}
		};

		entwife.setEntityClass("transparentnpc");
		entwife.setAlternativeImage("tendertwig");
		entwife.setPosition(25, 35);
		entwife.initHP(100);
		entwife.setDescription("Widzisz starego i mądrego entwife. Zwą go Tendertwig, jest strażnikiem tej okolicy."); 
		zone.add(entwife);
	}
}
