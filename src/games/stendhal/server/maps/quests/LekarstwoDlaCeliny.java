/***************************************************************************
 *                   (C) Copyright 2019-2021 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class LekarstwoDlaCeliny extends AbstractQuest {
	private static final String QUEST_SLOT = "lekarstwo_dla_celiny";
	private final SpeakerNPC npc = npcs.get("Bolesław");

	/** HISTORIA **/
	static final String HISTORY_QUEST_OFFERED = "Bolesław poprosił mnie o kupienie butelki od Bogusia w szpitalu w Zakopane.";
	static final String HISTORY_GOT_FLASK = "Mam butelkę i wkrótce zaniosę ją Bolesławowi.";
	static final String HISTORY_TAKE_FLASK_TO_JADZKA = "Bolesław poprosił mnie o zaniesienie bulteki do Gaździny Jadźki w szpitalu w Zakopane.";
	static final String HISTORY_ILISA_ASKED_FOR_HERB = "Gaździna Jadźka poprosiła mnie o dostarczenie zioła zwanego arandula, które rośnie na północ od Semos obok zagajnika.";
	static final String HISTORY_GOT_HERB = "Znalazłem trochę ziół arandula i zaniosę je do Gaździny Jadźki.";
	static final String HISTORY_POTION_READY = "Gaździna Jadźka zrobiła silne lekarstwo, który pomoże Celinie. Poprosiła mnie o przekazanie wiadomości Bolesławowi, że jest gotowe.";
	static final String HISTORY_DONE = "Bolesław w imieniu jej córki podziękował mi.";

	static final String STATE_START = "start";
	static final String STATE_DAD = "tata";
	static final String STATE_HERB = "arandula";
	static final String STATE_SHOWN_DRAWING = "shownDrawing";
	static final String STATE_POTION = "eliksir";
	static final String STATE_DONE = "done";

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wszystko już dobrze, dzięki ci. #Celina znowu ma rumieńce, a ja spokojniejszy oddech.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Serce mi cięży, bo moja córka, #Celina, gaśnie jak żar pod popiołem. Trza mi #butelki na lekarstwo. Zdobędziesz dla mnie pustą #butelkę?",
				null);

		// In case Quest has already been completed
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już nam pomogłeś, dobry człowieku. Niech cię los prowadzi, #Celina już nie słabnie.",
				null);

		// If quest is not started yet, start it.
		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Butelka jest u #Bogusia, w szpitalu w #Zakopane. Zagadasz do niego i przyniesiesz mi ją?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING, 
				"Niech ci Bóg i duchy gór sprzyjają! Leć prędko, jakby cię wiater niósł. Każda chwila się liczy.",
				new SetQuestAction(QUEST_SLOT, 0, STATE_START));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Ech… rozumiem. Ale jeśli zmienisz zdanie, wróć. Ojciec nie ma już kogo prosić…",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("bogusia", "boguś"),
				null,
				ConversationStates.QUEST_OFFERED,
				"#Boguś bywa w szpitalu w #Zakopane. Pomożesz?",
				null);

		// Remind player about the quest
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
						new NotCondition(new PlayerHasItemWithHimCondition("butelka"))),
				ConversationStates.ATTENDING,
				"Nie zwlekaj! Wracaj z #butelką od #Bogusia, bo mi serce pęka z troski.",
				null);

        // Remind player about the quest
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
                ConversationStates.ATTENDING,
                "Potrzebuję lekarstwa! Wracaj szybko z #butelką od #Bogusia.",
                null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("bogusia", "boguś"),
				null,
				ConversationStates.ATTENDING,
				"#Boguś bywa w szpitalu w #Zakopane. Powiedz mu, że to dla #Celiny, nie odmówi.",
				null);
	}

	private void step_2() {
		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new EquipItemAction("money", 100));
		processStep.add(new IncreaseXPAction(550));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_DAD));
		
		// starting the conversation the first time after getting a flask.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
						new PlayerHasItemWithHimCondition("butelka")),
				ConversationStates.ATTENDING, 
				"A niech cię! Masz #butelkę, chwała ci za to. Trzymaj te #pieniądze na drogę i wydatki. "
					+ "Teraz zanieś ją do #'Gaździny Jadźki' w szpitalu w #Zakopane… i powiedz jej imię mojej córki: #Celina.",
				new MultipleActions(processStep));

		// player said hi with flask on ground then picked it up and said flask
		npc.add(ConversationStates.ATTENDING, Arrays.asList("butelka", "butelkę", "butelką"),
                new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, STATE_START), new PlayerHasItemWithHimCondition("butelka")),
                ConversationStates.ATTENDING,
                "A niech cię! Masz #butelkę, chwała ci za to. Trzymaj te #pieniądze na drogę i wydatki. "
    				+ "Teraz zanieś ją do #'Gaździny Jadźki' w szpitalu w #Zakopane… i powiedz jej imię mojej córki: #Celina.",
                new MultipleActions(processStep));

		// remind the player to take the flask to Ilisa.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new PlayerHasItemWithHimCondition("butelka")),
				ConversationStates.ATTENDING, 
				"No, nie stójże! Zanieś #butelkę do #'Gaździny Jadźki'. Powiedz jej: #Celina.",
				null);

		// another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
                ConversationStates.ATTENDING,
                "Pamiętaj: #'Gaździna Jadźka', szpital w #Zakopane i imię: #Celina.",
                null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Gaździna", "Jadźka", "Gaździna Jadźka"),
				null,
				ConversationStates.ATTENDING,
				"#'Gaździna Jadźka' to mądra uzdrowicielka w szpitalu w #Zakopane. Ona wie, jak warzyć mikstury.",
				null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Celina", "choroba"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new NotCondition(new PlayerHasItemWithHimCondition("butelka"))),
				ConversationStates.ATTENDING, 
				"Bez #butelki nic nie uwarzę, hę? Przynieś mi naczynie, a dopiero wtedy pomyślimy o #lekarstwie dla #Celiny…",
				null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("butelka"));
		processStep.add(new IncreaseXPAction(350));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_HERB));

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Celina", "choroba"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new PlayerHasItemWithHimCondition("butelka")),
				ConversationStates.ATTENDING, 
				"O, jest i #butelka. Mówisz, że #Celina choruje… Dobrze. Ale potrzebuję jeszcze #'ziół aranduli'. Pomożesz mi je znaleźć?",
				new MultipleActions(processStep));

		ChatAction showArandulaDrawing = new ExamineChatAction("arandula.png", "Jadźka rysuje", "Arandula");
		ChatAction flagDrawingWasShown = new SetQuestAction(QUEST_SLOT, 1, STATE_SHOWN_DRAWING);
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("yes", "ok", "tak", "dobrze"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 1, STATE_SHOWN_DRAWING)),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING,
				"Słuchaj uważnie: na północ od #Semos, przy zagajniku i wzgórzach, rośnie #arandula. "
					+ "Tu masz rysunek, patrz i zapamiętaj. Jak wrócisz, powiedz mi tylko: #arandula.",
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("herb", "ziół", "zioła"),
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Na północ od #Semos rośnie #arandula. Rysunek już masz, a więc jak wrócisz, powiedz: #arandula.",
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("arandula"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING, 
				"Masz przy sobie #arandula? Bez niej nie będzie #lekarstwa.", null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("arandula"));
		processStep.add(new IncreaseXPAction(250));
        processStep.add(new IncreaseKarmaAction(10));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_POTION));

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("arandula"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING,
				"Dobrze… to jest to. Teraz uwarzę miksturę, jakiej w tych górach dawno nie widziano… "
					+ "Zanieś wieść do #Bolesława: niech przyjdzie do szpitala w #Zakopane z #Celiną. Lekarstwo będzie gotowe.",
				new MultipleActions(processStep));

		npc.add(ConversationStates.ATTENDING, Arrays.asList(STATE_POTION,
				"medicine", "mikstura", "lekarstwo", "lekarstwa"), null, ConversationStates.ATTENDING,
				"Mikstura już gotowa. Teraz #Bolesław musi przyprowadzić #Celinę, inaczej nie dopilnuję dawkowania.", null);
	}

	private void step_5() {
        // another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING,
        		ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_POTION),
                ConversationStates.ATTENDING,
                "Oby #'Gaździna Jadźka' zdążyła na czas… Ja już nie mam siły patrzeć, jak #Celina słabnie.",
                null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new IncreaseXPAction(1000));
		processStep.add(new IncreaseKarmaAction(15));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_DONE));
		
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_POTION)),
				ConversationStates.ATTENDING,
				"Dziękuję ci, z całego serca. Zaraz ruszamy z #Celiną do #'Gaździny Jadźki' w szpitalu w #Zakopane."
					+ "Niech ci to dobro wróci, stokrotnie.",
				new MultipleActions(processStep));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Lekarstwo dla Celiny",
				"Celina jest bardzo chora i potrzebuje lekarstwa, które mogłoby ją uleczyć.",
				false);
		step_1();
		step_2();
		step_3();
		step_4();
		step_5();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_START, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_QUEST_OFFERED);
		}
		if (questState.equals(STATE_START) && player.isEquipped("butelka")
				|| player.isQuestInState(QUEST_SLOT, 0, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_GOT_FLASK);
		}
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_TAKE_FLASK_TO_JADZKA);
		}
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_ILISA_ASKED_FOR_HERB);
		}
		if (questState.equals(STATE_HERB) && player.isEquipped("arandula")
				|| player.isQuestInState(QUEST_SLOT, 0, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_GOT_HERB);
		}
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_POTION_READY);
		}
		if (questState.equals(STATE_DONE)) {
			res.add(HISTORY_DONE);
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Lekarstwo dla Celiny";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
