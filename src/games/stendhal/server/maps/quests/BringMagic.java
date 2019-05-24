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
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class BringMagic extends AbstractQuest {
	private static final String QUEST_SLOT = "bring_magic";
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Czarnoksiężnika w jakieś starej wieży.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem Czarnoksiężnikowi pomocy.");
		return res;
		}
		if ("done".equals(questState)) {
			res.add("Za pomoc w zadaniach otrzymałem wspaniały magiczny hełm od Czarnoksiężnika.");
		}
		return res;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new QuestCompletedCondition("kill_mountain_elves"),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.QUEST_3_OFFERED,
			"Mam nadzieje, że i z tym zadaniem sobie poradzisz... Musisz mi przynieść po 100 każdego rodzaju magii na świecie! Jeżeli chcesz mogę Tobie podać dokładną #'listę' co masz mi przynieść. Zrobisz to?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"To nie wszystko...", null);

		npc.add(
			ConversationStates.QUEST_3_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze. Wróć jak przyniesiesz wszystko to co potrzebuję...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		npc.add(ConversationStates.QUEST_3_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"No cóż... już na początku wiedziałem, że się nie nadasz.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
		
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("lista", "listę", "list"),
			new QuestCompletedCondition("kill_mountain_elves"),
			ConversationStates.ATTENDING,
			"\nOto lista potrzebnych mi przedmiotów: "
				+ "\n100 magia ziemi,"
				+ "\n100 magia płomieni,"
				+ "\n100 magia deszczu,"
				+ "\n100 magia mroku"
				+ "\noraz 100 magii światła."
				+ " Masz mi to przynieść.", null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("magia ziemi", 100));
		reward.add(new DropItemAction("magia płomieni", 100));
		reward.add(new DropItemAction("magia deszczu", 100));
		reward.add(new DropItemAction("magia mroku", 100));
		reward.add(new DropItemAction("magia światła", 100));
		reward.add(new IncreaseXPAction(50000));
		reward.add(new SetQuestAction(QUEST_SLOT, "helmet"));
		reward.add(new IncreaseKarmaAction(10));

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("magia ziemi", 100),
					new PlayerHasItemWithHimCondition("magia płomieni", 100),
					new PlayerHasItemWithHimCondition("magia deszczu", 100),
					new PlayerHasItemWithHimCondition("magia mroku", 100),
					new PlayerHasItemWithHimCondition("magia światła", 100)),
			ConversationStates.ATTENDING, 
			"Dziękuję, że przyniosłeś dla mnie magię. Teraz jeżeli chcesz otrzymać lepszą nagrodę to przynieś mi #'hełm kolczy', który otrzymałeś wcześniej ode mnie.",
			new MultipleActions(reward));

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
						new AndCondition(new PlayerHasItemWithHimCondition("magia ziemi", 100),
								new PlayerHasItemWithHimCondition("magia płomieni", 100),
								new PlayerHasItemWithHimCondition("magia deszczu", 100),
								new PlayerHasItemWithHimCondition("magia mroku", 100),
								new PlayerHasItemWithHimCondition("magia światła", 100)))),
			ConversationStates.ATTENDING, 
			"Słuchaj... Wiem, że nie masz przy sobie tej magii, o którą Ciebie prosiłem... Potrafię to wyczuć.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("hełm kolczy"),
			new AndCondition(
					new NotCondition(new PlayerHasItemWithHimCondition("hełm kolczy"))),
			ConversationStates.ATTENDING,
			"Hełm kolczy jest doskonałą ochroną dla twojej głowy. Jeżeli miałbym go ulepszyć to lepiej żebyś mi go przyniósł.",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "helmet"),
					new PlayerHasItemWithHimCondition("hełm kolczy")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Oddasz mi ten #'hełm kolczy' na chwilę?", null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "helmet"),
					new NotCondition(new PlayerHasItemWithHimCondition("hełm kolczy"))),
			ConversationStates.ATTENDING, 
			"Nie masz przy sobie #'hełmu kolczego'. Chcesz otrzymać wyjątkową nagrodę ode mnie? To lepiej żebyś go teraz przyniósł. Nie mam wiecznie czasu na Twoje problemy...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("hełm kolczy"));
		reward.add(new EquipItemAction("magiczny hełm kolczy", 1, true));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(20));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "helmet"),
					new PlayerHasItemWithHimCondition("hełm kolczy")),
			ConversationStates.ATTENDING,
			"Dobrze, więc proszę! Wykorzystałem część magii, której mi przyniosłeś, aby zaczarować ten hełm kolczy! Stał się teraz bardziej odporny na siły natury.",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "helmet"),
			ConversationStates.ATTENDING,
			"Nie chcesz otrzymać wyjątkowej nagrody za pomoc, to nie, trudno.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Magia dla Czarnoksiężnika",
				"Ostatnie zadania to przyniesienie każdego rodzaju magii dla Czarnoksiężnika.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public int getMinLevel() {
		return 120;
	}
	
	@Override
	public String getName() {
		return "BringMagic";
	}
	
	@Override
	public String getNPCName() {
		return "Czarnoksiężnik";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}