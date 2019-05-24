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
package games.stendhal.server.maps.ados.forest;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds Karl, the farmer NPC.
 * He gives horse hairs needed for the BowsForOuchit quest
 * He gives help to newcomers about the area
 * He suggests you can buy milk from his wife Philomena
 * @author kymara
 */
public class FarmerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFarmer(zone);
	}

	private void buildFarmer(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Karl") {

			/* 
			 * Karl walks around near the red barn and along the path some way.
			 */
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(64, 76));
				nodes.add(new Node(64, 86));	
				nodes.add(new Node(68, 86));	
				nodes.add(new Node(68, 84));
				nodes.add(new Node(76, 84));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(68, 86));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(60, 89));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(64, 86));
				setPath(new FixedPath(nodes, true));
			}
			
			@Override
			protected void createDialog() {
				addGreeting("Heja! Miło Cię widzieć w naszym gospodarstwie.");
				addJob("Och praca tutaj jest ciężka. Nawet nie myślę o tym, że mógłbyś mi pomóc.");
				addOffer("Nasze mleko jest najlepsze. Zapytaj moją żonę #Philomena o mleko. Ja sprzedaję #'puste worki'");
				addReply("Philomena","Ona jest w domku na południowy-zachód stąd.");
				addHelp("Potrzebujesz pomocy? Mogę coś ci opowiedzieć o #sąsiedztwie.");
				addReply(Arrays.asList("neighborhood.", "sąsiedztwie."),"Na północy znajduje się jaskinia z niedźwiedziami i innymi potworami. Jeżeli pójdziesz na północny-wschód " +
						"to po pewnym czasie dojdziesz do dużego miasta Ados. Na wschodzie jest duuuuża skała. Balduin " +
						"wciąż tam mieszka? Chcesz wyruszyć na południowy-wschód? Cóż.. możesz tamtędy dojść do Ados, ale " +
						"droga jest trochę trudniejsza.");
				addQuest("Nie mam teraz czasu na takie rzeczy. Pracuję.. pracuję.. pracuję..");
				addReply(Arrays.asList("empty sack", "puste worki"),"Oh mam tego mnóstwo na sprzedaż. Czy chcesz kupić #'pusty worek'.");
                final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("pusty worek", 10);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye("Dowidzenia, dowidzenia. Bądź ostrożny.");
			}
		};

		npc.setDescription("Oto Karl miły starszy rolnik.");
		npc.setEntityClass("beardmannpc");
		npc.setPosition(64, 76);
		npc.initHP(100);
		zone.add(npc);
	}
}
