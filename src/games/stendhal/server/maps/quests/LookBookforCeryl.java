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
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * <li> 500 XP </li>
 * <li> some karma (10 + (5 | -5) </li>
 * <li> 150 gold coins </li>
 * </ul>
 * 
 * REPETITIONS: None
 */
public class LookBookforCeryl extends AbstractQuest {
	private static final String QUEST_SLOT = "ceryl_book";



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step1LearnAboutQuest() {

		final SpeakerNPC npc = npcs.get("Ceryl");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Szukam specjalnej #książki. ", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Nie mam nic dla Ciebie.", null);
			
     /** Other conditions not met e.g. quest completed */
		npc.addReply(Arrays.asList("book", "książki", "książka"),"Jeśli chcesz dowiedzieć się więcej, porozmawiać z moim przyjacielem Wikipedian w bibliotece Ados.", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			"book", new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Czy możesz poprosić #Jynath, aby zwróciła książkę? Przetrzymuje ją od miesiąca, a ludzie szukają jej.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Wspaniale! Proszę przynieś ją szybko jak to tylko możliwe... jest długa lista oczekujących!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och... Powinienem wybrać kogoś innego do zrobienia tego.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"jynath",
			null,
			ConversationStates.QUEST_OFFERED,
			"Jynath jest wiedźmą mieszkającą na południe od zamku Or'ril, a na południowy zachód stąd. Zdobędziesz dla mnie tą książkę?",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("book", "książki", "książka"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Potrzebuję tej książki! Idź i porozmawiaj z #Jynath.", null);

		npc.add(
			ConversationStates.ATTENDING,
			"jynath",
			null,
			ConversationStates.ATTENDING,
			"Jynath jest wiedźmą mieszkającą na południe od zamku Or'ril, a na południowy zachód stąd.",
			null);
	}

	private void step2getBook() {
		final SpeakerNPC npc = npcs.get("Jynath");

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
			"Och, Ceryl chce tą książkę z powrotem? Mój boże! Kompletnie zapomniałam o tym... oto ona!",
			new MultipleActions(new EquipItemAction("księga czarna", 1, true), new SetQuestAction(QUEST_SLOT, "jynath")));

		/** If player keep asking for book, just tell him to hurry up */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath")),
			ConversationStates.ATTENDING,
			"Lepiej weź tą książkę z powrotem do #Ceryl szybko... on czeka na Ciebie.",
			null);

		npc.add(ConversationStates.ATTENDING, "ceryl", null,
			ConversationStates.ATTENDING,
			"Ceryl jest bibliotekarzem w Semos.", null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(
			ConversationStates.ATTENDING,
			"książka",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Ciii! Koncentruję się nad recepturą mikstury... jest zawiła.",
			null);
	}

	private void step3returnBook() {
		final SpeakerNPC npc = npcs.get("Ceryl");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("księga czarna"));
		reward.add(new EquipItemAction("money", 150));
		reward.add(new IncreaseXPAction(500));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "jynath"),
					new PlayerHasItemWithHimCondition("księga czarna")),
			ConversationStates.ATTENDING,
			"Och, masz książkę z powrotem! Uff, dziękuję!",
			new MultipleActions(reward));

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
			"Nie przyniosłeś tej #książki od #Jynath? Proszę idź jej poszukać. Szybko!",
			new SetQuestAction(QUEST_SLOT, null));
	}

	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Ceryl w bibliotece, jest tam bibliotekarzem");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę szukać książki");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "jynath", "done")) {
			res.add("Chcę znaleść czarną księgę");
		}
		if (questState.equals("jynath") && player.isEquipped("księga czarna")
				|| questState.equals("done")) {
			res.add("Rozmawiałem z Jynath i mam książkę");
		}
		if (questState.equals("jynath") && !player.isEquipped("księga czarna")) {
			res.add("Nie mam książki od Jynath");
		}
		if (questState.equals("done")) {
			res.add("Zwróciłem książkę Cerylowi i dostałem nagrodę.");
		}
		return res;
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Poszukiwania Książki dla Ceryla",
				"Ceryl potrzebuje starej książki.",
				false);
		step1LearnAboutQuest();
		step2getBook();
		step3returnBook();
	}

	@Override
	public String getName() {
		return "LookBookforCeryl";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ceryl";
	}
}
