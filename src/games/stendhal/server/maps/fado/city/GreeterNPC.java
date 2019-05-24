/* $Id$ */
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
package games.stendhal.server.maps.fado.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the city greeter NPC.
 *
 * @author timothyb89
 */
public class GreeterNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	//
	// ZoneConfigurator
	//

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

	//
	// OL0_GreeterNPC
	//

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
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("fadoscrolls")));
				addGoodbye("Dowidzenia.");
			}
		};

		greeterNPC.setOutfit(new Outfit(0, 5, 2, 6, 2));
		greeterNPC.setPosition(39, 29);
		greeterNPC.initHP(1000);
		greeterNPC.setDescription("Widzisz Xhiphin Zohos. Jest pomocnym obywatelem Fado.");
		zone.add(greeterNPC);
	}
}
