/***************************************************************************
 *                   (C) Copyright 2007-2021 - Stendhal                    *
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

import games.stendhal.common.Rand;
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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * @author KarajuSs
 */
public class LostClub extends AbstractQuest {
	private static final String QUEST_SLOT = "lost_club";
	private final SpeakerNPC npc = npcs.get("Dorian");

	private void createRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"*płacz* Pomożesz odnaleźć moją #'maczugę'? *płacz*",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Bardzo dziękuję! Nie myślałem, że ją jeszcze zobaczę! *uśmiech*",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*uśmiech* Dziękuję! Będą tutaj czekał za tobą.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*płacz*",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("club", "maczuga", "maczugę"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Chciałem pomóc swojemu tatusiowi w pokonywaniu potworów. Chcę zostać dzielnym wojem, ale zgubiłem swoją maczugę. *płacz* *pociągnięcie nosem* Pomożesz?",
			null);
	}

	private void createBringingStep() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("maczuga")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Znalazłeś moją maczugę? *pociągnięcie nosem*", null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("maczuga"))),
			ConversationStates.ATTENDING,
			"*pociągnięcie nosem* ALE... obiecałeś, że odnajdziesz moją maczugę!! *płacz*",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("maczuga"));
		reward.add(new IncreaseXPAction(300));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(new EquipItemAction("pączek", Rand.roll1D3()));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("maczuga"),
			ConversationStates.ATTENDING,
			"Dziękuję ci przyjacielu! Odnalazłeś moją maczugę! *uśmiech*",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*pociągnięcie nosem* *płacz*",
			null);
	}

	@Override
	public void addToWorld() {
    	fillQuestInfo(
			"Zagubiona Maczuga",
			"Dorian zgubił swoją maczugę i nie może jej samemu odnaleźć.",
			false);
		createRequestingStep();
		createBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Spotkałem") + " Doriana wędrującego między drzewami.");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Muszę znaleźć maczugę dla chłopca.");
		if ((player.isQuestInState(QUEST_SLOT, "start")
				&& player.isEquipped("maczuga"))
				|| player.isQuestCompleted(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Znalazłem") + " maczugę.");
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Dostarczyłem") + " maczugę Dorianowi. Poczęstował mnie pączkami.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Zagubiona Maczuga";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
