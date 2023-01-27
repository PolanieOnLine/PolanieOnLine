/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.city.market;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author zekkeq
 */
public class GroceryNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Hermina") {
			@Override
			protected void createDialog() {
				addGreeting("Hej! W czym mogę pomóc?");
				addJob("Prowadzę sklepik spożywczy na tym rynku!");
				addHelp("Możesz mi pomóc kupując ode mnie rzeczy!");
				addOffer("Spójrz na tablicę, by zobaczyć co sprzedaję. Dla ciebie mam specjalną ofertę!");
				addQuest("Nie mam zadania dla Ciebie.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Hermina. Prowadzi mały sklepik spożywczy.");
		npc.setEntityClass("woman_004_npc");
		npc.setGender("F");
		npc.setPosition(39, 74);
		zone.add(npc);
	}
}
