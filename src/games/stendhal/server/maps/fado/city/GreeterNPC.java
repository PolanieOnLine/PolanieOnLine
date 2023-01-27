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
package games.stendhal.server.maps.fado.city;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the city greeter NPC.
 *
 * @author timothyb89
 */
public class GreeterNPC implements ZoneConfigurator {
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
		final SpeakerNPC greeterNPC = new SpeakerNPC("Xhiphin Zohos") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(39, 29));
				nodes.add(new Node(23, 29));
				nodes.add(new Node(23, 21));
				nodes.add(new Node(40, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Witam w Fado! Możesz się #dowiedzieć czegoś o Fado ode mnie.");
				addReply(Arrays.asList("dowiedzieć", "learn"),
				        "Fado strzeże mostu nad rzeką Or'ril, który jest traktem handlowym pomiędzy #Deniran i Ados. Toczy się tutaj aktywne życie towarzyskie ze względu na śluby i wyszukane jedzenie.");
				addReply("Deniran",
				        "Deniran jest perłą w koronie. Deniran jest centrum Faiumoni, posiada także wojsko, które jest gotowe pokonać wroga próbującego podbić Faiumoni.");
				addJob("Witam wszystkich nowo przybyłych do Fado. Mogę #zaoferować zwój jeżeli chciałbyś kiedyś tu wrócić.");
				addHelp("Możesz pójść do oberży, w której kupisz jedzenie i picie. Możesz także odwiedzać ludzi w domach lub odwiedzić kowala lub miejski hotel.");
				addGoodbye("Do widzenia.");
			}
		};

		greeterNPC.setDescription("Oto Xhiphin Zohos. Jest pomocnym obywatelem Fado.");
		greeterNPC.setOutfit(1, 6, 1, null, 0, null, 5, null, 0);
		greeterNPC.setGender("M");
		greeterNPC.setPosition(39, 29);
		zone.add(greeterNPC);
	}
}
