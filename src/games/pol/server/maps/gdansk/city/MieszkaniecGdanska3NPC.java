/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.gdansk.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author KarajuSs
 */

public class MieszkaniecGdanska3NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGdanskMieszkancy(zone);
	}

	private void buildGdanskMieszkancy(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Farmer Mścisław") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(52, 88));
				nodes.add(new Node(52, 82));
				nodes.add(new Node(42, 88));
				nodes.add(new Node(42, 88));
				nodes.add(new Node(36, 88));
				nodes.add(new Node(36, 89));
				nodes.add(new Node(57, 89));
				nodes.add(new Node(57, 88));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj dzielny wojowniku na mojej farmie!");
				addJob("Prace.. praca... praca... Może znajdzie się coś dla Ciebie.");
				addHelp("Pomocy? Może znajdzie się pewne #'zadanie' dla Ciebie. Jak je wykonasz to wtedy możemy chwilę pogawędzić.");
				addOffer("Nie mam nic do zaoferowania. Posiadam jedynie tę farmę... jak zechcesz możesz wziąć parę warzyw dla siebie.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto farmer Mścisław. Wygląda na miłego dla nowo poznanych osób.");
		npc.setEntityClass("hoeingmannpc");
		npc.setGender("M");
		npc.setPosition(52, 88);
		zone.add(npc);
	}
}
