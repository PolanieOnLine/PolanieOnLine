/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.blacksmith;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;

/**
 * The blacksmith's young assistant (original name: Hackim Easso).
 * He smuggles out weapons.
 * 
 * @see games.stendhal.server.maps.quests.MeetHackim
 */
public class BlacksmithAssistantNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Hackim Easso") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(5,2));
                nodes.add(new Node(5,5));
                nodes.add(new Node(10,5));
                nodes.add(new Node(10,9));
                nodes.add(new Node(7,9));
                nodes.add(new Node(7,12));
                nodes.add(new Node(3,12));
                nodes.add(new Node(3,8));
                nodes.add(new Node(9,8));
                nodes.add(new Node(9,5));
                nodes.add(new Node(12,5));
                nodes.add(new Node(12,2));
                nodes.add(new Node(15,2));
                nodes.add(new Node(15,5));
                nodes.add(new Node(5,5));
				setPath(new FixedPath(nodes, true));
			}

	@Override
			public void createDialog() {
				
				// A little trick to make NPC remember if it has met
		        // player before and react accordingly
		        // NPC_name quest doesn't exist anywhere else neither is
		        // used for any other purpose
				add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_hackim")),
						ConversationStates.ATTENDING,
						"Witaj nieznajomy. Zwą mnie Hackim Easso i jestem czeladnikiem kowala. Czy przybyłeś tu by kupić broń?",
				        new SetQuestAction("meet_hackim","start"));
				
				addGreeting(null, new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc?"));

				addHelp("Jestem czeladnikiem kowala. Powiedz mi... Czy przybyłeś tu by kupić broń?");
				addJob("Pomagam kowalowi Xoderosowi kuć broń i zbroje dla armii Denirana. Głównie dorzucam węgiel do pieca i układam broń na półkach. Czasami gdy Xoderos nie widzi lubię fechtować najlepszymi mieczami udając, iż jestem najsłynniejszym rycerzem!");
				addOffer("Możesz zapytać Xoderos. On sprzedaje jakąś broń własnej produkcji.");
				addGoodbye();
			}

		};
		npc.setPosition(5, 2);
		npc.setEntityClass("naughtyteennpc");
		npc.setDescription("Oto Hackim Easso, pomocnik kowala.");
		zone.add(npc);		
	}
}

		
