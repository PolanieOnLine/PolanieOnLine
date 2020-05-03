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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.KillsForQuestCounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

/**
 * QUEST: KillMonks
 *
 * PARTICIPANTS: <ul>
 * <li> Andy on Ados cemetery
 * <li> Darkmonks and normal monks
 * </ul>
 *
 * STEPS:<ul>
 * <li> Andy who is sad about the death of his wife, wants revenge for her death 
 * <li> Kill 25 monks and 25 darkmonks for him for reaching his goal
 * </ul>
 * 
 *
 * REWARD:<ul>
 * <li> 15000 XP
 * <li> 1-5 soup
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in two weeks</ul>
 * 
 * @author Vanessa Julius, idea by anoyyou

 */

public class KillMonks extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_monks";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	public KillMonks() {
		super();
		
		 creaturestokill.put("mnich", 
				 new Pair<Integer, Integer>(0, 25));

		 creaturestokill.put("mnich ciemności",
				 new Pair<Integer, Integer>(0, 25));
		 		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Andy");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Moja kochana żona została zamordowana, gdy szła do Wo'fol, aby zamówić pizzę u Kroipa. Jacyś mnichowie ją napadli i nie miała szansy. Teraz chcę się zemścić! Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)),
				ConversationStates.QUEST_OFFERED,
				"Ci mnichowie są okrutni, a ja wciąż nie mogę dokonać mojej zemsty. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)), new QuestStateStartsWithCondition(QUEST_SLOT, "killed")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2, "Ci mnichowie dostali lekcje, ale możliwe, że znów będę potrzebował twojej pomocy za"));
	

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new IncreaseKarmaAction(5));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Zabij 25 mnichów i 25 mnichów ciemności w imię mojej ukochanej żony.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyśzmienisz zdanie i pomożesz smutnemu człowiekowi.",
				new MultipleActions(
				        new SetQuestAction(QUEST_SLOT, 0, "rejected"),
				        new DecreaseKarmaAction(5)));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Andy");
		
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("zupa");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(4) + 1;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(15000));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "killed"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuję! Teraz mogę spać trochęspokojniej. Proszęprzyjmij tą zupę.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mi w dokonaniu zemsty!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Mnichów",
				"Żona Andiego została zamordowana przez mnichów, a teraz on chce dokonać na nich zemsty.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Spotkałem Andiego w mieście Ados. Poprosił mnie o pomszczenie jego żony.");
			final String questStateFull = player.getQuest(QUEST_SLOT);
			final String[] parts = questStateFull.split(";");
			final String questState = parts[0];

			if ("rejected".equals(questState)) {
				res.add("Odrzuciłem prośbę.");
			}
			if ("start".equals(questState)) {
				res.add("Obiecałem zabić 25 mnichów i 25 mnichów ciemności, aby dokonać zemsty za żone Andiego.");
				res.add(howManyWereKilled(player, parts[1]));
			}
			if (isCompleted(player)) {
				if(isRepeatable(player)){
					res.add("Teraz po dwóch tygodniach powinienem odwiedzić Andiego. Może potrzebuje mojej pomocy!");
				} else {
					res.add("Zabiłem paru mnichów, a Andi może teraz spać trochę spokojnie!");
				}
			}
			int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 0) {
				res.add("Zemściłem się dla Andiego "
						+ Grammar.quantityplnoun(repetitions, "razy") + ".");
			}
			return res;
	}

	private String howManyWereKilled(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		int remainingMonks = killsCounter.remainingKills(player, "mnich");
		int remainingDarkMonks = killsCounter.remainingKills(player, "mnich ciemności");
		if (remainingMonks > 0 && remainingDarkMonks > 0) {
			return "Wciąż muszę zabić " + Grammar.quantityplnoun(remainingMonks, "mnich")  + " i " + Grammar.quantityplnoun(remainingDarkMonks, "mnich ciemności") + ".";
		} else if (remainingMonks > 0) {
			return "Wciąż muszę zabić " + Grammar.quantityplnoun(remainingMonks, "mnich") + ".";
		} else if (remainingDarkMonks > 0) {
			return "Wciąż muszę zabić " + Grammar.quantityplnoun(remainingDarkMonks, "mnich ciemności") + ".";
		} else {
			return "Zabiłem 25 mnichów i 25 mnichów ciemności.";
		}
	}

	@Override
	public String getName() {
		return "KillMonks";

	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Andy";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
