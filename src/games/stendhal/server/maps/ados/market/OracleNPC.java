/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An oracle who lets players know how they can help others.
 */
public class OracleNPC implements ZoneConfigurator {
	
	/** 
	 * region that this NPC can give information about 
	 */
	private final List<String> regions = Arrays.asList(Region.ADOS_SURROUNDS, Region.ADOS_CITY);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Calla") {
			
			@Override
			public void createDialog() {
				addGreeting("Co za moc małego kwiatka! Jaką masz moc? Obywatele Ados szukają #pomocy...");
				
				// use a standard action to list the names of NPCs for quests which haven't been started in this region 
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));
				
				// if the player says an NPC name, describe the quest (same description as in the travel log)
				add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
				addQuest("Oh jest wielu innych w " + Grammar.enumerateCollection(regions) + ", którzy potrzebują #pomocy. Nie chciałabym zadać ci nic nowego.");
				addJob("Nie pracuję, ale mogę ci pokazać jak #pomóc innym szczególnie w " + Grammar.enumerateCollection(regions) + ".");
				addOffer("*chichocze* Nieczego nie sprzedaję. Mogę ci opowiedzieć o moich #siostrach lub o moim #imieniu jeśli chcesz.");
				addReply(Arrays.asList("sisters", "siostrach", "siostry"), "Moje siostry mieszkają w innych miastach. Znajdź je, aby dowiedzieć się jak #pomóc osobom w ich okolicy.");
				addReply(Arrays.asList("name", "imieniu"), "Ja i moje #siostry mamy imiona po kwiatach. " +
						"Nazywam się Calla i jest rodzaj lilli, która ma ten sam kolor co moja sukienka. Jest taka ładna.");
				
				// just to be nice :)
				addEmotionReply("hugs", "uściski");
				addGoodbye("Dziękuję. Miło byo cię poznać.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(62, 86));
				nodes.add(new Node(62, 87));
				nodes.add(new Node(64, 87));
				nodes.add(new Node(64, 88));
				nodes.add(new Node(66, 88));
				nodes.add(new Node(66, 89));
				nodes.add(new Node(67, 89));
				nodes.add(new Node(67, 92));
				nodes.add(new Node(66, 92));
				nodes.add(new Node(66, 95));
				nodes.add(new Node(64, 95));
				nodes.add(new Node(64, 97));
				nodes.add(new Node(63, 97));
				nodes.add(new Node(63, 99));
				nodes.add(new Node(56, 99));
				nodes.add(new Node(56, 101));
				nodes.add(new Node(52, 101));
				nodes.add(new Node(52, 100));
				nodes.add(new Node(51, 100));
				nodes.add(new Node(51, 99));
				nodes.add(new Node(50, 99));
				nodes.add(new Node(50, 98));
				nodes.add(new Node(49, 98));
				nodes.add(new Node(49, 96));
				nodes.add(new Node(50, 96));
				nodes.add(new Node(50, 91));
				nodes.add(new Node(54, 91));
				nodes.add(new Node(54, 90));
				nodes.add(new Node(56, 90));
				nodes.add(new Node(56, 88));
				nodes.add(new Node(58, 88));
				nodes.add(new Node(58, 87));
				nodes.add(new Node(60, 87));
				nodes.add(new Node(60, 86));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(62, 86);
		npc.setDescription("Oto Calla. Czuć od niej liliami.");
		npc.setEntityClass("oracle2npc");
		zone.add(npc);
	}

}