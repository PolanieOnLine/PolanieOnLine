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
package games.stendhal.server.maps.ados.fishermans_hut;

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
 * Ados Fisherman (Inside / Level 0).
 *
 * @author dine
 */
public class FishermanNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFisherman(zone);
	}

	private void buildFisherman(final StendhalRPZone zone) {
		final SpeakerNPC fisherman = new SpeakerNPC("Pequod") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 3));
				nodes.add(new Node(12, 3));
				nodes.add(new Node(3, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem rybakiem, ale wyrabiam też #olejek z tranu dorsza. Przydaje się do smarowania maszyn, gdy zaczynają się zacinać.");
				addHelp("Jeśli potrzebujesz #olejku, przynieś mi dwa dorsze na każdą buteleczkę. Powiedz na przykład #'zrób olejek' albo #'przygotuj olejek'. Gotowe zlecenie odbierzesz słowem #przypomnij.");
				addOffer("Wyrabiam #olejek z tranu dorsza. Jedna buteleczka kosztuje dwa dorsze i wymaga trochę czasu.");
				addGoodbye("Do widzenia.");
				addReply(Arrays.asList("oil", "olejek"),
						"Tak, robię olejek potrzebny między innymi do smarowania maszyn. Na jedną buteleczkę potrzebuję dwóch dorszy. Powiedz #'zrób olejek' lub #'przygotuj olejek'. Gdy wrócisz po gotowy produkt, powiedz #przypomnij.");
				addReply(Arrays.asList("can of oil", "olejku"),
						"Do jednej buteleczki olejku potrzebuję dwóch dorszy. Powiedz #'zrób olejek' albo #'przygotuj olejek'. Po zakończeniu pracy przypomnij mi o zleceniu słowem #przypomnij.");
			}
		};

		fisherman.setDescription("Oto Pequod, zapominalski stary rybak, który potrafi także wyrabiać olejek z tranu dorsza.");
		fisherman.setEntityClass("fishermannpc");
		fisherman.setGender("M");
		fisherman.setDirection(Direction.DOWN);
		fisherman.setPosition(3, 3);
		zone.add(fisherman);
	}
}
