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
package games.stendhal.server.maps.ados.sewingroom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.CloneManager;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Ados City, house with a woman who makes sails for the ships
 */
public class SeamstressNPC implements ZoneConfigurator {
	// clone to be used in twilight zone
	private static SpeakerNPC clone;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSeamstress(zone);
	}

	private void buildSeamstress(final StendhalRPZone zone) {
		final SpeakerNPC seamstress = new SpeakerNPC("Ida") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 7));
				nodes.add(new Node(7, 14));
				nodes.add(new Node(12, 14));
				nodes.add(new Node(12, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addJob("Jestem szwaczką. Produkuję żagle dla statków takich jak prom do Athor.");
				addHelp("Jeżeli chcesz się dostać promem na wyspę Athor to wyjdź z miasta i kieruj się na południe, a tam znajdziesz przystań.");
				addOffer("Skupuję płaszcze, ponieważ nie mamy materiałów na żagle. Za lepszy materiał płacę więcej. W moim notatniku na stole znajduje się cennik.");
				addGoodbye("Do widzenia i dziękuję za wstąpienie.");
			}
		};

		seamstress.setDescription("Oto Ida, jest znaną szwaczką w przemyśle stoczniowym. Ale może też tobie pomóc.");
		seamstress.setEntityClass("woman_002_npc");
		seamstress.setGender("F");
		seamstress.setPosition(7, 7);
		zone.add(seamstress);

		// initialize clone to be placed in twilight zone
		clone = CloneManager.get().clone(seamstress, "twilight_ida");
	}

	/**
	 * Retrieves the cloned instance.
	 *
	 * @return
	 * 		SpeakerNPC
	 */
	public static SpeakerNPC getClone() {
		return clone;
	}
}
