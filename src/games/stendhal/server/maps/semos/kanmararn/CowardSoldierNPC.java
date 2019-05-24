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
package games.stendhal.server.maps.semos.kanmararn;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CowardSoldierNPC implements ZoneConfigurator {


   /**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHideoutArea(zone);
	}

	private void buildHideoutArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Henry") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(57, 113));
				nodes.add(new Node(59, 113));
				nodes.add(new Node(59, 115));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ciii! Cicho bo ściągniesz więcej #krasnali.");
				addJob("Jestem żołnierzem.");
				addGoodbye("Dowidzenia i uważaj na wszystkie te krasnale!");
				addHelp("Potrzebuję pomocy. Zostałem oddzielony od #grupy. Teraz jestem sam.");
				addReply(Arrays.asList("dwarf", "dwarves", "krasnali", "krasnale"),
					"Są wszędzie! Ich #królestwo musi być w pobliżu.");
				addReply(Arrays.asList("kingdom", "Kanmararn", "królestwo"),
					"Kanmararn legendarne miasto #krasnali.");
				addReply(Arrays.asList("group", "grupy"),
					"Generał wysłał naszą piątkę do zbadania tego obszaru w poszukiwaniu #skarbu.");
				addReply(Arrays.asList("treasure", "skarbu"),
					"Duży skarb musi być ukryty #gdzieś w tych podziemiach.");
				addReply(Arrays.asList("somewhere", "gdzieś"), "Jeżeli mi #pomożesz to dam ci wskazówkę.");
			}
			// remaining behaviour is defined in maps.quests.KanmararnSoldiers.
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setDescription("Oto Henry. Jest jednym z zaginionych żołnierzy Semos, ukrywa się w ciemnej jaskini ...");
		npc.setPosition(57, 113);
		npc.setBaseHP(100);
		npc.initHP(20);
		npc.setLevel(5);
		zone.add(npc);
	}
}
