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

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a information giving NPC in Kirdneh city. 
 *
 * @author Vanessa Julius idea by miasma
 */
public class MummyNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Carey") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				//Greeting message in quest given (ChocolateForElisabeth)
				addJob("Jestem pełno etatową matką i kocham to.");
				addHelp("Słyszałam, że pare #domów w pobliżu wciąż jest na sprzedaż.");
				addReply(Arrays.asList("houses", "domów"), "Są ogromne! Mój przyjaciel jest właścicielem jednego i zaprrosił mnie parę razy.");
				addOffer("Przykro mi, ale nie mam żadnych #ofert dla Ciebie.");
				addReply(Arrays.asList("offers", "ofert"), "Odwiedziłeś już baza w Kirdneh? Pięknie tam pachnie.");
				addQuest("Nie mam zadań dla Ciebie, ale moja córka #Elisabeth szuka czekolady.");
				addReply("Elisabeth", "Jest takim kochanym dzieckiem. Zawsze dbam o nią!");
				addGoodbye("Dziękuję za spotkanie.");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("mothernpc");
		npc.setPosition(84, 9);
		npc.initHP(100);
		npc.setDescription("Oto Carey. Opiekuje się swoją córką Elisabeth.");
		zone.add(npc);
	}
}
