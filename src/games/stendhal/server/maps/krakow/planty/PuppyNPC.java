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
package games.stendhal.server.maps.krakow.planty;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SilentNPC;
import games.stendhal.server.entity.npc.behaviour.impl.idle.WanderIdleBehaviour;

/**
 * A playful puppy
 *
 * @author AntumDeluge
 */
public class PuppyNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		List<SilentNPC> dogs = new LinkedList<SilentNPC>();

		final SilentNPC d1 = new Puppy();
        d1.setPosition(90, 40);
        dogs.add(d1);

        final SilentNPC d2 = new Puppy();
        d2.setPosition(27, 78);
        dogs.add(d2);

        final SilentNPC d3 = new Puppy();
        d3.setPosition(80, 107);
        dogs.add(d3);

		for (SilentNPC dog : dogs) {
			dog.put("menu", "Pogłaskaj|Użyj");
			// Not visible, but used for the emote action
			dog.setName("Szczeniaczek");
			dog.setDescription("Widzisz zabawnego szczeniaczka.");
			dog.setEntityClass("animal/puppy");
			dog.setBaseSpeed(0.5);
			dog.setIdleBehaviour(new WanderIdleBehaviour(25));
			dog.setSounds(Arrays.asList("dog-small-bark-1", "dog-small-bark-2"));
			zone.add(dog);
		}
	}

	/**
	 * A puppy that can be petted.
	 */
	private static class Puppy extends SilentNPC implements UseListener {
		@Override
		public boolean onUsed(RPEntity user) {
			if (nextTo(user)) {
				say("!me macha ogonkiem.");
				return true;
			}
			return false;
		}
	}
}
