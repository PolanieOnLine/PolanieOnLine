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
package games.stendhal.server.maps.ados.church;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * The healer (original name: Valo). He makes mega potions. 
 */

public class HealerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Valo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>(); 
				nodes.add(new Node(26, 5));
				nodes.add(new Node(29, 5));
				nodes.add(new Node(29, 3));
				nodes.add(new Node(37, 3));
				nodes.add(new Node(37, 9));
				nodes.add(new Node(22, 9));
				nodes.add(new Node(22, 12));
				nodes.add(new Node(5, 12));
				nodes.add(new Node(5, 9));
				nodes.add(new Node(26, 9));
				nodes.add(new Node(26, 3));
				nodes.add(new Node(8, 3));
				nodes.add(new Node(26, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Dawno temu byłem księdzem w tym kościele, ale moje #pomysły nie spodobały się innym."); 
				addReply(Arrays.asList("pomysły", "ideas"),
				"Czytałem wiele tekstów i uczyłem się o dziwnych zwyczajach. Teraz mogę sporządzić specjalną magiczną miksturę #'wielki eliksir' dla wojowników takich jak ty. Powiedz tylko #sporządź.");
				addReply("serce olbrzyma",
		        "Olbrzymy mieszkają w jaskiniach na wschód stąd. Powodzenia w zabijaniu tych bestii ...");
				addOffer("Mogę sporządzić #'wielki eliksir' dla Ciebie. Do tego będę potrzebował #'serce olbrzyma'. Powiedz tylko #sporządź.");
				addReply("wielki eliksir", "Jest to silny eliksir. Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #sporządź #1 #wielki #eliksir.");
				addReply("money", "To twój problem. My duchowni nie musimy biegać, aby zarobić pieniądze.");
				addHelp("Jeżeli chcesz być mądry tak jak ja to powinieneś odwiedzić bibliotekę. Tam jest sporo pomocy naukowych.");
				addGoodbye("Trzymaj się.");

				// Valo makes mega potions if you bring giant heart and money
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 400);
				requiredResources.put("serce olbrzyma", 1);
				final ProducerBehaviour behaviour = new ProducerBehaviour("valo_concoct_potion",
						Arrays.asList("concoct", "sporządź"), "wielki eliksir", requiredResources, 2 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Pozdrawiam młodzieńcze. #Uleczę Cię i #pomogę Tobie.");
				// charge (1*the player level + 1) to heal
				new HealerAdder().addHealer(this, -1);
			}
		};
		npc.setEntityClass("grandadnpc");
		npc.setDescription("Oto Valo. Jest otoczony pewnego rodzaju poświatą.");
		npc.setPosition(26, 5);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.initHP(100);
		zone.add(npc);
	}
}
