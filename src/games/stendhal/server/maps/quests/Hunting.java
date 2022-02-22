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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import games.stendhal.common.Level;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.LevelBasedComparator;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.KillsForQuestCounter;

public class Hunting extends AbstractQuest {
	private static final String QUEST_SLOT = "hunting";
	private final SpeakerNPC npc = npcs.get("Janisław");

	private static final Logger logger = Logger.getLogger(Hunting.class);

	/** All creatures, sorted by level. */
	private static List<Creature> sortedcreatures;

	private static void refreshCreaturesList(final String excludedCreature) {
		final Collection<Creature> creatures = SingletonRepository.getEntityManager().getCreatures();
		sortedcreatures = new LinkedList<Creature>();
		for (Creature creature : creatures) {
			if (!creature.isAbnormal() && !creature.getName().equals(excludedCreature)) {
				sortedcreatures.add(creature);
			}
		}
		Collections.sort(sortedcreatures, new LevelBasedComparator());
	}

	/**
	 * constructor for quest
	 */
	public Hunting() {
		refreshCreaturesList(null);
	}

	static class HuntingAction implements ChatAction {
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
					questLast = tokens[1];
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

				int randomKillCount = Rand.roll1D50();
				String count;
				if (randomKillCount == 1) {
					count = " sztuki";
				} else {
					count = " sztuk";
				}
				raiser.say("Dobrze więc, upoluj dla mnie " + creatureName
						+ " w ilości " + randomKillCount + count + " i powiedz #załatwione, gdy skończysz.");

				questLast = "" + new Date().getTime();
				player.setQuest(
						QUEST_SLOT,
						creatureName + ",0,"+
						randomKillCount + "," +
						player.getSoloKill(creatureName)+"," +
						player.getSharedKill(creatureName)+";" +
						questLast + ";" +
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
				// Set the a little strongest creature
				if (creature.getLevel() > level + 4) {
					current--;
					break;
				}
				// Set the weakest creature
				if (!lowerBoundIsSet && creature.getLevel() > 0
						&& creature.getLevel() >= level - 10) {
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
				final int minSelected = 10;
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

	private String getCountCreatureToKillFromPlayer(Player player) {
		String actualQuestSlot = player.getQuest(QUEST_SLOT, 0);
		String[] split = actualQuestSlot.split(",");
		if (split.length > 3) {
			// only return object if the slot was in the format expected (i.e. not done;timestamp;count etc)
			return split[2];
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
				new AndCondition(new NotCondition(new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(QUEST_SLOT)))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Już " + Grammar.genderVerb(player.getGender(), "dostałeś") + " zadanie na upolowanie " +
								player.getQuest(QUEST_SLOT,0).split(",")[0] +
								" w ilości " +
								player.getQuest(QUEST_SLOT,0).split(",")[2] +
								". Powiedz #załatwione kiedy to zrobisz!");
					}
				});

