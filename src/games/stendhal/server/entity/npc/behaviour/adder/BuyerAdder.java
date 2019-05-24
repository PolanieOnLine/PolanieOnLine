/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.adder;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.action.ComplainAboutSentenceErrorAction;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.SentenceHasErrorCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;

public class BuyerAdder {
	private static Logger logger = Logger.getLogger(BuyerAdder.class);

    private final MerchantsRegister merchantsRegister = SingletonRepository.getMerchantsRegister();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	public void addBuyer(final SpeakerNPC npc, final BuyerBehaviour buyerBehaviour, final boolean offer) {
		final Engine engine = npc.getEngine();

		merchantsRegister.add(npc, buyerBehaviour);

		if (offer) {
			engine.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				null,
				false,
				ConversationStates.ATTENDING,
				"Skupuję " + Grammar.enumerateCollectionPlural(buyerBehaviour.dealtItems()) + ".",
				null);
		}
		engine.add(ConversationStates.ATTENDING,
			ConversationPhrases.SALES_MESSAGES,
			new SentenceHasErrorCondition(),
			false, ConversationStates.ATTENDING,
			null, new ComplainAboutSentenceErrorAction());

		engine.add(ConversationStates.ATTENDING,
			ConversationPhrases.SALES_MESSAGES,
			new AndCondition(
					new NotCondition(new SentenceHasErrorCondition()),
					new NotCondition(buyerBehaviour.getTransactionCondition())),
			false, ConversationStates.ATTENDING,
			null, buyerBehaviour.getRejectedTransactionAction());

		engine.add(ConversationStates.ATTENDING,
				ConversationPhrases.SALES_MESSAGES,
				new AndCondition(
						new NotCondition(new SentenceHasErrorCondition()),
						buyerBehaviour.getTransactionCondition()),
				false, ConversationStates.ATTENDING,
				null,
				new BehaviourAction(buyerBehaviour, ConversationPhrases.SALES_MESSAGES, "buy") {
					@Override
					public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isBadBoy()) {
							// don't buy from player killers at all
							raiser.say("Przepraszam, ale nie mogę ci zaufać. Wyglądasz na zbyt niebezpiecznego do handlowania. Odejdź proszę.");
							raiser.setCurrentState(ConversationStates.IDLE);
							return;
						}

						String chosenItemName = res.getChosenItemName();

						if (res.getAmount() > 1000) {
							logger.warn("Refusing to buy very large amount of "
									+ res.getAmount()
									+ " " + chosenItemName
									+ " from player "
									+ player.getName() + " talking to "
									+ raiser.getName() + " saying "
									+ sentence);
							raiser.say("Maksymalną ilość " 
									+ chosenItemName 
									+ " jaką mogę kupić jest 1000 naraz.");
						} else if (res.getAmount() > 0) {
							final String itemName = chosenItemName;
							// will check if player have claimed amount of items
							if (itemName.equals("sheep")) {
								// player have no sheep...
								if (!player.hasSheep()) {
									raiser.say("Nie posiadasz owcy " + player.getTitle() + "! Co chcesz zrobić?");
									return;
								}
							} else {
								// handle other items as appropriate
							}
							
							if (itemName.equals("goat")) {
								if (!player.hasGoat()) {
									raiser.say("Nie posiadasz kozy " + player.getTitle() + "! Co chcesz zrobić?");
									return;
								}
							} else {
								// handle other items as appropriate
							}

							final int price = buyerBehaviour.getCharge(res, player);

							if (price != 0) {
    							raiser.say(Grammar.quantityplnoun(res.getAmount(), chosenItemName, "")
	    								+ " " + Grammar.isare(res.getAmount()) + "warty jest "
	    								+ price + ". Czy chcesz sprzedać "
    									+ Grammar.itthem(res.getAmount()) + "?");

    							currentBehavRes = res;
    							npc.setCurrentState(ConversationStates.SELL_PRICE_OFFERED); // success
							} else {
								raiser.say("Przepraszam " 
										+ Grammar.thatthose(res.getAmount()) + " " 
										+ Grammar.plnoun(res.getAmount(), chosenItemName)
	    								+ " " + Grammar.isare(res.getAmount()) + " jest nic nie warte.");
							}
						} else {
							raiser.say("Przepraszam, ile " + Grammar.plural(chosenItemName) + " chcesz sprzedać?!");
						}
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						logger.debug("Buying something from player " + player.getName());

						boolean success = buyerBehaviour.transactAgreedDeal(currentBehavRes, raiser, player);
						if (success) {
							raiser.addEvent(new SoundEvent("coins-1", SoundLayer.CREATURE_NOISE));
						}

						currentBehavRes = null;
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				false,
				ConversationStates.ATTENDING, "Dobrze w czym mogę pomóc?", null);
	}

}
