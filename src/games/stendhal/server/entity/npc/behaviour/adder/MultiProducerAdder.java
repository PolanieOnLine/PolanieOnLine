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
package games.stendhal.server.entity.npc.behaviour.adder;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ComplainAboutSentenceErrorAction;
import games.stendhal.server.entity.npc.action.MultiProducerBehaviourAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.SentenceHasErrorCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ProducerWindowEvent;

public class MultiProducerAdder {
	private static Logger logger = Logger.getLogger(MultiProducerAdder.class);

	private final ProducerRegister producerRegister = SingletonRepository.getProducerRegister();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	public void addMultiProducer(final SpeakerNPC npc, final MultiProducerBehaviour behaviour, final String welcomeMessage) {
		addMultiProducer(npc, behaviour, null, welcomeMessage);
	}
	
	private ChatCondition setQuestCondition(String questComplete) {
		if (questComplete != null) {
			return new QuestCompletedCondition(questComplete);
		}
		return null;
	}

	/**
	 * Adds all the dialogue associated with a Producing NPC
	 *
	 * @param npc producer
	 * @param behaviour
	 * @param welcomeMessage
	 */
	public void addMultiProducer(final SpeakerNPC npc, final MultiProducerBehaviour behaviour, final String questComplete, final String welcomeMessage) {
		/** Which NPC is this? */
		final Engine engine = npc.getEngine();
		/** What quest slot is the production stored in? */
		final String QUEST_SLOT = behaviour.getQuestSlot();
		/** How should we greet the player? */
		final String thisWelcomeMessage = welcomeMessage;
		/** What is the NPC name? */
		final String npcName = npc.getName();

		/* add to producer register */
		producerRegister.add(npcName, behaviour);
		npc.put("job_producer", "");

		/* The Player greets the NPC.
		* The NPC is not currently producing for player (not started, is rejected, or is complete) */
		if (thisWelcomeMessage != null) {
			if (questComplete == null) {
				engine.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(npcName),
						new QuestNotActiveCondition(QUEST_SLOT)),
					false, ConversationStates.ATTENDING, thisWelcomeMessage, null);
			}

			if (questComplete != null) {
				engine.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(npcName),
						new QuestNotActiveCondition(QUEST_SLOT),
						new QuestCompletedCondition(questComplete)),
					false, ConversationStates.ATTENDING, thisWelcomeMessage, null);

				engine.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(npcName),
						new QuestNotActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestCompletedCondition(questComplete))),
					false, ConversationStates.ATTENDING, null, new SayTextAction("Hej! W czym mogę #pomóc?"));
			}
		}

		engine.add(ConversationStates.ATTENDING,
			behaviour.getProductionActivity(),
			new AndCondition(new SentenceHasErrorCondition(), setQuestCondition(questComplete)),
			false, ConversationStates.ATTENDING,
			null, new ComplainAboutSentenceErrorAction());

		/* In the behaviour a production activity is defined, e.g. 'cast' or 'mill'
		* and this is used as the trigger to start the production,
		* provided that the NPC is not currently producing for player (not started, is rejected, or is complete) */
		engine.add(ConversationStates.ATTENDING,
			behaviour.getProductionActivity(),
			new AndCondition(
				new NotCondition(new SentenceHasErrorCondition()),
				new QuestNotActiveCondition(QUEST_SLOT),
				setQuestCondition(questComplete)),
			false, ConversationStates.ATTENDING,
			null, new MultiProducerBehaviourAction(behaviour) {
				@Override
                               public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                                       player.addEvent(new ProducerWindowEvent(npc.getName(), npc.getTitle()));
                                       player.notifyWorldAboutChanges();
                                       super.fire(player, sentence, npc);
                               }

				@Override
				public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser npc) {
					// Find out how much items we shall produce.
					if (res.getAmount() > 1000) {
						logger.warn("Decreasing very large amount of "
								+ res.getAmount()
								+ " " + res.getChosenItemName()
								+ " to 1 for player "
								+ player.getName() + " talking to "
								+ npcName + " saying " + sentence);
						res.setAmount(1);
					}

					if (behaviour.askForResources(res, npc, player)) {
							currentBehavRes = res;
							npc.setCurrentState(ConversationStates.PRODUCTION_OFFERED);
					}
				}
			});

		/* Player agrees to the proposed production deal */
		engine.add(ConversationStates.PRODUCTION_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			false, ConversationStates.ATTENDING,
			null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					behaviour.transactAgreedDeal(currentBehavRes, npc, player);

					currentBehavRes = null;
				}
			});

		/* Player does not agree to the proposed production deal */
		engine.add(ConversationStates.PRODUCTION_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			false, ConversationStates.ATTENDING, "Dobrze nie ma problemu.", null);

		/* Player says the production trigger word but the NPC is already producing items for that player */
		engine.add(ConversationStates.ATTENDING,
			behaviour.getProductionActivity(),
			new QuestActiveCondition(QUEST_SLOT),
			false, ConversationStates.ATTENDING,
			null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence,
						final EventRaiser npc) {
					// TODO: check - can the StateRemainingTimeAction be used here?
					npc.say("Wciąż nie dokończyłem twojego ostatniego zlecenia. Wróć za "
							+ behaviour.getApproximateRemainingTime(player)
							+ "!");
				}
			});

		/* Player greets NPC and the NPC is already producing items for that player
		 * There are two options: the NPC is still busy or he is finished
		 * The method giveProduct(npc, player) used here takes care of both. */
		engine.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npcName),
					new QuestActiveCondition(QUEST_SLOT)),
			false, ConversationStates.ATTENDING,
			null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence,
						final EventRaiser npc) {
					behaviour.giveProduct(npc, player);
				}
			});
	}
}
