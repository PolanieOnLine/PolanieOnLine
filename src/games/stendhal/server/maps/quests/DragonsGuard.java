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
// Based on WizardBank and RainbowBeans
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
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
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
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import marauroa.common.game.IRPZone;

/**
 * Controls player access to the Dragon's Land via an NPC. 
 * <p>He takes a fee to enter. Players are allowed only 2 hours access at once.
 * 
 * @author edi18028
 */

public class DragonsGuard extends AbstractQuest implements LoginListener {

	// constants
	private static final String QUEST_SLOT = "dragons_land";

	private static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak";

	private static final String MITHRIL_SHIELD_QUEST_SLOT = "mithrilshield_quest";


	private static final String ZONE_NAME = "0_dragon_land_s";
	private static final List<String> DRAGONS_MAPS = Arrays.asList("0_dragon_land_n",
				"int_dragon_house_1", "int_dragon_house_2", "int_dragon_house_3",
				"int_dragon_house_4", "int_dragon_house_5", "int_dragon_house_6",
				"int_dragon_workshop", "int_dragon_castle", "int_dragon_castle_room_1",
				"int_dragon_castle_room_2", "int_dragon_castle_room_3", "int_dragon_castle_room_4",
				"int_dragon_castle_room_5", "int_dragon_castle_room_6", "int_dragon_castle_dragon_npc",
				"int_dragon_castle_dragon", "-1_dragon_cave");

	/** Time (in Minutes) allowed to stay at dragon's land. */
	private static final int TIME = 60 * 120;

	private static final int REQUIRED_MINUTES = 60 * 24 * 3;

	// Cost to access chests
	private static final int COST = 25000;

	// "static" data
	private StendhalRPZone zone = null;

	private StendhalRPZone mapsDragons = null;

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

				if (playerZone.equals(zone)) {
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
				} else {
					for(final String mapsName : DRAGONS_MAPS) {
						mapsDragons = SingletonRepository.getRPWorld().getZone(mapsName);

						if (playerZone.equals(mapsDragons)) {
							if (counter > 0) {
								counter = counter - 10 * 6;
								SingletonRepository.getTurnNotifier().notifyInTurns(10 * 3 * 6, this);
							} else {
								// teleport the player out
								teleportAway(playerTemp);
							}
						}
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

		npc = new SpeakerNPC("Strażnik Cieciurad") {
			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				// never started quest
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
								new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
								new QuestNotStartedCondition(QUEST_SLOT)),
						ConversationStates.QUEST_OFFERED,
						null,
						new SayTextAction("Witam w Krainie Smoków [name]. Za wejście pobierana jest opłata. Czy chcesz zapłacić za wstęp?"));

				// player returns after required wait time
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
								new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
								new QuestStartedCondition(QUEST_SLOT),
								//new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
								new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
						ConversationStates.QUEST_OFFERED,
						null,
						new SayTextAction("Witam w Krainie Smoków [name]. Czy chcesz zapłacić za wstęp?"));

				// player returns before required wait time
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
								new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
								new QuestStartedCondition(QUEST_SLOT),
								new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
						ConversationStates.ATTENDING,
						null,
						new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Na razie smoki są rozdrażnione ostatnią wizytą. Wróć za "));

