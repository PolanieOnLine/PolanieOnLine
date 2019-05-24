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
package games.stendhal.server.maps.kalavan.citygardens;

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
	private final List<String> regions = Arrays.asList(Region.KALAVAN, Region.KIRDNEH, Region.FADO_CITY, Region.FADO_CAVES);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Lobelia") {
			@Override
			public void createDialog() {
				addGreeting("Cześć. Zastałeś mnie przy podziwianiu kwiatków.");
				
				// use a standard action to list the names of NPCs for quests which haven't been started in this region 
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));
				
				// if the player says an NPC name, describe the quest (same description as in the travel log)
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
				addQuest("Są przyjaciele w " + Grammar.enumerateCollection(regions) + " którzy mogą potrzebować twojej #pomocy.");
				addJob("Czasami kwiaty tutaj wydają się piękne, ale tak naprawdę to jest dzieło wspaniałej ogrodniczki Sue.");
				addOffer("Lubię moje #siostry w innych rejonach. Jestem tutaj, aby pokazać Tobie jak #pomóc innym.");
				addReply(Arrays.asList("sisters", "siostry"), "Moje siostry i ja mamy na #imiona kwiatów. Odszukaj je i naucz się jak #pomóc innym w ich rejonie.");
				addReply(Arrays.asList("name", "imiona"), "Lobelia są to małe kwiatki. Być może widziałeś ich rabatki. Kocham to miejsce, a Sue jest taka mądra.");
				
				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("Dziękuję miło było cię poznać.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 111));
				nodes.add(new Node(58, 111));
				nodes.add(new Node(58, 109));
				nodes.add(new Node(61, 109));
				nodes.add(new Node(61, 100));
				nodes.add(new Node(56, 100));
				nodes.add(new Node(56, 101));
				nodes.add(new Node(54, 101));
				nodes.add(new Node(54, 105));
				nodes.add(new Node(27, 105));
				nodes.add(new Node(27, 107));
				nodes.add(new Node(22, 111));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(22,111);
		npc.setDescription("Oto Lobelia. Patrzy uważnie na rabatki wokół niej.");
		npc.setEntityClass("oracle4npc");
		zone.add(npc);
	}

}