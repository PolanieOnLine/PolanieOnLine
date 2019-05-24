/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tatry.kuznice.townhall;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

public class FinarfinNPC implements ZoneConfigurator {

	private final List<String> regions = Arrays.asList(Region.TATRY_MOUNTAIN, Region.ZAKOPANE_CITY);

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Finarfin") {

			@Override
			public void createDialog() {
				addGreeting("Witaj, nasz sołtys kazał mi powiedzieć kto aktualnie w naszych regionach potrzebuje Twojej #'pomocy'.");

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));

				// if the player says an NPC name, describe the quest (same description as in the travel log)
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));

				addQuest("Jest wielu innych, którzy mogą potrzebować #pomocy w " + Grammar.enumerateCollection(regions) + ".");
				addJob("Zajmuje się papierkową robotą tutaj. Możesz #pomóc innym, a szczególnie " + Grammar.enumerateCollection(regions) + ".");
				addOffer("Niczego nie sprzedaję.");
				addGoodbye("Dziękuję, że się zatrzymałeś.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setEntityClass("executivenpc");
		npc.setPosition(27, 6);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