		// player asked for quest first time or repeat it after passed proper time
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING,
				null,
				new HuntingAction());
	}

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
							final String questCountKill = player.getQuest(QUEST_SLOT, 0).split(",")[2];
							npc.say("Jeszcze nie upolowałeś " + questCountKill + " " + questKill
									+ ". Idź i zrób to oraz powiedz #załatwione, gdy skończysz.");
					}
				});

		final List<ChatAction> rewards = new LinkedList<ChatAction>();
		rewards.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		rewards.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		rewards.add(new ChatAction() {
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final int start = Level.getXP(player.getLevel());
				final int next = Level.getXP(player.getLevel() + 1);

				if (player.getNumberOfRepetitions(QUEST_SLOT, 2) <= 19) {
					int reward = (int) ((next - start) / 6);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(60.0);
					}
					player.addXP(reward);
					player.addKarma(5);

					if (player.getNumberOfRepetitions(QUEST_SLOT, 2) == 10) {
						final Item specialRing = SingletonRepository.getEntityManager().getItem("pierścień powrotu");
						specialRing.setBoundTo(player.getName());
						player.equipOrPutOnGround(specialRing);
						player.notifyWorldAboutChanges();
					}

					final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
					if (player.getLevel() < 10) {
						money.setQuantity(Rand.roll1D100());
					} else if (player.getLevel() >= 10 && player.getLevel() <= 50) {
						money.setQuantity(Rand.roll1D1000());
					} else {
						money.setQuantity(Rand.roll1D2000());
					}
					player.equipOrPutOnGround(money);
				} else if (player.getNumberOfRepetitions(QUEST_SLOT, 2) > 19 && player.getNumberOfRepetitions(QUEST_SLOT, 2) <= 34) {
					int reward = (int) ((next - start) / 5);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(90.0);
					}
					player.addXP(reward);
					player.addKarma(10);

					final StackableItem goldenbar = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka złota");
					if (player.getLevel() < 50) {
						goldenbar.setQuantity(Rand.roll1D10());
					} else {
						goldenbar.setQuantity(Rand.roll1D20());
					}
					player.equipOrPutOnGround(goldenbar);
				} else if (player.getNumberOfRepetitions(QUEST_SLOT, 2) > 34) {
					int reward = (int) ((next - start) / 4);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(110.0);
					}
					player.addXP(reward);
					player.addKarma(15);

					final StackableItem mithrilbar = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka mithrilu");
					if (player.getLevel() < 90) {
						mithrilbar.setQuantity(Rand.roll1D3());
					} else {
						mithrilbar.setQuantity(Rand.roll1D6());
					}
					player.equipOrPutOnGround(mithrilbar);
				}
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
				"Gratuluje! Zasłużyłeś na odpowiednią nagrodę!",
				new MultipleActions(rewards));
	}

	private String howManyWereKilled(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		final String creatureToKill = getCreatureToKillFromPlayer(player);
		final String creatureCountToKill = getCountCreatureToKillFromPlayer(player);
		int killed = Integer.parseInt(creatureCountToKill) - killsCounter.remainingKills(player, creatureToKill);
		return "Wciąż muszę zabić " + Grammar.quantityplnoun(killed, creatureToKill) + ".";
	}

	private List<String> howManyWereKilledFormatted(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		final String creatureToKill = getCreatureToKillFromPlayer(player);
		final String creatureCountToKill = getCountCreatureToKillFromPlayer(player);
		int killed = Integer.parseInt(creatureCountToKill) - killsCounter.remainingKills(player, creatureToKill);

		String count;
		if (killed == 1) {
			count = " sztukę";
		} else if (killed >= 2 && killed <= 4) {
			count = " sztuki";
		} else {
			count = " sztuk";
		}

		List<String> entries = new ArrayList<>();
		entries.add("Upolowałem: <tally>" + killed + count + "</tally>");
		return entries;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Polowanie",
				"Janisław proponuje wszystkim młodym wojom upolowanie stworzeń w zamian za nagrodę.",
				true);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
		return getHistory(player, false);
	}

	@Override
	public List<String> getFormattedHistory(final Player player) {
		return getHistory(player, true);
	}

	private List<String> getHistory(final Player player, boolean formatted) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(Grammar.genderVerb(player.getGender(), "Spotkałem") + " Janisława w domku Zakopanego.");
		final String questStateFull = player.getQuest(QUEST_SLOT);
		final String[] parts = questStateFull.split(";");
		final String questState = parts[0];

		if ("rejected".equals(questStateFull)) {
			res.add("Nie chcę polować na zwierzyny i inne stworzenia.");
			return res;
		}

		res.add("Chcę spróbować zapolować na stworzenia.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			final boolean questDone = new KilledForQuestCondition(QUEST_SLOT, 0).fire(player, null, null);
			final String creatureToKill = getCreatureToKillFromPlayer(player);
			final String creatureCountToKill = getCountCreatureToKillFromPlayer(player);
			if (!questDone) {
				res.add(Grammar.genderVerb(player.getGender(), "Zostałem") + " " + Grammar.genderVerb(player.getGender(), "poproszony") + " o upolowanie " + creatureToKill + " w ilości " + creatureCountToKill
						+ ". Jeszcze mi się nie udało.");
			} else {
				res.add(Grammar.genderVerb(player.getGender(), "Upolowałem") + " wszystkie " + creatureToKill + ".");
			}

			if (formatted) {
				res.addAll(howManyWereKilledFormatted(player, questState));
			} else {
				res.add(howManyWereKilled(player, questState));
			}
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add(Grammar.genderVerb(player.getGender(), "Zabiłem") + " ostatniego potwora o którego prosił mnie Janisław. Może znowu będzie miał dla mnie wyzwanie.");
		}
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(Grammar.genderVerb(player.getGender(), "Pomogłem") + " Janisławowi "
					+ Grammar.quantityplnounCreature(repetitions, "raz") + " do tej pory.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "Polowanie";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new QuestCompletedCondition(QUEST_SLOT).fire(player, null, null);
	}
}
