/* $Id: ChefNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
 
//Zrobiony na podstawie ChefNPC z Semos. 

package games.stendhal.server.maps.zakopane.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;


public class ChefNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}


	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jaś") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(3, 10));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(5, 3));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(15, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jam jest tutejszym piekarzem. Jedną z usług jakie prowadzę na tym terenie to produkcja kanapek dla naszych drogich klientów, którzy chwalą sobie ich smak! Powiedz tylko #zrób.");
				addHelp("Zajmuje się tylko robieniem kanapek. Powiedz #zrób jeżeli zdecydujesz się na #kanapkę.");
				addReply("chleb", "Tym w naszej firmie zajmuje się Erna. Podejdź do niej i porozmawiaj.");
				addReply("ser",
						"Z serem mamy spore trudności, bo mieliśmy niedawno plagę szczurów. Zastanawia mnie jak te wstrętne szkodniki zabrały wszystko ze sobą? Dlatego do kanapek dodajemy oscypek z mleka naszych owiec.");
				addReply("szynka",
						"Cóż, wyglądasz mi na dzielnego łowcę. Może po prostu idź do lasu i zapoluj. Tylko nie przynoś mi tych małych kawałków mięsiwa i suchych steków. Do kanapek nadaje się tylko najwyższej klasy szynka!");
				addReply(Arrays.asList("sandwich", "sandwiches", "kanapka", "kanapki", "kanapkę"),
						"Moje kanapki są smaczne i pożywne. Jeśli chcesz abym zrobił jedną dla Ciebie powiedz #'zrób' .");
				addGoodbye();
			}
		};

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("chleb", 1);
		requiredResources.put("ser", 2);
		requiredResources.put("szynka", 1);

		final ProducerBehaviour behaviour = new ProducerBehaviour(
				"jas_make_sandwiches", Arrays.asList("make", "zrób"), "kanapka",
				requiredResources, 3 * 60);

		new ProducerAdder().addProducer(npc, behaviour,
				"Witaj! Jakże miło, że zawitałeś do mojej piekarni, gdzie robię #kanapki.");

		npc.setEntityClass("chefnpc");
		npc.setPosition(15, 3);
		npc.initHP(1000);
		zone.add(npc);
	}
}
