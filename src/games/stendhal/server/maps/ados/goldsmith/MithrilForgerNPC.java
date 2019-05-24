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
package games.stendhal.server.maps.ados.goldsmith;

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
 * Ados MithrilForger (Inside / Level 0).
 *
 * @author kymara
 */
public class MithrilForgerNPC implements ZoneConfigurator {
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
		final SpeakerNPC forger = new SpeakerNPC("Pedinghaus") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam.");
				addJob("Wykuwam mithril. Mam na myśli za pomocą magii. Joshua udostępnił mi trochę miejsca do pracy pomimo tego, że jestem inny od innych w Ados.");
				addHelp("Jeżeli przyszedłeś tutaj po sztabki złota to musisz porozmawiać z Joshua. Odlewam rzadki i drogocenny #mithril w szatbkach. Powiedz tylko #odlej.");
				addGoodbye("Dowidzenia.");

				// Pedinghaus makes mithril if you bring him mithril nugget and wood
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	
				requiredResources.put("polano", 20);
				requiredResources.put("bryłka mithrilu", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("Pedinghaus_cast_mithril",
						Arrays.asList("cast", "odlej"), "sztabka mithrilu", requiredResources, 18 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Pozdrawiam. Sądzę, że jesteś zainteresowany mithrilem. Jeżeli chcesz, abym odlał #'sztabkę mithrilu' to daj znać. Powiedz tylko #odlej");
				addReply("polano",
		        		"Potrzebuję drewna do ognia. Mam nadzieję, że pozbierałeś w lesie, a nie zachowałeś się jak barbarzyńca i zabiłeś drzewce.");
				addReply(Arrays.asList("mithril ore", "mithril nugget","bryłka mithrilu"),
				        "W dzisiejszych czasach samorodki można znaleźć tylko w górach Ados oraz kopalni w Zakopanem. Nie mam pojęcia czy ten obszar jest ucywilizowany...");
				addReply(Arrays.asList("mithril bar", "mithril", "bar","sztabkę mithrilu"),
				        "Mithril jest bardzo cennym towarem. Służy do produkcji niesamowicie mocnych zbroi. Pilnuj każdej grutki mithrilu.");
			}
		};

		forger.setEntityClass("mithrilforgernpc");
		forger.setDirection(Direction.RIGHT);
		forger.setPosition(10, 12);
		forger.initHP(100);
		forger.setDescription("Oto Pedinghaus. Wygląda na to że zna się na magi...");
		zone.add(forger);
	}
}
