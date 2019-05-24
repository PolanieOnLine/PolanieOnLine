/* $Id$ */
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
package games.stendhal.server.maps.quests.revivalweeks;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.game.TicTacToeBoard;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.NPCSetDirection;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;

/**
 * A Tic Tac Toe game for two players
 *
 * @author hendrik
 */
public class TicTacToeGame implements LoadableContent {
	private StendhalRPZone zone = null;
	private TicTacToeBoard board;
	private SpeakerNPC paul;

	/**
	 * creates TicTacToeBoard and adds it to the world.
	 */
	private void addBoard() {
		board = new TicTacToeBoard();
		board.setPosition(83, 114);
		zone.add(board);
		board.addToWorld();
	}

	/**
	 * adds the NPC which moderates the game to the world.
	 */
	private void addNPC() {
		paul = new SpeakerNPC("Paul Sheriff") {
			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE, 
						ConversationPhrases.GREETING_MESSAGES,
						new GreetingMatchesNameCondition(getName()), true,
						ConversationStates.IDLE,
						"Cześć! Witam w naszej małej grze Tic Tac Toe. Zadaniem jest wypełnieniem rzędu "
						+ "(pionowo, poziomu lub po przekątnej) tymi samymi krążkami. "
						+ "Potrzebujesz rywala, aby #zagrać.",
						null);

				add(ConversationStates.IDLE, 
						ConversationPhrases.HELP_MESSAGES, 
						ConversationStates.IDLE,
						"Musisz stanąć obok krążka, aby go przesunąć.",
						null);
				add(ConversationStates.IDLE, 
						ConversationPhrases.JOB_MESSAGES, 
						ConversationStates.IDLE,
						"Jestem opiekunem tej gry.",
						null);
				add(ConversationStates.IDLE, 
						ConversationPhrases.GOODBYE_MESSAGES, 
						ConversationStates.IDLE,
						"Miło było Cię poznać.",
						new NPCSetDirection(Direction.DOWN));
				add(ConversationStates.IDLE,
						Arrays.asList("play", "game", "yes", "zagrać", "graj", "tak"),
						ConversationStates.IDLE,
						null,
						new PlayAction(board));
			}
		};
		paul.setEntityClass("paulnpc"); 
		paul.setPosition(84, 112);
		paul.setDirection(Direction.DOWN);
		zone.add(paul);
	}

	/**
	 * handles a play chat action
	 */
	private static class PlayAction implements ChatAction {
		private TicTacToeBoard board;
		private long lastPlayerAdded;

		/**
		 * creates a new PlayAction.
		 *
		 * @param board
		 */
		public PlayAction(TicTacToeBoard board) {
			this.board = board;
		}

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (board.isGameActive()) {
				npc.say("Przepraszam, ale " + player.getName() + " już gra. Poczekaj chwilę.");
				return;
			}

			if (lastPlayerAdded + 60000 < System.currentTimeMillis()) {
				board.getPlayers().clear();
			}

			if (board.getPlayers().isEmpty()) {
				lastPlayerAdded = System.currentTimeMillis();
				npc.say("Dobrze " + player.getName() + " jesteś następny do kolejnej gry. Czy ktoś chce #zagrać przeciwko " + player.getName() + "?"); 
				board.getPlayers().add(player.getName());
			} else {
				if (board.getPlayers().get(0).equals(player.getName())) {
					npc.say("Dobrze " + player.getName() + " jesteś następny do kolejnej gry. Czy ktoś chce #zagrać przeciwko " + player.getName() + "?");
					return;
				}

				npc.say(board.getPlayers().get(0) + " grasz niebieskimi X i zaczynasz pierwszy. " + player.getName() + " grasz czerwonymi O. Niech wygra lepszy!");
				board.startGame();
				board.getPlayers().add(player.getName());
			}
		}
	}

	@Override
	public void addToWorld() {
		zone = SingletonRepository.getRPWorld().getZone("0_semos_mountain_n2");

		addBoard();
		addNPC();
		board.setNPC(paul);
	}

	/**
	 * try to remove the content from the world-
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean removeFromWorld() {
		NPCList.get().remove("Paul Sheriff");
		zone.remove(paul);
		zone.remove(board);
		return true;
	}

}
