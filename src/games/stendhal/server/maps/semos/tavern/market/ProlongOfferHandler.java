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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;

public class ProlongOfferHandler extends OfferHandler {
	@Override
	public void add(SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, Arrays.asList("prolong", "przedłuż"), null, ConversationStates.ATTENDING, null, 
				new ProlongOfferChatAction());
		npc.add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.YES_MESSAGES, 
				ConversationStates.ATTENDING, null, new ConfirmProlongOfferChatAction());
		// PRODUCTION is a misnomer for "prolong all" service, but it's a simple
		// way to distinguish it from a single prolong
		npc.add(ConversationStates.PRODUCTION_OFFERED, ConversationPhrases.YES_MESSAGES, 
				ConversationStates.ATTENDING, null, new ConfirmProlongAllChatAction());
		npc.add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.NO_MESSAGES, null, 
				ConversationStates.ATTENDING, "Zatem, jak jeszcze mogę ci pomóc?", null);
		npc.add(ConversationStates.PRODUCTION_OFFERED, ConversationPhrases.NO_MESSAGES, null, 
				ConversationStates.ATTENDING, "Zatem, jak jeszcze mogę ci pomóc?", null);
	}
	
	protected class ProlongOfferChatAction extends KnownOffersChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (sentence.hasError()) {
				npc.say("Niestety, nie rozumiem co mówisz. "
						+ sentence.getErrorString());
			} else if (sentence.getExpressions().iterator().next().toString().equals("prolong")
				|| sentence.getExpressions().iterator().next().toString().equals("przedłuż")){
				handleSentence(player, sentence, npc);
			}
		}

		private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			try {
				String offerNumber = getOfferNumberFromSentence(sentence).toString();
				
				Map<String,Offer> offerMap = manager.getOfferMap();
				if (offerMap == null) {
					npc.say("Proszę, sprawdź najpierw swoje oferty.");
					return;
				}
				if(offerMap.containsKey(offerNumber)) {
					Offer o = offerMap.get(offerNumber);
					if(o.getOfferer().equals(player.getName())) {
						setOffer(o);
						int quantity = 1;
						if (o.hasItem()) {
							quantity = getQuantity(o.getItem());
						}
						StringBuilder message = new StringBuilder();
						
						if (TradeCenterZoneConfigurator.getShopFromZone(player.getZone()).contains(o)) {
							message.append("Twoja oferta na ");
							message.append(Grammar.quantityplnoun(quantity, o.getItemName(), "jeden"));
							message.append(" wygaśnie za ");
							message.append(TimeUtil.approxTimeUntil((int) ((o.getTimestamp() - System.currentTimeMillis() + 1000 * OfferExpirer.TIME_TO_EXPIRING) / 1000)));
							message.append(". Czy chcesz ją przedłużyć do ");
							message.append(TimeUtil.timeUntil(OfferExpirer.TIME_TO_EXPIRING));
							message.append(" za ");
							message.append(TradingUtility.calculateFee(player, o.getPrice()).intValue());
							message.append(" money?");
						} else {
							message.append("Czy chcesz przedłużyć swoją ofertę na ");
							message.append(Grammar.quantityplnoun(quantity, o.getItemName(), "jeden"));
							message.append(" w cenie ");
							message.append(o.getPrice());
							message.append(" i podatek ");
							message.append(TradingUtility.calculateFee(player, o.getPrice()).intValue());
							message.append(" money?");
						}
						npc.say(message.toString());
						npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
					} else {
						npc.say("Możesz jedynie przedłużyć swoje oferty. Powiedz proszę: #pokaż #moje aby zobaczyć swoje oferty.");
					}
				} else {
					npc.say("Oj, musisz wybrać numer spośród tych, które ci podałem aby przedłużyć ofertę.");
					return;
				}
			} catch (NumberFormatException e) {
				if (!handleProlongAll(player, sentence, npc)) {
					npc.say("Musisz powiedzieć #przedłuż #numer");
				}
			}
		}
	
		private boolean handleProlongAll(Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			int last = sentence.getExpressions().size();
			
			for (Expression expr : sentence.getExpressions().subList(1, last)) {
				if ("all".equals(expr.toString())) {
					Collection<Offer> offers = manager.getOfferMap().values();
					if (offers.isEmpty()) {
						npc.say("Musisz podać listę ofert do przedłużenia.");
						return true;
					}
					int price = 0;
					int numOffers = offers.size();
					List<String> offerDesc = new ArrayList<>(numOffers);
					for (Offer o : offers) {
						if (!o.getOfferer().equals(player.getName())) {
							npc.say("Możesz przedłużyć tylko swoje oferty. Powiedz #pokaż #moje lub #pokaż #wygaśnięte, aby zobaczyć swoje oferty.");
							return true;
						}
						int quantity = 1;
						if (o.hasItem()) {
							quantity = getQuantity(o.getItem());
						}
						price += TradingUtility.calculateFee(player, o.getPrice()).intValue();
						offerDesc.add(Grammar.quantityplnoun(quantity, o.getItemName(), "")
								+ " w cenie " + o.getPrice());
					}
					String total = numOffers > 1 ? "w sumie" : "";
					npc.say("Czychcesz przedłużyć " 
							+ Grammar.plnoun(numOffers, "offer") + " z "
							+ Grammar.enumerateCollection(offerDesc)
							+ " za " + total + " za opłatą " + price + " money?");
					npc.setCurrentState(ConversationStates.PRODUCTION_OFFERED);
					return true;
				}
			}

			return false;
		}
	}
	
	protected class ConfirmProlongOfferChatAction implements ChatAction {
		@Override
		public void fire (Player player, Sentence sentence, EventRaiser npc) {
			Offer offer = getOffer();
			if (!wouldOverflowMaxOffers(player, offer)) {
				Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, offer.getPrice()).intValue());
				if (player.isEquipped("money", fee)) { 
					if (prolongOffer(player, offer)) {
						TradingUtility.substractTradingFee(player, offer. getPrice());
						npc.say("Przedłużam ofertę i pobieram znów prowizję w wysokości "+fee.toString()+".");
					} else {
						npc.say("Niestety ta oferta już została zdjęta ze sprzedaży.");
					}
					// Changed the status, or it has been changed by expiration. Obsolete the offers
					((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
				} else {
					npc.say("Nie stać cię, na opłacenie prowizji w wysokości "+fee.toString());
				}
			} else {
				npc.say("Przykro mi, ale jednocześnie możesz mieć tylko " + TradingUtility.MAX_NUMBER_OFF_OFFERS
						+ " aktywne oferty.");
			}
		}
		
		/**
		 * Check if prolonging an offer would result the player having too many active offers on market.
		 * 
		 * @param player the player to be checked
		 * @param offer the offer the player wants to prolong
		 * @return true if prolonging the offer should be denied
		 */
		boolean wouldOverflowMaxOffers(Player player, Offer offer) {
			Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
			
			if ((market.countOffersOfPlayer(player) == TradingUtility.MAX_NUMBER_OFF_OFFERS)
					&& market.getExpiredOffers().contains(offer)) {
				return true;
			}
			
			return false;
		}
		
		boolean prolongOffer(Player player, Offer o) {
			Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
			if (market != null) {
				if (market.prolongOffer(o) != null) {
					String messageNumberOfOffers = "Dotychczas złożyłeś "+Integer.valueOf(market.countOffersOfPlayer(player)).toString()+" oferty.";
					player.sendPrivateText(messageNumberOfOffers);
					
					return true;
				}
			}
			
			return false;
		}
	}
	
	private class ConfirmProlongAllChatAction extends ConfirmProlongOfferChatAction {
		@Override
		public void fire (Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			Collection<Offer> offers = manager.getOfferMap().values();
			boolean clear = false;
			for (Offer offer : offers) {
				int quantity = 1;
				if (offer.hasItem()) {
					quantity = getQuantity(offer.getItem());
				}
				String offerDesc = Grammar.quantityplnoun(quantity, offer.getItemName(), "one");
				
				if (!offer.getOfferer().equals(player.getName())) {
					// This should not be possible, but it does not hurt to check
					// it anyway.
					npc.say("You can only prolong your own offers.");
					// clear the offer map as it apparently resulted in an
					// incorrect request already.
					clear = true;
					break;
				}
				
				if (!wouldOverflowMaxOffers(player, offer)) {
					Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, offer.getPrice()).intValue());
					if (player.isEquipped("money", fee)) {
					
						if (prolongOffer(player, offer)) {
							TradingUtility.substractTradingFee(player, offer.getPrice());
							npc.say("Przedłużyłam twoją ofertę " + offerDesc 
									+ " i pobrałam opłatę " + fee.toString() + ".");
						} else {
							npc.say("Przykro mi, ale ta oferta " + offerDesc + " już została usunięta z rynku.");
						}
						clear = true;
					} else {
						npc.say("Nie możesz opłacić opłaty handlowej " + fee.toString());
					}
				} else {
					npc.say("Przykro mi, ale możesz mieć tylko " + TradingUtility.MAX_NUMBER_OFF_OFFERS
							+ " ofert naraz.");
					// Avoid complaining about the offer limit multiple times
					break;
				}
			}
			
			if (clear) {
				// Changed the status, or it has been changed by expiration. Obsolete the offers
				((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
			}
		}
	}
}
