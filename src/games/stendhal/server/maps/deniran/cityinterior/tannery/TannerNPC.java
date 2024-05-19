/***************************************************************************
 *                 Copyright © 2020-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.tannery;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.DaylightPhase;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.DaylightCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.util.TimeUtil;

public class TannerNPC implements ZoneConfigurator {
	private static TannerNPC instance;
	private static SpeakerNPC tanner;

	private final static String FEATURE_SLOT = "pouch";
	private final static String QUEST_SLOT = "money_" + FEATURE_SLOT;
	private final static String QUEST_NAME = "Sakwa na Pieniądze";

	// players must have looted at least 100,000 money to get the money pouch
	private static final int requiredMoneyLoot = 100000;
	private static final int serviceFee = 50000;
	private static final int TAN_TIME = TimeUtil.MINUTES_IN_DAY;
	// required items to make pouch
	private static final Map<String, Integer> requiredItems = new LinkedHashMap<String, Integer>() {{
		put("igła do skór", 1);
		put("skórzana nić", 2);
		put("futro", 1);
	}};

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		prepareNPC(zone);
		prepareDialogue();
		prepareTravelLog();
	}

	private void prepareNPC(final StendhalRPZone zone) {
		tanner = new SpeakerNPC("Garbarz Rawhide") {
			/**
			 * Force NPC to face north after speaking with players.
			 */
			@Override
			public void setAttending(final RPEntity rpentity) {
				super.setAttending(rpentity);
				if (rpentity == null) {
					setDirection(Direction.UP);
				}
			}
		};

		tanner.addGoodbye();

		tanner.setGender("M");
		tanner.setPosition(10, 8);
		tanner.setDirection(Direction.UP);
		tanner.setEntityClass("skinnernpc");

		zone.add(tanner);
	}

	/**
	 * Creates a quest to acquire the money pouch.
	 *
	 * Reward:
	 * - new slot to carry money in
	 * - 100 karma
	 *
	 * Notes:
	 * - Players can only talk to Skinner during daytime.
	 */
	private void prepareDialogue() {
		// conditions to check if it is night or day time
		final ChatCondition nightCondition = new DaylightCondition(DaylightPhase.NIGHT);
		final ChatCondition dayCondition = new NotCondition(nightCondition);
		// required items to begin tanning
		final ChatCondition hasItemsCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				for (final String itemName: requiredItems.keySet()) {
					if (!player.isEquipped(itemName, requiredItems.get(itemName))) {
						return false;
					}
				}

				if (!player.isEquipped("money", serviceFee)) {
					return false;
				}

				return true;
			}
		};
		// player can start quest if they have looted 1,000,000 money & they have not already started or finished the quest
		final ChatCondition canStartQuestCondition = new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new QuestNotCompletedCondition(QUEST_SLOT),
				new PlayerLootedNumberOfItemsCondition(requiredMoneyLoot, "money"));
		// condition to check if tanner is making the money pouch
		final ChatCondition isTanningCondition = new AndCondition(
				new QuestActiveCondition(QUEST_SLOT),
				new QuestNotInStateCondition(QUEST_SLOT, "start"));
		// action to give items & start
		final ChatAction startAction = new ChatAction() {
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				for (final String itemName: requiredItems.keySet()) {
					player.drop(itemName, requiredItems.get(itemName));
				}

				player.drop("money", serviceFee);
			}
		};


		// cannot talk to tanner at night
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				nightCondition,
				ConversationStates.IDLE,
				"Jest późno. Muszę iść do łóżka. Proszę wróć rano.",
				null);

		// player has not met requirement
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						new QuestNotActiveCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(new PlayerLootedNumberOfItemsCondition(requiredMoneyLoot, "money"))),
				ConversationStates.IDLE,
				"Witamy w garbarni Deniran.",
				null);

		// player has met requirement & can get money pouch
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						canStartQuestCondition),
				ConversationStates.QUESTION_1,
				"Widzę, że masz doświadczenie w zbieraniu pieniędzy. Mogę zrobić dla ciebie sakiewkę, w której będziesz mógł nosić pieniądze."
				+ " Ale będę potrzebować kilku przedmiotów. Czy jesteś zainteresowany?",
				null);

		// player wants money pouch
		tanner.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				sayRequiredItems("Dobra. Będę potrzebował [items]. Ponadto moja opłata wynosi " + Integer.toString(serviceFee) + " money."
				+ " Proszę, wróć, kiedy to będziesz miał.", false),
				new SetQuestAction(QUEST_SLOT, "start"));

		// player does not want money pouch
		tanner.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.IDLE,
				"Oh? Myślę, że byłoby to zachęcane.",
				null);

		// player returns without items
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(hasItemsCondition)),
				ConversationStates.ATTENDING,
				sayRequiredItems("Przynieś mi [items], a wykonam sakiewkę do noszenia twych pieniędzy.", true),
				null);

		// player returns with items
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						new QuestInStateCondition(QUEST_SLOT, "start"),
						hasItemsCondition),
				ConversationStates.QUESTION_1,
				"Ach, znalazłeś przedmioty do wykonania sakiewki. Chcesz, żebym rozpoczął pracę?",
				null);

		tanner.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						startAction,
						new SetQuestToTimeStampAction(QUEST_SLOT),
						new SayTimeRemainingAction(QUEST_SLOT, TAN_TIME, "Dobra, zacznę szyć sakiewkę na pieniądze. Proszę, wróć do mnie za ")));

		tanner.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.IDLE,
				"Naprawdę? W porządku. Przyjdź do mnie ponownie, jeśli zmienisz zdanie.",
				null);

		// player returns before money pouch is finished
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						isTanningCondition,
						new NotCondition(new TimePassedCondition(QUEST_SLOT, TAN_TIME))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, TAN_TIME, "Przepraszam, twoja sakiewka nie jest jeszcze gotowa. Proszę, wróć do mnie za "));

		// money pouch is finished
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						isTanningCondition,
						new TimePassedCondition(QUEST_SLOT, TAN_TIME)),
				ConversationStates.IDLE,
				"Wróciłeś w samą porę. Twoja sakiewka na pieniądze jest gotowa. Wypróbuj to. Wiem, że ci się spodoba.",
				new MultipleActions(
						new EnableFeatureAction("pouch"),
						new IncreaseKarmaAction(100.0),
						new SetQuestAction(QUEST_SLOT, "done")));

		// player speaks to tanner after completing quest
		tanner.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						dayCondition,
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.IDLE,
				"Wiedziałem, że spodoba ci się sakiewka.",
				null);


		// keyword responses

		final Map<String, String> responses = new HashMap<String, String>() {{
			put("igła do skór", "Jestem pewien, że gdzieś tu miałem.");
			put("skórzana nić", "Skórzana nić może być wykonana przez pocięcie #skóry. Będziesz potrzebował #'obrotowy nożyk'.");
			put("futro", "Czasami możesz zdobyć futro ze zwierząt, które je upuszczają.");
			put("obrotowy nożyk", "Wygląda na to, że zgubiłem mój. Może mógłbyś pożyczyć od kogoś innego. Są nawet używane do krojenia pizzy"
					+ ", więc zapytaj w miejscach, w których robi się pizzę, jeśli nie możesz jej znaleźć nigdzie indziej.");
		}};

		for (final String res: responses.keySet()) {
			tanner.add(ConversationStates.ATTENDING,
					res,
					new QuestInStateCondition(QUEST_SLOT, "start"),
					ConversationStates.ATTENDING,
					responses.get(res),
					null);
		}
	}

	/**
	 * Creates a quest entry in the player's travel log.
	 */
	private void prepareTravelLog() {
		final IQuest quest = new AbstractQuest() {
			@Override
			public List<String> getHistory(final Player player) {
				final List<String> res = new LinkedList<String>();
				final String questState = player.getQuest(QUEST_SLOT);

				if (questState == null) {
					return res;
				}

				final String tannerName = tanner.getName();

				res.add(tannerName + " zrobi dla mnie sakiewkę, jeśli przyniosę mu jakieś materiały.");

				if (questState.equals("start")) {
					for (final String itemName: requiredItems.keySet()) {
						final int quantity = requiredItems.get(itemName);
						if (player.isEquipped(itemName, quantity)) {
							res.add(player.getGenderVerb("Zdobyłem") + " " + itemName + ".");
						} else {
							res.add("Nadal muszę znaleźć " + Integer.toString(quantity) + " " + itemName);
						}
					}
					if (player.isEquipped("money", serviceFee)) {
						res.add("Mam wystarczająco dużo pieniędzy na opłacenie opłaty serwisowej.");
					} else {
						res.add("Potrzebuję więcej pieniędzy na opłacenie opłaty serwisowej.");
					}
				}

				if (!questState.equals("start") && !questState.equals("done")) {
					if (new TimePassedCondition(QUEST_SLOT, TAN_TIME).fire(player, null, null)) {
						res.add(tannerName + " skończył robienie mojej sakiewki na pieniądze.");
					} else {
						try {
							final long timeRemains = Long.parseLong(questState) + (TAN_TIME * TimeUtil.MILLISECONDS_IN_MINUTE) - System.currentTimeMillis();
							final int secondsRemain = (int) (timeRemains / 1000L);

							res.add(tannerName + " robi moją sakiewkę z pieniędzmi. Skończy za "
									+ TimeUtil.approxTimeUntil(secondsRemain) + ".");
						} catch (NumberFormatException e) {
							res.add(tannerName + " robi moją sakiewkę z pieniędzmi.");
						}
					}
				}

				if (questState.equals("done")) {
					res.add("Teraz mogę nosić pieniądze w mojej sakiewce.");
				}

				return res;
			}

			@Override
			public String getSlotName() {
				return QUEST_SLOT;
			}

			@Override
			public void addToWorld() {
				fillQuestInfo(
						QUEST_NAME,
						tanner.getName() + " może wykonać sakiewkę dla pieniędzy.",
						isRepeatable(null));
			}

			@Override
			public String getName() {
				return QUEST_NAME.replace(" ", "");
			}

			@Override
			public String getRegion() {
				return Region.DENIRAN;
			}

			@Override
			public String getNPCName() {
				return tanner.getName();
			}
		};

		StendhalQuestSystem.get().loadQuest(quest);
	}

	public String sayRequiredItems(final String msg, final boolean includeFee, final boolean highlight) {
		final StringBuilder sb = new StringBuilder();

		final Map<String, Integer> tempList = new LinkedHashMap<String, Integer>(requiredItems);
		if (includeFee) {
			tempList.put("money", serviceFee);
		}

		int idx = 0;
		for (final String key: tempList.keySet()) {
			if (idx == tempList.size() - 1) {
				sb.append("oraz ");
			}

			// highlight keywords
			String itemName = key;
			if (!itemName.equals("money") && highlight) {
				itemName = "#'" + itemName + "'";
			}

			sb.append(Integer.toString(tempList.get(key)) + " " + itemName);

			if (idx < tempList.size() - 1) {
				sb.append(", ");
			}

			idx++;
		}

		return msg.replace("[items]", sb.toString());
	}

	public String sayRequiredItems(final String msg, final boolean includeFee) {
		return sayRequiredItems(msg, includeFee, true);
	}

	// some helper methods for tests
	public static TannerNPC getInstance() {
		if (instance == null) {
			instance = new TannerNPC();
		}

		return instance;
	}

	public SpeakerNPC getNPC() {
		return tanner;
	}

	public String getQuestSlot() {
		return QUEST_SLOT;
	}

	public String getFeatureSlot() {
		return FEATURE_SLOT;
	}

	public Integer getRequiredMoneyLoot() {
		return requiredMoneyLoot;
	}

	public Integer getServiceFee() {
		return serviceFee;
	}

	public Map<String, Integer> getRequiredItems() {
		return requiredItems;
	}

	public Integer getWaitTime() {
		return TAN_TIME;
	}
}
