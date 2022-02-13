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

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.GoToPosition;
import games.stendhal.server.core.pathfinder.MultiZonesFixedPath;
import games.stendhal.server.core.pathfinder.RPZonePath;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.interaction.NPCChatting;
import games.stendhal.server.entity.npc.interaction.NPCFollowing;
import games.stendhal.server.util.Observer;

/**
 * Implementation of Pied Piper's initial actions (coming, chatting, going to work place)
 * @author yoriy
 */
public class AwaitingPhase extends RAQuest {
	private SpeakerNPC knight;
	private final SpeakerNPC mainNPC = RAQuestHelperFunctions.getMainNPC();
	private final int minPhaseChangeTime;
	private int maxPhaseChangeTime;
	private List<RPZonePath> fullpathin =
		new LinkedList<RPZonePath>();
	private List<RPZonePath> fullpathout =
			new LinkedList<RPZonePath>();
	private final List<String> conversations = new LinkedList<String>();
	private final String explainations =
			"Widzę, że jest tutaj nasz wybawca. Muszę z nim szybko porozmawiać. "+
			"Proszę porozmawiaj ze mną ponownie później, gdy skończymy rozmowę.";

	/**
	 * adds quest's related conversations to mayor
	 */
	private void addConversations() {
		RA_Phase myphase = AWAITING;

		// Player asking about rats.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("zbójnicy", "zbójników", "zbójników!"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"Próbowaliśmy oczyścić miasto. "
				+ "Możesz teraz odebrać #nagrodę za pomoc. Zapytaj o #szczegóły "
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

		// Player asked about reward
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("reward", "nagroda", "nagrodę"),
				new RAQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new RewardPlayerAction());
	}

	/**
	 * fills conversations list between mayor and piper
	 */
	private void fillConversations() {
		//knight
		conversations.add("Dzień dobry, Burmistrzu. Dlaczego wzywasz mnie tym razem?");
		//mayor
		conversations.add("Hej, bardzo mi miło widzieć tutaj naszego szacownego bohatera. Któż nie słyszał o Tobie, jest prawie...");
		//knight
		conversations.add("Proszę, mów o sprawie, jaką dla mnie masz. Mój oraz gwardii czas jest bardzo cenny.");
		//mayor
		conversations.add("... dobrze, o czym to ja mówiłem? Ah, dobrze - nasze miasto ma problem ze #zbójnikami.");
		//knight
		conversations.add("Znów?");
		//mayor
		conversations.add("Tak, banda rozbójników nie chce się poddać...");
		//knight
		conversations.add("Moja gwardia może Ci pomóc, jeśli jesteś gotów nam zapłacić.");
		//mayor
		conversations.add("Miasto Zakopane nie ma innego wyjścia, by pozbyć się tej bandy. Zapłacimy Ci.");
		//knight
		conversations.add("Czy wiesz, jak wysoko cenione są nasze umiejętności?");
		//mayor
		conversations.add("Tak, mam to w moich dokumentach.");
		//knight
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
		createKnight();
	}

	/**
	 * function for creating pied piper npc
	 */
	private void createKnight() {
		knight = new SpeakerNPC("Rycerz");
		RAQuestHelperFunctions.setupKnight(knight);
		fullpathin = PathsBuildHelper.getZakopaneIncomingPath();
		fullpathout = PathsBuildHelper.getZakopaneTownHallBackwardPath();
		leadNPC();
	}

	/**
	 * function will remove piped piper npc object
	 */
	private void destroyKnight() {
		knight.getZone().remove(knight);
		knight = null;
	}

	/**
	 * prepare NPC to walk through his multizone pathes and do some actions during that.
	 */
	private void leadNPC() {
		final StendhalRPZone zone = fullpathin.get(0).get().first();
		final int x=fullpathin.get(0).get().second().get(0).getX();
		final int y=fullpathin.get(0).get().second().get(0).getY();
		knight.setPosition(x, y);
		zone.add(knight);
		Observer o = new MultiZonesFixedPath(knight, fullpathin,
						new NPCFollowing(mainNPC, knight,
							new NPCChatting(knight, mainNPC, conversations, explainations,
								new GoToPosition(knight, PathsBuildHelper.getZakopaneTownHallMiddlePoint(),
									new MultiZonesFixedPath(knight, fullpathout,
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
		destroyKnight();
		super.phaseToDefaultPhase(comments);
	}

	/**
	 * @param nextPhase - next phase
	 * @param comments - comments for switching event
	 */
	@Override
	public void phaseToNextPhase(IRAQuest nextPhase, List<String> comments) {
		destroyKnight();
		super.phaseToNextPhase(nextPhase, comments);
	}

	/**
	 *  Pied Piper will now start to collect rats :-)
	 *  @return - npc shouts at switching quest phase.
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
			"Gazda Wojtek oświadcza: Na szczęście, wszyscy #zbójnicy zostali wygnani z krainy podhalańskiej! " +
			"Rycerze z Zakonu Cienia wykonali świętną robotę dając im kolejną nauczkę. "+
			"Ci, którzy pomogli rycerzom z bandą rozbójników "+
			"mogą otrzymać teraz swoją #nagrodę.";

		return text;
	}

	/**
	 * @return - current phase
	 */
	@Override
	public RA_Phase getPhase() {
		return RA_Phase.RA_AWAITING;
	}
}
