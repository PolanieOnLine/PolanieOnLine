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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

/**
 * QUEST: Cloak Collector 2
 * <p>
 * PARTICIPANTS: - Josephine, a young woman who live in Fado
 * <p>
 * STEPS:
 * <ul>
 * <li> Josephine asks you to bring her a cloak in colours she didn't get already from you
 * <li> You bring cloaks to Josephine
 * <li> Repeat until Josephine
 * received all cloaks. (Of course you can bring several cloaks at the same
 * time.)
 * <li> Josephine gives you a reward
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 100,000 XP </li>
 * <li> 100 Karma (+5 for accepting, -5 for rejecting) </li>
 * <li> scent (when ready) </li>
 * </ul>
 * <p>
 * REPETITIONS: - None.
 */
public class CloakCollector2 extends AbstractQuest {

    private static final List<String> NEEDEDCLOAKS2 = Arrays.asList("płaszcz karmazynowy", "płaszcz cieni", "płaszcz xenocyjski",
								       "płaszcz elficki", "płaszcz chaosu", "płaszcz mainiocyjski",
								       "złoty płaszcz", "czarny płaszcz smoczy");
    private static final String OLD_QUEST = "cloaks_collector";
    private static final String QUEST_SLOT = "cloaks_collector_2";

    @Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	/**
	 * Returns a list of the names of all cloaks that the given player still has
	 * to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of cloak names
	 */
	private List<String> missingcloaks2(final Player player, final boolean hash) {

		String doneText2 = player.getQuest(QUEST_SLOT);
		final List<String> neededCopy2 = new LinkedList<String>(NEEDEDCLOAKS2);

		if (doneText2 == null) {
			doneText2 = "";
		}
		final List<String> done2 = Arrays.asList(doneText2.split(";"));
		neededCopy2.removeAll(done2);
		if (hash) {
			final List<String> result2 = new LinkedList<String>();
			for (final String cloak : neededCopy2) {
				result2.add("#" + cloak);
			}
			return result2;
		}

		return neededCopy2;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Josephine");

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
				ConversationStates.QUEST_2_OFFERED,
				"Witaj ponownie przybyszu! Doszły mnie słuchy o nowych płaszczach, i żałuję że poprzednio Cię o nie nie poprosiłam, ale nie przepadałam za nimi. Obawiam się, że moja #kolekcja jest niekompletna...",
				null);

		// player asks what cloaks are needed
		npc.add(ConversationStates.QUEST_2_OFFERED,
				Arrays.asList("collection", "kolekcja"),
				null,
				ConversationStates.QUEST_2_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingcloaks2(player, true);
						entity.say("Brakuje "
								+ Grammar.quantityplnoun(needed2.size(), "cloak", "a")
								+ ". To jest "
								+ Grammar.enumerateCollection(needed2)
								+ ". Znajdziesz dla mnie?");
					}

