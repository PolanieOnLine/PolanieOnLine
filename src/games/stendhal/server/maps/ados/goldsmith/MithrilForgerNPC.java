/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

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
			protected void createDialog() {
				addJob("Wykuwam mithril. Mam na myśli za pomocą magii. Joshua udostępnił mi trochę miejsca do pracy pomimo tego, że jestem inny od innych w Ados.");
				addHelp("Jeżeli przyszedłeś tutaj po sztabki złota to musisz porozmawiać z Joshua. Odlewam rzadki i drogocenny #mithril w sztabkach. Powiedz tylko #odlej.");
				addGoodbye("Do widzenia.");

				addReply("polano",
		        		"Potrzebuję drewna do ognia. Mam nadzieję, że pozbierałeś w lesie, a nie zachowałeś się jak barbarzyńca i zabiłeś drzewce.");
				addReply(Arrays.asList("mithril ore", "mithril nugget","bryłka mithrilu"),
				        "W dzisiejszych czasach samorodki można znaleźć tylko w górach Ados oraz kopalni w Zakopanem. Nie mam pojęcia czy ten obszar jest ucywilizowany...");
				addReply(Arrays.asList("mithril bar", "mithril", "bar","sztabkę mithrilu"),
				        "Mithril jest bardzo cennym towarem. Służy do produkcji niesamowicie mocnych zbroi. Pilnuj każdej grutki mithrilu.");
			}
		};

		forger.setDescription("Oto Pedinghaus. Wygląda na to że zna się na magi...");
		forger.setEntityClass("mithrilforgernpc");
		forger.setGender("M");
		forger.setDirection(Direction.RIGHT);
		forger.setPosition(10, 12);
		zone.add(forger);
	}
}
