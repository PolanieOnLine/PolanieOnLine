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
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** The sale of a spare key has been agreed, player meets conditions, 
 * here is the action to simply sell it. */
final class BuySpareKeyChatAction extends HouseChatAction implements ChatAction {


	protected BuySpareKeyChatAction(final String questslot) {
		super(questslot);
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		if (player.isEquipped("money", HouseChatAction.COST_OF_SPARE_KEY)) {

			final String housenumber = player.getQuest(questslot);
			final Item key = SingletonRepository.getEntityManager().getItem(
																			"klucz do drzwi");
			final int number = MathHelper.parseInt(housenumber);
			final HousePortal houseportal = HouseUtilities.getHousePortal(number);

			if (houseportal == null) {
				// something bad happened
				raiser.say("Przepraszam, ale stało się coś złego. Jest mi bardzo przykro.");
				return;
			}
			
			final int locknumber = houseportal.getLockNumber();
			final String doorId = houseportal.getDoorId();
			((HouseKey) key).setup(doorId, locknumber, player.getName());

			if (player.equipToInventoryOnly(key)) {
				player.drop("money", HouseChatAction.COST_OF_SPARE_KEY);
				raiser.say("Proszę oto zapasowy klucz do twojego domu. Pamiętaj, aby dawać zapasowy klucz osobom, którym #naprawdę, #naprawdę ufasz! Każdy z zapasowym kluczem ma dostęp do twojej skrzyni. Nie zapomnij powiedzieć osobom, którym dałeś klucz, żeby dali Tobie znać, gdy zgubią klucz. Jeżeli się to zdarzy to powinieneś #zmienić zamki.");
			} else {
				raiser.say("Nie możesz wziąć więcej kluczy!");
			}
		} else {
			raiser.say("Nie masz wystarczająco dużo pieniędzy na następny klucz!");
		}
	}
}
