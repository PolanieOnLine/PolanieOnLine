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
package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the flower seller in kirdneh.
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC sellernpc = new SpeakerNPC("Fleur") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Przyszedłeś tutaj #pohandlować?");
				addReply(ConversationPhrases.YES_MESSAGES, "Dobrze! Mogę sprzedać Tobie piękną czerwoną różę. Nie chodzi mi o rzadki orchideę. Tylko Róża Kwiaciarka wie, gdzie one rosną i nikt nie wie gdzie jest Róża Kwiaciarka!");
				addReply(ConversationPhrases.NO_MESSAGES, "Bardzo dobrze. Jeżeli będę mogła pomóc to daj znać.");
				addJob("Sprzedaję tutaj róże.");
				addHelp("Jeżeli będziesz potrzebował pieniędzy to tutaj w Kirdneh jest oddział banku Fado. Mieści się w małym budynku na północ od muzeum we wschodniej części miasta.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("róża", 50);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye("Dowidzenia i zapraszam ponownie!");
			}
		};

		sellernpc.setEntityClass("woman_001_npc");
		sellernpc.setPosition(64, 82);
		sellernpc.initHP(100);
			sellernpc.setDescription("Widzisz Fleur. Jej róże są dla młodych par.");
		zone.add(sellernpc);
	}
}
