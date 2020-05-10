/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.nalwor.forest;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.maps.nalwor.forest.TrainingArea.TrainerNPC;

public class Dojo implements ZoneConfigurator {

	/** quest/activity identifier */
	private static final String QUEST_SLOT = "dojo";

	/** time (in minutes) allowed for training session */
	private static final int BASE_TRAIN_TIME = 20;

	/** time player must wait to train again */
	private static final int COOLDOWN = 5;

	/** condition to check if training area is full */
	private ChatCondition dojoFullCondition;

	/** quest states */
	private static final String STATE_ACTIVE = "training";
	private static final String STATE_DONE = "done";

	/** zone info */
	private StendhalRPZone dojoZone;

	/** dojo area */
	private static TrainingArea dojoArea;

	/** NPC that manages dojo area */
	private static final String samuraiName = "Omura Sumitada";
	private TrainerNPC samurai;

	/** phrases used in conversations */
	private static final List<String> FEE_PHRASES = Arrays.asList("fee", "cost", "charge", "opłata", "opłatę", "koszt", "cena", "cenę");
	private static final List<String> TRAIN_PHRASES = Arrays.asList("train", "training", "enter", "start", "wejść", "wejście",
			"szkolić", "szkolenie", "przeszkolić", "trening", "trenować", "trenuj", "trenowanie", "potrenować");

	/** message when dojo is full */
	private static final String FULL_MESSAGE = "Dojo jest pełne. Proszę wróć później.";

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		dojoZone = zone;

		initNPC();

		dojoArea = new TrainingArea(QUEST_SLOT, zone, 5, 52, 35, 20, samurai, new Point(22, 74), new Point(22, 72), Direction.DOWN);
		dojoArea.setCapacity(16);

		// initialize condition to check if dojo is full
		dojoFullCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return dojoArea.isFull();
			}
		};

		initDialogue();
	}

	private void initNPC() {
		samurai = new TrainerNPC(samuraiName, FULL_MESSAGE, "Hej %s! Nie możesz sobie biegać po dojo za darmo.");
		samurai.setEntityClass("samurai1npc");
		samurai.setIdleDirection(Direction.DOWN);
		samurai.setPosition(24, 74);

		dojoZone.add(samurai);
	}

	/**
	 * Initializes conversation & actions for dojo training.
	 */
	private void initDialogue() {
		samurai.addGreeting("Witaj w dojo skrytobójców.");
		samurai.addGoodbye();
		samurai.addOffer("Mogę zaoferować sesję #szkoleniową za odpowiednią #opłatą.");
		samurai.addQuest("Nie potrzebuję żadnej pomocy, ale mogę pozwolić ci #przeszkolić się za #opłatę, jeśli zostałeś zatwierdzony przez kwaterę główną zabójców.");
		samurai.addHelp("To jest dojo skrytobójców. Mogę ci pozwolić #przeszkolić się tutaj za #opłatę, jeśli masz dobre relację z kwaterą główną.");
		samurai.addJob("Zarządzam tym dojo. Jeśli chcesz #potrenować, to się mnie o to spytaj.");

		samurai.add(ConversationStates.ATTENDING,
				FEE_PHRASES,
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						samurai.say("Opłata za #trening dla twoich umiejętności wynosi " + dojoArea.calculateFee(player.getAtk()) + " money.");
					}
				});

		final ChatCondition meetsLevelCapCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return dojoArea.meetsLevelCap(player, player.getAtk());
			}
		};

		final ChatCondition canAffordFeeCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return player.isEquipped("money", dojoArea.calculateFee(player.getAtk()));
			}
		};

		final ChatAction startTrainingAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final int trainTime = calculateTrainTime(player);
				samurai.say("Możesz trenować do " + trainTime + " minut. Wykorzystaj więc dobrze swój czas.");
				player.drop("money", dojoArea.calculateFee(player.getAtk()));
				samurai.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));
				dojoArea.startSession(player, trainTime * MathHelper.SECONDS_IN_ONE_MINUTE);
			}
		};


		// player has never trained before
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie"),
						new NotCondition(dojoFullCondition)),
				ConversationStates.QUESTION_1,
				null,
				new MultipleActions(
						new NPCEmoteAction(samuraiName + " sprawdza twoją licencję na zabijanie.", false),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								samurai.say("Hmmm, nie widziałem cię tutaj wcześniej, ale masz odpowiednie kwalifikacje. Czy chcesz, abym"
										+ " otworzył dojo? Opłata wynosi " + dojoArea.calculateFee(player.getAtk()) + " money.");
							}
						}));

		// player returns after cooldown period is up
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DONE),
						new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN),
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie")),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						samurai.say("Na twój poziom umiejętności, koszt wynosi " + dojoArea.calculateFee(player.getAtk()) + " money za trening w dojo. Czy jesteś pewny, by wejść?");
					}
				});

		// player returns before cooldown period is up
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN)),
						new NotCondition(meetsLevelCapCondition)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, COOLDOWN, "Nie możesz jeszcze trenować. Proszę wróć za"));

		// player's ATK level is too high
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				meetsLevelCapCondition,
				ConversationStates.ATTENDING,
				"Na twój aktualny poziom doświadczenia, twoja siła ataku jest zbyt duża, by tutaj trenować w tym momencie.",
				null);

		// player does not have an assassins id
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(meetsLevelCapCondition),
						new NotCondition(new PlayerHasItemWithHimCondition("licencja na zabijanie"))),
				ConversationStates.ATTENDING,
				"Nie możesz tu trenować bez pozwolenia z kwatery głównej zabójców.",
				null);

		// player training state is active
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				"Twoja obecna sesja treningowa nie została zakończona.",
				null);

		// player meets requirements but training area is full
		samurai.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie"),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE)),
						dojoFullCondition),
				ConversationStates.ATTENDING,
				FULL_MESSAGE,
				null);

		/* player has enough money to begin training
		 *
		 * XXX: If admin alters player's quest slot, timer/notifier is not removed. Which
		 *      could potentially lead to strange behavior. But this should likely never
		 *      happen on live server. In an attempt to prevent such issues, the old
		 *      timer/notifier will be removed if the player begins a new training session.
		 *      Else the timer will simply be removed once it has run its lifespan.
		 */
		samurai.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				canAffordFeeCondition,
				ConversationStates.IDLE,
				null,
				startTrainingAction);

		// player does not have enough money to begin training
		samurai.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(canAffordFeeCondition),
				ConversationStates.ATTENDING,
				"Nie masz nawet dość dużo money na #opłatę.",
				null);

		// player does not want to train
		samurai.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Powodzenia.",
				null);
	}

	/**
	 * Calculates the amount of time player can spend in dojo.
	 */
	private int calculateTrainTime(final Player player) {
		int trainTime = BASE_TRAIN_TIME;

		if (player.getLevel() >= 60) {
			trainTime = trainTime + 4;
		}

		return trainTime;
	}
}