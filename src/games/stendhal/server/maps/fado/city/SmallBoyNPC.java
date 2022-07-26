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
package games.stendhal.server.maps.fado.city;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Creates a Small Boy NPC
 *
 * @author jackrabbit
 */
public class SmallBoyNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSmallBoyNPC(zone);
	}

	private void buildSmallBoyNPC(final StendhalRPZone zone) {
		final SpeakerNPC boynpc = new SpeakerNPC("Bobby") {
			@Override
			protected void createDialog() {
				addGreeting("Hm?");
				addHelp("Zastanawiam się czy dzięki #balonikowi będę mógł się wznieść wystarczająco wysoko, aby dotknąć chmur...");
				addJob("Praca? Czy to coś takiego co mogę zjeść?");
				addReply("balonik", "Pewnego dnia będę miał tyle baloników, że będę mógł odlecieć daleko!");
				addReply(Arrays.asList("xkcd", "tables", "sql", "student", "drop", "table"), 
				        "Oh tak moje prawdziwe imię to Robert'); DROP TABLE students;--, ale możesz mnie nazywać Bobby.");
				addGoodbye("Do widzenia.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		boynpc.setDescription("Oto Bobby. Patrzy w niebo i wygląda na marzyciela.");
		boynpc.setOutfit(3, 3, 3, null, 4, null, 7, 1, 0);
		boynpc.setGender("M");
		boynpc.setPosition(42, 30);
		boynpc.setDirection(Direction.RIGHT);
		zone.add(boynpc);
	}
}
