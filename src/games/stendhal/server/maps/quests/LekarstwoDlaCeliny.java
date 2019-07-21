/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
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
	/** HISTORIA **/
	static final String HISTORY_QUEST_OFFERED = "Bolesław poprosił mnie o kupienie butelki od Bogusia w szpitalu w Zakopane.";
	static final String HISTORY_GOT_FLASK = "Mam butelkę i wkrótce zaniosę ją Bolesławowi.";
	static final String HISTORY_TAKE_FLASK_TO_ILISA = "Bolesław poprosił mnie o zaniesienie bulteki do Gaździny Jadźki w szpitalu w Zakopane.";
	static final String HISTORY_ILISA_ASKED_FOR_HERB = "Gaździna Jadźka poprosiła mnie o dostarczenie zioła zwanego arandula, które rośnie na północ od Semos obok zagajnika.";
	static final String HISTORY_GOT_HERB = "Znalazłem trochę ziół arandula i zaniosę je do Gaździny Jadźki.";
	static final String HISTORY_POTION_READY = "Gaździna Jadźka zrobiła silne lekarstwo, który pomoże Celinie. Poprosiła mnie o przekazanie wiadomości Bolesławowi, że jest gotowe.";
	static final String HISTORY_DONE = "Bolesław w imieniu jej córki podziękował mi.";

	static final String STATE_START = "start";
	static final String STATE_DAD = "tata";
	static final String STATE_HERB = "corpse&herbs";
	static final String STATE_SHOWN_DRAWING = "shownDrawing";
	static final String STATE_POTION = "eliksir";
	static final String STATE_DONE = "done";

	private static final String QUEST_SLOT = "lekarstwo_dla_celiny";

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
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_START, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_QUEST_OFFERED);
		}
		if (questState.equals(STATE_START) && player.isEquipped("butelka")
				|| player.isQuestInState(QUEST_SLOT, 0, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_GOT_FLASK);
		}
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_DAD, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_TAKE_FLASK_TO_ILISA);
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

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Bolesław");
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wszystko jest w porządku, dziękuję.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Moja córka nie czuję się za dobrze... Potrzebuje butelkę z lekarstwem. Czy możesz zdobyć dla mnie pustą #butelkę?",
				null);

		// In case Quest has already been completed
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już nam pomogłeś! Czuję się teraz lepiej.",
				null);

		// If quest is not started yet, start it.
		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Mógłbyś zdobyć butelkę od #'Bogusia'.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING, 
				"Wspaniale! Proszę idź szybko jak możesz.",
				new SetQuestAction(QUEST_SLOT, 0, STATE_START));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Och, proszę nie zmienisz zdania?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("bogusia", "boguś"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Boguś znajduje się w szpitalu w Zakopane. Pomożesz nam?",
				null);

		// Remind player about the quest
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("butelka", "butelkę", "butelką"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
						new NotCondition(new PlayerHasItemWithHimCondition("butelka"))),
				ConversationStates.ATTENDING,
				"Potrzebuję lekarstwa! Wracaj szybko z #butelką od #Bogusia.",
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
				"Boguś znajduje się w szpitalu w Zakopane.",
				null);
	}

	private void step_2() {

	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Bolesław");

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
				"Dobrze, że masz butelkę! Tutaj masz pieniądze na pokrycie twoich wydatków. Teraz potrzebuję, abyś wziął ją do #'Gaździna Jadźka'... ona będzie wiedziała co robić dalej.",
				new MultipleActions(processStep));

		// player said hi with flask on ground then picked it up and said flask
		npc.add(ConversationStates.ATTENDING, Arrays.asList("butelka", "butelkę", "butelką"),
                new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, STATE_START), new PlayerHasItemWithHimCondition("butelka")),
                ConversationStates.ATTENDING,
                "Dobrze, że masz butelkę! Tutaj masz pieniądze na pokrycie twoich wydatków. Teraz potrzebuję, abyś wziął ją do #'Gaździna Jadźka'... ona będzie wiedziała co robić dalej.",
                new MultipleActions(processStep));

		// remind the player to take the flask to Ilisa.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new PlayerHasItemWithHimCondition("butelka")),
				ConversationStates.ATTENDING, 
				"Dobrze, że masz butelkę! Teraz potrzebuję, abyś wziął ją do #'Gaździna Jadźka'... ona będzie wiedziała co robić dalej.",
				null);

		// another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
                ConversationStates.ATTENDING,
                "Potrzebuję Ciebie, abyś wziął flaszę do #'Gaździna Jadźka'... ona będzie widziała co robić dalej.",
                null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Gaździna", "Jadźka", "Gaździna Jadźka"),
				null,
				ConversationStates.ATTENDING,
				"Gaździna Jadźka jest uzdrowicielką w szpitalu w Zakopane.",
				null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new NotCondition(new PlayerHasItemWithHimCondition("butelka"))),
				ConversationStates.ATTENDING, 
				"Lekarstwo dla #Celiny? Jej ojciec nie powiedział Tobie, aby przynieść butelkę?",
				null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("butelka"));
		processStep.add(new IncreaseXPAction(350));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_HERB));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DAD),
						new PlayerHasItemWithHimCondition("butelka")),
				ConversationStates.ATTENDING, 
				"Ach widzę, że masz butelkę. #Celina potrzebuje lekarstwa? Hmm... Potrzebuję kilku #'ziół'. Pomożesz?",
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
				"Na północ od Semos koło trzech wzgórz rośnie zioło zwane arandula. Oto rysunek, który narysowałam. Teraz już wiesz czego szukać.",
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("herb", "arandula", "ziół", "zioła"),
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Na północ od Semos koło trzech wzgórz rośnie zioło zwane arandula. Oto rysunek, który narysowałam. Teraz już wiesz czego szukać.",
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("celina", "celiny"),
				null,
				ConversationStates.ATTENDING,
				"Potrzebuje silnego lekarstwa, aby mogła się wyleczyć. Jej ojciec oferuje dobrą nagrodę temu kto jej pomoże.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("bolesław", "boleslaw", "bolesławowi", "boleslawowi"),
				null,
				ConversationStates.ATTENDING,
				"Bolesław jest ojcem Celiny.",
				null);
	}

	private void step_5() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING, 
				"Masz przy sobie te #zioła do #lekarstwa?", null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("arandula"));
		processStep.add(new IncreaseXPAction(250));
        processStep.add(new IncreaseKarmaAction(10));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_POTION));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING,
				"Dobrze! Dziękuję. Teraz wymieszam... szczypta tego... i kilka kropli... jest! Powiedz #'Bolesławowi', że chcę zobaczyć jak się ma jego córka.",
				new MultipleActions(processStep));

		npc.add(ConversationStates.ATTENDING, Arrays.asList(STATE_POTION,
				"medicine", "mikstura", "lekarstwo", "lekarstwa"), null, ConversationStates.ATTENDING,
				"Oto lekarstwo, na które czeka #Celina.", null);
	}

	private void step_6() {
		SpeakerNPC npc = npcs.get("Bolesław");

        // another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING,
        		ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
                ConversationStates.ATTENDING,
                "Mam nadzieję, że #Gaździna Jadźka pospieszy się z tym lekarstwem...",
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
				"Dziękuję! Zaraz przejdę się z moją córką, by porozmawiać z #'Gaździną Jadźką' tak szybko jak tylko możemy.",
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
		step_6();
	}
	@Override
	public String getName() {
		return "LekrastwoDlaCeliny";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
	@Override
	public String getNPCName() {
		return "Bolesław";
	}
}
