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
package games.stendhal.server.maps.kalavan.cottage_2;

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
 * Provides Gertha, the cherry pies confectioner NPC.
 * She has a twin sister: Martha, the apple pies confectioner NPC.
 *
 * @author omero
 */
public class ConfectionerCherryPieNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Gertha") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 7));
				nodes.add(new Node(4, 7));
				nodes.add(new Node(9, 7));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(6, 3));
				nodes.add(new Node(10, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Mieszkam tutaj z moja bliźniaczą siostrą #Martha. Naszą pasją jest wypiekanie pysznych ciast owocowych!");

				addReply("Martha",
					"Jest moją bliźniaczą siostrą, z którą razem mieszkam... Jak ja także lubi piec owocowe placki! Powiedz #upiecz jeśli się zdecydujesz.");
				addReply("miód",
					"Spróbuj zapytać #Marthę gdzie go zdobywa.");
				addReply("mleko",
					"Sądzę, że mleko możesz znaleźć na farmie!");
				addReply("mąka",
					"Poszukałabym trochę w młynie...");
				addReply("jajo",
					"Jak znajdziesz kilka kur to znajdziesz też trochę jaj!");
				addReply("wisienka",
					"Mmm... Czasami ciężko je zdobyć. Pytałeś się o nie w tawernie?");

				addHelp("Jeżeli to pomoże to mogę upiec ciasto z wiśniami dla Ciebie!");
				addOffer("Kocham piec pyszne ciasta z wiśniami. Poproś mnie mówiąc #upiecz!");
				addQuest("Bardzo bym chciała spróbować i upiec ciasto z truskawkami... Ale niestey! truskawek nigdzie nie można dostać...");
				addGoodbye("Trzymaj się!");
			}
		};

		npc.setDescription("Oto Gertha. Kocha piec placek z wiśniami dla gości.");
		npc.setEntityClass("confectionercherrypienpc");
		npc.setGender("F");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(10, 6);
		zone.add(npc);
	}
}
