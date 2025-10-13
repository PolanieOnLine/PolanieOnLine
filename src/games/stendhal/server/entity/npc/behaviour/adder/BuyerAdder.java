/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.action.ComplainAboutSentenceErrorAction;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.SentenceHasErrorCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.NpcShopWindowEvent;
import games.stendhal.server.events.SoundEvent;

public class BuyerAdder {
	private static Logger logger = Logger.getLogger(BuyerAdder.class);

	private final MerchantsRegister merchantsRegister = SingletonRepository.getMerchantsRegister();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	/**
	 * Configures an NPC to buy items.
	 *
	 * @param npc
	 *	 The NPC that buys.
	 * @param buyerBehavior
	 *	 The <code>BuyerBehaviour</code>.
	 */
	public void addBuyer(final SpeakerNPC npc, final BuyerBehaviour buyerBehaviour) {
		addBuyer(npc, buyerBehaviour, true);
	}

	/**
	 * Configures an NPC to buy items.
	 *
	 * @param npc
	 *	 The NPC that buys.
	 * @param buyerBehavior
	 *	 The <code>BuyerBehaviour</code>.
	 * @param offer
	 *	 If <code>true</code>, adds reply to "offer".
	 */
		public void addBuyer(final SpeakerNPC npc, final BuyerBehaviour buyerBehaviour, final boolean offer) {
		final Engine engine = npc.getEngine();

		merchantsRegister.add(npc, buyerBehaviour);
		npc.put("job_merchant", "");
		final ShopWindowSupport shopSupport = new ShopWindowSupport(npc, buyerBehaviour, merchantsRegister);

		if (offer) {
			engine.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				null,
				false,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Skupuję " + Grammar.enumerateCollectionPlural(buyerBehaviour.dealtItems()) + ".");
						shopSupport.openShopWindow(player);
					}
				});
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
							raiser.say("Przepraszam, ale nie mogę ci zaufać. Wyglądasz na zbyt " + player.getGenderVerb("niebezpiecznego") + " do handlowania. Odejdź proszę.");
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
								String quantityplnounItem = Grammar.quantityplnoun(res.getAmount(), chosenItemName);
								raiser.say(Grammar.capitalize(quantityplnounItem)
										+ " " + Grammar.isare(res.getAmount()) + " "
										+ Grammar.worthForm(quantityplnounItem, res.getAmount()) + " "
										+ MoneyUtils.formatPrice(price) + ", Czy chcesz "
										+ Grammar.itthem(res.getAmount()) + " sprzedać?");

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
							raiser.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));
						}

						currentBehavRes = null;
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				false,
				ConversationStates.ATTENDING, "Dobrze w czym mogę pomóc?", null);
	}

	private static final class ShopWindowSupport {
		private static final Set<SpeakerNPC> REGISTERED = Collections.newSetFromMap(new WeakHashMap<SpeakerNPC, Boolean>());

		private final SpeakerNPC npc;
		private final BuyerBehaviour behaviour;
		private final MerchantsRegister register;

		ShopWindowSupport(final SpeakerNPC npc, final BuyerBehaviour behaviour, final MerchantsRegister register) {
			this.npc = npc;
			this.behaviour = behaviour;
			this.register = register;
		}

		void openShopWindow(final Player player) {
			if (player == null) {
				return;
			}
			final SellerBehaviour sellerBehaviour = register.findSeller(npc.getName());
			player.addEvent(NpcShopWindowEvent.open(npc, sellerBehaviour, behaviour, player));
			player.notifyWorldAboutChanges();
			registerListener();
		}

		private void registerListener() {
			synchronized (REGISTERED) {
				if (REGISTERED.add(npc)) {
					npc.addGoodbyeListener(new Consumer<RPEntity>() {
						@Override
						public void accept(final RPEntity entity) {
							if (entity instanceof Player) {
								final Player player = (Player) entity;
								player.addEvent(NpcShopWindowEvent.close(npc));
								player.notifyWorldAboutChanges();
							}
						}
					});
				}
			}
		}
	}

}
