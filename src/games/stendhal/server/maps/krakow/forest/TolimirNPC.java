/***************************************************************************
 *                   (C) Copyright 2018 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.forest;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.LogoutListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.BreakableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.ConditionAndActionPortal;
import games.stendhal.server.entity.mapstuff.portal.Gate;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.AreaIsFullCondition;
import games.stendhal.server.entity.npc.condition.ComparisonOperator;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerStatLevelCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ShowItemListEvent;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.Area;
import games.stendhal.server.util.TimeUtil;

/**
 * TODO: create JUnit test
 * FIXME: should bows wear & break even if hit not successful?
 * FIXME: (client) no sound for training targets when hit
 */
public class TolimirNPC implements ZoneConfigurator,LoginListener,LogoutListener {

	/** logger instance */
	private static Logger logger = Logger.getLogger(TolimirNPC.class);

	/** quest/activity identifier */
	private static final String QUEST_SLOT = "strzelnica_polan";

	/** cost to use archery range */
	private static final int COST = 1000;

	/** capped range attack level */
	private static final int RATK_LIMIT = 50;

	/** time (in seconds) allowed for training session */
	private static final int TRAIN_TIME = 60 * MathHelper.SECONDS_IN_ONE_MINUTE;

	/** time player must wait to train again */
	private static final int COOLDOWN = 30;

	/** max number of players allowed in training area at a time */
	private static final int MAX_OCCUPANTS = 8;

	/** condition to check if training area is full */
	AreaIsFullCondition rangeFullCondition;

	/** zone info */
	private StendhalRPZone archeryZone;
	private String archeryZoneID;

	/** archery range area */
	private final Rectangle2D archeryArea = new Rectangle(88, 87, 74, 79);

	/** NPC that manages archery area */
	private static final String npcName = "Tolimir";
	private SpeakerNPC npc;

	private static final int bowPrice = 4500;

	/** phrases used in conversations */
	private static final List<String> TRAIN_PHRASES = Arrays.asList("train", "training", "trening", "trenuj", "trenowanie", "trenować");
	private static final List<String> FEE_PHRASES = Arrays.asList("fee", "cost", "charge", "opłata", "opłatę", "koszt", "cena", "cenę");

	/** quest states */
	private static final String STATE_ACTIVE = "training";
	private static final String STATE_DONE = "done";

	private static final String FULL_MESSAGE = "Strzelnica jest pełna. Wróć później.";

	/** message when player tries to enter without paying */
	private static final String NO_ACCESS_MESSAGE = "Hej %s! Nie możesz sobie biegać po strzelnicy królestwa Polan za darmo.";

	/** position of gate that manages access to training area */
	private static final Point GATE_POS = new Point(81, 88);

	/** misc objects for JUnit test */
	private static AbstractQuest quest;
	private static ShopSign blackboard;


	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		// set up the login/logout notifiers
		SingletonRepository.getLoginNotifier().addListener(this);
		SingletonRepository.getLogoutNotifier().addListener(this);

		archeryZone = zone;
		archeryZoneID = zone.getName();

		// initialize condition to check if training area is full
		rangeFullCondition = new AreaIsFullCondition(new Area(archeryZone, archeryArea), MAX_OCCUPANTS);

