/***************************************************************************
 *                    Copyright © 2003-2023 - Arianne                      *
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

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class RopemakerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		zone.add(buildNPC());
	}

	private SpeakerNPC buildNPC() {
		final SpeakerNPC giles = new SpeakerNPC("Giles");

		giles.setEntityClass("ropemakernpc");
		giles.setPosition(11, 57);
		giles.setIdleDirection(Direction.LEFT);

		//giles.addGreeting();
		giles.addGoodbye();
		giles.addJob("Jestem rzemieślnikiem. Mój sklep jeszcze nie jest zbudowany, ale wciąż mogę #zapleść dla Ciebie linę.");
		giles.addOffer("Umiem #pleść linę.");
		giles.addHelp("Jeśli potrzebujesz liny, mogę #zapleść ją dla Ciebie.");
		giles.addReply(
			Arrays.asList("rope", "lina"),
			"Moje liny są wykonane z najlepszego #'końskiego włosia'.");
		giles.addReply(
			Arrays.asList("horse hair", "końskie włosie", "końskiego włosia"),
			"Wierzę, że #Karl ma zapas końskiej sierści. Ale być może będziesz musiał coś zrobić, zanim ci to sprzeda.");
		giles.addReply(
			"Karl",
			"Karl i jego żona Philomena opiekują się farmami na zachód od Ados.");

		return giles;
	}
}