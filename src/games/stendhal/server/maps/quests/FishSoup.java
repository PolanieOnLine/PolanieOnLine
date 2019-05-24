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
 * QUEST: Special Fish Soup.
 * <p>
 * PARTICIPANTS: <ul><li> Florence Boullabaisse on Ados market</ul>
 * 
 * STEPS: <ul><li> Florence Boullabaisse tells you the ingredients of a special fish soup <li> You
 * collect the ingredients <li> You bring the ingredients to Florence <li> The soup
 * is served at a market table<li> Eating the soup heals you fully over time <li> Making it adds karma
 * </ul>
 * 
 * REWARD: <ul><li>healing soup <li> Karma bonus of 10 (if ingredients given individually)<li>50 XP</ul>
 * 
 * REPETITIONS: <ul><li> as many as desired <li> Only possible to repeat once every twenty
 * minutes</ul>
 * 
 * @author Vanessa Julius and Krupi (idea)
 */
public class FishSoup extends AbstractQuest {

	private static final List<String> NEEDED_FOOD = Arrays.asList("pokolec",
			"dorsz", "palia alpejska", "płotka", "błazenek", "cebula", "makrela",
			"czosnek", "por", "okoń", "pomidor");

	private static final String QUEST_SLOT = "fishsoup_maker";

	private static final int REQUIRED_MINUTES = 20;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	/**
	 * Returns a list of the names of all food that the given player still has
	 * to bring to fulfil the quest.
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
				.getItem("zupa rybna");
		final IRPZone zone = SingletonRepository.getRPWorld().getZone("0_ados_city_n2");
		// place on market table
		soup.setPosition(64, 15);
		// only allow player who made soup to eat the soup
		soup.setBoundTo(player.getName());
		// here the soup is altered to have the same heal value as the player's
		// base HP. soup is already persistent so it will last.
		soup.put("amount", player.getBaseHP());
		zone.add(soup);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Florence Boullabaisse");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.INFORMATION_1,
			"Część i witam na rynku w Ados! Mam coś naprawdę smacznego dla Ciebie. Chciałbyś #zobaczyć?",
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
			"Witaj ponownie. Wróciłeś po mój specjał zupę rybną?",
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
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES , "Przykro mi, ale muszę umyć mój garnek nim zacznę gotować moją zupę dla Ciebie. Wróć za ")
			);

		// player responds to word 'revive'
		npc.add(ConversationStates.INFORMATION_1, 
				Arrays.asList("revive", "zobaczyć"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.hasQuest(QUEST_SLOT) && player.isQuestCompleted(QUEST_SLOT)) { 
						npc.say("Mam teraz wszystko z przepisu na zupę rybną.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else {
						npc.say("Moja specjalna zupa rybna ma magiczny smak. "
								+ "Chcę, abyś przyniósł mi potrzebne #składniki.");
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
									"składników", "one")
							+ " nim ugotuję zupę: "
							+ Grammar.enumerateCollection(needed)
							+ ". Zbierzesz je?");
				}
			});

		// player is willing to collect
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1, 
			"Dokonałeś dobrego wyboru i założe się, że nie będziesz rozczarowany. Masz wszystko czego potrzebuję?",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Mam nadzieję, że kiedyś zmienisz zdanie. Tracisz to co najlepsze!", null);

		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("pokolec","dorsz", "palia alpejska", "płotka", "błazenek", "makrela", "okoń"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Są różne miejsca połowów w Faiumoni. Jeżęli chcesz wiedzieć gdzie możesz znaleść każdy z rodzjów ryby" +
			" to zajrzyj do biblioteki w Ados. Przyniesziesz mi składniki?",
			null);

		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("por", "cebula"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Znajdziesz je w Fado. Przyniesiesz mi te składniki?",
			null);
		
		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("pomidor", "czosnek"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Są z ogrodów w Kalavan City, a dostaniesz je od miłej ogrodniczki Sue, która sprzedaje te warzywa. " 
			+ "Przyniesiesz mi te składniki?", null);
	}

	private void step_2() {
		// Fetch the ingredients and bring them back to Florence.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Florence Boullabaisse");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1,
			"Witaj z powrotem! Mam nadzieje, że zdobyłeś jakieś #składniki na zupę rybną lub #wszystkie.",
			null);

		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("ingredients", "składniki","wszystkie"),
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("Wciąż potrzebuję "
							+ Grammar.quantityplnoun(needed.size(),
									"składniki", "one") + ": "
							+ Grammar.enumerateCollection(needed)
							+ ". Przyniosłeś coś może?");
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
									npc.say("Dziękuję bardzo! Co jeszcze przyniosłeś?");
								} else {
									player.addKarma(15.0);
									player.addXP(50);
									placeSoupFor(player);
									player.getStatusList().removeAll(PoisonStatus.class);
									npc.say("Zupa dla Ciebie jest na stole na rynku. Uleczy Ciebie. "
											+ "Mój magiczny sposób gotowania zupy daje też trochę karmy.");
									player.setQuest(QUEST_SLOT, "done;"
											+ System.currentTimeMillis());
									player.notifyWorldAboutChanges();
									npc.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("No nie. Nie mam czasu na żarty! Nie masz "
									+ Grammar.a_noun(itemName)
									+ " ze sobą.");
							}
						} else {
							npc.say("Już mi przyniosłeś ten składnik.");
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
			"Nie dodam tego do twojej zupy rybnej.", null);

		// allow to say goodbye while Florence is listening for food names
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES, null,
				ConversationStates.IDLE, "Dowidzenia.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING,
			"Nie jestem pewien czego ode mnie oczekujesz.", null);

		// player says he didn't bring any Food to different question
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING, "Dobrze w takim razie wróć później.",
			null);
	}

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
		if (missing.size() > 0) {
			npc.say("Nie miałeś wszystkich składników, które potrzebuje. Wciąż potrzebuję "
							+ Grammar.quantityplnoun(missing.size(),
									"składniki", "one") + ": "
							+ Grammar.enumerateCollection(missing)
							+ ". Dostaniesz złą karmę jeżeli nadal będziesz popełniał błędy takie jak ten!");
			// to fix bug [ 2517439 ] 
			player.addKarma(-5.0);
			return;
		} else {
			// you get less XP if you did it the lazy way
			// and no karma
			player.addXP(30);
			placeSoupFor(player);
			player.getStatusList().removeAll(PoisonStatus.class);
			npc.say("Zupa dla Ciebie jest na stole na rynku. Uleczy cię. Powiedz mi jeżeli mógłbym jeszcze w czymś pomóc.");
			player.setQuest(QUEST_SLOT, "done;"
					+ System.currentTimeMillis());
			player.notifyWorldAboutChanges();
			npc.setCurrentState(ConversationStates.ATTENDING);
		}
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zupa Rybna",
				"Spróbuj zdobyć wszystkie składniki, które potrzebuję do zdrowej i smacznej zupy rybnej Florence Boullabaisse.",
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
				res.add("Jestem na etapie zbierania składników do zupy rybnej. Wciąż muszę dostarczyć " + Grammar.enumerateCollection(missingFood(player, false)) + ".");
			} else if(isRepeatable(player)){
				res.add("Florencja jest gotowa zrobić dla mnie zupę jeszcze raz!");
			} else {
				res.add("Florencja zmywa teraz naczynia. Po następną zupę muszę przyjść puźniej.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "FishSoup";
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Florence Boullabaisse";
	}
}
