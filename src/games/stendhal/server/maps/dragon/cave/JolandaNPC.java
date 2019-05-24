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
// Based on ../games/stendhal/server/maps/ados/entwives/EntwifeNPC.java
package games.stendhal.server.maps.dragon.cave;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * entwife located in 0_ados_mountain_n2_w2.
 */
public class JolandaNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildjolanda(zone);
	}

	private void buildjolanda(final StendhalRPZone zone) {
		final SpeakerNPC jolanda = new SpeakerNPC("Jolanda") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj dzielny i przystojny wojowniku.");
				addHelp("Pomóż mi. Zabij tego wstrętnego smoka, a #'złota klinga' będzie twoja!");
				addReply("złota klinga", "Napadł mnie ten wstrętny smok i połknął mi ją. Jeżeli go pokonasz to w nagrodę będziesz mógł ją sobie zatrzymać.");
				addOffer("Mam dla Ciebie #zadanie.");
				addQuest("Szukałam rzadkiego ziela do mikstury, ale smok zablokował mi drogę do niego. Mam wielką prośbę do Ciebie, abyś zabił smoka, który zagrodził mi drogę. Jeżeli podejmiesz się tego to powiedz #zabiję.");
				addReply("zabiję", "Wspaniale! Idź i rozpraw się z nim. Tę wpaniałą #broń, którą zdobędziesz możesz zatrzymać jako nagrodę.");
				addReply("broń", "Zaatakował mnie i połknął ją");
				addGoodbye("Niech moc będzie z tobą mój wybawco.");
			}
		};

		jolanda.setEntityClass("elegantladynpc");
		jolanda.setPosition(124, 106);
		jolanda.initHP(100); 
		jolanda.setDescription("Oto Jolanda. Podczas podróży została zaatakowana przez smoka.");
		zone.add(jolanda);
	}
}
