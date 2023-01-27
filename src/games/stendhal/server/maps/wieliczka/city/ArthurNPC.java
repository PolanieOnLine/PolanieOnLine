/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.wieliczka.city;

import java.util.Arrays;
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
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;

/**
 * @author zekkeq
 */
public class ArthurNPC implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Arthur") {

			@Override
			public void createDialog() {
				addGreeting(null,new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc tym razem?"));

				add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(getName()),
						new QuestNotCompletedCondition("Arthur")), 
					ConversationStates.ATTENDING, 
					"Witaj nieznajomy! Mam pewne informacje o jakimś pierścieniu. Chciałbyś usłyszeć?",
					new SetQuestAction("Arthur", "done"));

				add(ConversationStates.ATTENDING,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Miejscowy #kowal może wytworzyć jakiś pierścionek. Nie jestem pewien z jakiego materiału jest tworzony, ale możesz się do niego przejść i zapytać.",
					null);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.IDLE,
					"To w takim razie informacje zachowam dla siebie.",
                    null);

                add(ConversationStates.ATTENDING,
    				"kowal",
    				null,
    				ConversationStates.ATTENDING,
    				"Daj mi chwilę się zastanowić... Nie mam pojęcia gdzie ma swą kuźnię, ponieważ nigdy tam nie przechodziłem. Podejrzewam, że gdzieś na północny-zachód stąd, jedynie co słyszałem to od burmistrza co mówił, że w tamtych rejonach słychać jakieś stukające młoty, gdzieś w środku lasu.",
    				null);

                add(ConversationStates.ATTENDING,
    				Arrays.asList("zwój wieliczka", "wieliczka"),
    				null,
    				ConversationStates.ATTENDING,
    				"Może zabrać Cię do krainy soli.",
    				null);

                add(ConversationStates.ATTENDING,
        			"niezapisany zwój",
        			null,
        			ConversationStates.ATTENDING,
        			"Możesz zapisać swą aktualną pozycję w tym małym magicznym zwoju oraz do niej natychmiast wrócić.",
        			null);

				addHelp("Mogę Ci opowiedzieć o pewnych informacjach o niejakim pierścionku. Chcesz je usłyszeć?");
				addJob("Mam jedynie informacje do przekazania.");
				addQuest("Dzięki, że pytasz, ale w tej chwili nie mam żadnego zadania dla Ciebie.");
                addOffer("Mój przyjaciel #Herbert potrzebuje pomocy, siedzi na ławce. Mam jeszcze do zaoferowania #'zwój wieliczka' oraz #'niezapisany zwój' w dobrej cenie.");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(60, 72));
				nodes.add(new Node(60, 74));
				nodes.add(new Node(59, 74));
				nodes.add(new Node(59, 76));
				nodes.add(new Node(58, 76));
				nodes.add(new Node(58, 78));
				nodes.add(new Node(55, 78));
				nodes.add(new Node(55, 79));
				nodes.add(new Node(53, 79));
				nodes.add(new Node(53, 80));
				nodes.add(new Node(51, 80));
				nodes.add(new Node(53, 80));
				nodes.add(new Node(53, 79));
				nodes.add(new Node(55, 79));
				nodes.add(new Node(55, 78));
				nodes.add(new Node(58, 78));
				nodes.add(new Node(58, 76));
				nodes.add(new Node(59, 76));
				nodes.add(new Node(59, 74));
				nodes.add(new Node(60, 74));
				nodes.add(new Node(60, 72));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setDescription("Oto Arthur. Wygląda na przyjaznego faceta.");
		npc.setEntityClass("icecreamsellernpc");
		npc.setGender("M");
		npc.setPosition(60, 72);
		zone.add(npc);
	}
}
