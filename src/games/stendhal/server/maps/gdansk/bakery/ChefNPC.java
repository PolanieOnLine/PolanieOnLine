/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author daniel @edit by KarajuSs
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ernest") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15,3));
				nodes.add(new Node(15,8));
				nodes.add(new Node(13,8));
				nodes.add(new Node(13,10));
				nodes.add(new Node(10,10));
				nodes.add(new Node(10,12));
				nodes.add(new Node(7,12));
				nodes.add(new Node(7,10));
				nodes.add(new Node(3,10));
				nodes.add(new Node(3,4));
				nodes.add(new Node(5,4));
				nodes.add(new Node(5,3));
				nodes.add(new Node(5,4));
				nodes.add(new Node(15,4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Jam jest tutejszym piekarzem. Jedną z usług jakie prowadzę na tym terenie jest produkcja placków o niezwykłej nazwie #'pizza' oraz #'pączki'. Ostatnio widuję bardzo dużo głodnych klientów, którzy chwalą sobie ich smak!");
				addHelp("Jeśli chcesz zarobić trochę dukatów wyświadcz mi #przysługę i dostarcz to ciasto co zwą #'pizza'.");
				addReply("mąka", "Nasze dostawy pochodzą z królewskiego grodu.");
				addReply(Arrays.asList("mleko", "osełka masła"),
						"Philomena sprzedaje krowie mleko oraz osełke masła.");
				addReply(Arrays.asList("jajo", "cukier"),
						"Masz ogromne szczęście! Nasza znajoma Hermina zajmuje się sprowadzaniem jajekm, cukru oraz innych składników z różnych krain.");
				addReply(Arrays.asList("pizza", "pizzę"), "Potrzebuję kogoś, kto dostarczy pizzę. Może przyjmiesz to #zadanie.");
				addReply(Arrays.asList("donut", "donuts", "pączek", "pączki"),
						"Moje pączki jak to wielu klientów mówi są genialne. Jeśli chcesz, abym zrobił jednego dla Ciebie powiedz #'upiecz <ilość> pączek'.");
				addOffer("Moja #pizza potrzebuje sera, a my nie mamy żadnych zapasów. Kupię od Ciebie ser jeżeli chcesz #sprzedać.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Ernest. Jego praca daje mu piękny zapach.");
		npc.setEntityClass("chefnpc");
		npc.setGender("M");
		npc.setPosition(15, 3);
		zone.add(npc);
	}
}
