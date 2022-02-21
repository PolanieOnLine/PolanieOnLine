/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
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

import games.stendhal.common.grammar.Grammar;
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
			"Okej. Wróć do mnie z #'pazurem zielonego smoka', #'pazurem czerwonego smoka' oraz #'pazurem niebieskiego smoka' mówiąc mi #naszynik.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Może następnym razem się zdecydujesz.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem zielonego smoka", "pazur zielonego smoka"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING, 
			"Zielony pazurek możesz złupić z zielonych smoków.",
			null);
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem czerwonego smoka", "pazur czerwonego smoka"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING, 
			"Zielony pazurek możesz złupić z czerwonych smoków.",
			null);
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pazurem niebieskiego smoka", "pazur niebieskiego smoka"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING, 
			"Zielony pazurek możesz złupić z niebieskich smoków.",
			null);
	}

	private void prepareBringingStep() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("necklace", "neck", "naszyjnik", "amulet", "przypomnij"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition("pazur zielonego smoka", 1),
				new PlayerHasItemWithHimCondition("pazur czerwonego smoka", 1),
				new PlayerHasItemWithHimCondition("pazur niebieskiego smoka", 1)),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Ooo... zdobyłeś pazurki. Chcesz, abym wykonał dla Ciebie ten naszyjnik?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("necklace", "neck", "naszyjnik", "amulet", "przypomnij"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(new AndCondition(
					new PlayerHasItemWithHimCondition("pazur zielonego smoka", 1),
					new PlayerHasItemWithHimCondition("pazur czerwonego smoka", 1),
					new PlayerHasItemWithHimCondition("pazur niebieskiego smoka", 1)))),
			ConversationStates.ATTENDING, 
			"Wróć jak zdobędziesz już wszystkie pazurki, o które Cię poprosiłem.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pazur zielonego smoka", 1));
		reward.add(new DropItemAction("pazur czerwonego smoka", 1));
		reward.add(new DropItemAction("pazur niebieskiego smoka", 1));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new EquipItemAction("smocze pazury", 1, true));

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
		res.add(Grammar.genderVerb(player.getGender(), "Rozmawiałem") + " z Robercikiem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie potrzebuję jakiegoś amuletu...");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(Grammar.genderVerb(player.getGender(), "Zgodziłem") + " się zebrać pazurki dla Robercika w zamian za amulet.");
		}
		if ("done".equals(questState)) {
			res.add(Grammar.genderVerb(player.getGender(), "Zaniosłem") + " potrzebne smocze pazurki, a w zamian " + Grammar.genderVerb(player.getGender(), "otrzymałem") + " smoczy amulet.");
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
