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
package games.stendhal.server.maps.semos.library;

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

public class HistorianGeographerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosLibraryArea(zone);
	}

	private void buildSemosLibraryArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Zynn Iwuhos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(12, 3));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(13, 6));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 6));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(15, 7));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(17, 6));
				nodes.add(new Node(17, 7));
				nodes.add(new Node(17, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new SayTextAction("Witaj ponownie [name]. W czym mogę Ci #pomóc tym razem?"));
				addGoodbye();

						        // A little trick to make NPC remember if it has met
		        // player before and react accordingly
						        // NPC_name quest doesn't exist anywhere else neither is
						        // used for any other purpose
				add(ConversationStates.IDLE, 
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotCompletedCondition("Zynn")), 
						ConversationStates.ATTENDING, 
						"Witaj czytelniku! Znajdziesz tutaj zapisy z historii Semos i wiele innych interesujących faktów o wyspie Faiumoni. Jeżeli chcesz mogę udzielić szybkiego wstępu do geografii i historii! Mam też najświeższe wiadomości.",
						new SetQuestAction("Zynn", "done"));

				addHelp("Mogę pomóc, dzieląc się z tobą moją wiedzą o geografii i historii Faiumoni, a także najnowszymi wiadomościami.");
				addJob("Jestem historykiem i geografem oddanym spisywaniu każdego wydarzenia w Faiumoni. Czy wiesz, że napisałem większość książek w tej bibliotece? Cóż oprócz \"Jak Zabijać Potwory\", oczywiście autorstwa... Hayunn Naratha.");

				addQuest("Nie sądzę, aby było coś co mógłbyś dla mnie zrobić, ale dziękuję, że pytałeś!");

				add(ConversationStates.ATTENDING, Arrays.asList("offer", "buy", "trade", "deal","zwój", "scroll", "scrolls", "semos", "empty",
				        "marked", "summon", "magic", "wizard", "sorcerer", "oferta", "kupię"), null, ConversationStates.ATTENDING,
				        "Nie sprzedaję już zwojów... Pokłóciłem się z moim dostawcą co zwie się #Haizen.", null);

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("haizen", "haizen."),
				        null,
				        ConversationStates.ATTENDING,
				        "Haizen? Jest czarodziejem, który żyje w małym domku między Semos a Ados. Powinienem sprzedawać jego bilety podróżne, ale pokłóciliśmy się... Chyba będziesz musiał się z nim spotkać osobiście.",
				        null);
			}
		};

		npc.setEntityClass("wisemannpc");
		npc.setDescription("Oto Zynn Iwuhos. Wygląda na starszego niż mapy leżące tutaj wokoło.");
		npc.setPosition(15, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
