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
package games.stendhal.server.maps.semos.bakery;

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
 * The bakery chef. Father of the camping girl.
 * He makes sandwiches for players.
 * He buys cheese.
 *
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Leander") {

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
				addJob("Jam jest tutejszym piekarzem. Jedną z usług jakie prowadzę na tym terenie jest produkcja placków o niezwykłej nazwie #pizza . Zanim rozpętała się tu wojna, a oddziały z Ados zablokowały drogi wielu było chętnych na te smaczne placki. Ale dzięki temu mam więcej czasu na produkcję kanapek dla naszych drogich klientów, którzy chwalą sobie ich smak!");
				addHelp("Jeśli chcesz zarobić trochę dukatów wyświadcz mi #przysługę i dostarcz to ciasto co zwą #pizza . Moja siostra #Sally robiła to do tej pory, ale teraz jest gdzieś na biwaku.");
				addReply("chleb", "Tym w naszej firmie zajmuje się Erna. Podejdź do niej i porozmawiaj.");
				addReply("ser",
				"Z serem mamy spore trudności, bo mieliśmy niedawno plagę szczurów. Zastanawia mnie jak te wstrętne szkodniki zabrały wszystko ze sobą? Dlatego do kanapek dodajemy oscypek z mleka naszych owiec.");
				addReply("ham",
				"Cóż, wyglądasz mi na dzielnego łowcę. Może po prostu idź do lasu i zapoluj. Tylko nie przynoś mi tych małych kawałków mięsiwa i suchych steków. Do kanapek nadaje się tylko najwyższej klasy szynka!");
				addReply("Sally",
				"Moja siostra Sally może pomóc Ci w zdobyciu szynki. Natura to jej dom. Ostatnio doszły mnie słuchy, że rozbiła obóz na południe od Zamku Orril.");
				addReply(Arrays.asList("pizza", "pizzę"), "Potrzebuję kogoś, kto dostarczy pizzę. Może przyjmiesz to #zadanie.");
				addReply(Arrays.asList("sandwich", "sandwiches","kanapka","kanapki"),
				"Moje kanapki są smaczne i pożywne. Jeśli chcesz abym zrobił jedną dla Ciebie powiedz #'zrób <ilość> kanapka' .");
				addOffer("Moja #pizza potrzebuje sera, a my nie mamy żadnych zapasów. Kupię od Ciebie ser jeżeli chcesz #sprzedać.");
				final Map<String, Integer> offers = new TreeMap<String, Integer>();
				offers.put("ser", 5);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(offers), false);

				addGoodbye();

				// Leander makes sandwiches if you bring him bread, cheese, and ham.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("chleb", 1);
				requiredResources.put("ser", 2);
				requiredResources.put("szynka", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
						"leander_make_sandwiches", Arrays.asList("make", "zrób"), "kanapka",
						requiredResources, 3 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj! Jakże miło, że zawitałeś do mojej piekarni, gdzie robię ciasto o zamorskiej nazwie #pizza oraz #kanapki.");
			}};

			npc.setPosition(15, 3);
			npc.setEntityClass("chefnpc");
			npc.setDescription("Oto Leander. Jego praca daje mu piękny zapach.");
			zone.add(npc);
	}
}
