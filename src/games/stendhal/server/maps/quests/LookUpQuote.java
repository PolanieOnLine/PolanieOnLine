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

import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Quest to get a fishing rod 
 * <p>
 * 
 * PARTICIPANTS: <ul><li> Pequod the fisherman</ul>
 * 
 * 
 * STEPS: <ul><li> The fisherman asks you to go to the library to get him a quote of a
 * famous fisherman. <li> The player goes to the library where a book with some
 * quotes lies on the table and looks the correct one up. <li>The player goes back
 * to the fisherman and tells him the quote.</ul>
 * 
 * 
 * REWARD:
 * <ul>
 * <li> 750 XP
 * <li> some karma (10)
 * <li> A fishing rod
 * </ul>
 * 
 * REPETITIONS: <ul><li> no repetitions</ul>
 * 
 * @author dine
 */

public class LookUpQuote extends AbstractQuest {
	private static final String QUEST_SLOT = "get_fishing_rod";

	private static Map<String, String> quotes = new HashMap<String, String>();
	static {
		quotes.put("fisherman Bully", "Clownfish are always good for a laugh.");
		quotes.put("fisherman Jacky",
						"Don't mistake your trout for your old trout, she wouldn't taste so good.");
		quotes.put("fisherman Tommy",
						"I wouldn't trust a surgeonfish in a hospital, there's something fishy about them.");
		quotes.put("fisherman Sody",
				"Devout Crustaceans believe in the One True Cod.");
		quotes.put("fisherman Humphrey",
						"I don't understand why no-one buys my fish. The sign says 'Biggest Roaches in town'.");
		quotes.put("fisherman Monty",
						"My parrot doesn't like to sit on a perch. He says it smells fishy.");
		quotes.put("fisherman Charby",
						"That fish restaurant really overcooks everything. It even advertises char fish.");
		quotes.put("fisherman Ally", "Holy mackerel! These chips are tasty.");
	}


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
		res.add("Spotkałem Pequod w domku w mieście Ados i poprosił mnie o znalezienie cytatu znanego rybaka.");
		if (!player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Cytat, który muszę znaleść jest " + player.getQuest(QUEST_SLOT) + ".");
		} else {
			res.add("Znalazłem cytat dla Pequod i dostałem wędkę.");
		}
		return res;
	}

	private void createFishingRod() {
		final SpeakerNPC fisherman = npcs.get("Pequod");

		fisherman.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new GreetingMatchesNameCondition(fisherman.getName()), true,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (!player.hasQuest(QUEST_SLOT)) {
						npc.say("Witaj przybyszu! #Pomagam w drodze do zostania prawdziwym rybakiem!");
					} else if (!player.isQuestCompleted(QUEST_SLOT)) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("Witaj ponownie! Szukałeś ulubionego powiedzonka " + name + "?");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Witaj ponownie!");
					}                                                   
				}
			});

		// TODO: rewrite this to use standard conditions and actions
		fisherman.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.isQuestCompleted(QUEST_SLOT)) {
						npc.say("Nie dziękuję mam wszystko.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else if (player.hasQuest(QUEST_SLOT)) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("Już się Ciebie pytałem o przysługę! Znalazłeś ulubione powiedzonko " + name + "?");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Kiedyś miałem książkę z ulubionymi powiedzonkami rybaków, ale ją zgubiłem. Teraz nie mogę sobie przypomnieć powiedzonek. Możesz ich poszukać dla mnie?");
					}
				}
			});

		fisherman.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"W takim razie nie wyświadczę Tobie przysługi. Nigdy.", null);

		fisherman.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = Rand.rand(quotes.keySet());
					npc.say("Proszę poszukaj ulubionego powiedzonka " + name + ".");
					player.setQuest(QUEST_SLOT, name);
				}
			});

		fisherman.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_2, "Jak brzmi?", null);

		fisherman.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Nie dobrze. Mogłem mieć dla Ciebie niezłą nagrodę.", null);

		// TODO: rewrite this to use standard conditions and actions
		fisherman.addMatching(ConversationStates.QUESTION_2, Expression.JOKER, new JokerExprMatcher(), null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = player.getQuest(QUEST_SLOT);
					final String quote = quotes.get(name);

					final Sentence answer = sentence.parseAsMatchingSource();
					final Sentence expected = ConversationParser.parse(quote, new SimilarExprMatcher());

					if (answer.matchesFull(expected)) {
						npc.say("Oh racja to jest to! Jak mogłem to zapomnieć? Weź tą poręczną wędkę jako wyraz mojej wdzięczności!");
						final Item fishingRod = SingletonRepository.getEntityManager().getItem("wędka");
						fishingRod.setBoundTo(player.getName());
						player.equipOrPutOnGround(fishingRod);
						player.addXP(750);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "done");
						player.notifyWorldAboutChanges();
					} else if (ConversationPhrases.GOODBYE_MESSAGES.contains(sentence.getTriggerExpression().getNormalized())) {
						npc.say("Dowidzenia - do następnego razu!");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						npc.say("Sądzę, że się pomyliłeś. Wróć, gdy będziesz dobrze znał powiedzenie.");
						npc.setCurrentState(ConversationStates.IDLE);
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Poszukiwania Cytatu",
				"Pequod zapomniał cytatu znanego rybaka.",
				false);
		createFishingRod();
	}
	@Override
	public String getName() {
		return "LookUpQuote";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Pequod";
	}
}
