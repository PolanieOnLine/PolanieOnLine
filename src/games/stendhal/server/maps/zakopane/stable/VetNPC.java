/* $Id: VetNPC.java,v 1.0 2018/07/01 22:51:37 KarajuSs Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.stable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build the VetNPC
 *
 * @author KarajuSs
 */
public class VetNPC implements ZoneConfigurator {

	 @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Dr. Wojciech") {
			
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 12));
				nodes.add(new Node(5, 12));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem weterynarzem, a aktualnie próbuje właśnie wyleczyć konie stajennego.");
				addHelp("Tak, możesz mi pomóc. Powiedz mi tylko #'zadanie'.");
				addGoodbye();
			}
		};

		npc.setEntityClass("doctornpc");
		npc.setPosition(26, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}