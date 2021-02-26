/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.janosik;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.MultiZonesFixedPathsList;
import games.stendhal.server.core.pathfinder.RPZonePath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ActorNPC;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class OutgoingPhase extends RAQuest {
	private final SpeakerNPC knight = new SpeakerNPC("Rycerz");
	private final SpeakerNPC mainNPC = RAQuestHelperFunctions.getMainNPC();
	private final int minPhaseChangeTime;
	private int maxPhaseChangeTime;
	private List<List<RPZonePath>> fullpath = new LinkedList<List<RPZonePath>>();
	private LinkedList<Creature> monsters;

	/**
	 * adding quest related npc's fsm states
	 */
	private void addConversations() {
		RA_Phase myphase = OUTGOING;

		// Player asking about rats
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("zbójników", "zbójników!"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"Zbójnicy znikneli z krainy. "
				+ "Możesz teraz odebrać #nagrodę za pomoc, zapytaj o #szczegóły "
				+ "jeżeli chcesz wiedzieć więcej.",
				null);

		// Player asking about details.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("details", "szczegóły"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new DetailsKillingsAction());

		// Player asking about reward
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("reward", "nagroda", "nagrodę"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new RewardPlayerAction());
	}


	/**
	 * constructor
	 * @param timings
	 * - a pair of time parameters for phase timeout
	 */
	public OutgoingPhase(final Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(OUTGOING_TIME_MIN);
		maxPhaseChangeTime = timings.get(OUTGOING_TIME_MAX);
		addConversations();
		monsters=RAQuestHelperFunctions.getMonsters();
	}

	@Override
	public void prepare() {
		monsters.clear();
		createKnight();
	}

	/**
	 * summon new rat, attracted by piper
	 */
	public void SummonMonster() {
		final ActorNPC newCreature = new ActorNPC(false);

		// playing role of creature
		final Creature model = RAQuestHelperFunctions.getRandomMonster();
		newCreature.setRPClass("creature");
		newCreature.put("type", "creature");
		newCreature.put("title_type", "enemy");
		newCreature.setEntityClass(model.get("class"));
		newCreature.setEntitySubclass(model.get("subclass"));
		newCreature.setName("attracted "+model.getName());
		newCreature.setDescription(model.getDescription());

		// make actor follower of piper
		newCreature.setResistance(0);
		newCreature.setPosition(knight.getX(), knight.getY());
		knight.getZone().add(newCreature);

		logger.debug("monster summoned");
	}

	/**
	 * class for adding a random rat to a chain
	 * when piper staying near house's door
	 */
	class AttractMonster implements Observer {

		@Override
		public void update(Observable arg0, Object arg1) {
			SummonMonster();
		}

	}

	/**
	 * class for processing rats' outgoing to catacombs
	 */
	class RoadsEnd implements Observer {

		final Observer o;

		@Override
		public void update(Observable arg0, Object arg1) {
			logger.debug("road's end.");
			o.update(null, null);

		}

		public RoadsEnd(Observer o) {
			this.o = o;
		}

	}

	/**
	 * prepare NPC to walk through his multizone path.
	 */
	private void leadNPC() {
		final StendhalRPZone zone = fullpath.get(0).get(0).get().first();
		final int x=fullpath.get(0).get(0).getPath().get(0).getX();
		final int y=fullpath.get(0).get(0).getPath().get(0).getY();
		knight.setPosition(x, y);
		zone.add(knight);
		Observer o = new MultiZonesFixedPathsList(
						knight,
						fullpath,
						new AttractMonster(),
						new RoadsEnd(
								new PhaseSwitcher(this)));
		o.update(null, null);
	}

	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}


	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}


	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		destroyKnight();
		super.phaseToDefaultPhase(comments);
	}


	@Override
	public void phaseToNextPhase(IRAQuest nextPhase, List<String> comments) {
		destroyKnight();
		super.phaseToNextPhase(nextPhase, comments);
	}


	/*
	 *  Pied Piper sent rats away:-)
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
				"Gazda Wojtek oświadcza: Na szczęście, wszyscy #zbójnicy zostali wygnani z krainy podhalańskiej! "
				+ "Rycerze z Zakonu Cienia wykonali świętną robotę dając im kolejną nauczkę. "
				+ "Ci, którzy pomogli rycerzom z problem bandy rozbójników "
				+ "mogą otrzymać teraz swoją #nagrodę.";

		return text;
	}

	@Override
	public RA_Phase getPhase() {
		return RA_Phase.RA_OUTGOING;
	}

	/**
	 * function for creating pied piper npc
	 */
	private void createKnight() {
		RAQuestHelperFunctions.setupKnight(knight);
		fullpath = PathsBuildHelper.getZakopaneCollectingMonstersPaths();
		leadNPC();
	}

	/**
	 * function will remove piped piper npc object
	 */
	private void destroyKnight() {
		knight.getZone().remove(knight);
	}

}
