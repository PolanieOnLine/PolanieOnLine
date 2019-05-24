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
package games.stendhal.server.maps.kalavan.citygardens;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a little girl called Annie Jones.
 *
 * @author kymara
 */
public class LittleGirlNPC implements ZoneConfigurator {
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
		createNPC(zone);
	}


	private void createNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Annie Jones") {
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(44, 90));
				nodes.add(new Node(44, 86));
				nodes.add(new Node(42, 86));
				nodes.add(new Node(42, 90));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {	
				// greeting and quest in maps/quests/IcecreamForAnnie.java
			addOffer("Jestem małą dziewczynką. Nie mam nic do zaoferowania.");
			addJob("Pomagam mojej mamusi.");
			addHelp("Zapytaj mamusi.");
			addGoodbye("Ta ta.");
			}
		};

		npc.setDescription("Oto mała dziewczynka, bawiąca się na placu zabaw.");
		npc.setEntityClass("pinkgirlnpc");
		npc.setPosition(44, 90);
		npc.initHP(100);
		zone.add(npc);
	}
}
