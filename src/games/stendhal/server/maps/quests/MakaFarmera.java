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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class MakaFarmera extends AbstractQuest {
	
	private static final int ILOSC_MAKI = 50;

	private static final String QUEST_SLOT = "maka";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
			res.add("Spotkałem farmera o imieniu Bruno, który znajduje się na plantacji zboża.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać farmerowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "aisha", "done")) {
			res.add("Obiecałem Bruno, że zaniosę zapasy mąki do krakowskiej piekarni");
		}
		if (questState.equals("aisha") && player.isEquipped("mąka",
				ILOSC_MAKI)
				|| questState.equals("done")) {
			res.add("Otrzymałem od Aishy zapasy mąki i teraz muszę zanieść to do Edny.");
		}
		if (questState.equals("aisha")
				&& !player.isEquipped("mąka", ILOSC_MAKI)) {
			res.add("O nie! Zgubiłem całą mąkę, którą miałem przynieść Ednie!");
		}
		if (questState.equals("done")) {
			res.add("Zaniosłem zapasy do piekarni oraz zostałem pochwalony przez Edne.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Farmer Bruno");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dzięki Tobie nasza piekarnia może dalej piec znakomite chleby i nie tylko.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, "Muszę zanieść mąkę do naszej #'piekarni', lecz nie mam na to czasu zbytnio. Pomógłbyś mi to zanieść?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Super! Podejdź do #'Aishy' i powiedz jej #'mąka' to Ci poda worki z mąką, które będziesz musiał zanieść do piekarni. Znajduje się ona na rynku zaraz pod Kościołem.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT,"start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED, 
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Cóż przynajmniej jesteś uczciwy, ponieważ od razu mi to powiedziałeś.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("aisha", "aishy"),
			null,
			ConversationStates.ATTENDING,
			"Znajduje się pod naszym młynem.",
			null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Aisha");

		final List<ChatAction> giveflour = new LinkedList<ChatAction>();
		giveflour.add(new EquipItemAction("mąka",ILOSC_MAKI, true));
		giveflour.add(new SetQuestAction(QUEST_SLOT, "aisha"));	
		
		npc.add(
			ConversationStates.ATTENDING,
			"mąka",
			new AndCondition(new GreetingMatchesNameCondition(getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING,
			"To ty jesteś od Bruno? Świetnie! W końcu ktoś zaniesie te worki z mąką... Proszę weź je i zanieś do #'Edny' oraz powiedz jej #'mąka', żeby wiedziała, że masz przy sobie naszą mąkę. Szybko, ruszaj!",
			new MultipleActions(giveflour));

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
					new QuestInStateCondition(QUEST_SLOT, "aisha")),
			ConversationStates.ATTENDING,
			"Na co zwlekasz? Ruszaj do #'Edny'.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("edna", "edny"),
			null,
			ConversationStates.ATTENDING,
			"Jest ona żoną Lanosza oraz zajmuje się wypiekaniem znakomitego chleba. Znajdziesz ją w piekarni na krakowskim rynku.",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Edna");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("mąka", ILOSC_MAKI));
		reward.add(new IncreaseXPAction(10000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));

		npc.add(
			ConversationStates.ATTENDING,
			"mąka",
			new AndCondition(new GreetingMatchesNameCondition(getName()),
				new QuestInStateCondition(QUEST_SLOT, "aisha"),
				new PlayerHasItemWithHimCondition("mąka", ILOSC_MAKI)),
			ConversationStates.ATTENDING,
			"Przyniosłeś mąkę! Wspaniale! Dziękujemy za pomoc. Myślałam, że nigdy zapasów mąki już nie dostaniemy...",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING,
			"mąka",
			new AndCondition(new GreetingMatchesNameCondition(getName()),
				new QuestInStateCondition(QUEST_SLOT, "aisha"),
				new NotCondition(new PlayerHasItemWithHimCondition("mąka", ILOSC_MAKI))),
			ConversationStates.ATTENDING,
			"Hej! Nie oszukuj mnie! Wiem, że miały dotrzeć do mnie zapasy mąki i gdzie to zniknęło?",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Zabierz mąkę do Edny",
			"Krakowska piekarnia potrzebuje zapasów mąki do pieczenia chleba.",
			false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "MakaFarmera";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}

	@Override
	public String getNPCName() {
		return "Farmer Bruno";
	}
}