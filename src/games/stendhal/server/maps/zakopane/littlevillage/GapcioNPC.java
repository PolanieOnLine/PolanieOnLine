/***************************************************************************
 *                 (C) Copyright 2021-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.littlevillage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class GapcioNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gapcio") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(97, 123));
				nodes.add(new Node(100, 123));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witamy!! Dawno dużych ludzi nie widzieliśmy w naszej wiosce.");
				addJob("Jestem tutaj, aby skupować towary na deszczowy dzień.");
				addHelp("Skupuję kilka rzeczy. Poczytaj znak, aby dowiedzieć się czego potrzebujemy.");
				addOffer("Poczytaj znak, aby dowiedzieć się czego potrzebujemy.");
				addQuest("Dziękuję za pytanie, ale czuję się dobrze.");
				addGoodbye("Do widzenia. Cieszę się, że odwiedziłeś nas.");
			}
		};

		npc.setDescription("Oto Gapcio, mały uroczy skrzat. Czeka na klientów.");
		npc.setEntityClass("npcskrzat");
		npc.setGender("M");
		npc.setPosition(97, 123);
		zone.add(npc);
	}
}
