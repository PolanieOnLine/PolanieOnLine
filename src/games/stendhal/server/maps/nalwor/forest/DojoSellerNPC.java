/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.BreakableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;


/**
 * An NPC that sells special swords for training.
 */
public class DojoSellerNPC implements ZoneConfigurator {

	private static StendhalRPZone dojoZone;

	private final String sellerName = "Akutagawa";
	private SpeakerNPC seller;

	private static final int swordPrice = 5600;

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		dojoZone = zone;

		initNPC();
		initShop();
		initRepairShop();
		initDialogue();
	}

	private void initNPC() {
		seller = new SpeakerNPC(sellerName);
		seller.setEntityClass("samurai2npc");
		seller.setIdleDirection(Direction.LEFT);
		seller.setPosition(37, 80);

		dojoZone.add(seller);
	}

	private void initShop() {
		final Map<String, Integer> pricesSell = new LinkedHashMap<String, Integer>() {{
			put("miecz treningowy", swordPrice);
			put("shuriken", 80);
			put("płonący shuriken", 105);
		}};

		final ShopList shops = ShopList.get();
		for (final String itemName: pricesSell.keySet()) {
			shops.add("dojosell", itemName, pricesSell.get(itemName));
		}

		final String rejectedMessage = "Tylko członkowie gildii skrytobójców mogą tutaj handlować.";

		// can only purchase if carrying assassins id
		final SellerBehaviour sellerBehaviour = new SellerBehaviour(pricesSell) {
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

		final ShopSign shopSign = new ShopSign("dojosell", "Sklep w Dojo Skrytobójców", sellerName + " sprzedaje następujące przedmioty", true) {
			/**
			 * Can only view sign if carrying assassins id.
			 */
			@Override
			public boolean onUsed(final RPEntity user) {
				if (user.isEquipped("licencja na zabijanie")) {
					return super.onUsed(user);
				} else {
					seller.say(rejectedMessage);
				}

				return true;
			}
		};
		shopSign.setEntityClass("blackboard");
		shopSign.setPosition(36, 81);

		dojoZone.add(shopSign);
	}

	/**
	 * If players bring their worn miecz treningowys they can get them repaired for half the
	 * price of buying a new one.
	 */
	private void initRepairShop() {
		final List<String> repairPhrases = Arrays.asList("repair", "fix", "naprawa", "naprawić", "naprawiam");

		final ChatCondition needsRepairCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return getUsedSwordsCount(player) > 0;
			}
		};

		final ChatCondition canAffordRepairsCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return player.isEquipped("money", getRepairPrice(getUsedSwordsCount(player)));
			}
		};

		final ChatAction sayRepairPriceAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final int usedSwords = getUsedSwordsCount(player);
				final boolean multiple = usedSwords > 1;
				final boolean multiple2 = usedSwords > 4;

				final StringBuilder sb = new StringBuilder("Masz " + Integer.toString(usedSwords));
				if (multiple) {
					sb.append("zużyte miecze treningowe");
				} else if (multiple2) {
					sb.append("zużytych mieczy treningowych");
				} else {
					sb.append("zużyty miecz treningowy");
				}
				sb.append(". Mogę naprawić ");
				if (multiple) {
					sb.append("je wszystkie");
				} else {
					sb.append("to");
				}
				sb.append(" za " + Integer.toString(getRepairPrice(usedSwords)) + " money. Chciałbyś, żebym to zrobił?");

				npc.say(sb.toString());
			}
		};

		final ChatAction repairAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final int swordsCount = getUsedSwordsCount(player);
				player.drop("money", getRepairPrice(swordsCount));

				for (final Item sword: player.getAllEquipped("miecz treningowy")) {
					final BreakableItem breakable = (BreakableItem) sword;
					if (breakable.isUsed()) {
						breakable.repair();
					}
				}

				if (swordsCount > 1) {
					npc.say("Zrobione! Twoje miecze treningowe wyglądają jak nowe");
				} else {
					npc.say("Zrobione! Twój miecz treningowy wygląda jak nowy.");
				}
			}
		};


		seller.add(ConversationStates.ATTENDING,
				repairPhrases,
				new NotCondition(new PlayerHasItemWithHimCondition("licencja na zabijanie")),
				ConversationStates.ATTENDING,
				"Tylko członkowie gildii skrytobójców mogą naprawić swój #'miecz treningowy'.",
				null);

		seller.add(ConversationStates.ATTENDING,
				repairPhrases,
				new AndCondition(
						new PlayerHasItemWithHimCondition("licencja na zabijanie"),
						new NotCondition(needsRepairCondition)),
				ConversationStates.ATTENDING,
				"Nie masz żadnego #'miecza treningowego', który potrzebuje naprawy.",
				null);

		seller.add(ConversationStates.ATTENDING,
				repairPhrases,
				new AndCondition(
						new PlayerHasItemWithHimCondition("licencja na zabijanie"),
						needsRepairCondition),
				ConversationStates.QUESTION_1,
				null,
				sayRepairPriceAction);

		seller.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Okej. Daj mi znać jeśli będziesz czegoś jeszcze potrzebował.",
				null);

		seller.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(needsRepairCondition),
				ConversationStates.ATTENDING,
				"Czyżbyś zgubił swój miecz?",
				null);

		seller.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						needsRepairCondition,
						new NotCondition(canAffordRepairsCondition)),
				ConversationStates.ATTENDING,
				"Przepraszam, ale nie masz wystarczającą ilość money.",
				null);

		seller.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						needsRepairCondition,
						canAffordRepairsCondition),
				ConversationStates.ATTENDING,
				null,
				repairAction);
	}

	private void initDialogue() {
		seller.addGreeting("Jeśli szukasz specjalnego sprzętu treningowego, to trafiłeś we właściwe miejsce.");
		seller.addGoodbye();
		seller.addOffer("Spójrz na moją tablicę, by sprawdzić co sprzedaję. Mogę również #naprawić tobie każdy zużyty #'miecz treningowy'.");
		seller.addJob("Prowadzę sklep w dojo skrytobójców, gdzie sprzedaję sprzęt treningowy oraz #naprawiam każdy #'miecz treningowy'.");
		seller.addQuest("Nie mam żadnego zadania dla ciebie do zrobienia. Mogę jedynie #naprawić i sprzedać sprzęt treningowy.");
		seller.addHelp("Jeśli chcesz trenować w dojo, polecam zakupić #'miecz treningowy'.");
		seller.addReply("miecz treningowy", "Moje miecze treningowe są lekkie oraz łatwe do machania nimi. I tylko dlatego,"
				+ " że są wykonane z drewna, nie zaszkodzą ci, jeśli zostaniesz uderzony jednym.");
	}

	private int getUsedSwordsCount(final Player player) {
		int count = 0;
		for (final Item sword: player.getAllEquipped("miecz treningowy")) {
			if (((BreakableItem) sword).isUsed()) {
				count++;
			}
		}

		return count;
	}

	private int getRepairPrice(final int count) {
		return count * (swordPrice / 2);
	}
}