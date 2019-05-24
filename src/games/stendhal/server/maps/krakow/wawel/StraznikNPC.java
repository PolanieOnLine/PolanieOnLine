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
package games.stendhal.server.maps.krakow.wawel;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * A young lady (original name: Straznik) who heals players without charge. 
 */
public class StraznikNPC extends SpeakerNPCFactory {

	@Override
	public void createDialog(final SpeakerNPC npc) {
		npc.addGreeting("Witaj wędrowcze.");
		npc.addJob("Prowadzę spokojne życie. Pilnuje #wejścia na wawel.");
		npc.addHelp("Mogę sprzedać ci zwój krakowski lub zwój tatrzański.");
		new SellerAdder().addSeller(npc, new SellerBehaviour(SingletonRepository.getShopList().get("wawel")));
		npc.addGoodbye();
	}
}
