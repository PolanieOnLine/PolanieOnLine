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
 * Inside Ados Abandoned Keep - level -4 .
 */
public class DwarfWeaponArmorGuyNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildgulimo(zone);
	}

	private void buildgulimo(final StendhalRPZone zone) {
		final SpeakerNPC gulimo = new SpeakerNPC("Gulimo") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 24));
				nodes.add(new Node(3, 27));
				nodes.add(new Node(11, 27));
				nodes.add(new Node(11, 24));
				nodes.add(new Node(19, 24));
				nodes.add(new Node(19, 27));
				nodes.add(new Node(11, 27));
				nodes.add(new Node(11, 24));
				setPath(new FixedPath(nodes, true));
			}
	
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Sprzedaję dobrej jakości zbroję i broń.");
				addHelp("Spójrz na tablicę i zobacz co mogę zaoferować.");
				addOffer("Spójrz na tablicę, aby zapoznać się z moją ofertą.");
				addQuest("Dziękuję za zaoferowanie pomocy, ale wszystko w porządku.");
				addGoodbye();
			}
		};

		gulimo.setEntityClass("greendwarfnpc");
		gulimo.setDescription("Oto Gulimo. Sprzedaje dobrej jakości zbroje i broń.");
		gulimo.setGender("M");
		gulimo.setPosition(3, 24);
		zone.add(gulimo);
	}
}
