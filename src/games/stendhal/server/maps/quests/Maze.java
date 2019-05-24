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
package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.maze.MazeGenerator;
import games.stendhal.server.maps.quests.maze.MazeSign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Maze extends AbstractQuest {
	/** Minimum time between repeats. */
	private static final int COOLING_TIME = MathHelper.MINUTES_IN_ONE_HOUR * 24;
	private MazeSign sign;
	private MazeGenerator maze = null;
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Labirynt",
				"Wszystko co potrzebujesz to dobre oko...",
				false);
		addMazeSign();
		setupConversation();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(getSlotName())) {
				return res;
			}
			res.add("Haizen stworzył magiczny labirynt dla mnie do przejścia.");

			if (player.getZone().getName().endsWith("_maze")) {
				res.add("Obecnie jestem uwięziony w labiryncie.");
			} else {
				if (!isCompleted(player)) {
					res.add("Nie mogę przejść ostatniego labiryntu.");
				} else {
					res.add("Przeszedłem labirynt!");
				}
				if (isRepeatable(player)) {
					res.add("Mógłbym jeszcze raz spróbować przejść labirynt.");
				} else {
					res.add("Haizen nie ma czasu na stworznie nowego labiryntu dla mnie.");
				}
			}
			final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 1) {
				res.add("Do tej pory przeszedłem " + repetitions + " razy labirynt.");
			}

			return res;
	}

	@Override
	public String getName() {
		return "Maze";
	}

	@Override
	public String getSlotName() {
		return "maze";
	}
	
	@Override
	public boolean isRepeatable(Player player) {
		return new TimePassedCondition(getSlotName(), 1, COOLING_TIME).fire(player, null, null);
	}
	
	private SpeakerNPC getNPC() {
		return npcs.get("Haizen");
	}
	
	private void addMazeSign() {
		setSign(new MazeSign());
		getSign().setPosition(10, 7);
		getNPC().getZone().add(getSign());
	}
	
	private void setupConversation() {
		SpeakerNPC npc = getNPC();
		
		npc.addQuest("Mogę pokazać ci #labirynt, z którego będziesz musiał znaleźć drogę wyjścia. W tej niebieskiej księdze znajdziesz imiona rycerzy, którzy najszybciej pokonali drogę i znaleźli wyjście.");
	
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("maze", "labirynt"),
				new TimePassedCondition(getSlotName(), 1, COOLING_TIME),
				ConversationStates.QUEST_OFFERED,
				"W przeciwległym rogu znajdziesz wyjście z labiryntu. Zwykle w pozostałych rogach umieszczam zwoje, które możesz zabrać o ile pozwoli ci na to czas. Czy chcesz podjąć wyzwanie?",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("maze", "labirynt"),
				new NotCondition(new TimePassedCondition(getSlotName(), 1, COOLING_TIME)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(getSlotName(), 1, 
						COOLING_TIME, "Mogę wysłać cię do labiryntu tylko raz dziennie. Można tam powrócić za"));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new SendToMazeChatAction());
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Wyglądasz jakbyś już się zgubił.",
				null);
	}
	
	private void setSign(MazeSign sign) {
		this.sign = sign;
	}

	public MazeSign getSign() {
		return sign;
	}

	private class SendToMazeChatAction implements ChatAction {
		public SendToMazeChatAction() {
			// empty constructor to prevent warning
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			maze = new MazeGenerator(player.getName() + "_maze", 128, 128);
			maze.setReturnLocation(player.getZone().getName(), player.getX(), player.getY());
			maze.setSign(getSign());
			StendhalRPZone zone = maze.getZone();
			SingletonRepository.getRPWorld().addRPZone(zone);
			new SetQuestAction(getSlotName(), 0, "start").fire(player, sentence, raiser);
			new SetQuestToTimeStampAction(getSlotName(), 1).fire(player, sentence, raiser);
			maze.startTiming();
			player.teleport(zone, maze.getStartPosition().x, maze.getStartPosition().y, Direction.DOWN, player);
		}
	}

	/**
	 * Access the portal from MazeTest.
	 * 
	 * @return return portal from the maze
	 */
	protected Portal getPortal() {
		return maze.getPortal();
	}

	@Override
	public String getNPCName() {
		return "Haizen";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
