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

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.maps.athor.ship.AthorFerry.Status;

import java.util.Arrays;
import java.util.Map;

/** Factory for cargo worker on Athor Ferry. */

public class CargoWorkerNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Klaas") {

			/*@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the bucket
				nodes.add(new Node(24,42));
				// along the corridor
				nodes.add(new Node(24,35));
				// walk between barrels and boxes
				nodes.add(new Node(17,35));
				// to the stairs
				nodes.add(new Node(17,39));
				// walk between the barrels 
				nodes.add(new Node(22,39));
				// towards the bow
				nodes.add(new Node(22,42));
				setPath(new FixedPath(nodes, true));
			}*/

			@Override
			public void createDialog() {
				addGreeting("Ahoj! Miło Cię widzieć w ładowni!");
				addJob("Opiekuje się ładunkiem. Moja praca byłaby łatwiejsza gdyby nie było #szczurów.");
				addHelp("Mógłbyś zarobić jeżeli #zaoferowałbyś mi coś do wytrucia tych #szczurów.");
				addReply(Arrays.asList("szczur", "szczury", "szczurów"),
		        "Te szczury są wszędzie. Ciekaw jestem skąd one się biorą nawet nie zdążę ich powybijać tak szybko się pojawiają.");

				new BuyerAdder().addBuyer(this, 
						new BuyerBehaviour(SingletonRepository.getShopList().get("buypoisons")), true);

				addGoodbye("Proszę zabij kilka szczurów po drodze!");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

			new AthorFerry.FerryListener() {

			
				@Override
				public void onNewFerryState(final Status status) {
					switch (status) {
					case ANCHORED_AT_MAINLAND:
					case ANCHORED_AT_ISLAND:
						npc.say("UWAGA: Dopłyneliśmy!");
						break;

					default:
						npc.say("UWAGA: Wypływamy!");
						break;
					}
				}
			};

			npc.setPosition(25, 38);
			npc.setEntityClass("seller2npc");
			npc.setDescription ("Oto Klaas, który zajmuje się ładunkiem. Nie cierpi szczurów!");
			npc.setDirection(Direction.DOWN);
			zone.add(npc);
	}
}
