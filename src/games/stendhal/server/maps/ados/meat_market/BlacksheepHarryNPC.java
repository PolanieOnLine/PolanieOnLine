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
package games.stendhal.server.maps.ados.meat_market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
			addGoodbye("Dowidzenia. Poleć nas swoim znajomym.");

			// Blacksheep Harry makes you some tuna if you bring him a mackerel and a perch
			// (uses sorted TreeMap instead of HashMap)
			final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
			requiredResources.put("makrela", Integer.valueOf(1));
			requiredResources.put("okoń", Integer.valueOf(1));
			requiredResources.put("kolorowe kulki", Integer.valueOf(2));

			final ProducerBehaviour behaviour = new ProducerBehaviour("blacksheepharry_make_tuna",  Arrays.asList("make", "zrób"), "prasowany tuńczyk",
			        requiredResources, 2 * 60);

			new ProducerAdder().addProducer(this, behaviour,
			        "Witam w Blacksheep Meat Market. Czy mogę zrobić dla Ciebie prasowanego tuńczyka?");
		}
	};

	blacksheepharry.setEntityClass("blacksheepnpc");
	blacksheepharry.setDirection(Direction.DOWN);
	blacksheepharry.setPosition(5, 2);
	blacksheepharry.initHP(100);
	blacksheepharry.setDescription("oto Blacksheep Harry. Jest prasowania ryb.");
	zone.add(blacksheepharry);
		
	}
}