					@Override
					public String toString() {
						return "list missingcloaks2";
					}
				});
		// player says yes
		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Wspaniale! Jestem teraz szczęśliwa! Do widzenia!");
						player.setQuest(QUEST_SLOT, "");
						player.addKarma(5.0);
					}

					@Override
					public String toString() {
						return "answer offer2";
					}
				});

		// player is not willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_2_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						entity.say("Och ... nie jesteś zbyt przyjazny. Powiedziałeś tak?");
						player.addKarma(-5.0);
					}

					@Override
					public String toString() {
						return "answer refuse2";
					}
				});

		// player asks about an individual cloak. We used the trick before that all cloaks were named by colour
		// (their subclass) - so she would tell them what colour it was. In this case it fails for elvish,
		// xeno and shadow which are not named by colour. So, this time she'll say, e.g.
		// It's a shadow cloak, sorry if that's not much help, so will you find them all?
		// rather than say for elf cloak she'd said 'It's a white cloak, so will you find them all?'
		// it will still work for red (red_spotted is the subclass), black dragon (black),
		// golden, mainio (primary coloured), chaos (multicoloured).
		for(final String itemName : NEEDEDCLOAKS2) {
			npc.add(ConversationStates.QUEST_2_OFFERED,
				itemName,
				null,
				ConversationStates.QUEST_2_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("I don't know " + obj.getOriginal() + ". Can you name me another cloak please?");
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
		npc.add(ConversationStates.QUEST_2_OFFERED,
				"",
				null,
				ConversationStates.QUEST_2_OFFERED,
				"Sorry, I don't know about that. Please name me another cloak.",
				null);
	}

	private void step_2() {
		// Just find the cloaks and bring them to Josephine.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Josephine");

		// player returns while quest is still active
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_2,
				"Witaj z powrotem! Przyniosłeś ze sobą jakieś #płaszcze?", null);
		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_2,
				Arrays.asList("cloaks", "płaszcze"),
				null,
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						final List<String> needed2 = missingcloaks2(player, true);
						entity.say("Chcę "
								+ Grammar.quantityplnoun(needed2.size(), "cloak", "a")
								+ ". To jest "
								+ Grammar.enumerateCollection(needed2)
								+ ". Przyniosłeś jakiś?");
					}

					@Override
					public String toString() {
						return "enumerate missingcloaks2";
					}
				});
		// player says he has a required cloak with him
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_2,
				"Ooo! Jakie #płaszcze mi przyniosłeś?",
				null);

		for(final String itemName : NEEDEDCLOAKS2) {
			npc.add(ConversationStates.QUESTION_2,
				itemName,
				null,
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser entity) {
						List<String> missing = missingcloaks2(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register cloak as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all cloaks
								missing = missingcloaks2(player, true);

								if (missing.isEmpty()) {
									rewardPlayer(player);
									entity.say("Och, jej! Jesteś bardzo miły. Mogę się założyć, że masz wspaniałą Karmę! Słuchaj chcę Cię nagrodzić czymś specjalnym, ale jeszcze nie jest to gotowe. Mógłbyś przyjść za jakiś czas i przypomnieć mi. Nie chcę zapomnieć!");
									player.setQuest(QUEST_SLOT, "done;rewarded");
									final Item boots = SingletonRepository.getEntityManager().getItem("buty zabójcy");
									boots.setBoundTo(player.getName());
									player.equipOrPutOnGround(boots);
									player.notifyWorldAboutChanges();
									entity.setCurrentState(ConversationStates.ATTENDING);
								} else {
									entity.say("Dziękuję! Co jeszcze mi przyniosłeś?");
								}
							} else {
								entity.say("Och, jestem rozczarowana. Nie masz "
												+ itemName
												+ " ze sobą.");
							}
						} else {
							entity.say("Jesteś strasznie zapominalski. Już mi przyniosłeś ten płaszcz.");
						}
					}

					@Override
					public String toString() {
						return "answer neededcloaks2";
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

		// player says he didn't bring any cloaks to different question
		npc.add(ConversationStates.QUESTION_2,
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
				"Och! Nie nagrodziłam Ciebie za ponowną pomoc! Weź te buty. Sądzę, że są wspaniałe, ale nie pasują na mnie :(",
				new MultipleActions(new EquipItemAction("buty zabójcy", 1, true), new SetQuestAction(QUEST_SLOT, "done;rewarded")));

		//		 player returns after finishing the quest and was rewarded
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "done;rewarded")),
				ConversationStates.ATTENDING,
				"Jeszcze raz dziękuję za okazaną pomoc! Płaszcze wyglądają wspaniale!",
				null);
	}

	@Override
	public void addToWorld() {
		step_1();
		step_2();
		step_3();
		fillQuestInfo(
				"Płaszcze dla Kolekcjonera II",
				"Josephine potrzebuję więcej płaszczy!",
				false);
	}

	private static void rewardPlayer(final Player player) {
		player.addKarma(100.0);
		player.addXP(100000);
      	}

	@Override
	public String getName() {
		return "CloakCollector2";
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Muszę zebrać więcej płaszczy dla Josephine. Wciąż muszę " + Grammar.enumerateCollection(missingcloaks2(player, false)) + " zebrać.");
			} else {
				res.add("Mam wszystkie płaszcze jakie Josephine chciała, a ona dała mi wspaniałe buty mordercy.");
			}
			return res;
	}

	// The previous quest likely requires at least this level.
	// When adding the hint check, remember to check if the CloakCollector quest is completed.
	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
	@Override
	public String getNPCName() {
		return "Josephine";
	}
}
