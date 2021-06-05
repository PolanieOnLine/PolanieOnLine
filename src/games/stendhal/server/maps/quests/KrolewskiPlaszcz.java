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
		String text = "I ja mam Tobie zaufać? Gdzie pomogłeś mojemu ludowi?!";

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
											raiser.say("Potrzebuję nowego królewskiego płaszcza. Potrzebuję od Ciebie #'czarnego płaszcza smoczego' i to 10 sztuk! W nagrodę otrzymasz ode mnie status szlachcica w na tych ziemiach. Pomożesz?");
											raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
										}
									} else {
										npc.say(text + " Poszukaj małego chłopca o imieniu Leo, zgubił swojego ulubionego pluszaka i nie może odzyskać!");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say(text + " Poszukaj dziewczynki o imieniu Balbina, potrzebujego płaszcza do spełnienia swego marzenia!");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say(text + " Poszukaj rybaka o imieniu Thomas, potrzebuje pomocy przy naprawie jego łódki!");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say(text + " Poszukaj Farmera Bruno, bo od kilku tygodni nie można kupić nawet jednego chleba!");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Moja armia królewska potrzebuje wyposażenia! Nasz gwardzista złożył zamówienie u miejskiego kowala! Lepiej mu pomóż jeśli Ci na tym zależy.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			"Jako władca ziem Polan, dziękuję Ci za pomoc!",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Świetnie... Będę za tobą czekał mieszczaninie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Może nie zasługujesz na miano szlachcica... Precz!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
	}

	private void done() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("czarny płaszcz smoczy",10)),
			ConversationStates.ATTENDING,
			"Oto i przybył mój wysłannik za smoczymi płaszczami. Czuję, iż coś masz dla mnie, prawda?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("czarny płaszcz smoczy",10))),
			ConversationStates.IDLE,
			"Nie będę się powtarzał...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("czarny płaszcz smoczy",10));
		reward.add(new IncreaseXPAction(100000));
		reward.add(new EquipItemAction("tarcza cieni", 1, true));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Dziękuję Ci za pomoc! Teraz mój królewski krawiec uszyje dla mnie nowy płaszcz.",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Chyba o coś Ciebie prosiłem, prawda?",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Królewski Płaszcz",
				"Król Krak - władca ziem Polan potrzebuje nowego królewskiego płaszcza.",
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
		res.add("Rozmawiałem z królem Krakiem. Za pomoc obiecał nadać mi status szlachcica.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie zamierzam pomóc królowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Postanowiłem pomóc królowi w wykonaniu nowego płaszcza.");
		}
		if ("start".equals(questState) && player.isEquipped("płaszcz czarnego smoka", 10) || "done".equals(questState)) {
			res.add("Posiadam już 10 płaszczy dla Króla Kraka.");
		}
		if ("done".equals(questState)) {
			res.add("Oddałem potrzebne przedmioty do wykonania królewskiego płaszcza.");
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
