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
package games.stendhal.server.maps.krakow.church;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.FreeHealerAdder;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class StKingaNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("St Kinga") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 4));
				nodes.add(new Node(37, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, w czym mogę #'pomóc'?");
				addJob("Potrafię wyleczyć najgroźniejsze rany u rycerzy i wojoników.");
				addHelp("Mogę Cię #'uleczyć'.");
				addOffer("Mogłabym Ciebie #'uleczyć' nie pobierając za to opłaty.");
				new FreeHealerAdder().addHealer(this, 0);
				addGoodbye("Niech Bóg Cię prowadzi!");
			}
		};

		npc.setDescription("Oto St Kinga. Jest mnichem i zajmuje się leczeniem ran u dzielnych wojowników.");
		npc.setEntityClass("npckinga");
		npc.setPosition(37, 4);
		npc.initHP(100);
		zone.add(npc);
	}
}