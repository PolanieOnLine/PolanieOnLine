/* $Id: OzoNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.gorasmoka;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the NPC who deals in magiczny scroll.
 * Other behaviour defined in maps/quests/Labirynt.java
 *
 * @author kymara
 */
public class OzoNPC implements ZoneConfigurator {
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
	// IL0_GreeterNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC ozoNPC = new SpeakerNPC("Ozo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 49));
				nodes.add(new Node(33, 54));
				nodes.add(new Node(36, 54));
				nodes.add(new Node(36, 49));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Chyba wiesz czym się zajmuję.");
				addHelp("Jeśli mam być z Tobą szczery koleś to wiedz, że nie pomogę Ci z tym zbytnio, lepiej odejdź do miasta szukając odpowiedzi.");
				addQuest("Zadanie? u mnie?..ha ha... .");
				addOffer("Hm, mam coś co może cię zainteresować, #'magiczny bilet'.");
				addGoodbye("Dowidzenia.");
			}
		};

		ozoNPC.setEntityClass("scarletarmynpc");
		ozoNPC.setPosition(33, 49);
		ozoNPC.initHP(1000);
		zone.add(ozoNPC);
	}
}
