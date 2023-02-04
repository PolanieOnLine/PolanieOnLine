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
package games.stendhal.server.maps.krakow.church;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class HonoratNPC implements ZoneConfigurator {
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

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ojciec Honorat") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 5));
				nodes.add(new Node(14, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Zajmuję się sporządzaniem cudownego #'eliksiru'!");
				addHelp("Mogę sporządzić dla Ciebie wyjątkowy #'eliksir'. Powiedz mi tylko #'sporządź duży eliksir'.");
				addOffer("Mógłbym dla Ciebie sporządzić ten wyśmienity #'eliksir'. Powiedz mi tylko #'sporządź duży eliksir'.");
				addReply("eliskir", "Ten cudowny eliksir potrafi wyleczyć w okamgnieniu nawet bardzo rozległe rany!");
				addGoodbye("Niech Bóg Cię prowadzi!");
			}
		};

		npc.setDescription("Oto Ojciec Honorat. Jest mnichem i od lat zgłębia tajniki leczenia.");
		npc.setEntityClass("npcwikary");
		npc.setGender("M");
		npc.setPosition(14, 5);
		zone.add(npc);
	}
}
