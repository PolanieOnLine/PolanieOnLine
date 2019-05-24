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
public class BlacksheepJoeNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildblacksheepjoe(zone);
	}

	private void buildblacksheepjoe(final StendhalRPZone zone) {
		final SpeakerNPC blacksheepjoe = new SpeakerNPC("Blacksheep Joe") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(13, 13));
				nodes.add(new Node(13, 9));
				setPath(new FixedPath(nodes, true));

			}

		@Override
		protected void createDialog() {
			addJob("Zaopatruje cały świat w kiełbasę serową.");
			addHelp("Wyrabiam tylko kiełbasę serową. Moi braci robią paróweczki i prasowanego  tuńczyka.");
			addOffer("Zobacz tablicę z tyłu, aby dowiedzieć się czego potrzebuję do zrobienia kiełbasy serowej. Powiedz #zrób jak się zdecydujesz.");
			addQuest("Nie potrzebuję teraz pomocy. Dziękuję.");
			addGoodbye("Dowidzenia. Poleć nas swoim znajomym.");

			// Blacksheep Joe creates you some cheese sausages
			// (uses sorted TreeMap instead of HashMap)
			final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
			requiredResources.put("truchło wampira", Integer.valueOf(1));
			requiredResources.put("truchło nietoperza", Integer.valueOf(1));
			requiredResources.put("udko", Integer.valueOf(1));
			requiredResources.put("czarna perła", Integer.valueOf(1));
			requiredResources.put("ser", Integer.valueOf(1));

			final ProducerBehaviour behaviour = new ProducerBehaviour("blacksheepjoe_make_cheese_sausage", Arrays.asList("make", "zrób"), "kiełbasa serowa",
			        requiredResources, 2 * 60);

			new ProducerAdder().addProducer(this, behaviour,
			        "Hej. Witam w Blacksheep Meat Market. Czy mogę zrobić dla Ciebie kiełbasę z serem?");
		}
	};

	blacksheepjoe.setEntityClass("blacksheepnpc");
	blacksheepjoe.setPosition(13, 13);
	blacksheepjoe.initHP(100);
	blacksheepjoe.setDescription("Widzisz Blacksheep Joe. Jest znawcą kiełbasy z serem. Czy próbowałeś jej już?");
	zone.add(blacksheepjoe);
		
	}
}
