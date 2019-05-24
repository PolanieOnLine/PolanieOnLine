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
package games.stendhal.server.maps.athor.ship;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.maps.athor.ship.AthorFerry.Status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Factory for cargo worker on Athor Ferry. */

public class CookNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Laura") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
		        // to the oven 
				nodes.add(new Node(27,28));
				// to the table 
				nodes.add(new Node(27,31));
				// to the dining room
				nodes.add(new Node(18,31));
				// to the barrel
				nodes.add(new Node(28,31));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Ahoj! Witam w kambuzie!");
				addJob("Prowadzę kambuz na statku. #Oferuję dobre jedzenie dla pasażerów i alkohol dla załogi.");
				addHelp("Załoga cały dzień pije piwo i grog. Jeżeli chcesz lepsze drinki to idź do koktajl baru na plaży w Athor.");

				final Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("sok z chmielu", 10);
				offerings.put("napój z winogron", 15);
				// more expensive than in normal taverns
				offerings.put("szynka", 100);
				offerings.put("tarta", 150);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));

				addGoodbye();

			}};
			new AthorFerry.FerryListener() {
				@Override
				public void onNewFerryState(final Status status) {
					switch (status) {
					case ANCHORED_AT_MAINLAND:
					case ANCHORED_AT_ISLAND:
						npc.say("UWAGA: Dopłynęliśmy!");
						break;
					default:
						npc.say("UWAGA: Wypłynęliśmy!");
						break;
					}
				}
			};

			npc.setPosition(27, 28);
			npc.setEntityClass("tavernbarmaidnpc");
			npc.setDescription ("Laura prowadzi kambuz na statku. Porozmawiaj z nią jeśli zgłodniejesz lub będziesz spragniony.");
			zone.add(npc);	

	}
}
