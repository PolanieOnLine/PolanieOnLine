/* $Id: JuhasNPC.java,v 1.8 2011/03/09 00:48:01 Legolas Exp $ */
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
package games.stendhal.server.maps.semos.tavern;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Ados MithrilForger (Inside / Level 0).
 *
 * @author kymara
 */
public class JuhasNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildJuhas(zone);
	}

	private void buildJuhas(final StendhalRPZone zone) {
		final SpeakerNPC juhas = new SpeakerNPC("Juhas") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam.");
				addJob("Sprzedaję #'magiczne zwoje'. Zapytaj mnie o #ofertę.");
				addHelp("Sprzedaję #zwoje, które mogą uratować Tobie życie.");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("juhas")));

				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Nie mam dla Ciebie zadania. Jedynie mam do zaoferowania #'magiczne zwoje' takie jak #'zwój tatrzański', #'zwój krakowski', #'zwój wieliczka', #'zwój ados', #'zwój fado', #'zwój kalavan', #'zwój kirdneh' oraz #'bilet turystyczny'.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("magiczne zwoje", "zwoje"),
						null,
						ConversationStates.ATTENDING,
						"#Oferuję zwoje i bilety, które pomagają w szybszym podróżowaniu: #'zwój tatrzański', #'zwój krakowski', #'zwój wieliczka', #'zwój ados', #'zwój fado', #'zwój kalavan', #'zwój kirdneh' oraz #'bilet turystyczny'! Powiedz: kupię <ilość> <nazwa zwoju>.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("tatrzański", "zwój tatrzański"),
						null,
						ConversationStates.ATTENDING,
						"Zwój zabiera natychmiast do Zakopanego. Dobra droga ucieczki przed niebezpieczeństwem!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("krakowski", "zwój krakowski"),
						null,
						ConversationStates.ATTENDING,
						"Zwój zabiera do królewskiego miasta Krakowa. Jest to bardzo piękne miasto, które posiada nie jedną tajemnicę.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("ados", "zwój ados"),
						null,
						ConversationStates.ATTENDING,
						"ados city scroll zabiera natychmiast do miasta Ados znajdującego się na wschód stąd!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("semos", "zwój semos"),
						null,
						ConversationStates.ATTENDING,
						"Zwój semos przenosi Ciebie do małego miasta w Faiumoni.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("nalwor", "zwój nalwor"),
						null,
						ConversationStates.ATTENDING,
						"Zwój nalwor przenosi do miasta elfów zwanego Nalwor, które znajduje się w lesie na południe od Semos!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fado", "zwój fado"),
						null,
						ConversationStates.ATTENDING,
						"Zwój fado zabiera do miasta Fado znajdującego się na południowy-zachód od Semos!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("kalavan", "zwój kalavan"),
						null,
						ConversationStates.ATTENDING,
						"Zwój kalavan zabiera natychmiast daleko do miasta Kalavan znajdującego się na południowy-zachód od Semos, za miastem Fado!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("kirdneh", "zwój kirdneh"),
						null,
						ConversationStates.ATTENDING,
						"Zwój kirdneh przenosi do miasta Kirdneh znajdującego się na południowy-zachód od Semos, do którego dostępu broni rzeka Orril!", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("turystyczny", "bilet turystyczny"),
						null,
						ConversationStates.ATTENDING,
						"Bilet turystyczny zabiera na pustynie w pobliżu piramid. Udając się tam zaopatrz się w wodę, a także pamiętaj o upalnych dniach i zimnych nocach oraz niebezpiecznych burzach piaskowych!", null);

				addGoodbye("Dowidzenia i udanej podróży.");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		juhas.setEntityClass("npcjuhas");
		juhas.setPosition(10, 6);
		juhas.setDirection(Direction.UP);
		juhas.initHP(100);
		zone.add(juhas);
	}
}
