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
package games.stendhal.server.maps.ados.rosshouse;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>Creates a normal version of Susi in the ross house.
 */
public class LittleGirlNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createGirlNPC(zone);
	}

	public void createGirlNPC(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Susi") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 7));
				nodes.add(new Node(5, 7));
				nodes.add(new Node(5, 3));
				nodes.add(new Node(5, 8));
				nodes.add(new Node(10, 8));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(12, 12));
				nodes.add(new Node(9, 12));
				nodes.add(new Node(9, 11));
				nodes.add(new Node(7, 11));
				nodes.add(new Node(7, 7));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				// TODO: Add different greetings depending on whether Susi's is a friend of the player or not
				addGreeting("Cześć. Tata musiał znowu zostawić drzwi otwarte. Zawsze to robi.");
				addJob("Jestem małą dziewczynką.");
				addGoodbye("Miłej zabawy!");

				addQuest("Może kiedyś spotkamy się na #Mine #Town #Revival #Weeks.");

				// Revival Weeks
				add(
					ConversationStates.ATTENDING,
					Arrays.asList("Semos", "Mine", "Town", "Revival", "Weeks"),
					ConversationStates.ATTENDING,
					"Podczas Revival Weeks pod koniec października świętujemy stare i prawie martwe Mine Town na północ od miasta Semos.",
					null);
				
				// help
				addHelp("Miłej zabawy.");
			}
		};

		npc.setOutfit(new Outfit(0, 4, 15, 32, 14));
		npc.setPosition(3, 7);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setDescription("Oto Susi. Słyszałeś/aś kiedykolwiek opowieść o jej zaginięciu?");
		zone.add(npc);
	}

}
