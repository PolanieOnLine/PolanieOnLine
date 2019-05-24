/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
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
import games.stendhal.common.parser.ConvCtxForMatchingSource;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Quest to get a recipe for a potion for Imorgen
 * <p>
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li>Cameron</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> The librarian, Cameron, of Constantines Villa needs to find some books.</li>
 * <li> There are seven books missing in the library shelves.</li>
 * <li> Cameron knows the beginning of a sentence out of each book he is searching for.</li>
 * <li> You have to find the book and tell Cameron the rest of the sentence. He chooses the book randomly.</li>
 * <li> He knows then that you found the book and that these aren't lost.</li>
 * <li> You'll reward you for your efforts.</li>
 * </ul>
 * 
 * REWARD:
 *  <ul>
 *  <li>A recipe which Imorgen needs for her potion</li>
 *  </ul>
 * 
 * REPETITIONS: 
 * <ul>
 * <li>no repetitions</li>
 * </ul>
 * 
 * @author storyteller and bluelads4
 */

public class TheMissingBooks extends AbstractQuest {
	private static final String QUEST_SLOT = "find_book";

	private static Map<String, String> quotes = new HashMap<String, String>();
	static {
		quotes.put("W głębokim morzu wokół wyspy Athor...", 
						"leży, głęboko ukryty pod piaskiem - potężny skarb Faiumoni.");
		quotes.put("Jako potężny wojownik,...",
						"wiesz na pewno, że do walki potrzebujesz potężnej zbroij.");
		quotes.put("Nie wierzysz w magię? A eliksir...",
						"miłości, został zrobiony ekoma maga, działa nawet na pana Yeti.");
		quotes.put("Głodny? Spragniony? Zmęczony?...",
						"Przerwa może pomóc. Rozejrzyj się wokół pięknej natury Faiumoni, a może znajdziesz miejsce na relaks. Nawet gdy jesteś zajęty wykonywaniem zadań, mała przekąska wśród natury doda ci sił.");
		quotes.put("A tam są: dwaj nieznajomi, sami w tunelu...",
						"na wyspie Amazonek, odnajdziesz wejście do krajny pełnej radości, życia i pokoju - taką mają nadzieję.");
		quotes.put("Och nie, och nie! Co za straszna kreatura! Jest czerwona, jest olbrzymia,...",
						"jest potężna, ma potężne udeżenie...Kto to może być? To jest balrog!");
		quotes.put("Potrzebujesz mąki, pare jajek, nieco masła, trochę cukru, nieco czekolady i mleka...",
						"ciepły napój na zimę. Po jakimś czasie i skończonej pracy otrzymasz pyszne naleśniki z polewą czekoladową ");
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
		res.add("Spotkałem Cameron w Constantines Villa. Poprosił mnie abym znalazł dla niego cytat z książki.");
		if (!player.isQuestCompleted(QUEST_SLOT)) {
			res.add("To koniec tego cytatu ja muszę znaleść początek: " + player.getQuest(QUEST_SLOT) + ".");
		} else {
			res.add("Powiedziałem cytat do Cameron w zamian dostałem receptę, która może przydać się dla Imorgen.");
		}
		return res;
	}

	private void createRecipe() {
		final SpeakerNPC npc = npcs.get("Cameron");

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new GreetingMatchesNameCondition(npc.getName()), true,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (!player.hasQuest(QUEST_SLOT)) {
						npc.say("Cześć, witaj w mojej małej biblotece! Widzę, że jesteś przyjacielem Constantina, inaczej jego ochrona nie puściła by ciebie. Myślę, że mogę tobie zaufać. Mógłbyś zrobić dla mnie małą #przysługę!");
					} else if (!player.isQuestCompleted(QUEST_SLOT)) {
						final String startsentence = player.getQuest(QUEST_SLOT);
						npc.say("Witaj ponownie! Znalazłeś tę książkę, której szukam? Jak brzmi dalszy ciąg cytatu " + startsentence + "?");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Witaj ponownie!");
					}
				}
			});

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.isQuestCompleted(QUEST_SLOT)) {
						npc.say("Dziąkuje bardzo jestem już szczęśliwy. Książka, którą znalazłeś jest jedną z najcenniejszych i leży tam.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else if (player.hasQuest(QUEST_SLOT)) {
						final String startsentence = player.getQuest(QUEST_SLOT);
						npc.say("Och, a ja myślałem, że szukasz mojej książki. Szukałeś na górze, może jest tam? Jeden z cytatów w tej książce zaczyna się od słów " + startsentence + ". Proszę powiedz mi koniec tego cytatu.");
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("W mojej biblotece brakuje siedem książek. Mam nadzieje, że są one posortowane prawidłowo! Martwie się o moje bezcenne książki. Możesz poszukać tej najcenniejszej, podam ci cytat z niej?");
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Jestem zawiedziony. Z nerwów drżą mi nogi, nie mogę patrzeć na siebie, jak mogłem się tak pomylić. Nagroda umknie ci sprzed nosa. ", null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String startsentence = Rand.rand(quotes.keySet());
					npc.say("Proszę poszukaj książki, która zawiera cytat, zaczynający się od słów " + startsentence + ". Gdy ją znajdziesz powiedz mi dalszą część tego cytatu.");
					player.setQuest(QUEST_SLOT, startsentence);
				}
			});

		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_2, "No więc, jak brzmi dalszy ciąg cytatu?", null);

		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Szkoda, miałem ciekawą nagrdę dla ciebie. Kto wie może ta rzecz będzie ci potrzebna.", null);

		// TODO: rewrite this to use standard conditions and actions
		npc.addMatching(ConversationStates.QUESTION_2, Expression.JOKER, new JokerExprMatcher(), null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String startsentence = player.getQuest(QUEST_SLOT);
					final String quote = quotes.get(startsentence);
					
					final Sentence answer = sentence.parseAsMatchingSource();
					final Sentence expected = ConversationParser.parse(quote, new ConvCtxForMatchingSource());

					if (answer.matchesFull(expected)) {
						npc.say("Super, znalazłeś ją! Jestem szczęśliwy, że jedna z siedmniu książek znalazła się. Proszę, oto nagroda za twój wysiłek. Myślę, że Imorgen będzie zadowolony z tej recepty. Strzeż jej to jest orginał.");
						final Item recipe = SingletonRepository.getEntityManager().getItem("recepta");
						recipe.setBoundTo(player.getName());
						player.equipOrPutOnGround(recipe);
						player.addXP(500);
						player.setQuest(QUEST_SLOT, "done");
						player.notifyWorldAboutChanges();
					} else if (ConversationPhrases.GOODBYE_MESSAGES.contains(sentence.getTriggerExpression().getNormalized())) {
						npc.say("Czytasz...ech...zobaczymy się ponownie wkrótce!");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						npc.say("Och, Nie posiadam książki z takim cytatem. Możliwe, że znalazłeś nie tą co trzeba. Proszę poszukaj tej prawdziwej i podaj mi cytat z niej.");
						npc.setCurrentState(ConversationStates.IDLE);
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zagubione książki",
				"Cameron, Biblotekarz w Constantines Villa, zgineło mu parę drogocennych książek.",
				false);
		createRecipe();
	}
	@Override
	public String getName() {
		return "TheMissingBooks";
	
	}
	@Override
	public String getNPCName() {
		return "Cameron";
	}
}
