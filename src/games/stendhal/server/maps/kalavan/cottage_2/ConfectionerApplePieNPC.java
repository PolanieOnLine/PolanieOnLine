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
 * Provides Martha, the apple pies confectioner NPC.
 * She has a twin sister: Gertha, the cherry pies confectioner NPC.
 *
 * @author omero
 */
public class ConfectionerApplePieNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Martha") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 3));
				nodes.add(new Node(3, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(3, 11));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Mieszkam tutaj z moja bliźniaczą siostrą #Gertha. Naszą pasją jest wypiekanie pysznych ciast z owocami!");

				addReply("gertha",
					"Jest moją bliźniaczą siostrą, z którą razem mieszkam... Jak ja także lubi piec ciasta z owocami! Powiedz #upiecz jeśli się zdecydujesz.");
				addReply("miód",
					"Powinineś znaleźć tutejszego pszczelarza jest trochę na północny-zachód stąd...");
				addReply("mleko",
					"Powinieneś odwiedzić farmę. Znajduje się tam gdzie zobyczysz krowy...");
				addReply("mąka",
					"Ahh... Zdobywam mąkę z młyna, który jest na północ od miasta Semos!");
				addReply("jajo",
					"Jak znajdziesz kilka kur to znajdziesz też trochę jaj!");
				addReply("jabłko",
					"Mmm... Gdy od czasu do czasu podróżuje z Semos do Ados to zawsze zatrzymuje się w sadzie obok farmy przy drodze...");

				addHelp("Jeżeli to pomoże to mogę upiec jabłecznik dla Ciebie!");
				addOffer("Kocham #piec ciasta z jabłkami. Poproś mnie mówiąc #upiecz!");
                /** this is a teaser for a quest not yet available */
				addQuest("Teraz dopracowuję przepis na mój jabłecznik, ale w przyszłości możliwe, że będę chciała spróbować czegoś nowego. Dam ci znać.");
				addGoodbye("Uważaj na siebie!");
			}
		};

		npc.setDescription("Oto Martha. Kocha piec placki z jabłkami dla gości.");
		npc.setEntityClass("confectionerapplepienpc");
		npc.setGender("F");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(4, 3);
		zone.add(npc);
	}
}
