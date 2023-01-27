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
package games.stendhal.server.maps.magic.theater;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Inside Magic Theater.
 */
public class MagicBarmaidNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildmagicbarmaid(zone);
	}

	private void buildmagicbarmaid(final StendhalRPZone zone) {
		final SpeakerNPC magicbarmaid = new SpeakerNPC("Trillium") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(13, 3));
				nodes.add(new Node(19, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Mam nadzieję, że podoba się Tobie nasz wspaniały teatr.");
				addJob("Sprzedaję większość pysznego jedzenia w Magic City.");
				addHelp("Jeżeli jesteś głodny to sprawdź tablicę, aby dowiedzieć się jakie sprzedajemy jedzenie i po jakiej cenie.");
				addOffer("Spójrz na tablicę, aby zobaczyć ceny.");
				addQuest("Nie potrzebuję twojej pomocy. Dziękuję.");
				addReply("lukrecja", "Biedny Baldemar ma alergię na lukrecję.");
				addGoodbye("Wspaniale miło było Cię spotkać. Wróć ponownie.");
			}
		};

		magicbarmaid.setDescription("Oto Trillium. Jest ona barmanką teatru w magicznym mieście. Oferuje napoje i żywności.");
		magicbarmaid.setEntityClass("woman_015_npc");
		magicbarmaid.setGender("F");
		magicbarmaid.setPosition(13, 3);
		zone.add(magicbarmaid);
	}
}
