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
package games.pol.server.maps.krakow.wawel;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * A young lady (original name: Straznik) who heals players without charge.
 */
public class StraznikNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildguardian(zone);
	}

	private void buildguardian(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Strażnik") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Prowadzę spokojne życie. Pilnuje #wejścia na wawel.");
				addHelp("Mogę sprzedać ci zwój krakowski lub zwój tatrzański.");
				new SellerAdder().addSeller(this, new SellerBehaviour(SingletonRepository.getShopList().get("wawel")));
				addGoodbye();
			}
		};

		npc.setEntityClass("transparentnpc");
		npc.setAlternativeImage("guardian");
		npc.setDescription("Oto Strażnik. Chroni bramę Wawela przed słabymi i niegodnymi wojownikami.");
		npc.setPosition(102, 82);
		npc.initHP(100);
		npc.put("no_shadow", "");
		zone.add(npc);
	}
}