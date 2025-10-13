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
import java.util.Map;

import games.stendhal.common.NotificationType;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: CrownForTheWannaBeKing
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Ivan Abe, the wannabe king who lives in Sedah</li>
 * <li> Kendra Mattori, priestess living in Magic City</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Ivan Abe wants you to bring him 2 carbuncles, 2 diamonds, 4 emeralds, 2 gold bars, 1 obsidian, and 3 sapphires for his crown which he
 *      believes will help him to become the new king.</li>
 * <li> Kendra Mattori gives the reward after player brought all required items.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 100,000 XP</li>
 * <li> karma (100) </li>
 * <li> Player's ATK XP is increased by 0.1% of his/her XP.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class CrownForTheWannaBeKing extends AbstractQuest {
	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "sztabka złota=2;szmaragd=4;szafir=3;rubin=2;diament=2;obsydian=1";

	/**
	 * Name of the main NPC for this quest.
	 */
	private static final String NPC_NAME = "Ivan Abe";

	/**
	 * Name of the NPC giving the reward.
	 */
	private static final String REWARD_NPC_NAME = "Kendra Mattori";

	/**
	 * Name of the slot used for this quest.
	 */
	private static final String QUEST_SLOT = "crown_for_the_wannabe_king";

	/**
	 * how much ATK XP is given as the reward: formula is player's XP *
	 * ATK_BONUS_RATE ie. 0.001 = 0.1% of the player's XP
	 */
	private static final double ATK_REWARD_RATE = 0.002;

	/**
	 * how much XP is given as the reward.
	 */
	private static final int XP_REWARD = 100000;

	/**
	 * initialize the introduction and start of the quest.
	 */
	private void step_1() {
		final SpeakerNPC npc = npcs.get(NPC_NAME);
		npc.addOffer("Niczego nie sprzedaję!");
		npc.addGoodbye();
		npc.addJob("Moja praca nie jest ważna, ponieważ będę królem Kalavan!");

		/* player says hi before starting the quest */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Pozdrawiam. Szybko, jaką masz do mnie sprawę, bo mam dużo pracy do zrobienia."
					+ ", a następnym razem wyczyść swoje buty. Masz szczęście, że nie jestem królem...jeszcze!",
				null);

		npc.addQuest("Hmm mógłbyś się przydać w moim #planie...");
		npc.addReply(Arrays.asList("plan", "planie"),
					"Wkrótce zdetronizuję króla Kalavan i zostanę nowym królem! Teraz potrzebuję nowej #korony dla siebie.");

		/* player says crown */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("crown", "korony"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Tak, potrzebuję klejnotów i złota na moją nową koronę. Pomożesz mi?",
				null);

		/* player says yes */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, null,
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5),
								    new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Chcę, aby moja korona była piękna i lśniąca. Potrzebuję [items]. " +
										"Masz coś z tego przy sobie?")));


		/* player is not willing to help */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.IDLE,
				"Och. Nie chcesz mi pomóc?! Wynoś się stąd marnujesz mój cenny czas!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	/**
	 * Initializes the main part of the quest.
	 */
	private void step_2() {
		final SpeakerNPC npc = npcs.get(NPC_NAME);

		/* player returns while quest is still active */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "reward")),
				ConversationStates.QUESTION_1,
				"Och to znowu ty. Czy przyniosłeś jakieś #przedmioty do mojej korony?",
				null);

		/* player asks what exactly is missing (says items) */
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("items", "przedmioty"), null,
				ConversationStates.QUESTION_1, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Przyniosłeś coś z tego?"));


		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, "Świetnie, co przyniosłeś?",
				null);

		ChatAction completeAction = new  MultipleActions(
											new SetQuestAction(QUEST_SLOT, "reward"),
											new SayTextAction("Służyłeś mi dobrze. Moja korona będzie najwspanialsza spośród wszystkich!"
											+ " Spotkaj się z "+ REWARD_NPC_NAME+ " w mieście magów, aby odebrać swoją #nagrodę."),
											new IncreaseXPAction(XP_REWARD)
											);
		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_1, item.getKey(), null,
					ConversationStates.QUESTION_1, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"Dobra, masz coś jeszcze?","Już to przynisłeś!",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "reward")),
				ConversationStates.IDLE,
				"Cóż, nie wracaj dopóki nie znajdziesz czegoś dla mnie!", null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "reward")),
				ConversationStates.IDLE,
				"Do widzenia. Wróć gdy zdobędziesz to czego potrzebuję!", null);


		/* player says reward */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("reward", "nagroda", "nagrodę"), null,
				ConversationStates.IDLE, "Tak jak powiedziałem znajdź kapłankę " + REWARD_NPC_NAME
					+ " w świątyni w mieście czarodziejów. Ona da Ci nagrodę. Teraz idź już jestem zajęty!",
				null);

		/*
		 * player returns after finishing the quest or before collecting the
		 * reward
		 */
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new QuestInStateCondition(QUEST_SLOT, "reward"))),
				ConversationStates.IDLE,
				"Moja nowa korona będzie wkrótce gotowa i zdetronizuję króla! Uhahaha!",
				null);
	}

	/**
	 * initialize the rewarding NPC.
	 */
	private void step_3() {
		final SpeakerNPC npc = npcs.get(REWARD_NPC_NAME);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("reward", "nagroda", "nagrodę"),
				new QuestInStateCondition(QUEST_SLOT, "reward"),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Och tak "
									+ NPC_NAME
									+ " powiedział mi, abym Cię dobrze wynagrodziła! Mam nadzieję, że ucieszysz się ze swoich podniesionych umiejętności walki!");
						rewardPlayer(player);
						player.setQuest(QUEST_SLOT, "done");
					}
				});
	}

	/**
	 * Give the player the reward for completing the quest.
	 *
	 * @param player
	 */
	protected void rewardPlayer(final Player player) {
		player.addKarma(100.0);
		final long reward = Math.round(player.getXP() * ATK_REWARD_RATE);
		final int cappedReward = (int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, reward));
		player.setAtkXP(player.getAtkXP() + cappedReward);
		player.incAtkXP();
		player.sendPrivateText(NotificationType.POSITIVE, "Otrzymałeś " + Long.toString(reward) + " punktów doświadczenia ataku.");
	}

	@Override
	public void addToWorld() {
		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Korona Niedoszłego Króla",
				"Ivan Abe chce rządzić Kalavan... i potrzebuje korony.",
				false);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(NPC_NAME + " poprosił mnie, abym pomógł mu zdetronizować króla Cozarta. by być królem potrzebuje korony.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Myślę, że angażowanie się w politykę to zły pomysł.");
		} else if (!"done".equals(questState) && !"reward".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add(Grammar.enumerateCollection(missingItems.toStringList()) + " są nadal potrzebne do korony.");
		} else if ("reward".equals(questState)) {
			res.add(player.getGenderVerb("Oddałem") + " drogocenne kamienie do korony, a " + REWARD_NPC_NAME + " została poinformowana aby dać mi nagrodę.");
		} else {
			res.add(player.getGenderVerb("Oddałem") + " drogocenne kamienie do korony, a " + REWARD_NPC_NAME + " nagrodziła mnie, podnosząc moje zdolności ataku.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Korona Niedoszłego Króla";
	}

	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getNPCName() {
		return NPC_NAME;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CAVES;
	}
}
