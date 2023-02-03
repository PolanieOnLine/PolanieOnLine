/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.athor.cocktail_bar;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Cocktail Bar at the Athor island beach (Inside / Level 0).
 *
 * @author kymara
 */
public class BarmanNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBar(zone);
	}

	private void buildBar(final StendhalRPZone zone) {
		final SpeakerNPC barman = new SpeakerNPC("Pedro") {
			@Override
			protected void createDialog() {
				addJob("Mogę przyrządzić różne koktajle! Powiedz tylko #zrób.");
				addQuest("Co powiedziałeś?");
				addOffer("Może mogę #zrobić koktail z #kokosa i #ananasa dla ochłody... Powiedz tylko #zrób.");
				addReply("ananasa",
						"Ananasy nie rosną na Athor musisz sam je zdobyć.");
				addReply("kokosa",
						"Używam mleka z nich, aby #zrobić twój koktail. Poszukaj ich pod palmami.");
				addHelp("Chcesz napój z oliwką? Jestem do usług!");
				addGoodbye("Zdrówko!");
			}
		};

		barman.setDescription("Oto Pedro, barman. Może zmiksuje przepyszny koktajl dla ciebie.");
		barman.setEntityClass("barmannpc");
		barman.setGender("M");
		barman.setPosition(9, 5);
		barman.setDirection(Direction.DOWN);
		zone.add(barman);
	}
}
