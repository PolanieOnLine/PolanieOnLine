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
package games.stendhal.server.maps.semos.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * The bakery chef. Father of the camping girl.
 * He makes sandwiches for players.
 * He buys cheese.
 *
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Leander") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15,3));
				nodes.add(new Node(15,8));
				nodes.add(new Node(13,8));
				nodes.add(new Node(13,10));
				nodes.add(new Node(10,10));
				nodes.add(new Node(10,12));
				nodes.add(new Node(7,12));
				nodes.add(new Node(7,10));
				nodes.add(new Node(3,10));
				nodes.add(new Node(3,4));
				nodes.add(new Node(5,4));
				nodes.add(new Node(5,3));
				nodes.add(new Node(5,4));
				nodes.add(new Node(15,4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Jam jest tutejszym piekarzem. Jedną z usług jakie prowadzę na tym terenie jest produkcja placków o niezwykłej nazwie #pizza . Zanim rozpętała się tu wojna, a oddziały z Ados zablokowały drogi wielu było chętnych na te smaczne placki. Ale dzięki temu mam więcej czasu na produkcję kanapek dla naszych drogich klientów, którzy chwalą sobie ich smak!");
				addHelp("Jeśli chcesz zarobić trochę dukatów wyświadcz mi #przysługę i dostarcz to ciasto co zwą #pizza . Moja siostra #Sally robiła to do tej pory, ale teraz jest gdzieś na biwaku.");
				addReply("chleb", "Tym w naszej firmie zajmuje się Erna. Podejdź do niej i porozmawiaj.");
				addReply("ser",
						"Z serem mamy spore trudności, bo mieliśmy niedawno plagę szczurów. Zastanawia mnie jak te wstrętne szkodniki zabrały wszystko ze sobą? Dlatego do kanapek dodajemy oscypek z mleka naszych owiec.");
				addReply("ham",
						"Cóż, wyglądasz mi na dzielnego łowcę. Może po prostu idź do lasu i zapoluj. Tylko nie przynoś mi tych małych kawałków mięsiwa i suchych steków. Do kanapek nadaje się tylko najwyższej klasy szynka!");
				addReply("Sally",
						"Moja siostra Sally może pomóc Ci w zdobyciu szynki. Natura to jej dom. Ostatnio doszły mnie słuchy, że rozbiła obóz na południe od Zamku Orril.");
				addReply(Arrays.asList("pizza", "pizzę"), "Potrzebuję kogoś, kto dostarczy pizzę. Może przyjmiesz to #zadanie.");
				addReply(Arrays.asList("sandwich", "sandwiches","kanapka","kanapki"),
						"Moje kanapki są smaczne i pożywne. Jeśli chcesz abym zrobił jedną dla Ciebie powiedz #'zrób <ilość> kanapka' .");
				addOffer("Moja #pizza potrzebuje sera, a my nie mamy żadnych zapasów. Kupię od Ciebie ser jeżeli chcesz #sprzedać.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Leander. Jego praca daje mu piękny zapach.");
		npc.setEntityClass("chefnpc");
		npc.setGender("M");
		npc.setPosition(15, 3);
		zone.add(npc);
	}
}
