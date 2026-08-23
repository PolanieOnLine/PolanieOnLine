/***************************************************************************
 *                 (C) Copyright 2019-2026 - PolanieOnLine                 *
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

import games.stendhal.common.constants.ItemRarity;
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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

public class SmoczyAmulet extends AbstractQuest {
	public static final String QUEST_SLOT = "dragon_amulet";
	private static final String GREEN_CLAW = "pazur zielonego smoka";
	private static final String RED_CLAW = "pazur czerwonego smoka";
	private static final String BLUE_CLAW = "pazur niebieskiego smoka";
	private final SpeakerNPC npc = npcs.get("Robercik");

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Przynieś mi 3 różne smocze pazurki, a zrobię z nich naszyjnik dla Ciebie, który będzie cię chronił. Jesteś zainteresowany?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Już wykonałem dla Ciebie naszyjnik.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Okej. Wróć do mnie z #'pazurem zielonego smoka', #'pazurem czerwonego smoka' oraz #'pazurem niebieskiego smoka' i powiedz #naszyjnik.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Może następnym razem się zdecydujesz.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem zielonego smoka", GREEN_CLAW),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Zielony pazurek możesz złupić z zielonych smoków.",
			null);
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem czerwonego smoka", RED_CLAW),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Czerwony pazurek możesz złupić z czerwonych smoków.",
			null);
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem niebieskiego smoka", BLUE_CLAW),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Niebieski pazurek możesz złupić z niebieskich smoków.",
			null);
	}

	private void prepareBringingStep() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("necklace", "neck", "naszyjnik", "amulet", "przypomnij"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition(GREEN_CLAW, 1),
				new PlayerHasItemWithHimCondition(RED_CLAW, 1),
				new PlayerHasItemWithHimCondition(BLUE_CLAW, 1)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Ooo... zdobyłeś pazurki. Chcesz, abym wykonał dla Ciebie ten naszyjnik?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("necklace", "neck", "naszyjnik", "amulet", "przypomnij"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(new AndCondition(
					new PlayerHasItemWithHimCondition(GREEN_CLAW, 1),
					new PlayerHasItemWithHimCondition(RED_CLAW, 1),
					new PlayerHasItemWithHimCondition(BLUE_CLAW, 1)))),
			ConversationStates.ATTENDING,
			"Wciąż potrzebuję trzech konkretnych pazurów: zielonego, czerwonego i niebieskiego smoka. Wróć z kompletem.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction(GREEN_CLAW, 1));
		reward.add(new DropItemAction(RED_CLAW, 1));
		reward.add(new DropItemAction(BLUE_CLAW, 1));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new EquipItemAction("smocze pazury", 1, true, ItemRarity.EPIC));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Poczekaj chwilkę... Trochę sznurka przeciągnę.. ym.. No i proszę! Oto twój naszyjnik.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Twoja decyzja.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Smoczy Amulet",
				"Młody chłopiec wykona amulet, który będzie chronił przed złymi smokami.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " z Robercikiem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie potrzebuję jakiegoś amuletu...");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(player.getGenderVerb("Zgodziłem") + " się zebrać dla Robercika trzy różne pazury: pazur zielonego smoka, pazur czerwonego smoka i pazur niebieskiego smoka.");
		}
		if ("done".equals(questState)) {
			res.add(player.getGenderVerb("Zaniosłem") + " potrzebne smocze pazurki, a w zamian " + player.getGenderVerb("otrzymałem") + " smoczy amulet.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Smoczy Amulet";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
