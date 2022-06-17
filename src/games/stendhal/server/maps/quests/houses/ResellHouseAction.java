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
package games.stendhal.server.maps.quests.houses;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

final class ResellHouseAction implements ChatAction {
	private static final Logger logger = Logger.getLogger(ResellHouseAction.class);

	private int cost;
	private final String questSlot;
	private final int depreciationPercentage;
	private final HouseTax houseTax;

	ResellHouseAction(final int cost, final String questSlot, final int deprecationPercentage, final HouseTax houseTax) {
		this.questSlot = questSlot;
		this.cost = cost;
		depreciationPercentage = deprecationPercentage;
		this.houseTax = houseTax;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		// we need to find out where this house is so we know how much to refund them
		final String claimedHouse = player.getQuest(questSlot);

		try {
			final int id = Integer.parseInt(claimedHouse);
			final HousePortal portal = HouseUtilities.getHousePortal(id);
			// Kirdneh
			if (id > 25 && id < 50) {
				cost = KirdnehHouseSeller.COST_KIRDNEH;
			// Ados
			} else if(id > 49 && id < 78) {
				cost = AdosHouseSeller.COST_ADOS;
			// Zakopane
			} else if (id > 200 && id < 216) {
				cost = ZakopaneHouseSeller.COST_ZAKOPANE;
			// Rest
			} else {
				cost = 100000;
			}
			final int refund = (cost * depreciationPercentage) / 100 - houseTax.getTaxDebt(portal);

			final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
			money.setQuantity(refund);
			player.equipOrPutOnGround(money);

			portal.changeLock();
			portal.setOwner("");
			// the player has sold the house. clear the slot
			player.removeQuest(questSlot);
			raiser.say("Dziękuję oto " + Integer.toString(refund)
					   + " money ze sprzedaży domu, wartość domu minus podatki. Teraz nie masz własnego domu "
					   + "możesz teraz kupić inny o ile chcesz.");
		} catch (final NumberFormatException e) {
			logger.error("Invalid number in house slot", e);
			raiser.say("Przepraszam, ale stało się coś złego. Jest mi bardzo przykro.");
			return;
		}
	}
}
