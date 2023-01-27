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
package games.stendhal.server.maps.fado.forest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Fado forest NPC - beekeeper.
 *
 * @author kymara
 */
public class BeeKeeperNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Aldrin") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(44, 76));
				nodes.add(new Node(53, 76));
				nodes.add(new Node(53, 77));
				nodes.add(new Node(56, 77));
				nodes.add(new Node(56, 78));
				nodes.add(new Node(57, 78));
				nodes.add(new Node(57, 79));
                nodes.add(new Node(58, 79));
                nodes.add(new Node(58, 86));
                nodes.add(new Node(43, 86));
                nodes.add(new Node(43, 87));
                nodes.add(new Node(59, 87));
                nodes.add(new Node(59, 80));
                nodes.add(new Node(58, 80));
                nodes.add(new Node(58, 79));
				nodes.add(new Node(57, 79));
				nodes.add(new Node(57, 78));
				nodes.add(new Node(56, 78));
				nodes.add(new Node(56, 77));
				nodes.add(new Node(44, 77));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć.");
				addJob("Jestem właścicielem pszczół. Domyślam się, że widziałeś moje ule.");
				addQuest("Nie sądzę, abym miał dla Ciebie jakieś zadanie do zrobienia. Trzeba samemu pracować z pszczołami. Naprawdę.");
				addHelp("Pszczoły produkują miód i wosk. Mogę trochę Ci sprzedać o ile chcesz. Miód czy wosk to nie pszczoły!");
				addOffer("Sprzedaję słodki miód i wosk pszczeli, który zbieram osobiście.");
				addGoodbye("Do widzenia, bądź ostrożny i uważaj na ule!");
			}
		};

		npc.setDescription("Oto Aldrin. Dba o przczoły, które latają wokół niego...");
		npc.setEntityClass("beekeepernpc");
		npc.setGender("M");
		npc.setPosition(44, 76);
		zone.add(npc);
	}
}
