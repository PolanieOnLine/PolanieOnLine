/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.IRPZone;

/**
 * Controls player access to the Wizard's Bank via an NPC. 
 * <p>He takes a fee to enter. Players are allowed only 5 minutes access at once.
 * 
 * @author kymara
 */

public class WizardBank extends AbstractQuest implements LoginListener {
	
	// constants
	private static final String QUEST_SLOT = "wizard_bank";

	private static final String GRAFINDLE_QUEST_SLOT = "grafindle_gold";

	private static final String ZARA_QUEST_SLOT = "suntan_cream_zara";

	private static final String ZONE_NAME = "int_magic_bank";

	/** Time (in Seconds) allowed in the bank. */
	private static final int TIME = 60 * 5;

	// Cost to access chests
	private static final int COST = 1000;

	// "static" data
	private StendhalRPZone zone = null;

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
				}
			}
		}
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void createNPC() {
		
		npc = new SpeakerNPC("Javier X") {
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
								new QuestCompletedCondition(GRAFINDLE_QUEST_SLOT), 
								new QuestCompletedCondition(ZARA_QUEST_SLOT),
								new QuestCompletedCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    null,
					    new SayTextAction("Witam w Banku Czarodzieja [name]. Czy chcesz zapłacić za dostęp do skrzyń?"));

				// never started quest
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(GRAFINDLE_QUEST_SLOT), 
								new QuestCompletedCondition(ZARA_QUEST_SLOT),
								new QuestNotStartedCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    null,
					    new SayTextAction("Witam w Banku Czarodzieja [name]."));
				
				// currently in bank
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(GRAFINDLE_QUEST_SLOT), 
								new QuestCompletedCondition(ZARA_QUEST_SLOT),
								new QuestActiveCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    null,
					    new SayTextAction("Witam w Banku Czarodzieja [name]. Możesz #wyjść wcześniej jeśli chcesz."));

				// hasn't got access to all banks yet
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
							new OrCondition(
									new QuestNotCompletedCondition(GRAFINDLE_QUEST_SLOT), 
									new QuestNotCompletedCondition(ZARA_QUEST_SLOT))),
						ConversationStates.IDLE,
						"Nie możesz skorzystać z tego banku jeżeli nie masz prawa do korzystania ze skrzynki w Nalwor i jeżeli nie zdobyłeś zaufania u młodej kobiety. Dowidzenia!",
						null);
				
				add(ConversationStates.ATTENDING,
						Arrays.asList("fee", "opłaty", "opłata", "opłatę"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
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
				
				add(ConversationStates.ATTENDING,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("money", COST), 
								new QuestNotActiveCondition(QUEST_SLOT)),
								ConversationStates.ATTENDING,
								"Skrzynie banków Semos, Nalwor i Fado są po mojej prawej. Skrzynie banków z Ados i twojej przyjaciółki Zary są po mojej lewej. Jeżeli skończyłeś przed czasem to powiedz #wyjście.",
								new MultipleActions(
										new DropItemAction("money", COST),
										new TeleportAction(ZONE_NAME, 10, 10, Direction.DOWN),
										new SetQuestAction(QUEST_SLOT, "start"),
										new ChatAction() {
											@Override
											public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
												SingletonRepository.getTurnNotifier().notifyInTurns(0, new Timer(player));
											}}));
				
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
						"Hm nie rozumiem Ciebie. Jeżeli chcesz wyjść to powiedz #wyjście",
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
						"Hm nie rozumiem Ciebie. Jeżeli chcesz wyjść to powiedz #wyjście",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjście", "wyjdź"),
						new QuestNotActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Wyjść gdzie?",
						null);


				add(ConversationStates.ATTENDING,
						Arrays.asList("leave", "wyjście", "wyjdź"),
						new QuestActiveCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Dziękuję za skorzystanie z Banku Czarodzieja",
						// we used to use teleportAway() here 
						new MultipleActions(
								new TeleportAction(ZONE_NAME, 15, 16, Direction.DOWN),
								new SetQuestAction(QUEST_SLOT, "done"),
								new ChatAction() {
									@Override
									public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
										SingletonRepository.getTurnNotifier().dontNotify(new Timer(player));
									}}));


				addJob("Kontroluje dostęp do banku. Moje zaklęcia zapewniają, że nie można tutaj łatwo wejść. Pobieramy #opłaty za wejście.");

				addReply(Arrays.asList("magic", "magią"),
						"Nie słyszałeś o magii? Powoduje, że rośnie tutaj trawa. Pewnego razu twój rodzaj nauczy się jak korzystać z tej wspaniałej sztuki.");

				addOffer("Sądzę, że oferowana usługa #podatkowa nie jest zbyt dużym dla Ciebie obciążeniem.");

				addReply(Arrays.asList("fiscal", "podatkowa"),
						"Nie znasz znaczenia tego słowa? Powinieneś spędzać więcej czasu w bibliotece. Słyszałem, że w Ados jest najlepsza.");

				addHelp("Ten bank jest napełniony #magią, gdzie masz dostęp do każdego skarbca. Będziesz musiał zapłacić #opłatę za ten przywilej. Nie jesteśmy instytucją charytatywną.");

				addQuest("Możesz korzystać z banku tylko jeżeli masz prawo do używania skrzyni w Nalwor i wtedy gdy zapracowałeś na zaufanie młodej kobiety.");

				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto czarodziej, z którym nie powinieneś zadzierać.");
		npc.setEntityClass("brownwizardnpc");
		npc.setPosition(15, 10);
		npc.initHP(100);
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
				player.teleport(zone, 15, 16, Direction.DOWN, player);

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
				"Bank Czarodzieja",
				"Czy chcesz, abym zaopiekował się wszystkimi twoimi skrzyniami w tym samym czasie? Jeżeli tak to wejdź do Wizard Bank.",
				false);

		SingletonRepository.getLoginNotifier().addListener(this);

		zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
		createNPC();
	}

	@Override
	public String getName() {
		return "WizardBank";
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
		return "Javier X";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CAVES;
	}
}
