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
public class BlacksheepBobNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildblacksheepbob(zone);
	}

	private void buildblacksheepbob(final StendhalRPZone zone) {
		final SpeakerNPC blacksheepbob = new SpeakerNPC("Blacksheep Bob") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 13));
				nodes.add(new Node(2, 9));
				setPath(new FixedPath(nodes, true));

			}

		@Override
		protected void createDialog() {
			addJob("Jestem dumny mówiąc, że wyrabiam najlepsze paróweczki.");
			addHelp("Wyrabiam tylko paróweczki. Moi bracia robią prasowanego tuńczyka i kiełbasę serową.");
			addOffer("Zobacz tablicę, aby dowiedzieć się czego potrzebuję do zrobienia paróweczek. Powiedz #zrób jak się zdecydujesz.");
			addQuest("Nie potrzebuję pomocy.");
			addGoodbye("Dowidzenia. Poleć nas swoim znajomym.");

			// Blacksheep Bob makes you sausages if you supply his ingredients
			// (uses sorted TreeMap instead of HashMap)
			final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
			requiredResources.put("truchło wampira", Integer.valueOf(1));
			requiredResources.put("truchło nietoperza", Integer.valueOf(1));
			requiredResources.put("mięso", Integer.valueOf(1));
			requiredResources.put("napój z winogron", Integer.valueOf(2));
			
			final ProducerBehaviour behaviour = new ProducerBehaviour("blacksheepbob_make_sausage", Arrays.asList("make", "zrób"), "paróweczka",
			        requiredResources, 2 * 60);

			new ProducerAdder().addProducer(this, behaviour,
			        "Hej. Witam w Blacksheep Meat Market. Czy mogę zrobić dla Ciebie paróweczki?");
		}
	};

	blacksheepbob.setEntityClass("blacksheepnpc");
	blacksheepbob.setPosition(2, 13);
	blacksheepbob.initHP(100);
	blacksheepbob.setDescription("Oto Blacksheep Bob. jest popularny z robienia paróweczek.");
	zone.add(blacksheepbob);
		
	}
}
