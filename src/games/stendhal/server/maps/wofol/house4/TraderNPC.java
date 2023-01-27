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
package games.stendhal.server.maps.wofol.house4;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Inside the Kobold City, interior called house4
 */
public class TraderNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTrader(zone);
	}

	private void buildTrader(final StendhalRPZone zone) {
		final SpeakerNPC trader = new SpeakerNPC("Wrvil") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 4));
				nodes.add(new Node(4, 9));
				nodes.add(new Node(12, 9));
				nodes.add(new Node(12, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witajże w mieście Wofol władanym przez Koboldów. Mam nadzieję, iż w pokoju przybywasz.");
				addJob("Zajmuję się #handlem. Sprzedaję i skupuję od koboldów i od każdego, kto tutaj zawita. Jam jeden z nielicznych Koboldów, który mówi w waszym języku.");
				addHelp("Zajmuję się #handlem każdego rodzaju sprzętu.");
				addQuest("W tej sprawie udaj się raczej do Alraka - górskiego krasnala, który mieszka z Koboldami. Być może znajdzie Ci niejedno zadanie.");
				addOffer("Spójrz na tę tablicę na ścianie, to przeczytasz co aktualnie skupuję i sprzedaję.");
				addGoodbye("Żegnaj! Ale nie waż się atakować moich przyjaciół Koboldów.");
			}
		};

		trader.setDescription("Oto Wrvil. On może wyposażyć i może uczynić cię bogatym w tym samym czasie.");
		trader.setEntityClass("koboldnpc");
		trader.setGender("M");
		trader.setPosition(4, 4);
		zone.add(trader);
	}
}
