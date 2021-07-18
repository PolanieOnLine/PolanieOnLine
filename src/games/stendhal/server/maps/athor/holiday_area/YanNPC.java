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
package games.stendhal.server.maps.athor.holiday_area;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class YanNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Yan") {
			@Override
			public void createDialog() {
				addGreeting("Witaj nieznajomy!");
				addQuest("Nie mam dla Ciebie zadania.");
				addJob("Jestem na wakacjach. Nie chcę rozmawiać o pracy.");
				addHelp("Bar z koktajlami jest otwarty! Poszukaj chatki ze słomką na dachu.");
				addGoodbye("Do zobaczenia później!");
			}
		};

		npc.setDescription ("Oto Yan. Wyleguje się na plaży i popija koktaile.");
		npc.setEntityClass("swimmer4npc");
		npc.setGender("M");
		npc.setPosition(62, 72);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
