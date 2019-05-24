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
package games.stendhal.server.maps.semos.guardhouse;

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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.maps.quests.BeerForHayunn;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * An old hero (original name: Hayunn Naratha) who players meet when they enter the semos guard house.
 *
 * @see games.stendhal.server.maps.quests.BeerForHayunn
 * @see games.stendhal.server.maps.quests.MeetHayunn
 */
public class RetiredAdventurerNPC implements ZoneConfigurator {
	private static final String QUEST_SLOT="meet_hayunn";
	
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Hayunn Naratha") {
			
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
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
		        		"Witaj! Założę się, że przysłano Cię tutaj byś dowiedział się nieco o przygodach jakie Cię tu spotkają. Najpierw zobaczmy z jakiej gliny jesteś ulepiony. Idź i zabij szczura wałęsającego się gdzieś na zewnątrz. Czy chcesz się nauczyć jak atakować?",
				new MultipleActions(actions));

			   	add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestCompletedCondition(QUEST_SLOT),
								new NotCondition(new QuestActiveCondition(BeerForHayunn.QUEST_SLOT))),
						ConversationStates.ATTENDING,
						"Witaj ponownie. Jakiej #pomocy mogę Ci teraz udzielić?",
						null);
				 
				addHelp("Jak już mówiłem, byłem kiedyś poszukiwaczem przygód, a teraz jestem nauczycielem. Chcesz bym nauczył Cię co sam potrafię?");
				addJob("Moją pracą jest ochrona ludzi w Semos przed potworami, które mogą uciec z podziemi! Od kiedy młodzi ludzie wyruszyli na południe, aby walczyć ze złymi legionami Blordroughtów, potwory stały się pewniejsze i zaczęły wychodzić na powierzchnię. Semos oczekuje pomocy właśnie od ludzi takich jak ty. Idź do burmistrza i zapytaj o zadanie, z pewnością ma jakieś dla Ciebie.");
				addGoodbye();
		// further behaviour is defined in quests.
	}

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 9));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(6, 14));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(11, 9));
				setPath(new FixedPath(nodes, true));
			}
		
		};
		npc.setPosition(4, 9);
		npc.setEntityClass("oldheronpc");
		npc.setDescription("Oto Hayunn Naratha. Poniżej jego siwych włosów, a brudną zbroją widzisz świecące oczy i twarde mięśnie.");
		npc.setBaseHP(100);
		npc.setHP(85);
		zone.add(npc);
	}


}