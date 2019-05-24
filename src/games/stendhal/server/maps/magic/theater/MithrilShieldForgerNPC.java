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
package games.stendhal.server.maps.magic.theater;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Configure Baldemar - mithril shield forger.
 *
 * @author kymara
 */
public class MithrilShieldForgerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildbaldemar(zone);
	}

	private void buildbaldemar(final StendhalRPZone zone) {
		final SpeakerNPC baldemar = new SpeakerNPC("Baldemar") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Och cześć. Właśnie patrzysz na naszą lokalną tradycję.");
				addJob("Jestem czarodziejem. Studiowałem długo i ciężko, aż do perfekcji opanowałem sztukę wytapiania #mithrilu.");
				addHelp("Mogę wykuć tarczę dla Ciebie, o której tylko marzyłeś.");
				addOffer("Dam Ci radę. Poszukaj wszystkiego co potrzebuję do zrobienia tarczy dla Ciebie. Będziesz mi wdzięczny.");
				addReply(Arrays.asList("mithril", "mithrilu"), "Mithril był składowany przez żołnierzy Mithrilbourghtów w piwnicy dopóki się nie przenieśli. Nie wiem skąd pochodzi.");
				addGoodbye("Na razie. Spróbuj lukrecję Trilliuma. Można za nią oddać życie.");
			} //remaining behaviour defined in quest
		};

		baldemar.setDescription("Oto Baldemar czarodziej Mithrilbourghtów, który studiował wytapianie mithrilu.");
		baldemar.setEntityClass("mithrilforgernpc");
		baldemar.setPosition(4, 6);
		baldemar.initHP(100);
		zone.add(baldemar);
	}
}
