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
package games.stendhal.server.maps.semos.city;

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
 * A guy (original name: Nomyr Ahba) who looks into the windows of the bakery
 * and the house next to it.
 * 
 * Basically all he does is sending players to the retired adventurer at
 * the dungeon entrance. 
 */
public class GossipNPC implements ZoneConfigurator {
	
    @Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Nomyr Ahba") {
			
			@Override
			public void createDialog() {
				addGreeting(null,new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc tym razem?"));

				// A little trick to make NPC remember if it has met
		        // player before and react accordingly
				// NPC_name quest doesn't exist anywhere else neither is
				// used for any other purpose
				add(ConversationStates.IDLE, 
						ConversationPhrases.GREETING_MESSAGES,
                        new AndCondition(new GreetingMatchesNameCondition(getName()),
                        		new QuestNotCompletedCondition("Nomyr")), 
						ConversationStates.INFORMATION_1, 
						"Heh heh... O witaj nieznajomy! Wyglądasz na zdezorientowanego... chcesz usłyszeć najnowszą plotkę?",
						new SetQuestAction("Nomyr", "done"));

				add(ConversationStates.ATTENDING,
						ConversationPhrases.YES_MESSAGES,
						null,
						ConversationStates.INFORMATION_1,
						"Młodzi ludzie wstąpili do Wojsk Imperialnych Deniranu na południu i dlatego miasto jest niestrzeżone przed hordami potworów z podziemi. Czy możesz nam pomóc?",
						null);

				add(ConversationStates.ATTENDING,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.IDLE,
						"Czy mógłbyś mi pomóc i zerknąć przez tamto okno o ile nie jesteś zajęty?... Chcę sprawdzić co się tam dzieje.",
                    null);

                add(ConversationStates.ATTENDING,
                    Arrays.asList("sack", "worek"),
                    null,
                    ConversationStates.ATTENDING,
                    "Nie jesteś zbyt ciekawy o zawartość mojego worka. Możesz zapytać Karla o swój, aby zapełnić go czym chcesz. Nawet sugar!",
                    null);

                add(ConversationStates.ATTENDING,
                    "karl",
                    null,
                    ConversationStates.ATTENDING,
                    "Oh. Jest spokojnym rolnikiem, który mieszka na farmie trochę na wschód stąd. Idź drogą do Ados, a znajdziesz go.",
						null);

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						null,
						ConversationStates.IDLE,
						"Po pierwsze powinieneś porozmawiać z Hayunn Naratha. Jest on wielkim starym bohaterem oraz jest naszym obrońcą... Jestem pewien, że da Ci jakieś dobre rady! Powodzenia.",
						null);

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.IDLE,
						"Oj... więc jesteś tchórzem? Eh.",
						null);

				addHelp("Jestem... powiedzmy \"obserwatorem\". Mogę Ci opowiedzieć najnowsze plotki. Chcesz je usłyszeć?");
				addJob("Znam każdą plotkę w Semos i wymyśliłem większość z nich! Jedna jest o Hackimie przemycającym broń dla wędrujących łowców przygód takich jak ty, to prawda.");
				addQuest("Dzięki, że pytasz, ale w tej chwili nie mam żadnego zadania dla Ciebie.");
                addOffer("Tz także trzymam ten #worek ze sobą. Nie oznacza to, że mam coś dla Ciebie...Ale słyszałem, że stary Monogenes potrzebuje jakiejś pomocy...");
				addGoodbye();
	}


			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(46, 20));
				nodes.add(new Node(46, 21));
				nodes.add(new Node(50, 21));
				nodes.add(new Node(50, 20));
				nodes.add(new Node(46, 21));
				setPath(new FixedPath(nodes, true));
			}
		
		};
		npc.setPosition(46, 20);
		npc.setEntityClass("thiefnpc");
		zone.add(npc);
		npc.setDescription("Ten facet tutaj, Nomyr Ahba. Wydaje się być ciekawy. Jego ogromny worek maskuje go.");
	}

}
