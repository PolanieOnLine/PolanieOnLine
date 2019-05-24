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
package games.stendhal.server.maps.wofol.bakery;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds the wofol baker NPC.
 *
 * @author kymara
 */
public class BakerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	//
	// BakerNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC baker = new SpeakerNPC("Kroip") {

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
				nodes.add(new Node(7, 6));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(15, 5));
				nodes.add(new Node(27, 5));
				nodes.add(new Node(27, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// This isn't bad grammar. It's his tyle of speech! Do't correct pls.
				addJob("Wyrabiam pizzę. Nauczyłem się tego od wielkiego piekarza #Leander. Powiedz mi tylko #upiecz, abym stworzył dla Ciebie pizzę.");
				addReply(Arrays.asList("button mushroom", "pieczarka", "porcini", "borowik"),
				        "#Leander nauczył mnie, że grzyby występują w obszarach, gdzie jest drewno.");
				addReply(Arrays.asList("flour", "mąka"), "Mieli się ze zboża koło Semos.");
				addReply(Arrays.asList("cheese", "ser"), "Ser? Nie wiem.");
				addReply(Arrays.asList("tomato", "pomidor"), "To rośnie w szklarniach.");
				addReply(Arrays.asList("ham", "szynka"), "Świnki mają ham.");
				addHelp("Pracuję z #Leander. Wyrabiam pizzę. Wystarczy powiedzieć #upiecz a ci ją upiekę.");
				addReply("Leander", "Pracowałem z nim w Semos. Wielki Leander nauczył mnie jak wyrabiać pizzę.");
				addQuest("#Leander zajmuje się dostawami pizzy, a ja je wyrabiam. Tutaj masz składniki. Powiedz tylko #zrób o ile masz ochotę na pizzę.");
				addGoodbye("Nie bierz świecy!");

				// makes a pizza if you bring flour cheese mushroom porcini and ham
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("mąka", 2);
				requiredResources.put("ser", 1);
				requiredResources.put("pomidor", 1);
				requiredResources.put("pieczarka", 2);
				requiredResources.put("borowik", 1);
				requiredResources.put("szynka", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("kroip_make_pizza", Arrays.asList("make", "upiecz"), "pizza",
				        requiredResources, 5 * 60, true);

				new ProducerAdder().addProducer(this, behaviour,
				        "Witam!");
			}
		};

		baker.setEntityClass("koboldchefnpc");
		baker.setPosition(15, 3);
		baker.initHP(1000);
		baker.setDescription("Oto Kroip. Był stażystą Leandra, a obecnie jest znany jako piekarz pizzy w Wofol.");
		zone.add(baker);
	}
}
