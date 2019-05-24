/***************************************************************************
 *                   (C) Copyright 2003-2015 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Introduce new players to game <p>PARTICIPANTS:<ul>
 * <li> Tad
 * <li> Margaret
 * <li> Ilisa
 * <li> Ketteh Wehoh
 * </ul>
 * 
 * <p>
 * STEPS:<ul>
 * <li> Tad asks you to buy a flask to give it to Margaret.
 * <li> Margaret sells you a flask
 * <li> Tad thanks you and asks you to take the flask to Ilisa
 * <li> Ilisa asks you for a few herbs.
 * <li> Return the created dress potion to Tad.
 * <li> Ketteh Wehoh will reminder player about Tad, if quest is started but not complete.
 * </ul>
 * <p>
 * REWARD:<ul>
 * <li> 550 XP
 * <li> some karma (4)
 * <li> 100 gold coins
 * </ul>
 * <p>
 * REPETITIONS:<ul>
 * <li> None.
 * </ul>
 */
public class MedicineForTad extends AbstractQuest {

	static final String ILISA_TALK_ASK_FOR_FLASK = "Lekarstwo dla #Tad? Nie powiedział Tobie, aby przynieść flaszę?";
	static final String ILISA_TALK_ASK_FOR_HERB = "Ach widzę, że masz flaszę. #Tad potrzebuje lekarstwa? Hmm... Potrzebuję kilku #ziół. Pomożesz?";
	static final String ILISA_TALK_DESCRIBE_HERB = "Na północ od Semos koło trzech wzgórz rośnie zioło zwane arandula. Oto rysunek, który narysowałam. Teraz już wiesz czego szukać.";
	static final String ILISA_TALK_INTRODUCE_TAD = "Potrzebuje silnego lekarstwa, aby się wyleczyć. Oferuje dobrą nagrodę temu kto mu pomoże.";
	static final String ILISA_TALK_REMIND_HERB = "Masz przy sobie te #zioła do #lekarstwa?";
	static final String ILISA_TALK_PREPARE_MEDICINE = "Dobrze! Dziękuję. Teraz wymieszam... szczypta tego... i kilka kropli... jest! Czy możesz zapytać się #Tad o zaprzestanie zbierania tego? Chcę zobaczyć jak się ma.";
	static final String ILISA_TALK_EXPLAIN_MEDICINE = "Oto lekarstwo, na które czeka #Tad.";

	static final String KETTEH_TALK_BYE_INTRODUCES_TAD = "Żegnaj. Spotkałeś Tada w hostelu? Jeżeli będziesz miał okazję to proszę zaglądnij do niego. Słyszałam, że nie czuje się najlepiej. Hostel możesz znaleść w wiosce Semos obok Nishiya.";
	static final String KETTEH_TALK_BYE_REMINDS_OF_TAD = "Dowidzenia. Nie zapomnij sprawdzić Tada. Mam nadzieje, że czuję się lepiej.";

	static final String TAD_TALK_GOT_FLASK = "Dobrze, że masz flaszę!";
	static final String TAD_TALK_REWARD_MONEY = "Tutaj masz pieniądze na pokrycie twoich wydatków.";
	static final String TAD_TALK_FLASK_ILISA = "Teraz potrzebuję, abyś wziął ją do #Ilisa... ona będzie widziała co robić dalej.";
	static final String TAD_TALK_REMIND_FLASK_ILISA = "Potrzebuję Ciebie, abyś wziął flaszę do #ilisa... ona będzie widziała co robić dalej.";
	static final String TAD_TALK_INTRODUCE_ILISA = "Ilisa jest uzdrowicielką w świątyni w Semos.";
	static final String TAD_TALK_REMIND_MEDICINE = "*kaszlnięcie* Mam nadzieję, że #Ilisa pospieszy się z moim lekarstwem...";
	static final String TAD_TALK_COMPLETE_QUEST = "Dziękuję! Idę porozmawiać z #Ilisa tak szybko jak tylko mogę.";

	static final String TAD_TALK_ASK_FOR_EMPTY_FLASK = "Nie czuję się dobrze... Potrzebuję butelkę z lekarstwem. Czy możesz zdobyć dla mnie pustą #flaszę?";
	static final String TAD_TALK_ALREADY_HELPED_1 = "Wszystko w porządku, dziękuję.";
	static final String TAD_TALK_ALREADY_HELPED_2 = "Już mi pomogłeś! Czuję się teraz lepiej.";
	static final String TAD_TALK_WAIT_FOR_FLASK = "*kaszlnięcie* Och... Potrzebuję lekarstwa! Wracaj szybko z #flaszą od #Margaret.";
	static final String TAD_TALK_FLASK_MARGARET = "Mógłbyś zdobyć flaszę od #Margaret.";
	static final String TAD_TALK_INTRODUCE_MARGARET = "Margaret jest kelnerką w hotelu, do którego idzie się w dół ulicy.";
	static final String TAD_TALK_CONFIRM_QUEST = "Pomożesz mi?";
	static final String TAD_TALK_QUEST_REFUSED = "Och, proszę nie zmienisz zdania? *Apsiiiik*";
	static final String TAD_TALK_QUEST_ACCEPTED = "Wspaniale! Proszę idź szybko jak możesz. *Apsiiik*";

