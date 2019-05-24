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
package games.stendhal.server.maps.kalavan.castle;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the princess in Kalavan castle.
 *
 * @author kymara
 */
public class PrincessNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC princessNPC = new SpeakerNPC("Princess Ylflia") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 21));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(14, 41));
				nodes.add(new Node(14, 48));
				nodes.add(new Node(18, 48));
				nodes.add(new Node(19, 48));
				nodes.add(new Node(19, 41));
				nodes.add(new Node(22, 41));
				nodes.add(new Node(20, 41));
				nodes.add(new Node(20, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Jak się masz?");
				addReply(Arrays.asList("dobrze", "wspaniale", "good", "fine"), "Dobrze! W czym mogę pomóc?");
				addReply(Arrays.asList("źle", "bad"), "O rany ... W czym mogę pomóc?");
				addReply(Arrays.asList("wspaniale", "well"), "Cudownie! W czym mogę pomóc?");
				addJob("Jestem księżniczką tego królestwa. Aby zostać obywatelem tego królestwa porozmawiaj w mieście z Barrett Holmesem. Może sprzeda Ci dom. Zanim pójdziesz mógłbyś mi wyświadczyć przysługę ( #favour )...");
				addHelp("Uważaj na szalonych naukowców. Mój ojciec uwolnił ich, aby wykonali pewną pracę w podziemiach i obawiam się, że pewne rzeczy, wymknęły się im spod kontroli...");
				addOffer("Przepraszam, ale nie mam Ci nic do zaoferowania. Mógłbyś mi wyświadczyć pewną przysługę ( #favour ), chociaż...");
				addGoodbye("Dowidzenia i powodzenia.");
			}
		};

		princessNPC.setEntityClass("princess2npc");
		princessNPC.setPosition(19, 21);
		princessNPC.initHP(100);
		princessNPC.setDescription("Widzisz Princess Ylflia. Mimo, że jest księżniczką, wydaje się być bardzo przyjazną i pomocną.");
		zone.add(princessNPC);
	}
}
