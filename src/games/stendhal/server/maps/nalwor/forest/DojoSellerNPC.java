/***************************************************************************
 *                     Copyright © 2020-2024 - Arianne                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.nalwor.forest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.shop.ItemShopInventory;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;
import games.stendhal.server.maps.nalwor.forest.AssassinRepairerAdder.AssassinRepairer;

/**
 * An NPC that sells special swords for training.
 */
public class DojoSellerNPC implements ZoneConfigurator {
	/** Zone where NPC is to be located. */
	private static StendhalRPZone dojo;
	/** NPC being configured. */
	private static AssassinRepairer seller;
	/** List of items NPC sells. */
	private static ItemShopInventory inventory;

	/** List of items NPC repairs. */
	private static List<String> repairables = new ArrayList<String>() {{
		add("miecz treningowy");
	}};

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		dojo = zone;
		inventory = ShopsList.get().get("dojo", ShopType.ITEM_SELL);
		initNPC();
	}

	/**
	 * Entry point for setting up NPC.
	 */
	private void initNPC() {
		initRepairShop();
		initSellShop();
		initDialogue();

		seller.setEntityClass("samurai2npc");
		seller.setGender("M");
		seller.setIdleDirection(Direction.LEFT);
		seller.setPosition(37, 80);

		dojo.add(seller);
	}

	/**
	 * Sets up seller/repairer NPC and repair items prices.
	 *
	 * If players bring their worn training swords they can get them repaired for half the
	 * price of buying a new one.
	 */
	private void initRepairShop() {
		final Map<String, Integer> repairPrices = new LinkedHashMap<>();
		for (String itemName: repairables) {
			if (inventory.containsKey(itemName)) {
				// repairing is half the cost of buying new
				repairPrices.put(itemName, inventory.getPrice(itemName) / 2);
			}
		}
		// initialize NPC repairer
		seller = new AssassinRepairerAdder.AssassinRepairer("Akutagawa", repairPrices);
		new AssassinRepairerAdder().add(seller);
	}

	/**
	 * Sets up item shop and shop sign.
	 */
	private void initSellShop() {
		final String rejectedMessage = "Tylko członkowie gildii skrytobójców mogą tutaj handlować.";

		// can only purchase if carrying assassins id
		final SellerBehaviour sellerBehaviour = new SellerBehaviour(inventory) {
			@Override
			public ChatCondition getTransactionCondition() {
				return new PlayerHasItemWithHimCondition("licencja na zabijanie");
			}

			@Override
			public ChatAction getRejectedTransactionAction() {
				return new SayTextAction(rejectedMessage);
			}
		};
		new SellerAdder().addSeller(seller, sellerBehaviour, false);

		final ShopSign shopSign = new ShopSign("dojo", "Sklep w Dojo Skrytobójców", seller.getName()
				+ " sprzedaje następujące przedmioty", true) {
			@Override
			public boolean onUsed(final RPEntity user) {
				if (user.isEquipped("licencja na zabijanie")) {
					return super.onUsed(user);
				} else {
					// can only view sign if carrying assassins id
					seller.say(rejectedMessage);
				}
				return true;
			}
		};
		shopSign.setEntityClass("blackboard");
		shopSign.setPosition(36, 81);

		dojo.add(shopSign);
	}

	/**
	 * Sets up NPC's general dialogue.
	 */
	private void initDialogue() {
		seller.addGreeting("Jeśli szukasz sprzętu treningowego, trafiłeś we właściwe miejsce.");
		seller.addGoodbye();
		seller.addOffer("Spójrz na moją tablicę, by sprawdzić co sprzedaję. Mogę również #naprawić wszystkie używane #'miecze treningowe', które aktualnie masz.");
		seller.addJob("Prowadzę sklep w dojo dla skrytobójców, w którym sprzedaję sprzęt i #naprawiam #'miecze treningowe'.");
		seller.addQuest("Nie mam dla ciebie żadnego zadania. Tylko #naprawiam i sprzedaję sprzęt.");
		seller.addHelp("Jeśli chcesz trenować w dojo, polecam zakupić #'miecz treningowy'.");
		seller.addReply(Arrays.asList("training sword", "miecz treningowy", "miecza treningowego"), "Moje miecze treningowe są lekkie oraz łatwe do machania nimi. Dlatego,"
				+ " że zostały one wykonane z drewna, nie zaszkodzą ci gdy zostaniesz uderzony nim.");
	}
}
