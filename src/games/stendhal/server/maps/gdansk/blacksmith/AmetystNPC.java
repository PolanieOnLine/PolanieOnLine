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
package games.stendhal.server.maps.gdansk.blacksmith;

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
 * @author KarajuSs
 */
public class AmetystNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("czeladnik Wojtek") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 11));
				nodes.add(new Node(4, 13));
				nodes.add(new Node(2, 13));
				nodes.add(new Node(2, 10));
				nodes.add(new Node(4, 10));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem czeladnikiem. Zajmuję się oszlifowaniem #ametystu.");
				addOffer("Mam obrobić dla Ciebie kryształ #ametystu?");
				addReply("ametyst",
					"Zrobię to bez problemu. Proszę powiedzieć tylko #'oszlifuj ametyst'.");
				addReply("kryształ ametystu",
					"Cenny kryształ ametystu możesz znaleźć w podziemiach zakopane jak i również w kopalni kościeliska.");
				addGoodbye("Do widzenia.");
			}
		};

		npc.setDescription("Oto Wojtek. Wygląda na porządnego czeladnika.");
		npc.setEntityClass("weaponsellernpc");
		npc.setGender("M");
		npc.setPosition(4, 11);
		npc.setDirection(Direction.RIGHT);
		zone.add(npc);
	}
}
