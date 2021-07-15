/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.warszawa.blacksmith;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.behaviour.adder.ImproverAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ImproverAdder.ImproverNPC;

/**
 * @author KarajuSs
 */
public class BlacksmithNPC implements ZoneConfigurator {
	private static final String npcName = "Kowal Tworzymir";

	private StendhalRPZone zone;

	private ImproverNPC improver;
	private ImproverAdder improverAdder;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		this.zone = zone;

		initNPC();
		initImprove();
	}

	private void initNPC() {
		improverAdder = new ImproverAdder();

		improver = improverAdder.new ImproverNPC(npcName) {
			@Override
			public void say(final String text) {
				// don't turn toward player
				say(text, false);
			}
		};

		improver.setDescription("Oto " + npcName + ". Potrafi udoskonalać różne wyposażenie.");
		improver.setEntityClass("blacksmithnpc");
		improver.setIdleDirection(Direction.DOWN);

		improver.addGreeting();
		improver.addGoodbye();

		improver.addJob("Udoskonalam jakość wyposażenia, dzięki czemu jest wytrzymalsze!");
		improver.addOffer("Jeśli chcesz #sprawdzić ile jestem w stanie #ulepszyć dany przedmiot, no to się zapytaj!");
		improver.addQuest("Nie mam zadania dla Ciebie, ale mogę #ulepszyć wyposażenie.");
		improver.addHelp("Nie potrzebuję pomocy, lecz możesz poprosić mnie o ulepszenie swojego wyposażenia.");

		improver.addReply("sprawdzić", "Spytaj się mnie #sprawdź <#'nazwa przedmiotu'>, aby dowiedzieć się ile maksymalnie byłbym w stanie go #ulepszyć!");
		improver.addReply("ulepszyć", "Powiedz mi #ulepsz <#'nazwa przedmiotu'>, abym wiedział jaki przedmiot chcesz udoskonalić!");

		improver.setGender("M");
		improver.setPosition(10, 4);
		zone.add(improver);
	}

	private void initImprove() {
		improverAdder.add(improver);
	}
}
