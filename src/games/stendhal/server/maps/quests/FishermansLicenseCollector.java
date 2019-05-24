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
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
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

/**
 * QUEST: Fisherman's license Collector
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Santiago the fisherman</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> The player must bring all kinds of fishes to the fisherman</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 2000 XP</li>
 * <li> some karma (25 + (5 | -5)) </li>
 * <li> The player gets a fisherman's license (i.e. fishing skills increased by
 *      0.2).</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> No repetitions.</li>
 * </ul>
 *
 * @author dine
 */

public class FishermansLicenseCollector extends AbstractQuest {

	public static final String QUEST_SLOT = "fishermans_license2";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private static final List<String> neededFish = 
		Arrays.asList("pstrąg", "okoń", "makrela", "dorsz", "płotka", "palia alpejska", "błazenek", "pokolec");

	/**
	 * Returns a list of the names of all fish that the given player still has
	 * to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of fish names
	 */
	private List<String> missingFish(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}

		final List<String> done = Arrays.asList(doneText.split(";"));
		for (final String fish : neededFish) {
			if (!done.contains(fish)) {
				if (hash) {
					result.add("#" + fish);
				} else {
					result.add(fish);
				}
			}
		}
		return result;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Santiago");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(FishermansLicenseQuiz.QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj znowu! Druga część twojego #egzaminu czeka na Ciebie!",
			null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Przynieś mi rybę po jednej z każdego #rodzaju, abym wiedział, że je znasz.",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_2_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze możesz poćwiczyć trochę dłużej.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player asks what exactly is missing
		npc.add(ConversationStates.ATTENDING, Arrays.asList("species", "rodzaju", "rodzaj"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final List<String> needed = missingFish(player, true);
					raiser.say("Oto " + Grammar.isare(needed.size())
							+ " "
							+ Grammar.quantityplnoun(needed.size(), "fish", "jeden")
							+ " wciąż brakuje: "
							+ Grammar.enumerateCollection(needed)
							+ ". Czy masz przy sobie którąś z tych ryb?");
				}
			});

		// player says he doesn't have required fish with him
		npc.add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final List<String> missing = missingFish(player, false);
					raiser.say("Daj mi znać, gdy znajdziesz "
							+ Grammar.itthem(missing.size()) + ". Dowidzenia.");
				}
			});

		// player says he has a required fish with him
		npc.add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_2, "Jaką rybę złapałeś?",
			null);

		for(final String itemName : neededFish) {
			npc.add(ConversationStates.QUESTION_2, itemName, null,
				ConversationStates.QUESTION_2, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						List<String> missing = missingFish(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register fish as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all fish
								missing = missingFish(player, true);

								if (!missing.isEmpty()) {
									raiser.say("Ta ryba wygląda nieźle! Masz inną dla mnie?");
								} else {
									player.addXP(2000);
									player.addKarma(25);
									raiser.say("Wykonałeś dobrą robotę! Teraz jesteś prawdziwym rybakiem i będziesz lepszym, gdy złowisz rybę!");
									player.setQuest(QUEST_SLOT, "done");
									// once there are other ways to increase your
									// fishing skills, increase the old skills
									// instead of just setting to 0.2.
									player.setSkill("fishing", Double.toString(0.2));
									player.notifyWorldAboutChanges();
								}
							} else {
								raiser.say("Nie próbuj mnie oszukać! Wiem, że nie masz "
										+ Grammar.a_noun(itemName)
										+ ". Co masz dla mnie?");
							}
						} else {
							raiser.say("Nie możesz oszukiwać na tym egzaminie! Wiem, że już mi dałeś tą rybę. Masz inną rybę dla mnie?");
						}
					}
				});
		}
	}

	private void step_2() {
		// Just find some of the fish somewhere and bring them to Santiago.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Santiago");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj znowu. Mam nadzieję, że się nie ociągałeś i przyniosłeś mi inny #rodzaj ryby.",
			null);

		// player returns after finishing the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj rybaku! Miło Cię znowu widzieć. Życzę szczęścia w łowieniu ryb.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Karta Rybacka część 2",
				"Jesteś prawdziwym rybakiem? Jeżeli tak to spróbuj przekonać rybaka Santiago o swoich umiejętnościach przynosząć mu rybę z każdego rodzaju.",
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
			res.add("Druga część egzaminu Santiago to połów  wiele gatunków ryb.");
			if (!isCompleted(player)) {
				res.add("Wciąż muszę przynieść " + Grammar.enumerateCollection(missingFish(player, false)) + " dla Santiago do sprawdzenia.");
			} else {
				res.add("Przyniosłem wszystkie gatunki ryb jakie Santiago chciał, a teraz jestem prawdziwym rybakiem! Będę miał więcej sukcesów podczas połowów.");
			}
			return res;
	}
	
	@Override
	public String getName() {
		return "FishermansLicenseCollector";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Santiago";
	}
}
