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
package games.stendhal.server.maps.ados.library;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class WriterNPC implements ZoneConfigurator {
	final static String npc_name = "Marie-Henri";

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC(npc_name) {
			@Override
			protected void createPath() {
				// setPath(null);
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 3));
				nodes.add(new Node(19, 8));
				nodes.add(new Node(18, 8));
				nodes.add(new Node(18, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Bonjour!");
				addJob("Jestem słynnym francuskim pisarzem. Spędziłem czas na pisaniu nowel i czytaniu innych zagorzałych myślicieli w bibliotece.");
				addOffer("Niczego nie sprzedaje.");
				addHelp("Jeżeli chcesz być tak mądry jak ja to skontaktuję Ciebie z #Wikipedianem.");
				addReply(Arrays.asList("wikipedian", "wikipedianem"), "Mam na myśli tego biblotekarza z siwą brodą, możesz spytać go o tematy, o których chcesz więcej wiedzieć.");
				addGoodbye("Au revoir!");
			}
		};

		npc.setDescription("Oto " + npc_name + ", znany francuski pisarz.");
		npc.setEntityClass("writernpc");
		npc.setGender("M");
		npc.setPosition(19, 3);
		zone.add(npc);
	}
}
