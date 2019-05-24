/***************************************************************************
 *                     (C) Copyright 2017 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.wall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Soldiers on the wall
 *
 * @author snowflake
 * @author hendrik
 */
public class WallSoldier2NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAdosWallSoldier(zone);
	}

	/**
	 * Creatures a soldier on the city wall
	 *
	 * @param zone StendhalRPZone
	 */
	private void buildAdosWallSoldier(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Grekus") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(76, 20));
				path.add(new Node(76, 40));
				path.add(new Node(79, 40));
				path.add(new Node(79, 20));
				setPath(new FixedPath(path, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody przyjacielu! Baw się dobrze w naszym mieście!");
				addJob("Jestem żołnierzem i moja służba polega na strzeżeniu oto tych właśnie murów miasta.");
				addHelp("Jeżeli potrzebujesz wskazówek jak się odnaleźć w naszym mieście to spytaj się Juliusa. Strzeże głównej bramy miasta.");
				// addQuest("Ask Vicendus, he is always up to something.");
				addGoodbye("Życzę dobrego dnia.");
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(76, 20);
		npc.initHP(100);
		npc.setDescription("Oto Grekus, żołnierz, który strzeże mury miasta Ados.");
		zone.add(npc);
	}
}
