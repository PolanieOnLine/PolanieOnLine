/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.warszawa.castle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class KnightNPC implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("rycerz Mark") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(82, 80));
				nodes.add(new Node(75, 80));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting();
				addJob("Kiedyś zajmowałem się patrolowaniem, a teraz niestety próbuję ze swoim oddziałem odbić zamek.");
				addHelp("Każda #pomoc się nada. Potrzebuję pomocy w pewnym #zadaniu.");
				addOffer("Zaoferować mogę #zadanie.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto rycerz Mark. Wygląda na strasznie zaniepokojonego.");
		npc.setEntityClass("youngsoldiernpc");
		npc.setGender("M");
		npc.setPosition(82, 80);
		zone.add(npc);
	}
}
