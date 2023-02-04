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
package games.stendhal.server.maps.fado.bakery;

import java.util.Arrays;
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
 * Builds the bakery baker NPC.
 *
 * @author timothyb89/kymara
 */
public class BakerNPC implements ZoneConfigurator {
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
		final SpeakerNPC baker = new SpeakerNPC("Linzo") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(3, 10));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(5, 3));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(15, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem tutejszym piekarzem. Moją specjalnością są placki z rybą i porem. Jestem dumny z mojej punktualności w pieczeniu.");
				addReply(Arrays.asList("dorsz", "makrela"),
				        "Dorsza możesz złapać w Ados, a Makrele w morzu. Możliwe, że ryby dostaniesz też ze zwierząt, które się nimi żywią.");
				addReply("mąka", "Dostajemy zapasy mąki z Semos");
				addReply("por", "Mamy dość szczęścia, że mamy pory, które rosną na działkach tutaj w Fado.");
				addHelp("Poproś mnie, abym upiekł dla Ciebie tarte z rybą i porem. Powiedz tylko #upiecz. Nie są zapychające jak tarty z mięsem i dlatego możesz jeść je trochę szybciej.");
				addGoodbye();
			}
		};

		baker.setEntityClass("bakernpc");
		baker.setDescription("Oto Linzo. Jest lokalnym piekarzem w Fado, specjalizuje się w wypieku ciasta z ryb i porów.");
		baker.setGender("M");
		baker.setDirection(Direction.DOWN);
		baker.setPosition(15, 3);
		zone.add(baker);
	}
}