	static final String HISTORY_MET_TAD = "Spotkałem Tada w hostelu w Semos.";
	static final String HISTORY_QUEST_OFFERED = "Poprosił mnie o kupienie flaszy od Margaret w tawernie w Semos.";
	static final String HISTORY_GOT_FLASK = "Mam flaszę i wkrótce zaniosę ją Tadowi.";
	static final String HISTORY_TAKE_FLASK_TO_ILISA = "Tad poprosił mnie o zaniesienie flaszy do Ilisy w świątyni w Semos.";
	static final String HISTORY_ILISA_ASKED_FOR_HERB = "Ilisa poprosiła mnie o dostarczenie zioła zwanego Arandula, które rośnie na północ od Semos obok zagajnika.";
	static final String HISTORY_GOT_HERB = "Znalazłem trochę ziół Arandula i zaniosę je do Ilisy.";
	static final String HISTORY_POTION_READY = "Ilisa zrobiła silne lekarstwo, który pomoże Tadowi. Poproiła mnie o przekazanie wiadomości Tadowi, że jest gotowe.";
	static final String HISTORY_DONE = "Tad podziękował mi.";

	static final String STATE_START = "start";
	static final String STATE_ILISA = "ilisa";
	static final String STATE_HERB = "corpse&herbs";
	static final String STATE_SHOWN_DRAWING = "shownDrawing";
	static final String STATE_POTION = "eliksir";
	static final String STATE_DONE = "done";

