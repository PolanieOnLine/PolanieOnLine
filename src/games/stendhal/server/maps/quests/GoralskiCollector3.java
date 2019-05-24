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

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GoralskiCollector3 extends AbstractQuest {

    private static final List<String> NEEDEDGORAL3 = Arrays.asList("korale", "pas zbójecki", "złota ciupaga z wąsem", "góralski gorset", "cuha góralska", "chusta góralska", "portki bukowe", "polska tarcza ciężka");
    private static final String OLD_QUEST = "goralski_kolekcjoner2";
    private static final String QUEST_SLOT = "goralski_kolekcjoner3";

    @Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
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
		final SpeakerNPC npc = npcs.get("Gazda Bartek");

		// player says hi before starting the quest
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
				"Witoj ponownie młody bohaterze... Mam złe wieści... Częściowa moja #kolekcja góralskich ubrań się poniszczyła i mam kolejne #zadanie dla Ciebie... zebrałbyś niektóre ubrania ponownie? Powiedz tylko #'kolekcja', a będę wiedział, że chciałbyś mi pomóc.",
				null);

		// player asks what items are needed
		npc.add(ConversationStates.QUEST_3_OFFERED,
				Arrays.asList("collection", "kolekcja", "zadanie"),
				null,
				ConversationStates.QUEST_3_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingitems3(player, true);
						entity.say("Brakuje "
								+ Grammar.quantityplnoun(needed2.size(), "item", "a")
								+ ". To jest "
								+ Grammar.enumerateCollection(needed2)
								+ ". Znajdziesz dla mnie?");
					}

					@Override
					public String toString() {
						return "list missingitems3";
					}
				});
		// player says yes
		npc.add(ConversationStates.QUEST_3_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Wspaniale, ale się cieszę! Dowidzenia!");
						player.setQuest(QUEST_SLOT, "");
						player.addKarma(5.0);
					}

					@Override
					public String toString() {
						return "answer offer2";
					}
				});

		// player is not willing to help
		npc.add(ConversationStates.QUEST_3_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_3_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Cóż może ktoś inny mi pomoże.");
						player.addKarma(-5.0);
					}

					@Override
					public String toString() {
						return "answer refuse2";
					}
				});

		// player asks about an individual item. We used the trick before that all items were named by colour
		// (their subclass) - so she would tell them what colour it was. In this case it fails for elvish,
		// xeno and shadow which are not named by colour. So, this time she'll say, e.g.
		// It's a shadow item, sorry if that's not much help, so will you find them all?
		// rather than say for elf item she'd said 'It's a white item, so will you find them all?'
		// it will still work for red (red_spotted is the subclass), black dragon (black),
		// golden, mainio (primary coloured), chaos (multicoloured).
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
							raiser.say("I don't know " + obj.getOriginal() + ". Can you name me another item please?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Nie widziałeś przedtem? Cóż to jest ");

							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}

							stringBuilder.append(". Przepraszam, ale to mi nie pomaga! Znajdziesz je wszystkie?");
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
		// Just find the items and bring them to Gazda Bartek.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Gazda Bartek");

		// player returns while quest is still active
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_3,
				"Witaj z powrotem! Przyniosłeś ze sobą jakieś #przedmioty góralskie?", null);
		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_3,
				Arrays.asList("items", "przedmioty", "góralskie", "góral"),
				null,
				ConversationStates.QUESTION_3,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingitems3(player, true);
						entity.say("Chcę "
								+ Grammar.quantityplnoun(needed2.size(), "item", "a")
								+ ". To jest "
								+ Grammar.enumerateCollection(needed2)
								+ ". Przyniosłeś jakiś?");
					}

					@Override
					public String toString() {
						return "enumerate missingitems3";
					}
				});
		// player says he has a required item with him
		npc.add(ConversationStates.QUESTION_3,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_3,
				"Ooo! Jakie #przedmioty góralskie przyniosłeś mi?",
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
							if (player.drop(itemName)) {
								// register item as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all items
								missing = missingitems3(player, true);

								if (missing.isEmpty()) {
									rewardPlayer(player);
									entity.say("Wow, To niewiarygodne, mogę zobaczyć to z bliska! Wielkie dzięki! Tym razem lepiej zabezpieczę moją kolekcję góralskich ubrań. Mam dla Ciebie niespodziankę!\na"
													+ " Przyjdź do mnie za chwilę, tylko przygotuję nią, dobra? Tylko nie zapomnij!");
									player.setQuest(QUEST_SLOT, "done;rewarded");
									final Item spinka = SingletonRepository.getEntityManager().getItem("spinka");
									spinka.setBoundTo(player.getName());
									player.equipOrPutOnGround(spinka);
									player.notifyWorldAboutChanges();
									entity.setCurrentState(ConversationStates.ATTENDING);
								} else {
									entity.say("Dziękuję! Co jeszcze mi przyniosłeś?");
								}
							} else {
								entity.say("Może jestem stary, ale nie posiadasz "
												+ Grammar.a_noun(itemName)
												+ " . Czego tak naprawdę chcesz ode mnie?");
							}
						} else {
							entity.say("Już przyniosłeś mi ten góralski przedmiot! Zapomniałeś już?");
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
				"Dobrze. Jeżeli potrzebujesz pomocy to powiedz.",
				null);

		// player says he didn't bring any items to different question
		npc.add(ConversationStates.QUESTION_3,
				ConversationPhrases.NO_MESSAGES,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return !player.isQuestCompleted(QUEST_SLOT);
					}
				}, ConversationStates.ATTENDING, "Dobrze w takim razie wróć później.",
				null);

		// player returns after finishing the quest but not rewarded
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				"Witoj, a oto twoja nagroda, spójrz tylko na tą lśniącą złotym blaskiem #'spinke', czyż nie jest ona prześliczna? Proszę weź nią.. posiada magiczne właściwości... chroni odpowiednio osobę noszącą ten przedmiot. Niech Ci ona służy!",
				new MultipleActions(new EquipItemAction("spinka", 1, true), new SetQuestAction(QUEST_SLOT, "done;rewarded")));

		//		 player returns after finishing the quest and was rewarded
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "done;rewarded")),
				ConversationStates.ATTENDING,
				"Jeszcze raz dziękuję za okazaną pomoc!",
				null);
	}

	@Override
	public void addToWorld() {
		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Góralski Kolekcjoner III",
				"Kolekcjoner poprosił mnie abym przyniósł ponownie ubrania gdyż, niektóre poniszczyłu mu się. Słabo musiał je zabezpieczyć...",
				false);
	}

	private static void rewardPlayer(final Player player) {
		player.setBaseHP(30 + player.getBaseHP());
		player.heal(30, true);
		player.addKarma(35.0);
		player.addXP(100000);
    }

	@Override
	public String getName() {
		return "GoralskiCollector3";
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Jestem na etapie gromadzenia przedmiotów dla Gazdy Bartka, potrzebuje jeszcze " + Grammar.enumerateCollection(missingitems3(player, false)) + ".");
			} else {
				res.add("Znalazłem wszystkie góralskie przedmioty, o które prosił Gazda Bartek, a on wynagrodził mnie przepięknie lśniącym naszyjnikiem jakim jest spinka.");
			}
			return res;
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return "Gazda Bartek";
	}
}
