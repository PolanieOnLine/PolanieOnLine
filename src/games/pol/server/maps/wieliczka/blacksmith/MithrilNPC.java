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
package games.pol.server.maps.wieliczka.blacksmith;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zekkeq
 */
public class MithrilNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildForger(zone);
	}

	private void buildForger(final StendhalRPZone zone) {
		final SpeakerNPC forger = new SpeakerNPC("Larrangin") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Tworzę jedne z najlepszych strzał w całej krainie. Wykorzystuję magię do wytworzenia strzał z mithrilu.");
				addHelp("Jeżeli jesteś zainteresowany strzałami z mithrilu to wystarczy mi powiedzieć #'zrób strzała z mithrilu'.");
				addGoodbye();

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 4);
				requiredResources.put("bryłka mithrilu", 2);
				requiredResources.put("piórko", 2);
				requiredResources.put("money", 8);

				final ProducerBehaviour behaviour = new ProducerBehaviour("larrangin_make_mithril_arrow",
						Arrays.asList("make", "zrób"), "strzała z mithrilu", requiredResources, 4 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Pozdrawiam. Sądzę, że jesteś zainteresowany strzałami z mithrilu. Jeżeli chcesz, abym wytworzył #'strzałę z mithrilu' to daj znać. Powiedz tylko #'zrób'.");
				addReply("polano",
						"Potrzebuję drewna na promień do strzały. Porozmawiaj z drwalem on ci powie gdzie można ścinać drzewa.");
				addReply("piórko",
						"Potrzebuję je na lotki. Zabij kilka gołębi.");
				addReply(Arrays.asList("mithril ore", "mithril nugget","bryłka mithrilu"),
				        "W dzisiejszych czasach samorodki można znaleźć tylko w górach Ados oraz kopalni w Zakopanem. Nie mam pojęcia czy ten obszar jest ucywilizowany...");
			}
		};

		forger.setEntityClass("mithrilforgernpc");
		forger.setDirection(Direction.DOWN);
		forger.setPosition(17, 6);
		forger.initHP(100);
		forger.setDescription("Oto Larrangin. Wygląda na to że zna się na magi...");
		zone.add(forger);
	}
}
