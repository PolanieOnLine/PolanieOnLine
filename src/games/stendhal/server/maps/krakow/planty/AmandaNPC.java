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
package games.stendhal.server.maps.krakow.planty;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class AmandaNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Amanda") {

			@Override
			protected void createDialog() {
				addJob("Jestem pełno etatową matką i kocham to.");
				addHelp("Nie potrzebuję pomocy, ale możesz zapytać się mojej córki.");
				addOffer("Przykro mi, ale nie mam żadnych ofert dla Ciebie.");
				addQuest("Nie mam zadań dla Ciebie, ale moja córka #Balbina potrzebuje pomocy.");
				addReply("Balbina", "Jest takim kochanym dzieckiem. Zawsze dbam o nią!");
				addGoodbye("Dziękuję za spotkanie.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("mothernpc");
		npc.setDescription("Oto Amanda. Opiekuje się swoją córką Balbiną.");
		npc.setPosition(95, 66);
		zone.add(npc);
	}
}