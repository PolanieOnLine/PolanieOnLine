/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
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
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

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
		giles.addJob("Jestem rzemieślnikiem. Mój sklep jeszcze nie jest zbudowany, ale wciąż mogę #zaplecić dla Ciebie linę.");
		giles.addOffer("Umiem #pleść linę.");
		giles.addHelp("Jeśli potrzebujesz liny, mogę #zaplecić ją dla Ciebie.");
		giles.addReply(
			Arrays.asList("rope", "lina"),
			"Moje liny są wykonane z najlepszego #'końskiego włosia'.");
		giles.addReply(
			Arrays.asList("horse hair", "końskie włosie", "końskiego włosia"),
			"Wierzę, że #Karl ma zapas końskiej sierści. Ale być może będziesz musiał coś zrobić, zanim ci to sprzeda.");
		giles.addReply(
			"Karl",
			"Karl i jego żona Philomena opiekują się farmami na zachód od Ados.");

		// production
		final Map<String, Integer> required = new TreeMap<String, Integer>();
		required.put("money", 200);
		required.put("końskie włosie", 6);
		new ProducerAdder().addProducer(
			giles,
			new ProducerBehaviour(
				"ropemaker_braid_rope",
				Arrays.asList("braid", "zaplatanie", "pleść", "zaplecić"),
				"lina",
				required,
				15 * 60),
			"Hmmm... Myślę, że to dobre miejsce na zbudowania mojego sklepu. Och, cześć. Jak mogę ci pomóc?");

		return giles;
	}
}