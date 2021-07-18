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
package games.stendhal.server.maps.nalwor.forest;

import static games.stendhal.server.maps.nalwor.forest.AssassinRepairerAdder.ID_NO_AFFORD;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.constants.Testing;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.LogoutListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
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
import games.stendhal.server.entity.npc.TrainingDummy;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ShowItemListEvent;
import games.stendhal.server.maps.nalwor.forest.AssassinRepairerAdder.AssassinRepairer;
import games.stendhal.server.util.TimeUtil;

/**
 * TODO: create JUnit test
 * FIXME: (client) no sound for training targets when hit
 */
public class ArcheryRange implements ZoneConfigurator,LoginListener,LogoutListener {
	/** quest/activity identifier */
	private static final String QUEST_SLOT = "archery_range";

	/** cost to use archery range */
	private static final int COST = 5000;

	/** time (in seconds) allowed for training session */
	private static final int TRAIN_TIME = 15 * MathHelper.SECONDS_IN_ONE_MINUTE;

	/** time player must wait to train again */
	private static final int COOLDOWN = 15;

	/** condition to check if training area is full */
	private ChatCondition rangeFullCondition;

	/** zone info */
	private StendhalRPZone archeryZone;
	private String archeryZoneID;

	/** archery range area */
	private static TrainingArea archeryArea;

	/** NPC that manages archery area */
	private static final String npcName = "Chester";
	private AssassinRepairer ranger;

	private AssassinRepairerAdder repairerAdder;

	private static final Map<String, Integer> repairableSellPrices = new LinkedHashMap<String, Integer>() {{
		put("automatyczna kusza", 2000);
		put("automatyczna kusza A", 5500);
		put("automatyczna kusza A+", 18000);
	}};

	/** phrases used in conversations */
	private static final List<String> TRAIN_PHRASES = Arrays.asList("train", "training", "trening", "trenuj", "trenowanie", "trenować");
	private static final List<String> FEE_PHRASES = Arrays.asList("fee", "cost", "charge", "opłata", "opłatę", "koszt", "cena", "cenę");

	/** quest states */
	private static final String STATE_ACTIVE = "training";
	private static final String STATE_DONE = "done";

	private static final String FULL_MESSAGE = "Strzelnica jest pełna. Wróć później.";

	/** position of gate that manages access to training area */
	private static final Point GATE_POS = new Point(116, 104);

	private static ShopSign blackboard;

	/** message when player tries to enter without paying */
	private static final String NO_ACCESS_MESSAGE = "Hej %s! Nie możesz sobie biegać po mojej strzelnicy za darmo.";


	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		// set up the login/logout notifiers
		SingletonRepository.getLoginNotifier().addListener(this);
		SingletonRepository.getLogoutNotifier().addListener(this);

		archeryZone = zone;
		archeryZoneID = zone.getName();
		//archeryArea = new TrainingArea(archeryZoneID, archeryZone, 97, 97, 19, 10, ranger, new Point(118, 104), GATE_POS, Direction.RIGHT);
		//archeryArea.setCapacity(10);

