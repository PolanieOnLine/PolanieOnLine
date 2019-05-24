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
public class EmeraldNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMieszek(zone);
	}

	private void buildMieszek(final StendhalRPZone zone) {
		final SpeakerNPC mieszek = new SpeakerNPC("czeladnik Mieszek") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Dzień dobry.");
				addReply("mistrz",
						"Mistrz?? Tak, tak znam mistrza. Co potrzeba?");
				addReply("szmaragd",
						"Aaaa... mam obrobić emerald crystal. A może by tak ładnie poprosić #☺ coś w rodzaju #'oszlifuj szmaragd'.");    
				addGoodbye("Dowidzenia.");
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 180);
				requiredResources.put("kryształ szmaragdu", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
					"mieszek_cast_emerald", Arrays.asList("grind", "oszlifuj"), "szmaragd",
					requiredResources, 4 * 60);

				new ProducerAdder().addProducer(this, behaviour,
						"Dzień dobry.");
				addReply("money",
						"☺ Drogo? Nie drogo, mistrz Drogosz ma zamiar podnieść i tak cenę. Więc nie ma co się zastanawiać.");
			}
		};

		mieszek.setEntityClass("recruiter1npc");
		mieszek.setPosition(27, 29);
		mieszek.setDirection(Direction.LEFT);
		mieszek.initHP(100);
		zone.add(mieszek);
	}
}
