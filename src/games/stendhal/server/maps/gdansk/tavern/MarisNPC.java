/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.tavern;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.util.Area;

public class MarisNPC implements ZoneConfigurator {
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

	final CroupierNPC npc = new CroupierNPC("Maris") {
		@Override
		protected void createDialog() {
			addGreeting("Witam przy stole #bilardowym, gdzie marzenia stają się rzeczywistością.");
			addJob("Jestem jedyną osobą w Gdańsku, która posiada licencję na prowadzenie hazardu.");
			addReply(Arrays.asList("gambling", "hazardowym", "bilardowym"),
			        "Zasady są proste. Powiedz mi jeżeli chciałbyś #zagrać, dostaniesz ode mnie białą bile oraz kładziesz nią na stole przy wyznaczonej lini. Jeżeli uda ci się zrobić #''golden break'' to możesz coś wygrać. Zobacz tablicę na ścianie!");
			addReply(Arrays.asList("golden break", "złote rozbicie"),
			        "W momencie kiedy uda Ci się zbić bilę o numerze #9 to nazywamy właśnie #'golden break'.");
			addReply(Arrays.asList("9 ball"),
			        "Gra #'9 ball' polega na rozbijaniu wszystkich bil po kolei, od 1 do 9. Można oczywiście zbić inną bile pod jednym warunkiem, jeżeli dotkniemy chociaż białą bilą w najmniejszy cyferek dostępny na stole. Osoba, która zbije jako pierwszy bilę o numerze 9, wygrywa.");
			addHelp("Jeżeli szukasz Harrisona to jest on po prawej stronie.");
			addGoodbye();
		}

		@Override
		protected void onGoodbye(RPEntity player) {
			setDirection(Direction.DOWN);
		}
	};

	private void buildNPC(final StendhalRPZone zone) {
		npc.setDescription("Oto Maris, wygląda na zbyt młodą osobę, aby mieć swój własny interes w tawernie.");
		npc.setEntityClass("naughtyteen2npc");
		npc.setGender("M");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(2, 3);
		npc.initHP(100);
		final Rectangle tableArea = new Rectangle(3, 6, 1, 2);

		zone.add(npc);
		npc.setTableArea(tableArea);
	}

	/**
	 * Access the playing area for JUnit tests.
	 * @return playing area
	 */
	public Area getPlayingArea() {
		return npc.getPlayingArea();
	}
}
