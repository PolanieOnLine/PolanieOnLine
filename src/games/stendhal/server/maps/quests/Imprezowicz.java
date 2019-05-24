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
import games.stendhal.server.entity.npc.action.DropItemAction;
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

public class Imprezowicz extends AbstractQuest {
	public static final String QUEST_SLOT = "wino_dla_imprezowicza";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Cadhesem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę dać wina dla Cadhesa");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Dam Cadhesowi napój z winogron.");
		}
		if ("start".equals(questState) && player.isEquipped("napój z winogron")
				|| "done".equals(questState)) {
			res.add("Mam wino dla Cadhesa.");
		}
		if ("done".equals(questState)) {
			res.add("Dałem wino Cadhesowi.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Cadhes");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"*hicks* Wisz trochu mi skoczyło si #'wino'. Cz mozes mi po ni skoczyć *hicks*?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"*hicks* Dzikuję! *hicks*",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ja się *hicks* nigdiie nie *hicks* ruszam.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*hicks* OK. *hicks* Sam sobi pu ni pój *hicks* dę!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("wino", "napój z winogron"),
			null,
			ConversationStates.QUEST_OFFERED,
			"*hicks* Wy *hicks* starcy mi je *hicks* den kiliszek! Zrobis to da mne? *hicks*",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Cadhes");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new PlayerHasItemWithHimCondition("napój z winogron")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Hej! *hicks* Cy to wino jet dl mie? *hicks* *hicks*", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("napój z winogron"))),
			ConversationStates.ATTENDING, 
			"Hej, wciąż czekam na mje wino, pamintas? *hicks*",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("napój z winogron"));
		reward.add(new IncreaseXPAction(650));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(10));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("napój z winogron"),
			ConversationStates.ATTENDING,
			"*gul gul* Ach! Trafieś *hicks*!",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*hicks* Chya pamitas, że prosiem Cę o coś, prada? *hicks* Mógbym trazz napić si. *hicks*",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Wino dla Cadhesa",
				"Jeden z imprezowiczów o imieniu Cadhes potrzebuje wina.",
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
		return "Imprezowicz";
	}

	public String getTitle() {
		
		return "Wino dla Cadhesa";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}
	
	@Override
	public String getNPCName() {
		return "Cadhes";
	}
}