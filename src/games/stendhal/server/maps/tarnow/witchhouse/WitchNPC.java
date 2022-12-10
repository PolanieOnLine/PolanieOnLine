/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.witchhouse;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * @author KarajuSs
 */
public class WitchNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Pauli") {
			@Override
			protected void createDialog() {
				addGreeting("Cześć, trochę mnie zaskoczyłeś wchodząc do mojego domu, a nie sklepu...");
				addJob("Zajmuję się utylizacją różdżek. Jeżeli jakieś masz to chętnie odkupię od Ciebie.");
				addOffer("Jeżeli jakieś różdżki posiadasz to chętnie odkupię od Ciebie.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("wandbuyer")), true);
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Pauli. Siedzi przy stole i popija spokojnie herbatkę.");
		npc.setEntityClass("woman_004_npc");
		npc.setGender("F");
		npc.setPosition(16, 8);
		zone.add(npc);
	}
}
