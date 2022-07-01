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
package games.pol.server.maps.krakow.wawel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

/**
 * A young lady (original name: Straznik) who heals players without charge.
 */
public class StraznikNPC implements ZoneConfigurator {
	private final List<String> regions = Arrays.asList(Region.KRAKOW_CITY);

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildguardian(zone);
	}

	private void buildguardian(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Strażnik") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Prowadzę spokojne życie. Pilnuje #wejścia na wawel. Mogę również Tobie przekazać od samego Króla Kraka kto potrzebuje #pomocy.");
				new SellerAdder().addSeller(this, new SellerBehaviour(SingletonRepository.getShopList().get("wawel")));
				addGoodbye();

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.QUEST_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));
				// if the player says an NPC name, describe the quest (same description as in the travel log)
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
			}
		};

		npc.setDescription("Oto Strażnik. Chroni bramę Wawela przed słabymi i niegodnymi wojownikami.");
		npc.setEntityClass("transparentnpc");
		npc.setAlternativeImage("guardian");
		npc.setGender("M");
		npc.setPosition(102, 82);
		npc.put("no_shadow", "");
		zone.add(npc);
	}
}
