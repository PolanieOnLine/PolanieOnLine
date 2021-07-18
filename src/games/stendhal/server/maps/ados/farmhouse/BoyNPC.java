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
package games.stendhal.server.maps.ados.farmhouse;

import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * Boy NPC
 *
 * @author kymara
 */
public class BoyNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Hughie") {
			@Override
			protected void createDialog() {
				// boy says nothing at all
				// mother gets stressed
				addGreeting(null,new ChatAction(){
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final NPCList npcs = SingletonRepository.getNPCList();
						final SpeakerNPC npcAnastasia = npcs.get("Anastasia");
						if (npcAnastasia != null) {
							npcAnastasia.say("Ciii! Proszę nie obudź go!");
						}
						raiser.setCurrentState(ConversationStates.IDLE);
					}
				});
			}
		};

		npc.setDescription("Oto mały chłopiec o imieniu Hughie, który wygląda na jakby miał gorączkę.");
		npc.setEntityClass("kid8npc");
		npc.setGender("M");
		npc.setPosition(33, 6);
	    zone.add(npc);
	}
}
