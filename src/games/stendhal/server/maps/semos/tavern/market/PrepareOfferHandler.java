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
package games.stendhal.server.maps.semos.tavern.market;

import marauroa.server.db.command.DBCommandQueue;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.dbcommand.LogTradeEventCommand;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.RingOfLife;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.util.AsynchronousProgramExecutor;

import java.util.Arrays;

public class PrepareOfferHandler {
	private Item item;
	private int price;
	private int quantity;
	
	public void add(SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, Arrays.asList("sell", "sprzedam"),
				new LevelLessThanCondition(6), 
				ConversationStates.ATTENDING, 
				"Przepraszam, ale akceptuję oferty tylko od osób, które cieszą się dobrą reputacją. Możesz zdobyć moje przyzwolenie pod warunkiem, że zdobędziesz trochę doświadczenia na przykład pomagając ludziom wykonując ich zadania lub bronić miasta przed diabelskimi potworami.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("sell", "sprzedam"), 
				new LevelGreaterThanCondition(5), 
				ConversationStates.ATTENDING, null, 
				new PrepareOfferChatAction());
		npc.add(ConversationStates.ATTENDING, Arrays.asList("sell", "sprzedam"), null, ConversationStates.ATTENDING, null, 
				new PrepareOfferChatAction());
		npc.add(ConversationStates.SELL_PRICE_OFFERED, ConversationPhrases.YES_MESSAGES, 
				ConversationStates.ATTENDING, null, new ConfirmPrepareOfferChatAction());
		npc.add(ConversationStates.SELL_PRICE_OFFERED, ConversationPhrases.NO_MESSAGES, null, 
				ConversationStates.ATTENDING, "Dobrze, w czym jeszcze mogę pomóc?", null);
	}
	
	private void setData(Item item, int price, int quantity) {
		this.item = item;
		this.price = price;
		this.quantity = quantity;
	}
	
	/**
	 * Builds the message for the tweet to be posted
	 * @param i the offered item
	 * @param q the quantity of the offered item
	 * @param p the price for the item
	 * @return the message to be posted in the tweet
	 */
	public String buildTweetMessage(Item i, int q, int p) {
		StringBuilder message = new StringBuilder();
		message.append("Nowa oferta dla ");
		message.append(Grammar.quantityplnoun(q, i.getName(), "a"));
		message.append(" za ");
		message.append(p);
		message.append(" money. ");
		String stats = "";
		String description = i.describe();
		int start = description.indexOf("Statystyki są (");
		if(start > -1) {
			stats = description.substring(start);
		}
		message.append(stats);
		return message.toString();
	}
	
	private class PrepareOfferChatAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (sentence.hasError()) {
				npc.say("Niestety, nie zrozumiałem ani słowa z tej dziwnej oferty.");
				npc.setCurrentState(ConversationStates.ATTENDING);
			} else if ((sentence.getExpressions().iterator().next().toString().equals("sell"))
					|| (sentence.getExpressions().iterator().next().toString().equals("sprzedam"))) {
				handleSentence(player, sentence, npc);
			}
		}

		private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
			if(TradingUtility.isPlayerWithinOfferLimit(player)) {
				if (sentence.getExpressions().size() < 3 || sentence.getNumeralCount() != 1) {
					npc.say("Nie zrozumiałem co chcesz powiedzieć. Musisz mówić według wzoru: \"sprzedam przedmiot cena\".");
					npc.setCurrentState(ConversationStates.ATTENDING);
					return;
				}
				String itemName = determineItemName(sentence);
				int number = determineNumber(sentence);
				int price = determinePrice(sentence);
				Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, price).intValue());
				if(TradingUtility.canPlayerAffordTradingFee(player, price)) {
					Item item = player.getFirstEquipped(itemName);
					if (item == null) {
						// Some items are in plural. look for those
						item = player.getFirstEquipped(Grammar.plural(itemName));
					}

					if (item == null) {
						npc.say("Wybacz, ale wydaje mi się, że nie masz przy sobie " 
								+ Grammar.plural(itemName)+ ".");
						return;
					}
					// The item name might not be what was used for looking it up (plurals)
					itemName = item.getName();
					
					if ((number > 1) && !(item instanceof StackableItem)) {
						npc.say("Przykro mi, ale możesz położyć tylko jeden na sprzedaż jako indywidualny przedmiot.");
						return;
					} else if (item.isBound()) {
						npc.say("Ten " + itemName +  " może być używany tylko przez Ciebie. Nie możesz go sprzedać.");
						return;
					} else if (item.getDeterioration() > 0) {
						npc.say("Ten " + itemName + " jest uszkodzony. Nie mogę go sprzedać.");
						return;
					} else if (number > 1000) {
						npc.say("Niestety nie dysponuję takim zapleczem, które pomieściłoby tak dużą ilość " + Grammar.plural(itemName) + ".");
						return;
					} else if (price > 1000000) {
						npc.say("To niewiarygodna ilość pieniędzy jaką chcesz za " + Grammar.plural(itemName) + ". Wybacz, ale nie mogę przyjąć takiej oferty.");
						return;
					} else if (item.hasSlot("content") && item.getSlot("content").size() > 0) {
						npc.say("Proszę zwolnij najpierw  " + itemName + ".");
						return;
					} else if (item instanceof RingOfLife) {
					    // broken ring of life should not be sold via Harold
					    if(((RingOfLife) item).isBroken()) {
					        npc.say("Proszę przedtem napraw swój " + itemName + " przed próbą sprzedaży.");
					        return;
					    }
					}

					// All looks ok so far. Ask confirmation from the player.
					setData(item, price, number);
					StringBuilder msg = new StringBuilder();
					msg.append("Czy chcesz sprzedać ");
					msg.append(Grammar.quantityplnoun(number, itemName, "a"));
					msg.append(" za ");
					if (number != 1) {
						msg.append("łącznie ");
					}
					msg.append(price);
					msg.append(" money? Będzie cię to kosztowało ");
					msg.append(fee);
					msg.append(" money prowizji.");
					npc.say(msg.toString());
					
					npc.setCurrentState(ConversationStates.SELL_PRICE_OFFERED);
					return;
				}
				npc.say("Nie stać cię na zapłatę prowizji w wysokości " + fee.toString());
				return;
			}
			npc.say("Nie możesz umieścić więcej niż " + TradingUtility.MAX_NUMBER_OFF_OFFERS + " ofert.");
		}

		private int determineNumber(Sentence sentence) {
			Expression expression = sentence.getExpression(1,"");
			return expression.getAmount();
		}

		private String determineItemName(Sentence sentence) {
			Expression expression = sentence.getExpression(1,"");
			return expression.getNormalized();
		}

		private int determinePrice(Sentence sentence) {
			return sentence.getNumeral().getAmount();
		}
	}
	
	private class ConfirmPrepareOfferChatAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			int fee = TradingUtility.calculateFee(player, price).intValue();
			if (TradingUtility.canPlayerAffordTradingFee(player, price)) {
				if (createOffer(player, item, price, quantity)) {
					TradingUtility.substractTradingFee(player, price);
					new AsynchronousProgramExecutor("trade", buildTweetMessage(item, quantity, price)).start();
					DBCommandQueue.get().enqueue(new LogTradeEventCommand(player, item, quantity, price));
					npc.say("Dodałem twoją ofertę do sprzedaży i pobrałem prowizję w wysokości "+ fee +" money.");
					npc.setCurrentState(ConversationStates.ATTENDING);
				} else {
					npc.say("Nie masz " + Grammar.quantityplnoun(quantity, item.getName(), "a") + ".");
				}
				return;
			}
			npc.say("Nie stać cię na zapłatę prowizji w wysokości " + fee + " money.");
		}

		/**
		 * Try creating an offer.
		 * 
		 * @param player the player who makes the offer
		 * @param item item for sale
		 * @param price price for the item
		 * @param number number of items to sell
		 * @return true if making the offer was successful, false otherwise
		 */
		private boolean createOffer(Player player, Item item, int price, int number) {
			Market shop = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
			if(shop != null) {
				Offer o = shop.createOffer(player, item, Integer.valueOf(price), Integer.valueOf(number));
				if (o == null) {
					return false;
				}

				StringBuilder message = new StringBuilder("Utworzono ofertę na sprzedaż ");
				message.append(item.getName());
				message.append(" za ");
				message.append(price);
				message.append(". ");
				String messageNumberOfOffers = "W chwili obecnej masz "
					+ Grammar.quantityplnoun(Integer.valueOf(shop.countOffersOfPlayer(player)),"ofert", "jeden") + ".";
				player.sendPrivateText(message.toString() + messageNumberOfOffers);
				return true;
			}
			return false;
		}
	}
}