				// currently in dragons land
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
								new QuestCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT),
								new QuestActiveCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Witam w Krainie Smoków [name]. Możesz #wyjść wcześniej jeśli chcesz."));

				// hasn't got access to all banks yet
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
							new OrCondition(
									new QuestNotCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT),
									new QuestNotCompletedCondition(MITHRIL_SHIELD_QUEST_SLOT))),
						ConversationStates.IDLE,
						"Nie możesz wejść do krainy smoków, ponieważ nie poradzisz sobie. Nie ukończyłeś zadań, które przygotują cię na to co cię tam czeka. Wróć, gdy wykonasz zadania zlecone przez Josephine, Idę, Baldemara i Krasnoluda. Dowidzenia!",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fee", "opłaty", "opłata", "opłatę"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.QUEST_OFFERED,
						"Opłata wynosi " + COST
						+ " money. Czy chcesz zapłacić?",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("fee", "opłaty", "opłata", "opłatę"),
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Jak już wiesz opłata wynosi "
						+ COST + " money.",
						null);

				add(ConversationStates.QUEST_OFFERED,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("money", COST), 
								new QuestNotActiveCondition(QUEST_SLOT)),
								ConversationStates.ATTENDING,
								"Gratuluję i życzę udanego zwiedzania. Jeżeli skończyłeś przed czasem to powiedz #wyjście.",
								new MultipleActions(
										new DropItemAction("money", COST),
										new TeleportAction(ZONE_NAME, 26, 120, Direction.UP),
										new SetQuestAction(QUEST_SLOT, "start"),
										new ChatAction() {
											@Override
											public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
												SingletonRepository.getTurnNotifier().notifyInTurns(0, new Timer(player));
											}}));

				add(ConversationStates.QUEST_OFFERED,
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
						"Hm nie rozumiem Ciebie. Jeżeli chcesz wyjść to powiedz #wyjście",
						null);

				add(ConversationStates.QUEST_OFFERED,
						ConversationPhrases.NO_MESSAGES,
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Bardzo dobrze.",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.NO_MESSAGES,
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Hm nie rozumiem Ciebie. Jeżeli chcesz wyjść to powiedz #wyjście",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjść", "wyjście", "wyjdź"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Wyjść gdzie?",
						null);


				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjść", "wyjście", "wyjdź"),
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Dziękuję za odwiedzenie Krainy Smoków",
						// we used to use teleportAway() here 
						new MultipleActions(
								new TeleportAction(ZONE_NAME, 26, 125, Direction.DOWN),
								new SetQuestAction(QUEST_SLOT, "done;" + System.currentTimeMillis()),
								new ChatAction() {
									@Override
									public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
										SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
									}}));


				addJob("Pilnuję wejścia do krainy smoków, aby niedośwadczeni wojownicy nie stali się przekąską dla smoków. Za wstęp pobierana jest #opłata.");

				addOffer("Sądzę, że oferowana usługa za #wejściówkę nie jest zbyt dużym obciążeniem dla twojej kieszeni.");

				addReply(Arrays.asList("entrance", "wejściówka", "wejściówkę"),
						"Nie znasz znaczenia tego słowa? Powinieneś spędzać więcej czasu w bibliotece, a nie tylko na walkach i podróżach. Słyszałem, że w Ados jest najlepsza.");

				addHelp("Nie mogę ci za bardzo pomóc. Musisz liczyć na swój miecz lub nogi. Przedtem będziesz musiał uiścić #opłatę za wejście. Nie stoję tutaj na pokaz.");

				addQuest("Możesz wejść do krainy smoków jeżeli wykonałeś zadanie na zabójcze buty, tarczę z mithrilu, płaszcz z mithrilu i ciupagę z dwoma wąsami.");

				addGoodbye("Powodzenia i odwiedź mnie jeszcze.");
			}
		};

		npc.setDescription("Oto strażnik Cieciurad, z którym nie powinieneś zadzierać.");
		npc.setEntityClass("royalguardnpc");
		npc.setPosition(26, 123);
		npc.initHP(100);
		zone.add(npc);
	}

	@Override
	public void onLoggedIn(final Player player) {
		/*
		 *  Stop any possible running notifiers that might be left after the player
		 *  logged out while in the dragon's land. Otherwise the player could be thrown out 
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
				player.teleport(zone, 26, 125, Direction.DOWN, player);

				// complete the quest if it already started
				if (player.hasQuest(QUEST_SLOT)) {
					player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis());
				}
			} else {
				for(final String mapsName : DRAGONS_MAPS) {
					mapsDragons = SingletonRepository.getRPWorld().getZone(mapsName);

					if (playerZone.equals(mapsDragons)) {
						player.teleport(zone, 26, 125, Direction.DOWN, player);

						// complete the quest if it already started
						if (player.hasQuest(QUEST_SLOT)) {
							player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis());
						}
					}
				}
			}
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kraina Smoków",
				"Kraina smoków, gdzie wstęp mają tylko wybrani.",
				false);

		SingletonRepository.getLoginNotifier().addListener(this);

		zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
		createNPC();
	}

	@Override
	public String getName() {
		return "DragonsGuard";
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
		return "Strażnik Cieciurad";
	}

	@Override
	public String getRegion() {
		return Region.DRAGONS_LANDS;
	}
}
