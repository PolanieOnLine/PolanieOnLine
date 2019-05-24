/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
// Based on WizardBank.

package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import marauroa.common.game.IRPZone;

/**
 * Controls player access to the Wizard's Bank via an NPC. 
 * <p>He takes a fee to enter. Players are allowed only 5 minutes access at once.
 * 
 * @author kymara
 */

public class ZakopaneClouds extends AbstractQuest implements LoginListener {

	private static final String QUEST_SLOT = "zakopane_clouds";

	private static final String ZLOTA_CIUPAGA_WAS_QUEST_SLOT = "zlota_ciupaga_was";
	
	private static final String MITHRILSHIELD_QUEST_QUEST_SLOT = "mithrilshield_quest";

	private static final String ZAGADKI_QUEST_SLOT = "zagadki";

	private static final String ZONE_NAME = "int_zakopane_mill_0";

	private static final String ZONE_NAME_1 = "int_zakopane_mill_1";

	/** Time (in Seconds) allowed in the bank. */
	private static final int TIME = 60 * 30;

	// Cost to access chests
	private static final int COST = 10000;

	// "static" data
	private StendhalRPZone zone = null;

	private StendhalRPZone zone_1 = null;

	private SpeakerNPC npc;

	/**
	 * Tells the player the remaining time and teleports him out when his time
	 * is up.
	 */
	class Timer implements TurnListener {
		private final WeakReference<Player> timerPlayer;
		/**
		 * Using unique playername for hashcode.
		 */
		private final String playername;
		/**
		 * Starts a teleport-out-timer.
		 * 
		 * @param player
		 *            the player who started the timer
		 */
		protected Timer(final Player player) {
			timerPlayer = new WeakReference<Player>(player);
			playername = player.getName();
		}

		private int counter = TIME;

		// override equals

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			if (playername == null) {
				return prime * result;
			} else {
				return prime * result + playername.hashCode();
			}
		}

		@Override
		public boolean equals(final Object obj) {
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
			
			if (playername == null) {
				if (other.playername != null) {
					return false;
				}
			} else if (!playername.equals(other.playername)) {
				return false;
			}
			return true;
		}

