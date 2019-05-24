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
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.game.NineSwitchesGameBoard;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;

/**
 * A Game about Nine switches game for one player
 *
 * @author hendrik
 */
public class NineSwitchesGame implements LoadableContent {
	private StendhalRPZone zone = null;
	private NineSwitchesGameBoard board;
	private SpeakerNPC npc;

	private static final int CHAT_TIMEOUT = 60;
	
	private void addBoard() {
		board = new NineSwitchesGameBoard(zone, 94, 106);
	}

	private void addNPC() {
		npc = new SpeakerNPC("Maltos") {
			@Override
			protected void createPath() {
					// NPC doesn't move
					setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Witam w naszej małej grze dziewięć przycisków. Twoim zadaniem jest skierowanie wszystkich strzałekw prawo."
						+ " Łatwe? Cóż jest pewien #haczyk.");
				addReply(Arrays.asList("catch", "haczyk"), 
						" Każdy przycisk jest połączony z sąsiednim, który je zmieni. Masz jedną minutę na rozwiązanie zagadki."
						+ " Czy chcesz #zagrać?.");
				addJob("Jestem mistrzem tej gry.");
				addGoodbye("Było miło cię poznać.");
				add(ConversationStates.ATTENDING,
						Arrays.asList("play", "play?", "game", "yes", "zagrać", "zagrać?", "gra", "tak"),
						ConversationStates.ATTENDING,
						"Powodzenia.",
						new PlayAction(board));
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};
		npc.setEntityClass("gamesupervisornpc"); 
		npc.setPlayerChatTimeout(CHAT_TIMEOUT);
		npc.setPosition(98, 104);
		npc.setDescription("Oto Maltos. Nie jesteś ciekawy jego uczesania?");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

	/**
	 * handles a play chat action
	 */
	private static class PlayAction implements ChatAction {
		private NineSwitchesGameBoard board;

		/**
		 * creates a new PlayAction.
		 *
		 * @param board
		 */
		public PlayAction(NineSwitchesGameBoard board) {
			this.board = board;
		}

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (board.getPlayerName() != null) {
				npc.say("Przykro mi " + player.getName() + ", ale już ktoś gra. Poczekaj na swoją kolej.");
				return;
			}
			board.setPlayerName(player.getName());
		}
	}

	@Override
	public void addToWorld() {
		zone = SingletonRepository.getRPWorld().getZone("0_semos_mountain_n2");

		addBoard();
		addNPC();
		board.setNPC(npc);
	}


	/**
	 * try to remove the content from the world-
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean removeFromWorld() {
		NPCList.get().remove("Paul Sheriff");
		zone.remove(npc);
		board.remove();
		return true;
	}
}
