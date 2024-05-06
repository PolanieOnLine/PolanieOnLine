/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.wieliczka.blacksmith;

import java.util.Arrays;
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
public class KowalWincentyNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKuznia(zone);
	}

	private void buildKuznia(final StendhalRPZone zone) {
		final SpeakerNPC kuznia = new SpeakerNPC("Kowal Wincenty") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 5));
				nodes.add(new Node(5, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem tutejszym kowalem.");
				addHelp("Produkuję bełty do kuszy, a mój znajomy zajmuje się produkcją strzał z mithrilu.");
				addOffer("Mogę wyprodukować Tobie bełty do kuszy, powiedz tylko #'zrób bełt'. Będę potrzebował 2 #polano oraz #żelazo.");
				addGoodbye();

				addReply(Arrays.asList("polano", "drewno"),
						"Polano znajdziesz na obrzeżach lasów. Potrzebne mi są do podtrzymywania ognia czy wykonania rękojeści.");
				addReply(Arrays.asList("kowalem Andrzejem", "kowalem", "kowal", "andrzejem", "andrzej"),
						"Zajmuje się odlewaniem żelaza, znajduje się w swojej kuźni w Zakopcu.");
				addReply(Arrays.asList("bełt", "bełty"),
						"Jest to pocisk przypominający strzałę, używany do strzelania z kuszy.");
				addReply(Arrays.asList("ruda", "sztabka żelaza", "ruda żelaza"),
						"Rudę żelaza znajdziesz w górach Zakopanego i Kościeliska. Uważaj tam na siebie!");
			}
		};

		kuznia.setDescription("Oto kowal Wincenty. Jest strasznie zapracowany!");
		kuznia.setEntityClass("goldsmithnpc");
		kuznia.setGender("M");
		kuznia.setDirection(Direction.DOWN);
		kuznia.setPosition(5, 5);
		zone.add(kuznia);
	}
}
