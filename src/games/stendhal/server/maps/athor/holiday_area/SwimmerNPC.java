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
package games.stendhal.server.maps.athor.holiday_area;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SwimmerNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Enrique") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(67,68));
				nodes.add(new Node(67,63));
				setPath(new FixedPath(nodes, true));
			}
			
			@Override
			public void createDialog() {
				addGreeting("Nie przeszkadzaj mi. Próbuję pobić rekord!");
				addQuest("Nie mam zadań dla Ciebie. Jestem zajęty.");
				addJob("Jestem pływakiem!");
				addHelp("Spróbuj trampoliny! Zabaw się!");
				addGoodbye("Dowidzenia!");
			}

		};
		npc.setPosition(67, 63);
		npc.setEntityClass("swimmer3npc");
		npc.setDescription ("Oto Enrique pływa dookoła basenu jakby chciał ustanowić rekord.");
		zone.add(npc);		
	}
}
