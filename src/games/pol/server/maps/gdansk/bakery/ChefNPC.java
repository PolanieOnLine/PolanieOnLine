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
package games.pol.server.maps.gdansk.bakery;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author daniel @edit by KarajuSs
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ernest") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the well
				nodes.add(new Node(15,3));
				// to a barrel
				nodes.add(new Node(15,8));
				// to the baguette on the table
				nodes.add(new Node(13,8));
				// around the table
				nodes.add(new Node(13,10));
				nodes.add(new Node(10,10));
				// to the sink
				nodes.add(new Node(10,12));
				// to the pizza/cake/whatever
				nodes.add(new Node(7,12));
				nodes.add(new Node(7,10));
				// to the pot
				nodes.add(new Node(3,10));
				// towards the oven
				nodes.add(new Node(3,4));
				nodes.add(new Node(5,4));
				// to the oven
				nodes.add(new Node(5,3));
				// one step back
				nodes.add(new Node(5,4));
				// towards the well
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
				final Map<String, Integer> offers = new TreeMap<String, Integer>();
				offers.put("ser", 5);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(offers), false);

				addGoodbye();

				// Leander makes sandwiches if you bring him bread, cheese, and ham.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("mąka", 1);
				requiredResources.put("mleko", 1);
				requiredResources.put("jajo", 1);
				requiredResources.put("osełka masła", 1);
				requiredResources.put("cukier", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
						"ernest_make_donuts", Arrays.asList("make", "upiecz"), "pączek",
						requiredResources, 3 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj! Jakże miło, że zawitałeś do mojej piekarni, gdzie robię ciasto o przepysznej nazwie #pizza oraz #'pączki'.");
			}};

			npc.setDescription("Oto Ernest. Jego praca daje mu piękny zapach.");
			npc.setEntityClass("chefnpc");
			npc.setGender("M");
			npc.setPosition(15, 3);
			zone.add(npc);
	}
}
