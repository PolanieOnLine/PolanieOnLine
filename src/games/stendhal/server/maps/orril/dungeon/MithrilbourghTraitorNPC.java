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
package games.stendhal.server.maps.orril.dungeon;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configure Xavkas - mithrilbourgh traitor.
 *
 * @author kymara - modded by tigertoes
 */
public class MithrilbourghTraitorNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildXavkas(zone);
	}

	private void buildXavkas(final StendhalRPZone zone) {
		final SpeakerNPC Xavkas = new SpeakerNPC("Xavkas"){

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(101, 141));
				nodes.add(new Node(106,141));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Jestem niewinny, mówię ci.  Wyciągnij mnie stąd!");
				addJob("Jestem czarodziejem. Byłem kiedyś starszym w radzie Mithrilbourghtów.");
				addHelp("Nic nie mogę zrobić, aby ci pomóc.");
				addQuest("Nie wiem co możesz teraz dla mnie zrobić.  Wróć później.");
				addGoodbye("PNie zapomnij o mnie.");
			} //remaining behaviour defined in quest
		};

		Xavkas.setDescription("Xavkas czarodziej Mithrilbourgh, kiedyś starsz, a później zdrajca, a teraz więzień.  Kim on jest.");
		Xavkas.setEntityClass("mithrilforgernpc");
		Xavkas.setPosition(101, 141);
		Xavkas.initHP(100);
		zone.add(Xavkas);
	}
}
