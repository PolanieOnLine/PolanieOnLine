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
package games.stendhal.server.maps.semos.city;

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
 *
 */
public class OracleNPC implements ZoneConfigurator {

	/**
	 * region that this NPC can give information about
	 */
	private final List<String> regions = Arrays.asList(Region.SEMOS_CITY, Region.SEMOS_SURROUNDS);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Periwinkle") {

			@Override
			public void createDialog() {
				addGreeting("Róże są czerwone, a fiołki niebieskie. Semos potrzebuje #pomocy co możesz zrobic?");

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));

				// if the player says an NPC name, describe the quest (same description as in the travel log)
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));

				addQuest("Jest wielu innych, którzy mogą potrzebować #pomocy w " + Grammar.enumerateCollection(regions) + ". Nie poprosiłabym o nic nowego.");
				addJob("Nie mam prawdziwego zawodu. Moje umiejętności to pilnowanie ciebie w #pomocy innym, a szczególnie w " + Grammar.enumerateCollection(regions) + ".");
				addOffer("*chichot* Niczego nie sprzedaję. Mogę ci opowiedzieć o moich #siostrach i omoim #imieniu jeśli chcesz.");
				addReply(Arrays.asList("sisters", "siostrach", "siostry"), "Moje siostry żyją w innych miastach. Znajdź je, aby dowiedzieć się jak #pomagać osobom w ich regionie.");
				addReply(Arrays.asList("name", "imieniu"), "Ja i moje #siostry mamy imiona wzięte od kwiatów. " +
						"Nazywam się Periwinkle imię pochodzi od niezapominajki. Nie zapomnij mnie...");

				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("Dziękuję, że się zatrzymałeś.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 29));
				nodes.add(new Node(2, 31));
				nodes.add(new Node(9, 31));
				nodes.add(new Node(9, 32));
				nodes.add(new Node(5, 32));
				nodes.add(new Node(5, 33));
				nodes.add(new Node(3, 33));
				nodes.add(new Node(3, 32));
				nodes.add(new Node(2, 32));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(2, 29);
		npc.setDescription("Oto Periwinkle. Wygląda na senną i roztargnioną.");
		npc.setEntityClass("oracle1npc");
		zone.add(npc);
	}

}
