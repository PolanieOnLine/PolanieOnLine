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
package games.stendhal.server.maps.ados.tunnel;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * In recreation room of Blordrough's habitat in -1_ados_outside_w.
 */
public class CrulaminNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildCrulamin(zone);
	}

	private void buildCrulamin(final StendhalRPZone zone) {
		final SpeakerNPC crulamin = new SpeakerNPC("Crulamin") {
			@Override
			public void createDialog() {
				addGreeting("Jestem zajęty. Zostaw mnie w spokoju. Może wrócisz i odwiedzisz mnie innego dnia?");
				addOffer("Jestem osobą, która chce grać w szachy.");
				addJob("Ciężko się uczę, aby zostać Szachowym Ekspertem.");
				addHelp("Może kiedyś mi pokażesz do czego jesteś zdolny. Nim ten nadejdzie to proszę odejdź.");
				addGoodbye("Hmmmm. Co jeśli ruszę się tutaj, a potem tam...");
				// all other behaviour is defined in the quest.
			}
		};

		crulamin.setDescription("Oto wojownik Crulamin, który porzucił wszystko dla gry w szachy. Jego marzeniem jest zostanie 'Najlepszym'!");
		crulamin.setEntityClass("chessplayernpc");
		crulamin.setGender("M");
		crulamin.setPosition(73,93);
		zone.add(crulamin);
	}
}
