/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Emotion Crystals
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Julius (the Soldier who guards the entrance to Ados City)</li>
 * <li>Crystal NPCs around Faiumoni</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Julius wants some precious stones for his wife.</li>
 * <li>Find the 5 crystals and solve their riddles.</li>
 * <li>Bring the crystals to Julius.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>65000 XP</li>
 * <li>stone legs</li>
 * <li>Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * @author AntumDeluge
 */
public class EmotionCrystals extends AbstractQuest {
	private static final String QUEST_SLOT = "emotion_crystals";

	private static final String[] crystalColors = { "czerwony", "purpurowy", "żółty", "różowy", "niebieski" };

	// Amount of time, in minutes, player must wait before retrying the riddle (24 hours)
	private static final int WAIT_TIME_WRONG = 24 * 60;
	private static final int WAIT_TIME_RETRY = 7 * 24 * 60;

	private static final int OFFSET_TIMESTAMPS = 1;
	private static final int OFFSET_SUCCESS_MARKER = 6;

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		// Only include Julius in the quest log if player has spoken to him
		if (player.isQuestInState(QUEST_SLOT, 0, "start") || player.isQuestInState(QUEST_SLOT, 0,  "rejected")) {
			res.add("Rozmawiałem z żołnierzen Juliusem, który pilnuje wejścia do Ados.");
			if (player.isQuestInState(QUEST_SLOT, 0, "rejected")) {
				res.add("Jestem emocjonalnie nie przygotowany i musiałem odrzucić jego prośbę.");
			}
			else {
				res.add("Obiecałem, że zdobędę pięć kryształów z całego Faiumoni.");
			}
		}

		List<String> gatheredCrystals = new ArrayList<String>();
		boolean hasAllCrystals = true;

		for (String color : crystalColors) {
			if (player.isEquipped(color + " kryształ emocji")) {
				gatheredCrystals.add(color + " kryształ emocji");
			} else {
				hasAllCrystals = false;
			}
		}
		if (!gatheredCrystals.isEmpty()) {
			String tell = "Znalazłem następujące kryształy: ";
			tell += Grammar.enumerateCollection(gatheredCrystals);
			res.add(tell);
		}

		if (hasAllCrystals) {
			res.add("Zdobyłem wszystkie kryształy emocji i powinienem zanieść je do Juliusa w Ados.");
		}
		
