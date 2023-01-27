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
package games.stendhal.server.maps.ados.abandonedkeep;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Inside Ados Abandoned Keep - level -1 .
 */
public class OrcWeaponArmorGuyNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHagnurk(zone);
	}

	private void buildHagnurk(final StendhalRPZone zone) {
		final SpeakerNPC hagnurk = new SpeakerNPC("Hagnurk") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(104, 3));
				nodes.add(new Node(109, 3));
				nodes.add(new Node(109, 8));
				nodes.add(new Node(108, 8));
				nodes.add(new Node(108, 10));
				nodes.add(new Node(109, 10));
				nodes.add(new Node(109, 13));
				nodes.add(new Node(104, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem handlarzem, a Ty kim jesteś?");
				addHelp("Sprzedaję przedmioty, spójrz na tablicę na ścianie.");
				addOffer("Spójrz na tablicę na ścianie, aby zapoznać się z moją ofertą.");
				addQuest("Jestem szczęśliwy. Nie potrzebuję niczego.");
				addGoodbye();
			}
		};

		hagnurk.setDescription("Oto Hagnurk. Zajmuje się handlem u orków.");
		hagnurk.setEntityClass("orcsalesmannpc");
		hagnurk.setGender("M");
		hagnurk.setPosition(106, 5);
		zone.add(hagnurk);
	}
}