		// initialize condition to check if training area is full
		rangeFullCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return archeryArea.isFull();
			}
		};

		initEntrance();
		initNPC();
		initShop();
		initRepairShop();
		initTraining();

		if (Testing.COMBAT) {
			initTrainingDummies();
		}
	}

	/**
	 * Initializes portal & gate entities that manage access to the training area.
	 */
	private void initEntrance() {
		// prevents players who haven't paid from entering if gate is open (must be added before gate)
		archeryZone.add(new ArcheryRangeConditionAndActionPortal());

		// gate to enter
		final Gate gate = new Gate("v", "palisade_gate", new QuestInStateCondition("archery_range", 0, STATE_ACTIVE)) {

			@Override
			protected boolean isAllowed(final RPEntity user) {
				// don't worry about players trying to leave
				if (user.getDirectionToward(this) != Direction.LEFT) {
					return true;
				}

				// check if player has paid
				if (!super.isAllowed(user)) {
					ranger.say(NO_ACCESS_MESSAGE.replace("%s", user.getName()));
					return false;
				}

				// check if dojo is full
				if (archeryArea.isFull()) {
					ranger.say(FULL_MESSAGE);
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
		repairerAdder = new AssassinRepairerAdder();

		ranger = repairerAdder.new AssassinRepairer(npcName, repairableSellPrices) {
			@Override
			public void say(final String text) {
				// don't turn toward player
				say(text, false);
			}
		};

		ranger.setDescription("Oto Chester. Jest młodym mężczyzną, który wydaję się być utalentowanym zabójcą.");
		ranger.setEntityClass("rangernpc");
		ranger.setIdleDirection(Direction.DOWN);

		ranger.addGreeting("Witaj na strzelnicy skrytobójców. Lepiej uważaj na cięty język, jeśli nie chcesz zostać zraniony.");
		ranger.addGoodbye("Wróć, gdy zdobędziesz odrobinę gotówki. Dzięki uprzejmości nie ma tu waluty.");
		ranger.addJob("Prowadzę tutejszą strzelnicę. Należy ona do skrytobójców, więc nie wtykaj swego nosa tam, gdzie nie trzeba.");
		ranger.addQuest("Czy wyglądam, abym potrzebował pomocy!? Jeśli nie masz zamiaru #'trenować', to lepiej uciekaj z mojego pola widzenia!");
		ranger.addReply(Arrays.asList("automatyczna kusza", "automatyczne kusze"), "Automatyczne kusze są słabe, ale łatwe do opanowania, możesz z nich strzelać"
				+ " szybciej niżeli ze zwykłego łuku, lecz z powodu ich słabej jakości nie wytrzymują długo.");
		ranger.addHelp("Znajdujesz się w strzelnicy skrytobójców. Mogę pozwolić ci #'trenować' swoje umiejętności"
				+ " dystansowe za drobną #'opłatą'. Jeśli nie masz dobrej celności, wypróbuj tarcze na końcu."
				+ " Ninje je często wykorzystują. Zalecam używanie #'automatyczne kusze'.");
		ranger.addReply(FEE_PHRASES, "Opłata za #trenowanie tutaj to " + Integer.toString(COST) + " money.");

		ranger.setGender("M");
		ranger.setPosition(120, 99);
		archeryZone.add(ranger);
	}

	/**
	 * Adds bow & arrows for sale from NPC.
	 */
	private void initShop() {
		final String rejectedMessage = "Nie sprzedam ci niczego bez określonego dowodu, że można ci ufać.";

		// override the default offer message
		ranger.add(ConversationStates.ANY,
				ConversationPhrases.OFFER_MESSAGES,
				new PlayerHasItemWithHimCondition("licencja na zabijanie"),
				ConversationStates.ATTENDING,
				"Nie patrz się tak na mnie, wszyscy są głupcami! Sprawdź moje ceny łuków oraz strzał"
						+ " na tej czarnej tablicy znajdująca się obok mnie. Jeśli szukasz specjalnej okazji"
						+ " to jej tutaj nie znajdziesz! Znajdź sobie innego bezmyślnego frajera.",
				null);

		// player wants to buy items but does not have assassins id
		ranger.add(ConversationStates.ANY,
				ConversationPhrases.OFFER_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("licencja na zabijanie")),
				ConversationStates.ATTENDING,
				rejectedMessage,
				null);

		// prices are higher than those of other shops
		final Map<String, Integer> shop = new LinkedHashMap<>();
		shop.put("strzała", 4);
		shop.put("włócznia", 125);
		shop.put("drewniany łuk", 600);
		shop.put("długi łuk", 1200);
		for (final String crossbow: repairableSellPrices.keySet()) {
			shop.put(crossbow, repairableSellPrices.get(crossbow));
		}

		// override seller bahaviour so that player must have assassins id
		final SellerBehaviour seller = new SellerBehaviour(shop) {
			@Override
			public ChatCondition getTransactionCondition() {
				return new PlayerHasItemWithHimCondition("licencja na zabijanie");
			}

			@Override
			public ChatAction getRejectedTransactionAction() {
				return new SayTextAction(rejectedMessage);
			}
		};
		new SellerAdder().addSeller(ranger, seller, false);

		// a sign showing prices of items
		blackboard = new ShopSign("sellarcheryrange", "Sklep łuczniczy dla zabójców", "Sprzedawane są tu łuki i strzały:", true) {
			@Override
			public boolean onUsed(final RPEntity user) {
				if (user.isEquipped("licencja na zabijanie")) {
				// Chester is protective, even of his blackboard if player doesn't have assassins ID
					List<Item> itemList = generateItemList(shop);
					ShowItemListEvent event = new ShowItemListEvent(this.title, this.caption, itemList);
					user.addEvent(event);
					user.notifyWorldAboutChanges();
				} else {
					ranger.say("Odejdź od mojej tablicy kundlu!");
				}

				return true;
			}
		};
		blackboard.setEntityClass("blackboard");
		blackboard.setPosition(117, 100);
		archeryZone.add(blackboard);
	}

	/**
	 * If players bring their worn training swords they can get them repaired for half the
	 * price of buying a new one.
	 */
	private void initRepairShop() {
		final Sign repairSign = new Sign();
		repairSign.setEntityClass("notice");
		repairSign.setPosition(118, 100);
		repairSign.setText("Automatyczne kusze #naprawiane za połowę ceny nowych.");
		archeryZone.add(repairSign);

		final Map<String, Integer> repairPrices = new LinkedHashMap<>();
		for (final String itemName: repairableSellPrices.keySet()) {
			repairPrices.put(itemName, repairableSellPrices.get(itemName) / 2);
		}

		repairerAdder.add(ranger, repairPrices);
		repairerAdder.setReply(ID_NO_AFFORD, "Nie masz dość pieniędzy. Wynoś się stąd!");
	}

	/**
	 * Initializes conversation & actions for archery training.
	 */
	private void initTraining() {

		final ChatCondition meetsLevelCapCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				return archeryArea.meetsLevelCap(player, player.getRatk());
			}
		};

		// player has never trained before
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie")),
				ConversationStates.QUESTION_1,
				null,
				new MultipleActions(
						new NPCEmoteAction(npcName + " sprawdza twoją licencje na zabijanie.", false),
						new SayTextAction("Hmmm, nie widziałem cię tu wcześniej."
								+ " Ale posiadasz odpowiednie poświadczenia. Czy chcesz abym"
								+ " otworzył dla ciebie strzelnicę? Będzie cię to kosztować " + Integer.toString(COST)
								+ " money.")));

		// player returns after cooldown period is up
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, STATE_DONE),
						new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN),
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie")),
				ConversationStates.QUESTION_1,
				"To będzie cię kosztować " + Integer.toString(COST) + " money za trening. Więc, zgadzasz się na to?",
				null);

		// player returns before cooldown period is up
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, COOLDOWN)),
						new NotCondition(meetsLevelCapCondition)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, COOLDOWN, "Nie możesz jeszcze trenować. Wróc za"));

		// player's RATK level is too high
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				meetsLevelCapCondition,
				ConversationStates.ATTENDING,
				"Jesteś już zbyt wyszkolony, by tu trenować. A teraz wynoś się ty leniwcu i walcz z potworami!",
				null);

		// player does not have an assassins id
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(meetsLevelCapCondition),
						new NotCondition(new PlayerHasItemWithHimCondition("licencja na zabijanie"))),
				ConversationStates.ATTENDING,
				"Nie możesz tutaj trenować bez pozwolenia od kwatery głównej zabójców. A teraz uciekaj, zanim puszczę psy na ciebie!",
				null);

		// player training state is active
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				"Wynoś się stąd! Zapłaciłeś już za sesję szkoleniową.",
				null);

		// player meets requirements but training area is full
		ranger.add(ConversationStates.ATTENDING,
				TRAIN_PHRASES,
				new AndCondition(
						new NotCondition(meetsLevelCapCondition),
						new PlayerHasItemWithHimCondition("licencja na zabijanie"),
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
		ranger.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", COST),
				ConversationStates.IDLE,
				"Możesz trenować maksymalnie do " + Integer.toString(TRAIN_TIME / MathHelper.SECONDS_IN_ONE_MINUTE) + " minut. Więc dobrze wykorzystaj swój czas.",
				new MultipleActions(
						new DropItemAction("money", COST),
						new SetQuestAction(QUEST_SLOT, STATE_ACTIVE + ";" + Integer.toString(TRAIN_TIME)),
						new ArcheryRangeTimerAction()));

		// player does not have enough money to begin training
		ranger.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("money", COST)),
				ConversationStates.ATTENDING,
				"Co to ma być? Nie masz nawet wystarczająco dużo pieniędzy na #'opłatę'. Odejdź stąd!",
				null);

		// player does not want to train
		ranger.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"No to odejdź stąd i nie trać mojego cennego czasu!",
				null);

		/* FIXME: How to get updated remaining time?
		// player asks how much time is left in training session
				Arrays.asList("time", "czas"),
		ranger.add(ConversationStates.ATTENDING,
				new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, TRAIN_TIME, "Twój trening zakończy się za około"));
		*/
	}

	private void initTrainingDummies() {
		// normally added in tiled, but instantiated here so that can be disabled with "Testing.COMBAT"

		// locations of targets
		final List<Node> nodes = Arrays.asList(
				new Node(97, 99),
				new Node(99, 99),
				new Node(101, 99),
				new Node(103, 98),
				new Node(105, 97),
				new Node(107, 97),
				new Node(109, 97),
				new Node(111, 97),
				new Node(113, 97),
				new Node(115, 97));

		for (final Node node: nodes) {
			final TrainingDummy target = new TrainingDummy("other/bullseye", "Oto treningowa tarcza strzelecka.");
			target.setPosition(node.getX(), node.getY());
			archeryZone.add(target);
		}
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
		//if (archeryArea.contains(player) || (player.getX() == GATE_POS.x && player.getY() == GATE_POS.y)) {
		//	player.teleport(archeryZoneID, 118, 104, null, null);
		//}

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
	 * Teleports player out of archery range training area.
	 */
	private void endTrainingSession(final Player player) {
		if (player.get("zoneid").equals(archeryZoneID)) {
			ranger.say("Twój trening właśnie się skończył " + player.getName() + ".");
		}
		if (archeryArea.contains(player)) {
			player.teleport(archeryZoneID, 118, 104, null, null);
		}

		player.setQuest(QUEST_SLOT, STATE_DONE + ";" + Long.toString(System.currentTimeMillis()));
	}

	/**
	 * Retrieves objects used for archery range functions.
	 */
	public List<Object> getJunitObjects() {
		return Arrays.asList(
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
						ranger.say(playerTemp.getName() + ", pozostało Tobie " + TimeUtil.timeUntil(timeRemaining) + ".");
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

		private ArcheryRange getOuterType() {
			return ArcheryRange.this;
		}
	}

	/**
	 * Action that notifies
	 */
	private class ArcheryRangeTimerAction implements ChatAction {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
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

			rejections = new LinkedHashMap<>();
			rejections.put(
					new QuestInStateCondition(QUEST_SLOT, 0, STATE_ACTIVE),
					Arrays.asList(
							NO_ACCESS_MESSAGE,
							pushMessage));
			rejections.put(
					new NotCondition(rangeFullCondition),
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
							new TeleportAction(archeryZoneID, 117, 104, null),
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
			if (dir == Direction.LEFT) {
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
					rejectedAction.fire(player, ConversationParser.parse(user.get("text")), new EventRaiser(ranger));
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
