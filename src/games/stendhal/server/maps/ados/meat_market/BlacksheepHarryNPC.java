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
package games.stendhal.server.maps.ados.meat_market;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Inside Ados meat market.
 */
public class BlacksheepHarryNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildblacksheepharry(zone);
	}

	private void buildblacksheepharry(final StendhalRPZone zone) {
		final SpeakerNPC blacksheepharry = new SpeakerNPC("Blacksheep Harry") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 2));
				nodes.add(new Node(10, 2));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Zaopatruję cały świat w prasowanego tuńczyka.");
				addHelp("Wyrabiam tylko prasowanego tuńczyka. Moi braci robią próweczki i kiełbasę serową.");
				addOffer("Daj mi jakąś makrele, a zrobię dla Ciebie prasowanego tuńczyka. Powiedz tylko #zrób.");
				addQuest("Nie sądzę, abym powinien prosić Ciebie o pomoc.");
				addGoodbye("Do widzenia. Poleć nas swoim znajomym.");
			}
		};

		blacksheepharry.setDescription("Oto Blacksheep Harry. Jest znany z prasowania ryb.");
		blacksheepharry.setEntityClass("blacksheepnpc");
		blacksheepharry.setGender("M");
		blacksheepharry.setDirection(Direction.DOWN);
		blacksheepharry.setPosition(5, 2);
		zone.add(blacksheepharry);
	}
}
