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
package games.stendhal.server.maps.nalwor.bank;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BankTellerAdder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the nalwor bank npcs.
 *
 * @author kymara
 */
public class BankNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildoldNPC(zone);
		buildladyNPC(zone);
	}

	// TODO: He complains if someone steals something from his chest: they should be sent to elf jail.
	private void buildoldNPC(final StendhalRPZone zone) {
		final SpeakerNPC oldnpc = new SpeakerNPC("Grafindle") {
			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam. Jeżeli potrzebujesz #pomocy to pytaj.");
				addJob("Pracuję tutaj w banku.");
				addHelp("Pokój ma dwie skrzynie należące do tego banku i dwie skrzynie należące do banku w Semos.");
				addGoodbye("Do widzenia młodzieńcze.");
				//remaining behaviour defined in Take Gold for Grafindle quest
			}
		};

		oldnpc.setDescription("Oto Grafindle, który pracuje w banku Nalwor.");
		oldnpc.setEntityClass("elfbankeroldnpc");
		oldnpc.setGender("M");
		oldnpc.setDirection(Direction.DOWN);
		oldnpc.setPosition(13, 17);

		zone.add(oldnpc);
	}

	private void buildladyNPC(final StendhalRPZone zone) {
		final SpeakerNPC ladynpc = new SpeakerNPC("Nnyddion") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 30));
				nodes.add(new Node(16, 30));
				nodes.add(new Node(16, 31));
				nodes.add(new Node(17, 31));
				nodes.add(new Node(17, 30));
				nodes.add(new Node(26, 30));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w banku Nalwor. Jestem tutaj, aby #pomóc.");
				addHelp("Klienci mogą u mnie zdeponować swoje przedmioty w skrzyniach w tym pokoiku znajdującym się za mną. Dwie skrzynie po prawej są administrowane przez Semos.");
				addOffer("Mogę Ci #pomóc.");
				addJob("Pomagam klientom banku: elfom, a nawet ludziom.");
				addQuest("Niczego nie potrzebuję. Dziękuję.");
				addGoodbye("Do widzenia. Dziękuje za twój czas.");
			}
		};

		ladynpc.setDescription("Oto Nnyddion, piękna elfka w pięknej sukni.");
		ladynpc.setEntityClass("elfbankladynpc");
		ladynpc.setGender("F");
		ladynpc.setDirection(Direction.DOWN);
		ladynpc.setPosition(17, 31);

		// manage bank account balance
		BankTellerAdder.addTeller(ladynpc);

		zone.add(ladynpc);
	}
}
