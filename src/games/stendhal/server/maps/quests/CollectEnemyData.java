/***************************************************************************
 *                     Copyright © 2021 - Arianne                          *
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.constants.SkinColor;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.OwnedItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;
import marauroa.common.Pair;

/**
 * QUEST: Collect Enemy Data (collect_enemy_data)
 *
 * PARTICIPANTS:
 * <ul>
 *   <li>Rengard, a wandering adventurer.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 *   <li>Find Rengard wandering around Faimouni.</li>
 *   <li>He will ask for information on 3 different creatures.</li>
 *   <li>Kill each creature & bring him the requested information.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 *   <li>Can buy bestiary from Rengard.</li>
 *   <li>karma</li>
 *   <ul>
 *     <li>35.0 for starting quest.</li>
 *     <li>200.0 for completing quest.</li>
 *   </ul>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 *   <li>Not repeatable.</li>
 * </ul>
 */
public class CollectEnemyData extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(CollectEnemyData.class);

	private static final String QUEST_NAME = "Zbierz Informacje Wroga";
	private static final String QUEST_SLOT = "collect_enemy_data";

	private SpeakerNPC npc;

	private TeleporterBehaviour teleporterBehaviour;

	public static final String[] zonesWhitelist = {
			"0_semos_mountain_n2_w", "0_ados_mountain_n2_w2", "0_deniran_forest_n2_e",
			"-7_deniran_atlantis_mtn_n_e2", "0_orril_mountain_n_w2"
	};

	private static final int bestiaryPrice = 500000;

	private static final Map<String, Pair<String, String>> questionOptions = new HashMap<String, Pair<String, String>>() {{
		put("poziom", new Pair<String, String>("Jaki poziom on wynosi", null));
		put("zdrowia", new Pair<String, String>("Ile zdrowia posiada", null));
		put("obr", new Pair<String, String>("Jaki jest poziom obrony", null));
		put("atk", new Pair<String, String>("Jaki jest poziom ataku", null));
	}};

	// FIXME: QuestActiveCondition doesn't work for this quest because of the overridden isCompleted method
	private final ChatCondition questActiveCondition = new ChatCondition() {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			if (player.getQuest(QUEST_SLOT) != null) {
				return !isCompleted(player);
			}

			return false;
		}
	};

	// FIXME: QuestCompletedCondition doesn't work for this quest because of the overridden isCompleted method
	private final ChatCondition questCompletedCondition = new ChatCondition() {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			return isCompleted(player);
		}
	};

	private void initNPC() {
		npc = new SpeakerNPC("Rengard");
		npc.setOutfit("body=0,head=0,eyes=16,hair=9,dress=76,mask=5,hat=22");
		npc.setOutfitColor("skin", SkinColor.DARK);
		npc.setOutfitColor("hat", 0xff0000);
		npc.setDescription("Oto Rengard. Doświadczony poszukiwacz przygód z uśmiechem na twarzy oraz błyskiem w oku.");

		npc.addGreeting("Witaj kolego poszukiwaczu przygód.");
		npc.addGoodbye("Obyś miał szczęście w swoich przyszłych przygodach.");
		npc.addJob("Praca? Hah! Jestem wolnym duchem. Podróżuję po świecie, starając się poszerzyć swoją wiedzę i doświadczenie.");

		final List<String> helpTriggers = new ArrayList<>();
		helpTriggers.addAll(ConversationPhrases.HELP_MESSAGES);
		helpTriggers.addAll(ConversationPhrases.OFFER_MESSAGES);

		npc.add(ConversationStates.ATTENDING,
				helpTriggers,
				//new NotCondition(new QuestCompletedCondition(QUEST_SLOT)),
				new NotCondition(new QuestStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Jeśli chcesz poszerzyć swoją wiedzę tak jak ja, no to będzię miał jakieś #zadanie, gdzie mógłbym skorzystać.",
				null);

		npc.add(ConversationStates.ATTENDING,
				helpTriggers,
				questActiveCondition,
				ConversationStates.ATTENDING,
				"Pomóż mi w zbieraniu informacji o wrogach, a ja sprzedam ci coś bardzo przydatnego.",
				null);


		teleporterBehaviour = new TeleporterBehaviour(npc, Arrays.asList(zonesWhitelist), "", "♫♫♫") {
			@Override
			protected void doRegularBehaviour() {
				super.doRegularBehaviour();

				npc.addEvent(new SoundEvent("npc/whistling-01", SoundLayer.CREATURE_NOISE));
			}
		};

		teleporterBehaviour.setTarryDuration(MathHelper.SECONDS_IN_ONE_MINUTE * 15); // spends 15 minutes on a map
		teleporterBehaviour.setExitsConversation(false);
		teleporterBehaviour.setTeleportWarning("Muszę wkrótcę odejść.");

		// initialize Rengard with a zone so JUnit test does not fail
		SingletonRepository.getRPWorld().getZone(zonesWhitelist[0]).add(npc);
	}

	private void initQuest() {

		final ChatCondition hasKilledCreatureCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				final String[] state = player.getQuest(QUEST_SLOT).split(";");
				if (state == null) {
					return false;
				}

				final int currentStep = getCurrentStep(player);
				final String creature = getEnemyForStep(player, currentStep);
				final Integer recordedKills = getRecordedKillsForStep(player, currentStep);

				if (creature == null || recordedKills == null) {
					onError(null);
					return false;
				}

				return (player.getSoloKill(creature) + player.getSharedKill(creature)) > recordedKills;
			}
		};

		final ChatCondition answeredCorrectlyCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity raiser) {
				final int currentStep = getCurrentStep(player);
				final String fromQuestSlot = getEnemyForStep(player, currentStep);
				final Creature creature = SingletonRepository.getEntityManager().getCreature(fromQuestSlot);

				if (creature == null) {
					logger.error("Invalid creature name in quest slot: " + fromQuestSlot);
					return false;
				}

				final String answer = sentence.getTrimmedText();
				String correctAnswer = getAnswerForStep(player, creature, currentStep);

				// should not happen
				if (correctAnswer == null) {
					onError(null);
					return false;
				}

				return answer.equals(correctAnswer);
			}
		};

		final ChatCondition isFinalStepCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return getCurrentStep(player) > 1;
			}
		};

		final ChatAction setQuestAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final String selected = selectCreature(player);
				final int killCount = player.getSoloKill(selected) + player.getSharedKill(selected);

				if (player.getQuest(QUEST_SLOT) == null) {
					setQuestSlot(player, 0, selected, killCount);
					player.addKarma(35.0);

					npc.say("Świetnie! Zebrałem wiele informacji na temat stworzeń, które spotkałem. Ale wciąż brakuje mi trzech. Po pierwsze potrzebuję trochę informacji na temat "
							+ Grammar.singular(selected) + ".");
				} else {
					setQuestSlot(player, getCurrentStep(player), selected, killCount);

					npc.say("Dziękuję Ci! Spiszę to. Teraz potrzebuję informacji na temat " + Grammar.singular(selected) + ".");
				}
			}
		};

		final ChatAction completeStepAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final int currentStep = getCurrentStep(player);
				String[] stepState = player.getQuest(QUEST_SLOT, currentStep).split(",");
				stepState[1] = "done";
				player.setQuest(QUEST_SLOT, currentStep, String.join(",", stepState));
			}
		};

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Czy chciałbyś mi pomóc w gromadzeniu informacji na temat stworzeń znalezionych w świecie Faimouni oraz Prasłowiańskim?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						//new NotCondition(new QuestCompletedCondition(QUEST_SLOT)),
						new NotCondition(questCompletedCondition)),
				ConversationStates.ATTENDING,
				"Zgodziłeś się już pomóc mi w zbieraniu informacji o stworzeniach.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				//new QuestCompletedCondition(QUEST_SLOT),
				questCompletedCondition,
				ConversationStates.ATTENDING,
				"Dziękuje za pomoc w gromadzeniu informacji o stworzeniach.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Okej, jakoś sobie poradzę.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				setQuestAction);

		// player has to returned to give info
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				//new QuestActiveCondition(QUEST_SLOT), // FIXME: doesn't work for this quest because of the overridden isCompleted method
				questActiveCondition,
				ConversationStates.QUESTION_1,
				"Czy przyniosłeś informacje o stworzeniu, o które prosiłem?",
				null);

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"W porządku. W czym jeszcze mogę ci pomóc?",
				null);

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(hasKilledCreatureCondition),
				ConversationStates.ATTENDING,
				"Nie okłamuj mnie. Nawet nie zabiłeś jednego odkąd rozmawialiśmy.",
				null);

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				hasKilledCreatureCondition,
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final String question = getQuestionForStep(player, getCurrentStep(player));
						if (question == null) {
							onError("Nie można pobrać pytania dla gracza: " + player.getName());
							return;
						}

						npc.say(question);
					}
				});

		npc.add(ConversationStates.QUESTION_2,
				"",
				new NotCondition(answeredCorrectlyCondition),
				ConversationStates.IDLE,
				"Hmmm, to nie wydaje się być dokładne. Może mógłbyś dwukrotnie sprawdzić?",
				null);

		npc.add(ConversationStates.QUESTION_2,
				"",
				new AndCondition(
						answeredCorrectlyCondition,
						new NotCondition(isFinalStepCondition)),
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						completeStepAction,
						setQuestAction));

		npc.add(ConversationStates.QUESTION_2,
				"",
				new AndCondition(
						answeredCorrectlyCondition,
						isFinalStepCondition),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						completeStepAction,
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								npc.say("Dziękuję bardzo za pomoc. Teraz mam wszystkie informacje potrzebne do ukończenia mojego #bestiariusza."
										+ " Jeśli chcesz swój własny, mogę ci go sprzedać.");
								player.addKarma(200.0);
							}}));
	}

	private void initShop() {
		final Map<String, Integer> prices = new LinkedHashMap<String, Integer>() {{
			put("bestiariusz", bestiaryPrice);
		}};

		final SellerBehaviour behaviour = new SellerBehaviour(prices) {
			@Override
			public ChatCondition getTransactionCondition() {
				//return new QuestCompletedCondition(QUEST_SLOT);
				return questCompletedCondition;
			}

			@Override
			public ChatAction getRejectedTransactionAction() {
				return new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final String response;
						if (questActiveCondition.fire(player, null, null)) {
							response = "Nadal potrzebuję, abyś pomógł mi zebrać informacje na temat " + getEnemyForStep(player, getCurrentStep(player)) + ".";
						} else {
							response = "Najpierw potrzebuję twojej pomocy przy #zadaniu.";
						}

						npc.say(response);
					}
				};
			}

			@Override
			public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
				if (super.transactAgreedDeal(res, seller, player)) {
					seller.say("Zapisałem w nim twoje imię, na wypadek gdybyś zgubił.");

					return true;
				}

				return false;
			}

			@Override
			public Item getAskedItem(final String askedItem, final Player player) {
				final Item item = super.getAskedItem(askedItem, player);

				if (item != null && player != null) {
					// set owner to prevent others from using it
					((OwnedItem) item).setOwner(player.getName());
					return item;
				}

				if (player == null) {
					logger.error("Player is null, cannot set owner in bestiary");
				}
				if (item == null) {
					logger.error("Could not create bestiary item");
				}

				return null; // don't give a bestiary without owner
			}
		};
		new SellerAdder().addSeller(npc, behaviour, false);


		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				//new QuestCompletedCondition(QUEST_SLOT),
				questCompletedCondition,
				ConversationStates.ATTENDING,
				"Mogę sprzedać ci #bestiariusz.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.HELP_MESSAGES,
				//new QuestCompletedCondition(QUEST_SLOT),
				questCompletedCondition,
				ConversationStates.ATTENDING,
				"Jeśli posiadasz już #bestiariusz, możesz być w stanie znaleźć medium, które da ci więcej wglądu w stwory, które spotkałeś.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("bestiary", "bestiariusz"),
				//new QuestCompletedCondition(QUEST_SLOT),
				questCompletedCondition,
				ConversationStates.ATTENDING,
				"Bestiariusz pozwala śledzić wrogów, których pokonałeś.",
				null);
	}


	/**
	 * Fills in information in the quest slot for a step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param slotIndex
	 * 		The quest step in question.
	 * @param enemyName
	 * 		The enemy player is tasked to kill.
	 * @param killCount
	 * 		Recorded number of kills retrieved from player.
	 * @param questionKey
	 * 		Key to identify which question should be asked.
	 */
	private void setQuestSlot(final Player player, final int slotIndex, final String enemyName, final int killCount) {
		String slotString = enemyName + "," + killCount + "," + selectQuestionKey();

		if (slotIndex == 0) {
			slotString += ";null;null";
		}

		player.setQuest(QUEST_SLOT, slotIndex, slotString);
	}

	/**
	 * Randomly chooses the identifier for the question that should be asked.
	 *
	 * @return
	 * 		Key to identify which question should be be asked.
	 */
	private String selectQuestionKey() {
		return questionOptions.keySet().toArray(new String[] {})[Rand.randUniform(0, questionOptions.size() - 1)];
	}

	/**
	 * Randomly chooses an enemy based on player level.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @return
	 * 		Creature name.
	 */
	private String selectCreature(final Player player) {
		int threshold = 10;
		final int playerLevel = player.getLevel();

		final Collection<Creature> allCreatures = SingletonRepository.getEntityManager().getCreatures();
		// should not happen
		if (allCreatures.size() < 3) {
			logger.error("Not enough registered creatures for quest");
			return null;
		}

		final List<String> previous = new ArrayList<>();
		for (final String value: getStepsStates(player)) {
			if (value.contains(",")) {
				final String[] tmp = value.split(",");
				if (tmp[1].equals("done")) {
					previous.add(tmp[0]);
				}
			}
		}

		final List<String> eligible = new ArrayList<>();
		boolean satisfied = false;

		while (!satisfied) {
			for (final Creature creature: allCreatures) {
				// don't include rare & abnormal creatures
				if (creature.isAbnormal()) {
					continue;
				}

				final String creatureName = creature.getName();
				final int creatureLevel = creature.getLevel();

				if (!previous.contains(creatureName) && !eligible.contains(creatureName)
						&& creatureLevel >= playerLevel - threshold
						&& creatureLevel <= playerLevel + threshold) {
					eligible.add(creatureName);
				}
			}

			satisfied = eligible.size() > 0; // need at least 1 creature

			if (!satisfied) {
				// increase level threshold so more creatures can be added
				threshold += 5;
			}
		}

		// pick randomly from eligible creatures
		return eligible.get(Rand.randUniform(0, eligible.size() - 1));
	}

	/**
	 * Retrieves information for each step from quest slot.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @return
	 * 		List containing quest steps information.
	 */
	private List<String> getStepsStates(final Player player) {
		final String state = player.getQuest(QUEST_SLOT);
		if (state == null || !state.contains(";")) {
			return Arrays.asList("null", "null", "null");
		}

		final List<String> states = new ArrayList<>();
		for (final String st: state.split(";")) {
			states.add(st);
		}

		// in case there were less than 3 slot indexes
		while (states.size() < 3) {
			states.add("null");
		}

		return states;
	}

	/**
	 * Retrieves currently active step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @return
	 * 		The step index.
	 */
	public int getCurrentStep(final Player player) {
		int step = 0;

		// max 3 steps
		while (step < 3) {
			if (!isStepDone(player, step)) {
				break;
			}

			step++;
		}

		return step;
	}

	/**
	 * Checks if player has completed a step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		<code>true</code> if the player has completed the step.
	 */
	public boolean isStepDone(final Player player, final int step) {
		if (player.getQuest(QUEST_SLOT).equals("done")) {
			return true;
		}

		final List<String> states = getStepsStates(player);
		if (states.size() < step + 1) {
			return false;
		}

		final String[] currentState = states.get(step).split(",");
		if (currentState.length > 1) {
			return currentState[1].equals("done");
		}

		return false;
	}

	/**
	 * Retrieves enemy name stored in quest slot that player must kill for step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		Name of enemy player is tasked to kill for step.
	 */
	public String getEnemyForStep(final Player player, final int step) {
		final String enemy = getStepsStates(player).get(step).split(",")[0];
		if (enemy == null) {
			logger.error("Could not retrieve enemy/creature from quest slot for step " + step);
		} else if(enemy.equals("null") || enemy.equals("")) {
			return null;
		}

		return enemy;
	}

	/**
	 * Retrieves original kill count of enemy before quest was started.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		Recorded kill count stored in quest slot.
	 */
	public Integer getRecordedKillsForStep(final Player player, final int step) {
		Integer kills = null;
		final String[] indexState = getStepsStates(player).get(step).split(",");
		if (indexState.length > 1) {
			try {
				kills = Integer.parseInt(indexState[1]);
			} catch (final NumberFormatException e) {
				logger.error("Error parsing kill count from quest slot for step " + step);
			}
		}

		if (kills == null) {
			logger.error("Could not retrieve kill count from quest slot for step " + step);
		}

		return kills;
	}

	/**
	 * Retrieves the key to identify which question should be asked.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		Key stored in quest slot that identifies which question should be asked.
	 */
	private String getQuestionKeyForStep(final Player player, final int step) {
		final String[] indexState = getStepsStates(player).get(step).split(",");
		if (indexState.length < 3) {
			logger.warn("Question key not found in quest slog");
			return null;
		}

		final String questionKey = indexState[2];
		if (questionKey == null) {
			logger.error("Could not retrieve question key from quest slot for step " + step);
		}

		return indexState[2];
	}

	/**
	 * Retrieves the question that will be asked for the step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		Question to be asked to player.
	 */
	public String getQuestionForStep(final Player player, final int step) {
		String questionKey = getQuestionKeyForStep(player, getCurrentStep(player));
		if (questionKey == null || !questionOptions.containsKey(questionKey)) {
			// default to "poziom" in case an appropriate question cannot be found
			logger.warn("Using default \"level\" to retrieve question for step " + step);
			questionKey = "poziom";
		}

		final Pair<String, String> questionPair = questionOptions.get(questionKey);
		final String prefix = questionPair.first();
		final String suffix = questionPair.second();
		final String currentCreature = getEnemyForStep(player, step);

		if (prefix == null || currentCreature == null) {
			return null;
		}

		String questionString = prefix.trim() + " " + currentCreature;
		if (suffix != null) {
			questionString += " " + suffix.trim();
		}

		return questionString + "?";
	}

	/**
	 * Retrieves the correct answer for the step.
	 *
	 * @param player
	 * 		Player doing the quest.
	 * @param creature
	 * 		Creature which player was tasked to kill.
	 * @param step
	 * 		The current quest step.
	 * @return
	 * 		The answer to the question asked.
	 */
	public String getAnswerForStep(final Player player, final Creature creature, final int step) {
		String answer = null;

		// FIXME: is there a way to call these methods automatically?
		final String questionKey = getQuestionKeyForStep(player, step);

		if (questionKey.equals("zdrowia")) {
			answer = Integer.toString(creature.getBaseHP());
		} else if (questionKey.equals("poziom")) {
			answer = Integer.toString(creature.getLevel());
		} else if (questionKey.equals("obr")) {
			answer = Integer.toString(creature.getDef());
		} else if (questionKey.equals("atk")) {
			answer = Integer.toString(creature.getAtk());
		} else if (questionKey.equals("ratk")) {
			answer = Integer.toString(creature.getRatk());
		}

		if (answer == null) {
			// default to "poziom" in case an appropriate answer cannot be found
			logger.warn("Using default \"level\" to retrieve answer for step " + step);
			answer = Integer.toString(creature.getLevel());
		}

		return answer;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		res.add("Napotkałem " + npc.getName() + ", wędrownego poszukiwacza przygód.");

		for (int step = 0; step < 3; step++) {
			final String enemy = getEnemyForStep(player, step);
			if (enemy != null) {
				if (!isStepDone(player, step)) {
					res.add("Poprosił mnie o informacje " + enemy + ".");
				} else {
					String key = getQuestionKeyForStep(player, step);
					if (!key.equals("poziom")) {
						key = key.toUpperCase();
					}
					res.add("Zgłosiłem informacje na temat " + key + " dla stworzenia " + enemy + ".");
				}
			}
		}

		if (isCompleted(player)) {
			res.add("Zebrałem wszystkie informacje, które " + npc.getName() + " poprosił mnie o niektórych stworzeniach znalezionych w Faiumoni oraz w świecie Prasłowan. Teraz mogę kupić od niego bestiariusz.");
		}

		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		initNPC();
		initQuest();
		initShop();

		fillQuestInfo(
				QUEST_NAME,
				npc.getName() + " chce pomocy w gromadzeniu informacji o stworzeniach znalezionych w okolicach Faimouni oraz Prasłowan.",
				false);
	}
	@Override
	public boolean removeFromWorld() {
		final StendhalRPZone currentZone = npc.getZone();
		if (currentZone != null) {
			currentZone.remove(npc);
		}

		// remove the turn notifiers left from the TeleporterBehaviour
		SingletonRepository.getTurnNotifier().dontNotify(teleporterBehaviour);
		return true;
	}

	@Override
	public String getName() {
		return QUEST_NAME.replace(" ", "");
	}

	@Override
	public String getNPCName() {
		if (npc == null) {
			return null;
		}

		return npc.getName();
	}

	@Override
	public boolean isCompleted(final Player player) {
		if (player.hasQuest(QUEST_SLOT)) {
			return (isStepDone(player, 0) && isStepDone(player, 1) && isStepDone(player, 2)) || player.getQuest(QUEST_SLOT).equals("done");
		}

		return false;
	}

	/**
	 *
	 * @param msg
	 * 		Message to print in the console.
	 */
	private void onError(final String msg) {
		if (msg != null) {
			logger.error(msg);
		}

		npc.say("Dziwne. Tracę pamięć. Wygląda na to, że nie mogę pomóc ci ukończyć tego zadania.");
		npc.setCurrentState(ConversationStates.ATTENDING);
	}
}
