/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.tavern;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Food and drink seller, Inside Zakopane Tavern - Level 0 (ground floor)
 *
 * @author Legolas
 */
public class JagnaNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildJagna(zone);
	}

	private void buildJagna(final StendhalRPZone zone) {
		final SpeakerNPC jagna = new SpeakerNPC("Jagna") {
			@Override
			protected void createDialog() {
				addGreeting("Witam miłego gościa. Czy coś podać?");
				addJob("Jestem kelnerką w tej karczmie. Sprzedajemy importowane i lokalne trunki oraz dobre jedzenie. Na deser też coś się znajdzie.");
				addHelp("Karczma ta jest znana w całym Zakopanem. Można tu odpocząć i dobrze zjeść. Jeżeli chcesz poznać naszą #ofertę, to powiedz mi o tym.");
				addGoodbye("Smacznego i miłej zabawy do samego rana.");
			}
		};

		jagna.setDescription("Oto Jagna, wydaje się być bardzo miłą osobą.");
		jagna.setEntityClass("hotelreceptionistnpc");
		jagna.setGender("F");
		jagna.setPosition(25, 3);
		jagna.setDirection(Direction.DOWN);
		jagna.setSounds(Arrays.asList("hiccup-01"));
		zone.add(jagna);
	}
}
