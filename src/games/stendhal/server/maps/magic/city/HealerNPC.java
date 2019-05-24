/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.magic.city;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;

/**
 * Builds a Healer NPC for the magic city.
 *
 * @author kymara
 */
public class HealerNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Salva Mattori") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// walks along the aqueduct path, roughly
				nodes.add(new Node(5, 25));
				nodes.add(new Node(5, 51));
				nodes.add(new Node(18, 51));
				nodes.add(new Node(18, 78));
				nodes.add(new Node(20, 78));
				nodes.add(new Node(20, 109));
				// and back again
				nodes.add(new Node(20, 78));
				nodes.add(new Node(18, 78));
				nodes.add(new Node(18, 51));
				nodes.add(new Node(5, 51));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			        addGreeting("Pozdrawiam. W czym mogę #pomóc?");
				addJob("Praktykuję alchemię i mam zdolności do leczenia innych.");
				new HealerAdder().addHealer(this, 500);
				addReply(Arrays.asList("magical", "magiczną"), "My wszyscy mamy zdolności magiczne. Oczywiście różnego rodzaju. Moim ulubionym jest Sunlight Spell do utrzymywania trawy i kwiatków rosnących pod ziemią.");
				addHelp("Mam #magiczną moc uzdrowienia twoich dolegliwości. Powiedz tylko #ulecz.");
				addQuest("Niczego nie potrzebuję. Dziękuję.");
				addGoodbye("Powodzenia.");
			}
		};

		npc.setDescription("Oto cicha kobieta wyglądająca na życzliwą.");
		npc.setEntityClass("cloakedwomannpc");
		npc.setPosition(5, 25);
		npc.initHP(100);
		zone.add(npc);
	}
}
