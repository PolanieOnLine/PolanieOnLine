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
package games.pol.server.maps.zakopane.city;

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
 * @author Legolas
 */
public class GazdaJedrzejNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Gazda Jędrzej") {

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

		npc.setDescription("Oto gazda Jędrzej. Wygląda na bardzo smutnego, jakby potrzebował pomocy.");
		npc.setEntityClass("npcgazdajedrzej");
		npc.setGender("M");
		npc.setPosition(31, 125);
		zone.add(npc);
	}
}
