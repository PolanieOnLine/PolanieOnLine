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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.util.Area;

import java.util.Arrays;
import java.awt.Rectangle;
import java.util.Map;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class DiceDealerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildRicardo(zone);
	}

		final CroupierNPC ricardo = new CroupierNPC("Ricardo") {
			@Override
			protected void createPath() {
				// Ricardo doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam przy stole #hazardowym, gdzie marzenia stają się rzeczywistością.");
				addJob("Jestem jedyną osobą w Semos, która posiada licencję na prowadzenie hazardu.");
				addReply(
				        Arrays.asList("gambling", "hazardowym"),
				        "Zasady są proste. Powiedz mi jeżeli chciałbyś #zagrać, postaw pieniądze i rzuć kośćmi na stół. Suma wyrzuconych oczek daje nagrodę. Zobacz tablicę na ścianie!");
				addHelp("Jeżeli szukasz Ouchita to jest on na górze.");
				addGoodbye();
			}
		
		@Override
		protected void onGoodbye(RPEntity player) {
			setDirection(Direction.DOWN);
		}
		};

	private void buildRicardo(final StendhalRPZone zone) {
		ricardo.setEntityClass("naughtyteen2npc");
		ricardo.setPosition(26, 2);
		ricardo.setDirection(Direction.DOWN);
		ricardo.setDescription("Ricardo wygląda na zbyt młodą osobę , aby mieć swój własny interes w tawernie.");
		ricardo.initHP(100);
		final Rectangle tableArea = new Rectangle(25, 4, 2, 3);
		
		zone.add(ricardo);
		ricardo.setTableArea(tableArea);
	}

	/**
	 * Access the playing area for JUnit tests.
	 * @return playing area
	 */
	public Area getPlayingArea() {
		return ricardo.getPlayingArea();
	}
}
