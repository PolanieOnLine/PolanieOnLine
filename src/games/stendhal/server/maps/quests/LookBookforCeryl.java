/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.Arrays;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.orril.magician_house.WitchNPC;
import games.stendhal.server.maps.semos.library.LibrarianNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * QUEST: Look for a book for Ceryl
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Ceryl </li>
 * <li> Jynath </li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Talk with Ceryl to activate the quest. </li>
 * <li> Talk with Jynath for the book. </li>
 * <li> Return the book to Ceryl. </li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 100 XP </li>
 * <li> some karma (10 + (5 | -5) </li>
 * <li> 50 gold coins </li>
 * </ul>
 *
 * REPETITIONS: None
 */
public class LookBookforCeryl implements QuestManuscript {
	private final static String QUEST_SLOT = "ceryl_book";

	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Znajdź Książkę Ceryla")
			.description("Ceryl chce starej książki, która została wypożyczona.")
			.internalName(QUEST_SLOT)
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Ceryl");

		quest.history()
			.whenNpcWasMet("Spotkałem Ceryla w bibliotece, on jest tam bibliotekarzem.")
			.whenQuestWasRejected("Nie chcę szukać tej książki.")
			.whenQuestWasAccepted("Obiecałem zdobyć czarną książkę od Jynath.")
			.whenTaskWasCompleted("Porozmawiałem z Jynath i zdobyłem książkę.")
			.whenQuestWasCompleted("Oddałem książkę Cerylowi i dostałem małą nagrodę.");

		quest.offer()
			.respondToRequest("Szukam bardzo specjalnej książki. Czy mógłbyś poprosić #Jynath, żeby ją zwróciła? Ma ją od miesięcy, a ludzie pytają o nią.")
			.respondToUnrepeatableRequest("Teraz nie mam dla ciebie nic.")
			.respondToAccept("Świetnie! Proszę, zdobądź ją tak szybko, jak to możliwe... jest długa lista oczekujących!")
			.respondTo("jynath").saying("Jynath to czarownica mieszkająca na południowy zachód od zamku Or'ril, na południowy zachód stąd. Więc, zdobędziesz dla mnie tę książkę?")
			.respondToReject("Och... przypuszczam, że będę musiał znaleźć kogoś innego, kto to zrobi.")
			.rejectionKarmaPenalty(5.0)
			.remind("Teraz naprawdę potrzebuję tej książki! Idź porozmawiaj z #Jynath.");

		SpeakerNPC npc = NPCList.get().get("Ceryl");
		npc.addReply("jynath", "Jynath to czarownica mieszkająca na południowy zachód od zamku Or'ril, na południowy zachód stąd.");
		step2getBook();

		quest.task()
			.requestItem(1, "księga czarna");

		quest.complete()
			.greet("Cześć, czy zdobyłeś książkę od Jynath?")
			.respondToReject("Och... przypuszczam, że będę musiał znaleźć kogoś innego, kto to zrobi.")
			.respondToAccept("O, odebrałeś książkę! Uff, dzięki!")
			.rewardWith(new IncreaseXPAction(100))
			.rewardWith(new IncreaseKarmaAction(10))
			.rewardWith(new EquipItemAction("money", 50));

		// There is no other way to get the book.
		// Remove that quest slot so that the player can get
		// it again from Jynath
		// As the book is both bound and useless outside the
		// quest, this is not a problem
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new NotCondition(new PlayerHasItemWithHimCondition("księga czarna"))),
			ConversationStates.ATTENDING,
			"Czy nie odzyskałeś jeszcze tej książki od Jynath? Proszę, poszukaj jej szybko!",
			new SetQuestAction(QUEST_SLOT, "start"));

		return quest;
	}

	private void step2getBook() {
		final SpeakerNPC npc = NPCList.get().get("Jynath");

		/**
		 * If player has quest and is in the correct state, just give him the
		 * book.
		 */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING,
			"Och, Ceryl szuka tej książki z powrotem? O rany! Całkiem o niej zapomniałam... proszę bardzo!",
			new MultipleActions(new EquipItemAction("księga czarna", 1, true), new SetQuestAction(QUEST_SLOT, "jynath")));

		/** If player keeps asking for the book, just tell him to hurry up */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath")),
			ConversationStates.ATTENDING,
			"Lepiej szybko oddaj tę książkę do #Ceryla... na pewno będzie na ciebie czekał.",
			null);

		npc.add(ConversationStates.ATTENDING, "ceryl", null,
			ConversationStates.ATTENDING,
			"Ceryl to oczywiście bibliotekarz w Semos.", null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("book", "książka"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Sssz! Koncentruję się na przepisie na tę miksturę... to trudne wyzwanie.",
			null);
	}

	public boolean removeFromWorld() {
		final boolean res = ResetSpeakerNPC.reload(new LibrarianNPC(), "Ceryl")
			&& ResetSpeakerNPC.reload(new WitchNPC(), "Jynath");
		// reload other quests associated with Ceryl
		SingletonRepository.getStendhalQuestSystem().reloadQuestSlots("obsidian_knife");
		return res;
	}
}