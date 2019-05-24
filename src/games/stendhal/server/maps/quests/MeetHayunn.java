/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Speak with Hayunn 
 * <p>
 * PARTICIPANTS: <ul><li> Hayunn Naratha</ul>
 *
 * STEPS: <ul>
 * <li> Talk to Hayunn to activate the quest.
 * <li> He asks you to kill a rat, also offering to teach you how
 * <li> Return and get directions to Semos
 * <li> Return and learn how to click move, and get some URLs
 * </ul>
 *
 * REWARD: <ul><li> 200 XP <li> 150 gold coins <li> studded shield </ul>
 *
 * REPETITIONS: <ul><li> Get the URLs as much as wanted but you only get the reward once.</ul>
 */
public class MeetHayunn extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_hayunn";

	private static final int TIME_OUT = 60;

	private static Logger logger = Logger.getLogger(MeetHayunn.class);
	
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
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Hayunn Naratha jest pierwszą osobą jaką kiedykolwiek spotkałem na tym świecie, kazał mi zabić szczura.");
		if (player.getQuest(QUEST_SLOT, 0).equals("start") && new KilledForQuestCondition(QUEST_SLOT,1).fire(player, null, null)) {
			res.add("Zabiłem tego szczura, powinienem wrócić i mu powiedzieć!");
		}
		if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
			return res;
		} 
		res.add("Zabiłem szczura. Hayunn opowie mi teraz więcej o tym świecie.");
		if ("killed".equals(questState)) {
			return res;
		} 
		res.add("Hayunn dał mi trochę pieniędzy i kazał mi iść znaleźć Monogenes w Semos City. Dał mi też mapę.");
		if ("taught".equals(questState)) {
			return res;
		} 
		res.add("Hayunn powiedział mi wiele przydatnych informacji na temat jak przetrwać,  dał mi też tarczę ćwiekową i pieniądze.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasujące do stanu poszukiwania " + questState);
		return debug;
	}

	private void prepareHayunn() {

		final SpeakerNPC npc = npcs.get("Hayunn Naratha");

		// player wants to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Cóż, wróć. Gdy byłem młodym poszukiwaczem przygód to naciskałem na wrogach, aby ich atakować. Możesz się mnie zapytać jaki jest powód ryzykowania mojego życia, aby coś zabić? Tak?",
				null);

		//player doesn't want to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Dobrze, wyglądasz na inteligentną osobę. Jestem pewien, że sobie poradzisz!",
				null);

		//player returns to Hayunn not having killed a rat
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT,1))),
				ConversationStates.ATTENDING,
				"Widzę, że jeszcze nie zabiłeś szczura. Czy chcesz, abym powiedział Tobie jak walczyć z nimi?",
				null);

		//player returns to Hayunn having killed a rat
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(100));
		actions.add(new SetQuestAction(QUEST_SLOT, "killed"));

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.INFORMATION_1,
				"Zabiłeś szczura! Teraz możesz pozwiedzać. Czy chcesz uszłyszeć o Semos?",
				new MultipleActions(actions));


	   	// The player has had enough info for now. Send them to semos. When they come back they can learn some more tips.

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 150));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAction(QUEST_SLOT, "taught"));
		reward.add(new ExamineChatAction("monogenes.png", "Monogenes", "North part of Semos city."));

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Idź ścieżką z tej wioski na wschód. Nie przegapisz Semos. Jeżeli pójdziesz i porozmawiasz z Monogenes ten stary człowiek na zdjęciu to da Tobie mapę. Powodzenia!",
			new MultipleActions(reward));

	   	// incase player didn't finish learning everything when he came after killing the rat, he must have another chance. Here it is.
		// 'little tip' is a pun as he gives some money, that is a tip, too.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "killed")),
				ConversationStates.INFORMATION_1,
		      "Szybko się uwinąłeś od momentu, gdy przyszedłeś mi oznajmić, że zabiłeś szczura! Chciałbym Ci przekazać kilka wskazówek i podpowiedzi. Czy chcesz je usłyszeć?",
				null);
		
		// Player has returned to say hi again.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "taught")),
				ConversationStates.INFORMATION_2,
		        "Witaj ponownie. Przyszedłeś, aby się dowiedzieć więcej ode mnie?",
				null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"Być może znajdziesz lochy Semos. Korytarze są tam bardzo wąskie, więc musisz uważać. Chcesz usłyszeć więcej #'Tak'?",
			null);

		npc.add(
			ConversationStates.INFORMATION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_4,
		"To proste, naprawdę. Naciskaj raz na miejsce do którego chcesz się udać. Tam jest więcej informacji, których nie mogę sobie przypomnieć. Wyleciały mi z głowy... chcesz wiedzieć gdzie można o nich poczytać?",
			null);

		final String epilog = "Na #http://www.polskagra.net możesz znaleźć wiele odpowiedzi listy wszelkiego rodzaju zwierząt, potworów i innych wrogów\n Na #http://www.polskagra.net/node/722 możesz znaleźć informacje o punktach doświadczenia i zdobywaniu poziomów\nNa #http://www.polskagra.net możesz poczytać o najlepszych wojownikach\n ";
		
			//This is used if the player returns, asks for #help and then say #yes
			npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES, new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			epilog + "Wiesz przypominasz mnie, gdy byłem młody...",
			null);

		final List<ChatAction> reward2 = new LinkedList<ChatAction>();
		reward2.add(new EquipItemAction("drewniana tarcza"));
		reward2.add(new IncreaseXPAction(20));
		reward2.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES, new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE, 
			epilog + "Cóż powodzenia w podziemiach! Ta tarcza powinna Ci pomóc. Tutaj znajdziesz sławę i chwałę. Uważaj na potwory!",
				new MultipleActions(reward2));

		npc.add(new ConversationStates[] { ConversationStates.ATTENDING,
					ConversationStates.INFORMATION_1,
					ConversationStates.INFORMATION_2,
					ConversationStates.INFORMATION_3,
					ConversationStates.INFORMATION_4},
				ConversationPhrases.NO_MESSAGES, new NotCondition(new QuestInStateCondition(QUEST_SLOT, "start")), ConversationStates.IDLE,
				"Och mam nadzieję, że ktoś się zatrzyma i porozmawia ze mną.",
				null);

		npc.setPlayerChatTimeout(TIME_OUT);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Hayunna Naratha",
				"Hayunn Naratha może nauczyć młodych bohaterów podstaw świata PolskaGra.",
				false);
		prepareHayunn();
	}

	@Override
	public String getName() {
		return "MeetHayunn";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Hayunn Naratha";
	}
}
