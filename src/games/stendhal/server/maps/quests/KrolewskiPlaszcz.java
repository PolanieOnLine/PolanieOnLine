/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class KrolewskiPlaszcz extends AbstractQuest {
	public static final String QUEST_SLOT = "krolewski_plaszcz";
	private final SpeakerNPC npc = npcs.get("Król Krak");

	private static final String UZBROJENIE = "zamowienie_strazy";
	private static final String LUD1 = "maka";
	private static final String LUD2 = "naprawa_lodzi";
	private static final String LUD3 = "plaszcz_kapturka";
	private static final String LUD4 = "zabawka_leo";

	private void start() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(UZBROJENIE)) {
						if (player.isQuestCompleted(LUD1)) {
							if (player.isQuestCompleted(LUD2)) {
								if (player.isQuestCompleted(LUD3)) {
									if (player.isQuestCompleted(LUD4)) {
										if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
											raiser.say("Wieści o twojej pomocy dotarły na mój dwór. Udowodniłeś, że potrafisz służyć ludziom, a nie tylko własnej sławie. Mój królewski krawiec ma przygotować nowy płaszcz ceremonialny. Potrzebuje dziesięciu #'czarnych płaszczy smoczych', ponieważ wybierze z nich tylko najlepiej zachowane fragmenty skóry i łusek. Jeśli je zdobędziesz, nadam ci szlachectwo ziem Kraka i wręczę nagrodę z królewskiej zbrojowni. Pomożesz?");
											raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
										}
									} else {
										npc.say("Zanim powierzę ci sprawę mojego dworu, pokaż, że dbasz o mieszkańców. Poszukaj chłopca o imieniu Leo. Zgubił swojego ulubionego pluszaka i potrzebuje pomocy.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Zanim powierzę ci sprawę mojego dworu, pokaż, że dbasz o mieszkańców. Poszukaj Balbiny i pomóż jej spełnić marzenie związane z płaszczem.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Zanim powierzę ci sprawę mojego dworu, pokaż, że dbasz o mieszkańców. Rybak Tomasz potrzebuje pomocy przy naprawie łodzi.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Zanim powierzę ci sprawę mojego dworu, pokaż, że dbasz o mieszkańców. Farmer Bruno ma problem z dostawami mąki, przez co brakuje chleba.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Najpierw pomóż królewskiej straży. Gwardzista złożył zamówienie u miejskiego kowala i bez twojej pomocy żołnierze nie otrzymają potrzebnego wyposażenia.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Szlachectwo, które ci nadałem, pozostaje świadectwem twojej służby mieszkańcom i Koronie. Noś ten zaszczyt godnie.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Dobrze. Wróć, gdy zdobędziesz dziesięć czarnych płaszczy smoczych. Krawiec wybierze z nich najlepszy materiał.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Rozumiem. Szlachectwo wymaga gotowości do służby. Wróć, jeśli zmienisz zdanie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
	}

	private void done() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj ponownie, szlachcicu. Pamiętam twoją służbę moim ludziom i dworowi.",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("czarny płaszcz smoczy", 10)),
			ConversationStates.ATTENDING,
			"Widzę, że przyniosłeś smocze płaszcze. Czy mam przekazać je królewskiemu krawcowi?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("czarny płaszcz smoczy", 10))),
			ConversationStates.ATTENDING,
			"Królewski krawiec wciąż czeka na dziesięć czarnych płaszczy smoczych. Wróć, gdy zbierzesz cały materiał.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("czarny płaszcz smoczy", 10));
		reward.add(new IncreaseXPAction(100000));
		reward.add(new EquipItemAction("tarcza cieni", 1, true));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Doskonale. Krawiec wybierze z tych płaszczy najlepiej zachowane fragmenty i przygotuje strój godny Korony. Za twoją służbę moim ludziom i dworowi nadaję ci szlachectwo ziem Kraka. Przyjmij także tarczę cieni z królewskiej zbrojowni. Od dziś na moim dworze będziesz witany jako szlachcic.",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Dobrze. Zachowaj płaszcze i wróć, gdy będziesz gotowy przekazać je krawcowi.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Królewski Płaszcz",
				"Król Krak potrzebuje najlepszego smoczego materiału na ceremonialny płaszcz. Pomoc dla jego ludzi może otworzyć drogę do królewskiego szlachectwa.",
				false);
		start();
		done();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " z królem Krakiem. Uznał moje wcześniejsze czyny za dowód, że można mi powierzyć sprawę królewskiego dworu.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add(player.getGenderVerb("Odmówiłem") + " zdobycia materiału na królewski płaszcz.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(player.getGenderVerb("Zgodziłem się") + " zdobyć 10 czarnych płaszczy smoczych. Królewski krawiec wybierze z nich najlepiej zachowane fragmenty skóry i łusek.");
		}
		if (("start".equals(questState) && player.isEquipped("czarny płaszcz smoczy", 10)) || "done".equals(questState)) {
			res.add("Mam już 10 czarnych płaszczy smoczych potrzebnych królewskiemu krawcowi.");
		}
		if ("done".equals(questState)) {
			res.add(player.getGenderVerb("Przekazałem") + " płaszcze królowi Krakowi. W nagrodę otrzymałem tarczę cieni, a Krak nadał mi szlachectwo za służbę mieszkańcom i dworowi.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Królewski Płaszcz";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}

	@Override
	public String getNPCName() {
		return "Król Krak";
	}
}
