/***************************************************************************
 *                   (C) Copyright 2018 - Stendhal                         *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.antivenom_ring;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;

public class ApothecaryStage extends AVRQuestStage {

	/* infostring that identifies note item */
	private static final String NOTE_INFOSTRING = "note to apothecary";

	/* items taken to apothecary to create antivenom */
	private static final String MIX_ITEMS = "pierścień leczniczy=1;jad kobry=1;mandragora=2;mufinka=20";
	private static final List<String> MIX_NAMES = Arrays.asList("pierścień leczniczy", "jad kobry", "mandragora", "mufinka");

	private static final int FUSE_TIME = MathHelper.MINUTES_IN_ONE_DAY * 3;

	public ApothecaryStage(final String npc, final String questSlot) {
		super(npc, questSlot);
	}

	@Override
	protected void addDialogue() {
		addRequestQuestDialogue();
		addGatheringItemsDialogue();
		addBusyEnhancingDialogue();
		addQuestDoneDialogue();
		addGeneralResponsesDialogue();
	}


	/**
	 * Conversation states for NPC before quest is active.
	 */
	private void addRequestQuestDialogue() {
		final SpeakerNPC apothecary = npcs.get(npcName);

		// Player asks for quest without having Klass's note
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING)),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Przykro mi, ale jestem teraz zbyt zajęty. Może mógłbyś porozmaiwać z #Klaasem.",
				null);

		// Player speaks to apothecary while carrying note.
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Oh, wiadomość od Klaasa. Jest dla mnie?",
				null);

		// Player explicitly requests "quest" while carrying note (in case note is dropped before speaking to apothecary).
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Oh, wiadomość od Klaasa. Jest dla mnie?",
				null);

		// Player accepts quest
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT, MIX_ITEMS),
						new IncreaseKarmaAction(5.0),
						new DropInfostringItemAction("karteczka", NOTE_INFOSTRING),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT,
								"Klaas poprosił mnie o pomoc tobie. Mogę zrobić tobie pierścień, który zwiększy twoją odporność na truciznę. Musisz przynieść mi [items]. Czy masz przy sobie któreś z nich?",
								false)
				)
		);

		// Player accepts quest but dropped note
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING)),
				ConversationStates.ATTENDING,
				"Okej, więc będę musiał... poczekać, gdzie zgubiłeś ten liścik?",
				null
		);

		// Player tries to leave without accepting/rejecting the quest
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				"To nie pytanie typu \"tak\" lub \"nie\". Powiedziałem czy ten liścik, który masz jest dla mnie?",
				null);

		// Player rejects quest
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				// NPC walks away
				ConversationStates.IDLE,
				"Cóż, zachowaj go.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}


	/**
	 * Conversation states for NPC while quest is active.
	 */
	private void addGatheringItemsDialogue() {
		final SpeakerNPC apothecary = npcs.get(npcName);

		final ChatCondition gatheringStateCondition = new AndCondition(
				new QuestActiveCondition(QUEST_SLOT),
				new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "enhancing;")));

		// Player asks for quest after it is started
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				gatheringStateCondition,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż czekam, aż przyniesiesz mi [items]. Masz jakiś ze sobą?"));

		// Jameson is waiting for items
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				gatheringStateCondition,
				ConversationStates.ATTENDING,
				"Witaj ponownie! Czy przyniosłeś dla mnie jakieś #przedmioty, o które prosiłem?",
				null);

		// player asks what is missing (says "items")
		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("item", "items", "ingredient", "ingredients", "przedmiot", "przedmioty", "składnik", "składniki"),
				gatheringStateCondition,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Przyniosłeś coś?", false));

		// player says has a required item with him (says "yes")
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				gatheringStateCondition,
				ConversationStates.QUESTION_2,
				"Co przyniosłeś?",
				null);

		// Player says has required items (alternate conversation state)
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				gatheringStateCondition,
				ConversationStates.QUESTION_2,
				"Co przyniosłeś?",
				null);

		// player says does not have a required item with him (says "no")
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				gatheringStateCondition,
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze. Wciąż potrzebuję [items]", false));

		// Players says does not have required items (alternate conversation state)
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				gatheringStateCondition,
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze. Wciąż potrzebuję [items]"));

		List<String> GOODBYE_NO_MESSAGES = new LinkedList<>(ConversationPhrases.GOODBYE_MESSAGES);
		GOODBYE_NO_MESSAGES.addAll(ConversationPhrases.NO_MESSAGES);

		// player says "bye" while listing items
		apothecary.add(ConversationStates.QUESTION_2,
				GOODBYE_NO_MESSAGES,
				gatheringStateCondition,
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze. Wciąż potrzebuję [items]", false));

/*		// player says he didn't bring any items (says no)
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze. Daj znać, gdy coś znajdziesz.",
				null);

		// player says he didn't bring any items to different question
		apothecary.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze. Daj znać, gdy coś znajdziesz.",
				null);
		*/

		// player offers item that isn't in the list.
		apothecary.add(ConversationStates.QUESTION_2, "",
			new AndCondition(
					gatheringStateCondition,
					new NotCondition(new TriggerInListCondition(MIX_NAMES))),
			ConversationStates.QUESTION_2,
			"Nie wieżę, że prosiłem o to.", null);

		ChatAction mixAction = new MultipleActions (
		new SetQuestAction(QUEST_SLOT, "enhancing;" + Long.toString(System.currentTimeMillis())),
		new SayTextAction("Dziękuję. Biorę się do pracy nad sprządzeniem antyjadu dla twojego pierścienia, ale to jak zjem parę mufinek. Proszę wróć za "
				+ Integer.toString(FUSE_TIME / MathHelper.MINUTES_IN_ONE_DAY) + " dni.")
		);

		/* add triggers for the item names */
		for (final String iName : MIX_NAMES) {
			apothecary.add(ConversationStates.QUESTION_2,
					iName,
					gatheringStateCondition,
					ConversationStates.QUESTION_2,
					null,
					new CollectRequestedItemsAction(
							iName,
							QUEST_SLOT,
							"Wspaniale! Masz coś jeszcze ze sobą?",
							"Już mi to przyniosłeś.",
							mixAction,
							ConversationStates.IDLE));
		}
	}


	private void addBusyEnhancingDialogue() {
		final SpeakerNPC apothecary = npcs.get(npcName);

		// Returned too early; still working
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "enhancing;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, FUSE_TIME))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, FUSE_TIME, "Jeszcze nie skończyłem pierścienia. Wróć później za "));

		final List<ChatAction> mixReward = new LinkedList<ChatAction>();
		//reward.add(new IncreaseXPAction(2000));
		//reward.add(new IncreaseKarmaAction(25.0));
		mixReward.add(new EquipItemAction("pierścień antyjadowy", 1, true));
		mixReward.add(new SetQuestAction(QUEST_SLOT, "done"));
		mixReward.add(new SetQuestAction(QUEST_SLOT + "_extract", null)); // clear sub-quest slot

		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(apothecary.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "enhancing"),
						new TimePassedCondition(QUEST_SLOT, 1, FUSE_TIME)
				),
			ConversationStates.IDLE,
			"Skończyłem przerabiać twój pierścień. Teraz zjem resztę mufinek.",
			new MultipleActions(mixReward));
	}


	/**
	 * Conversation states for NPC after quest is completed.
	 */
	private void addQuestDoneDialogue() {
		final SpeakerNPC apothecary = npcs.get(npcName);

		// Quest has previously been completed.
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Bardzo dziękuję. Minęło tak dużo czasu od kiedy jadłem mufinkę. Jesteś zadowolony z pierścienia?",
				null);

		// Player is enjoying the ring
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wspaniale!",
				null);

		// Player is not enjoying the ring
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Oh, to bardzo źle.",
				null);
	}

	private void addGeneralResponsesDialogue() {
		final SpeakerNPC apothecary = npcs.get(npcName);

		/*
        // Player asks about required items
		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("gland", "venom gland", "glands", "venom glands", "gruczoł jadowy"),
				null,
				ConversationStates.QUESTION_1,
				"Niektóre #węże mają gruczoł, w którym mieści się jad.",
				null);

		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("mandragora", "mandragoras", "root of mandragora", "roots of mandragora", "root of mandragoras", "roots of mandragoras"),
				null,
				ConversationStates.QUESTION_1,
				"To jest moje ulubione ziele i bardzo rzadkie. W Kalavan jest ukryta ścieżka wśród drzew, a na jej końcu znajdziesz to co szukasz.",
				null);
		*/
		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("cake", "fairy cake", "mufinka"),
				null,
				ConversationStates.QUESTION_1,
				"Oh, one są najlepszym lekarstwem jakie mogłem spróbować. Tylko najbardziej anielskie istoty mogą zrobić tak anielskie jedzenie.",
				null);

		// Player asks about rings
		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("ring", "rings", "pierścień", "pierścienie"),
				null,
				ConversationStates.QUESTION_1,
				"Jest wiele rodzai pierścieni.",
				null);

		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("medicinal ring", "medicinal rings", "pierścień leczniczy"),
				null,
				ConversationStates.QUESTION_1,
				"Niektóre trujące potwory noszą go ze sobą.",
				null);

		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("antivenom ring", "antivenom rings", "pierścień antyjadowy", "pierścienie antyjadowy"),
				null,
				ConversationStates.QUESTION_1,
				"Jeżeli przyniesiesz mi to co potrzebuję to będę mógł wzmocnić #pierścień #leczniczy.",
				null);

		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("antitoxin ring", "antitoxin rings", "pierścień antytoksyczny", "pierścienie antytoksyczne"),
				null,
				ConversationStates.QUESTION_1,
				"Heh! Oto ostateczna ochroną przed zatruciami. Powodzenia w zdobyciu!",
				null);
		/*
		// Player asks about snakes
		apothecary.add(ConversationStates.QUESTION_1,
				Arrays.asList("snake", "snakes", "cobra", "cobras", "wąż", "węże", "kobra", "kobry"),
				null,
				ConversationStates.QUESTION_1,
				"Słyszałem najnowszą wieść, że odkryto jamę pełną węży gdzieś w Ados, ale nie sprawdzałem tego. Ten rodzaj pracy lepiej pozostawić podróżnikom.",
				null);

        // Player asks about required items
		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("gland", "venom gland", "glands", "venom glands", "gruczoł", "jadowy", "gruczoł jadowy"),
				null,
				ConversationStates.ATTENDING,
				"Parę #węży posiada gruczoł jadowy, w których znajduje się jad.",
				null);

		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("mandragora", "mandragoras", "root of mandragora", "roots of mandragora", "root of mandragoras", "roots of mandragoras"),
				null,
				ConversationStates.ATTENDING,
				"To mój ulubiony z pośród wszystkich ziół i den z najrzadszych. Obok Kalavan jest ukryta ścieżka pomiędzy drzewami. Na jej końcu znajdziesz to czego szukasz.",
				null);
		*/
		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("cake", "fairy cake", "mufinka"),
				null,
				ConversationStates.ATTENDING,
				"Oh, są one najlepszą przekąską jaką próbowałem. Tylko najbardziej niebiańskie istoty mogły zrobić tak nieziemskie jedzenie.",
				null);

		// Player asks about rings
		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("ring", "rings", "pierścień", "pierścienie"),
				null,
				ConversationStates.ATTENDING,
				"Jest wiele różnych typów pierścieni.",
				null);

		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("medicinal ring", "medicinal rings", "pierścień leczniczy", "pierścienie lecznicze"),
				null,
				ConversationStates.ATTENDING,
				"Parę jadowitych potworów ma je przy sobie.",
				null);

		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("antivenom ring", "antivenom rings", "pierścień antyjadowy", "pierścienie antyjadowe"),
				null,
				ConversationStates.ATTENDING,
				"Jeżeli przyniesiesz mi to co potrzebuję to będę mógł wzmocnić #pierścień #leczniczy.",
				null);

		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("antitoxin ring", "antitoxin rings", "gm antitoxin ring", "gm antitoxin rings", "pierścień antytoksynowy gm", "pierścienie antytoksynowe gm"),
				null,
				ConversationStates.ATTENDING,
				"Heh! To jest ostateczna ochrona przed trucizną. Powodzenia w zdobyciu jednego!",
				null);
		/*
		// Player asks about snakes
		apothecary.add(ConversationStates.ATTENDING,
				Arrays.asList("snake", "snakes", "cobra", "cobras", "wąż", "węże", "kobra", "kobry"),
				null,
				ConversationStates.ATTENDING,
				"Słyszałem najnowszą wieść, że odkryto jamę pełną węży gdzieś w Ados, ale nie sprawdzałem tego. Ten rodzaj pracy lepiej pozostawić podróżnikom.",
				null);
		*/
	}
}
