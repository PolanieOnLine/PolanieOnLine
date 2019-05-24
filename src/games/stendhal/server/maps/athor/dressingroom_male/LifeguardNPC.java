/* $Id$ */
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
package games.stendhal.server.maps.athor.dressingroom_male;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Dressing rooms at the Athor island beach (Inside / Level 0).
 *
 * @author daniel
 */

public class LifeguardNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildDressingRoom(zone);
	}

	private void buildDressingRoom(final StendhalRPZone zone) {
		final SpeakerNPC david = new SpeakerNPC("David") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			public void createDialog() {
				addJob("Jestem jednym z ratowników na tej plaży. Jak widzisz zajmuję się też męską przebieralnią. Mogę też zrobić #'olejek do opalania'.");
				addHelp("Powiedz #pożycz jeżeli chciałbyś pożyczyć kąpielówki ( #trunks )!");
				addGoodbye("Miłej zabawy!");

				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("trunks", 5);
				final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList);
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, Arrays.asList("borrow", "pożycz"));

				// stuff needed for the SuntanCreamForZara quest
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("arandula", 1);
				requiredResources.put("kokuda", 1);
				requiredResources.put("mały eliksir", 1);

				final ProducerBehaviour mixerBehaviour = new ProducerBehaviour("david_mix_cream",
						 Arrays.asList("mix", "zrób"), "olejek do opalania", requiredResources, 10 * 60);

				new ProducerAdder().addProducer(this, mixerBehaviour, "Hallo!");

				addReply(
						Arrays.asList("suntan", "cream", "suntan cream","olejek do opalania"),
				"Olejek do opalania Pam i mój słynny jest na całą wyspę, ale że wejście do labiryntu jest zablokowane to nie możemy zdobyć wszystkich składników. Jeżeli przyniesiesz mi składniki to mogę zrobić dla Ciebie nasz specjalny krem do opalania. Powiedz tylko #zrób.");

				addReply("arandula", "Arandula jest ziołem rosnącym w okolicach Semos.");

				addReply(
		        		"kokuda",
		        "Nie możemy zdobyć Kokudy, która rośnie na wyspie, ponieważ wejście do labiryntu gdzie można znaleźć to zioło jest zablokowane.");

				addReply("mały eliksir", "Jest to mała buteleczka wypełniona miksturą. Możesz ją kupić w kilku miejscach.");

			}
	};

		david.setEntityClass("lifeguardmalenpc");
		david.setDirection(Direction.RIGHT);
		david.setPosition(3, 11);
		david.initHP(100);
		david.setDescription("Oto David. Jest brązowy od siedzenia na plaży.");
		zone.add(david);
	}
}
