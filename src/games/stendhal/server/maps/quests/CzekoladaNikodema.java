/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class CzekoladaNikodema extends AbstractQuest {

	private static final String QUEST_SLOT = "czekolada_nikodema";

	/** The delay between repeating quests. */
	private static final int REQUIRED_MINUTES = 90; // 1 godzina i 30 minut
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void chocolateStep() {
		final SpeakerNPC npc = npcs.get("Nikodem");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"Mam ochotę na trochę #'czekolady'.",
				null);

		npc.addReply(Arrays.asList("chocolate", "czekolada", "czekolady"), "Słyszałem, że czekolade można kupić w zakopiańskiej tawernie, ale jestem jeszcze za młody i to strasznie daleko jest...");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("tabliczka czekolady")),
				ConversationStates.QUESTION_1,
				"Wspaniale! Ta tabliczka czekolady jest dla mnie?",
				null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("tabliczka czekolady"))),
				ConversationStates.ATTENDING,
				"Mam nadzieje, że ktoś mi przyniesie tabliczkę czekolady... :(",
				null);

		// player is in another state like eating
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.ATTENDING,
				"Cześć.",
				null);

		// player rejected quest
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"Cześć.",
				null);

		// player asks about quest for first time (or rejected)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Chciałabym dostać tabliczkę czekolady. Chociaż jedną. Ciemno brązową lub słodką białą lub z posypką. Zdobędziesz jedną dla mnie?",
				null);

		// shouldn't happen
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Został mi jeszcze kawałek czekolady, którą mi przyniosłeś, dziękuję!",
				null);

		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Mam nadzieję, że jeżeli poproszę o następną tabliczkę czekolady to nie będę zbyt zachłanny. Czy mógłbyś zdobyć następną?",
				null);

		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"Zjadłem za dużo czekolady. Nie czuję się dobrze.",
				null);

		// player should be bringing chocolate not asking about the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"))),
				ConversationStates.ATTENDING,
				"Łaaaaaaaa! Gdzie jest moja czekolada ...",
				null);

		// Player agrees to get the chocolate
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Dobrze, może poproszę mame, aby mi kupiła tabliczkę czekolady...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player has got chocolate bar and spoken to mummy
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("tabliczka czekolady"));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "eating;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new InflictStatusOnNPCAction("tabliczka czekolady"));

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("tabliczka czekolady"),
				ConversationStates.ATTENDING,
				"Dziękuję BARDZO! Jesteś świetny.",
				new MultipleActions(reward));

		// player did have chocolate but put it on ground after question?
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("tabliczka czekolady")),
				ConversationStates.ATTENDING,
				"Hej gdzie jest moja czekolada?!",
				null);

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Łaaaaaa! Jesteś wielkim tłuściochem.",
				new DecreaseKarmaAction(5.0));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Czekolada dla Nikodema",
				"Słodka, słodka czekolada! Nikt nie może bez niej żyć! A Nikodem chciałby ją mieć...",
				true);
		chocolateStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Nikodem jest dobrym chłopcem, który bawi się z przyjaciółmi.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie będę przynosił mu czekolady.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("Nikodem chce dostać tabliczkę czekolady.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") && player.isEquipped("tabliczka czekolady") || isCompleted(player)) {
			res.add("Znalazłem pyszną tabliczkę czekolady dla Nikodema.");
		}
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("Przyniosłem trochę czekolady dla Nikodema. Może chciałby więcej czekolady.");
            } else {
                res.add("Nikodem je czekoladę, którą mu dałem.");
            }
		}
		return res;
	}
	@Override
	public String getName() {
		return "CzekoladaNikodema";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"eating;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"eating;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}
	@Override
	public String getNPCName() {
		return "Nikodem";
	}
}
