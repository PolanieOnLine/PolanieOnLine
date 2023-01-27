/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.tavern;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class MerchantNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("kupczyk Piotr") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj! Zechcesz spojrzeć na moje towary?");
				addJob("Zajmuję się handlem zagranicznym towarem.");
				addOffer("Spójrz na moje książki, które leżą na stoliku.");
				addGoodbye("Do widzenia. Mam nadzieję, że się jeszcze spotkamy!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto kupczyk Piotr. Jest wędrownym handlarzem, który najczęściej lubi się zatrzymywać na dłużej w pokoiku gospody na Krakowskim Rynku.");
		npc.setEntityClass("npcmerchant");
		npc.setGender("M");
		npc.setPosition(5, 20);
		zone.add(npc);
	}
}
