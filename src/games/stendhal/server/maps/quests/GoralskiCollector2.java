/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
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

public class GoralskiCollector2 extends AbstractQuest {
	private static final String QUEST_SLOT = "goralski_kolekcjoner2";
	private final SpeakerNPC npc = npcs.get("Gazda Bartek");

	private static final List<String> neededGoral = Arrays.asList("złota ciupaga", "polska płytowa tarcza",
			"polska tarcza ciężka", "polska tarcza drewniana", "polska tarcza kolcza", "polska tarcza lekka");

	public List<String> getNeededItems() {
		return neededGoral;
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
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition("goralski_kolekcjoner1"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Dobrze, że wróciłeś. Strój już mamy, ale sama izba pamięci bez dawnego uzbrojenia pokazuje tylko połowę historii. Mam następne #zadanie.",
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
							raiser.say("Zbrojownia w izbie pamięci jest już kompletna. Dziękuję ponownie.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						} else {
							raiser.say("Chcę pokazać, że góralska tradycja to nie tylko odświętny strój. Potrzebuję kilku rodzajów tarcz oraz złotej ciupagi, która stanie się głównym elementem zbrojowni. Pomożesz mi je zebrać?");
						}
					}
				});

		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Dziękuję. Powiedz #'lista', a przypomnę ci, czego jeszcze brakuje do zbrojowni.");
						player.setQuest(QUEST_SLOT, "");
					}
				});

		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Rozumiem. Zbrojownia może jeszcze poczekać.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("list", "listą", "lista"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> needed = missingGoral(player, true);
						raiser.say("Do zbrojowni brakuje "
								+ Grammar.quantityplnoun(needed.size(), "przedmiot")
								+ ". Są to "
								+ Grammar.enumerateCollection(needed)
								+ ". Masz coś z tej listy przy sobie?");
					}
				});

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Dobrze. Wróć, gdy uda ci się odnaleźć kolejny egzemplarz.");
					}
				});

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_2,
				"Pokaż, co udało ci się zdobyć.",
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
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								missing = missingGoral(player, true);

								if (!missing.isEmpty()) {
									raiser.say("Dziękuję. Ten egzemplarz trafi do zbrojowni. Masz coś jeszcze?");
								} else {
									final Item pas = SingletonRepository.getEntityManager().getItem(
											"pas zbójnicki", ItemCreationContext.quest());
									pas.setBoundTo(player.getName());
									player.equipOrPutOnGround(pas);
									player.addXP(75000);
									player.addKarma(30);
									raiser.say("Teraz izba pamięci pokazuje nie tylko strój, lecz także broń i osłony dawnych górali. Dziękuję. Przyjmij ten #'pas zbójnicki'. Zachowałem go poza ekspozycją i wolę, żeby służył komuś, kto pomógł stworzyć to miejsce.");
									player.setQuest(QUEST_SLOT, "done");
									player.notifyWorldAboutChanges();
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								raiser.say("Nie masz przy sobie "
										+ itemName
										+ ". Sprawdź proszę, czy niczego nie zostawiłeś po drodze.");
							}
						} else {
							raiser.say("Ten egzemplarz już mamy w zbrojowni. Poszukajmy pozostałych.");
						}
					}
				});
		}
	}

	private void step_2() {
	}

	private void step_3() {
		playerReturnsWhileQuestIsActive(npc);
	}

	private void playerReturnsWhileQuestIsActive(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj z powrotem. Zbrojownia wciąż czeka na kilka eksponatów. Powiedz #lista, jeśli chcesz sobie przypomnieć, czego brakuje.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Góralski Kolekcjoner II",
				"Gazda Bartek rozbudowuje izbę pamięci o zbrojownię i potrzebuje tradycyjnych tarcz oraz złotej ciupagi.",
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
			res.add("Pomagam Gazdzie Bartkowi stworzyć zbrojownię w izbie pamięci. Brakuje jeszcze " + Grammar.enumerateCollection(missingGoral(player, false)) + ".");
		} else {
			res.add(player.getGenderVerb("Zebrałem") + " wszystkie elementy zbrojowni dla Gazdy Bartka. W podziękowaniu otrzymałem pas zbójnicki.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Góralski Kolekcjoner II";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}
}
