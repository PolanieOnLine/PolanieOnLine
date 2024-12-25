/***************************************************************************
 *                   (C) Copyright 2010-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

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
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

public class PierscienMieszczanina extends AbstractQuest {
	private static final String QUEST_SLOT = "pierscien_mieszczanina";
	private final SpeakerNPC npc = npcs.get("Marianek");

	private static final String MRSYETI_QUEST_SLOT = "mrsyeti";
	private static final String ANDRZEJ_MAKE_ZLOTA_CIUPAGA_QUEST_SLOT = "andrzej_make_zlota_ciupaga";

	private static Logger logger = Logger.getLogger(PierscienMieszczanina.class);

	private AndCondition hasRequiredItemsCondition() {
		return new AndCondition(
			new PlayerHasItemWithHimCondition("money", 200000),
			new PlayerHasItemWithHimCondition("ciupaga", 1),
			new PlayerHasItemWithHimCondition("sztabka złota", 50),
			new PlayerHasItemWithHimCondition("bryłka mithrilu", 20)
		);
	}

	private String requiredItemsList() {
		return "\n#'ciupagę'\n"
			+ "#'200,000 monet'\n"
			+ "#'50 sztabek złota'\n"
			+ "#'20 bryłek mithrilu'\n";
	}

	private void checkLevelHelm() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()){ 
						raiser.say("Z twej ręki zginął rycerz! Zhańbiłeś swoje imię piętnem czaszki. Nie masz tu czego"
							+ " szukać, aż oczyścisz swoją duszę. A teraz, precz mi z oczu!");
					} else {
						if (player.isQuestCompleted(MRSYETI_QUEST_SLOT)) {
							if (player.getLevel() >= 150) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Byłem kowalem starego klanu, The Soldiers of Honor. Czasy ich świetności minęły,"
										+ " ale ich duch nadal żyje we mnie. Teraz oferuję pierścień mieszczanina, symbol"
										+ " honoru i przynależności. Czy chcesz go zdobyć?");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Już odebrałeś swój pierścień, nie potrzebujesz kolejnego. Żegnaj.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twoja siła i doświadczenie nie są wystarczające, by podjąć to wyzwanie. Powróć, gdy osiągniesz"
									+ " poziom 150 – wtedy będziesz godzien, by nosić pierścień honoru.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Nie widzę w Tobie ducha honoru. Nie pomogłeś Mrs. Yeti, a to ona była lojalnym"
									+ " sprzymierzeńcem TSoH. Powróć, gdy okażesz jej wsparcie.");
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
					raiser.say("Wspaniale. #Pierścień mieszczanina to symbol honoru, ale również ciężkiej pracy."
						+ " Zanim go otrzymasz, musisz mi przynieść odpowiednie materiały. Przygotuj się!");
					player.addKarma(10);
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Nie interesuje Cię przynależność ani honor? Jak chcesz. Wracaj, gdy zmienisz zdanie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void checkCollectingQuests() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(MRSYETI_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj, wędrowcze. Mam dla Ciebie #wyzwanie, które wystawi Twoją wytrwałość na próbę."
				+ " Dzięki temu zdobędziesz #pierścień mieszczanina – symbol prawdziwego honoru.",
			null);

			npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"), 
			new AndCondition(new QuestCompletedCondition(MRSYETI_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT),
					new QuestNotCompletedCondition(ANDRZEJ_MAKE_ZLOTA_CIUPAGA_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Najpierw musisz wykazać się lojalnością wobec Andrzeja, mistrza kowalstwa – wykuć złotą ciupagę."
				+ " To była tradycja w TSoH, by kowale wspierali się nawzajem. Gdy się z tym uporasz, przyjdź do mnie.",
			null);
	}

	private void requestItem() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestCompletedCondition(MRSYETI_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Potrzebuję kilku rzeczy, by przygotować #pierścień. Przynieś mi:"
					+ requiredItemsList()
					+ "To będzie hołd dla dawnej chwały TSoH. Powróć, gdy wszystko zgromadzisz.",
			new SetQuestAction(QUEST_SLOT, "start"));

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("money",200000));
		reward.add(new DropItemAction("ciupaga",1));
		reward.add(new DropItemAction("sztabka złota",50));
		reward.add(new DropItemAction("bryłka mithrilu",20));
		reward.add(new EquipItemAction("pierścień mieszczanina", 1, true));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, "start"),
					hasRequiredItemsCondition()),
			ConversationStates.ATTENDING, "Widzę, że przyniosłeś wszystko, co było potrzebne. To dowód, że duch TSoH wciąż żyje w Tobie."
					+ " Przyjmij ten pierścień jako symbol honoru i przynależności.",
			new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(hasRequiredItemsCondition())),
			ConversationStates.ATTENDING, "Brakuje Ci potrzebnych przedmiotów. Aby otrzymać #pierścień, musisz przynieść wszystko naraz:"
					+ requiredItemsList()
					+ "Nie wracaj, póki tego nie skompletujesz.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Status Społeczny: Mieszczanin",
			"Kowal Marianek, mistrz rzemiosła i znawca sztuki kowalskiej, stawia przed tobą wyzwanie. Pokaż swoją wartość, zdobywając jego uznanie.",
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
		res.add(player.getGenderVerb("Spotkałem") + " kowala Marianka, znanego z kunsztu rzemiosła.");
		res.add("Zaprosił mnie, bym " + player.getGenderVerb("zasłużył") + " na pierścień mieszczanina – symbol poważania wśród ludu.");
		
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Odrzuciłem propozycję kowala Marianka, uznając, że błyskotki nie są mi potrzebne.");
			return res;
		} 
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Kowal Marianek zlecił mi trudne zadanie, dostarczyć:" + 
				" 50 sztabek złota, 20 bryłek czystego mithrilu, ciupagę oraz nieco złota w ilości 200,000. " +
				"Gdy zdobędę te rzeczy, mam do niego wrócić i wypowiedzieć słowo: pierścień.");
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Po spełnieniu jego żądań Marianek uznał moje starania za godne podziwu. W zamian " + 
				player.getGenderVerb("otrzymałem") + 
				" pierścień mieszczanina, symbol szacunku i pozycji wśród miejskich społeczności.");

		if (isCompleted(player)) {
			return res;
		} 

		// Jeśli stan questa jest nieznany, dodaj debugowanie
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu zadania: " + questState);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Mieszczanina";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
