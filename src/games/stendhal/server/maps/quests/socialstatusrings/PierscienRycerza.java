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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

public class PierscienRycerza extends AbstractQuest {
	private static final String QUEST_SLOT = "pierscien_rycerza";
	private final SpeakerNPC npc = npcs.get("Edgard");

	private static final String MITHRILSHIELD_QUEST_SLOT = "mithrilshield_quest";  

	private static Logger logger = Logger.getLogger(PierscienRycerza.class);

	private AndCondition hasRequiredItemsCondition() {
		return new AndCondition(
			new PlayerHasItemWithHimCondition("sztabka żelaza", 40),
			new PlayerHasItemWithHimCondition("sztabka złota", 40),
			new PlayerHasItemWithHimCondition("bryłka mithrilu", 30),
			new PlayerHasItemWithHimCondition("pierścień mieszczanina", 1),
			new PlayerHasItemWithHimCondition("tarcza z czaszką", 1),
			new PlayerHasItemWithHimCondition("spodnie elfickie", 1)
		);
	}

	private String requiredItemsList() {
		return "\n#'40 sztabek żelaza'\n"
			+ "#'40 sztabek złota'\n"
			+ "#'30 bryłek mithrilu'\n"
			+ "#'pierścień mieszczanina'\n"
			+ "#'1 tarcza z czaszką'\n"
			+ "#'1 spodnie elfickie'\n";
	}

	private void checkLevelHelm() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()) { 
						raiser.say("Twe czyny splamiły twoją duszę niczym chmury burzowe zasłaniające gwiazdy. Zmaż swe winy, nim powrócisz.");
					} else {
						if (player.getLevel() >= 250) {
							if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
								raiser.say("Czy godny żeś noszenia pierścienia rycerza? Twoja decyzja będzie zapisana w gwiazdach.");
							} else if (player.isQuestCompleted(QUEST_SLOT)) {
								raiser.say("Zaszczyt rycerza już posiadasz. Skup się teraz na utrwaleniu swej chwały.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Twoja siła i mądrość są jeszcze jak młode drzewo na wietrze. Wróć, gdy osiągniesz poziom 250.");
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
					raiser.say("Twoja odwaga zostanie wystawiona na próbę, a duch twój – na szalę. Przygotuj się dobrze. Mogę przygotować dla Ciebie #listę przedmiotów.");
					player.addKarma(10);
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Rozumiem. Każdy czas ma swoje przeznaczenie. Wróć, gdy poczujesz gotowość.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void checkCollectingQuests() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(MITHRILSHIELD_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj, wędrowcze. Pragniesz dowieść swej wartości, zdobywając pierścień rycerza?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("wyposażenie", "wyposazenie", "listę"), 
			new AndCondition(
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(MITHRILSHIELD_QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Najpierw wykaż się, wykonując zadanie na tarczę z mithrilu. Potem porozmawiamy o pierścieniu.",
			null);
	}

	private void requestItem() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("wyposażenie", "wyposazenie", "listę"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new QuestCompletedCondition(MITHRILSHIELD_QUEST_SLOT)),
			ConversationStates.ATTENDING, "Pierścień zdobyć możesz, jeśli przyniesiesz mi #przedmioty godne rycerza. Oto ich lista:"
				+ requiredItemsList()
				+ "Zbierz je wszystkie i wróć, bym mógł cię uhonorować. Gdy będziesz czuć potrzebę przypomnieć listę to wróć i powiedz #przypomnij.",
			new SetQuestAction(QUEST_SLOT, "przedmioty"));

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sztabka żelaza", 40));
		reward.add(new DropItemAction("sztabka złota", 40));
		reward.add(new DropItemAction("bryłka mithrilu", 30));
		reward.add(new DropItemAction("pierścień mieszczanina", 1));
		reward.add(new DropItemAction("tarcza z czaszką", 1));
		reward.add(new DropItemAction("spodnie elfickie", 1));
		reward.add(new EquipItemAction("pierścień rycerza", 1, true));
		reward.add(new IncreaseXPAction(100000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wyposażenie", "wyposazenie", "przedmioty"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "przedmioty"),
					hasRequiredItemsCondition()),
			ConversationStates.ATTENDING, "Widzę, że zebrałeś wszystko, co potrzebne. Oto twój pierścień rycerza. Noś go z dumą, a duchy przodków będą cię strzec.",
			new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wyposażenie", "wyposazenie", "przedmioty", "przypomnij"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "przedmioty"),
				new NotCondition(hasRequiredItemsCondition())),
			ConversationStates.ATTENDING, "Przynieś mi następujące #przedmioty, które dowiodą twej odwagi i męstwa:"
				+ requiredItemsList()
				+ "Zbierz je wszystkie i wróć, bym mógł cię uhonorować.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Status Społeczny: Rycerz",
				"Uporaj się z wyzwaniami i udowodnij swą odwagę, aby zdobyć pierścień od Edgarda.",
				true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);

		res.add(player.getGenderVerb("Spotkałem") + " Edgarda, posłańca mistrza Zakonu Rycerzy Cienia, w ich siedzibie.");
		res.add("Edgard poinformował mnie, że mistrz Zakonu zdecydował się uhonorować moje dokonania w świecie, nadając mi tytuł rycerza. Jednak aby to się stało, muszę udowodnić swoje męstwo, determinację i oddanie sprawom wyższym poprzez dostarczenie odpowiednich przedmiotów.");

		if ("rejected".equals(questState)) {
			res.add("Odrzuciłem ofertę Edgarda. Uznaję, że tytuł rycerza nie jest mi w tej chwili potrzebny.");
			return res;
		}

		if ("start".equals(questState)) {
			return res;
		}

		res.add("Edgard poprosił, abym " + player.getGenderVerb("dostarczył") + " następujące przedmioty, które świadczą o mojej wartości:");
		res.add("- Pierścień mieszczanina – symbol mojego dotychczasowego statusu społecznego.");
		res.add("- 40 sztabek żelaza – symbol siły i niezłomności w obliczu trudności.");
		res.add("- 40 sztabek złota – dar na rzecz wspólnoty, podkreślający moją hojność.");
		res.add("- 30 bryłek mithrilu – rzadki surowiec, świadczący o mojej wytrwałości.");
		res.add("- 1 tarczę z czaszką – dowód odwagi w walce i gotowości do poświęceń.");
		res.add("- 1 spodnie elfickie – oznaka zdolności do współpracy i zdobywania tego, co najtrudniej osiągalne.");
		res.add("Edgard przypomniał, że gdy zbiorę wszystkie przedmioty, mam do niego wrócić, aby dokończyć ceremonię nadania tytułu. Jeśli zapomnę, mogę powiedzieć 'przypomnij', a on poda mi szczegóły.");

		if ("przedmioty".equals(questState)) {
			return res;
		}

		res.add("Po dostarczeniu wszystkich przedmiotów Edgard ogłosił, że mistrz Zakonu zatwierdził nadanie mi tytułu rycerza.");
		res.add(player.getGenderVerb("Uzyskałem") + " tytuł rycerza, co jest świadectwem mojego awansu społecznego i uznania wśród mieszkańców świata.");

		if (isCompleted(player)) {
			res.add("Zadanie zakończone. Tytuł rycerza otwiera przede mną nowe możliwości i zwiększa mój prestiż w społeczeństwie.");
			return res;
		}

		// Debugging info for unexpected quest state
		final List<String> debug = new ArrayList<>();
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
		return "Pierścień Rycerza";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
