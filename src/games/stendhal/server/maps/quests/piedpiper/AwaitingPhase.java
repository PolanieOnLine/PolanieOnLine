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
package games.stendhal.server.maps.quests.piedpiper;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.GoToPosition;
import games.stendhal.server.core.pathfinder.RPZonePath;
import games.stendhal.server.core.pathfinder.MultiZonesFixedPath;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.interaction.NPCChatting;
import games.stendhal.server.entity.npc.interaction.NPCFollowing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 * Implementation of Pied Piper's initial actions (coming, chatting, going to work place)
 * @author yoriy
 */
public class AwaitingPhase extends TPPQuest {
	
	private SpeakerNPC piedpiper;	
	private final SpeakerNPC mainNPC = TPPQuestHelperFunctions.getMainNPC();
	private final int minPhaseChangeTime;
	private int maxPhaseChangeTime;
	private List<RPZonePath> fullpathin = 
		new LinkedList<RPZonePath>();
	private List<RPZonePath> fullpathout = 
			new LinkedList<RPZonePath>();
	private final List<String> conversations = new LinkedList<String>();
	private final String explainations = 
			"Widzę, że jest tutaj nasz miejski wybawca. Muszę z tobą szybko porozmawiać. "+
			"Proszę porozmawiaj ze mną ponownie, gdy skończymy rozmowę.";
	
	
	/**
	 * adds quest's related conversations to mayor
	 */
	private void addConversations() {
		TPP_Phase myphase = AWAITING;
		
		// Player asking about rats.
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("rats", "rats!", "szczurów", "szczurów!"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				"Próbowaliśmy oczyścić miasto. "+
	    		"Możesz teraz odebrać #nagrodę za pomoc. Zapytaj o #szczegóły "+
				  "jeżeli chcesz wiedzieć więcej.", 
				null);
		
		// Player asking about details.
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("details", "szczegóły"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				null, 
				new DetailsKillingsAction());
		
		// Player asked about reward
		mainNPC.add(
				ConversationStates.ATTENDING, 
				Arrays.asList("reward", "nagroda","nagrodę"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING, 
				null, 
				new RewardPlayerAction());
	}


	/**
	 * fills conversations list between mayor and piper
	 */
		private void fillConversations() {
			//piper
			conversations.add("Dzień dobry, Burmistrzu. Dlaczego wzywasz mnie tym razem?");
			//mayor
			conversations.add("Hej, bardzo mi miło widzieć tutaj naszego szacownego bohatera. Któż nie słyszał o Tobie, jest prawie...");
			//piper
			conversations.add("Proszę, mów o sprawie, jaką dla mnie masz. Mój czas jest bardzo cenny.");
			//mayor
			conversations.add("... dobrze, o czym to ja mówiłem? Ah, dobrze - nasze miasto ma problem ze #szczurami.");
			//piper
			conversations.add("Znów?");
			//mayor
			conversations.add("Tak, te zwierzęta są zbyt głupie, by zapamiętać lekcję jaką dostały ostatnim razem.");
			//piper
			conversations.add("Mogę Ci pomóc, jeśli jesteś gotów mi zapłacić.");
			//mayor
			conversations.add("Miasto Ados nie ma innego wyjścia, by pozbyć się tych zwierząt. Zapłacimy Ci.");
			//piper
			conversations.add("Czy wiesz, jak wysoko cenione są moje umiejętności?");
			//mayor
			conversations.add("Tak, mam to w moich dokumentach.");
			//piper
			conversations.add("Dobrze. Wrócę po moją nagrodę, proszę, przygotuj się na to.");
			//mayor
			conversations.add("Nie przejmuj się, jak mogę zawieść Twoje zaufanie do mnie i mojego miasta?");
		}
		
	
		/**
		 * constructor
	 * @param timings - a pair of time parameters for phase timeouts
		 */
	public AwaitingPhase(final Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(AWAITING_TIME_MIN);
		maxPhaseChangeTime = timings.get(AWAITING_TIME_MAX);
		addConversations();
			fillConversations();
		}
		
	
	/**
	 * prepare actions
	 */
	@Override
	public void prepare() {
		createPiedPiper();
			}
	
	
	/**
	 * function for creating pied piper npc
	 */
	private void createPiedPiper() {
		piedpiper = new SpeakerNPC("Pied Piper");
		TPPQuestHelperFunctions.setupPiper(piedpiper);
		fullpathin = PathsBuildHelper.getAdosIncomingPath();
		fullpathout = PathsBuildHelper.getAdosTownHallBackwardPath();
		leadNPC();
		}
	
	
	/**
	 * function will remove piped piper npc object
	 */
	private void destroyPiedPiper() {
		piedpiper.getZone().remove(piedpiper);
		piedpiper = null;
	}	
	
	/**
	 * prepare NPC to walk through his multizone pathes and do some actions during that.
	 */
	private void leadNPC() {
		final StendhalRPZone zone = fullpathin.get(0).get().first();
		final int x=fullpathin.get(0).get().second().get(0).getX();
		final int y=fullpathin.get(0).get().second().get(0).getY();
		piedpiper.setPosition(x, y);
		zone.add(piedpiper);
		Observer o = new MultiZonesFixedPath(piedpiper, fullpathin, 
						new NPCFollowing(mainNPC, piedpiper,
							new NPCChatting(piedpiper, mainNPC, conversations, explainations,
								new GoToPosition(piedpiper, PathsBuildHelper.getAdosTownHallMiddlePoint(),
								new MultiZonesFixedPath(piedpiper, fullpathout, 
										new PhaseSwitcher(this))))));
		o.update(null, null);
	}

	
	/**
	 * @return - min quest phase timeout
	 */
	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}
	

	/**
	 * @return - max quest phase timeout
	 */
	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}


	/**
	 * @param comments - comments for switching event
	 */
	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		destroyPiedPiper();
		super.phaseToDefaultPhase(comments);
	}


	/**
	 * @param nextPhase - next phase
	 * @param comments - comments for switching event
	 */
	@Override
	public void phaseToNextPhase(ITPPQuest nextPhase, List<String> comments) {
		destroyPiedPiper();
		super.phaseToNextPhase(nextPhase, comments);
	}
	
	 
	/**
	 *  Pied Piper will now start to collect rats :-)
	 *  @return - npc shouts at switching quest phase.
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text = 
			/*
			"Pied Piper shouts: Ados citizens, now i will clean up your city from rats. I will play " +
			"magical melody, and rats will attracts to me. Please do not try to block or kill them, " +
			"because melody will also protect MY rats. " +
			"Just enjoy the show.";
			 */
			"Burmistrz Chalmers oświadcza: Na szczęście, wszystkie #szczuy zostały wygnane, " +
			"Pied Piper zahipnotyzował je i zaprowadził‚ do podziemi. "+
			"Ci, którzy pomogli Ados City z problem szczurów "+
			"mogą dostać teraz swoją #nagrodę.";

		return text;
	}

	
	/**
	 * @return - current phase
	 */
	@Override
	public TPP_Phase getPhase() {
		return TPP_Phase.TPP_AWAITING;
	}		
	
}

