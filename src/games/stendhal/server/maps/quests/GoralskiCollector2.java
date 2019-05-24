/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GoralskiCollector2 extends AbstractQuest {

	private static final String QUEST_SLOT = "goralski_kolekcjoner2";


	private static final List<String> neededGoral = Arrays.asList("góralski kapelusz", "cuha góralska", "portki bukowe",
								       "złota ciupaga", "kierpce", "polska tarcza kolcza");

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public List<String> getNeededItems() {
		return neededGoral;
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Gazda Bartek");
	}

	/**
	 * Returns a list of the names of all weapons that the given player still
	 * has to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of weapon names
	 */
	private List<String> missingGoral(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}
		final List<String> done = Arrays.asList(doneText.split(";"));
		for (String weapon : neededGoral) {
			if (!done.contains(weapon)) {
				if (hash) {
					weapon = "#" + weapon;
				}
				result.add(weapon);
			}
		}
		return result;
	}

	private void step_1() {
		final SpeakerNPC npc = getNPC();

		// player says hi before starting the quest
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition("goralski_kolekcjoner1"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Pozdrawiam Cię stary przyjacielu. Jeżeli sobie życzysz to mam następne #zadanie dla Ciebie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition("goralski_kolekcjoner1"), new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_2_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.isQuestCompleted(QUEST_SLOT)) {
								raiser.say("Moja góralska kolekcja jest już kompletna! Dziękuję ponownie.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							} else {
								raiser.say("Chciałbym, abyś ponownie dla mnie przyniósł góralskie przedmioty. "
										+ "Tym razem chodzi mi dokładnie o góralskie ubrania męskie. Dałbyś radę?");
							}
						}
				});

		// player is willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Cudownie! Teraz #'lista', spójrz na nią, potrzebuję właśnie takich przedmiotów. "
								+ "Jeżeli wrócisz bezpiecznie to będę miał specjalną nagrodę dla Ciebie.");
						player.setQuest(QUEST_SLOT, "");
					}
				});

		// player is not willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Cóż może ktoś inny mi pomoże.",
				null);

		// player asks what exactly is missing
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("list", "listą", "lista"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> needed = missingGoral(player, true);
						raiser.say("Oto "
								+ Grammar.isare(needed.size())
								+ " "
								+ Grammar.quantityplnoun(needed.size(), "items", "a")
								+ " w mojej nowej kolekcji ubrań brakuje wciąż: "
								+ Grammar.enumerateCollection(needed)
								+ ". Czy masz coś takiego przy sobie?");
					}
				});

		// player says he doesn't have required weapons with him
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> missing = missingGoral(player, false);
						raiser.say("Powiadom mnie jeśli coś znajdziesz "
								+ Grammar.itthem(missing.size())
								+ ". Dowidzenia.");
					}
				});

		// player says he has a required weapon with him
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_2,
				"Co znalazłeś?",
				null);

		for(final String itemName : neededGoral) {
			npc.add(ConversationStates.QUESTION_2,
				itemName,
				null,
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						List<String> missing = missingGoral(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register weapon as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all weapons
								missing = missingGoral(player, true);

								if (!missing.isEmpty()) {
									raiser.say("Dziękuję bardzo! Masz coś jeszcze dla mnie?");
								} else {
									final Item pas = SingletonRepository.getEntityManager().getItem(
											"pas zbójecki");
									pas.setBoundTo(player.getName());
									player.equipOrPutOnGround(pas);
									player.addXP(65000);
									player.addKarma(20);
									raiser.say("W końcu moja kolekcja jest kompletna! Dziękuję bardzo. Spójrz tylko na ten #'pas zbójecki', czyż nie jest on piękny? Proszę weź go... przyda Ci się pewnie.");
									player.setQuest(QUEST_SLOT, "done");
									player.notifyWorldAboutChanges();
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								raiser.say("Może jestem stary, ale nie posiadasz "
										+ Grammar.a_noun(itemName)
										+ ". Czego tak naprawdę chcesz ode mnie?");
							}
						} else {
							raiser.say("Już mam to. Masz jakąś inną broń dla mnie?");
						}
					}
				});
		}
	}

	private void step_2() {
		// Just find some of the weapons somewhere and bring them to Gazda Bartek.
	}

	private void step_3() {
		final SpeakerNPC npc = getNPC();

		// player returns while quest is still active
		playerReturnsWhileQuestIsActive(npc);

		// player returns after finishing the quest
	//	playerReturnsAfterFinishingQuest(npc);
	}

	private void playerReturnsWhileQuestIsActive(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj z powrotem. Mam nadzieję, że przyszedłeś mi pomóc z ostatnią #listą broni.",
				null);
	}

/*	private void playerReturnsAfterFinishingQuest(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new SubjectOptMatchCondition(getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj! Dziękuję za powiększenie mojej kolekcji.",
				null);
	} */

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Góralski Kolekcjoner II",
				"Gazda Bartek potrzebuje nowych ubrań góralskich - tym razem męskie! Jego kolekcja naprawdę musi być duża.. chyba.",
				true);
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
				res.add("Jestem na etapie gromadzenia przedmiotów dla Gazdy Bartka, potrzebuje jeszcze " + Grammar.enumerateCollection(missingGoral(player, false)) + ".");
			} else {
				res.add("Znalazłem wszystkie góralskie przedmioty, o które prosił Gazda Bartek, a on wynagrodził mnie przepięknym pasem zbójeckim.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "GoralskiCollector2";
	}

	@Override
	public String getNPCName() {
		return "Gazda Bartek";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}
}
