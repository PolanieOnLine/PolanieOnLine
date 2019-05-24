/* $Id: PietrekNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
 //Na podstawie pliku RetiredAdventurerNPC z Semos/guardhouse
 
package games.stendhal.server.maps.zakopane.home;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;


/**
 * An old hero (original name: Hayunn Naratha) who players meet when they enter the semos guard house.
 *
 * @see games.stendhal.server.maps.quests.BeerForHayunn
 * @see games.stendhal.server.maps.quests.MeetHayunn
 */
public class PietrekNPC implements ZoneConfigurator {
	private final String QUEST_SLOT="meet_pietrek";

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Pietrek") {

			@Override
			public void createDialog() {
				// A little trick to make NPC remember if it has met
				// player before and react accordingly
				// NPC_name quest doesn't exist anywhere else neither is
				// used for any other purpose

				final List<ChatAction> actions = new LinkedList<ChatAction>();
				actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
				actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, "szczur", 0, 1));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new QuestNotStartedCondition(QUEST_SLOT),
						ConversationStates.ATTENDING,
						"Witaj! Założę się, że przysłano Cię tutaj byś dowiedział się nieco o przygodach jakie Cię tu spotkają. Najpierw zobaczmy z jakiej gliny jesteś ulepiony. Idź i zabij szczura wałęsającego się gdzieś na zewnątrz. Czy chcesz się nauczyć jak atakować?",
						new MultipleActions(actions));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new QuestCompletedCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
						"Witaj ponownie. Jakiej #pomocy mogę Ci teraz udzielić?",
						null);

				addHelp("Jak już mówiłem, byłem kiedyś poszukiwaczem przygód, a teraz jestem nauczycielem. Chcesz bym nauczył Cię co sam potrafię?");
				addJob("Moją pracą jest ochrona ludzi w Zakopanem przed potworami wałęsającymi się gdzieś w okolicy! Od kiedy młodzi wyruszyli w świat to potwory stały się zuchwalsze i zaczęły się niebezpiecznie zbliżać się do miasta. Zakopane oczekuje pomocy właśnie od ludzi takich jak ty. Idź do gazdy Wojtka i zapytaj o zadanie, z pewnością ma jakieś dla Ciebie.");
				addGoodbye();
				// further behaviour is defined in quests.
			}

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 3));
				nodes.add(new Node(28, 3));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setPosition(22, 3);
		npc.setEntityClass("oldheronpc");
		npc.setDescription("Oto Pietrek. Poniżej jego siwych włosów, a brudną zbroją widzisz świecące oczy i twarde mięśnie.");
		npc.setBaseHP(100);
		npc.setHP(100);
		zone.add(npc);
	}
}