		// override hash
		@Override
		public void onTurnReached(final int currentTurn) {
			// check that the player is still in game and stop the timer
			// in case the player is not playing anymore.
			// Note that "player" always refers to the current player
			// in order not to teleport the next player out too early,
			// we have to compare it to the player who started this timer
			final Player playerTemp = timerPlayer.get();

			if (playerTemp != null) {
				final IRPZone playerZone = playerTemp.getZone();

				if ((playerZone.equals(zone)) || (playerZone.equals(zone_1))) {
					if (counter > 0) {
						npc.say(playerTemp.getTitle() + " zostało Tobie "
								+ TimeUtil.timeUntil(counter) + ".");
						counter = counter - 10 * 6;
						SingletonRepository.getTurnNotifier().notifyInTurns(10 * 3 * 6, this);
					} else {
						// teleport the player out
						npc.say("Przepraszam " + playerTemp.getTitle()
								+ ", ale twój czas dobiegł końca.");
						teleportAway(playerTemp);
					}
				}
			}
		}
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void createNPC() {
		npc = new SpeakerNPC("Derwan") {
			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				// has been here before
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(ZLOTA_CIUPAGA_WAS_QUEST_SLOT), 
								new QuestCompletedCondition(MITHRILSHIELD_QUEST_QUEST_SLOT),
								new QuestCompletedCondition(ZAGADKI_QUEST_SLOT),
								new QuestCompletedCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Witaj  [name] w miejscu, z którego można dostać się do władcy smoków. Czy chcesz tam dostać się?"));

				// never started quest
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(ZLOTA_CIUPAGA_WAS_QUEST_SLOT), 
								new QuestCompletedCondition(MITHRILSHIELD_QUEST_QUEST_SLOT),
								new QuestCompletedCondition(ZAGADKI_QUEST_SLOT),
								new QuestNotStartedCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Witaj  [name] w miejscu, z którego można dostać się do władcy smoków."));

				// currently in bank
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(ZLOTA_CIUPAGA_WAS_QUEST_SLOT), 
								new QuestCompletedCondition(MITHRILSHIELD_QUEST_QUEST_SLOT),
								new QuestCompletedCondition(ZAGADKI_QUEST_SLOT),
								new QuestActiveCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Witaj  [name] w miejscu, z którego można dostać się do władcy smoków.. Możesz wyjść wcześniej. Jeżeli chcesz to powiedz tylko #wyjście.."));

				// hasn't got access to all banks yet
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
						new OrCondition(
								new QuestNotCompletedCondition(ZLOTA_CIUPAGA_WAS_QUEST_SLOT), 
								new QuestNotCompletedCondition(MITHRILSHIELD_QUEST_QUEST_SLOT), 
								new QuestNotCompletedCondition(ZAGADKI_QUEST_SLOT))),
						ConversationStates.IDLE,
						"Nie mogę cię przepuścić do władcy smoków dopóki nie posiądziesz złotej ciupagi z wąsem, mithril shieldu. Zrób tagże zadanie u Brzezdoma. Dowidzenia!",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fee", "enter", "opłaty", "opłata", "opłatę"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Cena wynosi " + COST
						+ " money. Czy chcesz zapłacić?",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fee", "enter", "opłaty", "opłata", "opłatę"),
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Jak już wiesz cena wynosi "
						+ COST + " money.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("money", COST), 
								new QuestNotActiveCondition(QUEST_SLOT)),
								ConversationStates.ATTENDING,
								"Uważaj tam na serafina!!!  Czasami pojawia się niespodziewanie.",
								new MultipleActions(
										new DropItemAction("money", COST),
										new TeleportAction(ZONE_NAME, 14, 8, Direction.DOWN),
										new SetQuestAction(QUEST_SLOT, "start"),
										new ChatAction() {
											@Override
											public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
												SingletonRepository.getTurnNotifier().notifyInTurns(0, new Timer(player));
											}
										}));

				add(ConversationStates.ATTENDING,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("money", COST)), 
								new QuestNotActiveCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						"Nie masz tyle pieniędzy!",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.YES_MESSAGES,
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Hm nie rozumiem Ciebie. Jeżeli chcesz wyjść to wyjdź.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.NO_MESSAGES,
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Bardzo dobrze.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.NO_MESSAGES,
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Hm nie rozumiem Ciebie. Chcesz wyjść powiedz #wychodzę",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjście", "wyjdź", "wychodzę"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Wyjść gdzie?",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjście", "wyjdź", "wychodzę"),
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"I jak było u władcy smoków?",
						// we used to use teleportAway() here 
						new MultipleActions(
								new TeleportAction(ZONE_NAME, 9, 12, Direction.DOWN),
								new SetQuestAction(QUEST_SLOT, "done"),
								new ChatAction() {
									@Override
									public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
										SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
									}}));

				addJob("Wpuszczam wojowników do komnaty, z której przeniesiesz się do władcy smoków. Bez #opłaty nie dostaniesz się do niego.");
				addOffer("Mogę cię wpuścić do komnaty, z której dostaniesz się do władcy smoków, ale uważaj czasami można tam spotkać serafina.");
				addHelp("Pomogę ci się dostać do władcy smoków. Będziesz musiał zapłacić #opłatę za ten przywilej. Nie robię tego za darmo. Powiedz #opłata #10000 jeżeli zdecydujesz się.");
				addQuest("Możesz korzystać z moich usług jeżeli spełniłeś pewne warunki.");
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto wielki i sławny Derwan.");
		npc.setEntityClass("brownwizardnpc");
		npc.setPosition(14, 10);
		npc.initHP(1000);
		zone.add(npc);
	}

	@Override
	public void onLoggedIn(final Player player) {
		/*
		 *  Stop any possible running notifiers that might be left after the player
		 *  logged out while in the bank. Otherwise the player could be thrown out 
		 *  too early if he goes back.
		 */
		SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
		teleportAway(player);
	}

	/**
	 * Finishes the time and teleports the player out.
	 * 
	 * @param player
	 *            the player to teleport out
	 */
	private void teleportAway(final Player player) {
		if (player != null) {
			final IRPZone playerZone = player.getZone();
			if (playerZone.equals(zone)) {
				player.teleport(zone, 9, 12, Direction.DOWN, player);

				// complete the quest if it already started
				if (player.hasQuest(QUEST_SLOT)) {
					player.setQuest(QUEST_SLOT, "done");
				}
			} else if (playerZone.equals(zone_1)) {
				player.teleport(zone_1, 17, 17, Direction.DOWN, player);

				// complete the quest if it already started
				if (player.hasQuest(QUEST_SLOT)) {
					player.setQuest(QUEST_SLOT, "done");
				}
			}
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Chmury Zakopanego",
			"Chcesz odwiedzić Władce Smoków? Musisz miec zrobione zadanie na: tarcze z mithrilu, ciupagę z wąsem i u Brzezdoma.",
			false);

		SingletonRepository.getLoginNotifier().addListener(this);

		zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
		zone_1 = SingletonRepository.getRPWorld().getZone(ZONE_NAME_1);
		createNPC();
	}

	@Override
	public String getName() {
		return "ZakopaneClouds";
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	@Override
	public String getNPCName() {
		return "Derwan";
	}
}
