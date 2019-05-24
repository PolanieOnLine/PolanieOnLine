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
package games.stendhal.server.maps.ados.wall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ados Wall North population.
 *
 * @author hendrik
 * @author kymara
 */
public class SoldierNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAdosGreetingSoldier(zone);
	}

	/**
	 * Creatures a soldier telling people a story, why Ados is so empty.
	 *
	 * @param zone StendhalRPZone
	 */
	private void buildAdosGreetingSoldier(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Julius") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(84, 109));
				path.add(new Node(84, 116));
				setPath(new FixedPath(path, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w mieście Ados!");
				addReply("map", "Legenda\n"
					+ "1 Bank,   2 Goldsmith,   3 Piekarnia,   4 Nawiedzony dom,\n"
					+ "5 Zamek,   6 Dom Feliny,   7 Baraki \n"
					+ "8 Bar,   9 Zakład krawiecki, ida \n"
					+ "10 Chatki Meat and Fish,   11 Ratusz,   12 Biblioteka",
					new ExamineChatAction("map-ados-city.png", "Miasto Ados", "Mapa miasta Ados"));
				addJob("Chronię miasto Ados przed atakami i #pomagam turystom.");
				addHelp("Jeżeli potrzebujesz #mapy Ados to zapytaj mnie.");
				addGoodbye("Mam nadzieje, że dobrze bawisz się w Ados.");
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(84, 109);
		npc.initHP(100);
		npc.setDescription("Oto Julius, żołnierz, który strzeże wejścia do miasta Ados.");
		zone.add(npc);
	}
}
