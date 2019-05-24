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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.LevelBasedComparator;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * QUEST: Daily Monster Kill Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor
 * <li> some creatures
 * <p>
 * STEPS:
 * <li> talk to Mayor to get a quest to kill one of a named creature class
 * <li> kill one creature of that class
 * <li> tell Mayor that you are done
 * <li> if after 7 days you were not able to kill the creature, you have an
 * option to get another quest
 * <p>
 * REWARD: - xp - 5 karma
 * <p>
 * REPETITIONS: - once a day
 */

public class DailyMonsterQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "daily";
	private final SpeakerNPC npc = npcs.get("Mayor Sakhs");
	private static Logger logger = Logger.getLogger("DailyMonsterQuest");

	private final static int delay = MathHelper.MINUTES_IN_ONE_DAY;
	private final static int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK;


	/** All creatures, sorted by level. */
	private static List<Creature> sortedcreatures;

	private static void refreshCreaturesList(final String excludedCreature) {
		final Collection<Creature> creatures = SingletonRepository.getEntityManager().getCreatures();
		sortedcreatures = new LinkedList<Creature>();
		for (Creature creature : creatures) {
			if (!creature.isRare() && !creature.getName().equals(excludedCreature)) {
				sortedcreatures.add(creature);
			}
		}
		Collections.sort(sortedcreatures, new LevelBasedComparator());
	}

	/**
	 * constructor for quest
	 */
	public DailyMonsterQuest() {
		refreshCreaturesList(null);
	}

	static class DailyQuestAction implements ChatAction {
		//private String debugString;

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

			if (player.isBadBoy()) {
				raiser.say("Z twej ręki zginął dzielny, szlachetny i poważany rycerz! Precz mi z oczu!");
			} else {
				final String questInfo = player.getQuest(QUEST_SLOT);
				String questCount = null;
				String questLast = null;

            	String previousCreature = null;

				if (questInfo != null) {
					final String[] tokens = (questInfo + ";0;0;0").split(";");
					if(!"done".equals(tokens[0])) {
						// can't use this method because this class is static
						// previousCreature = getCreatureToKillFromPlayer(player);
						String[] split = tokens[0].split(",");
						previousCreature = split[0];
					}
					//questLast = tokens[1];
					questCount = tokens[2];
				}

				refreshCreaturesList(previousCreature);

				// Creature selection magic happens here
				final Creature pickedCreature = pickIdealCreature(player.getLevel(),
						false, sortedcreatures);

				// shouldn't happen
				if (pickedCreature == null) {
					raiser.say("Dziękuję, ale teraz nie mam dla Ciebie zadania.");
					return;
				}

				String creatureName = pickedCreature.getName();


				raiser.say("Semos potrzebuje pomocy. Idź zabij " + Grammar.a_nounCreature(creatureName)
						+ " i powiedz #załatwione, gdy skończysz.");

				questLast = "" + new Date().getTime();
				player.setQuest(
						QUEST_SLOT,
						creatureName + ",0,1,"+
						player.getSoloKill(creatureName)+","+
						player.getSharedKill(creatureName)+";" +
						questLast + ";"+
						questCount);
			}
  		}

		// Returns a random creature near the players level, returns null if
		// there is a bug.
		// The ability to set a different level is for testing purposes
		public Creature pickIdealCreature(final int level, final boolean testMode, final List<Creature> creatureList) {
			// int level = player.getLevel();

			// start = lower bound, current = upper bound, for the range of
			// acceptable monsters for this specific player
			int current = -1;
			int start = 0;

			boolean lowerBoundIsSet = false;
			for (final Creature creature : creatureList) {
				current++;
				// Set the strongest creature
				if (creature.getLevel() > level + 5) {
					current--;
					break;
				}
				// Set the weakest creature
				if (!lowerBoundIsSet && creature.getLevel() > 0
						&& creature.getLevel() >= level - 5) {
					start = current;
					lowerBoundIsSet = true;
				}
			}

			// possible with low lvl player and no low lvl creatures.
			if (current < 0) {
				current = 0;
			}

			// possible if the player is ~5 levels higher than the highest level
			// creature
			if (!lowerBoundIsSet) {
				start = current;
			}

			// make sure the pool of acceptable monsters is at least
			// minSelected, the additional creatures will be weaker
			if (current >= start) {
				final int minSelected = 5;
				final int numSelected = current - start + 1;
				if (numSelected < minSelected) {
					start = start - (minSelected - numSelected);
					// don't let the lower bound go too low
					if (start < 0) {
						start = 0;
					}
				}
			}

			// shouldn't happen
			if (current < start || start < 0
					|| current >= creatureList.size()) {
				if (testMode) {
					logger.debug("ERROR: <"+level + "> start=" + start +
							", current=" + current);
				}
				return null;
			}

			// pick a random creature from the acceptable range.
			final int result = start + new Random().nextInt(current - start + 1);
			final Creature cResult = creatureList.get(result);

			if (testMode) {
				logger.debug("OK: <" + level + "> start=" + start
						+ ", current=" + current + ", result=" + result
						+ ", cResult=" + cResult.getName() + ". OPTIONS: ");
				for (int i = start; i <= current; i++) {
					final Creature cTemp = creatureList.get(i);
					logger.debug(cTemp.getName() + ":" + cTemp.getLevel()	+ "; ");
				}
			}
			return cResult;
		}

		/*
		// Debug Only, Performs tests
		// Populates debugString with test data.
		public void testAllLevels() {
			debugString = "";
			int max = Level.maxLevel();
			// in case max level is set to infinity in the future.
			if (max > 1100) {
				max = 1100;
			}
			for (int i = 0; i <= max; i++) {
				pickIdealCreature(i, true, sortedcreatures);
			}
		}
		*/
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
		res.add("Spotkałem się z burmistrzem  w Sakhs Semos Townhall");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Semos.");
			return res;
		}

		res.add("Chcę pomóc Semos.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			final boolean questDone = new KilledForQuestCondition(QUEST_SLOT, 0)
					.fire(player, null, null);
			final String creatureToKill = getCreatureToKillFromPlayer(player);
			if (!questDone) {
				res.add("Zostałem poproszony o zabicie" + Grammar.a_nounCreature(creatureToKill)
						+ ", aby pomóc Semos. Jeszcze go nie zabiłem.");
			} else {
				res.add("Zabiłem " + creatureToKill
						+ ", aby pomóc Semos.");
			}
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			final String[] tokens = (questState + ";0;0;0").split(";");
			final String questLast = tokens[1];
			final long timeRemaining = Long.parseLong(questLast) + MathHelper.MILLISECONDS_IN_ONE_DAY
					- System.currentTimeMillis();

			if (timeRemaining > 0L) {
				res.add("Zabiłem ostatniego potwora o którego prosił mnie burmistrz i odebrałem nagrodę w ciągu 24 godzin.");
			} else {
				res.add("Zabiłem ostatniego potwora o którego prosił mnie burmistrz i teraz Semos znów potrzebuje mojej pomocy.");
			}
		}
		// add to history how often player helped Semos so far
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("pomogłem Semos "
					+ Grammar.quantityplnounCreature(repetitions, "razy") + " do tej pory.");
		}
		return res;
	}

	private String getCreatureToKillFromPlayer(Player player) {
		String actualQuestSlot = player.getQuest(QUEST_SLOT, 0);
		String[] split = actualQuestSlot.split(",");
		if (split.length > 1) {
			// only return object if the slot was in the format expected (i.e. not done;timestamp;count etc)
			return split[0];
		}
		return null;
	}

	/**
	 * player said "quest"
	 */
	private void step_1() {
		// player asking for quest when he have active non-expired quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(
								new OrCondition(
										new QuestNotStartedCondition(QUEST_SLOT),
										new QuestCompletedCondition(QUEST_SLOT))),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Już dostałeś zadanie na zgładzenie " + 
								Grammar.a_nounCreature(player.getQuest(QUEST_SLOT,0).split(",")[0]) +
								". Powiedz #załatwione kiedy to zrobisz!");
					}
				});

		// player have expired quest time
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(
								new OrCondition(
										new QuestNotStartedCondition(QUEST_SLOT),
										new QuestCompletedCondition(QUEST_SLOT))),
						new TimePassedCondition(QUEST_SLOT, 1, expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						if(player.getQuest(QUEST_SLOT, 0)!=null) {
								npc.say("Już otrzymałeś zadanie na zgładzenie " +
										Grammar.a_nounCreature(player.getQuest(QUEST_SLOT, 0).split(",")[0]) +
										". Powiedz #załatwione kiedy to zrobisz!" +
										" Jeżeli nie możesz go znaleźć to może znaczyć, że już nie przychodzi do Semos. Możesz zabić #innego potwora jeżeli chcesz.");
						}
					}
				});

		// player asking for quest before allowed time interval
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Możesz dostać tylko jedno zadanie dziennie. Proszę wróć za "));

		// player asked for quest first time or repeat it after passed proper time
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new DailyQuestAction());
	}

	/**
	 * player killing monster
	 */
	private void step_2() {
		// kill the monster
	}

	/**
	 * player said "done"
	 */
	private void step_3() {
		// player never asked for this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Obawiam się, że jeszcze nie dałem Tobie #zadania.",
				null);

		// player already completed this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już ukończyłeś ostatnie zadanie, które Tobie dałem.",
				null);

		// player didn't killed creature
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(
						        new KilledForQuestCondition(QUEST_SLOT, 0))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
							final String questKill = player.getQuest(QUEST_SLOT, 0).split(",")[0];
							npc.say("Jeszcze nie zabiłeś " + Grammar.a_nounCreature(questKill)
									+ ". Idź i zrób to i powiedz #załatwione, gdy skończysz.");
					}
				});

		// player killed creature
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT),
				        new KilledForQuestCondition(QUEST_SLOT, 0)),
				ConversationStates.ATTENDING,
				"Gratuluje! Pozwól mi podziekowac w imieniu mieszkanców Semos!",
				new MultipleActions(
						new IncreaseXPDependentOnLevelAction(5, 95.0),
						new IncreaseAtkXPDependentOnLevelAction(5, 95.0),
						new IncreaseDefXPDependentOnLevelAction(5, 95.0),
						new IncreaseKarmaAction(5.0),
						new IncrementQuestAction(QUEST_SLOT, 2, 1),
						new SetQuestToTimeStampAction(QUEST_SLOT,1),
						new SetQuestAction(QUEST_SLOT,0,"done")
		));
	}

	/**
	 * player said "another"
	 */
	private void step_4() {
		// player have no active quest and trying to get another
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Obawiam się, że jeszcze nie dałem Tobie #zadania.",
				null);

		// player have no expired quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new NotCondition(
						new TimePassedCondition(QUEST_SLOT, 1, expireDelay)),
				ConversationStates.ATTENDING,
				"Nie minęło zbyt wiele czasu od rozpoczęcia zadania. Nie pozwolę Ci poddać się tak szybko.",
				null);

		// player have expired quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new TimePassedCondition(QUEST_SLOT, 1, expireDelay),
				ConversationStates.ATTENDING,
				null,
				new DailyQuestAction());
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Dzienne Zadanie na Potwora",
				"Mayor Sakhs potrzebuje wojowników do utrzymania bezpieczeństwa w mieście Semos.",
				true);
		step_1();
		step_2();
		step_3();
		step_4();
	}

	@Override
	public String getName() {
		return "DailyMonsterQuest";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Mayor Sakhs";
	}
}
