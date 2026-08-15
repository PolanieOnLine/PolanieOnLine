/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.sewinghouse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author ZEKKEQ
 */
public class CollectorNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Anastazja") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(12, 15));
				nodes.add(new Node(12, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Chcę zostać projektantką mody. Teraz uczę się na rękawicach, bo dobrze pokazują różnicę między wyglądem, ochroną i swobodą ruchu.");
				addHelp("Jeśli znajdziesz ciekawe rękawice, pokaż mi je. Porównuję materiały i sposoby wykonania różnych ludów.");
				addOffer("Moja znajoma piętro niżej skupuje stare rękawice oraz skóry. Ja zajmuję się projektowaniem i badaniem wzorów.");
				addGoodbye("Do widzenia. Powodzenia w poszukiwaniu nowych wzorów!");
			}
		};

		npc.setDescription("Oto Anastazja, początkująca projektantka mody badająca sposoby wykonywania rękawic.");
		npc.setEntityClass("woman_009_npc");
		npc.setGender("F");
		npc.setPosition(7, 12);
		zone.add(npc);
	}
}
