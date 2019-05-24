/* $Id: ShopAssistantNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
 
 //Zrobiony na podstawie asystent piekarza z Semos.
 
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


public class ShopAssistantNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}


	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Małgosia") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 9));
				nodes.add(new Node(26, 6));
				nodes.add(new Node(28, 6));
				nodes.add(new Node(28, 2));
				nodes.add(new Node(28, 5));
				nodes.add(new Node(22, 5));
				nodes.add(new Node(22, 4));
				nodes.add(new Node(22, 7));
				nodes.add(new Node(26, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {

				addJob("Ja piekę chleb w tej piekarni.");
				addReply("mąka",
						"Do naszej pracy potrzebujemy mąkę, którą mielono we młynie na północ stąd, ale wilki pożarły chłopca, który nam ją przynosił! Jeśli przyniesiesz nam mąkę w nagrodę upieczemy przepyszny chleb dla Ciebie. Powiedz tylko #upiecz.");
				addHelp("Chleb jest bardzo dobry, zwłaszcza dla takiego śmiałka jak ty, któremu już niedobrze, gdy widzi surowe mięsiwo. Mój szef Jaś, robi najlepsze kanapki w okolicy!");
				addGoodbye();
			}
		};

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mąka", 2);

		final ProducerBehaviour behaviour = new ProducerBehaviour("malgosia_bake_bread",
				Arrays.asList("bake", "upiecz"), "chleb", requiredResources, 7 * 60);

		new ProducerAdder().addProducer(npc, behaviour,
						"Witaj w piekarni w Zakopanem! Możemy upiec pyszny chleb dla każdego kto pomoże nam przynosząc mąkę z młyna. Powiedz tylko #upiecz.");

		npc.setEntityClass("housewifenpc");
		npc.setPosition(26, 9);
		npc.initHP(1000);
		zone.add(npc);
	}
}
