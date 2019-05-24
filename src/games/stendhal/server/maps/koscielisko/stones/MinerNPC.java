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
// Based on ../games/stendhal/server/maps/ados/fishermans_hut/FishermanNPC.java
package games.stendhal.server.maps.koscielisko.stones;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Miner (Inside / Level 0).
 *
 * @author dine
 */
public class MinerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMiner(zone);
	}

	private void buildMiner(final StendhalRPZone zone) {
		final SpeakerNPC minerman = new SpeakerNPC("Bercik") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// from left
				nodes.add(new Node(18, 3));
				// to right
				nodes.add(new Node(19, 3));
				// to left
				nodes.add(new Node(20, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj! Co cię do mnie sprowadza?");
				addJob("Jestem sztygarem. Ludzie przychodzą tutaj, aby zdać #egzamin górniczy.");
				addHelp("Jeżeli będziesz szukać pod ziemią to znajdziesz kilka dobrych kopalni.");

			}
		};

		minerman.setEntityClass("fishermannpc");
		minerman.setDirection(Direction.DOWN);
		minerman.setPosition(19, 3);
		minerman.initHP(100);
		zone.add(minerman);
	}
}
