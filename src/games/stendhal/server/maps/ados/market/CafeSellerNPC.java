/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Map;

/**
 * Mia works in the Botanical Gardens cafe.
 */
public class CafeSellerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * region that this NPC can give information about
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mia") {

			@Override
			public void createDialog() {
				addGreeting("Witamy w naszej kawiarni w Ogrodzie Botanicznym Ados!");
				addHelp("Nie zapomnij spojrzeć na znaki, które wyjaśniają skąd dana roślina pochodzi!");
				addQuest("Jesteś taki miły! Powinieneś zapytać Callę. Ona zawsze wie kto potrzebuje pomocy.");
				addJob("Sprzedaję napoje i przekąski w kawiarni. Zawze chciałabym powiedzieć, że zrobiłam to jedzenie, ale niestety wszystko jest dostarczane.");
				addOffer("Możesz kupić napoje i przekąski zajrzyj do naszego menu. Wszystko jest dostarczane więc jest drogie, ale za to najlepsze w okolicy!");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("cafe")), false);

				// just to be nice :)
				addEmotionReply("thanks", "ciepłe podziękowanie");
				addEmotionReply("smile", "uśmiecha się");
				
				addGoodbye("Wróć ponownie!");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			protected void createPath() {
				setPath(null);
			}
		};
		npc.setPosition(69, 114);
		npc.setDescription("Oto Mia gotowa obsłużyć klientów z pięknym uśmiechem.");
		npc.setEntityClass("cafesellernpc");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}