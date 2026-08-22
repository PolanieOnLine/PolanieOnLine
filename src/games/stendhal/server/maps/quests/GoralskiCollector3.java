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

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class GoralskiCollector3 extends AbstractQuest {
	private static final String QUEST_SLOT = "goralski_kolekcjoner3";
	private static final String OLD_QUEST = "goralski_kolekcjoner2";
	private final SpeakerNPC npc = npcs.get("Gazda Bartek");

    private static final List<String> NEEDEDGORAL3 = Arrays.asList(
    		"złota ciupaga z wąsem", "korale", "pas zbójnicki", "kierpce",
    		"góralski kapelusz", "cuha góralska", "portki bukowe");

	/**
	 * Returns a list of the names of all items that the given player still has
	 * to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of item names
	 */
	private List<String> missingitems3(final Player player, final boolean hash) {
		String doneText2 = player.getQuest(QUEST_SLOT);
		final List<String> neededCopy2 = new LinkedList<String>(NEEDEDGORAL3);

		if (doneText2 == null) {
			doneText2 = "";
		}
		final List<String> done2 = Arrays.asList(doneText2.split(";"));
		neededCopy2.removeAll(done2);
		if (hash) {
			final List<String> result2 = new LinkedList<String>();
			for (final String item : neededCopy2) {
				result2.add("#" + item);
			}
			return result2;
		}

		return neededCopy2;
	}

	private void step_1() {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							return !player.hasQuest(QUEST_SLOT) && player.isQuestCompleted(OLD_QUEST);
						}
					}),
				ConversationStates.QUEST_3_OFFERED,
				"Witoj ponownie. Strój i zbrojownia już opowiadają kawał naszej historii. Zostało mi ostatnie #zadanie, żeby domknąć izbę pamięci.",
				null);

		npc.add(ConversationStates.QUEST_3_OFFERED,
				Arrays.asList("collection", "kolekcja", "zadanie"),
				null,
				ConversationStates.QUEST_3_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingitems3(player, true);
						entity.say("Chcę stworzyć reprezentacyjny zestaw gazdy, który połączy odświętny strój, ozdoby i broń. Część tych rzeczy już kiedyś trafiła w nasze ręce. Do pełnego zestawu brakuje "
								+ Grammar.quantityplnoun(needed2.size(), "przedmiot")
								+ ". Są to "
								+ Grammar.enumerateCollection(needed2)
								+ ". Pomożesz mi zakończyć tę pracę?");
					}

					@Override
					public String toString() {
						return "list missingitems3";
					}
			});

		npc.add(ConversationStates.QUEST_3_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Dziękuję. Kiedy zbierzemy ten zestaw, izba pamięci będzie naprawdę kompletna.");
						player.setQuest(QUEST_SLOT, "");
						player.addKarma(5.0);
					}

					@Override
					public String toString() {
						return "answer offer2";
					}
				});

		npc.add(ConversationStates.QUEST_3_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_3_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Rozumiem. Ostatni zestaw może jeszcze poczekać.");
						player.addKarma(-5.0);
					}

					@Override
					public String toString() {
						return "answer refuse2";
					}
				});

		for(final String itemName : NEEDEDGORAL3) {
			npc.add(ConversationStates.QUEST_3_OFFERED,
				itemName,
				null,
				ConversationStates.QUEST_3_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie poznaję " + obj.getOriginal() + ". Podaj proszę nazwę przedmiotu, o który pytasz.");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Ten przedmiot będzie częścią reprezentacyjnego zestawu. To ");

							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}

							stringBuilder.append(". Jeśli masz odpowiedni egzemplarz, przynieś go do izby pamięci.");
							raiser.say(stringBuilder.toString());
						}
					}

					@Override
					public String toString() {
						return "describe item";
					}
			});
		}
	}

	private void step_2() {
	}

	private void step_3() {
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_3,
				"Witaj z powrotem. Przyniosłeś coś do reprezentacyjnego #zestawu?", null);

		npc.add(ConversationStates.QUESTION_3,
				Arrays.asList("items", "przedmioty", "góralskie", "góral", "zestaw"),
				null,
				ConversationStates.QUESTION_3,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingitems3(player, true);
						entity.say("Do pełnego zestawu brakuje "
								+ Grammar.quantityplnoun(needed2.size(), "przedmiot")
								+ ". Są to "
								+ Grammar.enumerateCollection(needed2)
								+ ". Masz któryś przy sobie?");
					}

					@Override
					public String toString() {
						return "enumerate missingitems3";
					}
			});

		npc.add(ConversationStates.QUESTION_3,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_3,
				"Dobrze. Pokaż, co udało ci się zdobyć.",
				null);

		for(final String itemName : NEEDEDGORAL3) {
			npc.add(ConversationStates.QUESTION_3,
				itemName,
				null,
				ConversationStates.QUESTION_3,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						List<String> missing = missingitems3(player, false);

						if (missing.contains(itemName)) {
							if (player.isEquippedWithItemdata(itemName, "burglary")) {
								entity.say("Skradzionych rzeczy nie przyjmę. Izba pamięci ma przechowywać tradycję, a nie cudzą krzywdę.");
								return;
							}

							if (player.drop(itemName)) {
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								missing = missingitems3(player, true);

								if (missing.isEmpty()) {
									rewardPlayer(player);
									entity.say("Gotowe. Mamy pełny strój, zbrojownię i reprezentacyjny zestaw gazdy. Dzięki tobie ta historia zostanie tutaj na długo. Mam dla ciebie spinkę po moich przodkach. Skoro pomogłeś ocalić ich pamięć, chcę, żeby teraz należała do ciebie.");
									player.setQuest(QUEST_SLOT, "done;rewarded");
									final Item spinka = SingletonRepository.getEntityManager().getItem(
											"spinka", ItemCreationContext.questReward());
									spinka.setBoundTo(player.getName());
									player.equipOrPutOnGround(spinka);
									player.notifyWorldAboutChanges();
									entity.setCurrentState(ConversationStates.ATTENDING);
								} else {
									entity.say("Dziękuję. Ten element już mamy. Co jeszcze przyniosłeś?");
								}
							} else {
								entity.say("Nie masz przy sobie "
												+ itemName
												+ ". Sprawdź proszę, czy niczego nie zostawiłeś po drodze.");
							}
						} else {
							entity.say("Ten przedmiot już trafił do zestawu. Poszukajmy pozostałych.");
						}
					}

					@Override
					public String toString() {
						return "answer NEEDEDGORAL3";
					}
			});
		}

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return !player.isQuestCompleted(QUEST_SLOT);
					}
				},
				ConversationStates.ATTENDING,
				"Dobrze. Jeśli chcesz sobie przypomnieć, czego szukamy, zapytaj o zestaw.",
				null);

		npc.add(ConversationStates.QUESTION_3,
				ConversationPhrases.NO_MESSAGES,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return !player.isQuestCompleted(QUEST_SLOT);
					}
				}, ConversationStates.ATTENDING, "Dobrze. Wróć, kiedy znajdziesz kolejny element zestawu.",
				null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				"Witoj. Izba pamięci jest już kompletna, a ja wciąż jestem ci winien nagrodę. Przyjmij tę #'spinke'. Należała do moich przodków i chcę, żeby teraz służyła tobie.",
				new MultipleActions(EquipItemAction.boundQuestReward("spinka"), new SetQuestAction(QUEST_SLOT, "done;rewarded")));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "done;rewarded")),
				ConversationStates.ATTENDING,
				"Dzięki tobie izba pamięci jest kompletna. Jeszcze raz dziękuję za pomoc.",
				null);
	}

	private static void rewardPlayer(final Player player) {
		player.addKarma(65.0);
		player.addXP(150000);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Góralski Kolekcjoner III",
				"Gazda Bartek chce domknąć izbę pamięci reprezentacyjnym zestawem łączącym strój, ozdoby i rzadką złotą ciupagę z wąsem.",
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
			res.add("Pomagam Gazdzie Bartkowi przygotować ostatni reprezentacyjny zestaw do izby pamięci. Brakuje jeszcze " + Grammar.enumerateCollection(missingitems3(player, false)) + ".");
		} else {
			res.add(player.getGenderVerb("Ukończyłem") + " wraz z Gazdą Bartkiem izbę pamięci góralskiej tradycji. W podziękowaniu otrzymałem spinkę należącą do jego przodków.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "Góralski Kolekcjoner III";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
