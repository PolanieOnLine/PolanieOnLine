/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Creates the NPCs and portals in Ados City.
 *
 * @author hendrik
 */
public class WeaponryTraderNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAlexander(zone);
	}

	private void buildAlexander(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Alexander") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(52, 34));
				nodes.add(new Node(56, 34));
                nodes.add(new Node(56, 48));
                nodes.add(new Node(26, 48));
                nodes.add(new Node(26, 42));
                nodes.add(new Node(10, 42));
                nodes.add(new Node(10, 29));
                nodes.add(new Node(27, 29));
                nodes.add(new Node(27, 14));
                nodes.add(new Node(44, 14));
                nodes.add(new Node(44, 32));
                nodes.add(new Node(52, 32));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. W czym mogę #'pomóc'?");
				addHelp("Byłbym wdzięczny, gdybym mógł pomóc tobie skupując twoje przedmioty. Pomoże to nam obu.");

				// this is a hint that one of the items Anna wants is a dress (goblin dress)
				addQuest("Nie mam dla Ciebie zadań.");
				addJob("Jestem handlarzem broni. Wolę sam pracować.");
				//addReply("offer", "Look at the blackboard to see my offers.");
				addGoodbye("Do widzenia. Wracaj szybko.");
			}
		};

		npc.setDescription("Oto Alexander. Zwykły handlarz brońmi, nikomu nie ufa...");
		npc.setEntityClass("weaponrytradernpc");
		npc.setGender("M");
		npc.setPosition(52, 34);
		npc.setDirection(Direction.RIGHT);
		zone.add(npc);
	}
}
