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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.IRPZone;

/**
 * QUEST: Special Soup.
 * <p>
 * PARTICIPANTS: <ul><li> Old Mother Helena in Fado tavern</ul>
 * 
 * STEPS: <ul><li> Old Mother Helena tells you the ingredients of a special soup <li> You
 * collect the ingredients <li> You bring the ingredients to the tavern <li> The soup
 * is served at table<li> Eating the soup heals you fully over time <li> Making it adds karma
 * </ul>
 * 
 * REWARD: <ul><li>healing soup <li> Karma bonus of 5 (if ingredients given individually)<li>20 XP</ul>
 * 
 * REPETITIONS: <ul><li> as many as desired <li> Only possible to repeat once every ten
 * minutes</ul>
 * 
 * @author kymara
 */
public class Soup extends AbstractQuest {

	private static final List<String> NEEDED_FOOD = Arrays.asList("marchew",
			"szpinak", "cukinia", "kapusta", "sałata", "cebula", "kalafior",
			"brokuł", "por");

	private static final String QUEST_SLOT = "soup_maker";

	private static final int REQUIRED_MINUTES = 10;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	/**
	 * Returns a list of the names of all food that the given player still has
	 * to bring to fulfill the quest.
	 * 
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of food item names
	 */
	private List<String> missingFood(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}
		final List<String> done = Arrays.asList(doneText.split(";"));
		for (String ingredient : NEEDED_FOOD) {
			if (!done.contains(ingredient)) {
				if (hash) {
					ingredient = "#" + ingredient;
				}
				result.add(ingredient);
			}
		}
		return result;
	}

	/**
	 * Serves the soup as a reward for the given player.
	 * @param player to be rewarded
	 */
	private void placeSoupFor(final Player player) {
		final Item soup = SingletonRepository.getEntityManager()
				.getItem("zupa");
		final IRPZone zone = SingletonRepository.getRPWorld().getZone("int_fado_tavern");
		// place on table (for effect only :) )
		soup.setPosition(17, 23);
		// only allow player who made soup to eat the soup
		soup.setBoundTo(player.getName());
		// here the soup is altered to have the same heal value as the player's
		// base HP. soup is already persistent so it will last.
		soup.put("amount", player.getBaseHP());
		zone.add(soup);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Old Mother Helena");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.INFORMATION_1,
			"Witaj nieznajomy. Wyglądasz na zmęczonego podróżami. Wiem co mogłoby Cię #ożywić.",
			null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),  
			ConversationStates.QUEST_OFFERED,
			"Witaj ponownie. Przyszedłeś po więcej smacznej zupy?",
			null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))), 
				ConversationStates.ATTENDING, 
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES , "Mam nadzieję, że nie przyszedłeś po więcej zupy, ponieważ nie skończyłam zmywać naczyń. Proszę wróć za ")
			);

		// player responds to word 'revive'
		npc.add(ConversationStates.INFORMATION_1, 
				Arrays.asList("revive", "ożywić"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.hasQuest(QUEST_SLOT) && player.isQuestCompleted(QUEST_SLOT)) { 
						// to be honest i don't understand when this
								// would be implemented. i put the text i
								// want down in stage 3 and it works fine.
						npc.say("Mam wszystkie składniki do mojego przepisu.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else {
						npc.say("Moja specjalna zupa posiada magiczne właściwości. "
								+ "Potrzebuję Ciebie, abyś przyniósł #składniki.");
					}
				}
			});

		// player asks what exactly is missing
		npc.add(ConversationStates.QUEST_OFFERED, Arrays.asList("ingredients", "składniki"), null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("Potrzebuję "
							+ Grammar.quantityplnoun(needed.size(),
									"składników", "jeden")
							+ " nim zrobię zupę: "
							+ Grammar.enumerateCollection(needed)
							+ ". Uzbierasz to wszystko?");
				}
			});

		// player is willing to collect
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1, 
			"Dokonałeś słusznego wyboru. Masz coś czego potrzebuję?",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och, nie ważne. Twoja strata.", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("szpinak", "cukinia", "cebula", "kalafior", "brokuł", "por"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Znajdziesz to na działkach w Fado. Przyniesiesz mi składniki?",
			null);

		// players asks about the vegetables individually
		npc.add(ConversationStates.QUEST_OFFERED, "kapusta", null,
			ConversationStates.QUEST_OFFERED,
			"Rośnie w domu w doniczkach. Ktoś taki jak czarownica lub elf mogą spowodować, że wyrośnie. "
					+ "Przyniosłeś składniki?", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("sałata", "marchew"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Zazwyczaj zdobywam importowane z Semos. Czy chcesz zupę?",
			null);
	}

	private void step_2() {
		// Fetch the ingredients and bring them back to Helena.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Old Mother Helena");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1,
			"Witaj ponownie! Mam nadzieje, że zebrałeś jakieś #składniki na zupę.",
			null);

		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("ingredients", "składniki"),
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("Wciąż potrzebuję "
							+ Grammar.quantityplnoun(needed.size(),
									"składnik", "jeden") + ": "
							+ Grammar.enumerateCollection(needed)
							+ ". Przyniosłeś coś?");
				}
			});

		// player says he has a required ingredient with him
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, "Co przyniosłeś?", null);

		for(final String itemName : NEEDED_FOOD) {
			npc.add(ConversationStates.QUESTION_1, itemName, null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						List<String> missing = missingFood(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register ingredient as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all Food
								missing = missingFood(player, true);

								if (!missing.isEmpty()) {
									npc.say("Dziękuję bardzo! Co jeszcze mi przyniosłeś?");
								} else {
									player.addKarma(5.0);
									player.addXP(20);
									/*
									 * place soup after XP added otherwise
									 * the XP change MIGHT change level and
									 * player MIGHT gain health points which
									 * changes the base HP, which is desired
									 * to be accurate for the place soup
									 * stage
									 */
									placeSoupFor(player);
									player.getStatusList().removeAll(PoisonStatus.class);
									npc.say("Zupę masz na stole. Wyleczy Cię. "
											+ "Mój magiczny sposób robienia zupy da Ci też trochę karmy.");
									player.setQuest(QUEST_SLOT, "done;"
											+ System.currentTimeMillis());
									player.notifyWorldAboutChanges();
									npc.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Chyba się pomyliłeś. Nie masz "
									+ Grammar.a_noun(itemName)
									+ " przy sobie.");
							}
						} else {
							npc.say("Już mi przyniosłeś to warzywo.");
						}
					}
			});
		}
		
		// Perhaps player wants to give all the ingredients at once
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("everything", "wszystko"),
				null,
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
			    @Override
			    public void fire(final Player player, final Sentence sentence,
					   final EventRaiser npc) {
			    	checkForAllIngredients(player, npc);
			}
		});

		// player says something which isn't in the needed food list.
		npc.add(ConversationStates.QUESTION_1, "",
			new NotCondition(new TriggerInListCondition(NEEDED_FOOD)),
			ConversationStates.QUESTION_1,
			"Nie dodam tego do twojej zupy.", null);

		// allow to say goodbye while Helena is listening for food names
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES, null,
				ConversationStates.IDLE, "Dowidzenia.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING,
			"Nie wiem czego chcesz ode mnie.", null);

		// player says he didn't bring any Food to different question
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING, "Dobrze. Przyjdź później.",
			null);
	}

	// if we're checking all at once it's a bit different method
	// also player gets no karma (don't get karma for being lazy)
	private void checkForAllIngredients(final Player player, final EventRaiser npc) {
		List<String> missing = missingFood(player, false);
		for (final String food : missing) {
		if (player.drop(food)) {							
			// register ingredient as done
			final String doneText = player.getQuest(QUEST_SLOT);
			player.setQuest(QUEST_SLOT, doneText + ";"
			+ food);
			}
		}
		// check if the player has brought all Food
		missing = missingFood(player, true);
		if (!missing.isEmpty()) {
			npc.say("Nie masz wszystkich potrzebnych składników. Wciąż potrzebuję "
							+ Grammar.quantityplnoun(missing.size(),
									"składnika", "jeden") + ": "
							+ Grammar.enumerateCollection(missing)
							+ ". Dostaniesz mało karmy jeżeli nadal będziesz popełniał takie błędy!");
			// to fix bug [ 2517439 ] 
			player.addKarma(-5.0);
			return;
		} else {
			// you get less XP if you did it the lazy way
			// and no karma
			player.addXP(20);
			placeSoupFor(player);
			player.getStatusList().removeAll(PoisonStatus.class);
			npc.say("Zupa, która jest na stole jest dla Ciebie. Uleczy Cię. Daj mi znać jeżeli mogłabym jeszcze w czymś pomóc.");
			player.setQuest(QUEST_SLOT, "done;"
					+ System.currentTimeMillis());
			player.notifyWorldAboutChanges();
			npc.setCurrentState(ConversationStates.ATTENDING);
		}
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zupa",
				"Mother Helena robi pożywną i smaczną zupę..",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Zbieram składniki do zupy jarzynowej. Wciąż potrzebuje " + Grammar.enumerateCollection(missingFood(player, false)) + ".");
			} else if(isRepeatable(player)){
				res.add("Old Mother Helena może zrobić mi następną zupę!");
			} else {
				res.add("Dostałem kilka smacznych zup. Old Mother Helena jest teraz zajęta zmywaniem naczyń.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "Soup";
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "Old Mother Helena";
	}
}