		if (player.isQuestInState(QUEST_SLOT, 0, "done")) {
			res.add("Dałem kryształy Juliusowi dla jego żony. Dostałem doświadczenie, karmę i przydatne spodnie kamienne.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Julius");


		// Player asks for quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
					new QuestNotCompletedCondition(QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED,
			"Nie widuję mojej żony zbyt często ponieważ jestem zajęty pilnowaniem wejścia. Chciałbym zrobić coś dla niej. Pomożesz mi?",
			null);

		// Player accepts quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. Chciałbym zdobyć pięć #kryształów #emocji jako prezent dla moje żony. Proszę znajdź je wszystkie i przynieś mi je.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "start"),
					new IncreaseKarmaAction(5)));

		// Player rejects quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			// Julius walks away
			ConversationStates.IDLE,
			"Hm! Zapytam kogoś innego.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(5)));

		// Player tries to leave without accepting/rejecting the quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.GOODBYE_MESSAGES,
			null,
			ConversationStates.QUEST_OFFERED,
			"To nie jest pytanie \"tak\" lub \"nie\". Zapytałem czy wyświadczysz mi przysługę?",
			null);

		// Player asks about crystals
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("crystal", "crystals", "emotion", "emotions", "emotion crystal", "emotion crystals", "emotions crystal", "emotions crystals", "kryształ", "kryształy", "kryształów", "kryształ emocji", "kryształy emocji"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Nie wiesz co to są kryształy emocji? Na pewno doświadczyłeś radości i smutku. Słyszałem, że kryształy są rozsiane po Faiumoni. Specjalne kryształy, które mogą doprowadzić do jakichkolwiek emocji. Istnieje w sumie pięć, ukrytych w podziemiach, górach, lasach. Słyszałem, że jeden stoi obok domku, w lesie.",
			null);

		// Player asks for quest after completed
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, 0, "done"),
			ConversationStates.ATTENDING,
			"Moja żona z pewnością pokocha te kryształy emocji.",
			null);

		// Player asks for quest after already started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wiem, że pytałem ciebie o parę #kryształów.",
				null);
	}


	private void prepareRiddlesStep() {
		// List of NPCs
		final List<SpeakerNPC> npcList = new ArrayList<SpeakerNPC>();

		// Add the crystals to the NPC list with their riddle
		for (String color : crystalColors) {
			npcList.add(npcs.get(Character.toUpperCase(color.charAt(0)) + color.substring(1) + " Kryształ"));
		}

		// Riddles
		final List<String> riddles = new ArrayList<String>();
		// Answers to riddles
		final List<List<String>> answers = new ArrayList<List<String>>();

		// Red Crystal (crystal of anger)
		riddles.add("Płonę jak ogień. Moja obecność za bardzo nie cieszy, a ci którzy spróbują to poczują mój gniew. Czym jestem?");
		answers.add(Arrays.asList("anger", "angry", "mad", "offended", "hostility", "hostile", "hate", "hatred", "animosity", "wściekłością", "złością", "szaleństwem", "wrogością", "nienawiścią", "niechęcią"));
		// Purple Crystal (crystal of fear)
		riddles.add("Nie śmiem wyjść bez uniknięcia konsekwencji. Próbóją mnie przekonać, ale nie będę. Drżenie jest moim ulubionym zajęciem. Czym jestem?");
		answers.add(Arrays.asList("fear", "fearful", "fearfullness", "fright", "frightened", "afraid", "scared", "strach", "strachem", "trwogą", "lękiem", "obawą"));
		// Yellow Crystal (crystal of joy)
		riddles.add("Nie można mnie zatrzymać. Tylko pozytywne, a nie negatywne uczucia są w mym sercu. Jeśli mmie uwolnisz to życie będzie jak słoneczny dzień. Czym jestem?");
		answers.add(Arrays.asList("joy", "joyful", "joyfulness", "happy", "happiness", "happyness", "cheer", "cheery",
						"cheerful", "cheerfulness", "radością", "szczęściem", "pomyślnością", "pogodnością", "wesołością"));
		// Pink Crystal (crystal of love)
		riddles.add("Dbam o wszystkie rzeczy. Jestem najczystsza ze wszystkich. Jeśli się ze mną podzielisz to na pewno odwzajemnię. Czym jestem?");
		answers.add(Arrays.asList("love", "amor", "amour", "amity", "compassion", "miłością", "amorem", "romansem", "przyjaźnią", "współczuciem"));
		// Blue Crystal (crystal of peace)
		riddles.add("Nie pozwalam, aby pewne rzeczy mi przeszkadzały. Nigdy nie jestem zbyt energiczny. Mediacja jest moją mocną stroną. Czym jestem?");
		answers.add(Arrays.asList("peace", "peaceful", "peacefullness", "serenity", "serene", "calmness", "calm", "spokojem", "pokojem"));

		// Add conversation states
		for (int n = 0; n < npcList.size(); n++)
		{
			SpeakerNPC crystalNPC = npcList.get(n);
			String rewardItem = crystalColors[n] + " kryształ emocji";
			String crystalRiddle = riddles.get(n);
			List<String> crystalAnswers = answers.get(n);

			// In place of QUEST_SLOT
			//String RIDDLER_SLOT = crystalColors.get(n) + "_crystal_riddle";

			final List<ChatAction> rewardAction = new LinkedList<ChatAction>();
			rewardAction.add(new EquipItemAction(rewardItem,1,true));
			rewardAction.add(new IncreaseKarmaAction(5));
			rewardAction.add(new SetQuestToTimeStampAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n));
			rewardAction.add(new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"));

			final List<ChatAction> wrongGuessAction = new LinkedList<ChatAction>();
			wrongGuessAction.add(new SetQuestToTimeStampAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n));
			wrongGuessAction.add(new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "wrong"));

			// Player asks about riddle
			crystalNPC.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					new OrCondition(
						new QuestNotInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
						new AndCondition(
							new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
							new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
							new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY)
						)
					)
				),
				ConversationStates.ATTENDING,
				"Odpowiedz na #zagadkę, którą mam dla ciebie...",
				null);

			// Player asks about riddle
			crystalNPC.add(ConversationStates.ATTENDING,
					Arrays.asList("riddle", "question", "query", "puzzle", "zagadka", "zagadkę", "pytanie"),
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new OrCondition(
							new QuestNotInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
							new AndCondition(
								new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
								new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
								new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY)
							)
						)
					),
					ConversationStates.ATTENDING,
					crystalRiddle,
					new SetQuestAction(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"));

			// Player gets the riddle right
			crystalNPC.add(ConversationStates.ATTENDING,
					crystalAnswers,
					new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"),
					ConversationStates.IDLE,
					"Zgadza się. Weź ten kryształ jako nagrodę.",
					new MultipleActions(rewardAction));


			// Player gets the riddle wrong
			crystalNPC.add(ConversationStates.ATTENDING,
					"",
					new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle"),
					ConversationStates.IDLE,
					"Przykro mi, ale to niepoprawna odpowiedź.",
					new MultipleActions(wrongGuessAction));

			// Player returns before time is up, to get another chance
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "wrong"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_WRONG))
					),
					ConversationStates.IDLE,
					null,
					new SayTimeRemainingAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_WRONG, "Pomyśl dobrze nad swoją odpowiedzią i wróć do mnie ponownie za"));

			// Player returns before time is up, to get another crystal
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, OFFSET_SUCCESS_MARKER + n, "riddle_solved"),
						new NotCondition(new PlayerHasItemWithHimCondition(rewardItem)),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY))
					),
					ConversationStates.IDLE,
					null,
					new SayTimeRemainingAction(QUEST_SLOT, OFFSET_TIMESTAMPS + n, WAIT_TIME_RETRY, "Zgubiłeś mój kryształ? Mogę dać ci następny za"));

			// Player can't do riddle twice while they still have the reward
			crystalNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new PlayerHasItemWithHimCondition(rewardItem)
					),
					ConversationStates.ATTENDING,
					"Mam nadzieję, że kogoś uszczęśliwisz tym kryształem!",
					null);


			// Player asks for quest without talking to Julius first
			crystalNPC.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new QuestNotStartedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"Przykro mi, ale nie mam nic do zaoferowania. Może ktoś będzie potrzebował #kryształu...",
					null);

			crystalNPC.add(ConversationStates.ATTENDING,
					Arrays.asList("crystal", "sparkling crystal", "kryształu", "kryształ"),
					new QuestNotStartedCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					"W Ados jest żołnierz, który używa kryształów do biżuterii dla swojej żony...",
					null);

		}
	}


	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Julius");

		// Reward
		final List<ChatAction> rewardAction = new LinkedList<ChatAction>();
		for (String color : crystalColors) {
			rewardAction.add(new DropItemAction(color + " kryształ emocji"));
		}
		rewardAction.add(new EquipItemAction("spodnie kamienne", 1, true));
		rewardAction.add(new IncreaseXPAction(65000));
		rewardAction.add(new IncreaseKarmaAction(15));
		rewardAction.add(new SetQuestAction(QUEST_SLOT, 0, "done"));

		// Player has all crystals
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("czerwony kryształ emocji"),
						new PlayerHasItemWithHimCondition("purpurowy kryształ emocji"),
						new PlayerHasItemWithHimCondition("żółty kryształ emocji"),
						new PlayerHasItemWithHimCondition("różowy kryształ emocji"),
						new PlayerHasItemWithHimCondition("niebieski kryształ emocji")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Przyniosłeś kryształ?",
				null);

		// Player is not carrying all the crystals
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new OrCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("czerwony kryształ emocji")),
								new NotCondition(new PlayerHasItemWithHimCondition("purpurowy kryształ emocji")),
								new NotCondition(new PlayerHasItemWithHimCondition("żółty kryształ emocji")),
								new NotCondition(new PlayerHasItemWithHimCondition("różowy kryształ emocji")),
								new NotCondition(new PlayerHasItemWithHimCondition("niebieski kryształ emocji")))),
			ConversationStates.ATTENDING,
			"Proszę przynieś mi wszystkie kryształy emocji jakie znajdziesz.",
			null);

		// Player says "yes" (has brought crystals)
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Dziękuję bardzo! Jestem pewien, że moja żona poczuje się po nich znacznie lepiej. Proszę przyjmij te spodnie kamienne jak nagrodę.",
				new MultipleActions(rewardAction));

		// Player says "no" (has not brought crystals)
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Proszę poszukaj. W międzyczasie w czym mogę pomóc?",
				null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kryształy Emocji",
				"Julius potrzebuję trochę kryształów dla swojej żony, które znajdują się gdzieś w Faiumoni.",
				false);
		prepareRequestingStep();
		prepareRiddlesStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "EmotionCrystals";
	}

	public String getTitle() {

		return "Kryształy Emocji";
	}

	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Julius";
	}
}
