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
package games.stendhal.server.maps.kalavan.citygardens;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.HashMap;
import java.util.Map;


/**
 * Builds an ice cream seller npc.
 *
 * @author kymara
 */
public class IceCreamSellerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	//
	// IceCreamSellerNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Sam") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Czy mogę #zaoferować Tobie porcję lodów?");
				addJob("Sprzedaje pyszne lody.");
				addHelp("Mogę #zaoferować odświeżającą porcję lodów.");
				addQuest("Prowadzę proste życie. Nie potrzebuję wiele do szczęścia.");
			 
			 add(ConversationStates.ATTENDING, 
					ConversationPhrases.YES_MESSAGES, 
					null, 
					ConversationStates.ATTENDING, 
					null, 
					new ChatAction() {
						@Override
						public void fire(final Player player,final Sentence sentence, final EventRaiser npc) {
							((SpeakerNPC) npc.getEntity()).getEngine().step(player, "buy ice cream");
							}
						} );

				final Map<String, Integer> offers = new HashMap<String, Integer>();
				offers.put("lody", 30);
				new SellerAdder().addSeller(this, new SellerBehaviour(offers));
				addGoodbye("Dowidzenia. Ciesz się dniem!");
				
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("icecreamsellernpc");
		npc.setPosition(73, 54);
		npc.initHP(100);
		npc.setDescription("Sama praca polega na uszczęśliwieniu dzieci. Sprzedaje lody. Jupi ja! ");
		zone.add(npc);
	}
}
