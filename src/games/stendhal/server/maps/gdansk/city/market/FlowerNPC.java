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
package games.stendhal.server.maps.gdansk.city.market;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zekkeq
 */

public class FlowerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Petronela") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(30, 70));
				nodes.add(new Node(34, 70));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Przyszedłeś tutaj #pohandlować?");
				addReply(ConversationPhrases.YES_MESSAGES, "Dobrze! Mogę sprzedać Tobie piękną czerwoną różę. Nie chodzi mi o rzadką orchideę. Tylko Róża Kwiaciarka wie, gdzie one rosną i nikt nie wie gdzie jest Róża Kwiaciarka!");
				addReply(ConversationPhrases.NO_MESSAGES, "Bardzo dobrze. Jeżeli będę mogła pomóc to daj znać.");
				addJob("Sprzedaję tutaj róże.");
				addHelp("Słyszałam, że Mieczysław poszukuje osoby do zadania. Znajduje się on w naszym muzeum.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("róża", 55);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye("Dowidzenia i zapraszam ponownie!");
			}
		};

		npc.setEntityClass("woman_001_npc");
		npc.setPosition(30, 70);
		npc.initHP(100);
		zone.add(npc);
	}
}