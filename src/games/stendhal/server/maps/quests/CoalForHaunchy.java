/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Coal for Haunchy
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Haunchy Meatoch, the BBQ grillmaster on the Ados market</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Haunchy Meatoch asks you to fetch coal for his BBQ</li>
 * <li>Find some coal in Semos Mine or buy some from other players</li>
 * <li>Take the coal to Haunchy</li>
 * <li>Haunchy gives you a tasty reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>XP +1000 in all</li>
 * <li>Some grilled steaks, random between 1 and 4.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it each 2 days.</li>
 * </ul>
 * 
 * @author Vanessa Julius and storyteller
 */
public class CoalForHaunchy extends AbstractQuest {
	private static final String QUEST_SLOT = "coal_for_haunchy";
	private final SpeakerNPC npc = npcs.get("Haunchy Meatoch");

	// The delay between repeating quests is 48 hours or 2880 minutes
	private static final int REQUIRED_MINUTES = 2880;

	private void offerQuestStep() {
		// player says quest when he has not ever done the quest before (rejected or just new)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Nie mogę wykorzystać polan do tego wielkiego grilla. Aby utrzymać temperaturę potrzebuję węgla, ale nie zostało go dużo. Problem w tym, że nie mogę go zdobyć ponieważ moje steki mogłby się spalić i dlatego muszę tu zostać. Czy mógłbyś przynieść mi 25 kawałków #węgla do mojego grilla?",
				null);

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("węgiel","węgla"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Węgiel nie jest łatwo znaleźć. Normalnie możesz go znaleźć pod ziemią, ale może będziesz miał szczęście i znajdziesz w tunelach starej kopalni Semos...",
				null);

        // player has completed the quest (doesn't happen here)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Mogę teraz grilować moje pyszne steki! Dziękuję!",
				null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.QUEST_OFFERED,
				"Ostatnio węgiel, który mi przyniosłeś już wykorzystałem. Przyniesiesz mi go więcej?",
				null);
		
		// player asks about quest which he has done already but it is not time to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Zapas węgla jest wystarczająco spory. Nie będę potrzebował go w ciągu "));

		// Player agrees to get the coal, increase 5 karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dziękuję! Jeżeli znalazłeś 25 kawałków to powiedz mi #węgiel to będę widział, że masz. Będę wtedy pewien, że będę mógł ci dać pyszną nagrodę.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh nie ważne. Myślałem, że kochasz grillowane steki jak ja. Żegnaj.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	/**
	 * Get Coal Step:
	 * Players will get some coal in Semos Mine and with buying some from other players.
	 */
	private void bringCoalStep() {
		final List<String> triggers = new ArrayList<String>();
		triggers.add("węgiel");
		triggers.add("stone coal");
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		// player asks about quest or says coal when they are supposed to bring some coal and they have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("węgiel",25)),
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(
						new DropItemAction("węgiel",25), 
						new IncreaseXPAction(1000),
						new IncreaseKarmaAction(20),
						new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								int grilledsteakAmount = Rand.rand(4) + 1;
								new EquipItemAction("grillowany stek", grilledsteakAmount, true).fire(player, sentence, npc);
								npc.say("Dziękuję!! Przyjmij te " + Grammar.thisthese(grilledsteakAmount) + " " +
										Grammar.quantityNumberStrNoun(grilledsteakAmount, "grillowany stek") + " z mojego grilla!");
								new SetQuestAndModifyKarmaAction(getSlotName(), "waiting;" 
										+ System.currentTimeMillis(), 10.0).fire(player, sentence, npc);
							}
						}));

		// player asks about quest or says coal when they are supposed to bring some coal and they don't have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("węgiel",25))),
				ConversationStates.ATTENDING,
				"Nie masz wystaczającej ilości węgla. Proszę idź i wydobądź kilka kawałków.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("węgiel","stone coal"),
				new QuestNotInStateCondition(QUEST_SLOT,"start"),
				ConversationStates.ATTENDING,
				"Czasami mógłbyś mi wyświadczyć #przysługę ...", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Węgiel do Grilla",
				"Haunchy Meatoch ma wątpliwości co do swojego ognia w grillu, jego zapas węgla może nie wystarczyć do usmażenia przepysznych steków.",
				true);
		offerQuestStep();
		bringCoalStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(npc.getName() + " powitał mnie na rynku w Ados.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Poprosił mnie o dostarzenie kilku kawałków węgla, ale nie mam czasu na ich zbieranie.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("Ze względu, że płomień w grillu jest bardzo mały to przyrzekłem " + npc.getName() + ", że pomogę mu zdobyć węgiel do grilla.");
		}
		if ("start".equals(questState) && player.isEquipped("węgiel",25) || isCompleted(player)) {
			res.add("Znalazłem 25 kawałków węgla dla " + npc.getName() + ". Sądzę, że się ucieszy.");
		}
		if (isCompleted(player)) {
			if (isRepeatable(player)) {
				res.add("Wziąłem 25 kawałków węgla do " + npc.getName() + ", ale założe się to mało i będze potrzebował więcej. Może wezmę więcej pszynych steków z grilla.");
			} else {
				res.add(npc.getName() + " był zadowolony, gdy dałem mu węgiel. Ma go teraz wystarczająco dużo. W zamian otrzymałem od niego kilka pysznych steków jakich w życiu nie jadłem!");
			}			
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Węgiel do Grilla";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;").fire(player, null, null);
	}
}
