/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class ShopAssistantNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Lena") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(26,9));
                nodes.add(new Node(26,6));
                nodes.add(new Node(28,6));
                nodes.add(new Node(28,2));
                nodes.add(new Node(28,5));
                nodes.add(new Node(22,5));
                nodes.add(new Node(22,4));
                nodes.add(new Node(22,7));
                nodes.add(new Node(26,7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Pomagam Ernestowi przygotowywać świeże #pieczywo dla mieszkańców naszego miasta.");
				addHelp("Chleb jest bardzo dobry, zwłaszcza dla takiego śmiałka jak ty, któremu"
						+ " już niedobrze, gdy widzi surowe mięsiwo. Mój szef Ernest,"
						+ " robi najlepsze kanapki w okolicy!");
				addOffer("Mogę zaoferować #'upieczenie chleba' specjalnie dla ciebie. "
						+ "Akurat mam chwilę wolnego czasu.");
				addReply(Arrays.asList("bread", "upieczenie", "chleba", "pieczywo"),
						"Powiedz mi tylko #'upiecz <ilość> chleb', a zajmę "
						+ "się przygotowywaniem dla ciebie pieczywa.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Lena. Wygląda na bardzo pracowitą osobę oraz na związaną z jej zawodem.");
		npc.setEntityClass("housewifenpc");
		npc.setGender("F");
		npc.setPosition(26, 9);
		zone.add(npc);
	}
}
