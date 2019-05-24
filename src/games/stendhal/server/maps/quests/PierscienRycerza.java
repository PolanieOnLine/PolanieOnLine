/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
// Based on UltimateCollector
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

	public class PierscienRycerza extends AbstractQuest {

	private static final String QUEST_SLOT = "pierscien_rycerza";

	private static final String MITHRILSHIELD_QUEST_SLOT = "mithrilshield_quest";  

	private static Logger logger = Logger.getLogger(PierscienRycerza.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void checkLevelHelm() {
		final SpeakerNPC npc = npcs.get("Edgard");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()){ 
						raiser.say("Z twej ręki zginął rycerz! Nie masz tu czego szukać, pozbądź się piętna czaszki. A teraz precz mi z oczu!");
					} else {
							if (player.getLevel() >= 250) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Czyżbyś chciał zdobyć pierścień rycerza? Jesteś zainteresowany?");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Jeżeli przyszedłeś po następny pierścień to zapomnij. A teraz nie przeszkadzaj mi w trenowaniu latania.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else  {
								npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 250 lvl.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
					}
				}
			}); 

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Ale wpierw sprawdzę czy masz wszystkie zadania zrobione nim dostaniesz #listę rzeczy, krórych potrzebuję.");
					player.addKarma(10);
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Nie to nie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

	}

	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("Edgard");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MITHRILSHIELD_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj przyjacielu. Czyż byś chciał dostać #zadanie na pierścień rycerza?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("wyposażenie", "wyposazenie","listę"), 
			new AndCondition(
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(MITHRILSHIELD_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Najpierw zrób zadanie na tarczę z mithrilu, wtedy porozmawiamy o pierścieniu.",
			null);
	}

	private void requestItem() {
		final SpeakerNPC npc = npcs.get("Edgard");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("wyposażenie", "wyposazenie","listę"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(MITHRILSHIELD_QUEST_SLOT)),
					ConversationStates.ATTENDING, "Pierścień zdobędziesz przynosząc mi potrzebne #przedmioty.",
					new SetQuestAction(QUEST_SLOT, "przedmioty" ));

		final List<ChatAction> monetaactions = new LinkedList<ChatAction>();
		monetaactions.add(new DropItemAction("żelazo",40));
		monetaactions.add(new DropItemAction("sztabka złota",40));
		monetaactions.add(new DropItemAction("bryłka mithrilu",30));
		monetaactions.add(new DropItemAction("pierścień mieszczanina",1));
		monetaactions.add(new DropItemAction("tarcza z czaszką",1));
		monetaactions.add(new DropItemAction("spodnie elfickie",1));
		monetaactions.add(new EquipItemAction("pierścień rycerza", 1, true));
		monetaactions.add(new IncreaseXPAction(100000));
		monetaactions.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wyposażenie", "wyposazenie","przedmioty"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT,"przedmioty"),
			new PlayerHasItemWithHimCondition("żelazo",40),
			new PlayerHasItemWithHimCondition("sztabka złota",40),
			new PlayerHasItemWithHimCondition("bryłka mithrilu",30),
			new PlayerHasItemWithHimCondition("pierścień mieszczanina",1),
			new PlayerHasItemWithHimCondition("tarcza z czaszką",1),
			new PlayerHasItemWithHimCondition("spodnie elfickie",1)),
			ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. A oto twój pierścień rycerza.",
		new MultipleActions(monetaactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wyposażenie", "wyposazenie", "przedmioty"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"przedmioty"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("żelazo",40),
												  new PlayerHasItemWithHimCondition("sztabka złota",40),
												  new PlayerHasItemWithHimCondition("bryłka mithrilu",30),
												  new PlayerHasItemWithHimCondition("pierścień mieszczanina",1),
												  new PlayerHasItemWithHimCondition("tarcza z czaszką",1),
												  new PlayerHasItemWithHimCondition("spodnie elfickie",1)))),
				ConversationStates.ATTENDING, "Przynieś mi:\n"
									+"#'40 żelazo'\n"
									+"#'40 sztabka złota'\n"
									+"#'30 bryłka mithrilu'\n"
									+"#'1 pierścień mieszczanina'\n"
									+"#'1 tarcza z czaszką'\n"
									+"#'1 spodnie elfickie'\n"
									+" Proszę przynieś mi to wszystko naraz."
									+" Słowo klucz to #'/przedmioty/'.  Dziękuję!", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień Rycerza",
				"Uporaj się z wyzwaniami, które postawił przed tobą Edgard.",
				true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Edgarda na terenie Zakonu.");
			res.add("zaproponował mi pierścień rycerza wzamian za parę rzeczy.");
			if ("rejected".equals(questState)) {
				res.add("Nie potrzebny jest mi pierścień rycerza.");
				return res;
			} 
			if ("start".equals(questState)) {
				return res;
			} 
			res.add("Edgard poprosił abym mu dostarczył potrzebne przedmioty. Jeżeli nie będę pamiętał co mam przynieść mam powiedzieć przypomnij.");
			if ("przedmioty".equals(questState)) {
				return res;
			} 
			res.add("Edgard jest zadowolony z mojej postawy. W zamian dostałem pierścień rycerza.");
			if (isCompleted(player)) {
				return res;
			} 

			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Stan zadania to: " + questState);
			logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
			return debug;
	}

	@Override
	public String getName() {
		return "PierscienRycerza";
	}

	@Override
	public String getNPCName() {
		return "Edgard";
	}
}
