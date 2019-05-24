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
package games.stendhal.server.maps.koscielisko.jeweller;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
/*
 * Twilight zone is a copy of sewing room in dirty colours with a delirious sick lda (like Ida) in it
 */
public class CarbuncleNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZiemirad(zone);
	}

	private void buildZiemirad(final StendhalRPZone zone) {
		final SpeakerNPC ziemirad = new SpeakerNPC("czeladnik Ziemirad") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Dzień dobry.");
				addReply("mistrz",
						"Mistrz przysłał Wielmożność do mnie w sprawie rubinu? Obrobię go, wystarczy powiedzieć #'oszlifuj rubin'.");
				addGoodbye("Dowidzenia.");
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 580);
				requiredResources.put("kryształ rubinu", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
					"ziemirad_cast_crbuncle", Arrays.asList("grind", "oszlifuj"), "rubin",
					requiredResources, 4 * 60);

				new ProducerAdder().addProducer(this, behaviour,
						"Dzień dobry.");
				addReply("money",
						"Mistrz jest w tym temacie obeznany. Proszę jego zapytać.");
			}
		};

		ziemirad.setEntityClass("weaponsellernpc");
		ziemirad.setPosition(4, 17);
		ziemirad.setDirection(Direction.RIGHT);
		ziemirad.initHP(100);
		zone.add(ziemirad);
	}
}
