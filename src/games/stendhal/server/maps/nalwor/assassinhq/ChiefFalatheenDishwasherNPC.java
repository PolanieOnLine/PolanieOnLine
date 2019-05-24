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
package games.stendhal.server.maps.nalwor.assassinhq;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.Arrays;
import java.util.Map;

/**
 * Inside Nalwor Assassin Headquarters - cellar .
 */
public class ChiefFalatheenDishwasherNPC implements ZoneConfigurator  {

	private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Chief Falatheen Humble Dishwasher") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGreeting("Lepiej miej dobry powód za zawracanie mi głowy. Jestem zajęty zmywaniem naczyń!");
				addJob("To moja praca zmywanie wszystkich naczyń po tych wszystkich małych bachorach.");
				addHelp("Mogę kupić od Ciebie warzywa i zioła. Spójrz na tablicę na ścianie, aby dowiedzieć się czego potrzebuję.");
				addOffer("Spójrz na tablice na ścianie, aby poznać moje ceny.");
				addQuest("Mógłbyś mi pomóc w #ucieczce od tych bandziorów. Cóż... może nie.");
				addGoodbye("Nie zapomnij gdzie teraz jestem. Wróć kiedyś. Jestem tutaj samotny.");
				addReply(Arrays.asList("escape", "ucieczce"), "Tak! Chce spełnić swoje marzenie. Mother Helena zaoferowała mi wspaniałą pracę. Potrzebuje osoby do zmywania naczyń. Pełno narzekających klientów!!!");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyveggiesandherbs")), false);			    
			}};
			npc.setPosition(20, 3);
			npc.setDescription("Oto wyglądający na silnego mężczyzna. Je dużo zdrowych warzyw, aby tak wyglądać!");
			npc.setEntityClass("chieffalatheennpc");
			zone.add(npc);		
			   	}
}

