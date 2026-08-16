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
package games.stendhal.server.maps.zakopane.gorasmoka;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the NPC who deals in magiczny scroll.
 * Other behaviour defined in maps/quests/Labirynt.java
 *
 * @author Legolas
 */
public class OzoNPC implements ZoneConfigurator {
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
		final SpeakerNPC ozoNPC = new SpeakerNPC("Ozo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 49));
				nodes.add(new Node(33, 54));
				nodes.add(new Node(36, 54));
				nodes.add(new Node(36, 49));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Zajmuję się starymi znakami przejścia. Juhas sprzedaje ich stabilniejsze odmiany. Ja zachowuję wzory, których działania nie da się tak łatwo przewidzieć.");
				addHelp("Jeśli nigdy nie wróciłeś z pustyni po użyciu biletu Juhasa, zacznij od niego. Nie powierzam swoich przejść komuś, kto nie zna nawet bezpieczniejszej odmiany tej magii.");
				addQuest("Nie potrzebuję posłańca ani zbieracza. Jeśli chcesz poznać moje przejścia, najpierw udowodnij, że potrafisz z nich wracać.");
				addOffer("Mam #'magiczny bilet', ale nie sprzedaję go każdemu. Najpierw muszę wiedzieć, czy masz doświadczenie z biletem Juhasa.");
				addGoodbye("Do widzenia.");
			}
		};

		ozoNPC.setDescription("Oto Ozo, badacz starych znaków przejścia, który z dużą ostrożnością wybiera ludzi dopuszczanych do swojej magii.");
		ozoNPC.setEntityClass("scarletarmynpc");
		ozoNPC.setGender("M");
		ozoNPC.setPosition(33, 49);
		zone.add(ozoNPC);
	}
}
