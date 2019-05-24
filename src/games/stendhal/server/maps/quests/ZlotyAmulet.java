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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
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
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class ZlotyAmulet extends AbstractQuest {
	public static final String QUEST_SLOT = "zloty_amulet";
	
	private static final int REQUIRED_MINUTES = 10; // 10 minut

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Jagienką.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie pomogę Jagience.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Pomogę Jagience ze złotym amuletem.");
		}
		if ("start".equals(questState) && player.isEquipped("bryłka złota", 20) || "done".equals(questState)) {
			res.add("Mam 20 bryłek złota dla Jagienki.");
		}
		if ("kowal".equals(questState) || "done".equals(questState)) {
			res.add("Złotnik stwierdził, że te bryłki złota idealnie się nadają na jej amulet, więc kazała mi pójść do Kowala Jacka i powiedzieć mu jej imię.");
		}
		if ("jagienka".equals(questState) && player.isEquipped("złoty amulet")
				|| "done".equals(questState)) {
			res.add("Mam złoty amulet dla Jagienki.");
		}
		if ("done".equals(questState)) {
			res.add("Oddałem złoty amulet Jagience.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Jagienka");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Zawsze chciałam sobie zrobić złoty amulet z wydobytego z jeziora złota. Pomógłbyś mi go zrobić?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Oh. Dziękuję! Jest prześliczny!",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze! Pamiętaj, że jezioro znajduje się w dolinie Rybiego Potoku i musisz mi przynieść conajmniej 20 bryłek złota! Będę tutaj na Ciebie czekała!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Może inny dzielny wojownik mi pomoże...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Jagienka");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("bryłka złota",20)),
			ConversationStates.ATTENDING,
			"Widzę, że masz przy sobie bryłki złota. To dobrze! Podaj mi je, a ja przejdę się do złotnika, dobrze?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("bryłka złota",20))),
			ConversationStates.ATTENDING,
			"Hej, wciąż czekam na bryłki złota do mojego amuletu! Pamiętasz?",
			null);

		final List<ChatAction> reward1 = new LinkedList<ChatAction>();
		reward1.add(new DropItemAction("bryłka złota",20));
		reward1.add(new IncreaseXPAction(1250));
		reward1.add(new SetQuestAction(QUEST_SLOT, "zlotnik;"));
		reward1.add(new IncreaseKarmaAction(10));
		reward1.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("bryłka złota",20),
			ConversationStates.ATTENDING,
			"Za 10 minut wrócę do Ciebie!",
			new MultipleActions(reward1));

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Chyba pamiętasz o co Cię prosiłam?",
			null);
	}

	private void zlotnikStep() {
		final SpeakerNPC npc = npcs.get("Jagienka");

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "zlotnik;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.IDLE, 
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Złotnik jeszcze nie skończył badać tego złota. Wróć za "));

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "zlotnik;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.ATTENDING, 
				"Złotnik właśnie skończył badać i powiedział, że to złoto idealnie się nadaje na mój złoty amulet! Musisz teraz się przejść do kowala Jacka, który znajduje się w domku na kościelisku południowy-wschód. Powiedz mu moje imię, a będzie wiedział co ma zrobić!", 
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final StackableItem brylki = (StackableItem) SingletonRepository.getEntityManager().getItem("bryłka złota");
						brylki.setQuantity(20);
						player.equipOrPutOnGround(brylki);
						player.addXP(250);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "kowal");
					};
				});
	}
	
	private void kowalStep() {
		final SpeakerNPC npc = npcs.get("Kowal Jacek");

		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("Jagienka", "złoty amulet", "zadanie", "złoto", "bryłki", "amulet"),
				new QuestInStateCondition(QUEST_SLOT, "kowal"),
				ConversationStates.QUEST_2_OFFERED,
				"Hej! Kim jest Jagienka? Ahhh.. Dobra, już sobie przypomniałem. Rozumiem, że ona chce zrobić złoty amulet dla siebie?",
				null);
		
		npc.add(
				ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "kowal"), 
						new PlayerHasItemWithHimCondition("bryłka złota",20)),
				ConversationStates.IDLE,
				"Dobra. To biorę się od razu do pracy! Przyjdź do mnie za jakieś " + REQUIRED_MINUTES * 3 + " minut.",
				new MultipleActions(
						new DropItemAction("bryłka złota",20),
						new SetQuestAction(QUEST_SLOT, "forging;"),
						new SetQuestToTimeStampAction(QUEST_SLOT, 1)));
		
		npc.add(
				ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "kowal"),
						new NotCondition(new PlayerHasItemWithHimCondition("bryłka złota",20))),
				ConversationStates.IDLE,
				"Jak mam wziąść się do pracy, jeżeli nie posiadasz przy osobie odpowiedniej ilości bryłek złota?! Dowidzenia.",
				null);
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES * 3))),
				ConversationStates.IDLE, 
				null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES * 3, "Jeszcze nie skończyłem wyrabiać amuletu. Proszę wróć za "));
		
		final List<ChatAction> reward2 = new LinkedList<ChatAction>();
		reward2.add(new IncreaseXPAction(500));
		reward2.add(new IncreaseKarmaAction(10));
		reward2.add(new SetQuestAction(QUEST_SLOT, "jagienka"));
		reward2.add(new EquipItemAction("złoty amulet", 1, true));
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.IDLE, 
				"Proszę, oto złoty amulet dla Jagienki. Teraz możesz wrócić do niej i jej wręczyć.",
				new MultipleActions(reward2));
	}
	
	private void lastStep() {
		final SpeakerNPC npc = npcs.get("Jagienka");
		
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "jagienka"),
						new PlayerHasItemWithHimCondition("złoty amulet")),
				ConversationStates.ATTENDING,
				"Hej! Czy ten amulet jest dla mnie? Dziękuję!",
				new MultipleActions(
						new IncreaseXPAction(5500),
						new IncreaseKarmaAction(15),
						new DropItemAction("złoty amulet"),
						new SetQuestAction(QUEST_SLOT, "done")));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Złoty amulet",
				"Jagienka zamarzyła sobie o jakimś złotym amulecie i nie może przestać o nim śnić.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
		zlotnikStep();
		kowalStep();
		lastStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "ZlotyAmulet";
	}

	public String getTitle() {
		return "Złoty amulet";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return "Jagienka";
	}
}
