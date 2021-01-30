/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.wieliczka.scientisthouse;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * @author zekkeq
 */
public class NaukowiecNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Agrypin") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			public void createDialog() {
				addJob("Jestem naukowcem i udało mi się rozpracować #'smoczy eliksir'!");
				addReply(Arrays.asList("smoczy eliksir", "smoczy", "potion", "dragon potion"),
				"Powiedz mi tylko #'sporządź 1 smoczy eliksir'.");
				addReply(Arrays.asList("krew", "krew smoka", "dragon blood", "blood"),
				"Krew smoka możesz uzyskać tylko z najsilniejszych smoków... Powiedz mi tylko #'sporządź 1 smoczy eliksir'.");
				addReply("eliksiru",
		        "Dobrze słyszałeś... Wynalazłem cudowną recepture, która nawet zmarłego podniesie! #'Krew smoka' daje ludzkości niesamowite efekty zdrowotne.");
				addOffer("Mogę sporządzić dla Ciebie #'smoczy eliksir'. Do tego będę potrzebował #'krew smoka' oraz trochę pieniędzy! Powiedz mi tylko #sporządź.");
				addHelp("Jeżeli chcesz być mądry tak jak ja to powinieneś odwiedzić bibliotekę. Tam jest sporo pomocy naukowych.");
				addGoodbye();

				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 800);
				requiredResources.put("smocza krew", 1);
				final ProducerBehaviour behaviour = new ProducerBehaviour("agrypin_concoct_potion",
						Arrays.asList("concoct", "sporządź"), "smoczy eliksir", requiredResources, 1 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj rycerzu, nie chciałbyś czasem spróbować mojego nowego #'eliksiru'?");
			}
		};

		npc.setDescription("Oto Agrypin. Wygląda jakby coś rozpracował.");
		npc.setEntityClass("madscientistnpc");
		npc.setPosition(6, 4);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
