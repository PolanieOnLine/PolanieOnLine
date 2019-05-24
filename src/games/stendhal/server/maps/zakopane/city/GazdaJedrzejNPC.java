/* $Id: GazdaJedrzejNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
// based on ../games/stendhal/server/maps/ados/abandonedkeep/OrcKillGiantDwarfNPC.java
package games.stendhal.server.maps.zakopane.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the orc kill diant dwarf NPC.
 *
 * @author Teiv
 */
public class GazdaJedrzejNPC implements ZoneConfigurator {

	//
	// ZoneConfigurator
	//

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
		final SpeakerNPC gazdajedrzejNPC = new SpeakerNPC("Gazda Jędrzej") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(31, 125));
				nodes.add(new Node(35, 125));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w miejscu nękanym przez hordy zbójników.");
				addJob("Czekam na silnych wojowników, aby pokonali #zbójników grasujących w naszej okolicy.");
				addReply(Arrays.asList("misyjkę dla prawdziwego rycerza", "misyjka dla prawdziwego rycerza"), "Ach ten Andrzej z tymi swoimi hasłami. He he he. Wystarczy, że powiesz #zadanie.");
				addReply(Arrays.asList("zbójnik", "zbójników"), "Przybyli nie wiadomo skąd i zaczęli siać strach i chaos. Czy pomożesz nam z tym problemem i wykonasz to #zadanie?");
				addHelp("Przedtem prowadziliśmy spokojne i szczęśliwe życie, aż do czasu, gdy w naszej okolicy pojawili się wstrętni zbójnicy. Ciągle nas napadają i rabują z jedzenia. Teraz szukamy #prawdziwego #rycerza, który poradziłby sobie z tym ciężkim zadaniem.");
				addGoodbye("Życzę powodzenia i szczęścia na wyprawach.");
			}
		};

		gazdajedrzejNPC.setEntityClass("npcgazdajedrzej");
		gazdajedrzejNPC.setPosition(31, 125);
		gazdajedrzejNPC.initHP(1000);
		zone.add(gazdajedrzejNPC);
	}
}
