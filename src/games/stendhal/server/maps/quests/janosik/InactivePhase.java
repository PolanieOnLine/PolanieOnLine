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
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class InactivePhase extends RAQuest {

	private final int minPhaseChangeTime;
	private final int maxPhaseChangeTime;

	private void addConversations(final SpeakerNPC mainNPC) {
		RA_Phase myphase = INACTIVE;

		// Player asking about rats
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("zbójnicy", "zbójników", "zbójników!"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"Zakopane nie jest teraz nawiedzane przez bandę zbójników. Wciąż masz możliwość "
				+ "odebrania #nagrody za ostatnią pomoc. Możesz zapytać o #szczegóły "
				+ "jeśli chcesz.",
				null);

		// Player asking about details
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("details", "szczegóły"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new DetailsKillingsAction());

		// Player asked about reward
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("reward", "nagroda", "nagrodę", "nagrody"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new RewardPlayerAction());
	}

	/**
	 * constructor
	 * @param timings
	 */
	public InactivePhase(Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime=timings.get(INACTIVE_TIME_MIN);
		maxPhaseChangeTime=timings.get(INACTIVE_TIME_MAX);
		addConversations(RAQuestHelperFunctions.getMainNPC());
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
		// not used
	}

	@Override
	public void prepare() {

	}


	@Override
	public RA_Phase getPhase() {
		return RA_Phase.RA_INACTIVE;
	}

}
