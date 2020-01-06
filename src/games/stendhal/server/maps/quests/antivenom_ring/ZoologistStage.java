/***************************************************************************
 *                   (C) Copyright 2019 - Arianne                          *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;

/**
 * Step where player asks NPC to extract some cobra venom to take to apothecary.
 *
 * Required items:
 * - vial
 * - venom gland
 *
 * Reward:
 * - cobra venom
 * - +5 karma
 */
 public class ZoologistStage extends AVRStage {
 	private final SpeakerNPC zoologist;
 	private final String subquestName;

	private static final int EXTRACT_TIME = 20;

	public ZoologistStage(final String npcName, final String questName) {
		super(questName);

		zoologist = SingletonRepository.getNPCList().get(npcName);
		subquestName = questName + "_extract";
	}

	@Override
	public void addToWorld() {
		prepareNPCInfo();
		prepareResponses();
		prepareRequestVenom();
		prepareExtractVenom();
	}

	private void prepareNPCInfo() {
		// prepare helpful info
		final String jobInfo = "Jestem zoologiem i pracuję tu w pełnym wymiarze godzin w sanktuarium zwierząt. Specjalizuję się w #jadowitych zwierzętach.";
		zoologist.addJob(jobInfo);
		zoologist.addHelp(jobInfo);
		zoologist.addOffer(jobInfo);
		zoologist.addReply(Arrays.asList("venomous", "jadowitych"), "Mogę użyć mojego sprzętu do #ekstrakcji trucizny z jadowitych zwierząt.");
		zoologist.addQuest("Nie potrzebuję niczego w tym momencie. Ale może mógłbyś mi pomóc #wydoić niektóre #węże.");

		// player speaks to Zoey after starting antivenom ring quest
		zoologist.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"O! Zaskoczyłeś mnie. Nie widziałam cię tam. Jestem bardzo zajęta, więc jeśli potrzebujesz czegoś, proszę, powiedz mi szybko.",
				null);
	}

	private void prepareResponses() {
		final String replyVial = "Hmmm... Nie mam tego tutaj. Ale może znajdziesz gdzieś w laboratorium.";
		final String replyGland = "Potrzebowałabym gruczołu z wystarczająco dużego węża, aby wydobyć przyzwoitą ilość.";

		zoologist.add(ConversationStates.ATTENDING,
				Arrays.asList("vial", "fiolka"),
				null,
				ConversationStates.ATTENDING,
				replyVial,
				null);

		zoologist.add(ConversationStates.QUESTION_1,
				Arrays.asList("vial", "fiolka"),
				null,
				ConversationStates.QUESTION_1,
				replyVial + " Więc... Masz te przedmioty?",
				null);

		zoologist.add(ConversationStates.ATTENDING,
				Arrays.asList("gland", "venom gland", "gruczoł", "gruczołu jadu", "gruczoł jadu"),
				null,
				ConversationStates.ATTENDING,
				replyGland,
				null);

		zoologist.add(ConversationStates.QUESTION_1,
				Arrays.asList("gland", "venom gland", "gruczoł", "gruczołu jadu", "gruczoł jadu"),
				null,
				ConversationStates.QUESTION_1,
				replyGland + " Więc... Masz te przedmioty?",
				null);
	}

	private void prepareRequestVenom() {
		// player asks for venom
		zoologist.add(ConversationStates.ATTENDING,
				Arrays.asList(
						"jameson", "apothecary", "antivenom", "extract", "cobra", "venom", "snake",
						"snakes", "poison", "milk", "wydoić", "ekstrakcji", "trucizn", "jad", "kobra", "węża", "wąż", "gruczoł"),
				new QuestActiveCondition(questName),
				ConversationStates.QUESTION_1,
				"Co jest? Potrzebujesz jadu do stworzenia antyjadu? Mogę wyodrębnić jad z #'gruczołu jadu'"
				+ " kobry, ale potrzębuję fiolki, aby gdzieś to przetrzymywać. Czy masz te przedmioty?",
				null);

		zoologist.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Och? W porządku. Wróć kiedy to zdobędziesz.",
				null);

		// player requests venom but doesn't have required items
		zoologist.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new AndCondition(
						new PlayerHasItemWithHimCondition("fiolka"),
						new PlayerHasItemWithHimCondition("gruczoł jadowy"))),
				ConversationStates.IDLE,
				"Och? Więc gdzie to jest?",
				null);

		// player requests venom and has required items
		zoologist.add(
				ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new PlayerHasItemWithHimCondition("fiolka"),
						new PlayerHasItemWithHimCondition("gruczoł jadowy")
				),
				ConversationStates.IDLE,
				"Dobra, przygotuję twój jad za około " + Integer.toString(EXTRACT_TIME) + " minut.",
				new MultipleActions(
						new DropItemAction("fiolka"),
						new DropItemAction("gruczoł jadowy"),
						new SetQuestToTimeStampAction(subquestName)
				)
		);

		// TODO: extract again in case player loses venom
	}

	private void prepareExtractVenom() {
		// player returns too soon
		zoologist.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestActiveCondition(subquestName),
						new NotCondition(new TimePassedCondition(subquestName, EXTRACT_TIME))
				),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(subquestName, EXTRACT_TIME, "Jad nie jest jeszcze gotowy. Proszę wróć za ")
		);

		// player returns after enough time has passed
		zoologist.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestActiveCondition(subquestName),
						new TimePassedCondition(subquestName, EXTRACT_TIME)
				),
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						new SayTextAction("Twój jad kobry jest gotowy."),
						new EquipItemAction("jad kobry", 1, true),
						new SetQuestAndModifyKarmaAction(subquestName, "done", 5)
				)
		);
	}
}
