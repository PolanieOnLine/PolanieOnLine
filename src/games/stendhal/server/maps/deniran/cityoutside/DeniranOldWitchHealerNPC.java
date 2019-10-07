/***************************************************************************
 *                    (C) Copyright 2003-2019 - Stendhal                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityoutside;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

/**
 * Provides Ermenegilda, a Healer for Deniran
 *
 * @author omero
 *
 */
public class DeniranOldWitchHealerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] mumbles = {
			"Szczypta niebieskiego!",
			"Dotyk grzywki!",
			"Rzut oka dziwny..."
		};
		new MonologueBehaviour(buildNPC(zone), mumbles, 1);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ermenegilda") {

			@Override
			public void createDialog() {
				addGreeting("Witaj.");
				addJob("Och, nie przejmuj się mną, jestem tylko starszą kobietą.");
				addOffer("Mogę ciebie #'uleczyć'.");
				addGoodbye();
			}
		};

		// Finalize Ermenegilda
		npc.setEntityClass("oldwitchnpc");
		npc.setDescription("Oto Ermenegilda. Może ciebie uleczyć.");
		npc.setPosition(18, 105);
		//heal according to player level (cost -1)
		new HealerAdder().addHealer(npc, -1);
		npc.initHP(100);
		zone.add(npc);
		return npc;
	}
}
