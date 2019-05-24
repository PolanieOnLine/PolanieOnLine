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
package games.stendhal.server.maps.ados.townhall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to keep track of all the traders in Faiumoni
 * This means players can come find prices of all items. 
 * The shop signs now have to be coded in XML not java because the implementation got moved over :(
 * So if you want to read them see data/conf/zones/ados.xml
 * @author kymara
 */
public class TaxmanNPC implements ZoneConfigurator {

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
		// Please change the NPCOwned Chest name if you change this NPC name.
		final SpeakerNPC npc = new SpeakerNPC("Mr Taxman") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 14));
				nodes.add(new Node(9, 14));
				nodes.add(new Node(9, 16));
				nodes.add(new Node(16, 16));
				nodes.add(new Node(9, 16));
				nodes.add(new Node(9, 14));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Czego chcesz?");
				addJob("Obliczam cło i podatki, które winien jest każdy handlarz w tym kraju. Muszę być ostrożny w przypadku osoby, która skupuje broń. Muszę być jeszcze bardziej ostrożny, ponieważ pobieram #opłatę od właścicieli domów.");
				addHelp("Domyślam się, że zastanawiasz się co ten chaos tutaj robi. Cóż, każda księga, którą widzisz dotyczy różnych sklepów lub handlarzy. Zapisuje w niej jaki podatek musi zapłacić właściciel sklepu. Nie wsadzaj nosa do nich to jest prywatny interes!");
				addOffer("Ja? Handluję? Musiałeś się pomylić! Jestem poborcą podatkowym. Moim zadaniem jest kontrolowanie handlarzy w tym kraju. Dlatego mam tyle otwartych ksiąg. Muszę wiedzieć co dany sprzedawca robi.");
				addQuest("Zapytaj piętro wyżej Mayor Chalmersa o potrzeby Ados.");
				addGoodbye("Dowidzenia - i nie myśl nawet o próbie zaglądnięcia w księgi!");
			}
		};
		npc.setDescription("Oto siejący postrach, bezwzględny Poborca Podatkowy.");
		npc.setEntityClass("taxmannpc");
		npc.setPosition(2, 14);
		npc.initHP(100);
		zone.add(npc);


	}
}
