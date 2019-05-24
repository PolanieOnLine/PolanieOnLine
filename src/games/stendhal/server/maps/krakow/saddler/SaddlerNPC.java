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
package games.stendhal.server.maps.krakow.saddler;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * The miller (original name: Jenny). She mills flour for players who bring
 * grain. 
 */
public class SaddlerNPC extends SpeakerNPCFactory {

	@Override
	public void createDialog(final SpeakerNPC npc) {
		npc.addJob("Zajmuję się wyprawianiem skór. Przynieś mi skórę zwierzęcą, a uszyję Tobie bukłak na wodę.");
		npc.addReply("skóra zwierzęca",
					"Polując na różne zwierzęta w końcu ją zdobędziesz.");
		npc.addHelp("Wyprawiam skóry i szyje bukłaki. Powiedz tylko #'uszyj pusty bukłak'.");
		npc.addGoodbye("Dowidzenia");

		// Jenny mills flour if you bring her grain.
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("skóra zwierzęca", 1);
		requiredResources.put("money", 20);

		final ProducerBehaviour behaviour = new ProducerBehaviour("rymarz_make_buklak",
				Arrays.asList("sew", "uszyj"), "pusty bukłak", requiredResources, 1 * 5);

		new ProducerAdder().addProducer(npc, behaviour,
				"Pozdrawiam! Jeżeli przyniesiesz mi #skórę zwierzęcą to uszyję Tobie bukłak na wodę. Powiedz tylko #'uszyj pusty bukłak'.");
	}
}
