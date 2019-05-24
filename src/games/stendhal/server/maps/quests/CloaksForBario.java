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
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Cloaks for Bario
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Bario, a guy living in an underground house deep under the Ados Wildlife Refuge</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Bario asks you for a number of blue elf cloaks.</li>
 * <li> You get some of the cloaks somehow, e.g. by killing elves.</li>
 * <li> You bring the cloaks to Bario and give them to him.</li>
 * <li> Repeat until Bario received enough cloaks. (Of course you can bring up
 * all required cloaks at the same time.)</li>
 * <li> Bario gives you a golden shield in exchange.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> golden shield</li>
 * <li> 80000 XP</li>
 * <li> Karma: 25</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class CloaksForBario extends AbstractQuest {

	private static final int REQUIRED_CLOAKS = 10;

	private static final String QUEST_SLOT = "cloaks_for_bario";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player says hi before starting the quest
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Hej! Jak się tutaj dostałeś? Co zrobiłeś? Ha. Cóż jestem Bario. Przypuszczam, że nie zrobiłbyś dla mnie drobnego #'zadania'.",
				null);

		// player is willing to help
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Nigdy nie zamierzam wyjść na górę, ponieważ ukradłem beczkę piwa należącą do krasnali. Tutaj jest strasznie zimno... Pomożesz mi?",
				null);

		// player should already be getting cloaks
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Obiecałeś, że przyniesiesz mi lazurowe płaszcze elfickie. Pamiętasz?",
				null);

		// player has already finished the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Nie mam żadnego zadania dla Ciebie.", null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Potrzebuję lazurowych płaszczy elfickich jeżeli chcę przeżyć zimę. Przynieś mi dziesięć, a dam Ci nagrodę.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, Integer.toString(REQUIRED_CLOAKS), 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och nie... Będę miał kłopoty...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		// Just find some of the cloaks somewhere and bring them to Bario.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Bario");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Witaj znowu! Wciąż potrzebuję "
							+ player.getQuest(QUEST_SLOT)
							+ " lazurowy płaszcz elficki "
							+ Grammar.plnoun(
									MathHelper.parseInt(player.getQuest(QUEST_SLOT)),
									"cloak") + ". Masz jakiś dla mnie?");
					}
				});

		// player returns after finishing the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj! Jeszcze raz dziękuję za te płaszcze.", null);

		// player says he doesn't have any blue elf cloaks with him
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Niedobrze.", null);

		// player says he has a blue elf cloak with him but he needs to bring more than one still
		// could also have used GreaterThanCondition for Quest State but this is okay, note we can only get to question 1 if we were active
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
				ConversationStates.QUESTION_1, null,
				new MultipleActions(
						new DropItemAction("lazurowy płaszcz elficki"),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								// find out how many cloaks the player still has to
								// bring. incase something has gone wrong and we can't parse the slot, assume it was just started
								final int toBring = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_CLOAKS) -1;

								player.setQuest(QUEST_SLOT,
										Integer.toString(toBring));
								raiser.say("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję "
										+ Grammar.quantityplnoun(toBring,
												"cloak", "jeden") + ".");

							}
						}));
		
		// player says he has a blue elf cloak with him and it's the last one
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("lazurowy płaszcz elficki"));
		reward.add(new EquipItemAction("złota tarcza", 1, true));
		reward.add(new IncreaseXPAction(80000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(25));
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "1"), new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
				ConversationStates.ATTENDING,
				"Dziękuję bardzo! Mam teraz odpowiednio dużo płaszczy, aby przetrwać zimę. Weź tę złotą tarcze jako nagrodę.",
				new MultipleActions(reward));
		
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				new NotCondition(new PlayerHasItemWithHimCondition("lazurowy płaszcz elficki")),
				ConversationStates.ATTENDING, 
				"Naprawdę? Nie widzę żadnego...", 
				null);
	}

	@Override
	public void addToWorld() {
		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Płaszcze dla Bario",
				"Bario, potrzebuję płaszczy aby ogrzać się.",
				false);
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem zmarźniętego krasnala, który ukrywa się pod ziemią w Ados Outside NW. Poprosił mnie, żebym przyniósł mu 10 lazurowych płaszczy elfickich.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę, pomóc Bario..");
		} else if (!questState.equals("done")) {
			int cloaks = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT),  REQUIRED_CLOAKS);
			res.add("Muszę przynieść Bario " + Grammar.quantityplnoun(cloaks, "lazurowy płaszcz elficki", "one") + "." );
		} else {
			res.add("Bario dał mi cenną złotą tarczę w zamian za płaszcze!");
		}
		return res;
	}
	
	@Override
	public String getName() {
		return "CloaksForBario";
	}
	
	@Override
	public int getMinLevel() {
		return 20;
	}
	@Override
	public String getNPCName() {
		return "Bario";
	}
}
