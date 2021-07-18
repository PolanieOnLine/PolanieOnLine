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
package games.stendhal.server.maps.deniran.cityoutside;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Provides a grocery seller in Deniran Marketplace 
 *
 * @author omero
 *
 */
public class DeniranMarketGrocerySellerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] yells = {
			"HEYOH! Artykuły spożywcze tutaj na rynku... Zbliż się, spójrz!",
			"HOYEH! Mam wszystko, aby przygotować skromny posiłek i jeszcze więcej!",
			"HAYAH! Czy to rynek czy cmentarz?!... Wydaje się, że jest tu tak cicho..."
		};
		new MonologueBehaviour(buildNPC(zone), yells, 3);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		//Ambrogio is a temporary name
		final SpeakerNPC npc = new SpeakerNPC("Ambrogio") {
			@Override
			public void createDialog() {
				addGreeting(
						"Witaj zwiedzaczu! Nie wyglądasz znajomo..." +
						"Jeśli przyszedłeś szukać jedzenia, #oferuję przedmioty spożywcze... " + 
						"Och, naprawdę powinienem założyć jedną z tych tablic, na których są wymienione oferty!");
				addOffer(
						"Och, głównie sprzedaję przedmioty spożywcze... " +
								"Jaja, ziemniaki, dobre fasolki pinto, " +
								"prawdziwą paprykę habanero (naprawdę OSTRE rzeczy), " +
								"trochę oliwy z oliwek lub ocet... " +
								"i cukier oczywiście... Mam tego mnóstwo! " +
								"Oraz miód też jeżeli naprawdę tego potrzebujesz..." +
						"Jeśli chcesz coś #kupić, to powiedz mi czego potrzebujesz... " +
						"Och, naprawdę powinienem założyć jedną z tych tablic, na których są wymienione oferty!");
				//Offered items:
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("jajo", 5);
                offerings.put("ziemniaki", 5);
                offerings.put("fasola pinto", 5);
                offerings.put("papryka habanero", 25);
                offerings.put("oliwa z oliwek", 130);
                offerings.put("ocet", 135);
                offerings.put("kekik", 135);
                offerings.put("sclaria", 135);
                offerings.put("cukier", 250);
                offerings.put("miód", 350);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);

				addJob("Jestem tutaj, aby #oferować przedmioty spożywcze podróżnym takim jak ty... " +
					   "Jeśli chcesz #kupić coś, powiedz mi... " +
					   "Och, naprawdę powinienem założyć jedną z tych tablic, na których są wymienione oferty!");
				addHelp(
						"Jeśli przyszedłeś szukać jedzenia, #oferuję przedmioty spożywcze... " +
						"Jeśli chcesz #kupić coś, powiedz mi... " +
						"Och, naprawdę powinienem założyć jedną z tych tablic, na których są wymienione oferty!");
				addGoodbye("Tak długo...");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(29, 120));
				nodes.add(new Node(20, 121));
				nodes.add(new Node(20, 118));
				nodes.add(new Node(23, 118));
				nodes.add(new Node(23, 116));
				nodes.add(new Node(29, 116));
				setPath(new FixedPath(nodes, true));
			}
		};

		// Finalize Deniran Market Seller NPC (Grocery)
		//npc.setEntityClass("fatsellernpc");
		npc.setEntityClass("deniran_marketsellernpc1grocery");
		npc.setDescription("Oto Ambrogio, zapracowany sprzedawca na rynku.");
		npc.setGender("M");
		npc.setPosition(26, 122);
		npc.setCollisionAction(CollisionAction.REVERSE);
		zone.add(npc);

		return npc;
	}
}
