/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.gdansk.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author KarajuSs
 */

public class MieszkaniecGdanska4NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGdanskMieszkancy(zone);
	}

	private void buildGdanskMieszkancy(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ścibor") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(18, 101));
				nodes.add(new Node(18, 110));
				nodes.add(new Node(20, 110));
				nodes.add(new Node(20, 118));
				nodes.add(new Node(22, 126));
				nodes.add(new Node(49, 126));
				nodes.add(new Node(49, 121));
				nodes.add(new Node(51, 121));
				nodes.add(new Node(51, 116));
				nodes.add(new Node(53, 116));
				nodes.add(new Node(53, 112));
				nodes.add(new Node(56, 112));
				nodes.add(new Node(56, 105));
				nodes.add(new Node(58, 105));
				nodes.add(new Node(58, 94));
				nodes.add(new Node(55, 94));
				nodes.add(new Node(55, 88));
				nodes.add(new Node(53, 88));
				nodes.add(new Node(53, 83));
				nodes.add(new Node(51, 83));
				nodes.add(new Node(51, 80));
				nodes.add(new Node(49, 80));
				nodes.add(new Node(49, 78));
				nodes.add(new Node(47, 78));
				nodes.add(new Node(47, 76));
				nodes.add(new Node(44, 76));
				nodes.add(new Node(44, 73));
				nodes.add(new Node(42, 73));
				nodes.add(new Node(42, 72));
				nodes.add(new Node(30, 72));
				nodes.add(new Node(30, 74));
				nodes.add(new Node(21, 74));
				nodes.add(new Node(21, 77));
				nodes.add(new Node(19, 77));
				nodes.add(new Node(19, 86));
				nodes.add(new Node(18, 86));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam, nazywam się Ścibor i jestem mieszkańcem tego miasteczka.");
				addJob("Nie mam żadnej pracy Tobie do zaoferowania, ale może jak się przejdziesz do naszego #'muzeum' to.... *Ciii* Właściciel tego muzeum szuka chętnych osób do pracy.");
				addHelp("Pomocy? Ja nie potrzebuję pomocy.");
				addOffer("Nie posiadam nic do sprzedania. Przejdź się do #'tawerny' lub #'karczmy' (różnie ludzie to nazywają), która znajduje się tutaj zarazem obok muzeum.");
				addReply(Arrays.asList("tawerna", "karczma", "tavern"),
						"W tawernie pracuje pewna osoba o imieniu Rosa. Jest ona barmantką tego lokalu. Słyszałem również, że właścicel tej tawerny ma mały #'problem ze szczurami' i poprosiła Rose o poszukaniu odpowiedniej osoby do tej roboty.");
				addReply("problem ze szczurami",
						"Słyszałem jedynie pogłoski tego. Nie jestem do końca przekonany, ale możesz pójść i sprawdzić pytając się Rosy o #'zadanie'. Jak to będzie prawda możliwe, że otrzymasz jakąś nagrodę za wykonanie tego zlecenia.");
				addReply(Arrays.asList("muzeum", "museum"),
						"Muzeum jest chlubą tego miasta.. Pokazuje ono jak kiedyś miasto wyglądało oraz jak się rozwinęło.");
				addGoodbye("Życzę Ci powodzenia!");
			}
		};

		npc.setDescription("Oto Ścibor. Wygląda jakby wiedział bardzo dużo na temat tego miasta.");
		npc.setEntityClass("manwithhatnpc");
		npc.setGender("M");
		npc.setPosition(18, 101);
		zone.add(npc);
	}
}
