/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.warszawa.house;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author zekkeq
 */
public class CollectorNPC implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Eltefia") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 3));
				nodes.add(new Node(12, 3));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting();
				addJob("Mam marzenie zostać projektantką pasów!");
				addHelp("Dziękuję za pomoc w zebraniu pasów! Chciałabym kiedyś zaprojektować swój własny pas!");
				addOffer("Nie mam jeszcze nic do zaoferowania, ale za niedługo otworzę swój własny sklepik!");
				addGoodbye("Do widzenia, do widzenia!");
			}
		};

		npc.setDescription("Oto Eltefia. Wygląda młodo oraz pięknie!");
		npc.setEntityClass("woman_004_npc");
		npc.setGender("F");
		npc.setPosition(4, 3);
		zone.add(npc);
	}
}
