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
package games.stendhal.server.maps.ados.farmhouse;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mother NPC
 *
 * @author kymara
 */
public class MotherNPC implements ZoneConfigurator {

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

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Anastasia") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(30, 4));
				nodes.add(new Node(41, 4));
				nodes.add(new Node(41, 9));
				nodes.add(new Node(30, 9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Przykro mi, ale nie mam za dużo czasu, gdy mój synek choruje...");
				addJob("Mój brat opiekuje się tą farmą. Ja tylko opiekuję się tutaj synem.");
				addHelp("Philomena sprzeda Ci mleko i masło.");
				addGoodbye("Dowidzenia.");
			}
		};
		npc.setEntityClass("woman_006_npc");
		npc.setPosition(30, 9);
		npc.initHP(100);
		npc.setDescription("Oto Anastasia, wygląda na przybitą.");
	    zone.add(npc);
	}
}
