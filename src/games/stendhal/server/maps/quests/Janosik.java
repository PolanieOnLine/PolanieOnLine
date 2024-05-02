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
package games.stendhal.server.maps.quests;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.janosik.AwaitingPhase;
import games.stendhal.server.maps.quests.janosik.IRAQuest;
import games.stendhal.server.maps.quests.janosik.IRAQuestConstants;
import games.stendhal.server.maps.quests.janosik.InactivePhase;
import games.stendhal.server.maps.quests.janosik.InvasionPhase;
import games.stendhal.server.maps.quests.janosik.RAQuestHelperFunctions;

public class Janosik extends AbstractQuest implements IRAQuestConstants {
	protected static final Logger logger = Logger.getLogger(Janosik.class);

	private static LinkedList<IRAQuest> phases = new LinkedList<IRAQuest>();
    private static RA_Phase phase = INACTIVE;

	protected LinkedHashMap<String, Integer> timings = new LinkedHashMap<String, Integer>();

	/**
	 * function will set timings to either test server or game server.
	 */
	private void setTimings() {
		if (System.getProperty("stendhal.testserver.janosik") == null) {
			// game timings */
			timings.put(INACTIVE_TIME_MAX, 60 * 60 * 24 * 21);
			timings.put(INACTIVE_TIME_MIN, 60 * 60 * 24 * 2);
			timings.put(INVASION_TIME_MAX, 60 * 60 * 2);
			timings.put(INVASION_TIME_MIN, 60 * 60 * 2);
			timings.put(AWAITING_TIME_MAX, 60 * -1);
			timings.put(AWAITING_TIME_MIN, 60 * -1);
			timings.put(OUTGOING_TIME_MAX, 60 * -1);
			timings.put(OUTGOING_TIME_MIN, 60 * -1);
			timings.put(FINAL_TIME_MAX, 60 * 60 * 6);
			timings.put(FINAL_TIME_MIN, 60 * 60 * 4);
			timings.put(SHOUT_TIME, 60 * 10);
			}
		else {
			// test timings
			timings.put(INACTIVE_TIME_MAX, 60 * 2);
			timings.put(INACTIVE_TIME_MIN, 60 * 1);
			timings.put(INVASION_TIME_MAX, 60 * 20);
			timings.put(INVASION_TIME_MIN, 60 * 20);
			timings.put(AWAITING_TIME_MAX, 60 * -1);
			timings.put(AWAITING_TIME_MIN, 60 * -1);
			timings.put(OUTGOING_TIME_MAX, 60 * -1);
			timings.put(OUTGOING_TIME_MIN, 60 * -1);
			timings.put(FINAL_TIME_MAX, 60 * 60 * 6);
			timings.put(FINAL_TIME_MIN, 60 * 60 * 4);
			timings.put(SHOUT_TIME, 60 * 2);
			}
	}

    /**
     *
     * @param ph
     * @return phase index
     */
    public static int getPhaseIndex(RA_Phase ph) {
    	for (int i=0; i<getPhases().size(); i++) {
    		if(getPhases().get(i).getPhase().compareTo(ph)==0) {
    			return i;
    		}
    	}
    	// didnt found it!
    	logger.warn("Using improper phase for quest ("+ph.name()+"). size: "+getPhases().size());
		return -1;
    }

    /**
     * return next available quest phase
     * @param ph -
     * @return next phase
     */
    public static RA_Phase getNextPhase(RA_Phase ph) {
    	int pos=getPhaseIndex(ph);
    	if(pos!=getPhases().size()-1) {
		   return getPhases().get(pos+1).getPhase();
    	}
    	return getDefaultPhaseClass().getPhase();
    }

    /**
     * return next instance of quest phases classes from list
     * @param ph
     * @return next phase class
     */
    public static IRAQuest getNextPhaseClass(RA_Phase ph) {
		return getPhases().get(getPhaseIndex(getNextPhase(ph)));
    }

    /**
     * return instance of quest phases classes
     * @param ph
     * @return phase class
     */
    public static IRAQuest getPhaseClass(RA_Phase ph) {
    	/*
    	if(getPhaseIndex(ph)==-1) {
    		return getDefaultPhaseClass();
    	}
    	*/
    	return getPhases().get(getPhaseIndex(ph));
    }

    /**
     * function return default phase class
     * @return default phase class
     */
    public static IRAQuest getDefaultPhaseClass() {
    	return getPhases().get(getPhaseIndex(INACTIVE));
    }

	/**
	 * switching quest to next available phase.
	 */
	public static void switchToNextPhase() {
	getPhaseClass(
				getPhase()).phaseToNextPhase(
						getNextPhaseClass(
								getPhase()), new LinkedList<String>());
	}

	private static QuestTimer questTimer;
	/**
	 * Timings logic of quest.
	 */
	private static final class QuestTimer implements TurnListener {
		@Override
		public void onTurnReached(final int currentTurn) {
			final IRAQuest i = getPhaseClass(getPhase());
			i.phaseToNextPhase(getNextPhaseClass(getPhase()),
					new LinkedList<String>(Arrays.asList("janosik")));
		}
	}


	/**
	 * Set new time period for quest timer (time to next quest phase).
	 * @param max - maximal time in seconds
	 * @param min - minimal time in seconds
	 */
	public static void setNewNotificationTime(int max, int min) {
		TurnNotifier.get().dontNotify(questTimer);
		if(max >= 0 && min >= 0) {
			TurnNotifier.get().notifyInSeconds(
					Rand.randUniform(max, min),	questTimer);
		}
	}


	/**
	 * first start
	 */
	private void startQuest() {
		setTimings();
		getPhases().add(new InactivePhase(timings));
		getPhases().add(new InvasionPhase(timings));
		getPhases().add(new AwaitingPhase(timings));
		//getPhases().add(new OutgoingPhase(timings));
		setNewNotificationTime(
				getDefaultPhaseClass().getMinTimeOut(),
				getDefaultPhaseClass().getMaxTimeOut());
	}

 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

 	@Override
 	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("monsters".equals(questState)) {
			history.add("Pozbyłem się kilku zbójników napadających Zakopane i postaram się pozbyć ich więcej.");
		}
		if ("done".equals(questState)) {
			history.add("Pozbyłem się kilku zbójników napadających Zakopane i " + player.getGenderVerb("otrzymałem") + " nagrodę od Gazdy Wojtka!");
		}
		return history;
 	}

 	@Override
	public String getName() {
		return "Janosik";
	}

	@Override
	public void addToWorld() {
		questTimer = new QuestTimer();
		fillQuestInfo(
				"Janosik",
				"Okresowo banda rozbójników napada obszar podhalański, by zrabować cenne przedmioty.",
				true);

		startQuest();
	}

	public static void setPhase(RA_Phase phase) {
		Janosik.phase = phase;
	}

	public static RA_Phase getPhase() {
		return phase;
	}

	public static void setPhases(LinkedList<IRAQuest> phases) {
		Janosik.phases = phases;
	}

	public static LinkedList<IRAQuest> getPhases() {
		return phases;
	}

	public int getRemainingTurns() {
		return TurnNotifier.get().getRemainingTurns(questTimer);
	}

	public int getRemainingSeconds() {
		return TurnNotifier.get().getRemainingSeconds(questTimer);
	}

	@Override
	public String getNPCName() {
		return RAQuestHelperFunctions.MAIN_NPC_NAME;
	}
}
