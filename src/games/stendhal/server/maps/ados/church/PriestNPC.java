/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Priest to make holy water for An Old Man's Wish quest.
 */
public class PriestNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC priest = new SpeakerNPC("Priest Calenus");
		priest.setEntityClass("priest2npc");

		priest.addGreeting("Witaj moje dziecko.");
		priest.addGoodbye("Idź w pokoju.");
		priest.addJob("Jestem zarządcą tego świętego domu.");
		priest.addHelp("Jedynym prawdziwym szczęściem jest wewnętrzny spokój.");
		priest.addOffer("Znajdź wewnętrzny spokój. Tylko wtedy zrozumiesz wartość życia.");

		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(16, 4));
		nodes.add(new Node(24, 4));
		priest.setPathAndPosition(new FixedPath(nodes, true));
		priest.setCollisionAction(CollisionAction.STOP);

		zone.add(priest);
	}
}