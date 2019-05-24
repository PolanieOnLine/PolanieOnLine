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
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
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
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Beer For Hayunn
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Hayunn Naratha (the veteran warrior in Semos)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Hayunn asks you to buy a beer from Margaret.</li>
 * <li>Margaret sells you a beer.</li>
 * <li>Hayunn sees your beer, asks for it and then thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>20 gold coins</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class BeerForHayunn extends AbstractQuest {
	public static final String QUEST_SLOT = "beer_hayunn";
	private static final String OTHER_QUEST_SLOT = "meet_hayunn";

	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Hayunn.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę dać soku z chmielu dla Hayunn.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Dam Hayunn sok z chmielu.");
		}
		if ("start".equals(questState) && player.isEquipped("sok z chmielu")
				|| "done".equals(questState)) {
			res.add("Mam sok z chmielu.");
		}
		if ("done".equals(questState)) {
			res.add("Dałem sok z chmielu Hayunn. Zapłacił mi 20 złotych monet i 50 pd.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Hayunn Naratha");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			// Don't give the task until the previous is completed to avoid
			// confusing Hayunn in a lot of places later.
			new AndCondition(new QuestNotCompletedCondition(QUEST_SLOT),
					new QuestCompletedCondition(OTHER_QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED, 
			"Zaschło mi w gardle, ale nie mogę opuścić mojego posterunku! Czy mógłbyś mi przynieść #sok z chmielu z #oberży?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dziękuję zawsze to samo, ale nie chcę mi się pić. Wciąż jestem na służbie! Będę potrzebował trzeźwego umysłu, gdy pokażą się potwory...",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję! Będę tutaj czekał. I oczywiście strzegł.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och, zapomnij o tym. Mam nadzieję, że zacznie padać deszcz, a wtedy będę mógł się napić.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("tavern", "oberży"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Jeżeli nie wiesz gdzie jest hotel, to możesz się zapytać starego Monogenesa. Jest dobry w udzielaniu wskazówek. Zamierzasz pomóc?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("sok", "sok z chmielu"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Wystarczy mi butelka chłodnego soku z chmielu od #Margaret. Zrobisz to dla mnie?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"Margaret",
			null,
			ConversationStates.QUEST_OFFERED,
			"Margaret jest piękną kelnerką w oberży! Ładnie wygląda... heh. Pójdziesz dla mnie do niej?",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Hayunn Naratha");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new PlayerHasItemWithHimCondition("sok z chmielu")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Hej! Czy ten sok z chmielu jest dla mnie?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("sok z chmielu"))),
			ConversationStates.ATTENDING, 
			"Hej, wciąż czekam na sok z chmielu, pamiętasz? Poza tym co mogę zrobić dla Ciebie?",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sok z chmielu"));
		reward.add(new EquipItemAction("money", 100));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("sok z chmielu"),
			ConversationStates.ATTENDING,
			"*gul gul* Ach! Trafiłeś. Daj znać jeżeli będziesz czegoś potrzebował, dobrze?",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Niech to! Chyba pamiętasz, że prosiłem Cię o coś, prawda? Mógłbym teraz napić się.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Piwo dla Hayunna",
				"Hayunn Naratha największy wojownik w Semos Guard House potrzebuje soku z chmielu.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "BeerForHayunn";
	}

	public String getTitle() {
		
		return "Piwo dla Hayunn";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	
	@Override
	public String getNPCName() {
		return "Hayunn Naratha";
	}
}
