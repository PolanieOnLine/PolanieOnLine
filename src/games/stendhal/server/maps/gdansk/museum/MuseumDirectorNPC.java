/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.museum;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * @author KarajuSs
 */
public class MuseumDirectorNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

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

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mieczysław") {

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem dyrektorem tutejszej instytucji jakim jest muzeum w Gdańsku!");
				addHelp("Muzeum potrzebuje nowych eksponatów, jeśli jesteś gotów na #zadanie ode mnie to się odezwij.");
				addOffer("Mogę sprzedać #'zwój gdański', #'zwój tatrzański' oraz #'niezapisany zwój'. Chyba, że szukasz u mnie #zadania.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("mieczyslaw")));
				addGoodbye("Życzę powodzenia!");
			}
		};

		npc.setDescription("Oto dyrektor muzeum w Gdańsku, Mieczysław. Być może potrzebuje pomocy z eksponatami.");
		npc.setEntityClass("beggarnpc");
		npc.setGender("M");
		npc.setPosition(24, 3);
		zone.add(npc);
	}
}
