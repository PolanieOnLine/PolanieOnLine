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
package games.stendhal.server.maps.krakow.woodcutter;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.FixedPath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the reaper in hell.
 * Remaining behaviour will be in games.stendhal.server.maps.quests.SolveRiddles
 * @author kymara
 */
public class WoodcutterNPC implements ZoneConfigurator {
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
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Drwal") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 4));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(10, 5));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(4, 10));
				nodes.add(new Node(4, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, jeżeli chcesz ścinać drzewa to musisz przystąpić do #egzaminu");
				addReply(Arrays.asList("exam", "egzamin", "egzaminu"), "Egzamin składa się z trzech pytań na które musisz odpowiedzieć prawidłowo. Jedna błędna odpowiedź i zostanie niezaliczony. Do egzaminu możesz podchodzić, aż do skutku. Za zdanie go zdobędziesz umiejętność ścinania drzew. Powodzenia.");
				// Remaining behaviour is in games.stendhal.server.maps.quests.SolveRiddles
				addReply(Arrays.asList("test"), "Zdałeś już test. Idź teraz szukać drzew oznaczonych do wyrębu.");
				addJob("Zajmuje się wyrębem lasu, możesz u mnie przystąpić do #egzaminu na drwala.");
				addHelp("Organizuje egzaminy na drwala. Czy chcesz przystąpić do egzaminu, aby otrzymać pozwolenie do wyrębu drzew?");
				addGoodbye("Do zobaczenia i uważaj na spadające drzewa podczas wyrębu...");
			}
		};

		npc.setEntityClass("woodcutternpc");
		npc.setPosition(4, 4);
		npc.initHP(100);
		zone.add(npc);
	}
}
