/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.desert;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.idle.WanderIdleBehaviour;

public class DesertWandererNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Omar") {
			@Override
			protected void createDialog() {
				addGreeting("Wędrowcze, cóż cię przywiało na tę jałową pustynię?");
				addJob("Jestem poszukiwaczem reliktów. Pod piaskiem znajduję pozostałości dawnych matryc glifów, które kiedyś służyły jako nośniki magicznych znaków. Zoryk Runiczny potwierdził, że część z nich można jeszcze wykorzystać.");
				addOffer("Jeżeli chcesz pomóc mi w wykopaliskach, zapytaj o #zadanie. Ja potrafię odnaleźć i zrekonstruować stare matryce, a Zoryk Runiczny potrafi nasycić odzyskany fragment nową mocą.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Omar, pustynny poszukiwacz reliktów badający pozostałości dawnych matryc glifów.");
		npc.setEntityClass("nomadnpc");
		npc.setGender("M");
		npc.setIdleBehaviour(new WanderIdleBehaviour());
		npc.setPosition(12, 6);
		// hide location from website
	 	npc.hideLocation();
		zone.add(npc);
	}
}