	private static final String QUEST_SLOT = "introduce_players";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest("TadFirstChat")) {
			res.add(HISTORY_MET_TAD);
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_START, STATE_ILISA, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_QUEST_OFFERED);
		}
		if (questState.equals(STATE_START) && player.isEquipped("flasza")
				|| player.isQuestInState(QUEST_SLOT, 0, STATE_ILISA, STATE_HERB, STATE_POTION, STATE_DONE)) {
			res.add(HISTORY_GOT_FLASK);
		}
		if (player.isQuestInState(QUEST_SLOT, 0, STATE_ILISA, STATE_HERB, STATE_POTION, STATE_DONE)) {
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
		final SpeakerNPC npc = npcs.get("Tad");
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				TAD_TALK_ALREADY_HELPED_1,
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				TAD_TALK_ASK_FOR_EMPTY_FLASK,
				null);

		// In case Quest has already been completed
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flasza", "flaszka", "flaszę", "flaszą"),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				TAD_TALK_ALREADY_HELPED_2,
				null);

		// If quest is not started yet, start it.
		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("flasza", "flaszka", "flaszę", "flaszą"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				TAD_TALK_FLASK_MARGARET,
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING, 
				TAD_TALK_QUEST_ACCEPTED,
				new SetQuestAction(QUEST_SLOT, 0, STATE_START));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				TAD_TALK_QUEST_REFUSED,
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"margaret",
				null,
				ConversationStates.QUEST_OFFERED,
				TAD_TALK_INTRODUCE_MARGARET + " " + TAD_TALK_CONFIRM_QUEST,
				null);

		// Remind player about the quest
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flasza", "flaszka", "flaszę"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
						new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING,
				TAD_TALK_WAIT_FOR_FLASK,
				null);

        // Remind player about the quest
        npc.add(ConversationStates.ATTENDING,
                ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
                ConversationStates.ATTENDING,
                TAD_TALK_WAIT_FOR_FLASK,
                null);

		npc.add(ConversationStates.ATTENDING,
				"margaret",
				null,
				ConversationStates.ATTENDING,
				TAD_TALK_INTRODUCE_MARGARET,
				null);
	}

	private void step_2() {
		/** Just buy the stuff from Margaret. It isn't a quest */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Tad");

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new EquipItemAction("money", 100));
		processStep.add(new IncreaseXPAction(150));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_ILISA));
		
		// starting the conversation the first time after getting a flask.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_START),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				TAD_TALK_GOT_FLASK + " " + TAD_TALK_REWARD_MONEY + " " + TAD_TALK_FLASK_ILISA,
				new MultipleActions(processStep));

		// player said hi with flask on ground then picked it up and said flask
		npc.add(ConversationStates.ATTENDING, Arrays.asList("flasza", "flaszka", "flaszę"),
                new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, STATE_START), new PlayerHasItemWithHimCondition("flasza")),
                ConversationStates.ATTENDING,
                TAD_TALK_GOT_FLASK + " " + TAD_TALK_REWARD_MONEY + " " + TAD_TALK_FLASK_ILISA,
                new MultipleActions(processStep));

		// remind the player to take the flask to Ilisa.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_ILISA),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				TAD_TALK_GOT_FLASK + " " + TAD_TALK_FLASK_ILISA,
				null);

		// another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_ILISA),
                ConversationStates.ATTENDING,
                TAD_TALK_REMIND_FLASK_ILISA,
                null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("ilisa", "iiisa", "llisa"),
				null,
				ConversationStates.ATTENDING,
				TAD_TALK_INTRODUCE_ILISA,
				null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Ilisa");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_ILISA),
						new NotCondition(new PlayerHasItemWithHimCondition("flasza"))),
				ConversationStates.ATTENDING, 
				ILISA_TALK_ASK_FOR_FLASK,
				null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("flasza"));
		processStep.add(new IncreaseXPAction(150));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_HERB));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_ILISA),
						new PlayerHasItemWithHimCondition("flasza")),
				ConversationStates.ATTENDING, 
				ILISA_TALK_ASK_FOR_HERB,
				new MultipleActions(processStep));

		ChatAction showArandulaDrawing = new ExamineChatAction("arandula.png", "Ilisa rysuje", "Arandula");
		ChatAction flagDrawingWasShown = new SetQuestAction(QUEST_SLOT, 1, STATE_SHOWN_DRAWING);
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("yes", "ok", "tak", "dobrze"),
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 1, STATE_SHOWN_DRAWING)),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING,
				ILISA_TALK_DESCRIBE_HERB,
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("herb", "arandula", "ziół", "zioła"),
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				ILISA_TALK_DESCRIBE_HERB,
				new MultipleActions(showArandulaDrawing, flagDrawingWasShown));

		npc.add(
				ConversationStates.ATTENDING,
				"tad",
				null,
				ConversationStates.ATTENDING,
				ILISA_TALK_INTRODUCE_TAD,
				null);
	}

	private void step_5() {
		final SpeakerNPC npc = npcs.get("Ilisa");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new NotCondition(new PlayerHasItemWithHimCondition("arandula"))),
				ConversationStates.ATTENDING, 
				ILISA_TALK_REMIND_HERB, null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new DropItemAction("arandula"));
		processStep.add(new IncreaseXPAction(150));
        processStep.add(new IncreaseKarmaAction(4));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_POTION));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
						new PlayerHasItemWithHimCondition("arandula")),
				ConversationStates.ATTENDING, 
				ILISA_TALK_PREPARE_MEDICINE,
				new MultipleActions(processStep));

		npc.add(ConversationStates.ATTENDING, Arrays.asList(STATE_POTION,
				"medicine", "mikstura", "lekarstwo", "lekarstwa"), null, ConversationStates.ATTENDING,
				ILISA_TALK_EXPLAIN_MEDICINE, null);
	}

	private void step_6() {
		SpeakerNPC npc = npcs.get("Tad");

        // another reminder in case player says task again
        npc.add(ConversationStates.ATTENDING,
        		ConversationPhrases.QUEST_MESSAGES,
                new QuestInStateCondition(QUEST_SLOT, 0, STATE_HERB),
                ConversationStates.ATTENDING,
                TAD_TALK_REMIND_MEDICINE,
                null);

		final List<ChatAction> processStep = new LinkedList<ChatAction>();
		processStep.add(new IncreaseXPAction(100));
		processStep.add(new SetQuestAction(QUEST_SLOT, 0, STATE_DONE));
		
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_POTION)),
				ConversationStates.ATTENDING,
				TAD_TALK_COMPLETE_QUEST,
				new MultipleActions(processStep));
	
		/*
		 * if player has not finished this quest, ketteh will remind player about him.
		 * if player has not started, and not finished, ketteh will ask if player has met him.
		 */
		npc = npcs.get("Ketteh Wehoh");

        npc.add(ConversationStates.ATTENDING, 
        		ConversationPhrases.GOODBYE_MESSAGES,
        		new AndCondition(
        				new QuestStartedCondition(QUEST_SLOT),
        					     new QuestNotCompletedCondition(QUEST_SLOT)),
                ConversationStates.IDLE,
                KETTEH_TALK_BYE_REMINDS_OF_TAD,
                null);

        npc.add(ConversationStates.ATTENDING, 
        		ConversationPhrases.GOODBYE_MESSAGES,
        		new QuestNotStartedCondition(QUEST_SLOT),
                ConversationStates.IDLE,
                KETTEH_TALK_BYE_INTRODUCES_TAD,
                null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Lekarstwo dla Tada",
				"Tad chłopiec w hostelu Semos, który potrzebuje pomocy w zdobyciu lekarstwa.",
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
		return "MedicineForTad";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Tad";
	}
}
