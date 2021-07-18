/***************************************************************************
 *                     (C) Copyright 2019 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.tinyhouse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A trapped wizard
 *
 * @author hendrik
 */
public class WizardNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Byron Mcgalister") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Witaj.");
				addJob("Robię teraz badania. Proszę, nie przeszkadzaj mi. Muszę się skoncentrować.");
				addGoodbye();

			}
		};

		npc.setDescription("Oto Byron Mcgalister. Przypomina wyglądem czarodzieja.");
		npc.setEntityClass("wizardnpc");
		npc.setGender("M");
		npc.setPosition(4, 2);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
