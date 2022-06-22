/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.tatry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class IceElementalNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Żywioł lodu") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 4));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(10, 5));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(4, 10));
				nodes.add(new Node(4, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addOffer("Nie mam nic do zaoferowania, ale możesz sprawdzić czy nie mam czasem specjalnego #zadania dla ciebie.");
				addHelp("Mogę mieć #zadanie dla ciebie, które będzie polegało na zdobyciu kilku odłamków żywiołu od mych złych braci i sióstr.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto żywioł lodu. Wygląda na przyjazną wobec obcych.");
		npc.setEntityClass("elementalicenpc");
		npc.setGender("F");
		npc.setPosition(4, 4);
		zone.add(npc);
	}
}
