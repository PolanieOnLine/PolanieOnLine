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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
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
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;

public class ApothecaryStage extends AVRStage {
	private final SpeakerNPC apothecary;

	/* infostring that identifies note item */
	private static final String NOTE_INFOSTRING = "liścik do aptekarza";

	/* items taken to apothecary to create antivenom */
	private static final String MIX_ITEMS = "jad kobry=1;mandragora=2;kokuda=1;mufinka=20";
	private static final List<String> MIX_NAMES = Arrays.asList("jad kobry", "mandragora", "kokuda", "mufinka");

	// time required (in minutes) to mix the antivenom
	private static final int MIX_TIME = 30;

	private static final String QUEST_STATE_NAME = "mixing";

	public ApothecaryStage(final String npcName, final String questName) {
		super(questName);

		apothecary = SingletonRepository.getNPCList().get(npcName);
	}

	public static String getMixItems() {
		return MIX_ITEMS;
	}

	@Override
	public void addToWorld() {
		addRequestQuestDialogue();
		addGatheringItemsDialogue();
		addBusyMixingDialogue();
		addDoneMixingDialogue();
		addQuestDoneDialogue();
		addGeneralResponsesDialogue();
	}

	/**
	 * Conversation states for NPC before quest is active.
	 */
	private void addRequestQuestDialogue() {
		// Player asks for quest without having Klass's note
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING)),
						new QuestNotStartedCondition(questName)),
				ConversationStates.ATTENDING,
				"Przykro mi, ale jestem teraz zbyt zajęty. Może mógłbyś porozmaiwać z #Klaasem.",
				null);

		// Player speaks to apothecary while carrying note.
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING),
						new QuestNotStartedCondition(questName)),
				ConversationStates.QUEST_OFFERED,
				"Oh, wiadomość od Klaasa. Jest dla mnie?",
				null);

		// Player explicitly requests "quest" while carrying note (in case note is dropped before speaking to apothecary).
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING),
						new QuestNotStartedCondition(questName)),
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
						new SetQuestAction(questName, MIX_ITEMS),
						new IncreaseKarmaAction(5.0),
						new DropInfostringItemAction("karteczka", NOTE_INFOSTRING),
						new SayRequiredItemsFromCollectionAction(questName,
								"Klaas poprosił mnie o pomoc tobie. Mogę zrobić mieszanine antyjadu, który później połączy się z twoim pierścieniem, by zwiększyć twoją odporność na truciznę."
								+ " Musisz przynieść mi [items]. Czy masz przy sobie któreś z nich?",
								false)
				)
		);

		// Player accepts quest but dropped note
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new NotCondition(new QuestInStateCondition(questName, "ringmaker")),
						new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", NOTE_INFOSTRING))
				),
				ConversationStates.ATTENDING,
				"Okej, więc będę musiał... poczekać, gdzie zgubiłeś ten liścik?",
				null
		);

		// Player tries to leave without accepting/rejecting the quest
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				"To nie pytanie typu #tak lub #nie. Powiedziałem czy ten liścik, który masz jest dla mnie?",
				null);

		// Player rejects quest
		apothecary.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				// NPC walks away
				ConversationStates.IDLE,
				"Cóż, zachowaj go.",
				new SetQuestAndModifyKarmaAction(questName, "rejected", -5.0));
	}


	/**
	 * Conversation states for NPC while quest is active.
	 */
	private void addGatheringItemsDialogue() {
		final ChatCondition gatheringStateCondition = new AndCondition(
				new QuestActiveCondition(questName),
				new NotCondition(new QuestInStateCondition(questName, QUEST_STATE_NAME)),
				new NotCondition(new QuestInStateCondition(questName, RingMakerStage.QUEST_STATE_NAME)),
				new NotCondition(new QuestInStateCondition(questName, 0, "ringmaker")));

		// Player asks for quest after it is started
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				gatheringStateCondition,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(questName, "Wciąż czekam, aż przyniesiesz mi [items]. Masz jakiś ze sobą?"));

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
				new SayRequiredItemsFromCollectionAction(questName, "Potrzebuję [items]. Przyniosłeś coś?", false));

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
				new SayRequiredItemsFromCollectionAction(questName, "Dobrze. Wciąż potrzebuję [items]", false));

		// Players says does not have required items (alternate conversation state)
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				gatheringStateCondition,
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(questName, "Dobrze. Wciąż potrzebuję [items]"));

		List<String> GOODBYE_NO_MESSAGES = new LinkedList<>(ConversationPhrases.GOODBYE_MESSAGES);
		GOODBYE_NO_MESSAGES.addAll(ConversationPhrases.NO_MESSAGES);

		// player says "bye" while listing items
		apothecary.add(ConversationStates.QUESTION_2,
				GOODBYE_NO_MESSAGES,
				gatheringStateCondition,
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(questName, "Dobrze. Wciąż potrzebuję [items]", false));

		// player offers item that isn't in the list.
		apothecary.add(ConversationStates.QUESTION_2, "",
			new AndCondition(
					gatheringStateCondition,
					new NotCondition(new TriggerInListCondition(MIX_NAMES))),
			ConversationStates.QUESTION_2,
			"Nie wieżę, że prosiłem o to.", null);

		ChatAction mixAction = new MultipleActions (
		new SetQuestAction(questName, QUEST_STATE_NAME + ";" + Long.toString(System.currentTimeMillis())),
		new SayTextAction("Dziękuję. Biorę się do pracy nad sprządzeniem antyjadu, ale to jak zjem parę mufinek. Proszę wróć za "
				+ Integer.toString(MIX_TIME) + " minut.")
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
							questName,
							"Wspaniale! Masz coś jeszcze ze sobą?",
							"Już mi to przyniosłeś.",
							mixAction,
							ConversationStates.IDLE));
		}
	}

	private void addBusyMixingDialogue() {
		// Returned too early; still working
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(apothecary.getName()),
						new QuestStateStartsWithCondition(questName, QUEST_STATE_NAME),
						new NotCondition(new TimePassedCondition(questName, 1, MIX_TIME))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(questName, 1, MIX_TIME, "Jeszcze nie skończyłem mieszać antyjadu. Wróć później za "));

		final List<ChatAction> mixReward = new LinkedList<ChatAction>();
		mixReward.add(new IncreaseXPAction(1000));
		mixReward.add(new IncreaseKarmaAction(50.0));
		mixReward.add(new EquipItemAction("antyjad", 1, true));
		mixReward.add(new SetQuestAction(questName, "ringmaker"));
		mixReward.add(new SetQuestAction(questName + "_extract", null)); // clear sub-quest slot

		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(apothecary.getName()),
						new QuestInStateCondition(questName, 0, QUEST_STATE_NAME),
						new TimePassedCondition(questName, 1, MIX_TIME)
				),
			ConversationStates.IDLE,
			"Skończyłem mieszać antyjad. Ognir jest wykwalifikowanym kowalem pierścieniowym. Może połączyć antyjad z twym pierścieniem."
			+ " Zapytaj jego o #'pierścień antyjadowy'. Teraz sobie zjem resztę mufinek jeśli pozwolisz.",
			new MultipleActions(mixReward));
	}

	/**
	 * Conversation states for NPC after antivenom has been acquired
	 */
	private void addDoneMixingDialogue() {
		final ChatCondition missingAntivenom = new AndCondition(
				new QuestInStateCondition(questName, 0, "ringmaker"),
				new NotCondition(new PlayerHasItemWithHimCondition("antyjad")));

		apothecary.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(questName, 0, "ringmaker"),
				new PlayerHasItemWithHimCondition("antyjad")
			),
			ConversationStates.IDLE,
			"Czy czasem nie widziałeś już twórcy pierścieni Ognira?",
			null);

		// player lost antivenom
		apothecary.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				missingAntivenom,
				ConversationStates.QUEST_OFFERED,
				"Co jest? Straciłeś antyjad? Mogę wymieszać kolejną partię, ale potrzebuję, abyś ponownie zebrał dla mnie składniki. Czy chcesz, żebym wymieszał inny antyjad?",
				null);

		// NPC offers to mix another vial of antivenom

		// this is so player doesn't lose karma by saying "no"
		apothecary.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			missingAntivenom,
			ConversationStates.IDLE,
			"No cóż, wróć do mnie, jeśli nie znajdziesz swojego antyjadu.",
			null);

		apothecary.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			missingAntivenom,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(
				new SetQuestAction(questName, MIX_ITEMS),
				new SayRequiredItemsFromCollectionAction(questName,
					"Dobra, potrzebuję, żebyś przyniósł mi [items]. Czy masz przy sobie któreś z nich?",
					false))
			);
	}

	/**
	 * Conversation states for NPC after quest is completed.
	 */
	private void addQuestDoneDialogue() {
		// Quest has previously been completed.
		apothecary.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(questName),
				ConversationStates.QUESTION_1,
				"Bardzo dziękuję. Minęło tak dużo czasu od kiedy jadłem mufinkę. Jesteś zadowolony z pierścienia?",
				null);

		// Player is enjoying the ring
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestCompletedCondition(questName),
				ConversationStates.ATTENDING,
				"Wspaniale!",
				null);

		// Player is not enjoying the ring
		apothecary.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestCompletedCondition(questName),
				ConversationStates.ATTENDING,
				"Och, to bardzo źle.",
				null);
	}

	private void addGeneralResponsesDialogue() {
		// responses to quest related items/ingredients
		apothecary.addReply(
				Arrays.asList("ring", "rings", "pierścień", "pierścienie"),
				"Jest wiele rodzai pierścieni.");
		apothecary.addReply(
				Arrays.asList("medicinal ring", "medicinal rings", "pierścień leczniczy"),
				"Niektóre trujące potwory noszą go ze sobą.");
		apothecary.addReply(
				Arrays.asList("antivenom ring", "antivenom rings", "pierścień antyjadowy", "pierścienie antyjadowy"),
				"Jeżeli przyniesiesz mi to co potrzebuję to będę mógł wzmocnić #'pierścień leczniczy'.");
		/* this item is not available
		apothecary.addReply(
				Arrays.asList("antitoxin ring", "antitoxin rings", "pierścień antytoksyczny", "pierścienie antytoksyczne"),
				"Heh! Oto ostateczna ochroną przed zatruciami. Powodzenia w zdobyciu!");
		*/
		apothecary.addReply(
				Arrays.asList("venom", "cobra venom", "jad", "jad kobry"),
				"Ktoś specjalizujący się w zwierzętach może wiedzieć, jak to zdobyć."
				+ " Proponuję odwiedzić sanktuarium w Ados.");
		apothecary.addReply(
				Arrays.asList("gland", "venom gland", "glands", "venom glands", "gruczoł", "gruczoły", "jadowy", "gruczoł jadowy", "gruczoły jadowe"),
				"Parę #węży posiada gruczoł jadowy, w których znajduje się jad.");
		apothecary.addReply(
				Arrays.asList("snake", "snakes", "cobra", "cobras", "wąż", "węże", "węży", "kobra", "kobry"),
				"Słyszałem najnowszą wieść, że odkryto jamę pełną węży gdzieś w Ados, ale"
				+ " nie sprawdzałem tego. Ten rodzaj pracy lepiej pozostawić podróżnikom.");
		apothecary.addReply(
				Arrays.asList("mandragora", "mandragoras", "root of mandragora", "roots of mandragora", "root of mandragoras", "roots of mandragoras",
						"mandragory", "korzeń mandragory", "korzenie mandragory"),
				"To mój ulubiony z pośród wszystkich ziół i ten z najrzadszych. W Faimouni jest tylko"
				+ " kilka miejsc, w których rośnie. Miej czujne oko, bo przegapisz je.");
		apothecary.addReply(
				Arrays.asList("cake", "fairy cake", "mufinka"),
				"Och, są one najlepszą przekąską jaką próbowałem. Tylko najbardziej niebiańskie istoty mogły zrobić tak nieziemskie jedzenie.");
	}
}
