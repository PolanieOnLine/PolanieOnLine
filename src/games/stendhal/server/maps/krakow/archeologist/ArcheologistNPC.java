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
package games.stendhal.server.maps.krakow.archeologist;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class ArcheologistNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Amileusz") {

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem archeologiem i zbieram różne cenne znaleziska.");
				addHelp("Oczywiście, że możesz mi pomóc. Powiedz mi tylko #'zadanie'.");
				addOffer("Na chwilę obecną nie mam nic Tobie do zaoferowania!");
				addGoodbye();
			}
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto archeolog Amileusz ubrany w swój ulubiony stary szlafrok.");
		npc.setEntityClass("noimagenpc"); // npcarchelogist
		npc.setPosition(10, 6);
		npc.setDirection(Direction.UP);
		zone.add(npc);
	}
}