		initEntrance();
		initNPC();
		initShop();
		initRepairShop();
		initTraining();
		addToQuestSystem();
	}

	/**
	 * Initializes portal & gate entities that manage access to the training area.
	 */
	private void initEntrance() {
		// prevents players who haven't paid from entering if gate is open (must be added before gate)
		archeryZone.add(new ArcheryRangeConditionAndActionPortal());

		// gate to enter
		final Gate gate = new Gate("h", "palisade_gate", new QuestInStateCondition("strzelnica_polan", 0, STATE_ACTIVE)) {

			@Override
			protected boolean isAllowed(final RPEntity user) {
				// don't worry about players trying to leave
				if (user.getDirectionToward(this) != Direction.UP) {
					return true;
				}

				// check if player has paid
				if (!super.isAllowed(user)) {
					npc.say(NO_ACCESS_MESSAGE.replace("%s", user.getName()));
					return false;
				}

				// check if dojo is full
				if (isFull()) {
					npc.say(FULL_MESSAGE);
					return false;
				}

				return true;
			}

			@Override
			public boolean onUsed(final RPEntity user) {
				if (this.nextTo(user)) {
					if (isAllowed(user)) {
						setOpen(!isOpen());
						return true;
					}
				}
				return false;
			}
		};
		gate.setAutoCloseDelay(2);
		gate.setPosition(GATE_POS.x, GATE_POS.y);
		archeryZone.add(gate);
	}

	private void initNPC() {
		npc = new SpeakerNPC(npcName) {
			@Override
			protected void createDialog() {
				addGreeting("Witaj w strzelnicy królestwa Polan! W czym mogę #pomóc?");
				addGoodbye("Możesz wrócić kiedy będziesz miał trochę gotówki.");
				addJob("Zarządzam tą tutaj strzelnicą. Należy ona do królestwa Polan, więc nie wtykaj swojego nosa tam, gdzie nie trzeba.");
				addQuest("Jeśli chcesz komuś pomóc, to pójdź pomóż naszemu królowi!");
				addReply(Arrays.asList("łuk treningowy", "łuku treningowego"), "Łuki treningowe są słabe, ale łatwe w użyciu, więc możesz strzelać z nich znacznie szybciej niż"
						+ " przy zwykłym łuku. Ale z powodu słabej jakości nie wytrzymują długo.");
				addHelp("Znajdujesz się w strzelnicy królestwa. Mogę ci pozwolić #'trenować' swoje umiejętności"
						+ " dystansowe za drobną #'opłatą'. Zalecam używanie #'łuku treningowego'.");
				addReply(FEE_PHRASES, "Koszt #trenowania u mnie to " + Integer.toString(COST) + " money.");
			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				setDirection(Direction.RIGHT);
			}

			@Override
			public void say(final String text) {
				// don't turn toward player
				say(text, false);
			}
		};

		npc.setDescription("Oto Tolimir, który wydaje się być utalentowanym strzelcem.");
		npc.setPosition(77, 90);
		npc.setEntityClass("rangernpc");
		npc.setDirection(Direction.RIGHT);
		archeryZone.add(npc);
	}

	/**
	 * Adds bow & arrows for sale from NPC.
	 */
	private void initShop() {

		// override the default offer message
		npc.add(ConversationStates.ANY,
				ConversationPhrases.OFFER_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Sprawdź moje ceny łuków oraz strzał na tej czarnej tablicy"
						+ " znajdująca się obok mnie. Jeśli szukasz specjalnej okazji"
						+ " to jej tutaj nie znajdziesz!",
				null);

		// prices are higher than those of other shops
		final Map<String, Integer> shop = new LinkedHashMap<>();
		shop.put("strzała", 3);
		shop.put("włócznia", 110);
		shop.put("drewniany łuk", 500);
		shop.put("długi łuk", 1200);
		shop.put("łuk treningowy", bowPrice);

		new SellerAdder().addSeller(npc, new SellerBehaviour(shop));

		// a sign showing prices of items
		blackboard = new ShopSign("sellarcheryrange", "Sklep łuczniczy", "Sprzedawane są tu łuki i strzały:", true) {
			@Override
			public boolean onUsed(final RPEntity user) {
				List<Item> itemList = generateItemList(shop);
				ShowItemListEvent event = new ShowItemListEvent(this.title, this.caption, itemList);
				user.addEvent(event);
				user.notifyWorldAboutChanges();

				return true;
			}
		};
		blackboard.setEntityClass("blackboard");
		blackboard.setPosition(77, 88);
		archeryZone.add(blackboard);
	}
	
	/**
	 * If players bring their worn training swords they can get them repaired for half the
	 * price of buying a new one.
	 */
	private void initRepairShop() {
		final Sign repairSign = new Sign();
		repairSign.setEntityClass("notice");
		repairSign.setPosition(78, 88);
		repairSign.setText("Łuki treningowe #naprawiane za połowę ceny nowych.");
		archeryZone.add(repairSign);

		final List<String> repairPhrases = Arrays.asList("repair", "fix", "napraw", "naprawa", "naprawić", "naprawiam", "naprawiane");

		final ChatCondition needsRepairCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return getUsedBowsCount(player) > 0;
			}
		};

		final ChatCondition canAffordRepairsCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return player.isEquipped("money", getRepairPrice(getUsedBowsCount(player)));
			}
		};

		final ChatAction sayRepairPriceAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final int usedBows = getUsedBowsCount(player);
				final boolean multiple = usedBows > 1;
				final boolean multiple2 = usedBows > 4;

				final StringBuilder sb = new StringBuilder("Masz " + Integer.toString(usedBows));
				if (multiple) {
					sb.append("zużyte łuki treningowe");
				} else if (multiple2) {
					sb.append("zużytych łuków treningowych");
				} else {
					sb.append("zużyty łuk treningowy");
				}
				sb.append(". Mogę naprawić ");
				if (multiple) {
					sb.append("je wszystkie");
				} else {
					sb.append("to");
				}
				sb.append(" za " + Integer.toString(getRepairPrice(usedBows)) + " money. Chciałbyś, żebym to zrobił?");

				npc.say(sb.toString());
			}
		};

		final ChatAction repairAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final int usedBows = getUsedBowsCount(player);
				player.drop("money", getRepairPrice(usedBows));

				for (final Item bow: player.getAllEquipped("łuk treningowy")) {
					final BreakableItem breakable = (BreakableItem) bow;
					if (breakable.isUsed()) {
						breakable.repair();
					}
				}

				if (usedBows > 1) {
					npc.say("Gotowe! Twoje łuki treningowe wyglądają jak nowe.");
				} else {
					npc.say("Gotowe! Twój łuk treningowy wygląda jak nowy.");
				}

				npc.addEvent(new SoundEvent("coins-01", SoundLayer.CREATURE_NOISE));
			}
		};

		npc.add(ConversationStates.ATTENDING,
				repairPhrases,
				new NotCondition(needsRepairCondition),
				ConversationStates.ATTENDING,
				"Nie masz przy sobie żadnego #'łuku treningowego' do naprawienia.",
				null);

		npc.add(ConversationStates.ATTENDING,
				repairPhrases,
				needsRepairCondition,
				ConversationStates.QUESTION_2,
				null,
				sayRepairPriceAction);

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"W takim razie powodzenia. Pamiętaj, że gdy się całkowicie zepsują, nie będzie można ich naprawić.",
				null);

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(needsRepairCondition),
				ConversationStates.ATTENDING,
				"Zgubiłeś swój łuk?",
				null);

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						needsRepairCondition,
						new NotCondition(canAffordRepairsCondition)),
				ConversationStates.ATTENDING,
				"Nie masz wystarczająco pieniędzy. Wynoś się stąd!",
				null);

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						needsRepairCondition,
						canAffordRepairsCondition),
				ConversationStates.ATTENDING,
				null,
				repairAction);
	}

	private int getUsedBowsCount(final Player player) {
		int count = 0;
		for (final Item bow: player.getAllEquipped("łuk treningowy")) {
			if (((BreakableItem) bow).isUsed()) {
				count++;
			}
		}

		return count;
	}

	private int getRepairPrice(final int count) {
		return count * (bowPrice / 2);
	}

	/**
	 * Initializes conversation & actions for archery training.
	 */
	private void initTraining() {

		// player has never trained before
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new PlayerStatLevelCondition("ratk", ComparisonOperator.LESS_THAN, RATK_LIMIT)),
				ConversationStates.QUESTION_1,
				null,
				new SayTextAction("Czy chcesz abym otworzył dla ciebie strzelnicę?"
					+ " Będzie cię to kosztować " + Integer.toString(COST) + " money."));

		// player returns after cooldown period is up
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DONE),
						new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN),
						new PlayerStatLevelCondition("ratk", ComparisonOperator.LESS_THAN, RATK_LIMIT)),
				ConversationStates.QUESTION_1,
				"To będzie cię kosztować " + Integer.toString(COST) + " money za trening. Zgadzasz się na to?",
				null);

		// player returns before cooldown period is up
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN)),
						new PlayerStatLevelCondition("ratk", ComparisonOperator.LESS_THAN, RATK_LIMIT)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, COOLDOWN, "Nie możesz jeszcze trenować. Wróc za"));

		// player's RATK level is too high
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new PlayerStatLevelCondition("ratk", ComparisonOperator.GREATER_OR_EQUALS, RATK_LIMIT),
				ConversationStates.ATTENDING,
				"Jesteś już zbyt wyszkolony, by tu trenować. A teraz wynoś się ty leniwcu i walcz z potworami!",
				null);

		// player training state is active
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				"Wynoś się stąd! Zapłaciłeś już za sesję szkoleniową.",
				null);

		// player meets requirements but training area is full
		npc.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new PlayerStatLevelCondition("ratk", ComparisonOperator.LESS_THAN, RATK_LIMIT),
						rangeFullCondition),
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
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", COST),
				ConversationStates.IDLE,
				"Możesz trenować maksymalnie do " + Integer.toString(TRAIN_TIME / MathHelper.SECONDS_IN_ONE_MINUTE) + " minut. Więc dobrze wykorzystaj swój czas.",
				new MultipleActions(
						new DropItemAction("money", COST),
						new SetQuestAction(QUEST_SLOT, STATE_ACTIVE + ";" + Integer.toString(TRAIN_TIME)),
						new ArcheryRangeTimerAction()));

		// player does not have enough money to begin training
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("money", COST)),
				ConversationStates.ATTENDING,
				"Co to ma być? Nie masz nawet wystarczająco dużo pieniędzy na #'opłatę'. Odejdź stąd!",
				null);

		// player does not want to train
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"No to odejdź stąd i nie trać mojego cennego czasu!",
				null);

		/* FIXME: How to get updated remaining time?
		// player asks how much time is left in training session
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("time", "czas"),
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, TRAIN_TIME, "Twój trening zakończy się za około"));
		*/
	}

	/**
	 * Makes visible in inspect command.
	 */
	private void addToQuestSystem() {
		quest = new AbstractQuest() {

			@Override
			public List<String> getHistory(Player player) {
				return null;
			}

			@Override
			public String getSlotName() {
				return QUEST_SLOT;
			}

			@Override
			public void addToWorld() {
			}

			@Override
			public String getName() {
				return "TolimirNPC";
			}
		};

		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
	}

	/**
	 * Allows time remaining to be altered by changing quest slot.
	 */
	private Integer updateTimeRemaining(final Player player) {
		try {
			final int timeRemaining = Integer.parseInt(player.getQuest(QUEST_SLOT, 1)) - 1;
			player.setQuest(QUEST_SLOT, 1, Integer.toString(timeRemaining));
			return timeRemaining;
		} catch (NumberFormatException e) {
			// couldn't get time remaining from quest state
			SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Checks if entity is within bounds of an area.
	 *
	 * @param area
	 * 		Area dimensions to check.
	 * @return
	 * 		<code>true</code> if entity is within area.
	 */
	public boolean isPlayerInArea(final Player player, final String zoneid, final Rectangle2D area) {
		// TODO: Use standard collision check, which can handle entities larger than 1x1
		if (!player.get("zoneid").equals(zoneid)) {
			return false;
		}
		return area.contains(player.getInt("x"), player.getInt("y"));
	}

	/**
	 * Actions to take when player logs in.
	 */
	@Override
	public void onLoggedIn(final Player player) {
		// don't allow players to login within archery range area boundaries
		if (isPlayerInArea(player, archeryZoneID, archeryArea) || (player.getX() == GATE_POS.x && player.getY() == GATE_POS.y)) {
			player.teleport(archeryZoneID, 81, 89, null, null);
		}

		final String sessionState = player.getQuest(QUEST_SLOT, 0);
		if (sessionState != null && sessionState.equals(STATE_ACTIVE)) {
			final String sessionTimeString = player.getQuest(QUEST_SLOT, 1);
			if (sessionTimeString != null) {
				// re-initialize turn notifier if player still has active training session
				new ArcheryRangeTimerAction().fire(player, null, null);
			}
		}
	}

	/**
	 * Actions to take when player logs out.
	 */
	@Override
	public void onLoggedOut(Player player) {
		// disable timer/notifier
		SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
	}

	/**
	 * Checks if dojo is full.
	 *
	 * @return
	 * 		<code>true</code> if max number of occupants are within training area bounds.
	 */
	private boolean isFull() {
		return rangeFullCondition.fire(null, null, null);
	}

	/**
	 * Teleports player out of archery range training area.
	 */
	private void endTrainingSession(final Player player) {
		if (player.get("zoneid").equals(archeryZoneID)) {
			npc.say("Twój trening właśnie się skończył " + player.getName() + ".");
		}
		if (isPlayerInArea(player, archeryZoneID, archeryArea)) {
			player.teleport(archeryZoneID, 81, 89, null, null);
		}

		player.setQuest(QUEST_SLOT, STATE_DONE + ";" + Long.toString(System.currentTimeMillis()));
	}

	/**
	 * Retrieves objects used for archery range functions.
	 */
	public List<Object> getJunitObjects() {
		return Arrays.asList(
				quest,
				blackboard,
				COST,
				TRAIN_TIME);
	}

	/**
	 * Notifies player of time remaining for training & ends training session.
	 */
	private class Timer implements TurnListener {
		private final WeakReference<Player> timedPlayer;

		private Integer timeRemaining = 0;

		protected Timer(final Player player) {
			timedPlayer = new WeakReference<Player>(player);

			try {
				final String questState = timedPlayer.get().getQuest(QUEST_SLOT, 0);
				if (questState != null && questState.equals(STATE_ACTIVE)) {
					// set player's time remaining from quest slot value
					timeRemaining = Integer.parseInt(timedPlayer.get().getQuest(QUEST_SLOT, 1));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onTurnReached(int currentTurn) {
			final Player playerTemp = timedPlayer.get();

			if (playerTemp != null) {
				if (timeRemaining != null && timeRemaining > 0) {
					// notify players at 10 minute mark & every minute after 5 minute mark
					if (timeRemaining == 10 * MathHelper.SECONDS_IN_ONE_MINUTE ||
							(timeRemaining <= 5 * MathHelper.SECONDS_IN_ONE_MINUTE && timeRemaining % 60 == 0)) {
						npc.say(playerTemp.getName() + ", pozostało Tobie " + TimeUtil.timeUntil(timeRemaining) + ".");
					}
					// remaining time needs to be updated every second in order to be saved if player logs out
					timeRemaining = updateTimeRemaining(playerTemp);
					SingletonRepository.getTurnNotifier().notifyInSeconds(1, this);
				} else {
					endTrainingSession(playerTemp);
				}
			}
		}

		@Override
		public int hashCode() {
			final Player player = timedPlayer.get();

			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((player == null) ? 0 : player.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			final Player player = timedPlayer.get();

			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Timer other = (Timer) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (player == null) {
				if (other.timedPlayer.get() != null) {
					return false;
				}
			} else if (!player.equals(other.timedPlayer.get())) {
				return false;
			}
			return true;
		}

		private TolimirNPC getOuterType() {
			return TolimirNPC.this;
		}
	}

	/**
	 * Action that notifies
	 */
	private class ArcheryRangeTimerAction implements ChatAction {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			// remove any existing notifiers
			SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));

			// create the new notifier
			SingletonRepository.getTurnNotifier().notifyInTurns(0, new Timer(player));
		}
	}

	/**
	 * Special portal for checking multiple conditions.
	 */
	private class ArcheryRangeConditionAndActionPortal extends ConditionAndActionPortal {
		/** messages for different rejection reasons */
		private final Map<ChatCondition, List<String>> rejections;

		/** message for when player is pushed into training area by another player */
		private final String pushMessage = "Hej %s! Nie pchaj innych!";

		/** determines if entity was pushed onto portal */
		private boolean wasPushed = false;
		private RPEntity pusher = null;


		public ArcheryRangeConditionAndActionPortal() {
			super(null, null);
			Area area = new Area(SingletonRepository.getRPWorld().getZone(archeryZoneID), archeryArea);

			rejections = new LinkedHashMap<>();
			rejections.put(
					new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
					Arrays.asList(
							NO_ACCESS_MESSAGE,
							pushMessage));
			rejections.put(
					new NotCondition(new AreaIsFullCondition(area, MAX_OCCUPANTS)),
					Arrays.asList(
							FULL_MESSAGE,
							pushMessage));

			setPosition(GATE_POS.x, GATE_POS.y);
			setIgnoreNoDestination(true);
			setResistance(0);
			setForceStop(true);
		}

		private String formatMessage(String message, final RPEntity user) {
			return String.format(message, user.getName());
		}

		/**
		 * Checks the list of conditions & sets the rejection message text.
		 */
		@Override
		protected boolean isAllowed(final RPEntity user) {
			int msgIndex = 0;
			RPEntity msgTarget = user;
			if (wasPushed && pusher != null) {
				msgIndex = 1;
				msgTarget = pusher;
			}

			final Sentence sentence = ConversationParser.parse(user.get("text"));
			for (final ChatCondition cond : rejections.keySet()) {
				if (!cond.fire((Player) user, sentence, this)) {
					setRejectedAction(new MultipleActions(
							new TeleportAction(archeryZoneID, 81, 89, null),
							new SayTextAction(formatMessage(rejections.get(cond).get(msgIndex), msgTarget))));
					return false;
				}
			}

			return true;
		}

		@Override
		public boolean onUsed(final RPEntity user) {
			boolean res = false;

			// don't worry about players trying to leave
			final Direction dir = user.getDirectionToward(this);
			if (dir == Direction.UP) {
				res = super.onUsed(user);
			}

			return res;
		}

		/**
		 * Check access for players pushed onto portal.
		 */
		@Override
		public void onPushedOntoFrom(final RPEntity pushed, final RPEntity pusher, final Point prevPos) {
			wasPushed = true;
			if (pusher != null) {
				this.pusher = pusher;
			}

			// check if entity is being pushed from the right
			if (prevPos.x == getX() + 1) {
				super.onUsed(pushed);
			}

			// reset pushed status
			wasPushed = false;
			this.pusher = null;
		}

		/**
		 * Override to avoid java.lang.NullPointerException.
		 */
		@Override
		protected void rejected(final RPEntity user) {
			if (user instanceof Player) {
				final Player player = (Player) user;

				if (rejectedAction != null) {
					rejectedAction.fire(player, ConversationParser.parse(user.get("text")), new EventRaiser(npc));
				}

				if (forceStop) {
					player.forceStop();
					return;
				}
			}

			user.stop();
		}
	}
}